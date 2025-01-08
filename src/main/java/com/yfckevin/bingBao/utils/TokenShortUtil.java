package com.yfckevin.bingBao.utils;

import java.util.Random;

public class TokenShortUtil {
    public static String genShortStr(int length) {
        StringBuilder sb = new StringBuilder(length);
        Random random = new Random();

        for (int i = 0; i < length; i++) {
            // 隨機選擇一個大寫字母 (A-Z)
            char randomChar = (char) ('A' + random.nextInt(26));
            sb.append(randomChar);
        }

        return sb.toString();
    }
}
