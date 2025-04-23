package com.lawding.leavecalc.repository;

import com.lawding.leavecalc.db.RdsConnector;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class HolidayJdbcRepository {

    private static final String COUNT_HOLIDAY_SQL = """
            SELECT COUNT(*) FROM holidays
            WHERE holiday_date BETWEEN ? AND ?
              AND DAYOFWEEK(holiday_date) NOT IN (1, 7)
        """;

    public int countWeekdayHolidays(LocalDate startDate, LocalDate endDate) {
        try (Connection conn = RdsConnector.getConnection();
            PreparedStatement ps = conn.prepareStatement(COUNT_HOLIDAY_SQL)) {

            ps.setDate(1, Date.valueOf(startDate));
            ps.setDate(2, Date.valueOf(endDate));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new AnnualLeaveException(ErrorCode.DATABASE_QUERY_FAILED);
        }
        return 0;
    }
}
