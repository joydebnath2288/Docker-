package com.studentmanagement.util;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class SemesterUtil {

    public static final String TERM_SPRING = "SPRING";
    public static final String TERM_SUMMER = "SUMMER";
    public static final String TERM_FALL = "FALL";

    public static final String[] TERMS = {TERM_SPRING, TERM_SUMMER, TERM_FALL};

    private static final Pattern SEMESTER_PATTERN = Pattern.compile("^(SPRING|SUMMER|FALL)\\s+\\d{4}$", Pattern.CASE_INSENSITIVE);

    public static int getCurrentYear() {
        return LocalDate.now().getYear();
    }

    public static String getCurrentTerm() {
        int month = LocalDate.now().getMonthValue();
        if (month >= 1 && month <= 4) {
            return TERM_SPRING;
        } else if (month >= 5 && month <= 8) {
            return TERM_SUMMER;
        } else {
            return TERM_FALL;
        }
    }

    public static String getCurrentSemester() {
        return getCurrentTerm() + " " + getCurrentYear();
    }

    public static List<Integer> getAvailableYears() {
        int currentYear = getCurrentYear();
        List<Integer> years = new ArrayList<>();
        for (int y = currentYear - 3; y <= currentYear + 3; y++) {
            years.add(y);
        }
        return Collections.unmodifiableList(years);
    }

    public static List<String> getAvailableSemesters() {
        List<String> list = new ArrayList<>();
        int currentYear = getCurrentYear();
        for (int y = currentYear - 3; y <= currentYear + 3; y++) {
            for (String term : TERMS) {
                list.add(term + " " + y);
            }
        }
        return Collections.unmodifiableList(list);
    }

    public static String formatSemester(String term, Object year) {
        if (term == null || term.trim().isEmpty() || year == null) {
            return getCurrentSemester();
        }
        String cleanTerm = term.trim().toUpperCase();
        return cleanTerm + " " + year.toString().trim();
    }

    public static boolean isValidSemester(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            return false;
        }
        return SEMESTER_PATTERN.matcher(semester.trim()).matches();
    }

    public static String getNextSemester(String semester) {
        if (semester == null || !isValidSemester(semester)) {
            semester = getCurrentSemester();
        }
        String[] parts = semester.trim().split("\\s+");
        String term = parts[0].toUpperCase();
        int year = Integer.parseInt(parts[1]);

        if (TERM_SPRING.equalsIgnoreCase(term)) {
            return TERM_SUMMER + " " + year;
        } else if (TERM_SUMMER.equalsIgnoreCase(term)) {
            return TERM_FALL + " " + year;
        } else {
            return TERM_SPRING + " " + (year + 1);
        }
    }

    public static String getUpcomingSemester() {
        return getNextSemester(getCurrentSemester());
    }

    public static String getSemesterDuration(String semester) {
        if (semester == null || semester.trim().isEmpty()) {
            return "4 Months";
        }
        String term = semester.trim().split("\\s+")[0].toUpperCase();
        switch (term) {
            case TERM_SPRING:
                return "4 Months (January - April)";
            case TERM_SUMMER:
                return "4 Months (May - August)";
            case TERM_FALL:
                return "4 Months (September - December)";
            default:
                return "4 Months";
        }
    }
}
