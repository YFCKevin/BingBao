package com.yfckevin.bingBao.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class INUtil {
    static SimpleDateFormat ssf = new SimpleDateFormat("yyyyMMdd");
    public static String generateStoreNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder("IN");

        for (int i = 0; i < 12; i++) {
            int digit = random.nextInt(10);
            sb.append(digit);
        }

        return sb.toString();
    }
}
