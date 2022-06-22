package com.personalproject.homepage.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;

/********************************************************************************
    @NoRepositoryBean
        - 중계자 역할을 하는 interface로 설정하여 bean을 생성하지 않는다.
    https://spring.io/blog/2022/02/22/announcing-listcrudrepository-friends-for-spring-data-3-0
        - findAll 반환타입을 List로 받기 위한 방법.
        - 왜 반환타입이 Iterable인가?

    JPA는 scan한 repository를 상속한 Proxy 클래스를 만들어 bean으로 등록한다.
    아래 interface를 상속받은 interface에서 @Repository를 붙이지 않아도 의존성 주입이 가능하다.

    최소한의 기능을 가진 interface를 상속받고 필요한 기능이 있으면 추가하려 한다.
********************************************************************************/
@NoRepositoryBean
public interface CommonRepository<T, ID> extends Repository<T, ID> {

    <S extends T> S save(S entity);

    Optional<T> findById(Long id);

    List<T> findAll();

    void delete(T entity);
}
