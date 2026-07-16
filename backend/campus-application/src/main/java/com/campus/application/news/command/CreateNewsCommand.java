package com.campus.application.news.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 创建新闻命令（Command）—— 应用层「写操作」的入参。
 *
 * <p>命令 = 一次「要改变系统状态」的意图（新建/修改/删除）。
 * 在这里做参数校验（{@code @NotBlank} 等），Controller 直接把它作为
 * {@code @RequestBody} 接收，校验不通过会被全局异常处理器统一拦截。</p>
 *
 * <p>与「查询（Query）」区分开：命令改数据，查询只读数据（CQRS 思想的轻量落地）。</p>
 */
public class CreateNewsCommand {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题最长 200 字")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    @NotBlank(message = "作者不能为空")
    private String author;

    @NotBlank(message = "栏目不能为空")
    private String category;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
