package com.campus.infrastructure.util;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt());
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        PasswordEncoder encoder = new PasswordEncoder();
        String encoded = encoder.encode("123456");
        System.out.println("加密后: " + encoded);
        System.out.println("验证: " + encoder.matches("123456", encoded));
    }
}