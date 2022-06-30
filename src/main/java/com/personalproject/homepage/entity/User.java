package com.personalproject.homepage.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class User {

    private String id;

    private String pw;

    private String roles;

}
