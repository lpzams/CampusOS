package com.campus.application.notice.command;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 置顶/取消置顶命令
 */
@Data
public class TopNoticeCommand {

    @NotNull(message = "isTop不能为空")
    private Boolean isTop;
}
