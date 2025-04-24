package com.lawding.leavecalc.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Period;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LeaveCalculatorLatest {

    static Scanner sc = new Scanner(System.in);
    static DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // ExtraPeriod 클래스: 별도 처리 기간의 정보를 저장 / 비근무 기간 클래스
    static class ExtraPeriod {

        String type;  // "출근간주", "결근처리", "소정근로제외"
        LocalDate start;
        LocalDate end;

        public ExtraPeriod(String type, LocalDate start, LocalDate end) {
            this.type = type;
            this.start = start;
            this.end = end;
        }
    }

    // 입력용 헬퍼 메서드들
    static int getInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (Exception e) {
                System.out.println("정수 형식으로 입력해주세요. 예: 2024");
            }
        }
    }

    // String -> LocalDate 형식
    static LocalDate getDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return LocalDate.parse(sc.nextLine().trim(), dateFormatter);
            } catch (DateTimeParseException e) {
                System.out.println("날짜 형식이 올바르지 않습니다. 예: 2020-01-15");
            }
        }
    }

    // 프롬프트 창 출력
    static String getChoice(String prompt, List<String> choices) {
        while (true) {
            System.out.print(prompt + " " + choices + ": ");
            String input = sc.nextLine().trim();
            if (choices.contains(input)) {
                return input;
            }
            System.out.println("올바른 선택지를 입력해주세요: " + choices);
        }
    }

    // 정규식을 사용하여 회계연도 시작일 문자열 검증 (예: "1월 1일")
    static String getFiscalStartString(String prompt) {
        Pattern p = Pattern.compile("^\\d{1,2}월\\s?\\d{1,2}일$");
        while (true) {
            System.out.print(prompt);
            String s = sc.nextLine().trim();
            if (p.matcher(s).matches()) {
                return s;
            } else {
                System.out.println("입력 형식이 올바르지 않습니다. 예: 1월 1일");
            }
        }
    }


    // ExtraPeriod 입력 받기 & 비근무 기간 입력한 배열
    static List<ExtraPeriod> getExtraPeriods() {
        List<ExtraPeriod> extraPeriods = new ArrayList<>();
        System.out.println("별도 처리 기간을 추가 입력합니다. 입력을 마치려면 '끝'을 입력하세요.");
        while (true) {
            System.out.println("=== (출근간주기간) ===");
            System.out.println("1. 육아휴직기간");
            System.out.println("2. 출산전후휴가기간");
            System.out.println("3. 유사산휴가기간");
            System.out.println("4. 예비군훈련기간");
            System.out.println("5. 업무상 부상 또는 질병으로 휴업한 기간(산재인정기간)");
            System.out.println("6. 공민권 행사를 위한 휴무일");
            System.out.println("7. 배우자 출산휴가");
            System.out.println("8. 가족돌봄휴가기간");
            System.out.println("9. 부당해고기간");
            System.out.println("10. 불법직장폐쇄기간");
            System.out.println("=== (결근 처리기간) ===");
            System.out.println("11. 무단결근기간");
            System.out.println("12. 징계로 인한 정직, 강제휴직, 직위해제(출근X)기간");
            System.out.println("13. 불법쟁의행위기간");
            System.out.println("=== (소정근로 제외기간) ===");
            System.out.println("14. 병역의무 이행을 위한 휴직기간");
            System.out.println("15. 개인사유로 인한 휴직기간");
            System.out.println("16. 개인질병(업무상질병X)으로 인한 휴직기간");
            System.out.print("해당 별도 처리 기간의 종류(번호)를 입력하거나 '끝'을 입력하세요: ");
            String inp = sc.nextLine().trim();
            if (inp.equalsIgnoreCase("끝")) {
                break;
            }
            int num;
            try {
                num = Integer.parseInt(inp);
            } catch (NumberFormatException e) {
                System.out.println("번호는 1부터 16 사이여야 합니다.");
                continue;
            }
            if (num < 1 || num > 16) {
                System.out.println("번호는 1부터 16 사이여야 합니다.");
                continue;
            }
            String type = "";
            if (num >= 1 && num <= 10) {
                type = "출근간주";
            } else if (num >= 11 && num <= 13) {
                type = "결근처리";
            } else if (num >= 14 && num <= 16) {
                type = "소정근로제외";
            }
            LocalDate start = getDate("해당 기간의 시작일 (YYYY-MM-DD): ");
            LocalDate end = getDate("해당 기간의 종료일 (YYYY-MM-DD): ");
            extraPeriods.add(new ExtraPeriod(type, start, end));
        }
        return extraPeriods;
    }

    // 회계연도 산정단위기간 계산 (예: "1월 1일" 입력 시, 기준일 2025-03-06이면 2024-01-01 ~ 2024-12-31)
    static LocalDate[] computeFiscalCalcPeriod(String fiscalStartStr, LocalDate calcDate) {
        Pattern p = Pattern.compile("(\\d{1,2})월\\s*(\\d{1,2})일");
        Matcher m = p.matcher(fiscalStartStr);
        if (!m.find()) {
            throw new IllegalArgumentException("회계연도 시작일 입력 형식 오류");
        }
        int fiscalMonth = Integer.parseInt(m.group(1));
        int fiscalDay = Integer.parseInt(m.group(2));
        LocalDate candidate = LocalDate.of(calcDate.getYear(), fiscalMonth, fiscalDay);
        if (calcDate.isBefore(candidate)) {
            candidate = LocalDate.of(calcDate.getYear() - 1, fiscalMonth, fiscalDay);
        }
        LocalDate periodComplete = candidate.plusYears(1);
        LocalDate fiscalPeriodStart;
        if (calcDate.isBefore(periodComplete)) {
            fiscalPeriodStart = candidate.minusYears(1);
        } else {
            fiscalPeriodStart = candidate;
        }
        LocalDate fiscalPeriodEnd = fiscalPeriodStart.plusYears(1).minusDays(1);
        return new LocalDate[]{fiscalPeriodStart, fiscalPeriodEnd};
    }

    // 연차산정단위기간 계산: "입사일" 방식과 "회계연도" 방식 구분
    static LocalDate[] computeAnnualCalcPeriod(LocalDate hireDate, LocalDate calcDate, String mode,
        String fiscalStartStr) {
        if (mode.equals("입사일")) {
            if (calcDate.isBefore(hireDate.plusYears(1))) { // 2025-4-5 2024-6-2
                return new LocalDate[]{hireDate, calcDate};
            } else {
                int n = Period.between(hireDate, calcDate).getYears();
                LocalDate periodStart = hireDate.plusYears(n - 1);
                LocalDate periodEnd = hireDate.plusYears(n).minusDays(1);
                return new LocalDate[]{periodStart, periodEnd};
            }
        } else { // 회계연도
            return computeFiscalCalcPeriod(fiscalStartStr, calcDate);
        }
    }

    // 1년 미만 입사일 기준: hireDate부터 calcDate까지를 1달 단위의 기간으로 나누어 개근 연차 산출 (소정근로제외 비례 적용)
    static Object[] calculateEffectiveMonthlyLeave(LocalDate hireDate, LocalDate periodEnd,
        List<ExtraPeriod> extraPeriods) {
        double totalLeave = 0;
        List<String> breakdown = new ArrayList<>();
        LocalDate currentStart = hireDate;
        LocalDate fullPeriodEnd = currentStart.plusMonths(1).minusDays(1);
        while (!fullPeriodEnd.isAfter(periodEnd)) {
            int totalWd = countWeekdays(currentStart, fullPeriodEnd);
            int excludedWd = 0;
            for (ExtraPeriod ep : extraPeriods) {
                if (ep.type.equals("소정근로제외")) {
                    LocalDate interStart = currentStart.isAfter(ep.start) ? currentStart : ep.start;
                    LocalDate interEnd = fullPeriodEnd.isBefore(ep.end) ? fullPeriodEnd : ep.end;
                    if (!interStart.isAfter(interEnd)) {
                        excludedWd += countWeekdays(interStart, interEnd);
                    }
                }
            }
            double periodLeave =
                totalWd > 0 ? 1.0 * ((totalWd - excludedWd) / (double) totalWd) : 0;
            totalLeave += periodLeave;
            breakdown.add(currentStart.format(DateTimeFormatter.ofPattern("MM월 dd일")) +
                " ~ " +
                fullPeriodEnd.format(DateTimeFormatter.ofPattern("MM월 dd일")) +
                ": " + String.format("%.2f", periodLeave) + "일");
            currentStart = fullPeriodEnd.plusDays(1);
            fullPeriodEnd = currentStart.plusMonths(1).minusDays(1);
        }
        return new Object[]{totalLeave, breakdown};
    }

    // 평일 수 세기
    static int countWeekdays(LocalDate start, LocalDate end) {
        int count = 0;
        for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
            DayOfWeek dow = d.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    // AR 및 ASR 집계: extraPeriod 집계 (소정근로제외, 출근간주, 결근처리)
    static Map<String, Integer> aggregateExtraPeriods(List<ExtraPeriod> extraPeriods,
        LocalDate[] annualPeriod) {
        Map<String, Integer> extra = new HashMap<>();
        extra.put("출근간주", 0);
        extra.put("결근처리", 0);
        extra.put("소정근로제외", 0);
        for (ExtraPeriod ep : extraPeriods) {
            LocalDate interStart = annualPeriod[0].isAfter(ep.start) ? annualPeriod[0] : ep.start;
            LocalDate interEnd = annualPeriod[1].isBefore(ep.end) ? annualPeriod[1] : ep.end;
            if (!interStart.isAfter(interEnd)) {
                int wd = countWeekdays(interStart, interEnd);
                if (ep.type.equals("소정근로제외")) {
                    extra.put("소정근로제외", extra.get("소정근로제외") + wd);
                } else if (ep.type.equals("출근간주")) {
                    extra.put("출근간주", extra.get("출근간주") + wd);
                } else if (ep.type.equals("결근처리")) {
                    extra.put("결근처리", extra.get("결근처리") + wd);
                }
            }
        }
        return extra;
    }


    // ASR 계산: 전체 소정근로일에서 제외 평일 수 비율이 20% 이하이면 1.0, 초과 시 비례산출
    static double computeActualScheduledWorkRatio(int originalWorkdays,
        Map<String, Integer> extra) {
        double exclusionRatio = extra.get("소정근로제외") / (double) originalWorkdays;
        if (exclusionRatio <= 0.20) {
            return 1.0;
        } else {
            return (originalWorkdays - extra.get("소정근로제외")) / (double) originalWorkdays;
        }
    }

    // 최종 연차 산출 함수
    static Object[] computeLeave(String calculationMethod, LocalDate hireDate, LocalDate calcDate,
        Map<String, Integer> extra, double finalAttendanceRate, double actualScheduledWorkRatio,
        LocalDate[] annualCalcPeriod, List<ExtraPeriod> extraPeriods, String fiscalStartStr) {
        double leave = 0;
        String explanation = "";
        if (calculationMethod.equals("입사일")) {
            if (calcDate.isBefore(hireDate.plusYears(1))) {
                Object[] result = calculateEffectiveMonthlyLeave(hireDate, calcDate, extraPeriods);
                double effectiveLeave = (double) result[0];
                @SuppressWarnings("unchecked")
                List<String> breakdown = (List<String>) result[1];
                // 전체 기간 ASR
                int overallWorkdays = countWeekdays(hireDate, calcDate);
                Map<String, Integer> overallExtra = aggregateExtraPeriods(extraPeriods,
                    new LocalDate[]{hireDate, calcDate});
                double overallASR = computeActualScheduledWorkRatio(overallWorkdays, overallExtra);
                leave = effectiveLeave * overallASR;
                StringBuilder breakdownText = new StringBuilder();
                for (String line : breakdown) {
                    breakdownText.append(line).append("\n");
                }
                explanation = "(입사일 기준, 1년 미만)\n" +
                    "근속기간: " + Period.between(hireDate, calcDate).getYears() + "년 " +
                    Period.between(hireDate, calcDate).getMonths() + "개월 " +
                    Period.between(hireDate, calcDate).getDays() + "일 >> 1년미만\n" +
                    "입사일: " + hireDate + "\n" +
                    "연차산정기준일: " + calcDate + "\n" +
                    "연차산정단위기간: " + hireDate + " ~ " + calcDate + "\n" +
                    "통상근무와 다른 기간:\n";
                if (!extraPeriods.isEmpty()) {
                    for (ExtraPeriod ep : extraPeriods) {
                        String periodType = "";
                        if (ep.type.equals("출근간주")) {
                            periodType = "출근간주기간";
                        } else if (ep.type.equals("결근처리")) {
                            periodType = "결근 처리기간";
                        } else if (ep.type.equals("소정근로제외")) {
                            periodType = "소정근로 제외기간";
                        } else {
                            periodType = "알 수 없음";
                        }
                        explanation += periodType + ": " + ep.start + " ~ " + ep.end + "\n";
                    }
                } else {
                    explanation += "없음\n";
                }
                explanation += "──────────────────────────────\n";
                explanation += "(입사일 기준, 1년 미만인 경우 상세 산출 내역)\n" + breakdownText.toString() + "\n";
                explanation +=
                    "전체 소정근로일 대비 제외 비율에 따라 ASR = " + String.format("%.1f", overallASR * 100)
                        + "%가 적용되어, 최종 " + String.format("%.2f", leave) + "일 산출됨.";
            } else {
                int serviceYears = Period.between(hireDate, calcDate).getYears();
                if (finalAttendanceRate < 0.80) {
                    leave = 0;
                    explanation = "출근율이 80% 미만이므로, 연차 지급 대상이 아닙니다.";
                } else {
                    int basicLeave = 15;
                    int additional = (serviceYears - 1) / 2;
                    if (actualScheduledWorkRatio < 0.80) {
                        additional = (int) (additional * actualScheduledWorkRatio);
                        explanation = "출근율은 80% 이상이지만, 실질 소정근로일비율이 80% 미만이어서 가산 연차가 비례삭감됩니다.";
                    } else {
                        explanation = "출근율과 실질 소정근로일비율 모두 80% 이상이므로, 기본 및 가산 연차가 전액 지급됩니다.";
                    }
                    if (additional > 10) {
                        additional = 10;
                    }
                    int total = basicLeave + additional;
                    if (total > 25) {
                        total = 25;
                    }
                    leave = total * actualScheduledWorkRatio;
                    explanation =
                        "(입사일 기준, 1년 이상) 연차산정단위기간(" + annualCalcPeriod[0] + " ~ "
                            + annualCalcPeriod[1] +
                            ") 기준, 기본 15일과 가산 연차 " + additional + "일을 합해 " + total
                            + "일에서 소정근로 제외 비율(" +
                            String.format("%.1f", actualScheduledWorkRatio * 100) + "%)을 반영하여 "
                            + String.format("%.2f", leave) + "일 산출됨.";
                }
            }
        } else { // 회계연도 기준
            if (calcDate.isBefore(hireDate.plusYears(1))) {
                int part1 = calculateAnnualLeaveLessThanOneYear(hireDate, calcDate);
                int hireYear = hireDate.getYear();
                LocalDate endOfHireYear = LocalDate.of(hireYear, 12, 31);
                long daysWorked = ChronoUnit.DAYS.between(hireDate, endOfHireYear) + 1;
                int totalDaysInYear = Year.isLeap(hireYear) ? 366 : 365;
                double part2 = 15 * ((double) daysWorked / totalDaysInYear);
                leave = part1 + part2;
                explanation = "(회계연도 기준, 1년 미만)\n" +
                    "1. 입사 후 1년 미만자의 개근 연차: " + String.format("%.2f", (double) part1) + "일\n" +
                    "2. 새 회계연도 발생 연차: " + String.format("%.2f", part2) + "일\n" +
                    "총 산출 연차: " + String.format("%.2f", leave) + "일";
            } else {
                annualCalcPeriod = computeAnnualCalcPeriod(hireDate, calcDate, "회계연도",
                    fiscalStartStr);
                int originalWorkdaysLocal = countWeekdays(annualCalcPeriod[0], annualCalcPeriod[1]);
                System.out.println(
                    "계산된 연차산정단위기간: " + annualCalcPeriod[0] + " ~ " + annualCalcPeriod[1]);
                System.out.println("해당 기간 소정근로일 수: " + originalWorkdaysLocal + "일");
                LocalDate nextPeriodStart = annualCalcPeriod[0].plusYears(1);
                if (calcDate.isBefore(nextPeriodStart)) {
                    int monthlyComponent = calculateAnnualLeaveLessThanOneYear(hireDate, calcDate);
                    double newFiscalComponent = 15 * ((double) countWeekdays(
                        (annualCalcPeriod[0].isAfter(hireDate) ? annualCalcPeriod[0] : hireDate),
                        annualCalcPeriod[1])
                        / originalWorkdaysLocal);
                    leave = monthlyComponent + newFiscalComponent;
                    explanation = "(회계연도 기준, 1년 미만)\n" +
                        "개근 연차: " + String.format("%.2f", (double) monthlyComponent)
                        + "일, 새 회계연도 발생 연차: " +
                        String.format("%.2f", newFiscalComponent) + "일, 총 산출 연차: " + String.format(
                        "%.2f",
                        leave) + "일";
                } else {
                    int serviceYears = Period.between(hireDate, calcDate).getYears();
                    int additional = (serviceYears - 1) / 2;
                    if (finalAttendanceRate < 0.80) {
                        leave = 0;
                        explanation = "(회계연도 기준) 출근율이 80% 미만이므로, 연차 지급 대상이 아닙니다.";
                    } else {
                        if (additional > 10) {
                            additional = 10;
                        }
                        int total = 15 + additional;
                        if (total > 25) {
                            total = 25;
                        }
                        leave = total * actualScheduledWorkRatio;
                        explanation =
                            "(회계연도 기준, 1년 이상) 출근율이 80% 이상이므로, 기본 연차 15일과 추가 가산 연차 " + additional +
                                "일을 합해 " + total + "일에서 소정근로 제외 비율(" +
                                String.format("%.1f", actualScheduledWorkRatio * 100) + "%)을 반영하여 "
                                +
                                String.format("%.2f", leave) + "일 산출됨.";
                    }
                }
            }
        }
        if (leave % 1 != 0) {
            double roundedLeave = Math.ceil(leave * 2) / 2.0;
            explanation += "\n※ 원칙적으로 연차유급휴가가 소수점 단위로 산정되는 경우에는 시간단위로 부여하나, " +
                "법 위반을 방지하기 위하여 0.5일단위 또는 1일단위로 올림 처리하여 부여함이 바람직합니다. " +
                "따라서 " + String.format("%.1f", roundedLeave) + "일을 부여하는 것을 고려해볼 수 있습니다.";
        }
        return new Object[]{leave, explanation};
    }

    // AR 계산: 결근 처리만 차감 (출근간주는 중복 반영 안함)
    static double computeFinalAttendanceRate(int originalWorkdays, Map<String, Integer> extra) {
        int numerator = originalWorkdays - extra.get("결근처리");
        int denominator = originalWorkdays - extra.get("소정근로제외");
        if (denominator <= 0) {
            return 1.0;
        }
        return (double) numerator / denominator;
    }


    // 계산용 헬퍼: 1년 미만 연차 (입사일 기준) 단순 개근 개월 수
    static int calculateAnnualLeaveLessThanOneYear(LocalDate hireDate, LocalDate periodEnd) {
        Period delta = Period.between(hireDate, periodEnd);
        int fullMonths = delta.getYears() * 12 + delta.getMonths();
        return Math.min(fullMonths, 11);
    }

    public static void main(String[] args) {
        /*
        분석 형태

        코드
        위의 코드 설명 및 분석 결과

        예시)

        String extra = extraPeriods(A, B, C)
        extra : 비근무 기간 집계 함수
        1. 해당 함수 안을 뜯어보면 이렇게 되어있음
        2. 코드 분석을 기반으로 이해한 방식이 맞는지 확인 + Q에 대한 답변 달아주세요.
        3. 컨펌은 바로 밑에 달아주세요.(A. or 수정: 어떤 형태든 상관x)

        */

        while (true) {
            /*

            @Param calculationMethod : 연차유급휴가 산정 방식(입사일 or 회계연도)
            @Param fiscalStartStr : 회계연도
            @Param hireDate : 입사일
            @Param calcDate : 연차 산정 기준 날짜(특정 날짜를 기준으로 발생한 연차)
            @Param extraInput : 비근무 기간 있는지 여부(예/아니오만 가능)
            @Param extraPeriods : 비근무 기간 있을 시 저장하는 배열
            @Param annualCalcPeriod : 연차 산정 단위 기간 배열
            @Method getChoice() : 프롬프트창 출력
            @Method getDate() : String -> LocalDate 형식
            @Method countWeekdays() : 평일 수 세기

             */
            String calculationMethod = getChoice("Q1. 귀하(귀 사업장)의 연차유급휴가 산정방식은",
                Arrays.asList("입사일", "회계연도"));
            String fiscalStartStr = "";
            if (calculationMethod.equals("회계연도")) {
                fiscalStartStr = getFiscalStartString(
                    "Q1-1. 귀 사업장의 회계연도 시작일(월/일)을 입력해주세요 (예: 1월 1일): ");
            }
            LocalDate hireDate = getDate("Q2. 입사일을 입력해주세요 (YYYY-MM-DD): ");
            LocalDate calcDate = getDate("Q3. 어느 시점을 기준으로 발생한 연차를 확인하고 싶나요? (YYYY-MM-dd): ");
            String extraInput = getChoice(
                "Q4. 육아휴직, 출산전후휴가, 육아기근로시간 단축 등 휴직 등으로 특별한 사유로 인하여 통상적으로 근무하지 않은 기간이 있나요?",
                Arrays.asList("예", "아니오"));
            List<ExtraPeriod> extraPeriods = new ArrayList<>();
            if (extraInput.equals("예")) {
                extraPeriods = getExtraPeriods();
            }

            LocalDate[] annualCalcPeriod;
            if (calculationMethod.equals("회계연도")) // 연차유급휴가 산정 방식이 회계연도이면,
            // 연차 산정 단위 기간 : 입사일, 연차 산정 기준 날짜, 회계연도
            {
                annualCalcPeriod = computeAnnualCalcPeriod(hireDate, calcDate, "회계연도",
                    fiscalStartStr);
            } else
            // 연차 산정 단위 기간 : 입사일, 연차 산정 기준 날짜, 입사일
            {
                annualCalcPeriod = computeAnnualCalcPeriod(hireDate, calcDate, "입사일", "");
            }

            /*

            @Param calculationMethod : 연차유급휴가 산정 방식(입사일 or 회계연도)
            @Param fiscalStartStr : 회계연도
            @Param hireDate : 입사일
            @Param calcDate : 연차 산정 기준 날짜(특정 날짜를 기준으로 발생한 연차) = 기준일
            @Param extraInput : 비근무 기간 있는지 여부(예/아니오만 가능)

            annualCalcPeriod : 연차 산정 기간 저장 배열
            연차 산정 단위 기간 계산(computeAnnualCalcPeriod)
            I) 입사일 방식
            - 입사일이 1년 미만일 경우 => 연차 산정 단위 기간 = [입사일, 기준일]
            n = 기준일과 입사일 사이의 총 기간의 연수 = 근속년수?
            - 입사일이 1년 이상일 경우 => 연차 산정 단위 기간 = [입사일 + (근속년수 - 1), 입사일 + 근속년수 - 1일]
            Q. 기간 마지막 날 = (기준년도 입사일 - 1일) 해도 되는건가? No
            예시) 입사일 : 2020-03-09 , 기준연도 2024-02-25 => n = 3, [2022-03-09, 2023-03-08] 이 맞나? 나머지 2년은 어디감?
            n년 산정 방식 과 마지막 1년(~기준 날짜)까지 산정 방식이 다른가? n년은 신경 안쓰나? 어차피 1년 안에 무조건 다써야하나?

            Q. 입사일 2023-03-21 / 기준일 2024-03-20,21,22 인 경우? 혹은 한달내(2.22 ~ 3.20)까지 같은 처리?

            II) 회계연도 방식

            @Param candidate : 회계연도 시작일

            연차 산정 기준 날짜(예: 2023-03-21) & 회계연도 시작일(1월 1일)인 경우,
            회계연도가 1월 1일 => (기준일의 연도? 현재 연도?) 회계연도 : 2023년 1월 1일 => 로직에선 기준일의 연도
            1. 회계연도(2023-01-01) < 연차 기준일(2023-03-21) 인 경우  (Q. =? 같을 경우는 어떻게 계산??)
            회계연도 시작일 = 2023-01-01
            회계연도 종료일 = 시작일 + 1년 - 1일 = 2023-12-31 > 연차 기준일(2023-03-21) ==> -1년
            결과 : [2022-01-01, 2022-12-31]

            2. 회계연도(2023-03-01) > 연차 기준일(2023-01-08)
            회계연도 시작일 = 2022-03-01
            회계연도 종료일 = 2023-02-28 > 연차 기준일(2023-01-08) ==> -1년
            결과 : [2021-03-01, 2022-02-28]

            2-1. 회계연도(2023-03-01) > 연차 기준일(2023-02-28)
            회계연도 시작일 = 2022-03-01
            회계연도 종료일 = 2023-02-28 (= 연차기준일) ==> 같음 이 경우 어떻게 처리??

            3. 회계연도(2023-03-01) = 연차 기준일(2023-03-01) (일단 <= 경우로 처리)
            회계연도 시작일 = 2023-03-01
            회계연도 종료일 = 2024-02-28 > 연차 기준일(2023-03-01) ==> -1년
            결과 : [2022-03-01, 2023-02-28]

            => 이러한 계산식에선 항상 회계연도 종료일 <= 연차 기준일 (연도도 계산하는 거 맞지? 월,일로만 계산하는게 아니고?)
            일단 작은 경우는 -1년 처리한다 이해하면 되나?

            */

            // 평일 수 세기 => 공휴일은? 임시공휴일? 주말만 고려하면 되는건가?
            int originalWorkdays = countWeekdays(annualCalcPeriod[0], annualCalcPeriod[1]);

            Map<String, Integer> extra = new HashMap<>();
            extra.put("출근간주", 0);
            extra.put("결근처리", 0);
            extra.put("소정근로제외", 0);
            // 비근무 기간 있을 시 집계
            if (!extraPeriods.isEmpty()) {
                extra = aggregateExtraPeriods(extraPeriods, annualCalcPeriod);
                /*
                    aggregateExtraPeriods : 비근무 기간 집계 함수
                    @Param extraPeriods : 비근무 기간 배열(class : 비근무 형태(type), 기간(start, end))
                    @Param annualCalcPeriod : 연차 산정 기간 배열

                    1. 비근무 기간과 연차 산정 기간 사이의 겹치는 기간 계산(입력 받을 때 겹치지 않는 일부 기간 고려한 계산)
                    2. 기간 내 평일 수를 계산
                    3. 출근 간주 / 결근 처리 / 소정근로제외 유형에 따라 누계
                 */
            }

            double finalAttendanceRate = computeFinalAttendanceRate(originalWorkdays, extra);
            /*
                computeFinalAttendanceRate : AR(출근율) 계산
                @Param originalWorkdays : 소정근로일 수(연차 산정 기간에서의 평일 수)
                @Param extra : 비근무 기간 배열

                출근 간주 / 결근 처리 / 소정근로제외 형태 중 출근 간주는 이미 반영되어있어 중복 반영x
                만약 소정근로일 수 <= 소정근로제외일수 일 경우 AR = 1.0
                그 외) AR = (소정 근로일 수 - 결근처리 일 수) / (소정근로일 수 - 소정근로제외 일 수)

             */
            double actualScheduledWorkRatio = computeActualScheduledWorkRatio(originalWorkdays,
                extra);
            /*
                computeActualScheduledWorkRatio : ASR(실질 소정근로일 비율) 계산
                @Param originalWorkdays : 소정근로일 수(연차 산정 기간에서의 평일 수)
                @Param extra : 비근무 기간 배열

                (소정근로제외일 수 / 소정근로일 수) <= 0.2 이면 ASR = 1.0
                그게 아니면
                ASR = (소정근로일 수 - 소정근로제외일 수) / 소정근로일 수

             */

            Object[] leaveResult = computeLeave(calculationMethod, hireDate, calcDate, extra,
                finalAttendanceRate,
                actualScheduledWorkRatio, annualCalcPeriod, extraPeriods, fiscalStartStr);
            /*
                computeLeave : 최종 연차 산출 함수
                @Param calculationMethod : 연차유급휴가 산정 방식(입사일 or 회계연도)
                @Param hireDate : 입사일
                @Param calcDate : 연차 산정 기준 날짜(특정 날짜를 기준으로 발생한 연차)
                @Param extra : 비근무 기간 형태 누계한 배열(출근 간주/ 결근 처리/ 소정근로제외 형태에 따른 총 누계)
                @Param finalAttendanceRate : AR
                @Param actualScheduledWorkRatio : ASR
                @Param annualCalcPeriod : 연차 산정 단위 기간
                @Param extraPeriods : 비근무 기간 목록 저장한 배열(누계x)
                @Param fiscalStartStr : 회계연도

                I) 산정 방식이 "입사일"인 경우
                I-1) 입사일이 1년 미만일 경우(연차 산정 기준 날짜 < 입사일 + 1년) => calculateEffectiveMonthlyLeave
                입사일(hireDate) ~ 연차 산정 기준 날짜(calcDate)까지 1달 단위의 기간으로 나누어 개근 연차 산출(소정근로제외 비례 적용)
                calculateEffectiveMonthlyLeave 함수
                @Param hireDate : 입사일
                @Param calcDate : 연차 산정 기준 날짜(특정 날짜를 기준으로 발생한 연차)
                @Param extraPeriods : 비근무 기간 목록 저장한 배열(누계x)
                (아래 프로세스는 입사일 ~ 연차 산정 기준 날짜 까지 1달 단위로 쪼개서 계산을 반복)
                --------------------------------------------------------------
                1. (시작 날짜) ~ (시작 날짜 + 1달 - 1일) 까지 평일 수 계산(소정근로일 수 계산)
                2. 해당 기간 중 소정근로제외 일수 계산
                3. 발생하는 연차 일 수(소수점 단위) = (해당 월 소정 근로일 - 소정근로제외 일수) / 소정근로일 수
                => 3번은 ASR 계산 방식 적용하면 돼? 20% 이하시 1.0 하지 말고 ASR 기본 공식만 적용하면 되나?
                이렇게 해서 나온 결과 : 총 연차일 수 & 월별 상세 산출 내역
                --------------------------------------------------------------
                전체 기간(입사일 ~ 연차 산정 기준 날짜) ASR => 이때의 20% 고려한 기존 계산 방식
                최종 연차 = 총 연차일 수 * 전체 기간 ASR
                < 최종 연차 & 결과 상세 내역 출력해주기 >
                < + 출력 요소 >
                1. 근속 기간 = 연차 산정 기준 날짜 - 입사일
                2. 입사일
                3. 연차산정기준일(=연차 산정 기준 날짜)
                4. 연차산정단위기간
                5. 통상근무기간과 다른 기간(있을 경우)
                    출근간주기간 : YYYY.MM.DD - YYYY.MM.DD
                    결근처리기간 : YYYY.MM.DD - YYYY.MM.DD
                    소정근로 제외기간 : YYYY.MM.DD - YYYY.MM.DD
                6. 월별 상세 산출 내역
                7. 전체 기간 ASR이 xx.xx %가 적용되어, 최종 연차가 산출됨.

                ********************************************
                I-2) 입사일이 1년 이상일 경우
                AR < 0.8인 경우 => 연차 지급 대상x
                AR >= 0.8인 경우, 가산 연차 계산
                가산 연차 = (근속연수(연차 산정 기준 날짜 - 입사일 의 년수) -1) / 2;
                지급 가능한 연차 = 15일 + 가산 연차(최대 10일) => 최대 25일까지 지급
                ASR < 0.8인 경우 => 가산 연차 = 지급 가능한 "가산" 연차 * ASR;
                ==> 현재 계산기는 가산 연차 값이 3.3, 3.7과 같이 소수점이 붙으면 소수점 버림으로 설정되어있음. 맞는지 확인 부탁
                ASR >= 0.8인 경우 100% 가산 연차 지급
                최종 연차 = 지급 가능한 연차(15일 + 가산 연차) * ASR
                < 출력 요소 >
                (입사일 기준, 1년 이상) 연차산정단위기간(YYYY-MM-DD ~ YYYY-MM-DD) 기준,
                기본 15일과 가산 연차 X일을 합해 XX일에서 소정근로 제외 비율(XX.X%)을 반영하여 XX.XX일 산출됨.

                &&&&

                II) 산정 방식이 "회계연도"인 경우
                II-1) 입사일이 1년 미만일 경우(입사일로부터 1년이상 & 다음 회계연도 전의 경우도 포함)
                calculateAnnualLeaveLessThanOneYear
                @Param hireDate : 입사일
                @Param calcDate : 연차 산정 기준 날짜(특정 날짜를 기준으로 발생한 연차)
                1. 입사일 ~ 연차 산정 기준 날짜 까지 개월 수 계산(part1)
                - 2022-03-02 ~ 2023-02-11 : 11개월 9일 => 11개월
                - 2022-05-16 ~ 2022-09-26 : 3개월 20일 => 3개월
                2. 입사일 ~ 회계연도 마지막날까지의 남은 일 수 계산(평일 수 계산x) + 윤년 고려
                3. 해당 연도 발생 연차(part2) = 15 * (남은 일 수 / (회계연도 종료일 - 회계연도 시작일)(365 or 366(윤년)))
                최종 연차 = part1 + part2 (소수점 그대로) ex) 7.3, 7.7
                < 출력 요소 >
                (회계연도 기준, 1년 미만)
                1. 입사 후 1년 미만자의 개근 연차: part1
                2. 새 회계연도 발생 연차: part2
                총 산출 연차: part1 + part2

                **************************************************

                II-2) 입사일이 1년 이상일 경우(회계연도 2번이상 경험한 사람들)

                1. 입사일 기준으로 1년 차를 계산한다.
                ex) 회계연도 : 1월 1일
                입사일 : 2024-09-15 ==> 2026-01-01 : 1년차
                         2025-01-01 ==> 2026-01-01 : 1년차
                2. 2년차당 +1일 가산연차, 최대 +10일(20년차이상)
                ex) 입사일 : 2024-09-15 ==> 2027-01-01 : 2년차 => +1 연차 발생
                3. AR < 0.8 => 연차 지급x
                4. ASR이 0.8 미만이든 이상이든 상관없이 최종연차 = 지급 가능한 연차 & ASR
                <출력 요소>
                예시)
                (회계연도 기준, 1년 이상) 출근율이 80% 이상이므로,
                기본 연차 15일과 추가 가산 연차 2일을 합해 17일에서 소정근로 제외 비율(90.0%)을 반영하여 15.30일 산출됨.

             */

            double leave = (double) leaveResult[0];
            String explanation = (String) leaveResult[1];

            Period rd = Period.between(hireDate, calcDate);
            String careerStr = rd.getYears() + "년 " + rd.getMonths() + "개월 " + rd.getDays() + "일";
            String careerType = calcDate.isBefore(hireDate.plusYears(1)) ? "1년미만" : "1년이상";

            StringBuilder extraText = new StringBuilder();
            if (!extraPeriods.isEmpty()) {
                for (ExtraPeriod ep : extraPeriods) {
                    String periodType = "";
                    if (ep.type.equals("출근간주")) {
                        periodType = "출근간주기간";
                    } else if (ep.type.equals("결근처리")) {
                        periodType = "결근 처리기간";
                    } else if (ep.type.equals("소정근로제외")) {
                        periodType = "소정근로 제외기간";
                    } else {
                        periodType = "알 수 없음";
                    }
                    extraText.append(periodType).append(": ").append(ep.start).append(" ~ ")
                        .append(ep.end)
                        .append("\n");
                }
            } else {
                extraText.append("없음");
            }

            System.out.println("\n=== 연차유급휴가 산출 결과 ===");
            System.out.println("산정 방식: " + calculationMethod);
            System.out.println("근속기간: " + careerStr + " >> " + careerType);
            System.out.println("입사일: " + hireDate);
            System.out.println("연차산정기준일: " + calcDate);
            System.out.println("연차산정단위기간: " + annualCalcPeriod[0] + " ~ " + annualCalcPeriod[1]);
            System.out.println("통상근무와 다른 기간:");
            System.out.println(extraText.toString());
            System.out.println("──────────────────────────────");
            System.out.println(explanation);
            System.out.println("=> 최종 산출된 연차유급휴가 일수: " + String.format("%.2f", leave) + "일");
            System.out.println("=================================================\n");

            System.out.print("계속 계산하시겠습니까? (예/아니오): ");
            String cont = sc.nextLine().trim();
          if (cont.equals("아니오")) {
            break;
          }
        }
    }
}
