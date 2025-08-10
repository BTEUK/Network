package net.bteuk.network.core.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseInit {

    // Creates the mysql connection.
    public DataSource mysqlSetup(String database, String host, int port, String username, String password) throws SQLException {

        HikariConfig cfg = new HikariConfig();

        cfg.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database
                + "?rewriteBatchedStatements=true"
                + "&allowPublicKeyRetrieval=true"
                + "&useSSL=false"
                + "&cachePrepStmts=true"
                + "&prepStmtCacheSize=256"
                + "&prepStmtCacheSqlLimit=2048"
                + "&connectTimeout=10000"
                + "&socketTimeout=30000"
                + "&connectionTimeZone=UTC");

        cfg.setUsername(username);
        cfg.setPassword(password);

        cfg.setMaximumPoolSize(20);
        cfg.setMinimumIdle(2);
        cfg.setConnectionTimeout(15000);
        cfg.setIdleTimeout(300000);
        cfg.setMaxLifetime(1800000);
        cfg.setKeepaliveTime(300000);
        cfg.setPoolName("NetworkHikariPool");

        Properties dsProps = new Properties();
        dsProps.setProperty("useUnicode", "true");
        dsProps.setProperty("characterEncoding", "utf8");
        cfg.setDataSourceProperties(dsProps);

        return new HikariDataSource(cfg);
    }
}
