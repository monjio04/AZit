package com.example.AZit.repository;

import com.example.AZit.domain.Songs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SongsRepository extends JpaRepository<Songs, Long> {
}
