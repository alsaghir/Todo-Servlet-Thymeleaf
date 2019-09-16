package com.myapp.interfaces.web.servlet;

import com.myapp.interfaces.web.config.JdbcProvider;
import com.myapp.interfaces.web.config.ThymeleafConfig;
import com.myapp.interfaces.web.servlet.dto.RegistrationRequest;
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

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register", "/register.do"})
public class RegisterServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    System.out.println("RegisterServlet");

    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(request.getServletContext());
    WebContext context = new WebContext(request, response, request.getServletContext());
    context.setVariable("registerTabStyle", "active");
    engine.process("register.html", context, response.getWriter());
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(request.getServletContext());
    WebContext context = new WebContext(request, response, request.getServletContext());

    String name = request.getParameter("name");
    String email = request.getParameter("email");
    String password = request.getParameter("password");
    String confirmationPassword = request.getParameter("confirmationPassword");

    RegistrationRequest registrationRequest = new RegistrationRequest();
    registrationRequest.setName(name);
    registrationRequest.setEmail(email);
    registrationRequest.setPassword(password);
    registrationRequest.setConfirmationPassword(confirmationPassword);

    validateRegistrationRequest(registrationRequest, context);

    if (context.containsVariable("error")) {
      engine.process("register.html", context, response.getWriter());
      return;
    }

    Connection connection = null;
    try {
      connection = JdbcProvider.createAndGetConnection().getConnection();
      User userFoundByEmail = findByEmail(registrationRequest.getEmail(), connection);

      if(userFoundByEmail != null){
        context.setVariable("error", "Registration failed! Email is already used!");
        engine.process("register.html", context, response.getWriter());
        return;
      }

      User user = new User(name, email, password);
      User storedUser = storeNewuser(user, connection);
      HttpSession session = request.getSession(true);
      session.setAttribute("user", storedUser);
      request.getRequestDispatcher("/todos").forward(request, response);

      if(!connection.isClosed())
        connection.close();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  private User storeNewuser(User user, Connection connection) throws SQLException {
    PreparedStatement ps = connection.prepareStatement("INSERT INTO MYAPP_USER(NAME, EMAIL, PASSWORD) VALUES (?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
    ps.setString(1, user.getName());
    ps.setString(2, user.getEmail());
    ps.setString(3, user.getPassword());
    int success = ps.executeUpdate();
    if (success == 1 && ps.getGeneratedKeys().next()){
      user.setId(ps.getGeneratedKeys().getLong(1));
      return user;
    } else
      return null;
  }

  private User findByEmail(String email, Connection connection) throws SQLException {
    PreparedStatement ps = connection.prepareStatement("SELECT * FROM MYAPP_USER WHERE EMAIL = ?");
    ps.setString(1, email);
    ResultSet rs = ps.executeQuery();
    if (rs.next()) {
      return new User(rs.getLong(1),
          rs.getString(2),
          rs.getString(3),
          rs.getString(4));
    } else
      return null;
  }

  private void validateRegistrationRequest(RegistrationRequest registrationRequest, WebContext context) {
    validateName(registrationRequest.getName(), context);
    validateEmail(registrationRequest.getEmail(), context);
    validatePassword(registrationRequest.getPassword(), context);
    validateConfirmationPassword(registrationRequest.getPassword(), registrationRequest.getConfirmationPassword(), context);
  }

  private void validatePassword(String password, WebContext context) {
    if (!(password != null && password.length() >= 6)) {
      context.setVariable("errorPassword", "Password must have at least 6 characters");
      addGlobalRegistrationError(context);
    }
  }

  private void validateConfirmationPassword(String password, String confirmationPassword, WebContext context) {
    if (!(confirmationPassword != null && confirmationPassword.equals(password))) {
      context.setVariable("errorConfirmationPassword", "Confirmation password must match the password field");
      addGlobalRegistrationError(context);
    }
  }

  private void validateEmail(String email, WebContext context) {
    if (!(EmailValidator.getInstance().isValid(email))) {
      context.setVariable("errorEmail", "Email is not entered or has an invalid format ");
      addGlobalRegistrationError(context);
    }

  }

  private void validateName(String name, WebContext context) {
    if (name == null || name.isBlank() || name.isEmpty()) {
      context.setVariable("errorName", "Name is required");
      addGlobalRegistrationError(context);
    }
  }

  private void addGlobalRegistrationError(WebContext context) {
    context.setVariable("error", "Registration failed! please fix the following errors");
  }

}
