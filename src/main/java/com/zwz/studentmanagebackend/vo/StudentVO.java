package com.zwz.studentmanagebackend.vo;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class StudentVO {
    private Long id;
    private String studentNo;
    private String name;
    private Integer age;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private Long classId;
    private String className;
    private LocalDateTime createdAt;
}