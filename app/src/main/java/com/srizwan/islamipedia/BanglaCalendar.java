package com.srizwan.islamipedia;

import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BanglaCalendar {

    private int bDay;
    private int bMonth;
    private int bYear;

    public void addMonth(int year, int month) {
        bYear = (month % 12 == 0) ? year - 1 : year;
        bMonth = (month % 12 == 0) ? 12 : month % 12;
        bDay = 1;
    }

    public String now(Locale locale) {
        Calendar today = Calendar.getInstance();
        return toBanglaDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), locale);
    }

    public String toBanglaDate(int gYear, int gMonth, int gDay, Locale locale) {
        int[] totalDaysInMonth = isLeapYear(gYear) ? new int[]{31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 29, 30} : new int[]{31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 30};

        int banglaYear = (gMonth < 3 || (gMonth == 3 && gDay < 14)) ? gYear - 594 : gYear - 593;
        int epochYear = (gMonth < 4 || (gMonth == 4 && gDay < 14)) ? gYear - 1 : gYear;

        Calendar banglaCalendar = Calendar.getInstance();
        banglaCalendar.set(gYear, gMonth, gDay);
        Calendar epochCalendar = Calendar.getInstance();
        epochCalendar.set(epochYear, 4, 14);

        long diff = banglaCalendar.getTimeInMillis() - epochCalendar.getTimeInMillis();
        long difference = TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);

        int banglaMonthIndex = 0;
        for (int i = 1; i <= 12; i++) {
            if (difference <= totalDaysInMonth[i]) {
                banglaMonthIndex = i;
                break;
            }
            difference -= totalDaysInMonth[i];
        }

        int banglaDate = (int) difference;

        return bDate(
                translateNumbersToBangla(Integer.toString(banglaYear)),
                getBanglaMonths()[banglaMonthIndex],
                translateNumbersToBangla(Integer.toString(banglaDate))
        );
    }

    private boolean isLeapYear(int year) {
        return ((year % 4 == 0) && (year % 100 != 0)) || (year % 400 == 0);
    }

    private String translateNumbersToBangla(String inputNumber) {
        String[] banglaNumbers = {"০", "১", "২", "৩", "৪", "৫", "৬", "৭", "৮", "৯"};
        StringBuilder sb = new StringBuilder();
        for (char ch : inputNumber.toCharArray()) {
            if (Character.isDigit(ch)) {
                sb.append(banglaNumbers[ch - '0']);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    private String bDate(String year, String month, String day) {
        return day + " " + month + ", " + year;
    }

    private String[] getBanglaMonths() {
        return new String[]{
                "বৈশাখ",
                "জ্যৈষ্ঠ",
                "আষাঢ়",
                "শ্রাবণ",
                "ভাদ্র",
                "আশ্বিন",
                "কার্তিক",
                "অগ্রহায়ণ",
                "পৌষ",
                "মাঘ",
                "ফাল্গুন",
                "চৈত্র"
        };
    }
}
