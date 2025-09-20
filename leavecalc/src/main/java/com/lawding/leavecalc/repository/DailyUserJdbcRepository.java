package com.lawding.leavecalc.repository;

import com.lawding.leavecalc.db.RdsConnector;
import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;

public class DailyUserJdbcRepository {

    public DailyUserJdbcRepository() {
        // ✅ 생성자에서는 DB 연결하지 않음
    }
    private static final String SQL_UPSERT_WEB= """
        INSERT INTO daily_user (record_date, web)
        VALUES(?,1)
        ON DUPLICATE KEY UPDATE web = web + 1
        """;

    private static final String SQL_UPSERT_IOS= """
        INSERT INTO daily_user (record_date, ios)
        VALUES(?,1)
        ON DUPLICATE KEY UPDATE ios = ios + 1
        """;

    public void incrementCount(LocalDate date, String platform){
        String sql = switch (platform) {
            case "web" -> SQL_UPSERT_WEB;
            case "ios" -> SQL_UPSERT_IOS;
            default -> throw new AnnualLeaveException(ErrorCode.INVALID_PLATFORM);
        };

        try (Connection conn = RdsConnector.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(date));
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new AnnualLeaveException(ErrorCode.DATABASE_QUERY_FAILED);
        }
    }
}
