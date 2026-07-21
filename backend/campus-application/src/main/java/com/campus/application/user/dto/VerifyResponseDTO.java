package com.campus.application.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 实名认证响应（2.6 POST /user/verify）
 */
@Data
@Builder
public class VerifyResponseDTO {

    private Long verifyId;
    private String status;
    private String statusDesc;
    private String submitTime;
    private String expectedFinishTime;
}
