package com.myapp.interfaces.web.servlet;

import com.myapp.interfaces.web.config.ThymeleafConfig;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;

@WebServlet(name = "IndexServlet", urlPatterns = {"", "index"})
public class IndexServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    System.out.println("IndexServlet");

    TemplateEngine engine = ThymeleafConfig.getTemplateEngine(request.getServletContext());
    response.setContentType("text/html;charset=UTF-8");
    WebContext context = new WebContext(request, response, request.getServletContext());
    engine.process("index.html", context, response.getWriter());
  }
}
