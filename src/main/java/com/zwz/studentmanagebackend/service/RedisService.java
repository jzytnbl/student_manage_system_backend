package com.zwz.studentmanagebackend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 新增：删除键
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }

    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // List: 学生访问日志
    public void addStudentLog(Long studentId, String operation) {
        String key = "student:logs:" + studentId;
        String log = System.currentTimeMillis() + ":" + operation;
        redisTemplate.opsForList().leftPush(key, log);
        redisTemplate.opsForList().trim(key, 0, 49);
    }

    // Set: 在线用户
    public void addOnlineUser(String username) {
        redisTemplate.opsForSet().add("online:users", username);
    }

    public Set<Object> getOnlineUsers() {
        return redisTemplate.opsForSet().members("online:users");
    }

    // ZSet: 学生访问排名
    public void incrementStudentVisit(Long studentId) {
        String key = "student:visit:ranking";
        redisTemplate.opsForZSet().incrementScore(key, studentId.toString(), 1);
    }

    public Set<Object> getTopVisitedStudents(int topN) {
        String key = "student:visit:ranking";
        return redisTemplate.opsForZSet().reverseRange(key, 0, topN - 1);
    }

    // Hash: 学生详情缓存
    public void cacheStudentDetail(Long studentId, Map<String, Object> detail) {
        String key = "student:detail:" + studentId;
        redisTemplate.opsForHash().putAll(key, detail);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }
}