package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.repository.HolidayJdbcRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class HireDateStrategyTest {

    @Mock
    HolidayJdbcRepository holidayRepository;

    @InjectMocks
    HireDateStrategy hireDateStrategy;

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

//        when(holidayRepository.findWeekdayHolidays(any()))
//            .thenReturn(List.of());

        // when
        AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);

        //then
        assertEquals(6, result.getAnnualLeaveDays());
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
        assertEquals(11, result.getAnnualLeaveDays());
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
        assertEquals(5, result.getAnnualLeaveDays());
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
        assertEquals(4, result.getAnnualLeaveDays());
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
        assertEquals(15, result.getAnnualLeaveDays());
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
        assertEquals(17, result.getAnnualLeaveDays());
    }
    @Test
    void 입사일_1년미만_결근이있는경우_그게주말인경우_고려해야함() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 7, 1);
        LocalDate referenceDate = LocalDate.of(2025, 4, 1);
        List<DatePeriod> excludedPeriods = List.of(
            new DatePeriod(LocalDate.of(2024, 7, 7), LocalDate.of(2024, 7, 7)) // 주말
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
        assertEquals(9, result.getAnnualLeaveDays());
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
        assertEquals(8, result.getAnnualLeaveDays());
    }
}
