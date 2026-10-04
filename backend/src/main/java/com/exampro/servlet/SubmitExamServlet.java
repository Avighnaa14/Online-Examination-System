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
import java.util.ArrayList;
import java.util.List;

@WebServlet("/student/exam/submit")
public class SubmitExamServlet extends HttpServlet {

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

        String studentIdText =
                request.getParameter("student_id");

        String examIdText =
                request.getParameter("exam_id");

        if (studentIdText == null ||
                examIdText == null ||
                studentIdText.trim().isEmpty() ||
                examIdText.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Student ID and exam ID are required."
                }
                """);

            return;
        }

        int studentId;
        int examId;

        try {

            studentId =
                    Integer.parseInt(
                            studentIdText.trim()
                    );

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
                    "message": "Invalid student ID or exam ID."
                }
                """);

            return;
        }

        /*
         * Answers are received like:
         *
         * question_1=A
         * question_2=C
         * question_3=B
         *
         * We first collect the question IDs
         * from the exam.
         */

        String questionSql = """
                SELECT
                    question_id,
                    correct_answer,
                    marks
                FROM questions
                WHERE exam_id = ?
                ORDER BY question_id
                """;

        String examSql = """
                SELECT
                    total_marks
                FROM exams
                WHERE exam_id = ?
                """;

        String resultSql = """
                INSERT INTO results
                (
                    student_id,
                    exam_id,
                    score,
                    total_marks,
                    percentage,
                    status
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        String answerSql = """
                INSERT INTO answers
                (
                    result_id,
                    question_id,
                    selected_answer,
                    correct_answer
                )
                VALUES (?, ?, ?, ?)
                """;

        Connection connection = null;

        try {

            connection =
                    DBConnection.getConnection();

            /*
             * Get exam total marks.
             */

            int totalMarks = 0;

            try (
                    PreparedStatement examStatement =
                            connection.prepareStatement(
                                    examSql
                            )
            ) {

                examStatement.setInt(
                        1,
                        examId
                );

                try (
                        ResultSet examResult =
                                examStatement.executeQuery()
                ) {

                    if (examResult.next()) {

                        totalMarks =
                                examResult.getInt(
                                        "total_marks"
                                );

                    } else {

                        response.setStatus(
                                HttpServletResponse.SC_NOT_FOUND
                        );

                        out.print("""
                            {
                                "success": false,
                                "message": "Exam not found."
                            }
                            """);

                        return;
                    }
                }
            }

            /*
             * Read all questions.
             */

            List<QuestionAnswer> questionAnswers =
                    new ArrayList<>();

            try (
                    PreparedStatement questionStatement =
                            connection.prepareStatement(
                                    questionSql
                            )
            ) {

                questionStatement.setInt(
                        1,
                        examId
                );

                try (
                        ResultSet resultSet =
                                questionStatement.executeQuery()
                ) {

                    while (resultSet.next()) {

                        int questionId =
                                resultSet.getInt(
                                        "question_id"
                                );

                        String correctAnswer =
                                resultSet.getString(
                                        "correct_answer"
                                );

                        int marks =
                                resultSet.getInt(
                                        "marks"
                                );

                        String selectedAnswer =
                                request.getParameter(
                                        "question_" + questionId
                                );

                        if (selectedAnswer == null ||
                                selectedAnswer.trim().isEmpty()) {

                            selectedAnswer = "";
                        } else {

                            selectedAnswer =
                                    selectedAnswer
                                            .trim()
                                            .toUpperCase();
                        }

                        questionAnswers.add(
                                new QuestionAnswer(
                                        questionId,
                                        selectedAnswer,
                                        correctAnswer,
                                        marks
                                )
                        );
                    }
                }
            }

            if (questionAnswers.isEmpty()) {

                response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                );

                out.print("""
                    {
                        "success": false,
                        "message": "This exam has no questions."
                    }
                    """);

                return;
            }

            /*
             * Calculate score.
             */

            int score = 0;

            for (
                    QuestionAnswer answer :
                    questionAnswers
            ) {

                if (
                        answer.selectedAnswer()
                                .equals(
                                        answer.correctAnswer()
                                )
                ) {

                    score += answer.marks();
                }
            }

            /*
             * Calculate percentage.
             *
             * We use the exam's total_marks
             * from the database.
             */

            double percentage = 0;

            if (totalMarks > 0) {

                percentage =
                        ((double) score / totalMarks)
                                * 100;
            }

            /*
             * Passing mark:
             * 40% or above = PASS
             */

            String status =
                    percentage >= 40
                            ? "PASS"
                            : "FAIL";

            /*
             * Start transaction.
             */

            connection.setAutoCommit(false);

            int resultId;

            /*
             * Insert result.
             */

            try (
                    PreparedStatement resultStatement =
                            connection.prepareStatement(
                                    resultSql,
                                    java.sql.Statement.RETURN_GENERATED_KEYS
                            )
            ) {

                resultStatement.setInt(
                        1,
                        studentId
                );

                resultStatement.setInt(
                        2,
                        examId
                );

                resultStatement.setInt(
                        3,
                        score
                );

                resultStatement.setInt(
                        4,
                        totalMarks
                );

                resultStatement.setDouble(
                        5,
                        percentage
                );

                resultStatement.setString(
                        6,
                        status
                );

                resultStatement.executeUpdate();

                try (
                        ResultSet generatedKeys =
                                resultStatement.getGeneratedKeys()
                ) {

                    if (!generatedKeys.next()) {

                        connection.rollback();

                        response.setStatus(
                                HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                        );

                        out.print("""
                            {
                                "success": false,
                                "message": "Failed to create result."
                            }
                            """);

                        return;
                    }

                    resultId =
                            generatedKeys.getInt(1);
                }
            }

            /*
             * Insert every answer.
             */

            try (
                    PreparedStatement answerStatement =
                            connection.prepareStatement(
                                    answerSql
                            )
            ) {

                for (
                        QuestionAnswer answer :
                        questionAnswers
                ) {

                    /*
                     * The database column is NOT NULL.
                     * If the student did not answer,
                     * store "N".
                     */

                    String selectedAnswer =
                            answer.selectedAnswer();

                    if (
                            selectedAnswer == null ||
                            selectedAnswer.isEmpty()
                    ) {

                        selectedAnswer = "N";
                    }

                    answerStatement.setInt(
                            1,
                            resultId
                    );

                    answerStatement.setInt(
                            2,
                            answer.questionId()
                    );

                    answerStatement.setString(
                            3,
                            selectedAnswer
                    );

                    answerStatement.setString(
                            4,
                            answer.correctAnswer()
                    );

                    answerStatement.addBatch();
                }

                answerStatement.executeBatch();
            }

            /*
             * Everything succeeded.
             */

            connection.commit();

            out.print("""
                {
                    "success": true,
                    "message": "Exam submitted successfully.",
                    "result": {
                        "result_id": %d,
                        "student_id": %d,
                        "exam_id": %d,
                        "score": %d,
                        "total_marks": %d,
                        "percentage": %.2f,
                        "status": "%s"
                    }
                }
                """.formatted(
                    resultId,
                    studentId,
                    examId,
                    score,
                    totalMarks,
                    percentage,
                    status
            ));

        } catch (Exception e) {

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (Exception rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("""
                {
                    "success": false,
                    "message": "Failed to submit exam."
                }
                """);

            e.printStackTrace();

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (Exception closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }

    private record QuestionAnswer(
            int questionId,
            String selectedAnswer,
            String correctAnswer,
            int marks
    ) {
    }
}