package com.zwz.studentmanagebackend.service;

import com.zwz.studentmanagebackend.dto.StudentDTO;
import com.zwz.studentmanagebackend.entity.Student;
import com.zwz.studentmanagebackend.mapper.StudentMapper;
import com.zwz.studentmanagebackend.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaService kafkaService;

    // 使用JPA查询
    public Page<Student> getStudents(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        if (keyword != null && !keyword.isEmpty()) {
            return studentRepository.search(keyword, pageable);
        }
        return studentRepository.findAll(pageable);
    }

    // 使用MyBatis查询
    public List<Student> getStudentsByParams(Map<String, Object> params) {
        return studentMapper.selectStudents(params);
    }

    // 带缓存的学生详情
    @Cacheable(value = "student", key = "#id")
    public Student getStudentById(Long id) {
        return studentRepository.findById(id).orElse(null);
    }

    // 保存学生，同时发Kafka消息
    @CacheEvict(value = "student", allEntries = true)
    public Student saveStudent(StudentDTO studentDTO) {
        Student student = new Student();
        student.setStudentNo(studentDTO.getStudentNo());
        student.setName(studentDTO.getName());
        student.setAge(studentDTO.getAge());
        student.setGender(studentDTO.getGender());
        student.setEmail(studentDTO.getEmail());
        student.setPhone(studentDTO.getPhone());
        student.setAddress(studentDTO.getAddress());
        student.setClassId(studentDTO.getClassId());

        Student saved = studentRepository.save(student);

        // 发送Kafka消息
        kafkaService.sendStudentEvent("student.created", saved);

        // 使用Redis List记录操作日志
        redisService.addStudentLog(saved.getId(), "创建学生");

        return saved;
    }

    // 删除学生
    @CacheEvict(value = "student", key = "#id")
    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);

        // 发送Kafka消息
        kafkaService.sendStudentEvent("student.deleted", id.toString());
    }
}