package com.lawding.leavecalc.db;

import com.lawding.leavecalc.exception.AnnualLeaveException;
import com.lawding.leavecalc.exception.ErrorCode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rds.RdsUtilities;
import software.amazon.awssdk.services.rds.model.GenerateAuthenticationTokenRequest;

public class RdsConnector {

    private static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String RDS_HOSTNAME;
    private static final int RDS_PORT;
    private static final String RDS_DATABASE ;
    private static final String RDS_USERNAME ;
    private static final String REGION ;

    static {
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            throw new AnnualLeaveException(ErrorCode.JDBC_DRIVER_NOT_FOUND);
        }

        try {
            RDS_HOSTNAME = getEnv("RDS_HOSTNAME");
            RDS_PORT = Integer.parseInt(getEnv("RDS_PORT"));
            RDS_DATABASE = getEnv("RDS_DATABASE");
            RDS_USERNAME = getEnv("RDS_USERNAME");
            REGION = getEnv("REGION");
        } catch (NumberFormatException e) {
            throw new AnnualLeaveException(ErrorCode.RDS_ENV_MISSING);
        }
    }
    public static Connection getConnection() {
        try {
            String authToken = generateAuthToken();
            String jdbcUrl = String.format("jdbc:mysql://%s:%d/%s",
                RDS_HOSTNAME, RDS_PORT, RDS_DATABASE);
            return DriverManager.getConnection(jdbcUrl, RDS_USERNAME, authToken);
        } catch (SQLException e) {
            throw new AnnualLeaveException(ErrorCode.DATABASE_CONNECTION_FAILED);
        }
    }

    private static String generateAuthToken() {
        try {
            Region region = Region.of(REGION);
            DefaultCredentialsProvider credentialsProvider = DefaultCredentialsProvider.create();

            RdsUtilities rdsUtilities = RdsUtilities.builder()
                .credentialsProvider(credentialsProvider)
                .region(region)
                .build();

            String token = rdsUtilities.generateAuthenticationToken(
                GenerateAuthenticationTokenRequest.builder()
                    .hostname(RDS_HOSTNAME)
                    .port(RDS_PORT)
                    .username(RDS_USERNAME)
                    .build()
            );
            return token;
        } catch (Exception e) {
            throw new AnnualLeaveException(ErrorCode.IAM_AUTH_TOKEN_FAILED);
        }
    }

    private static String getEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new AnnualLeaveException(ErrorCode.RDS_ENV_MISSING);
        }
        return value;
    }
}
