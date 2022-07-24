package com.personalproject.homepage.entity;

import static com.google.common.base.Preconditions.checkArgument;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.personalproject.homepage.error.ErrorMessage;

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

    private String desc;

    private Long hit = 0L;

    private Boolean visible;

    Post() {/** empty */}

    @Builder
    private Post (Category category, String title, String content, String desc, Boolean visible) {
        /********************************************************************************
            생성 시 title, content, visible이 not null이어야 하지만
            변경 시 null인 field는 변경 대상에서 제외될 수 있으므로
            null check는 create시 수행한다.
        ********************************************************************************/
        checkArgument(category != null, ErrorMessage.NOT_ALLOWED_NULL.getMessage("카테고리"));
        checkArgument(category.getParentCategory() != null, ErrorMessage.NOT_ALLOWED_TOPLEVEL_POST.getMessage());
        this.category = category;
        this.title = title;
        this.content = content;
        this.desc = desc;
        this.visible = visible;
    }

    public void updateInfo(Category category, String title, String content, String desc, Boolean visible) {
        if (title != null) this.title = title;
        if (content != null) this.content = content;
        if (desc != null) this.desc = desc;
        if (visible != null) this.visible = visible;
        if (category != null) setCategory(category);
    }

    private void setCategory(Category category) {
        if (this.category != null) {
            this.category.getPostsOfCategory().remove(this);
        }
        this.category = category;
        this.category.getPostsOfCategory().add(this);
    }

    public void addHit() {
        ++this.hit;
    }
}
