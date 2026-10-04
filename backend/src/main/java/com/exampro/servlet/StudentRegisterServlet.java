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

@WebServlet("/student/register")
public class StudentRegisterServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Access-Control-Allow-Origin", "*");

        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        PrintWriter out = response.getWriter();

        // Basic validation
        if (name == null || name.trim().isEmpty()
                || email == null || email.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {

            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            out.print("""
                {
                    "success": false,
                    "message": "All fields are required."
                }
                """);

            return;
        }

        String sql = """
                INSERT INTO students (name, email, password)
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, name.trim());
            statement.setString(2, email.trim());
            statement.setString(3, password);

            statement.executeUpdate();

            out.print("""
                {
                    "success": true,
                    "message": "Student registered successfully."
                }
                """);

        } catch (java.sql.SQLIntegrityConstraintViolationException e) {

            response.setStatus(HttpServletResponse.SC_CONFLICT);

            out.print("""
                {
                    "success": false,
                    "message": "Email already registered."
                }
                """);

        } catch (Exception e) {

            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            out.print("""
                {
                    "success": false,
                    "message": "Registration failed."
                }
                """);

            e.printStackTrace();
        }
    }
}