package com.personalproject.homepage.entity;

import java.time.LocalDateTime;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Getter;

@Getter
@MappedSuperclass
public class CommonEntity {

    /********************************************************************************
        Setting fields to private makes them unchangeable from inherited class.
    ********************************************************************************/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    protected CommonEntity() {/** empty */}

    @PrePersist
    private void create() {
        createAt = LocalDateTime.now();
    }

    @PreUpdate
    private void update() {
        updateAt = LocalDateTime.now();

    }
}
