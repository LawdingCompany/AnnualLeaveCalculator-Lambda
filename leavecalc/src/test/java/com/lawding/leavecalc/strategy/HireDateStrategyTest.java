package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HireDateStrategyTest {


    @Mock
    HolidayJdbcRepository holidayRepository;

    @InjectMocks
    HireDateStrategy hireDateStrategy;

    private static List<LocalDate> getHolidayList() {
        return List.of(
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
    }

    @BeforeEach
    void setUp() {
        when(holidayRepository.findWeekdayHolidays(any(DatePeriod.class))).thenReturn(
            getHolidayList());
    }

    @Nested
    @DisplayName("입사일 1년 미만인 경우(월차)")
    class LessThanOneYearHireTests {

        @Test
        @DisplayName("6개월간(+a일) 개근한 근로자는 6개의 월차가 발생한다.")
        void returns6WhenPerfectAttendance() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 7, 1);
            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
            List<DatePeriod> excludedPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .nonWorkingPeriods(Map.of(2, excludedPeriods))
                .companyHolidays(List.of())
                .build();
            // When
            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

            // Then
            assertEquals(6, result.getCalculationDetail().getTotalLeaveDays());
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
        }

        @Test
        @DisplayName("6개월 -1일 개근한 근로자는 5개의 월차가 발생한다.")
        void returns5WhenPerfectAttendance() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 7, 1);
            LocalDate referenceDate = LocalDate.of(2024, 12, 31);
            List<DatePeriod> excludedPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .nonWorkingPeriods(Map.of(2, excludedPeriods))
                .companyHolidays(List.of())
                .build();
            // When
            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

            // Then
            assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("11개월간 개근한 근로자는 11개(최대)의 월차가 발생한다.")
        void returns11WhenPerfectAttendance() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 7, 1);
            LocalDate referenceDate = LocalDate.of(2025, 6, 10);
            List<DatePeriod> excludedPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .nonWorkingPeriods(Map.of(2, excludedPeriods))
                .companyHolidays(List.of())
                .build();
            // When
            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

            // Then
            assertEquals(11, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("6개월 중 결근이 있는 달(8월)이 있을 때, 5개의 월차가 발생한다.")
        void returns5WhenOneMonthHasAbsenceWithin6Months() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 7, 1);
            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 8, 2),
                    LocalDate.of(2024, 8, 8)
                )
            );
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .nonWorkingPeriods(Map.of(2, excludedPeriods))
                .companyHolidays(List.of())
                .build();
            // When
            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

            // Then
            assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
        }
        @Test
        @DisplayName("6개월 중 결근이 있는 날이 휴일(주말/공휴일)이면 6개의 월차가 발생한다.")
        void return5WhenOneMonthHasAbsenceWithin6Months() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 7, 1);
            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 8, 3), // 토
                    LocalDate.of(2024, 8, 4)  // 일
                ),
                new DatePeriod(
                    LocalDate.of(2024, 8, 15), // 공휴일
                    LocalDate.of(2024, 8, 15)
                )
            );
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .nonWorkingPeriods(Map.of(2, excludedPeriods))
                .companyHolidays(List.of())
                .build();
            // When
            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

            // Then
            assertEquals(6, result.getCalculationDetail().getTotalLeaveDays());
        }

    }

    @Test
    void 입사일_1년미만_개근한_근로자는_월마다_연차가_1개씩_발생한다() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 7, 1);
        LocalDate referenceDate = LocalDate.of(2025, 1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(6, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년미만_개근한_근로자는_월차_최댓값() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2024, 12, 31);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(11, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년미만_개근한_근로자는_월마다_연차가_1개씩_발생한다_경계값() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2024, 6, 30);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년미만_결근이_있는_달은_연차가_발생하지_않는다() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2024, 7, 1);
        List<DatePeriod> excludedPeriods = List.of(new DatePeriod(
                LocalDate.of(2024, 4, 2),
                LocalDate.of(2024, 5, 20)
            )
        );
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(4, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년이상_근로자의_일반적인_연차_개수_가산연차없음() {
        // given
        LocalDate hireDate = LocalDate.of(2022, 7, 1);
        LocalDate referenceDate = LocalDate.of(2025, 1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년이상_근로자의_일반적인_연차_개수_가산연차있음() {
        // given
        LocalDate hireDate = LocalDate.of(2018, 7, 1);
        LocalDate referenceDate = LocalDate.of(2025, 1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(17, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년미만_결근이있는경우_그게주말인경우_고려해야함() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 7, 1);
        LocalDate referenceDate = LocalDate.of(2025, 4, 1);
        List<DatePeriod> excludedPeriods = List.of(
            new DatePeriod(
                LocalDate.of(2024, 7, 7), // 일요일
                LocalDate.of(2024, 7, 7)
            )
        );
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(9, result.getCalculationDetail().getTotalLeaveDays());
    }


    @Test
    void 입사일_1년이상_출근율이_80퍼미만일때() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2025, 4, 1);
        List<DatePeriod> excludedPeriods = List.of(
            new DatePeriod(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 4, 30))
        );
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(8, result.getCalculationDetail().getTotalLeaveDays());
    }
}
