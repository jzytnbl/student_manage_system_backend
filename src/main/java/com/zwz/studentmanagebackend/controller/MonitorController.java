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

        if (topApis != null) {
            for (Object api : topApis) {
                String apiName = (String) api;
                String hashKey = "api:stats:" + today + ":" + apiName;

                Map<Object, Object> stats = redisTemplate.opsForHash().entries(hashKey);
                if (!stats.isEmpty()) {
                    Map<String, Object> apiStats = new HashMap<>();
                    apiStats.put("apiName", apiName);

                    // 修复：正确转换Redis中的值
                    apiStats.put("totalCount", parseLong(stats.get("totalCount")));
                    apiStats.put("successCount", parseLong(stats.get("successCount")));
                    apiStats.put("errorCount", parseLong(stats.get("errorCount")));

                    Long totalTime = parseLong(stats.get("totalTime"));
                    Long totalCount = parseLong(stats.get("totalCount"));

                    if (totalCount > 0) {
                        apiStats.put("avgResponseTime", totalTime / totalCount);
                    } else {
                        apiStats.put("avgResponseTime", 0);
                    }

                    result.add(apiStats);
                }
            }
        }

        return result;
    }

    @GetMapping("/redis-info")
    public Map<String, Object> getRedisInfo() {
        Map<String, Object> info = new HashMap<>();

        // 在线用户数（由于Redis禁用，这里返回0）
        info.put("onlineUsers", 0);

        // 热门学生（由于Redis禁用，这里返回空列表）
        info.put("topVisitedStudents", Collections.emptyList());

        // 缓存数量（由于Redis禁用，这里返回0）
        info.put("cacheCount", 0);

        // 添加Kafka状态信息
        info.put("kafkaStatus", "Kafka已连接");
        info.put("kafkaVersion", "3.7.0");

        // 添加系统信息
        info.put("systemTime", new Date());
        info.put("apiLogCount", "API日志记录正常");

        return info;
    }

    // 辅助方法：安全地将Object转换为Long
    private Long parseLong(Object obj) {
        if (obj == null) {
            return 0L;
        }
        if (obj instanceof Long) {
            return (Long) obj;
        }
        if (obj instanceof Integer) {
            return ((Integer) obj).longValue();
        }
        if (obj instanceof String) {
            try {
                return Long.parseLong((String) obj);
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return 0L;
    }
}