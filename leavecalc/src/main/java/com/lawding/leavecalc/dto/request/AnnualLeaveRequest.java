package com.lawding.leavecalc.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AnnualLeaveRequest {
    @JsonProperty("calculationType")
    private int calculationType;

    @JsonProperty("fiscalYear")
    private String fiscalYear;

    @JsonProperty("hireDate")
    private String hireDate;

    @JsonProperty("referenceDate")
    private String referenceDate;

    @JsonProperty("nonWorkingPeriods")
    private List<NonWorkingPeriodDto> nonWorkingPeriods;

    @JsonProperty("companyHolidays")
    private List<String> companyHolidays;
}
