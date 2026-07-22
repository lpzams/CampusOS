package com.campus.infrastructure.auth;

import com.campus.domain.auth.CredentialService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtCredentialService implements CredentialService {
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecretKey key;
    private final long expiration;

    public JwtCredentialService(@Value("${campus.jwt.secret}") String secret,
                                @Value("${campus.jwt.expiration:7200000}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    @Override public String hash(String password) { return encoder.encode(password); }
    @Override public boolean matches(String password, String hash) { return encoder.matches(password, hash); }

    @Override
    public String createToken(Long userId, String username, Integer userType) {
        Date now = new Date();
        return Jwts.builder().subject(userId.toString()).claim("username", username)
                .claim("userType", userType).issuedAt(now).expiration(new Date(now.getTime() + expiration))
                .signWith(key).compact();
    }

    @Override
    public TokenClaims parseToken(String token) {
        Claims claims = Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
        return new TokenClaims(Long.valueOf(claims.getSubject()), claims.get("username", String.class),
                claims.get("userType", Integer.class));
    }
}
