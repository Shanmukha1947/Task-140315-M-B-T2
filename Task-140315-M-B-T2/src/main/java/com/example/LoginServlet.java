package com.example;



import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final String USERNAME = "your_database_username";
    private final String PASSWORD = "your_database_password";
    private final String DB_URL = "jdbc:mysql://localhost:3306/your_database";

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        String query = "SELECT password FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (isPasswordMatch(password, hashedPassword)) {
                    HttpSession session = req.getSession();
                    session.setAttribute("username", username);
                    resp.sendRedirect("home.jsp");
                } else {
                    req.setAttribute("error", "Invalid username or password.");
                    req.getRequestDispatcher("login.jsp").forward(req, resp);
                }
            } else {
                req.setAttribute("error", "Invalid username or password.");
                req.getRequestDispatcher("login.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            req.setAttribute("error", "An error occurred during login.");
            req.getRequestDispatcher("login.jsp").forward(req, resp);
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isPasswordMatch(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}

