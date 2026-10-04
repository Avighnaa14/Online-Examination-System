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

@WebServlet("/admin/question/delete")
public class DeleteQuestionServlet extends HttpServlet {

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

        PrintWriter out = response.getWriter();

        String questionIdText =
                request.getParameter("question_id");

        if (questionIdText == null ||
                questionIdText.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Question ID is required."
                }
                """);

            return;
        }

        int questionId;

        try {

            questionId =
                    Integer.parseInt(
                            questionIdText.trim()
                    );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Invalid question ID."
                }
                """);

            return;
        }

        String sql = """
                DELETE FROM questions
                WHERE question_id = ?
                """;

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setInt(
                    1,
                    questionId
            );

            int rowsDeleted =
                    statement.executeUpdate();

            if (rowsDeleted > 0) {

                out.print("""
                    {
                        "success": true,
                        "message": "Question deleted successfully."
                    }
                    """);

            } else {

                response.setStatus(
                        HttpServletResponse.SC_NOT_FOUND
                );

                out.print("""
                    {
                        "success": false,
                        "message": "Question not found."
                    }
                    """);
            }

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("""
                {
                    "success": false,
                    "message": "Failed to delete question."
                }
                """);

            e.printStackTrace();
        }
    }
}