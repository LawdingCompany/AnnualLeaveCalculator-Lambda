package com.lawding.leavecalc.domain;

import java.time.LocalDate;
import lombok.ToString;

@ToString
public record DatePeriod(
    LocalDate startDate,
    LocalDate endDate) {
}
