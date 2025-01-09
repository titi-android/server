package com.example.busnotice.util;

import java.time.DayOfWeek;
import java.time.LocalDate;

public class DayConverter {

    public static String getTodayAsString() {
        DayOfWeek dayOfWeek = LocalDate.now().getDayOfWeek();

        switch (dayOfWeek) {
            case MONDAY:
                return "월요일";
            case TUESDAY:
                return "화요일";
            case WEDNESDAY:
                return "수요일";
            case THURSDAY:
                return "목요일";
            case FRIDAY:
                return "금요일";
            case SATURDAY:
                return "토요일";
            case SUNDAY:
                return "일요일";
            default:
                throw new IllegalStateException("Invalid day of week: " + dayOfWeek);
        }
    }
}