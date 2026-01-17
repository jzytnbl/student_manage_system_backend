package com.zwz.studentmanagebackend.service;

import com.zwz.studentmanagebackend.dto.UserDTO;
import com.zwz.studentmanagebackend.entity.User;
import com.zwz.studentmanagebackend.repository.StudentRepository;
import com.zwz.studentmanagebackend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaService kafkaService;

    public Page<User> getUsers(Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        return userRepository.findAll(pageable);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
    }

    public User createUser(UserDTO userDTO) {
        if (userRepository.existsByUsername(userDTO.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole());
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());

        User savedUser = userRepository.save(user);

        // 如果是学生角色，检查是否已有学生信息
        if ("STUDENT".equals(userDTO.getRole())) {
            try {
                // 检查是否已存在该学号的学生
                if (!studentRepository.findByStudentNo(userDTO.getUsername()).isPresent()) {
                    System.out.println("提示：已创建学生账户 " + userDTO.getUsername() +
                            "，但对应的学生信息需要单独创建");
                }
            } catch (Exception e) {
                // 忽略错误
            }
        }

        // 发送Kafka消息
        kafkaService.sendUserEvent("user.created", savedUser.getUsername());

        return savedUser;
    }
    public User updateUser(Long id, UserDTO userDTO) {
        User user = getUserById(id);
        user.setEmail(userDTO.getEmail());
        user.setPhone(userDTO.getPhone());
        user.setRole(userDTO.getRole());

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);

        // 清理Redis缓存
        String userKey = "user:" + user.getUsername();
        redisService.delete(userKey);

        // 发送Kafka消息
        kafkaService.sendUserEvent("user.deleted", user.getUsername());
    }
}
