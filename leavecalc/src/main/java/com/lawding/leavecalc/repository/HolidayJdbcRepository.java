package com.lawding.leavecalc.repository;

import com.lawding.leavecalc.db.RdsConnector;
import com.lawding.leavecalc.domain.DatePeriod;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HolidayJdbcRepository {

    private static final String SQL_FIND_HOLIDAYS = """
            SELECT DISTINCT holiday_date FROM holidays
            WHERE holiday_date BETWEEN ? AND ?
        """;

    public Set<LocalDate> findWeekdayHolidays(DatePeriod period) {
        try (Connection conn = RdsConnector.getConnection();
            PreparedStatement ps = conn.prepareStatement(SQL_FIND_HOLIDAYS)) {

            ps.setDate(1, Date.valueOf(period.startDate()));
            ps.setDate(2, Date.valueOf(period.endDate().plusDays(1)));

            try (ResultSet rs = ps.executeQuery()) {
                Set<LocalDate> holidays = new HashSet<>();
                while (rs.next()) {
                    holidays.add(rs.getDate("holiday_date").toLocalDate());
                }
                return holidays;
            }
        } catch (SQLException e) {
            throw new AnnualLeaveException(ErrorCode.DATABASE_QUERY_FAILED);
        }
    }
}
