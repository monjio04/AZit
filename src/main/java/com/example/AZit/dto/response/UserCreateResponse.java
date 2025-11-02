package com.example.AZit.dto.response;

import com.example.AZit.domain.Users;
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

    public static UserCreateResponse of(Users users){
        return  UserCreateResponse.builder()
                .id(users.getId())
                .nickName(users.getNickName())
                .email(users.getEmail())
                .build();
    }
}
