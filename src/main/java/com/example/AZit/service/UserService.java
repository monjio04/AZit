package com.example.AZit.service;

import com.example.AZit.domain.Users;
import com.example.AZit.dto.request.UserCreateRequest;
import com.example.AZit.dto.response.UserCreateResponse;
import com.example.AZit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserCreateResponse createUser(UserCreateRequest request){

        Users user = Users.builder()
                .nickName(request.getNickName())
                .email(request.getEmail())
                .build();

        userRepository.save(user);

        return UserCreateResponse.of(user);
    }


}
