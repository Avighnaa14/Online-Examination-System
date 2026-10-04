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

@WebServlet("/test-db")
public class TestConnectionServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");

        PrintWriter out = response.getWriter();

        try {
            Connection connection = DBConnection.getConnection();

            out.println("<html>");
            out.println("<head><title>Database Test</title></head>");
            out.println("<body>");
            out.println("<h1>Database Connection Successful!</h1>");
            out.println("<p>Java → JDBC → MySQL is working.</p>");
            out.println("</body>");
            out.println("</html>");

            connection.close();

        } catch (Exception e) {

            out.println("<html>");
            out.println("<head><title>Database Test</title></head>");
            out.println("<body>");
            out.println("<h1>Database Connection Failed</h1>");
            out.println("<pre>");
            out.println(e.getMessage());
            out.println("</pre>");
            out.println("</body>");
            out.println("</html>");
        }
    }
}