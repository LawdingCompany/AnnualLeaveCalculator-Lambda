package com.lawding.leavecalc.strategy;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.domain.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class HireDateStrategyTest {

    private final HireDateStrategy strategy = new HireDateStrategy();

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

        // when
        AnnualLeaveResult result = strategy.annualLeaveCalculate(context);

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
        AnnualLeaveResult result = strategy.annualLeaveCalculate(context);

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
        AnnualLeaveResult result = strategy.annualLeaveCalculate(context);

        //then
        assertEquals(4, result.getAnnualLeaveDays());
    }
}
