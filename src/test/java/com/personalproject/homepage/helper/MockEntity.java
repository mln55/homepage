package com.personalproject.homepage.helper;

import java.lang.reflect.Constructor;

import com.personalproject.homepage.entity.CommonEntity;

import org.springframework.test.util.ReflectionTestUtils;

/********************************************************************************
    A class that help create entity instance in test environment.
    This class has two static method
        - return entity instance which inherit CommonEntity according to input class type
********************************************************************************/
public class MockEntity {
    public static <T extends CommonEntity> T mock(Class<T> clazz, Long id) {
        try {
            Constructor<T> entityClass = clazz.getDeclaredConstructor(); // no args constructor
            entityClass.setAccessible(true); // 접근 가능하게 변경
            T entity = entityClass.newInstance();
            ReflectionTestUtils.setField(entity, "idx", id);
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T extends CommonEntity> T mock(Class<T> clazz) {
        return mock(clazz, null);
    }
}
