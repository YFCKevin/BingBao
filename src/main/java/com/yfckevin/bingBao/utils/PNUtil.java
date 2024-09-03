package com.yfckevin.bingBao.utils;

import java.util.Random;

public class PNUtil {
    public static String generatePackageNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("PN");

        // 產生隨機的12個數字
        for (int i = 0; i < 12; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }

        return sb.toString();
    }
}
