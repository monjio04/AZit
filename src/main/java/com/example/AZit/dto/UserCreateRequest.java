package com.example.AZit.dto;


import com.example.AZit.domain.User;

public class UserCreateRequest {
    private String nickName;
    private String email;

    public User toEntity() {
        return User.builder()
                .nickName(nickName)
                .email(email)
                .build();
    }
}
