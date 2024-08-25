package com.yfckevin.bingBao.utils;

import java.text.SimpleDateFormat;
import java.util.Random;

public class RNUtil {
    static SimpleDateFormat ssf = new SimpleDateFormat("yyyyMMdd");
    public static String generateReceiveNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("RN");

        for (int i = 0; i < 12; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }

        return sb.toString();
    }
}
