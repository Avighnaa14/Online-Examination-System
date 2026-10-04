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

@WebServlet("/admin/results")
public class GetAllResultsServlet extends HttpServlet {

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
                    r.result_id,
                    r.student_id,
                    s.name AS student_name,
                    s.email AS student_email,
                    r.exam_id,
                    e.exam_name,
                    r.score,
                    r.total_marks,
                    r.percentage,
                    r.status,
                    r.exam_date
                FROM results r
                INNER JOIN students s
                    ON r.student_id = s.student_id
                INNER JOIN exams e
                    ON r.exam_id = e.exam_id
                ORDER BY r.exam_date DESC
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
                    "{\"success\":true,\"results\":["
            );

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"result_id\":")
                        .append(
                                resultSet.getInt(
                                        "result_id"
                                )
                        )
                        .append(",");

                json.append("\"student_id\":")
                        .append(
                                resultSet.getInt(
                                        "student_id"
                                )
                        )
                        .append(",");

                json.append("\"student_name\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "student_name"
                                        )
                                )
                        )
                        .append("\",");

                json.append("\"student_email\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "student_email"
                                        )
                                )
                        )
                        .append("\",");

                json.append("\"exam_id\":")
                        .append(
                                resultSet.getInt(
                                        "exam_id"
                                )
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

                json.append("\"score\":")
                        .append(
                                resultSet.getInt(
                                        "score"
                                )
                        )
                        .append(",");

                json.append("\"total_marks\":")
                        .append(
                                resultSet.getInt(
                                        "total_marks"
                                )
                        )
                        .append(",");

                json.append("\"percentage\":")
                        .append(
                                resultSet.getDouble(
                                        "percentage"
                                )
                        )
                        .append(",");

                json.append("\"status\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "status"
                                        )
                                )
                        )
                        .append("\",");

                json.append("\"exam_date\":\"")
                        .append(
                                escapeJson(
                                        resultSet.getString(
                                                "exam_date"
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
                    "message": "Failed to load results."
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