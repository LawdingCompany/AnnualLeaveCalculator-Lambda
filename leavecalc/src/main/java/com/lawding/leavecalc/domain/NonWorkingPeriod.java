package com.lawding.leavecalc.domain;

import java.time.LocalDate;
import java.util.List;

/**
 * 관련 근무 기간에 대한 시작일, 종료일을 저장하는 객체입니다.
 * <p>
 *
 * @param type       비근무 형태
 * @param dateRange  시작일, 종료일
 */

public record NonWorkingPeriod(
    int type,
    List<LocalDate> dateRange
) {

}
