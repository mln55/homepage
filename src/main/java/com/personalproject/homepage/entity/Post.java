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
        checkArgument(title != null, "Post Entity title must not be null");
        checkArgument(content != null, "Post Entity content must not be null");
        checkArgument(visible != null, "Post Entity visible must not be null");

        this.category = category;
        this.title = title;
        this.content = content;
        this.visible = visible;
    }

    public void updateInfo(Category category, String title, String content, Boolean visible) {
        if (category != null) setCategory(category);
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (visible != null) this.visible = visible;
    }

    private void setCategory(Category category) {
        if (this.category != null) {
            this.category.getPostsOfCategory().remove(this);
        }
        this.category = category;
        category.getPostsOfCategory().add(this);
    }
}
