//package com.lawding.leavecalc.strategy;
//
//import com.lawding.leavecalc.domain.AnnualLeaveContext;
//import com.lawding.leavecalc.domain.CalculationType;
//import com.lawding.leavecalc.dto.AnnualLeaveResult;
//import com.lawding.leavecalc.domain.DatePeriod;
//import com.lawding.leavecalc.flow.CalculationFlow;
//import com.lawding.leavecalc.flow.HireDateFlow;
//import com.lawding.leavecalc.repository.HolidayJdbcRepository;
//import java.time.LocalDate;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//public class HireDateStrategyTest {
//
//
//    @Mock
//    HolidayJdbcRepository holidayRepository;
//
//    @InjectMocks
//    HireDateStrategy hireDateStrategy;
//
//    private static Set<LocalDate> getHolidayList() {
//        return Set.of(
//            LocalDate.of(2024, 1, 1),
//            LocalDate.of(2024, 2, 9),
//            LocalDate.of(2024, 2, 10),
//            LocalDate.of(2024, 2, 11),
//            LocalDate.of(2024, 2, 12),
//            LocalDate.of(2024, 3, 1),
//            LocalDate.of(2024, 4, 10),
//            LocalDate.of(2024, 5, 5),
//            LocalDate.of(2024, 5, 6),
//            LocalDate.of(2024, 5, 15),
//            LocalDate.of(2024, 6, 6),
//            LocalDate.of(2024, 8, 15),
//            LocalDate.of(2024, 9, 16),
//            LocalDate.of(2024, 9, 17),
//            LocalDate.of(2024, 9, 18),
//            LocalDate.of(2024, 10, 1),
//            LocalDate.of(2024, 10, 3),
//            LocalDate.of(2024, 10, 9),
//            LocalDate.of(2024, 12, 25)
//        );
//    }
//
//    @BeforeEach
//    void setUp() {
//        when(holidayRepository.findWeekdayHolidays(any(DatePeriod.class))).thenReturn(
//            getHolidayList());
//        CalculationFlow flow = new HireDateFlow(holidayRepository);
//        hireDateStrategy = new HireDateStrategy(flow);
//    }
//
//    @Nested
//    @DisplayName("입사일 1년 미만인 경우(월차)")
//    class BeforeOneYearFromHireDateTests {
//
//        @Test
//        @DisplayName("6개월간(+a일) 개근한 근로자는 6개의 월차가 발생한다.")
//        void returns6WhenPerfectAttendance() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2024, 7, 1);
//            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//            List<DatePeriod> absentPeriods = List.of();
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .calculationType(CalculationType.HIRE_DATE)
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(1, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//            // Then
//            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            assertEquals(6,result.calculationDetail().getTotalLeaveDays());
//        }
//
//        @Test
//        @DisplayName("6개월 -1일 개근한 근로자는 5개의 월차가 발생한다.")
//        void returns5WhenPerfectAttendance() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2024, 7, 1);
//            LocalDate referenceDate = LocalDate.of(2024, 12, 31);
//            List<DatePeriod> absentPeriods = List.of();
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(2, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//            // Then
//            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
////            assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
//        }
//
//        @Test
//        @DisplayName("11개월간 개근한 근로자는 11개(최대)의 월차가 발생한다.")
//        void returns11WhenPerfectAttendance() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2024, 7, 1);
//            LocalDate referenceDate = LocalDate.of(2025, 6, 10);
//            List<DatePeriod> absentPeriods = List.of();
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(2, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//            // Then
//            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//        }
//
//        @Test
//        @DisplayName("6개월 중 결근이 있는 달(8월)이 있을 때, 5개의 월차가 발생한다.")
//        void returns5WhenOneMonthHasAbsenceWithin6Months() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2024, 7, 1);
//            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//            List<DatePeriod> absentPeriods = List.of(
//                new DatePeriod(
//                    LocalDate.of(2024, 8, 2),
//                    LocalDate.of(2024, 8, 8)
//                )
//            );
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(2, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//            // Then
//            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//        }
//
//        @Test
//        @DisplayName("6개월 중 결근이 있는 날이 휴일(주말/공휴일)이면 6개의 월차가 발생한다.")
//        void return6WhenAbsenceIsOnHolidayWithin6Months() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2024, 7, 1);
//            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//            List<DatePeriod> absentPeriods = List.of(
//                new DatePeriod(
//                    LocalDate.of(2024, 8, 3), // 토
//                    LocalDate.of(2024, 8, 4)  // 일
//                ),
//                new DatePeriod(
//                    LocalDate.of(2024, 8, 15), // 공휴일
//                    LocalDate.of(2024, 8, 15)
//                )
//            );
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(2, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//            // Then
//            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//        }
//
//    }
//
//
//    @Nested
//    @DisplayName("입사일 1년 이상인 경우")
//    class AfterThanOneYearHireDateTests {
//
//        @Test
//        @DisplayName("AR & PWR 80% 이상인 1년 차 근로자의 연차 개수는 15개이다.")
//        void return15WhenFirstYearEmployeeHasARAndPWROver80Percent() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2024, 1, 1);
//            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//            List<DatePeriod> absentPeriods = List.of();
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(2, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//
//        }
//
//        @Test
//        @DisplayName("AR & PWR 80% 이상인 3년 차 근로자의 연차 개수는 16개이다.")
//        void return16WhenThreeYearsEmployeeHasARAndPWROver80Percent() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2024, 1, 1);
//            LocalDate referenceDate = LocalDate.of(2027, 1, 1);
//            List<DatePeriod> absentPeriods = List.of();
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(2, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//            // Then
//
//        }
//
//        @Test
//        @DisplayName("AR & PWR 80% 이상인 23년 차 근로자의 연차 개수는 25개(최대)이다.")
//        void return25WhenTwentyFiveYearsEmployeeHasARAndPWROver80Percent() {
//            // Given
//            LocalDate hireDate = LocalDate.of(2001, 1, 1);
//            LocalDate referenceDate = LocalDate.of(2024, 1, 1);
//            List<DatePeriod> absentPeriods = List.of();
//            AnnualLeaveContext context = AnnualLeaveContext.builder()
//                .hireDate(hireDate)
//                .referenceDate(referenceDate)
//                .nonWorkingPeriods(Map.of(2, absentPeriods))
//                .companyHolidays(List.of())
//                .build();
//            // When
//            AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//            // Then
//
//        }
//
//        @Nested
//        @DisplayName("출근율(AR) 분기(경계값) 테스트")
//        class AttendanceRateBoundaryTests {
//
//            @Test
//            @DisplayName("2024년 6 ~ 8월 중 소정근로일 50일을 결근한 경우, 출근율(AR)이 79.67%이므로 월차는 9개다.")
//            void return9WhenEmployeeHasARUnder80Percent() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(List.of())
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(2)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 6 ~ 8월 중 결근 기간(소정근로일 50일)을 겹쳐서 기간 기재한 경우, 출근율(AR)이 79.67%이므로 월차는 9개다.")
//            void return9WhenEmployeeHasARUnder80PercentAndOverlapDuration50s() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 7, 29)
//                    ),
//                    new DatePeriod(
//                        LocalDate.of(2024, 7, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(List.of())
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(2)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 결근 기간(소정근로일 50일) 외 회사 휴일이 1일인 경우, 출근율(AR)이 79.59%이므로 월차는 9개다.")
//            void return9WhenEmployeeHasARUnder80PercentCase1() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 14)
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(2)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 결근 기간(소정근로일 50일) 외 회사 휴일이 2일(주말 1일)인 경우, 출근율(AR)이 79.59%이므로 월차는 9개다.")
//            void return9WhenEmployeeHasARUnder80PercentCase2() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 14),
//                    LocalDate.of(2024, 8, 17)   // 토
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(2)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 결근 기간(소정근로일 50일) 외 회사 휴일이 2일(공휴일 1일)인 경우, 출근율(AR)이 79.59%이므로 월차는 9개다.")
//            void return9WhenEmployeeHasARUnder80PercentCase3() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 14),
//                    LocalDate.of(2024, 8, 15) // 법정공휴일
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(2)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 결근 기간(소정근로일 50일) 내 회사 휴일이 1일인 경우, 출근율(AR)이 80%이므로 연차는 15개다.")
//            void return15WhenEmployeeHasARIsExactly80PercentCase1() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 1)
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 결근 기간(소정근로일 50일) 내 회사 휴일이 2일(주말 1일)인 경우, 출근율(AR)이 80%이므로 연차는 15개다.")
//            void return15WhenEmployeeHasARIsExactly80PercentCase2() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 1),
//                    LocalDate.of(2024, 8, 3) // 토요일
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 결근 기간(소정근로일 50일) 내 회사 휴일이 2일(법정공휴일 1일)인 경우, 출근율(AR)이 80%이므로 연차는 15개다.")
//            void return15WhenEmployeeHasARIsExactly80PercentCase3() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 6, 6), // 법정공휴일
//                    LocalDate.of(2024, 8, 1)
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(2, absentPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 결근 기간(소정근로일 50일) 내 소정근로제외기간이 1일인 경우, 출근율(AR)이 80%이므로 연차는 15개다.")
//            void returns15WhenAttendanceRateIsEightyPercentAndOneExcludedDay() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> absentPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 8, 8),
//                        LocalDate.of(2024, 8, 8)
//                    )
//                );
//
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(
//                        2, absentPeriods,
//                        3, excludedPeriods
//                    ))
//                    .companyHolidays(List.of())
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//        }
//
//        @Nested
//        @DisplayName("소정근로비율(PWR) 분기(경계값) 테스트")
//        class PrescribeWorkingRatioBoundaryTests {
//
//            @Test
//            @DisplayName("1년차 근로자의 소정근로제외일 수가 50일인 경우, 소정근로비율(PWR)은 79.67%이므로 비례삭감하여 적용한다.")
//            void returnProratedLeaveWhenFirstYearAndPWRIsUnder80Percent() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of();
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("3년차 근로자의 소정근로제외일 수가 50일인 경우, 소정근로비율(PWR)은 79.67%이므로 비례삭감하여 적용한다.")
//            void returnProratedLeaveWhenThirdYearAndPWRIsUnder80Percent() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2022, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of();
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("23년차 근로자의 소정근로제외일 수가 50일인 경우, 소정근로비율(PWR)은 79.67%이므로 비례삭감하여 적용한다.")
//            void returnProratedLeaveWhenTwentyThirdYearAndPWRIsUnder80Percent() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2002, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of();
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 6 ~ 8월 중 소정근로제외기간(소정근로일 50일)을 겹쳐서 기간 기재한 경우, 소정근로비율(PWR)이 79.67%이므로 비례삭감하여 적용한다.")
//            void returnProratedLeaveWhenOverlappingExcludedPeriodsEqual50Days() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 7, 29)
//                    ),
//                    new DatePeriod(
//                        LocalDate.of(2024, 7, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(List.of())
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 소정근로제외기간(소정근로일 50일) 외 회사 휴일이 1일인 경우, 소정근로비율(PWR)이 79.59%이므로 비례삭감하여 적용한다.")
//            void returnProratedLeaveWhenPWRIsUnder80PercentWith1CompanyHoliday() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 14)
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 소정근로제외기간(소정근로일 50일) 외 회사 휴일이 2일(주말 1일)인 경우, 소정근로비율(PWR)이 79.59%이므로 비례삭감하여 적용한다.")
//            void returnProratedLeaveWhenPWRIsUnder80PercentWith2CompanyHolidaysIncludingWeekend() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 14),
//                    LocalDate.of(2024, 8, 17)   // 토
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 소정근로제외기간(소정근로일 50일) 외 회사 휴일이 2일(공휴일 1일)인 경우, 소정근로비율(PWR)이 79.59%이므로 비례삭감하여 적용한다.")
//            void returnProratedLeaveWhenPWRIsUnder80PercentWith1PublicHoliday() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 14),
//                    LocalDate.of(2024, 8, 15) // 법정공휴일
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 소정근로제외기간(소정근로일 50일) 내 회사 휴일이 1일인 경우, 소정근로비율(PWR)이 80%이므로 연차는 15개다.")
//            void return15WhenPWRIsExactly80With1CompanyHolidayInExcludedPeriod() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 1)
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 소정근로제외기간(소정근로일 50일) 내 회사 휴일이 2일(주말 1일)인 경우, 소정근로비율(PWR)이 80%이므로 연차는 15개다.")
//            void return15WhenPWRIsExactly80With2CompanyHolidaysIncludingWeekend() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 8, 1),
//                    LocalDate.of(2024, 8, 3) // 토요일
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//
//            @Test
//            @DisplayName("2024년 소정근로제외기간(소정근로일 50일) 내 회사 휴일이 2일(법정공휴일 1일)인 경우, 소정근로비율(PWR)이 80%이므로 연차는 15개다.")
//            void return15WhenPWRIsExactly80With1PublicHolidayInExcludedPeriod() {
//                // Given
//                LocalDate hireDate = LocalDate.of(2024, 1, 1);
//                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
//                List<DatePeriod> excludedPeriods = List.of(
//                    new DatePeriod(
//                        LocalDate.of(2024, 6, 1),
//                        LocalDate.of(2024, 8, 12)
//                    )
//                );
//                List<LocalDate> companyHolidays = List.of(
//                    LocalDate.of(2024, 6, 6), // 법정공휴일
//                    LocalDate.of(2024, 8, 1)
//                );
//                AnnualLeaveContext context = AnnualLeaveContext.builder()
//                    .hireDate(hireDate)
//                    .referenceDate(referenceDate)
//                    .nonWorkingPeriods(Map.of(3, excludedPeriods))
//                    .companyHolidays(companyHolidays)
//                    .build();
//
//                // When
//                AnnualLeaveResult result = hireDateStrategy.annualLeaveCalculate(context);
//
//                // Then
//                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
//            }
//        }
//    }
//
//}
