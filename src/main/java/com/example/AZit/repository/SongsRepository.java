package com.example.AZit.repository;

import com.example.AZit.domain.Songs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SongsRepository extends JpaRepository<Songs, Long> {

    @Query("select distinct s from Song s " +
            "join fetch s.user u " +
            "join fetch s.element e " +
            "left join fetch e.keywords k " +
            "order by s.createdAt desc")
    List<Songs> findAllWithUserAndElementAndKeywords();
}
