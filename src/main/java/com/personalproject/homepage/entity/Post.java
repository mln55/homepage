package com.personalproject.homepage.entity;

import static com.google.common.base.Preconditions.checkArgument;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@DynamicUpdate
@DynamicInsert
public class Post extends CommonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postIdx;

    @ManyToOne
    @JoinColumn(name = "category_idx")
    private Category category;

    private String title;

    private String content;

    private Long hit = 0L;

    private Boolean visible;

    public Post() {/** empty */}

    @Builder
    public Post(String title, String content, Boolean visible, Category category) {
        checkArgument(title != null, "Post title은 null일 수 없습니다.");
        checkArgument(content != null, "Post content은 null일 수 없습니다.");
        checkArgument(visible != null, "Post visible은 null일 수 없습니다.");
        this.title = title;
        this.content = content;
        this.visible = visible;

        if (category != null) {
            setCategory(category);
        }
    }

    public void setCategory(Category category) {
        if (this.category != null) {
            this.category.getPostsOfCategory().remove(this);
        }
        this.category = category;
        category.getPostsOfCategory().add(this);
    }
}
