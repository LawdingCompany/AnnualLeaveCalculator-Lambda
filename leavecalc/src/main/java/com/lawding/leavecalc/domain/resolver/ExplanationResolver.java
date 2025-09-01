package com.lawding.leavecalc.domain.resolver;

import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.FlowStep;

public class ExplanationResolver {
    public static String resolve(FlowStep step, CalculationType type) {
        return switch (step) {
            case HD_LESS_ONE_YEAR -> switch (type) {
                case HIRE_DATE -> "입사 1년이 될 때까지, 개근 1개월당 1일의 연차가 발생해요 (최대 11일)";
                case FISCAL_YEAR -> "입사 1년이 될 때까지, 개근 1개월당 1일의 연차가 발생해요 (최대 11일), " +
                                    "이와 별개로 입사 후 다음연도 회계연도 시작일에 비례연차가 발생해요";
            };

            default -> "";
        };
    }
}
