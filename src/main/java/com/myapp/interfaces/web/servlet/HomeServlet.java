package com.myapp.interfaces.web.servlet;

import com.myapp.interfaces.web.config.JdbcProvider;
import com.myapp.interfaces.web.config.Priority;
import com.myapp.interfaces.web.config.ThymeleafConfig;
import com.myapp.interfaces.web.servlet.entity.Todo;
import com.myapp.interfaces.web.servlet.entity.User;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

@WebServlet(name = "HomeServlet", urlPatterns = "/todos")
public class HomeServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(request.getServletContext());
    WebContext context = new WebContext(request, response, request.getServletContext());

    HttpSession session = request.getSession();
    User user = (User) session.getAttribute("user");

    if (user == null) {
      response.sendRedirect("/login");
      return;
    }

    Connection connection = null;

    try {
      connection = JdbcProvider.createAndGetConnection().getConnection();
      List<Todo> todoList = findTodoListByUserId(user.getId(), connection);

      context.setVariable("todoList", todoList);

      int totalCount = todoList.size();
      int doneCount = countTotalDone(todoList);
      int todoCount = totalCount - doneCount;

      context.setVariable("totalCount", totalCount);
      context.setVariable("doneCount", doneCount);
      context.setVariable("todoCount", todoCount);

      engine.process("home.html", context, response.getWriter());

      if(!connection.isClosed())
        connection.close();

    } catch (SQLException ex) {
      ex.printStackTrace();
    }


  }

  private int countTotalDone(List<Todo> todoList) {
    int count = 0;
    for (Todo todo: todoList) {
      if (todo.isDone())
        count++;
    }
    return count;
  }

  private List<Todo> findTodoListByUserId(Long userId, Connection connection) throws SQLException {
    PreparedStatement ps = connection.prepareStatement("select * from MYAPP_TODO where USER_ID = ?");
    ps.setLong(1, userId);
    ResultSet rs = ps.executeQuery();
    List<Todo> todoList = new ArrayList<>();
    while (rs.next()) {
      todoList.add(getTodo(rs));
    }
    return todoList;
  }

  private Todo getTodo(ResultSet rs) throws SQLException {
    return new Todo(rs.getLong(1),
        rs.getString(2),
        rs.getBoolean(3),
        Priority.getByNummericValue(rs.getInt(4)),
        rs.getTimestamp(5).toLocalDateTime().toLocalDate(),
        rs.getLong(6));
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    doGet(request, response);
  }
}
