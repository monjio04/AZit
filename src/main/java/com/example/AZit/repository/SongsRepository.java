package com.example.AZit.repository;

import com.example.AZit.domain.Songs;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SongsRepository extends JpaRepository<Songs, Long> {

    @Query("select distinct s from Songs s " +
            "join fetch s.element e " +
            "join fetch e.memory m " +
            "join fetch m.users u " +
            "order by s.createdAt desc")
    List<Songs> findAllWithUserAndElementAndKeywords();


    @EntityGraph(attributePaths = {
            "element",
            "element.memory",
            "element.memory.users"
    })
    @Query("SELECT s FROM Songs s WHERE s.id = :id")
    Optional<Songs> findDetailById(Long id);


}
