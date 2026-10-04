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

@WebServlet("/admin/students")
public class GetStudentsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        response.setHeader(
                "Access-Control-Allow-Origin",
                "*"
        );

        PrintWriter out = response.getWriter();

        String sql = """
                SELECT
                    student_id,
                    name,
                    email,
                    created_at
                FROM students
                ORDER BY student_id DESC
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        statement.executeQuery()
        ) {

            StringBuilder json =
                    new StringBuilder();

            json.append(
                    "{\"success\":true,\"students\":["
            );

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"student_id\":")
                        .append(
                                resultSet.getInt(
                                        "student_id"
                                )
                        )
                        .append(",");

                json.append("\"name\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "name"
                                        )
                                )
                        )
                        .append("\",");

                json.append("\"email\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "email"
                                        )
                                )
                        )
                        .append("\",");

                json.append("\"created_at\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "created_at"
                                        )
                                )
                        )
                        .append("\"");

                json.append("}");
            }

            json.append("]}");

            out.print(
                    json.toString()
            );

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("""
                {
                    "success": false,
                    "message": "Failed to load students."
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