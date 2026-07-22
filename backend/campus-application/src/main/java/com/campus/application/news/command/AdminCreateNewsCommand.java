package com.campus.application.news.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AdminCreateNewsCommand {
    @NotBlank private String title;
    @NotBlank private String content;
    @NotNull private Integer categoryId;
    private String coverImage;
    private String summary;
    private Boolean isTop;
    private Boolean isPublished;

    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; } public void setContent(String content) { this.content = content; }
    public Integer getCategoryId() { return categoryId; } public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getCoverImage() { return coverImage; } public void setCoverImage(String coverImage) { this.coverImage = coverImage; }
    public String getSummary() { return summary; } public void setSummary(String summary) { this.summary = summary; }
    public Boolean getIsTop() { return isTop; } public void setIsTop(Boolean top) { isTop = top; }
    public Boolean getIsPublished() { return isPublished; } public void setIsPublished(Boolean published) { isPublished = published; }
}
