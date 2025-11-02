package com.example.AZit.service;

import com.example.AZit.domain.User;
import com.example.AZit.dto.UserCreateRequest;
import com.example.AZit.dto.UserCreateResponse;
import com.example.AZit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserCreateResponse createUser(UserCreateRequest request){
        User user = request.toEntity();

        userRepository.save(user);

        return UserCreateResponse.of(user);
    }


}
