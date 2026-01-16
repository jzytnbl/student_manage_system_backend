package com.zwz.studentmanagebackend.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@Aspect
@Component
public class RateLimitAspect {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Pointcut("@annotation(com.zwz.studentmanagebackend.aop.RateLimit)")
    public void rateLimitPointcut() {}

    @Around("rateLimitPointcut()")
    public Object rateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();
        String ip = getClientIp(request);
        String method = joinPoint.getSignature().toShortString();

        // 滑动窗口限流
        String key = "rate:limit:" + ip + ":" + method;
        long currentTime = System.currentTimeMillis();
        long windowSize = 60000; // 1分钟
        long maxRequests = 100;  // 最大100次

        redisTemplate.opsForZSet().add(key, String.valueOf(currentTime), currentTime);
        redisTemplate.opsForZSet().removeRangeByScore(key, 0, currentTime - windowSize);
        Long count = redisTemplate.opsForZSet().zCard(key);

        redisTemplate.expire(key, 2, TimeUnit.MINUTES);

        if (count != null && count > maxRequests) {
            throw new RuntimeException("请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip != null ? ip.split(",")[0] : "unknown";
    }
}