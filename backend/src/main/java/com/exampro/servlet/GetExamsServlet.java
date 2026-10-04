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

@WebServlet("/student/exams")
public class GetExamsServlet extends HttpServlet {

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
                    exam_id,
                    exam_name,
                    description,
                    duration,
                    total_marks
                FROM exams
                WHERE status = 'ACTIVE'
                ORDER BY exam_id
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
                    "{\"success\":true,\"exams\":["
            );

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"exam_id\":")
                    .append(
                        resultSet.getInt("exam_id")
                    )
                    .append(",");

                json.append("\"exam_name\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "exam_name"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"description\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "description"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"duration\":")
                    .append(
                        resultSet.getInt(
                            "duration"
                        )
                    )
                    .append(",");

                json.append("\"total_marks\":")
                    .append(
                        resultSet.getInt(
                            "total_marks"
                        )
                    );

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
                    "message": "Failed to load examinations."
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