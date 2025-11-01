package com.example.AZit.domain;

import com.example.AZit.util.StringListConverter;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "elements")
public class Elements {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="element_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="memory_id",nullable = false,unique = true)
    private Memory memory;

    @OneToOne(mappedBy = "song", fetch = FetchType.LAZY)
    private Songs songs;

    @Column(name="mood",nullable = false)
    private String mood;

    @Column(name = "scale",nullable = false)
    private String scale;

    @Column(name="tempo",nullable = false)
    private String tempo;

    @Column(name="atmosphere",nullable = false)
    private String atmosphere;

    @Column(name="instruments",nullable = false)
    private String instruments;

    @Column(name="length",nullable = false)
    private String length;

    @Column(name="pitch_range",nullable = false)
    private String pitchRange;

    @Column(name="texture",nullable = false)
    private String texture;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT",nullable = false)
    private List<String> keywords;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public void setMemory(Memory memory) {
        this.memory = memory;
    }
}
