package com.lawding.leavecalc.domain;

import java.time.LocalDate;

/**
 * 관련 근무 기간에 대한 시작일, 종료일을 저장하는 객체입니다.
 * <p>
 *
 * @param startDate 시작일
 * @param endDate   종료일
 */
public record DateRange(
    LocalDate startDate,
    LocalDate endDate
) {

}
