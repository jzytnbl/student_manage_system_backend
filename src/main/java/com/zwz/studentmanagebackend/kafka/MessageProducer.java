package com.zwz.studentmanagebackend.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendStudentEvent(String eventType, String data) {
        String message = String.format("{\"event\":\"%s\",\"data\":\"%s\",\"timestamp\":%d}",
                eventType, data, System.currentTimeMillis());
        kafkaTemplate.send("student-events", message);
    }
}
