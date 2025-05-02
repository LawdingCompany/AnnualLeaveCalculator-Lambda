package com.lawding.leavecalc.constant;

public final class AnnualLeaveMessages {

    private AnnualLeaveMessages() {
    }

    public static final class HireDate {

        public static final String LESS_THAN_ONE_YEAR = "근무기간이 1년 미만인 근로자는 1개월 개근시 1일 발생 (근로기준법 제60조 제2항)";

        public static final String AR_UNDER_80_AFTER_ONE_YEAR =
            "출근율이 80% 미만인 경우 월차 부여";
        public static final String PWR_UNDER_80_AFTER_ONE_YEAR =
            "출근일(출근율) / 연간 소정근로일(소정근로비율)이 80% 이상인 경우 비례삭감없이 전부부여\n"
            + "출근일(출근율) / 연간 소정근로일(소정근로비율)이 80% 미만인 경우 비례삭감하여 부여";

        public static final String AR_AND_PWR_OVER_80_AFTER_ONE_YEAR = "연차 : 근로기준법 제60조 제1항";
    }


}
