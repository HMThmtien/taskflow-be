package com.taskflow.taskflow_be.security.jwt;

import java.security.SecureRandom;
import java.util.Base64;

public class GenSecret {
    public static void main(String[] args) {
        byte[] key = new byte[32]; // 256 bits
        new SecureRandom().nextBytes(key);
        System.out.println(Base64.getEncoder().encodeToString(key));
    }
}
