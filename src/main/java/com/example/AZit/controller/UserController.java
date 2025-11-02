package com.example.AZit.controller;

import com.example.AZit.dto.response.ApiResponse;
import com.example.AZit.dto.request.UserCreateRequest;
import com.example.AZit.dto.response.UserCreateResponse;
import com.example.AZit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody UserCreateRequest request) {
        UserCreateResponse response=userService.createUser(request);
        return ResponseEntity.ok(new ApiResponse(true,201,"유저 등록 완료",response));
    }
}
