package com.personalproject.homepage.entity;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Builder;
import lombok.Getter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "parent_category_idx" }))
@Getter
@DynamicInsert
@DynamicUpdate
public class Category extends CommonEntity {

    private String name;

    @ManyToOne
    @JoinColumn(name = "parent_category_idx")
    private Category parentCategory;

    @OneToMany(mappedBy = "parentCategory")
    private Set<Category> categoriesOfCategory = new HashSet<>();

    @OneToMany(mappedBy = "category")
    private Set<Post> postsOfCategory = new HashSet<>();

    /********************************************************************************
        https://docs.jboss.org/hibernate/orm/5.4/quickstart/html_single/

        Invisible no args constructor prevents creating Entity instance via new keyword causing null fields.
        To get new a instance, call Builder class.
        Invoke updateInfo method in order to update existent instance.
    ********************************************************************************/
    Category() {/** empty */}

    @Builder
    private Category (String name, Category parentCategory) {
        checkArgument(name != null, "Category Entity name must not be null");
        this.name = name;
        this.parentCategory = parentCategory;
    }

    public void updateInfo(String name, Category parentCategory) {
        // args could be nullable because it's just update
        if (name != null) this.name = name;
        if (parentCategory != null) setParentCategory(parentCategory);
    }

    /********************************************************************************
        엔티티 간 @OneToMany, @ManyToOne 두 개의 단향향 관계를 통한
        양방향 관계 설정 시 객체의 관점에서 연관 관계를 매핑해주기 위한 메소드
        RDBMS에선 외래키를 통해 관계를 관리 하지만
        객체의 관점에선 각각이 서로에 대한 정보만 가지고 있으므로
        mappedBy owner를 가진 엔티티에서 그 관계를 설정해 주어야 한다.
        ********************************************************************************/
    private void setParentCategory(Category parentCategory) {
        // 매핑 되어있는 관계를 끊는다.
        if (this.parentCategory != null) {
            this.parentCategory.getCategoriesOfCategory().remove(this);
        }
        // 양쪽 객체에 새로운 관계를 맺는다.
        this.parentCategory = parentCategory;
        parentCategory.getCategoriesOfCategory().add(this);
    }
}
