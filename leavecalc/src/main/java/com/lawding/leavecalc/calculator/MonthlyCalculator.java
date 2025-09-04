package com.lawding.leavecalc.calculator;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MAX_MONTHLY_LEAVE;
import static com.lawding.leavecalc.util.AnnualLeaveHelper.formatDouble;

import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.domain.MonthlyLeaveRecord;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import com.lawding.leavecalc.dto.detail.CalculationDetail;
import com.lawding.leavecalc.dto.detail.MonthlyDetail;
import com.lawding.leavecalc.util.AnnualLeaveHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MonthlyCalculator implements LeaveCalculator<MonthlyContext> {

    @Override
    public CalculationDetail calculate(MonthlyContext context) {
        DatePeriod period = context.getAccrualPeriod();
        Set<LocalDate> absentDays = context.getAbsentDays();
        Set<LocalDate> excludedDays = new HashSet<>(context.getExcludedDays());
        Set<LocalDate> companyHolidays = new HashSet<>(context.getCompanyHolidays());
        Set<LocalDate> statutoryHolidays = context.getStatutoryHolidays();

        excludedDays.addAll(companyHolidays);

        List<MonthlyLeaveRecord> records = new ArrayList<>();
        double totalMonthlyLeaves = 0.0;

        LocalDate currentStart = period.startDate();

        while (totalMonthlyLeaves < MAX_MONTHLY_LEAVE) {
            LocalDate currentEnd = currentStart.plusMonths(1).minusDays(1);

            if (currentEnd.isAfter(period.endDate())) {
                break;
            }

            // 간격 내 소정근로일
            Set<LocalDate> prescribedSet = currentStart
                .datesUntil(currentEnd.plusDays(1))
                .filter(AnnualLeaveHelper::isWeekday)
                .filter(day -> !statutoryHolidays.contains(day))
                .collect(Collectors.toSet());

            int denominator = prescribedSet.size();
            double granted = 0.0;

            if (denominator > 0) {

                boolean hasAbsence = absentDays.stream()
                    .anyMatch(prescribedSet::contains);

                if (!hasAbsence) {
                    int excludedDay = (int) excludedDays.stream()
                        .filter(prescribedSet::contains)
                        .count();

                    int attendanceDays = denominator - excludedDay;
                    granted = (double) attendanceDays / denominator;
                }

            }
            granted = formatDouble(granted);
            records.add(
                MonthlyLeaveRecord.builder()
                    .period(new DatePeriod(currentStart, currentEnd))
                    .monthlyLeave(granted)
                    .build()
            );

            totalMonthlyLeaves = Math.min(MAX_MONTHLY_LEAVE, totalMonthlyLeaves + granted);
            if (totalMonthlyLeaves >= MAX_MONTHLY_LEAVE) {
                break;
            }

            currentStart = currentEnd.plusDays(1);
        }

        return MonthlyDetail.builder()
            .records(records)
            .totalLeaveDays(totalMonthlyLeaves)
            .build();
    }
}
