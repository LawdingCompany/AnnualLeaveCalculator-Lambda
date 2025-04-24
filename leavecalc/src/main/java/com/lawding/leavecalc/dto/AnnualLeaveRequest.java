package com.lawding.leavecalc.dto;

import java.util.List;
import lombok.Getter;


@Getter
public class AnnualLeaveRequest{
    private int calculationType;
    private String fiscalYear; // nullable
    private String hireDate;
    private String referenceDate;
    private List<NonWorkingPeriodRequest> nonWorkingPeriods; // nullable
    private List<String> companyHolidays; // nullable
}
