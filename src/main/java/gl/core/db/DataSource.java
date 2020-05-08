package gl.core.db;

import java.sql.SQLException;
import java.sql.Connection;
import gl.core.fw.Config;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariConfig;

public class DataSource
{
    private static HikariConfig config;
    private static HikariDataSource ds;
    
    private static void setConnection() {
        DataSource.config.setJdbcUrl(Config.db_conn);
        DataSource.config.setUsername(Config.db_user);
        DataSource.config.setPassword(Config.db_pass);
        DataSource.config.addDataSourceProperty("cachePrepStmts", (Object)"true");
        DataSource.config.addDataSourceProperty("prepStmtCacheSize", (Object)"250");
        DataSource.config.addDataSourceProperty("prepStmtCacheSqlLimit", (Object)"2048");
        DataSource.config.setIdleTimeout(2147483647L);
        DataSource.ds = new HikariDataSource(DataSource.config);
    }
    
    private DataSource() {
    }
    
    public static Connection getConnection() {
        if (DataSource.ds == null) {
            setConnection();
        }
        try {
            return DataSource.ds.getConnection();
        }
        catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    static {
        DataSource.config = new HikariConfig();
    }
}