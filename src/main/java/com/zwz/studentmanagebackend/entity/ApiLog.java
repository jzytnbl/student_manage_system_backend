package com.zwz.studentmanagebackend.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "api_logs")
public class ApiLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "api_name")
    private String apiName;

    @Column(name = "method_name")
    private String methodName;

    @Column(name = "user_id")
    private Long userId;

    private String username;
    private String ip;

    @Column(name = "request_time")
    private LocalDateTime requestTime;

    @Column(name = "response_time")
    private Long responseTime; // ms

    private Boolean success;
    private String errorMsg;
}