package com.yfckevin.bingBao.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class DateUtil {
    public static String genDateFormatted(String startDate, String endDate) {
        // 定義日期時間格式
        DateTimeFormatter fullFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter dateOnlyFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 解析日期字符串
        LocalDateTime start = LocalDateTime.parse(startDate, fullFormatter);
        LocalDateTime end = parseEndDate(endDate, fullFormatter, dateOnlyFormatter);

        // 計算年、月、日差異
        Period period = Period.between(start.toLocalDate(), end.toLocalDate());

        if (start.toLocalDate().isEqual(end.toLocalDate())) {
            return "0天";
        }

        // 構建結果字符串
        StringBuilder result = new StringBuilder();
        if (period.getYears() > 0) {
            result.append(period.getYears()).append("年");
        }
        if (period.getMonths() > 0) {
            result.append(period.getMonths()).append("個月");
        }
        if (period.getDays() > 0) {
            result.append(period.getDays()).append("天");
        }

        return result.toString().trim();
    }

    private static LocalDateTime parseEndDate(String endDate, DateTimeFormatter fullFormatter, DateTimeFormatter dateOnlyFormatter) {
        // 檢查 endDate 字符串長度來決定使用的解析器
        if (endDate.length() == 10) { // "yyyy-MM-dd" 格式
            return LocalDate.parse(endDate, dateOnlyFormatter).atStartOfDay();
        } else { // "yyyy-MM-dd HH:mm:ss" 格式
            return LocalDateTime.parse(endDate, fullFormatter);
        }
    }


    public static String genNoticeDateFormatted(String expiryDate, String overdueNotice){
        // 定義日期格式
        DateTimeFormatter shortFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 解析有效期限日期
        LocalDate expiry = LocalDate.parse(expiryDate, shortFormatter);

        // 計算通知日期
        LocalDate noticeDate = expiry.minusDays(Long.parseLong(overdueNotice));

        // 格式化通知日期
        return noticeDate.format(shortFormatter);
    }
}
