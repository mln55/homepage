package com.personalproject.homepage.entity;

import static com.google.common.base.Preconditions.checkArgument;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@DynamicUpdate
@DynamicInsert
public class Post extends CommonEntity {

    @ManyToOne
    @JoinColumn(name = "category_idx")
    private Category category;

    private String title;

    private String content;

    private Long hit = 0L;

    private Boolean visible;

    Post() {/** empty */}

    @Builder
    private Post (Category category, String title, String content, Boolean visible) {
        /********************************************************************************
            생성 시 title, content, visible이 not null이어야 하지만
            변경 시 null인 field는 변경 대상에서 제외될 수 있으므로
            null check는 create시 수행한다.
        ********************************************************************************/
        this.category = category;
        this.title = title;
        this.content = content;
        this.visible = visible;
    }

    public void updateInfo(Category category, String title, String content, Boolean visible) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (visible != null) this.visible = visible;
        if (this.category == null && category == null) return;
        setCategory(category); // null로 변경할 경우 카테고리가 없다.
    }

    private void setCategory(Category category) {
        if (this.category != null) {
            this.category.getPostsOfCategory().remove(this);
        }
        this.category = category;
        if (category != null) {
            category.getPostsOfCategory().add(this);
        }
    }

    public void addHit() {
        ++this.hit;
    }
}
