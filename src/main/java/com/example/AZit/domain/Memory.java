package com.example.AZit.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "memory")
@Builder
public class Memory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="memory_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @OneToOne(mappedBy = "memory", fetch = FetchType.LAZY)
    private Elements element;

    @Column(name="answer1",nullable = false,columnDefinition = "TEXT")
    private String answer1;

    @Column(name="answer2",nullable = false,columnDefinition = "TEXT")
    private String answer2;

    @Column(name="answer3",nullable = false,columnDefinition = "TEXT")
    private String answer3;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
