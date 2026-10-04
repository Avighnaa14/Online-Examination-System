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

@WebServlet("/admin/exam/create")
public class CreateExamServlet extends HttpServlet {

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

        String name =
                request.getParameter("name");

        String description =
                request.getParameter("description");

        String durationText =
                request.getParameter("duration");

        String marksText =
                request.getParameter("marks");

        PrintWriter out =
                response.getWriter();


        if (name == null ||
            name.trim().isEmpty() ||

            description == null ||
            description.trim().isEmpty() ||

            durationText == null ||
            durationText.trim().isEmpty() ||

            marksText == null ||
            marksText.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "All examination details are required."
                }
                """);

            return;
        }


        int duration;

        int marks;


        try {

            duration =
                    Integer.parseInt(
                            durationText.trim()
                    );

            marks =
                    Integer.parseInt(
                            marksText.trim()
                    );

        }

        catch (NumberFormatException e) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Duration and marks must be numbers."
                }
                """);

            return;
        }


        if (duration <= 0 || marks <= 0) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Duration and marks must be greater than zero."
                }
                """);

            return;
        }


        String sql = """
                INSERT INTO exams
                (exam_name, description, duration, total_marks, status)
                VALUES (?, ?, ?, ?, 'ACTIVE')
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    name.trim()
            );

            statement.setString(
                    2,
                    description.trim()
            );

            statement.setInt(
                    3,
                    duration
            );

            statement.setInt(
                    4,
                    marks
            );


            int rows =
                    statement.executeUpdate();


            if (rows > 0) {

                out.print("""
                    {
                        "success": true,
                        "message": "Examination created successfully."
                    }
                    """);

            }

            else {

                response.setStatus(
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR
                );

                out.print("""
                    {
                        "success": false,
                        "message": "Examination could not be created."
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
                    "message": "Failed to create examination."
                }
                """);

            e.printStackTrace();
        }
    }
}