package com.campus.api.auth;

import com.campus.common.exception.BusinessException;
import com.campus.domain.auth.CredentialService;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class AuthSession {
    private CredentialService.TokenClaims principal;
    public void setPrincipal(CredentialService.TokenClaims principal) { this.principal = principal; }
    public Long userId() {
        if (principal == null) throw new BusinessException(401, "未授权");
        return principal.userId();
    }
    public Integer userType() {
        if (principal == null) throw new BusinessException(401, "未授权");
        return principal.userType();
    }
}
