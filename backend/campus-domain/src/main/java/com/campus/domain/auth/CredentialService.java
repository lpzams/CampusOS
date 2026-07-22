package com.campus.domain.auth;

public interface CredentialService {
    String hash(String password);
    boolean matches(String password, String hash);
    String createToken(Long userId, String username, Integer userType);
    TokenClaims parseToken(String token);

    record TokenClaims(Long userId, String username, Integer userType) {}
}
