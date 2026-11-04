package com.adesso.awesomepizza.order;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class TrackingCodeGenerator {

    private static final String ALPHABET = "0123456789ABCDEFGHJKMNPQRSTVWXYZ";
    private static final int CHARACTERS = 12;
    private static final int GROUP_SIZE = 4;

    private final SecureRandom random = new SecureRandom();

    public String generate() {
        StringBuilder code = new StringBuilder("ORD");
        for (int i = 0; i < CHARACTERS; i++) {
            if (i % GROUP_SIZE == 0) {
                code.append('-');
            }
            code.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return code.toString();
    }
}
