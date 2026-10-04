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

@WebServlet("/admin/questions")
public class GetQuestionsServlet extends HttpServlet {

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

        String examIdText =
                request.getParameter("exam_id");

        PrintWriter out =
                response.getWriter();

        if (examIdText == null ||
            examIdText.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Exam ID is required."
                }
                """);

            return;
        }

        int examId;

        try {

            examId =
                    Integer.parseInt(
                            examIdText.trim()
                    );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Invalid exam ID."
                }
                """);

            return;
        }

        String sql = """
                SELECT
                    question_id,
                    question_text,
                    option_a,
                    option_b,
                    option_c,
                    option_d,
                    correct_answer,
                    marks
                FROM questions
                WHERE exam_id = ?
                ORDER BY question_id
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    examId
            );

            ResultSet resultSet =
                    statement.executeQuery();

            StringBuilder json =
                    new StringBuilder();

            json.append(
                    "{\"success\":true,\"questions\":["
            );

            boolean first = true;

            while (resultSet.next()) {

                if (!first) {
                    json.append(",");
                }

                first = false;

                json.append("{");

                json.append("\"question_id\":")
                    .append(
                        resultSet.getInt(
                            "question_id"
                        )
                    )
                    .append(",");

                json.append("\"question_text\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "question_text"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"option_a\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "option_a"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"option_b\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "option_b"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"option_c\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "option_c"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"option_d\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "option_d"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"correct_answer\":\"")
                    .append(
                        escapeJson(
                            resultSet.getString(
                                "correct_answer"
                            )
                        )
                    )
                    .append("\",");

                json.append("\"marks\":")
                    .append(
                        resultSet.getInt(
                            "marks"
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
                    "message": "Failed to load questions."
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