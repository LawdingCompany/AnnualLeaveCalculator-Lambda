package com.lawding.leavecalc.domain;

import java.time.LocalDate;


public record DatePeriod(
    LocalDate startDate,
    LocalDate endDate) {
}
