package com.myapp.interfaces.web.servlet;

import com.myapp.interfaces.web.config.JdbcProvider;
import com.myapp.interfaces.web.config.Priority;
import com.myapp.interfaces.web.config.ThymeleafConfig;
import com.myapp.interfaces.web.servlet.entity.Todo;
import com.myapp.interfaces.web.servlet.entity.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

@WebServlet(name = "CreateTodoServlet", urlPatterns = {"/todos/new", "/todos/new.do"})
public class CreateTodoServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException, IOException {
    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(request.getServletContext());
    WebContext context = new WebContext(request, response, request.getServletContext());
    context.setVariable("today", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    engine.process("create.html", context, response.getWriter());
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    HttpSession session = request.getSession();
    User user = (User) session.getAttribute("user");

    String title = request.getParameter("title");
    String dueDate = request.getParameter("dueDate");
    String priority = request.getParameter("priority");

    Todo todo = new Todo(title,
        false,
        Priority.valueOf(priority),
        DateTimeFormatter.ofPattern("dd/MM/yyyy").parse(dueDate, LocalDate::from),
        user.getId());

    Connection connection = null;

    try {
      connection = JdbcProvider.createAndGetConnection().getConnection();
      saveTodo(todo, connection);
      request.getRequestDispatcher("/todos").forward(request, response);

      if (!connection.isClosed()) {
        connection.close();
      }
    } catch (SQLException ex) {
      ex.printStackTrace();
    }

    response.sendRedirect("/todos");
  }

  private Todo saveTodo(Todo todo, Connection connection) throws SQLException {
      PreparedStatement ps = connection.prepareStatement("INSERT INTO MYAPP_TODO(TITLE, DONE, PRIORITY, DUEDATE, USER_ID) VALUES (?, ?, ?, ?, ?)", PreparedStatement.RETURN_GENERATED_KEYS);
      int success = bindPreparedStatementWithTodo(todo, ps);
      if (success == 1 && ps.getGeneratedKeys().next()) {
          todo.setId(ps.getGeneratedKeys().getInt(1));
          return todo;
      } else
          return null;
  }

    private int bindPreparedStatementWithTodo(Todo todo, PreparedStatement ps) throws SQLException {
        ps.setString(1, todo.getTitle());
        ps.setBoolean(2, todo.isDone());
        ps.setInt(3, todo.getPriority().numeric());
        ps.setTimestamp(4, Timestamp.valueOf(todo.getDueDate().atTime(0,0,0)));
        ps.setLong(5, todo.getUserId());
        return ps.executeUpdate();
    }

}
