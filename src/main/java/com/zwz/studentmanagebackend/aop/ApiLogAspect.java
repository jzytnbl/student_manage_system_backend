package com.zwz.studentmanagebackend.aop;

import com.zwz.studentmanagebackend.entity.ApiLog;
import com.zwz.studentmanagebackend.repository.ApiLogRepository;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class ApiLogAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ApiLogRepository apiLogRepository;

    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void apiPointcut() {}

    @Around("apiPointcut()")
    public Object logApi(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        // 获取请求信息
        ServletRequestAttributes attributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        // 获取用户信息
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonymous";

        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String apiName = className + "." + methodName;

        Object result;
        boolean success = true;
        String errorMsg = null;

        try {
            result = joinPoint.proceed();
        } catch (Exception e) {
            success = false;
            errorMsg = e.getMessage();
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            // 1. 统计到Redis
            String today = LocalDateTime.now().toLocalDate().toString();
            String hashKey = "api:stats:" + today + ":" + apiName;

            redisTemplate.opsForHash().increment(hashKey, "totalCount", 1);
            redisTemplate.opsForHash().increment(hashKey, "totalTime", responseTime);
            if (success) {
                redisTemplate.opsForHash().increment(hashKey, "successCount", 1);
            } else {
                redisTemplate.opsForHash().increment(hashKey, "errorCount", 1);
            }

            // 使用ZSet记录接口热度
            String zsetKey = "api:ranking:" + today;
            redisTemplate.opsForZSet().incrementScore(zsetKey, apiName, 1);

            // 设置过期时间（30天）
            redisTemplate.expire(hashKey, 30, TimeUnit.DAYS);
            redisTemplate.expire(zsetKey, 30, TimeUnit.DAYS);

            // 2. 保存到数据库
            ApiLog apiLog = new ApiLog();
            apiLog.setApiName(apiName);
            apiLog.setMethodName(methodName);
            apiLog.setUsername(username);
            apiLog.setRequestTime(LocalDateTime.now());
            apiLog.setResponseTime(responseTime);
            apiLog.setSuccess(success);
            apiLog.setErrorMsg(errorMsg);
            apiLogRepository.save(apiLog);
        }

        return result;
    }
}