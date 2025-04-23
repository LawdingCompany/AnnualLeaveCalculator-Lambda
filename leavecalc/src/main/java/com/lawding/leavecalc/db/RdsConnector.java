package com.lawding.leavecalc.db;

import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import com.lawding.leavecalc.util.ParameterStoreUtils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class RdsConnector {

    public static final String JDBC_URL = ParameterStoreUtils.getSecureParameter(
        "/leavecalc/db/url");
    public static final String DB_USERNAME = ParameterStoreUtils.getSecureParameter(
        "/leavecalc/db/username/lambda");
    public static final String DB_PASSWORD = ParameterStoreUtils.getSecureParameter(
        "/leavecalc/db/password/lambda");

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(JDBC_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            throw new AnnualLeaveException(ErrorCode.DATABASE_CONNECTION_FAILED);
        }
    }


}
