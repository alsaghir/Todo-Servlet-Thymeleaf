package com.myapp.interfaces.web.config;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.extras.java8time.dialect.Java8TimeDialect;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ITemplateResolver;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

@WebListener
public class ThymeleafConfig implements ServletContextListener {

  private static final String TEMPLATE_ENGINE_ATTR = "com.myapp.TemplateEngineInstance";

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    TemplateEngine engine = templateEngine(sce.getServletContext());
    sce.getServletContext().setAttribute(TEMPLATE_ENGINE_ATTR, engine);
  }

  public static TemplateEngine getTemplateEngine(ServletContext context) {
    return (TemplateEngine) context.getAttribute(TEMPLATE_ENGINE_ATTR);
  }

  private TemplateEngine templateEngine(ServletContext servletContext) {
    TemplateEngine engine = new TemplateEngine();
    engine.setTemplateResolver(templateResolver(servletContext));
    engine.addDialect(new Java8TimeDialect());
    return engine;
  }

  private ITemplateResolver templateResolver(ServletContext servletContext) {
    ServletContextTemplateResolver resolver = new ServletContextTemplateResolver(servletContext);
    resolver.setPrefix("/views/");
    resolver.setTemplateMode(TemplateMode.HTML);
    return resolver;
  }

}
