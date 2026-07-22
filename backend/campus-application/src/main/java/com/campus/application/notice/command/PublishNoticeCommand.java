package com.campus.application.notice.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class PublishNoticeCommand {
    @NotBlank private String title;
    @NotBlank private String content;
    @Pattern(regexp = "SCHOOL|DEPT") private String type;
    @NotBlank private String department;
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
}
