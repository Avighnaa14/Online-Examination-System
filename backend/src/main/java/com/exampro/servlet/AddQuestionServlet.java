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

@WebServlet("/admin/question/add")
public class AddQuestionServlet extends HttpServlet {

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

        String examIdText =
                request.getParameter("exam_id");

        String questionText =
                request.getParameter("question_text");

        String optionA =
                request.getParameter("option_a");

        String optionB =
                request.getParameter("option_b");

        String optionC =
                request.getParameter("option_c");

        String optionD =
                request.getParameter("option_d");

        String correctAnswer =
                request.getParameter("correct_answer");

        String marksText =
                request.getParameter("marks");

        PrintWriter out =
                response.getWriter();


        if (examIdText == null ||
            questionText == null ||
            optionA == null ||
            optionB == null ||
            optionC == null ||
            optionD == null ||
            correctAnswer == null ||
            marksText == null ||
            examIdText.trim().isEmpty() ||
            questionText.trim().isEmpty() ||
            optionA.trim().isEmpty() ||
            optionB.trim().isEmpty() ||
            optionC.trim().isEmpty() ||
            optionD.trim().isEmpty() ||
            correctAnswer.trim().isEmpty() ||
            marksText.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "All question fields are required."
                }
                """);

            return;
        }


        int examId;
        int marks;


        try {

            examId =
                    Integer.parseInt(
                            examIdText.trim()
                    );

            marks =
                    Integer.parseInt(
                            marksText.trim()
                    );

        } catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Exam ID and marks must be numbers."
                }
                """);

            return;
        }


        String answer =
                correctAnswer
                        .trim()
                        .toUpperCase();


        if (!answer.equals("A") &&
            !answer.equals("B") &&
            !answer.equals("C") &&
            !answer.equals("D")) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Correct answer must be A, B, C or D."
                }
                """);

            return;
        }


        if (marks <= 0) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Marks must be greater than zero."
                }
                """);

            return;
        }


        String sql = """
                INSERT INTO questions
                (
                    exam_id,
                    question_text,
                    option_a,
                    option_b,
                    option_c,
                    option_d,
                    correct_answer,
                    marks
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
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

            statement.setString(
                    2,
                    questionText.trim()
            );

            statement.setString(
                    3,
                    optionA.trim()
            );

            statement.setString(
                    4,
                    optionB.trim()
            );

            statement.setString(
                    5,
                    optionC.trim()
            );

            statement.setString(
                    6,
                    optionD.trim()
            );

            statement.setString(
                    7,
                    answer
            );

            statement.setInt(
                    8,
                    marks
            );


            int rows =
                    statement.executeUpdate();


            if (rows > 0) {

                out.print("""
                    {
                        "success": true,
                        "message": "Question added successfully."
                    }
                    """);

            } else {

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                out.print("""
                    {
                        "success": false,
                        "message": "Question could not be added."
                    }
                    """);
            }

        }

        catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("""
                {
                    "success": false,
                    "message": "Failed to add question."
                }
                """);

            e.printStackTrace();
        }
    }
}