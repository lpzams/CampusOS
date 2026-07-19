package com.campus.application.news.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 发布新闻命令（管理员）
 */
@Data
public class PublishNewsCommand {

    @NotBlank(message = "新闻标题不能为空")
    private String title;

    @NotBlank(message = "新闻内容不能为空")
    private String content;

    private String coverImage;

    @NotNull(message = "分类ID不能为空")
    private Long categoryId;

    private String summary;

    /** 是否置顶：0-否 1-是 */
    private Integer isTop;

    /** 是否发布：0-草稿 1-已发布，默认1 */
    private Integer isPublished;
}
