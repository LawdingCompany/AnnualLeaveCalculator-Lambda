package com.lawding.leavecalc.db;

import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RdsConnector {

    public static final String JDBC_URL = System.getenv("RDS_URL");
    public static final String DB_USERNAME = System.getenv("RDS_USERNAME");
    public static final String DB_PASSWORD = System.getenv("RDS_PASSWORD");

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            throw new AnnualLeaveException(ErrorCode.DATABASE_CONNECTION_FAILED);
        }
    }


}
