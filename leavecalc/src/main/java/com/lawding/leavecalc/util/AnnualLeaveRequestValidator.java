package com.lawding.leavecalc.util;

import com.lawding.leavecalc.dto.request.AnnualLeaveRequest;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import java.util.Locale;
import java.util.Map;

public class AnnualLeaveRequestValidator {

    private AnnualLeaveRequestValidator() {
    }

    public record HeaderInfo(String platform, boolean testMode) {

    }

    public static HeaderInfo validate(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            throw new AnnualLeaveException(ErrorCode.HEADER_REQUIRED);
        }
        String platform = validatePlatform(headers);
        boolean testMode = validateTestMode(headers);
        return new HeaderInfo(platform, testMode);
    }

    /**
     * X-Platform 검증 (필수, web | ios 만 허용)
     */
    private static String validatePlatform(Map<String, String> headers) {
        String platform = headers.get("x-platform");
        if (platform == null || platform.isBlank()) {
            throw new AnnualLeaveException(ErrorCode.PLATFORM_REQUIRED);
        }

        String normalized = platform.trim().toLowerCase(Locale.ROOT);
        if (!normalized.equals("web") && !normalized.equals("ios")) {
            throw new AnnualLeaveException(ErrorCode.INVALID_PLATFORM);
        }

        return normalized;
    }

    /**
     * X-Test 검증 (선택, 값이 true일 때만 testMode 활성화)
     */
    private static boolean validateTestMode(Map<String, String> headers) {
        String testHeader = headers.get("x-test");
        return "true".equalsIgnoreCase(testHeader);
    }

    public static void validate(AnnualLeaveRequest request) {
        if (request.getHireDate() == null || request.getHireDate().isBlank()) {
            throw new AnnualLeaveException(ErrorCode.HIRE_DATE_REQUIRED);
        }
        if (request.getReferenceDate() == null || request.getReferenceDate().isBlank()) {
            throw new AnnualLeaveException(ErrorCode.REFERENCE_DATE_REQUIRED);
        }
        if (request.getCalculationType() == 2 && (request.getFiscalYear() == null
                                                  || request.getFiscalYear().isBlank())) {
            throw new AnnualLeaveException(ErrorCode.FISCAL_YEAR_REQUIRED);
        }
    }
}
