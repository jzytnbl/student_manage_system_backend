package com.zwz.studentmanagebackend.service;

import com.zwz.studentmanagebackend.dto.UserDTO;
import com.zwz.studentmanagebackend.entity.User;
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
