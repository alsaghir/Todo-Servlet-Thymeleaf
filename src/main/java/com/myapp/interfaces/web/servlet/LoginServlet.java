package com.myapp.interfaces.web.servlet;

import com.myapp.interfaces.web.config.JdbcProvider;
import com.myapp.interfaces.web.config.ThymeleafConfig;
import com.myapp.interfaces.web.servlet.entity.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.validator.routines.EmailValidator;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login", "/login.do"})
public class LoginServlet extends HttpServlet {
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(request.getServletContext());
    WebContext context = new WebContext(request, response, request.getServletContext());

    String email = request.getParameter("email");
    String password = request.getParameter("password");

    validateEmail(email, context);
    validatePassword(password, context);

    if (context.containsVariable("error")) {
      engine.process("login.html", context, response.getWriter());
      return;
    }

    Connection connection = null;
    try {
      connection = JdbcProvider.createAndGetConnection().getConnection();
      User user = findUserByEmailAndPassword(email, password, connection);

      if(user == null){
        context.setVariable("error", "Login failed! Error in email or password");
        engine.process("login.html", context, response.getWriter());
        return;
      }

      HttpSession session = request.getSession(true);
      session.setAttribute("user", user);
      request.getRequestDispatcher("/todos").forward(request, response);

      if(!connection.isClosed())
        connection.close();
    } catch (SQLException ex){
      ex.printStackTrace();
    }

  }

  private User findUserByEmailAndPassword(String email, String password, Connection connection) throws SQLException {
    PreparedStatement ps = connection.prepareStatement("SELECT * FROM MYAPP_USER WHERE EMAIL = ? AND PASSWORD = ?");
    ps.setString(1, email);
    ps.setString(2, password);
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      return new User(rs.getLong(1),
          rs.getString(2),
          rs.getString(3),
          rs.getString(4));
    } else
      return null;
  }

  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(request.getServletContext());
    WebContext context = new WebContext(request, response, request.getServletContext());
    context.setVariable("loginTabStyle", "active");
    engine.process("login.html", context, response.getWriter());
  }

  private void validateEmail(String email, WebContext context) {
    if (!(EmailValidator.getInstance().isValid(email))) {
      context.setVariable("errorEmail", "Email is not entered or has an invalid format ");
      addGlobalRegistrationError(context);
    }
  }

  private void validatePassword(String password, WebContext context) {
    if (!(password != null && password.length() >= 6)) {
      context.setVariable("errorPassword", "Password must have at least 6 characters");
      addGlobalRegistrationError(context);
    }
  }

  private void addGlobalRegistrationError(WebContext context) {
    context.setVariable("error", "Login failed! please fix the following errors");
  }
}
