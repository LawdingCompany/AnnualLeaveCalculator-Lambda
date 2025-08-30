package com.lawding.leavecalc.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.lawding.leavecalc.domain.AnnualLeaveContext;
import com.lawding.leavecalc.dto.AnnualLeaveResult;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.dto.detail.FullAnnualLeaveDetail;
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

        @Nested
        @DisplayName("산정기준일이 입사일과 같은 회계연도인 경우")
        class ReferenceDateAndHireDateAreInSameFiscalYear {

            @Test
            @DisplayName("2024년 7월 ~ 12월 31일까지 개근한 근로자의 월차는 5개다.")
            void return5WhenFullyAttendedInSameFiscalYear() {
                // Given
                LocalDate hireDate = LocalDate.of(2024, 7, 1);
                LocalDate referenceDate = LocalDate.of(2024, 12, 31);
                MonthDay fiscalYear = MonthDay.of(1, 1);
                List<DatePeriod> absentPeriods = List.of();
                AnnualLeaveContext context = AnnualLeaveContext.builder()
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .fiscalYear(fiscalYear)
                    .nonWorkingPeriods(Map.of(2, absentPeriods))
                    .companyHolidays(List.of())
                    .build();

                // When
                AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

                //Then
                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
                assertEquals(5, result.getCalculationDetail().getTotalLeaveDays());
            }

            @Test
            @DisplayName("2024년 7월 ~ 12월 5일동안 결근이 있는 달이 1개월 있을 경우 월차는 4개다.")
            void return4WhenOneMonthHasAbsenceDuringAccrualPeriod() {
                // Given
                LocalDate hireDate = LocalDate.of(2024, 7, 1);
                LocalDate referenceDate = LocalDate.of(2024, 12, 5);
                MonthDay fiscalYear = MonthDay.of(1, 1);
                List<DatePeriod> absentPeriods = List.of(
                    new DatePeriod(
                        LocalDate.of(2024, 9, 1),
                        LocalDate.of(2024, 9, 5)
                    )
                );
                AnnualLeaveContext context = AnnualLeaveContext.builder()
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .fiscalYear(fiscalYear)
                    .nonWorkingPeriods(Map.of(2, absentPeriods))
                    .companyHolidays(List.of())
                    .build();

                // When
                AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);
                //Then
                verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
                assertEquals(4, result.getCalculationDetail().getTotalLeaveDays());
            }

        }

        @Nested
        @DisplayName("산정기준일이 입사일과 다른 회계연도인 경우")
        class ReferenceDateIsInDifferentFiscalYearFromHireDate {

            @Test
            @DisplayName("산정기준일(1/1)이 다음 회계연도 시작일(1/1)인 경우(경계값), 월차와 비례연차가 발생하여 총 13.57개이다.")
            void return4WhenOneMonthHasAbsenceDuringAccrualPeriod() {
                // Given
                LocalDate hireDate = LocalDate.of(2024, 7, 1);
                LocalDate referenceDate = LocalDate.of(2025, 1, 1);
                MonthDay fiscalYear = MonthDay.of(1, 1);
                List<DatePeriod> absentPeriods = List.of();
                AnnualLeaveContext context = AnnualLeaveContext.builder()
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .fiscalYear(fiscalYear)
                    .nonWorkingPeriods(Map.of(2, absentPeriods))
                    .companyHolidays(List.of())
                    .build();

                // When
                AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

                //Then
                verify(holidayRepository, times(3)).findWeekdayHolidays(any(DatePeriod.class));
                assertEquals(13.57, result.getCalculationDetail().getTotalLeaveDays());
            }

            @Test
            @DisplayName("산정기준일(6/30)이 입사 후 1년(7/1)이 되기 전인 경우, 월차와 비례연차가 발생하여 총 18.57개이다.")
            void return18Point57WhenReferenceDateBeforeOneYear() {
                // Given
                LocalDate hireDate = LocalDate.of(2024, 7, 1);
                LocalDate referenceDate = LocalDate.of(2025, 6, 30);
                MonthDay fiscalYear = MonthDay.of(1, 1);
                List<DatePeriod> absentPeriods = List.of();
                AnnualLeaveContext context = AnnualLeaveContext.builder()
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .fiscalYear(fiscalYear)
                    .nonWorkingPeriods(Map.of(2, absentPeriods))
                    .companyHolidays(List.of())
                    .build();

                // When
                AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

                //Then
                verify(holidayRepository, times(3)).findWeekdayHolidays(any(DatePeriod.class));
                assertEquals(18.57, result.getCalculationDetail().getTotalLeaveDays());
            }

            @Test
            @DisplayName("산정기준일(6/30)이 입사 후 1년(7/1)이 되기 전 연차산정기간(입사일 ~ 회계연도종료일)의 출근율이 80% 미만인 경우, 월차는 9개이다.")
            void return9WhenBeforeOneYearAndARIsUnder80Percent() {
                // Given
                LocalDate hireDate = LocalDate.of(2024, 7, 1);
                LocalDate referenceDate = LocalDate.of(2025, 6, 30);
                MonthDay fiscalYear = MonthDay.of(1, 1);
                List<DatePeriod> absentPeriods = List.of(
                    new DatePeriod(
                        LocalDate.of(2024, 8, 1),
                        LocalDate.of(2024, 9, 11)
                    )
                );
                AnnualLeaveContext context = AnnualLeaveContext.builder()
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .fiscalYear(fiscalYear)
                    .nonWorkingPeriods(Map.of(2, absentPeriods))
                    .companyHolidays(List.of())
                    .build();

                // When
                AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

                //Then
                verify(holidayRepository, times(2)).findWeekdayHolidays(any(DatePeriod.class));
                assertEquals(9, result.getCalculationDetail().getTotalLeaveDays());
            }

            @Test
            @DisplayName("산정기준일이 입사 후 1년과 같을 경우, 비례연차가 발생하여 총 7.57개이다.")
            void return7Point57WhenReferenceDateEqualsOneYearAfterHire() {
                // Given
                LocalDate hireDate = LocalDate.of(2024, 7, 1);
                LocalDate referenceDate = LocalDate.of(2025, 7, 1);
                MonthDay fiscalYear = MonthDay.of(1, 1);
                List<DatePeriod> absentPeriods = List.of(
                );
                AnnualLeaveContext context = AnnualLeaveContext.builder()
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .fiscalYear(fiscalYear)
                    .nonWorkingPeriods(Map.of(2, absentPeriods))
                    .companyHolidays(List.of())
                    .build();

                // When
                AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

                //Then
                verify(holidayRepository, times(3)).findWeekdayHolidays(any(DatePeriod.class));
                assertEquals(7.57, result.getCalculationDetail().getTotalLeaveDays());
            }

            @Test
            @DisplayName("산정기준일이 입사 후 1년이상, 연차산정기간(입사일 ~ 회계연도 종료일)의 출근율이 80% 미만인 경우 월차는 10개이다.")
            void return10WhenOverOneYearButAttendanceRateIsBelow80Percent() {
                // Given
                LocalDate hireDate = LocalDate.of(2024, 7, 1);
                LocalDate referenceDate = LocalDate.of(2025, 9, 30);
                MonthDay fiscalYear = MonthDay.of(1, 1);
                List<DatePeriod> absentPeriods = List.of(
                    new DatePeriod(
                        LocalDate.of(2024, 8, 1),
                        LocalDate.of(2024, 9, 11)
                    )
                );
                AnnualLeaveContext context = AnnualLeaveContext.builder()
                    .hireDate(hireDate)
                    .referenceDate(referenceDate)
                    .fiscalYear(fiscalYear)
                    .nonWorkingPeriods(Map.of(2, absentPeriods))
                    .companyHolidays(List.of())
                    .build();

                // When
                AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

                //Then
                verify(holidayRepository, times(2)).findWeekdayHolidays(any(DatePeriod.class));
                assertEquals(10, result.getCalculationDetail().getTotalLeaveDays());
            }

        }

    }

    @Nested
    @DisplayName("산정기준일이 첫 정기 회계연도이거나 이후인 경우")
    class ReferenceDateIsAfterFirstFiscalYearStart {

        @Test
        @DisplayName("산정기준일이 첫 정기 회계연도와 같을 경우 연차는 15개이다.")
        void return15WhenReferenceDateIsFirstFiscalYearStartDate() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 1, 1);
            LocalDate referenceDate = LocalDate.of(2025, 1, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(2, absentPeriods))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);
            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("회계연도 1월 1일 기준 2024년 2월 1일 입사자의 2028년에 3년차이므로 연차는 16개이다.")
        void return16WhenInThirdFiscalYearCase1() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 2, 1);
            LocalDate referenceDate = LocalDate.of(2028, 1, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(2, absentPeriods))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);
            FullAnnualLeaveDetail detail = (FullAnnualLeaveDetail) result.getCalculationDetail();

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(16, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("회계연도 1월 1일 기준 2024년 1월 1일 입사자의 2027년에 3년차이므로 연차는 16개이다.")
        void return16WhenInThirdFiscalYearCase2() {
            // Given
            LocalDate hireDate = LocalDate.of(2024, 1, 1);
            LocalDate referenceDate = LocalDate.of(2027, 1, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(2, absentPeriods))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);
            FullAnnualLeaveDetail detail = (FullAnnualLeaveDetail) result.getCalculationDetail();
            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(16, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("위와 같은 조건의 23년차인 근로자의 연차는 25개(최대)이다.")
        void return25WhenInTwentyThreeFiscalYear() {
            // Given
            LocalDate hireDate = LocalDate.of(2004, 1, 1);
            LocalDate referenceDate = LocalDate.of(2027, 1, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of();
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(2, absentPeriods))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(25, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("연차산정기간(2024.1.1 - 2024.12.31) 내 6월 ~ 8월(50일) 결근해 AR이 79.67%이므로 월차는 9개이다.")
        void return9WhenARIs79Point67() {
            // Given
            LocalDate hireDate = LocalDate.of(2023, 1, 1);
            LocalDate referenceDate = LocalDate.of(2025, 7, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 7, 29)
                ),
                new DatePeriod(
                    LocalDate.of(2024, 7, 1),   // 기간 중복 겹침 테스트
                    LocalDate.of(2024, 8, 12)
                )
            );
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(2, absentPeriods))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(9, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("연차산정기간(2024.1.1 - 2024.12.31) 내 6월 ~ 8월(49일) 결근해 AR이 80.08%이므로 연차는 15개이다.")
        void return15WhenARIs80Point67() {
            // Given
            LocalDate hireDate = LocalDate.of(2023, 1, 1);
            LocalDate referenceDate = LocalDate.of(2025, 7, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 8, 11)
                )
            );
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(2, absentPeriods))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("연차산정기간(2024.1.1 - 2024.12.31) 중 결근 기간(6월 ~ 8월, 50일)내 소정근로제외일(1일)이 있어 AR이 80%이므로 연차는 15개이다.")
        void returns15WhenAttendanceRateIs80AndOneExcludedDay() {
            // Given
            LocalDate hireDate = LocalDate.of(2023, 1, 1);
            LocalDate referenceDate = LocalDate.of(2025, 7, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 8, 12)
                )
            );
            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 8, 9),
                    LocalDate.of(2024, 8, 9)
                )
            );
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(
                    2, absentPeriods,
                    3, excludedPeriods
                ))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("연차산정기간(2024.1.1 - 2024.12.31) 중 결근 기간(6월 ~ 8월, 50일)내 회사휴일(1일)이 있어 AR이 80%이므로 연차는 15개이다.")
        void returns15WhenAttendanceRateIs80AndOneCompanyHolidayWithinAbsencePeriod() {
            // Given
            LocalDate hireDate = LocalDate.of(2023, 1, 1);
            LocalDate referenceDate = LocalDate.of(2025, 7, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> absentPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 8, 12)
                )
            );
            List<LocalDate> companyHolidays = List.of(
                LocalDate.of(2024, 8, 9)
            );
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(
                    2, absentPeriods
                ))
                .companyHolidays(companyHolidays)
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
        }

        @Test
        @DisplayName("연차산정기간(2024.1.1 - 2024.12.31) 중 소정근로제외 기간(6월 ~ 8월, 50일)이므로 PWR이 79.67%이므로 연차는 11.96개이다.")
        void returns11Point96WhenPwrIsLessThan80AndExclusionPeriodOfFiftyDays() {
            // Given
            LocalDate hireDate = LocalDate.of(2023, 1, 1);
            LocalDate referenceDate = LocalDate.of(2025, 7, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 8, 12)
                )
            );

            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(
                    3, excludedPeriods
                ))
                .companyHolidays(List.of())
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(11.96, result.getCalculationDetail().getTotalLeaveDays());
        }
        @Test
        @DisplayName("연차산정기간(2024.1.1 - 2024.12.31) 중 소정근로제외 기간(6월 ~ 8월, 50일)내 회사휴일(1일)이 있어 PWR이 80%이므로 연차는 15개이다.")
        void returns15WhenPwrIs80AndOneCompanyHolidayWithinExclusionPeriod() {
            // Given
            LocalDate hireDate = LocalDate.of(2023, 1, 1);
            LocalDate referenceDate = LocalDate.of(2025, 7, 1);
            MonthDay fiscalYear = MonthDay.of(1, 1);
            List<DatePeriod> excludedPeriods = List.of(
                new DatePeriod(
                    LocalDate.of(2024, 6, 1),
                    LocalDate.of(2024, 8, 12)
                )
            );
            List<LocalDate> companyHolidays = List.of(
                LocalDate.of(2024, 8, 9)
            );
            AnnualLeaveContext context = AnnualLeaveContext.builder()
                .hireDate(hireDate)
                .referenceDate(referenceDate)
                .fiscalYear(fiscalYear)
                .nonWorkingPeriods(Map.of(
                    3, excludedPeriods
                ))
                .companyHolidays(companyHolidays)
                .build();

            // When
            AnnualLeaveResult result = fiscalYearStrategy.annualLeaveCalculate(context);

            //Then
            verify(holidayRepository, times(1)).findWeekdayHolidays(any(DatePeriod.class));
            assertEquals(15, result.getCalculationDetail().getTotalLeaveDays());
        }
    }


}
