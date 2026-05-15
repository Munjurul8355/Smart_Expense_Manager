package com.expensemanager.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter PRETTY_DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy");

    public static String format(LocalDate date) {
        if (date == null) return "";
        return DATE_FORMATTER.format(date);
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) return "";
        return DATETIME_FORMATTER.format(dateTime);
    }

    public static String formatPretty(LocalDate date) {
        if (date == null) return "";
        return PRETTY_DATE_FORMATTER.format(date);
    }

    public static LocalDate parseDate(String dateString) {
        try {
            return DATE_FORMATTER.parse(dateString, LocalDate::from);
        } catch (Exception e) {
            return null;
        }
    }
}
