package com.autotasker.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ConvertDateUtil {
    public static LocalDate convertDate(String str) {
        // formatters for all supported formats
        final DateTimeFormatter[] FORMATTERS = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("yyyy.MM.dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd.MM.yyyy")
        };

        for (DateTimeFormatter fmt : FORMATTERS) {
            try {
                return LocalDate.parse(str, fmt);
            } catch (DateTimeParseException ignored) {
            }
        }
        return null;
    }
}
