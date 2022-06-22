package com.personalproject.homepage.dto;

import lombok.Getter;

@Getter
public class LoginResultDto {

    private String id;

    public LoginResultDto(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "id: " + id;
    }
}
