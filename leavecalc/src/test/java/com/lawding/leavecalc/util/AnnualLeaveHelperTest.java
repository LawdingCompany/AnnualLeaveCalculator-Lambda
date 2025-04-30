package com.lawding.leavecalc.util;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("연차 계산 보조 유틸 함수 단위 테스트")
public class AnnualLeaveHelperTest {

    @Nested
    @DisplayName("입사 후 1년 미만인지 확인하는 함수")
    class isLessThanOneYearTest {

        @Test
        @DisplayName("기준일이 입사일로부터 1년이 지나지 않은 경우 true")
        void returnTrueWhenReferenceDateIsBeforeOneYearFromHireDate() {
            // given
            LocalDate hireDate = LocalDate.of(2024, 2, 1);
            LocalDate referenceDate = LocalDate.of(2025, 1, 31);

            // when
            boolean result = isLessThanOneYear(hireDate, referenceDate);

            // then
            assertTrue(result);
        }

        @Test
        @DisplayName("기준일이 입사일로부터 정확히 1년인 경우 false")
        void returnFalseWhenReferenceDateIsExactlyOneYearFromHireDate() {
            // given
            LocalDate hireDate = LocalDate.of(2024, 2, 1);
            LocalDate referenceDate = LocalDate.of(2025, 2, 1);

            // when
            boolean result = isLessThanOneYear(hireDate, referenceDate);

            // then
            assertFalse(result);
        }

        @Test
        @DisplayName("기준일이 입사일로부터 1년이 지난 경우 false")
        void returnFalseWhenReferenceDateIsAfterOneYearFromHireDate() {
            // given
            LocalDate hireDate = LocalDate.of(2024, 2, 1);
            LocalDate referenceDate = LocalDate.of(2025, 2, 2);

            // when
            boolean result = isLessThanOneYear(hireDate, referenceDate);

            // then
            assertFalse(result);
        }
    }

    @Nested
    @DisplayName("근속연수에 따른 가산 연차를 계산하는 함수")
    class calculateAdditionalLeaveTest {

        @Test
        @DisplayName("근속연수가 1년 미만이면 가산 연차는 0일이다.")
        void returnZeroWhenLessThanOneYear() {
            // given
            int serviceYears = 0;
            // when
            int result = calculateAdditionalLeave(serviceYears);
            //then
            assertEquals(0, result);
        }

        @Test
        @DisplayName("근속연수가 3,4년이면 가산 연차는 1일이다.")
        void returnOneWhenThreeOrFourYears() {
            // given
            int serviceYears1 = 3;
            int serviceYears2 = 4;
            // when
            int result1 = calculateAdditionalLeave(serviceYears1);
            int result2 = calculateAdditionalLeave(serviceYears2);
            //then
            assertEquals(1, result1);
            assertEquals(1, result2);
        }

        @Test
        @DisplayName("근속연수가 23년이상이면 가산 연차는 10일(최대)이다.")
        void returnTenWhenOverTwentyOneYears() {
            // given
            int serviceYears = 23;
            // when
            int result = calculateAdditionalLeave(serviceYears);
            //then
            assertEquals(10, result);
        }
    }

    @Nested
    @DisplayName("해당 날짜가 주중(월-금)인지 판단하는 함수")
    class isWeekdayTest {

        @Test
        @DisplayName("해당 날짜가 주중이면 true")
        void returnTrueWhenIsWeekDays() {
            // given
            LocalDate monday = LocalDate.of(2025, 5, 5); // 월
            LocalDate friday = LocalDate.of(2025, 5, 9); // 금

            // when
            boolean result1 = isWeekday(monday);
            boolean result2 = isWeekday(friday);

            // then
            assertTrue(result1);
            assertTrue(result2);
        }

        @Test
        @DisplayName("해당 날짜가 주말이면 false")
        void returnFalseWhenIsWeekends() {
            // given
            LocalDate saturday = LocalDate.of(2025, 5, 3); // 월
            LocalDate sunday = LocalDate.of(2025, 5, 4); // 금

            // when
            boolean result1 = isWeekday(saturday);
            boolean result2 = isWeekday(sunday);

            // then
            assertFalse(result1);
            assertFalse(result2);
        }
    }

    @Nested
    @DisplayName("출근율(AR)을 계산하는 함수")
    class calculateAttendanceRateTest {

        @Test
        @DisplayName("결근처리일, 소정근로제외일수가 없으면 출근율 1.0")
        void returnsOneWhenNoAbsentAndNoExclusion() {
            // Given
            int prescribedWorkingDays = 262;
            int absentDays = 0;
            int excludedWorkingDays = 0;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(1.0, attendanceRate);
        }
        @Test
        @DisplayName("결근처리일 없음, 소정근로제외일수가 있으면 출근율 1.0")
        void returnsOneWhenNoAbsent() {
            // Given
            int prescribedWorkingDays = 262;
            int absentDays = 0;
            int excludedWorkingDays = 10;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(1.0, attendanceRate);
        }

    }
}
