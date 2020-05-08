package gl.vms.db;

import java.sql.Connection;

import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import gl.core.fw.Config;

public class DataSource {

	private static HikariConfig config = new HikariConfig();
	private static HikariDataSource ds;

	private static void setConnection() {

		config.setJdbcUrl(Config.db_conn);
		config.setUsername(Config.db_user);
		config.setPassword(Config.db_pass);

		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		ds = new HikariDataSource(config);
	}

	private DataSource() {

	}

	public static Connection getConnection() throws SQLException {
		if (ds == null) {
			setConnection();
		}
		return ds.getConnection();
	}
}