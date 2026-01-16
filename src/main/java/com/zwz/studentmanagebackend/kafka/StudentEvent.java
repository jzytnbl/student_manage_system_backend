package com.zwz.studentmanagebackend.kafka;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentEvent {
    private String eventType;
    private Long studentId;
    private String studentNo;
    private String studentName;
    private LocalDateTime eventTime;

    public StudentEvent() {
        this.eventTime = LocalDateTime.now();
    }
}