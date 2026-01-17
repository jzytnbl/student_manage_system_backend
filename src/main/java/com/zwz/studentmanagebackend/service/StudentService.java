package com.zwz.studentmanagebackend.service;

import com.zwz.studentmanagebackend.dto.StudentDTO;
import com.zwz.studentmanagebackend.dto.UserDTO;
import com.zwz.studentmanagebackend.entity.Student;
import com.zwz.studentmanagebackend.entity.User;
import com.zwz.studentmanagebackend.mapper.StudentMapper;
import com.zwz.studentmanagebackend.repository.StudentRepository;
import com.zwz.studentmanagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private StudentMapper studentMapper;

    @Autowired
    private UserRepository userRepository;  // 新增

    @Autowired
    private PasswordEncoder passwordEncoder;  // 新增

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

    // 保存学生，同时自动创建用户账户
    @CacheEvict(value = "student", allEntries = true)
    public Student saveStudent(StudentDTO studentDTO) {
        // 1. 检查学号是否已存在
        if (studentRepository.findByStudentNo(studentDTO.getStudentNo()).isPresent()) {
            throw new RuntimeException("学号已存在");
        }

        // 2. 创建学生信息
        Student student = new Student();
        student.setStudentNo(studentDTO.getStudentNo());
        student.setName(studentDTO.getName());
        student.setAge(studentDTO.getAge());
        student.setGender(studentDTO.getGender());
        student.setEmail(studentDTO.getEmail());
        student.setPhone(studentDTO.getPhone());
        student.setAddress(studentDTO.getAddress());
        student.setClassId(studentDTO.getClassId());

        Student savedStudent = studentRepository.save(student);

        // 3. 自动创建对应的用户账户
        createUserAccountForStudent(savedStudent);

        // 4. 发送Kafka消息
        kafkaService.sendStudentEvent("student.created", savedStudent);

        // 5. 使用Redis记录操作日志
        redisService.addStudentLog(savedStudent.getId(), "创建学生");

        return savedStudent;
    }

    // 自动为学生创建用户账户
    private void createUserAccountForStudent(Student student) {
        try {
            // 检查是否已存在相同用户名的账户
            if (userRepository.findByUsername(student.getStudentNo()).isPresent()) {
                System.out.println("用户账户已存在，学号: " + student.getStudentNo());
                return;
            }

            // 创建用户DTO
            User user = new User();
            user.setUsername(student.getStudentNo()); // 学号作为用户名

            // 默认密码：123456
            String defaultPassword = "123456";
            user.setPassword(passwordEncoder.encode(defaultPassword));

            user.setRole("STUDENT");
            user.setEmail(student.getEmail());
            user.setPhone(student.getPhone());

            // 保存用户
            User savedUser = userRepository.save(user);

            // 可选：在学生表中记录用户ID
            // student.setUserId(savedUser.getId());
            // studentRepository.save(student);

            System.out.println("已自动创建学生用户账户: " + student.getStudentNo() +
                    ", 默认密码: " + defaultPassword);

        } catch (Exception e) {
            // 如果创建用户失败，记录日志但不影响学生创建
            System.out.println("自动创建用户账户失败，学号: " + student.getStudentNo() +
                    ", 错误: " + e.getMessage());
        }
    }

    // 删除学生
    @CacheEvict(value = "student", key = "#id")
    public void deleteStudent(Long id) {
        Student student = studentRepository.findById(id).orElse(null);
        if (student != null) {
            // 可选：删除对应的用户账户
            deleteUserAccountForStudent(student);

            studentRepository.deleteById(id);

            // 发送Kafka消息
            kafkaService.sendStudentEvent("student.deleted", id.toString());
        }
    }

    // 删除学生对应的用户账户
    private void deleteUserAccountForStudent(Student student) {
        try {
            userRepository.findByUsername(student.getStudentNo())
                    .ifPresent(user -> {
                        userRepository.delete(user);
                        System.out.println("已删除学生用户账户: " + student.getStudentNo());
                    });
        } catch (Exception e) {
            System.out.println("删除用户账户失败，学号: " + student.getStudentNo());
        }
    }
}