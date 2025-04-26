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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HireDateStrategyTest {

    @Mock
    HolidayJdbcRepository holidayRepository;

    @InjectMocks
    HireDateStrategy hireDateStrategy;

    @Test
    void 입사일_1년미만_개근한_근로자는_월마다_연차가_1개씩_발생한다() {
        // given
        LocalDate hireDate = LocalDate.of(2024, 1, 1);
        LocalDate referenceDate = LocalDate.of(2024, 7, 1);
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
}
