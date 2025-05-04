package com.lawding.leavecalc.util;

import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.detail.MonthlyLeaveDetail;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.lawding.leavecalc.util.AnnualLeaveHelper.*;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("연차 계산 보조 유틸 함수 단위 테스트")
public class AnnualLeaveHelperTest {

    private static final List<LocalDate> STATUTORY_HOLIDAYS_2024 = List.of(
        LocalDate.of(2024, 1, 1),
        LocalDate.of(2024, 2, 9),
        LocalDate.of(2024, 2, 10),
        LocalDate.of(2024, 2, 11),
        LocalDate.of(2024, 2, 12),
        LocalDate.of(2024, 3, 1),
        LocalDate.of(2024, 4, 10),
        LocalDate.of(2024, 5, 5),
        LocalDate.of(2024, 5, 6),
        LocalDate.of(2024, 5, 15),
        LocalDate.of(2024, 6, 6),
        LocalDate.of(2024, 8, 15),
        LocalDate.of(2024, 9, 16),
        LocalDate.of(2024, 9, 17),
        LocalDate.of(2024, 9, 18),
        LocalDate.of(2024, 10, 1),
        LocalDate.of(2024, 10, 3),
        LocalDate.of(2024, 10, 9),
        LocalDate.of(2024, 12, 25)
    );

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
            boolean result = isBeforeOneYearFromHireDate(hireDate, referenceDate);

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
            boolean result = isBeforeOneYearFromHireDate(hireDate, referenceDate);

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
            boolean result = isBeforeOneYearFromHireDate(hireDate, referenceDate);

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
        @DisplayName("결근처리일, 소정근로제외일수가 없으면 출근율(AR) 1.0")
        void returnsOneWhenNoAbsentAndNoExclusion() {
            // Given
            int prescribedWorkingDays = 246;
            int absentDays = 0;
            int excludedWorkingDays = 0;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(1.0, attendanceRate);
        }

        @Test
        @DisplayName("결근처리일 없음, 소정근로제외일수가 있으면 출근율(AR) 1.0")
        void returnsOneWhenNoAbsent() {
            // Given
            int prescribedWorkingDays = 246;
            int absentDays = 0;
            int excludedWorkingDays = 10;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(1.0, attendanceRate);
        }

        @Test
        @DisplayName("결근처리가 소정근로일의 50%, 소정근로제외일수가 없으면 출근율(AR) 0.5")
        void returnsHalfAttendanceRateWhenAbsentDaysAreHalfOfWorkingDaysAndNoExcludedDays() {
            // Given
            int prescribedWorkingDays = 100;
            int absentDays = 50;
            int excludedWorkingDays = 0;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(0.5, attendanceRate);
        }

        @Test
        @DisplayName("소정근로일(100), 소정근로제외일(10), 결근처리일(20) 입력 시 출근율(AR) 0.78")
        void returnsCorrectAttendanceRateGivenAllParameters() {
            // Given
            int prescribedWorkingDays = 100;
            int absentDays = 20;
            int excludedWorkingDays = 10;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(0.78, attendanceRate, 0.01);
        }

        @Test
        @DisplayName("소정근로일과 소정근로제외일수가 같은 경우 출근율(AR) 0")
        void returnsZeroWhenAllWorkingDaysAreExcluded() {
            // Given
            int prescribedWorkingDays = 100;
            int absentDays = 0;
            int excludedWorkingDays = 100;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(0, attendanceRate);
        }

        @Test
        @DisplayName("소정근로일과 결근처리일수가 같은 경우 출근율(AR) 0")
        void returnsZeroWhenAllWorkingDaysAreAbsent() {
            // Given
            int prescribedWorkingDays = 100;
            int absentDays = 100;
            int excludedWorkingDays = 0;

            // When
            double attendanceRate = calculateAttendanceRate(prescribedWorkingDays,
                absentDays, excludedWorkingDays);

            // Then
            assertEquals(0, attendanceRate);
        }
    }

    @Nested
    @DisplayName("연차산정기간의 개근 여부를 판단하여 월차를 계산하는 함수")
    class monthlyAccruedLeavesTest {

        @Test
        @DisplayName("6개월간 결근 처리일이 없다면, 월차 6개")
        void returnsFiveMonthlyLeavesWhenOneDayShortOfSixMonths() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)
            );
            List<DatePeriod> excludedPeriods = List.of();

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(6, monthlyLeave.getTotalLeaveDays());
        }

        @Test
        @DisplayName("6개월 -1일간 결근 처리일이 없다면, 월차 5개")
        void returnsSixMonthlyLeavesWhenNoAbsenceOrExcludedPeriodInSixMonths() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 29)
            );
            List<DatePeriod> excludedPeriods = List.of();

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(5, monthlyLeave.getTotalLeaveDays());
        }

        @Test
        @DisplayName("6개월 +1일간 결근 처리일이 없다면, 월차 6개")
        void returnsSixMonthlyLeavesWhenMoreThanSixMonthsByOneDay() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 7, 1)
            );
            List<DatePeriod> excludedPeriods = List.of();

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(6, monthlyLeave.getTotalLeaveDays());
        }

        @Test
        @DisplayName("1년간 결근 처리일이 없다면, 월차 11개")
        void returnsElevenWhenNoAbsenceOrExcludedPeriodInOneYear() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );
            List<DatePeriod> excludedPeriods = List.of();

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(11, monthlyLeave.getTotalLeaveDays());
        }

        @Test
        @DisplayName("1년간 결근 처리일이 N월에 있다면, 월차 11개")
        void returnsElevenWhenAbsentDaysAreOneInOneYearPeriod() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );
            List<DatePeriod> excludedPeriods = List.of(new DatePeriod(
                    LocalDate.of(2024, 5, 1),
                    LocalDate.of(2024, 5, 1)
                )
            );

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(11, monthlyLeave.getTotalLeaveDays());
        }

        @Test
        @DisplayName("1년간 결근 처리일이 5, 8월에 있다면, 월차 10개")
        void returnsTenWhenAbsentDaysOccurInMayAndAugust() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );
            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 5, 2),
                    LocalDate.of(2024, 5, 2)
                ),
                new DatePeriod(
                    LocalDate.of(2024, 8, 2),
                    LocalDate.of(2024, 8, 2)
                )
            );

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(10, monthlyLeave.getTotalLeaveDays());
        }

        @Test
        @DisplayName("1년간 결근 처리일이 연달아(5, 6월)있다면, 월차 10개")
        void returnsTenWhenAbsentDaysOccurInMayAndJune() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );

            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 5, 2),
                    LocalDate.of(2024, 5, 6)
                ),
                new DatePeriod(
                    LocalDate.of(2024, 5, 2),
                    LocalDate.of(2024, 6, 1)
                )
            );

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(10, monthlyLeave.getTotalLeaveDays());
        }

        @Test
        @DisplayName("1년간 결근 처리일이 모든 달에 있으면, 월차 0개")
        void returnsZeroWhenAbsentDaysExistInEveryMonth() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );
            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 1, 1),
                    LocalDate.of(2024, 12, 1)
                )
            );

            // When
            MonthlyLeaveDetail monthlyLeave = monthlyAccruedLeaves(period, excludedPeriods);

            // Then
            assertEquals(0, monthlyLeave.getTotalLeaveDays());
        }


    }

    @Nested
    @DisplayName("소수점 둘째자리까지 올림하는 함수")
    class formatDoubleTest {

        @Test
        @DisplayName("0.478인 경우 0.48")
        void test1() {
            // Given
            double value = 0.478;

            // When
            double result = formatDouble(value);

            // Then
            assertEquals(0.48, result);
        }

        @Test
        @DisplayName("0.471인 경우 0.48")
        void test2() {
            // Given
            double value = 0.471;

            // When
            double result = formatDouble(value);

            // Then
            assertEquals(0.48, result);
        }
    }

    @Nested
    @DisplayName("연차산정기간 내 소정근로일을 계산하는 함수")
    class calculatePrescribedWorkingDaysTest {

        @Test
        @DisplayName("2024년의 주말 제외 일수(법정공휴일x, 회사휴일x)는 262일이다.")
        void returns262WhenCountingWeekdaysIn2024() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );
            List<LocalDate> companyHolidays = List.of();
            List<LocalDate> statutoryHolidays = List.of();

            // When
            int result = calculatePrescribedWorkingDays(period, companyHolidays, statutoryHolidays);

            // Then
            assertEquals(262, result);
        }

        @Test
        @DisplayName("2024년의 전체 일수 - 주말 - 법정공휴일(회사휴일x)을 뺀 일수는 246일이다.")
        void returns246WorkingDaysIn2024WhenWeekendsAndStatutoryHolidaysAreExcluded() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );
            List<LocalDate> companyHolidays = List.of();

            // When
            int result = calculatePrescribedWorkingDays(period, companyHolidays,
                STATUTORY_HOLIDAYS_2024);

            // Then
            assertEquals(246, result);
        }

        @Test
        @DisplayName("2024년의 전체 일수 - 주말 - 법정공휴일 - 회사휴일(평일2, 주말2, 공휴일1)을 뺀 일수는 244일이다.")
        void returns244WorkingDaysIn2024WhenWeekendsAndAllHolidaysAreExcluded() {
            // Given
            DatePeriod period = new DatePeriod(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 12, 31)
            );
            List<LocalDate> companyHolidays = List.of(
                LocalDate.of(2024, 4, 5), // 금
                LocalDate.of(2024, 4, 6), // 토
                LocalDate.of(2024, 4, 7), // 일
                LocalDate.of(2024, 4, 8), // 월
                LocalDate.of(2024, 4, 10) // 법정공휴일

            );

            // When
            int result = calculatePrescribedWorkingDays(period, companyHolidays,
                STATUTORY_HOLIDAYS_2024);

            // Then
            assertEquals(244, result);
        }

    }

    @Nested
    @DisplayName("연차 산정 기간과 겹치는 결근/소정근로제외일 중 소정근로일(주말·공휴일 제외) 계산")
    class calculatePrescribedWorkingDaysWithinPeriodsTest {

        @Test
        @DisplayName("2024년 5월 중 5.6 ~ 5.20 까지 결근했을 시 결근한 소정근로일은 9일이다.")
        void returnsNineWorkingDaysWhenAbsentFromMay6ToMay20WithinMayPeriod() {
            // Given
            DatePeriod standard = new DatePeriod(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31)
            );

            List<DatePeriod> periods1 = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 5, 6),
                    LocalDate.of(2024, 5, 20))
            );

            List<DatePeriod> periods2 = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 5, 6),
                    LocalDate.of(2024, 5, 8)),
                new DatePeriod(
                    LocalDate.of(2024, 5, 8),
                    LocalDate.of(2024, 5, 20))
            );

            List<DatePeriod> periods3 = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 5, 6),
                    LocalDate.of(2024, 5, 8)),
                new DatePeriod(
                    LocalDate.of(2024, 5, 7),
                    LocalDate.of(2024, 5, 20))
            );

            List<LocalDate> companyHolidays = List.of();

            // When
            int result1 = calculatePrescribedWorkingDaysWithinPeriods(periods1, standard,
                companyHolidays, STATUTORY_HOLIDAYS_2024);
            int result2 = calculatePrescribedWorkingDaysWithinPeriods(periods2, standard,
                companyHolidays, STATUTORY_HOLIDAYS_2024);
            int result3 = calculatePrescribedWorkingDaysWithinPeriods(periods3, standard,
                companyHolidays, STATUTORY_HOLIDAYS_2024);

            // Then
            assertEquals(9, result1);
            assertEquals(9, result2);
            assertEquals(9, result3);
        }

        @Test
        @DisplayName("2024년 5월 중 5.6 ~ 5.20 까지 결근 & 회사 휴일(5/10,11(토),15(공휴일)) 일 때 결근한 소정근로일은 8일이다.")
        void returns8WorkingDaysWhenAbsentFromMay6To20IncludingCompanyHolidayOnMay9AndWeekendOnMay10() {
            // Given
            DatePeriod standard = new DatePeriod(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31)
            );

            List<DatePeriod> periods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 5, 6),
                    LocalDate.of(2024, 5, 20))
            );

            List<LocalDate> companyHolidays = List.of(
                LocalDate.of(2024, 5, 10),  // 평일
                LocalDate.of(2024, 5, 11),  // 토
                LocalDate.of(2024, 5, 15)   // 법정공휴일(중복)
            );

            // When
            int result = calculatePrescribedWorkingDaysWithinPeriods(periods, standard,
                companyHolidays, STATUTORY_HOLIDAYS_2024);

            // Then
            assertEquals(8, result);

        }

        @Test
        @DisplayName("2024년 5월 중 5.6 ~ 5.16, 5.20 ~ 5.25 까지 결근했을 때 결근한 소정근로일은 12일이다.")
        void returns12WorkingDaysWhenAbsentFromMay6To16AndMay20To25() {
            // Given
            DatePeriod standard = new DatePeriod(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31)
            );

            List<DatePeriod> periods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 5, 6),
                    LocalDate.of(2024, 5, 16)),
                new DatePeriod(
                    LocalDate.of(2024, 5, 20),
                    LocalDate.of(2024, 5, 25))
            );

            List<LocalDate> companyHolidays = List.of();

            // When
            int result = calculatePrescribedWorkingDaysWithinPeriods(periods, standard,
                companyHolidays, STATUTORY_HOLIDAYS_2024);

            // Then
            assertEquals(12, result);

        }

        @Test
        @DisplayName("2024년 5월 중 6.2 ~ 6.15 까지 결근했을 때 결근한 소정근로일은 0일이다.")
        void returnsZeroWorkingDaysWhenAbsentFromJune2To15() {
            // Given
            DatePeriod standard = new DatePeriod(
                LocalDate.of(2024, 5, 1),
                LocalDate.of(2024, 5, 31)
            );

            List<DatePeriod> periods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 6, 2),
                    LocalDate.of(2024, 6, 15))
            );

            List<LocalDate> companyHolidays = List.of();

            // When
            int result = calculatePrescribedWorkingDaysWithinPeriods(periods, standard,
                companyHolidays, STATUTORY_HOLIDAYS_2024);

            // Then
            assertEquals(0, result);

        }
    }

    @Nested
    @DisplayName("소정근로비율(PWR)을 계산하는 함수")
    class calculatePrescribedWorkingRatioTest {

        @Test
        @DisplayName("소정근로제외일수가 0이면, 소정근로비율(PWR)은 1이다.")
        void returnsOneAsWorkingRatioWhenExcludedWorkingDaysIsZero() {
            // Given
            int prescribedWorkingDays = 246;
            int excludedWorkingDays = 0;

            // When
            double result = calculatePrescribedWorkingRatio(prescribedWorkingDays,
                excludedWorkingDays);

            // Then
            assertEquals(1, result);
        }

        @Test
        @DisplayName("소정근로제외일수가 소정근로일 수와 같으면 소정근로비율(PWR)은 0이다.")
        void returnsZeroAsWorkingRatioWhenExcludedDaysEqualPrescribedWorkingDays() {
            // Given
            int prescribedWorkingDays = 246;
            int excludedWorkingDays = 246;

            // When
            double result = calculatePrescribedWorkingRatio(prescribedWorkingDays,
                excludedWorkingDays);

            // Then
            assertEquals(0, result);
        }
    }

    @Nested
    @DisplayName("기간 내 소정근로일만 추출해 DatePeriod 단위로 변환하는 함수")
    class filterWorkingDayOnlyPeriodsTest {

        @Test
        @DisplayName("빈 리스트를 넣은 경우 빈 리스트를 반환한다")
        void returnsEmptyList_whenInputIsEmpty() {
            // Given
            List<DatePeriod> periods = List.of();
            List<LocalDate> companyHolidays = List.of();
            List<LocalDate> statutoryHolidays = List.of();
            // When
            List<DatePeriod> result = filterWorkingDayOnlyPeriods(periods, companyHolidays,
                statutoryHolidays);
            // Then

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("2025년 5월의 소정근로일은 20일(주말 9일, 공휴일 2일)이다.")
        void returns20WhenWorkingDaysInMay2025() {
            // Given
            List<DatePeriod> periods = List.of(
                new DatePeriod(
                    LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 5, 6)
                ),
                new DatePeriod(
                    LocalDate.of(2025, 5, 7),
                    LocalDate.of(2025, 5, 31)
                )
            );
            List<LocalDate> statutoryHolidays = List.of(
                LocalDate.of(2025, 5, 5),
                LocalDate.of(2025, 5, 6)
            );
            List<LocalDate> companyHolidays = List.of();
            // When
            List<DatePeriod> result = filterWorkingDayOnlyPeriods(periods, companyHolidays,
                statutoryHolidays);
            // Then
            List<LocalDate> actualDates = result.stream()
                .flatMap(p -> p.startDate().datesUntil(p.endDate().plusDays(1)))
                .sorted()
                .toList();

            List<LocalDate> expectedDates = LocalDate.of(2025, 5, 1)
                .datesUntil(LocalDate.of(2025, 5, 31).plusDays(1))
                .filter(AnnualLeaveHelper::isWeekday)
                .filter(d -> !statutoryHolidays.contains(d))
                .sorted()
                .toList();

            assertEquals(expectedDates, actualDates);
            assertEquals(20,actualDates.size());
        }

        @Test
        @DisplayName("2025년 5.1 ~ 5.10 , 5.16 ~ 5.20 까지 소정근로일은 8일이다.")
        void returns8WorkingDays_whenPeriodsAreMay1To10AndMay16To20_2025() {
            // Given
            List<DatePeriod> periods = List.of(
                new DatePeriod(
                    LocalDate.of(2025, 5, 1),
                    LocalDate.of(2025, 5, 10)
                ),
                new DatePeriod(
                    LocalDate.of(2025, 5, 16),
                    LocalDate.of(2025, 5, 20)
                )
            );
            List<LocalDate> statutoryHolidays = List.of(
                LocalDate.of(2025, 5, 5),
                LocalDate.of(2025, 5, 6)
            );
            List<LocalDate> companyHolidays = List.of();
            // When
            List<DatePeriod> result = filterWorkingDayOnlyPeriods(periods, companyHolidays,
                statutoryHolidays);
            // Then
            List<LocalDate> actualDates = result.stream()
                .flatMap(p -> p.startDate().datesUntil(p.endDate().plusDays(1)))
                .sorted()
                .toList();

            List<LocalDate> expectedDates = List.of(
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 2),
                LocalDate.of(2025, 5, 7),
                LocalDate.of(2025, 5, 8),
                LocalDate.of(2025, 5, 9),
                LocalDate.of(2025, 5, 16),
                LocalDate.of(2025, 5, 19),
                LocalDate.of(2025, 5, 20)
            );


            assertEquals(expectedDates, actualDates);
            assertEquals(8,actualDates.size());
        }
    }
}
