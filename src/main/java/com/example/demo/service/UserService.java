package com.example.demo.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.CreateUserRequest;
import com.example.demo.dto.UpdateUserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.exception.UsernameAlreadyExistsException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse addUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        User user = new User();
        user.setName(request.name());
        user.setAge(request.age());
        user.setUsername(request.username());
        user.setPassword(passwordEncoder.encode(request.password()));
        try {
            return toResponse(userRepository.save(user));
        } catch (DataIntegrityViolationException exception) {
            throw new UsernameAlreadyExistsException(request.username(), exception);
        }
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String username) {
        return toResponse(findUser(username));
    }

    @Transactional
    public UserResponse updateCurrentUser(String currentUsername, UpdateUserRequest request) {
        User user = findUser(currentUsername);
        if (!user.getUsername().equals(request.username()) && userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }
        user.setName(request.name());
        user.setAge(request.age());
        user.setUsername(request.username());
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteCurrentUser(String username) {
        userRepository.delete(findUser(username));
    }

    private User findUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getAge(), user.getUsername());
    }
}