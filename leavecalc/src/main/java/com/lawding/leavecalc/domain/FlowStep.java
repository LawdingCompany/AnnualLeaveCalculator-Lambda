package com.lawding.leavecalc.domain;

import java.time.LocalDate;

public enum FlowStep {
    SERVICE_YEARS_3_PLUS, // 근속연수 3년이상

    LESS_ONE_YEAR, // 입사일 1년미만
    ANNIVERSARY_EVE, // 입사일 1년째 되는날( != 1주년)
    AFTER_ONE_YEAR, // 입사일 1년이상


    UNDER_AR, // 출근율 80% 미만
    OVER_AR, // 출근율 80% 이상

    UNDER_PWR, // 소정근로비율 80% 미만
    OVER_PWR, // 소정근로비율 80% 이상


    // 회계연도 기준 흐름
    BEFORE_PRORATED, // 첫 정기 회계연도 이전 & 비례연차 발생일 이전
    MONTHLY_AND_PRORATED, //  첫 정기 회계연도 이전 & 비례연차 발생일 & 월차 + 비례연차 유형
    PRORATED_AND_UNDER_AR, // 첫 정기 회계연도 이전 & 비례연차 발생 & 출근율 80% 미만
    PRORATED_FULL, // 첫 정기 회계연도 이전 & 비례연차 발생 & 출근율 80% 이상

    AFTER_FIRST_REGULAR_START_DATE_AND_UNDER_AR, // 첫 정기 회계연도 이후 & 출근율 80% 미만
    AFTER_FIRST_REGULAR_START_DATE_AND_OVER_AR_AND_UNDER_PWR; // 첫 정기 회계연도 이후 & 출근율 80% 이상 & 소정근로비율 80% 미만

    /**
     * 입사일/기준일로 입사 1년 상태를 분류한다.
     * business rule:
     * - 입사 1주년 당일의 전날을 '딱 1년 되는 날(ANNIVERSARY_EVE)'로 본다.
     * - 로직상으로는 1년 미만에 포함되지만, 안내문 구분을 위해 추가 분기가 필요하다.
     *
     * @return
     *   ANNIVERSARY_EVE : 입사 1주년 전날
     *   LESS_ONE_YEAR   : 입사 1년 미만 (단, 전날 제외)
     *   AFTER_ONE_YEAR  : 입사 1년 이상 (1주년 당일 포함)
     */
    public static FlowStep resolveOneYearStep(LocalDate hireDate, LocalDate referenceDate) {
        LocalDate firstAnniv = hireDate.plusYears(1);     // 1주년 당일
        LocalDate lastUnderOne = firstAnniv.minusDays(1); // 전날

        if (referenceDate.isBefore(firstAnniv)) {
            return referenceDate.isEqual(lastUnderOne) ? ANNIVERSARY_EVE : LESS_ONE_YEAR;
        }
        return AFTER_ONE_YEAR; // 기준일이 1주년 당일 이상이면 모두 여기에 포함
    }

}
