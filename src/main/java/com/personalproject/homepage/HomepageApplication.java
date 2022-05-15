package com.personalproject.homepage;

import com.personalproject.homepage.repository.CommonRepository;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/********************************************************************************
    @EnableJpaRepositories
        JPA rpository를 사용하기 위한 어노테이션.
        해당 어노테이션이 붙은 config class의 패키지를 스캔한다.
        생략 시 @SpringBootApplication 하위 패키지를 스캔.
********************************************************************************/
@SpringBootApplication
@EnableJpaRepositories(basePackageClasses = CommonRepository.class)
public class HomepageApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomepageApplication.class, args);
    }

}
