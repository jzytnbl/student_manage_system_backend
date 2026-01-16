package com.zwz.studentmanagebackend.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class StudentDTO {
    private Long id;

    @NotBlank(message = "学号不能为空")
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private Integer age;
    private String gender;
    private String email;
    private String phone;
    private String address;
    private Long classId;
}