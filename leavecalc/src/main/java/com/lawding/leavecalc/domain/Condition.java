package com.lawding.leavecalc.domain;

public enum Condition {
    // 입사일 기준 흐름
    HD_LESS_ONE_YEAR, // 입사일 1년미만, 월차
    HD_AFTER_ONE_YEAR_AND_UNDER_AR, // 입사일 1년 이상 & 출근율 80% 미만
    HD_AFTER_ONE_YEAR_AND_OVER_AR_AND_UNDER_PWR, // 입사일 1년 이상 & 출근율 80% 이상 & 소정근로비율 80% 미만
    HD_FULL, // 정기 연차 전체 부여

    // 회계연도 기준 흐름
    FY_BEFORE_PRORATED, // 첫 정기 회계연도 이전 & 비례연차 발생일 이전
    FY_MONTHLY_AND_PRORATED, //  첫 정기 회계연도 이전 & 비례연차 발생일 & 월차 + 비례연차 유형
    FY_PRORATED_AND_UNDER_AR, // 첫 정기 회계연도 이전 & 비례연차 발생 & 출근율 80% 미만
    FY_PRORATED_FULL, // 첫 정기 회계연도 이전 & 비례연차 발생 & 출근율 80% 이상

    FY_AFTER_FIRST_REGULAR_START_DATE_AND_UNDER_AR, // 첫 정기 회계연도 이후 & 출근율 80% 미만
    FY_AFTER_FIRST_REGULAR_START_DATE_AND_OVER_AR_AND_UNDER_PWR,  // 첫 정기 회계연도 이후 & 출근율 80% 이상 & 소정근로비율 80% 미만
    FY_FULL, // 정기 연차 전체 부여

}
