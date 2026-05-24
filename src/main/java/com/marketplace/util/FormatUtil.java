package com.marketplace.util;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class FormatUtil {
    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getCurrencyInstance(new Locale("ru", "RU"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public static String formatPrice(BigDecimal price) {
        if (price == null) return "0 ₽";
        return String.format("%,.0f ₽", price.doubleValue()).replace(",", " ");
    }

    public static String formatDate(String isoDate) {
        if (isoDate == null) return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDate.substring(0, 19));
            return dt.format(DATE_FORMATTER);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String formatShortDate(String isoDate) {
        if (isoDate == null) return "";
        try {
            LocalDateTime dt = LocalDateTime.parse(isoDate.substring(0, 19));
            return dt.format(SHORT_DATE);
        } catch (Exception e) {
            return isoDate;
        }
    }

    public static String formatRating(double rating) {
        return String.format("%.1f", rating);
    }

    public static String stars(double rating) {
        int full = (int) rating;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            sb.append(i < full ? "★" : "☆");
        }
        return sb.toString();
    }
}
