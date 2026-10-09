package com.cmai.service;

import com.cmai.dto.UserDto;
import com.cmai.exception.ApiException;
import com.cmai.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository users;

    public UserService(UserRepository users) {
        this.users = users;
    }

    public UserDto get(Long userId) {
        return users.findById(userId)
                .map(UserDto::from)
                .orElseThrow(() -> new ApiException(404, "User not found"));
    }
}
