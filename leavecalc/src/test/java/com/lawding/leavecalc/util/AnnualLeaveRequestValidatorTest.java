package com.lawding.leavecalc.util;

import com.lawding.leavecalc.dto.request.AnnualLeaveRequest;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AnnualLeaveRequestValidatorTest {

    @Test
    @DisplayName("입사일이 null이면 예외 발생")
    void validate_NullHireDate_ThrowsException() {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .hireDate(null)
            .referenceDate("2024-06-03")
            .calculationType(1)
            .build();

        // When & Then
        AnnualLeaveException exception = assertThrows(AnnualLeaveException.class,
            () -> AnnualLeaveRequestValidator.validate(request));

        assertEquals(ErrorCode.HIRE_DATE_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("입사일이 빈 문자열이면 예외 발생")
    void validate_EmptyHireDate_ThrowsException() {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .hireDate("")
            .referenceDate("2024-06-03")
            .calculationType(1)
            .build();

        // When & Then
        AnnualLeaveException exception = assertThrows(AnnualLeaveException.class,
            () -> AnnualLeaveRequestValidator.validate(request));

        assertEquals(ErrorCode.HIRE_DATE_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("기준일이 null이면 예외 발생")
    void validate_NullReferenceDate_ThrowsException() {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .hireDate("2024-03-01")
            .referenceDate(null)
            .calculationType(1)
            .build();

        // When & Then
        AnnualLeaveException exception = assertThrows(AnnualLeaveException.class,
            () -> AnnualLeaveRequestValidator.validate(request));

        assertEquals(ErrorCode.REFERENCE_DATE_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("기준일이 빈 문자열이면 예외 발생")
    void validate_EmptyReferenceDate_ThrowsException() {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .hireDate("2024-03-01")
            .referenceDate("")
            .calculationType(1)
            .build();

        // When & Then
        AnnualLeaveException exception = assertThrows(AnnualLeaveException.class,
            () -> AnnualLeaveRequestValidator.validate(request));

        assertEquals(ErrorCode.REFERENCE_DATE_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("회계연도 방식인데 회계연도가 null이면 예외 발생")
    void validate_FiscalYearTypeWithNullFiscalYear_ThrowsException() {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .hireDate("2024-03-01")
            .referenceDate("2024-06-03")
            .calculationType(2) // 회계연도 방식
            .fiscalYear(null)
            .build();

        // When & Then
        AnnualLeaveException exception = assertThrows(AnnualLeaveException.class,
            () -> AnnualLeaveRequestValidator.validate(request));

        assertEquals(ErrorCode.FISCAL_YEAR_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("회계연도 방식인데 회계연도가 빈 문자열이면 예외 발생")
    void validate_FiscalYearTypeWithEmptyFiscalYear_ThrowsException() {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .hireDate("2024-03-01")
            .referenceDate("2024-06-03")
            .calculationType(2) // 회계연도 방식
            .fiscalYear("")
            .build();

        // When & Then
        AnnualLeaveException exception = assertThrows(AnnualLeaveException.class,
            () -> AnnualLeaveRequestValidator.validate(request));

        assertEquals(ErrorCode.FISCAL_YEAR_REQUIRED, exception.getErrorCode());
    }

    @Test
    @DisplayName("입사일 방식이면 회계연도가 없어도 정상")
    void validate_HireDateTypeWithoutFiscalYear_Success() {
        // Given
        AnnualLeaveRequest request = AnnualLeaveRequest.builder()
            .hireDate("2024-03-01")
            .referenceDate("2024-06-03")
            .calculationType(1) // 입사일 방식
            .fiscalYear(null) // 회계연도 없음
            .build();

        // When & Then - 예외가 발생하지 않아야 함
        assertDoesNotThrow(() -> AnnualLeaveRequestValidator.validate(request));
    }

    @Test
    @DisplayName("모든 필수 값이 있으면 정상")
    void validate_AllRequiredFieldsPresent_Success() {
        // Given - 회계연도 방식
        AnnualLeaveRequest request1 = AnnualLeaveRequest.builder()
            .hireDate("2024-03-01")
            .referenceDate("2024-06-03")
            .calculationType(2)
            .fiscalYear("01-01")
            .build();

        // Given - 입사일 방식
        AnnualLeaveRequest request2 = AnnualLeaveRequest.builder()
            .hireDate("2024-03-01")
            .referenceDate("2024-06-03")
            .calculationType(1)
            .build();

        assertDoesNotThrow(() -> AnnualLeaveRequestValidator.validate(request1));
        assertDoesNotThrow(() -> AnnualLeaveRequestValidator.validate(request2));
    }
}