package com.lawding.leavecalc.resolver;

import static com.lawding.leavecalc.constant.AnnualLeaveConstants.MINIMUM_WORK_RATIO;

import com.lawding.leavecalc.domain.CalculationType;
import com.lawding.leavecalc.domain.FlowStep;
import com.lawding.leavecalc.domain.flow.context.AnnualContext;
import com.lawding.leavecalc.domain.flow.context.CalculationContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyAndProratedContext;
import com.lawding.leavecalc.domain.flow.context.MonthlyContext;
import com.lawding.leavecalc.domain.flow.context.ProratedContext;
import com.lawding.leavecalc.dto.request.NonWorkingPeriodDto;
import java.util.ArrayList;
import java.util.List;

public class ExplanationResolver {

    private ExplanationResolver() {
    }

    public static List<String> resolveAll(List<FlowStep> steps, CalculationType type) {
        if (steps == null || steps.isEmpty()) {
            return List.of();
        }

        List<String> explanations = new ArrayList<>();

        for (FlowStep step : steps) {
            String msg = resolve(step, type);
            if (!msg.isEmpty()) {
                explanations.add(msg);
            }
        }
        return explanations;
    }

    public static String resolve(FlowStep step, CalculationType type) {
        return switch (step) {
            case LESS_ONE_YEAR -> switch (type) {
                case HIRE_DATE -> "입사 1년이 될 때까지, 개근 1개월당 1일의 연차가 발생해요 (최대 11일)";
                case FISCAL_YEAR -> "입사 1년이 될 때까지, 개근 1개월당 1일의 연차가 발생해요 (최대 11일), " +
                                    "이와 별개로 입사 후 다음연도 회계연도 시작일에 비례연차가 발생해요";
            };
            case ANNIVERSARY_EVE -> switch (type) {
                case HIRE_DATE -> "입사일로부터 1년이 지났습니다! 오늘까지 개근하였다면 11일의 월차를 사용할 수 있습니다."
                                  + " 1년 초과로 근무가 예정되어있다면 다음 출근일에 15일의 연차가 발생합니다.";
                case FISCAL_YEAR -> "입사일로부터 1년이 지났습니다! 오늘까지 개근하였다면 11일의 월차를 사용할 수 있습니다."
                                    + " 1년 초과로 근무가 예정되어있다면 다음 출근일에 15일의 연차가 발생합니다.";
            };
            case AFTER_ONE_YEAR -> switch (type) {
                case HIRE_DATE -> "근무기간이 1년 이상인 경우, 1년간 출근율이 80%이상이라면, 1년 1일차 되는 날 15일의 연차가 발생해요.";
                case FISCAL_YEAR -> "근무기간이 1년 이상인 경우, 월차(1년 최대 11개) 사용기간이 종료하여 비례연차만 사용할 수 있어요";
            };
            case SERVICE_YEARS_3_PLUS -> switch (type) {
                case HIRE_DATE -> "3년 이상 계속해서 근로하였다면 매 2년에 대해서 1일을 가산한 연차유급휴가를 지급하여야만 합니다."
                                  + "즉, 3년차면 15+1일=16일, 5년차 : 15+2일 = 17일… 로 지급합니다."
                                  + "단, 연차휴가는 최대 25일을 넘을 수 없습니다(근로기준법 제60조 제4항)";
                case FISCAL_YEAR ->
                    "3년 이상 계속해서 근로하였다면 매 2년에 대해서 1일을 가산한 연차유급휴가를 지급하여야만 합니다. 회계연도로 연차를 산정하는 회사는 FULL-TIME근무횟수로 가산연차를 산정합니다.\n"
                    + "\n"
                    + "따라서, 회계연도 시작일부터 종료일까지 한해를 풀타임으로 2년 근무하고 출근율이 80%를 넘었을 때, 3년차에 비로소 가산연차를 받게 됩니다(15+1일 = 16일)\n"
                    + "\n"
                    + "가산연차를 포함한 연차유급휴가는 최대 25일을 넘을 수 없습니다(근로기준법 제60조 제4항)";
            };
            case UNDER_AR -> "무단 결근 등으로 인하여 출근율이 80% 미만이므로 1달 개근시 1일의 월차가 발생해요.";

            default -> "";
        };
    }

    public static Double extractPWR(CalculationContext ctx) {
        if (ctx instanceof MonthlyContext mCtx) {
            return mCtx.getPrescribedWorkingRatio(); // null 가능
        }
        if (ctx instanceof AnnualContext aCtx) {
            return aCtx.getPrescribedWorkingRatio();
        }
        if (ctx instanceof ProratedContext pCtx) {
            return pCtx.getPrescribedWorkingRatio();
        }
        if (ctx instanceof MonthlyAndProratedContext mpCtx) {
            return extractPWR(mpCtx.getProratedContext());
        }
        return null;
    }

    public static List<String> resolveNonWorkingExplanations(List<NonWorkingPeriodDto> periods,
        int serviceYears, Double prescribedWorkRatio) {
        List<String> message = new ArrayList<>();
        for (NonWorkingPeriodDto p : periods) {
            switch (p.getType()) {
                case 1 -> { // 출근처리
                    String msg = """
                        육아휴직, 출산전후휴가, 예비군훈련, 배우자출산휴가 등의 기간은 출근간주일로 처리되어 연차유급휴가 산정시 “출근”한 것으로 처리합니다.
                        """;
                    message.add(msg);
                }
                case 2 -> { // 결근처리
                    String msg;
                    if (serviceYears > 0) {
                        msg = """
                            결근일이 있으나, 연차산정기간(1년)간 출근율이 80%이상이라면 다음년도 연차유급휴가 발생에 영향을 주지 않습니다.
                            """;
                    } else {
                        msg = """
                            근무기간이 1년 미만인 근로자에게 주어지는 월차는 개근한 월에만 주어지게 되므로, 결근일이 있는 월에 월차가 발생하지 않습니다.
                            """;
                    }
                    message.add(msg);
                }
                case 3 -> { // 소정근로제외
                    String msg;
                    if (serviceYears < 1 || prescribedWorkRatio == null) {
                        msg = """
                            개인사유로 인한 휴직, 질병(산재X)휴직, 병역휴직 등의 경우 해당 기간을 “소정근로기간”에서 제외합니다(고용노동부유권해석_임금근로시간과-1818, 2021.8.12)
                            입사일 기준 매 달 개근하면 발생하는 1일의 연차가 매달 소정근로제외기간 비율만큼 비례하여 발생합니다.
                            """;
                    } else if (prescribedWorkRatio != null
                               && prescribedWorkRatio < MINIMUM_WORK_RATIO) {
                        msg =
                            """
                                개인사유로 인한 휴직, 질병(산재X)휴직, 병역휴직 등의 경우 해당 기간을 “소정근로기간”에서 제외합니다(고용노동부유권해석_임금근로시간과-1818, 2021.8.12)
                                다만, 연차산정 단위기간에서 소정근로제외기간을 제외하였을 때 기간이 80% 미만이 된다면,
                                평상적인 근로관계에서 출근율이 80%이상일 때 산출되었을 연차휴가일수 * (실질 소정근로일/연간 소정근로일)로 비례하여 지급할 연차휴가를 산정하게 됩니다.
                                """;

                    } else {
                        msg = """
                            개인사유로 인한 휴직, 질병(산재X)휴직, 병역휴직 등의 경우 해당 기간을 “소정근로기간”에서 제외합니다(고용노동부유권해석_임금근로시간과-1818, 2021.8.12)
                            다만, 연차산정 단위기간에서 소정근로제외기간을 제외한다 하더라도 80%이상 출근하였다면, 정상적으로 연차유급휴가를 부여합니다.
                            """;
                    }
                    message.add(msg);
                }
            }
        }
        return message;
    }
}
