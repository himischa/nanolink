package com.nanolink.common.util;

import java.security.SecureRandom;

public final class ShortCodeGenerator {
    private static final char[] CHARSET =
            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
    private static final int CODE_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    private ShortCodeGenerator() {
    }

    public static String generate() {
        char[] buffer = new char[CODE_LENGTH];
        for (int i = 0; i < CODE_LENGTH; i++) {
            buffer[i] = CHARSET[RANDOM.nextInt(CHARSET.length)];
        }
        return new String(buffer);
    }
}
