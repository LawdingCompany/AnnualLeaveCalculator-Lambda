package com.lawding.leavecalc.dto;

import lombok.Getter;

@Getter
public record DateRangeRequest(String startDate, String endDate) {

}
