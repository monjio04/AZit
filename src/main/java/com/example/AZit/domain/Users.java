package com.example.AZit.domain;


import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name="users")
@Builder
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="users_id")
    private Long id;

    @Column(name = "users_name",nullable = false)
    private String nickName;

    @Column(name="email")
    private String email;

    @OneToMany(mappedBy = "users", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Memory> memories = new ArrayList<>();

    @Builder
    public Users(String nickName, String email) {
        this.nickName = nickName;
        this.email = email;
    }

}
