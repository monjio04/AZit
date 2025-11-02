package com.example.AZit.dto;

import com.example.AZit.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UserCreateResponse {
    private final Long id;
    private final String nickName;
    private final String email;

    public static UserCreateResponse of(User user){
        return  UserCreateResponse.builder()
                .id(user.getId())
                .nickName(user.getNickName())
                .email(user.getEmail())
                .build();
    }
}
