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

@WebServlet("/student/login")
public class StudentLoginServlet extends HttpServlet {

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

        String email =
                request.getParameter("email");

        String password =
                request.getParameter("password");

        PrintWriter out =
                response.getWriter();


        if (email == null ||
            email.trim().isEmpty() ||
            password == null ||
            password.trim().isEmpty()) {

            response.setStatus(
                    HttpServletResponse.SC_BAD_REQUEST
            );

            out.print("""
                {
                    "success": false,
                    "message": "Email and password are required."
                }
                """);

            return;
        }


        String sql = """
                SELECT student_id, name, email
                FROM students
                WHERE email = ? AND password = ?
                """;


        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1,
                    email.trim()
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

                    int studentId =
                            resultSet.getInt(
                                    "student_id"
                            );

                    String name =
                            resultSet.getString(
                                    "name"
                            );

                    String studentEmail =
                            resultSet.getString(
                                    "email"
                            );


                    out.print("""
                        {
                            "success": true,
                            "message": "Login successful.",
                            "student": {
                                "student_id": %d,
                                "name": "%s",
                                "email": "%s"
                            }
                        }
                        """.formatted(
                            studentId,
                            escapeJson(name),
                            escapeJson(studentEmail)
                    ));

                }

                else {

                    response.setStatus(
                            HttpServletResponse.SC_UNAUTHORIZED
                    );

                    out.print("""
                        {
                            "success": false,
                            "message": "Invalid email or password."
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
                    "message": "Login failed."
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