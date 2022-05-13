package com.personalproject.homepage.entity;

import java.time.LocalDateTime;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public class CommonEntity {

    private LocalDateTime createAt;

    private LocalDateTime updateAt;

    @PrePersist
    public void create() {
        this.setCreateAt(LocalDateTime.now());
    }

    @PreUpdate
    public void update() {
        this.setUpdateAt(LocalDateTime.now());
    }
}
