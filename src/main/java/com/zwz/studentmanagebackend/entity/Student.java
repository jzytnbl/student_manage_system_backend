package com.zwz.studentmanagebackend.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String studentNo;

    @Column(nullable = false)
    private String name;

    private Integer age;
    private String gender;
    private String email;
    private String phone;
    private String address;

    @Column(name = "class_id")
    private Long classId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}