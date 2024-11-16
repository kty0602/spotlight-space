package com.spotlightspace.core.coupon;

import java.util.Random;

public class CouponCodeGenerator {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int SEGMENT_LENGTH = 4;
    private static final int TOTAL_SEGMENTS = 3;
    private static final String DELIMITER = "-";

    public static String generateCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();

        for (int i = 0; i < TOTAL_SEGMENTS; i++) {
            for (int j = 0; j < SEGMENT_LENGTH; j++) {
                code.append(CHAR_POOL.charAt(random.nextInt(CHAR_POOL.length())));
            }
            if (i < TOTAL_SEGMENTS - 1) {
                code.append(DELIMITER);
            }
        }

        return code.toString();
    }
}
