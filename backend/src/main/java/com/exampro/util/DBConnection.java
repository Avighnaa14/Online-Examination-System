package com.exampro.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static String getRequiredEnv(String name)
            throws SQLException {
        String value = System.getenv(name);

        if (value == null || value.trim().isEmpty()) {
            throw new SQLException(
                "Missing required environment variable: " + name
            );
        }

        return value.trim();
    }

    public static Connection getConnection() throws SQLException {

        String host = getRequiredEnv("DB_HOST");
        String port = getRequiredEnv("DB_PORT");
        String database = getRequiredEnv("DB_NAME");
        String user = getRequiredEnv("DB_USER");
        String password = getRequiredEnv("DB_PASSWORD");

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException(
                "MySQL JDBC driver not found.", e
            );
        }

        String url = "jdbc:mysql://" + host + ":" + port + "/"
                + database
                + "?sslMode=REQUIRED"
                + "&connectTimeout=10000"
                + "&socketTimeout=15000"
                + "&tcpKeepAlive=true"
                + "&serverTimezone=UTC";

        return DriverManager.getConnection(url, user, password);
    }
}