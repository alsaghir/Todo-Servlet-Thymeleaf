package com.myapp.interfaces.web.config;

import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.sql.PooledConnection;
import org.apache.derby.jdbc.ClientConnectionPoolDataSource;

@WebListener
public class JdbcProvider implements ServletContextListener {

  private static ClientConnectionPoolDataSource DATASOURCE = null;

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    createDerbyDataSource();

  }

  private static ClientConnectionPoolDataSource createDerbyDataSource(){
    if(DATASOURCE == null) {
      ClientConnectionPoolDataSource cpds = new ClientConnectionPoolDataSource();
      cpds.setMaxStatements(20);
      cpds.setDatabaseName("tododb");
      cpds.setUser("u");
      cpds.setPassword("p");
      cpds.setServerName("127.0.0.1");
      cpds.setPortNumber(65488);
      DATASOURCE = cpds;
    }
    return DATASOURCE;
  }

  public static PooledConnection createAndGetConnection() throws SQLException {
    return DATASOURCE.getPooledConnection();
  }

}
