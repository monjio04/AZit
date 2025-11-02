package com.example.AZit.service;

import com.example.AZit.domain.Memory;
import com.example.AZit.domain.Users;
import com.example.AZit.dto.request.MemoryRequest;
import com.example.AZit.dto.response.MemoryCreateResponse;
import com.example.AZit.repository.MemoryRepository;
import com.example.AZit.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemoryService {
    private final MemoryRepository memoryRepository;
    private final UserRepository userRepository;

    public MemoryCreateResponse createMemory(Long userId, MemoryRequest request){
        Users user = userRepository.findById(userId)
                .orElseThrow(()->new IllegalArgumentException("존재하지 않는 user입니다."));

        Memory memory=Memory.builder()
                .users(user)
                .answer1(request.getAnswer1())
                .answer2(request.getAnswer2())
                .answer3(request.getAnswer3())
                .build();

        memoryRepository.save(memory);

        return new MemoryCreateResponse(
                memory.getId(),
                memory.getCreatedAt()
        );
    }

}
