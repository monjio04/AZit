package com.example.AZit.service;

import com.example.AZit.domain.Songs;
import com.example.AZit.dto.response.ArchiveAllResponse;
import com.example.AZit.repository.ElementsRepository;
import com.example.AZit.repository.SongsRepository;
import com.example.AZit.repository.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ArchiveService {
    private final UserRepository userRepository;
    private final ElementsRepository elementsRepository;
    private final SongsRepository songsRepository;

    @Transactional(readOnly = true)
    public List<ArchiveAllResponse> getAllArchive() {

        List<Songs> songsList = songsRepository.findAllWithUserAndElementAndKeywords();

        if (songsList == null || songsList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다.");
        }

        return songsList.stream()
                .map(this::toArchiveAllResponse)
                .collect(Collectors.toList());
    }

    private ArchiveAllResponse toArchiveAllResponse(Songs s) {
        return new ArchiveAllResponse(
                s.getId(),
                new ArchiveAllResponse.ArchiveUserDto(
                        s.getElement().getMemory().getUsers().getId(),
                        s.getElement().getMemory().getUsers().getNickName()
                ),
                new ArchiveAllResponse.ArchiveElementDto(
                        s.getElement().getId(),
                        s.getElement().getKeywords()
                ),
                s.getSvgUrl()
        );
    }

}
