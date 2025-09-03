package com.lawding.leavecalc.domain;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;

import java.time.LocalDate;

public enum FlowStep {
    SERVICE_YEARS_3_PLUS, // 근속연수 3년이상

    LESS_ONE_YEAR, // 입사일 1년미만
    ANNIVERSARY_EVE, // 입사일 1년째 되는날( != 1주년)
    AFTER_ONE_YEAR, // 입사일 1년이상

    UNDER_AR, // 출근율 80% 미만
    OVER_AR, // 출근율 80% 이상

    UNDER_PWR, // 소정근로비율 80% 미만
    OVER_PWR; // 소정근로비율 80% 이상

    /**
     * 입사일/기준일로 입사 1년 상태를 분류한다. business rule: - 입사 1주년 당일의 전날을 '딱 1년 되는 날(ANNIVERSARY_EVE)'로 본다. -
     * 로직상으로는 1년 미만에 포함되지만, 안내문 구분을 위해 추가 분기가 필요하다.
     *
     * @return ANNIVERSARY_EVE : 입사 1주년 전날 LESS_ONE_YEAR   : 입사 1년 미만 (단, 전날 제외) AFTER_ONE_YEAR  :
     * 입사 1년 이상 (1주년 당일 포함)
     */
    public static FlowStep resolveOneYearStep(LocalDate hireDate, LocalDate referenceDate) {
        LocalDate firstAnniv = hireDate.plusYears(1);     // 1주년 당일
        LocalDate lastUnderOne = firstAnniv.minusDays(1); // 전날

        if (referenceDate.isBefore(firstAnniv)) {
            return referenceDate.isEqual(lastUnderOne) ? ANNIVERSARY_EVE : LESS_ONE_YEAR;
        }
        return AFTER_ONE_YEAR; // 기준일이 1주년 당일 이상이면 모두 여기에 포함
    }

    public static FlowStep stepPWR(double prescribeWorkingRatio) {
        if (prescribeWorkingRatio < MINIMUM_WORK_RATIO) {
            return FlowStep.UNDER_PWR;
        } else {
            return FlowStep.OVER_PWR;
        }
    }
}
