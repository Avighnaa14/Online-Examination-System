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

@WebServlet("/admin/dashboard")
public class DashboardServlet extends HttpServlet {

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

        String studentSql =
                "SELECT COUNT(*) FROM students";

        String examSql =
                "SELECT COUNT(*) FROM exams";

        String questionSql =
                "SELECT COUNT(*) FROM questions";

        String resultSql =
                "SELECT COUNT(*) FROM results";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement studentStatement =
                        connection.prepareStatement(studentSql);

                PreparedStatement examStatement =
                        connection.prepareStatement(examSql);

                PreparedStatement questionStatement =
                        connection.prepareStatement(questionSql);

                PreparedStatement resultStatement =
                        connection.prepareStatement(resultSql)
        ) {

            int totalStudents = 0;
            int totalExams = 0;
            int totalQuestions = 0;
            int totalResults = 0;

            try (
                    ResultSet resultSet =
                            studentStatement.executeQuery()
            ) {
                if (resultSet.next()) {
                    totalStudents =
                            resultSet.getInt(1);
                }
            }

            try (
                    ResultSet resultSet =
                            examStatement.executeQuery()
            ) {
                if (resultSet.next()) {
                    totalExams =
                            resultSet.getInt(1);
                }
            }

            try (
                    ResultSet resultSet =
                            questionStatement.executeQuery()
            ) {
                if (resultSet.next()) {
                    totalQuestions =
                            resultSet.getInt(1);
                }
            }

            try (
                    ResultSet resultSet =
                            resultStatement.executeQuery()
            ) {
                if (resultSet.next()) {
                    totalResults =
                            resultSet.getInt(1);
                }
            }

            out.print("""
                {
                    "success": true,
                    "dashboard": {
                        "total_students": %d,
                        "total_exams": %d,
                        "total_questions": %d,
                        "total_results": %d
                    }
                }
                """.formatted(
                    totalStudents,
                    totalExams,
                    totalQuestions,
                    totalResults
            ));

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            out.print("""
                {
                    "success": false,
                    "message": "Failed to load dashboard data."
                }
                """);

            e.printStackTrace();
        }
    }
}