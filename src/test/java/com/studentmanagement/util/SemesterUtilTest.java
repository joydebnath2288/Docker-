package com.studentmanagement.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SemesterUtil Tests")
class SemesterUtilTest {

    @Test
    @DisplayName("Should dynamically determine current term and year without hardcoding")
    void testGetCurrentSemester() {
        String currentSemester = SemesterUtil.getCurrentSemester();
        assertNotNull(currentSemester);
        assertTrue(SemesterUtil.isValidSemester(currentSemester), "Current semester format must be valid");

        int currentYear = LocalDate.now().getYear();
        assertTrue(currentSemester.endsWith(String.valueOf(currentYear)),
                "Current semester should end with current year " + currentYear);

        String currentTerm = SemesterUtil.getCurrentTerm();
        assertNotNull(currentTerm);
        assertTrue(List.of("SPRING", "SUMMER", "FALL").contains(currentTerm));
        assertEquals(currentTerm + " " + currentYear, currentSemester);
    }

    @Test
    @DisplayName("Should generate dynamic list of years spanning past and future")
    void testGetAvailableYears() {
        List<Integer> years = SemesterUtil.getAvailableYears();
        assertNotNull(years);
        assertFalse(years.isEmpty());

        int currentYear = LocalDate.now().getYear();
        assertTrue(years.contains(currentYear), "Available years must include current year");
        assertTrue(years.contains(currentYear - 1), "Available years must include previous year");
        assertTrue(years.contains(currentYear + 1), "Available years must include next year");
    }

    @Test
    @DisplayName("Should generate dynamic list of available semesters with all 3 terms")
    void testGetAvailableSemesters() {
        List<String> semesters = SemesterUtil.getAvailableSemesters();
        assertNotNull(semesters);
        assertFalse(semesters.isEmpty());

        int currentYear = LocalDate.now().getYear();
        assertTrue(semesters.contains("SPRING " + currentYear));
        assertTrue(semesters.contains("SUMMER " + currentYear));
        assertTrue(semesters.contains("FALL " + currentYear));
    }

    @Test
    @DisplayName("Should validate semesters correctly")
    void testIsValidSemester() {
        assertTrue(SemesterUtil.isValidSemester("FALL 2025"));
        assertTrue(SemesterUtil.isValidSemester("SPRING 2026"));
        assertTrue(SemesterUtil.isValidSemester("SUMMER 2024"));
        assertTrue(SemesterUtil.isValidSemester("fall 2025"));

        assertFalse(SemesterUtil.isValidSemester(null));
        assertFalse(SemesterUtil.isValidSemester(""));
        assertFalse(SemesterUtil.isValidSemester("WINTER 2025"));
        assertFalse(SemesterUtil.isValidSemester("FALL"));
        assertFalse(SemesterUtil.isValidSemester("2025"));
        assertFalse(SemesterUtil.isValidSemester("FALL 25"));
    }
}
