package com.exampro.servlet;

import com.exampro.util.DBConnection;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@WebServlet("/admin/login")
public class AdminLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader(
                "Access-Control-Allow-Origin",
                "*"
        );

        String username =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        PrintWriter out =
                response.getWriter();


        if (username == null ||
            username.trim().isEmpty() ||
            password == null ||
            password.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Username and password are required."
                }
                """);

            return;
        }


        String sql = """
                SELECT admin_id, username
                FROM admins
                WHERE username = ? AND password = ?
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    username.trim()
            );

            statement.setString(
                    2,
                    password
            );


            try (
                    ResultSet resultSet =
                            statement.executeQuery()
            ) {

                if (resultSet.next()) {

                    int adminId =
                            resultSet.getInt(
                                    "admin_id"
                            );

                    String adminUsername =
                            resultSet.getString(
                                    "username"
                            );

                    out.print("""
                        {
                            "success": true,
                            "message": "Login successful.",
                            "admin": {
                                "admin_id": %d,
                                "username": "%s"
                            }
                        }
                        """.formatted(
                            adminId,
                            escapeJson(adminUsername)
                    ));

                }

                else {

                    response.setStatus(
                            HttpServletResponse.SC_UNAUTHORIZED
                    );

                    out.print("""
                        {
                            "success": false,
                            "message": "Invalid username or password."
                        }
                        """);

                }

            }

        }

        catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("""
                {
                    "success": false,
                    "message": "Admin login failed."
                }
                """);

            e.printStackTrace();
        }
    }


    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }
}