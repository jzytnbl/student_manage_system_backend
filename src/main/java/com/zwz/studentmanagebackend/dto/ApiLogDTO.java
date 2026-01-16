package com.zwz.studentmanagebackend.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ApiLogDTO {
    private String apiName;
    private String username;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Boolean success;
    private Integer page = 1;
    private Integer size = 10;
}