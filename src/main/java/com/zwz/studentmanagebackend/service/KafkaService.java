package com.zwz.studentmanagebackend.service;

import com.alibaba.fastjson.JSON;
import com.zwz.studentmanagebackend.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    // 修改：接受Student对象
    public void sendStudentEvent(String eventType, Student student) {
        String message = JSON.toJSONString(student);
        String kafkaMessage = String.format("{\"event\":\"%s\",\"data\":%s,\"timestamp\":%d}",
                eventType, message, System.currentTimeMillis());
        kafkaTemplate.send("student-events", kafkaMessage);
    }

    // 新增：接受学生ID
    public void sendStudentEvent(String eventType, Long studentId) {
        String message = String.format("{\"event\":\"%s\",\"studentId\":%d,\"timestamp\":%d}",
                eventType, studentId, System.currentTimeMillis());
        kafkaTemplate.send("student-events", message);
    }

    // 新增：接受原始字符串
    public void sendStudentEvent(String eventType, String data) {
        String message = String.format("{\"event\":\"%s\",\"data\":\"%s\",\"timestamp\":%d}",
                eventType, data, System.currentTimeMillis());
        kafkaTemplate.send("student-events", message);
    }

    // 用户事件方法
    public void sendUserEvent(String eventType, String data) {
        String message = String.format("{\"event\":\"%s\",\"data\":\"%s\",\"timestamp\":%d}",
                eventType, data, System.currentTimeMillis());
        kafkaTemplate.send("user-events", message);
    }
}