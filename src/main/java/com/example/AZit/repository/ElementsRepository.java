package com.example.AZit.repository;

import com.example.AZit.domain.Elements;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ElementsRepository extends JpaRepository<Elements,Long> {
}
