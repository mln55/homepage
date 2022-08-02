package com.personalproject.homepage.entity;

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

import com.personalproject.homepage.repository.CategoryRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "name", "parent_category_idx" }))
@Getter
@DynamicInsert
@DynamicUpdate
public class Category extends CommonEntity {

    private String name;

    /********************************************************************************
        자기 참조 외래키로서, 계층이 깊어질 수도 있다.
        idx로 find하지 않기에 원하는 entity를 찾기 위해 재귀적 탐색이 필요하다.
        불필요하게 복잡해지는 프로세스를 막기 위해
        최상위(parentCategory == null)
        하위(parentCategory != null)인 두 종류의 entity만 입력받을 수 있게 한다.
    ********************************************************************************/
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
        /********************************************************************************
            생성 시 name != null이어야 하지만
            변경 시 null name이 들어와도 변경 대상에서 제외되므로 null check는 create시 수행한다.
        ********************************************************************************/
        this.name = name;
        this.parentCategory = parentCategory;
    }

    public void updateInfo(String name, Category parentCategory) {
        // args could be nullable because it's just update
        if (name != null) this.name = name;
        if (this.parentCategory == null && parentCategory == null) return;
        setParentCategory(parentCategory); // null로 변경할 경우 top-level이 된다.
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
        if (parentCategory != null) {
            parentCategory.getCategoriesOfCategory().add(this);
        }
    }

    /**
     * 카테고리에 등록된 게시글 수를 포함한 쿼리에 매핑될 entity 내부 클래스
     * @see CategoryRepository#allCategoriesWithPostsCount(Boolean)
     */
    @Getter
    @AllArgsConstructor
    public static class WithPostsCount {
        private Category category;

        private Long postsCount;
    }
}
