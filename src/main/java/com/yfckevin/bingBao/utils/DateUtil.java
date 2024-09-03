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

        // 計算天數差異
        long totalDaysBetween = ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
        long daysInFullYears = period.getYears() * 365; // 基於365天的年份
        long daysInFullMonths = period.getMonths() * 30; // 基於30天的月份
        long daysInPeriods = daysInFullYears + daysInFullMonths + period.getDays();

        long remainingDays = totalDaysBetween - daysInPeriods;

        int years = period.getYears();
        int months = period.getMonths();
        int days = period.getDays() + (int) remainingDays; // 加上剩餘的天數

        // 構建結果字符串
        StringBuilder result = new StringBuilder();
        if (years > 0) {
            result.append(years).append("年");
        }
        if (months > 0) {
            result.append(months).append("個月");
        }
        result.append(days).append("天");

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
