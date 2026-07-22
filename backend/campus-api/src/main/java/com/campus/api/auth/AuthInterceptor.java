package com.campus.api.auth;

import com.campus.application.auth.AuthAppService;
import com.campus.common.exception.BusinessException;
import com.campus.domain.auth.CredentialService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    public static final String PRINCIPAL = "campusPrincipal";
    private final AuthAppService auth;
    private final AuthSession session;

    public AuthInterceptor(AuthAppService auth, AuthSession session) { this.auth = auth; this.session = session; }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod method)) return true;
        RoleAllowed roleAllowed = method.getMethodAnnotation(RoleAllowed.class);
        if (roleAllowed == null) roleAllowed = method.getBeanType().getAnnotation(RoleAllowed.class);
        boolean authenticated = method.hasMethodAnnotation(Authenticated.class)
                || method.getBeanType().isAnnotationPresent(Authenticated.class)
                || roleAllowed != null;
        if (!authenticated) return true;
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) throw new BusinessException(401, "未授权");
        try {
            CredentialService.TokenClaims principal = auth.parse(header.substring(7));
            request.setAttribute(PRINCIPAL, principal);
            session.setPrincipal(principal);
            if (roleAllowed != null) {
                for (int allowedType : roleAllowed.value()) {
                    if (allowedType == principal.userType()) return true;
                }
                throw new BusinessException(403, "当前角色无权使用此功能");
            }
            return true;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(401, "Token 无效或已过期");
        }
    }
}
