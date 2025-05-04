package com.lawding.leavecalc.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.time.MonthDay;
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

@ExtendWith(MockitoExtension.class)
public class FiscalYearStrategyTest {

    @Mock
    HolidayJdbcRepository holidayRepository;

    @InjectMocks
    FiscalYearStrategy fiscalYearStrategy;

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
    @DisplayName("산정기준일이 첫 정기 회계연도 이전인 경우")
    class BeforeFirstRegularFiscalYearTests {

        @Test
        @DisplayName("산정기준일이 입사일과 같은 회계연도인 경우")
        void returnMonthlyLeaveWhenReferenceDateIsInSameFiscalYear(){
            // Given
            LocalDate hireDate = LocalDate.of(2024, 7, 1);
            LocalDate referenceDate = LocalDate.of(2024, 12, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> excludedPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(2, excludedPeriods))
                .companyHolidays(List.of())
                .build();


            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
        }

    }

    @Test
    void 입사일_1년미만_첫_회계연도_도래_이전_회계연도_시작일과_동일하지_않은_경우() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 7, 1);
        LocalDate referenceDate = LocalDate.of(2024, 12, 1);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년이상_월차와_비례연차가_같이_발생한_경우() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 7, 1);
        LocalDate referenceDate = LocalDate.of(2025, 5, 5);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년이상_첫_정기_회계연도_이후() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 7, 1);
        LocalDate referenceDate = LocalDate.of(2026, 2, 5);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년이상_첫_정기_회계연도_이후_입사일과_회계연도_동일_가산연차_유() {
        // given
        LocalDate hireDate = LocalDate.of(2021, 1, 1);
        LocalDate referenceDate = LocalDate.of(2026, 2, 5);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(17, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일_1년이상_첫_정기_회계연도_이후_가산연차() {
        // given
        LocalDate hireDate = LocalDate.of(2021, 1, 1);
        LocalDate referenceDate = LocalDate.of(2026, 2, 5);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(17, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일과회계연도시작일이같지않을때_가산연차가없을때() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 3, 1);
        LocalDate referenceDate = LocalDate.of(2026, 2, 5);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일과회계연도시작일이같을때_가산연차가없을때() {
        // given
        LocalDate hireDate = LocalDate.of(2025, 1, 1);
        LocalDate referenceDate = LocalDate.of(2026, 2, 5);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 입사일1년이상_정규회계연도가도래한경우_가산연차없음() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2026, 2, 5);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 딱1년근무했을떄() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2024, 12, 31);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(11, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void 딱1년1일근무했을떄() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2025, 1, 1);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of();
        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(2, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
    }

    @Test
    void ddd() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2025, 1, 1);
        MonthDay fiscalYear = MonthDay.of(1, 1);
        List<DatePeriod> excludedPeriods = List.of(
            new DatePeriod(LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 10)),
            new DatePeriod(LocalDate.of(2024, 5, 1), LocalDate.of(2024, 5, 10))
        );

        AnnualLeaveContext context = AnnualLeaveContext.builder()
            .hireDate(hireDate)
            .referenceDate(referenceDate)
            .fiscalYear(fiscalYear)
            .nonWorkingPeriods(Map.of(3, excludedPeriods))
            .companyHolidays(List.of())
            .build();

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
    }
}
