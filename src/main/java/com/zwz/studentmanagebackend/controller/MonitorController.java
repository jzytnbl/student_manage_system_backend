package com.zwz.studentmanagebackend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/monitor")
public class MonitorController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/api-stats")
    public List<Map<String, Object>> getApiStatistics() {
        String today = LocalDate.now().toString();
        List<Map<String, Object>> result = new ArrayList<>();

        // 获取今日热门API
        String zsetKey = "api:ranking:" + today;
        Set<Object> topApis = redisTemplate.opsForZSet().reverseRange(zsetKey, 0, 9);

        for (Object api : topApis) {
            String apiName = (String) api;
            String hashKey = "api:stats:" + today + ":" + apiName;

            Map<Object, Object> stats = redisTemplate.opsForHash().entries(hashKey);
            if (!stats.isEmpty()) {
                Map<String, Object> apiStats = new HashMap<>();
                apiStats.put("apiName", apiName);
                apiStats.put("totalCount", stats.getOrDefault("totalCount", 0));
                apiStats.put("successCount", stats.getOrDefault("successCount", 0));
                apiStats.put("errorCount", stats.getOrDefault("errorCount", 0));

                Long totalTime = (Long) stats.getOrDefault("totalTime", 0L);
                Long totalCount = (Long) stats.getOrDefault("totalCount", 1L);
                apiStats.put("avgResponseTime", totalCount > 0 ? totalTime / totalCount : 0);

                result.add(apiStats);
            }
        }

        return result;
    }

    @GetMapping("/redis-info")
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();

        // 在线用户数
        Set<Object> onlineUsers = redisTemplate.opsForSet().members("online:users");
        info.put("onlineUsers", onlineUsers != null ? onlineUsers.size() : 0);

        // 热门学生
        Set<Object> topStudents = redisTemplate.opsForZSet().reverseRange("student:visit:ranking", 0, 9);
        info.put("topVisitedStudents", topStudents);

        // 缓存数量
        Long cacheCount = (long) redisTemplate.keys("student:*").size();
        info.put("cacheCount", cacheCount);

        return info;
    }
}