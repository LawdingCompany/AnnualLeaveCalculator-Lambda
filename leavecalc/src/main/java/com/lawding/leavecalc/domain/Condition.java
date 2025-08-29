package com.lawding.leavecalc.domain;

public enum Condition {
    // 입사일 기준 흐름
    HD_LESS_THAN_ONE_YEAR, // 입사일 1년미만, 월차
    HD_LOW_AR, // 입사일 1년 이상 & 출근율 80% 미만
    HD_LOW_PWR, // 입사일 1년 이상 & 출근율 80% 이상 & 소정근로비율 80% 미만
    HD_FULL // 정기 연차 전체 부여
    
    // 회계연도 기준 흐름
}
