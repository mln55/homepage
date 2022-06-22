package com.personalproject.homepage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/********************************************************************************
    @EnableJpaRepositories
        JPA rpository를 사용하기 위한 어노테이션.
        해당 어노테이션이 붙은 config class의 패키지를 스캔한다.
        생략 시 @SpringBootApplication 하위 패키지를 스캔.
    @WebMvcTest는 JPA 관련 bean을 생성하지 않으므로 이 어노테이션이 붙어있다면 에러가 발생한다.
    별다른 설정 없이 명시적으로 선언해 놓은 것이기에 주석처리 한다.
********************************************************************************/
@SpringBootApplication
// @EnableJpaRepositories(basePackageClasses = CommonRepository.class)
public class HomepageApplication {

    public static void main(String[] args) {
        SpringApplication.run(HomepageApplication.class, args);
    }

}
