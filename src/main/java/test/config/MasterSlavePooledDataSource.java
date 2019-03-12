//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package test.config;

import com.codahale.metrics.MetricRegistry;
import com.zaxxer.hikari.HikariDataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MasterSlavePooledDataSource implements DataSource {
    private static final long DEFAULT_SLOW_STATEMENT_THRESHOLD = 500L;
    private static final int DEFAULT_MASTER_MINIMUM_IDLE = 10;
    private static final int DEFAULT_SLAVE_MINIMUM_IDLE = 10;
    private static final int DEFAULT_MASTER_BORROW_TIMEOUT = 2000;
    private static final int DEFAULT_SLAVE_BORROW_TIMEOUT = 2000;
    private static final int DEFAULT_MASTER_CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_SLAVE_CONNECT_TIMEOUT = 30000;
    private static final int DEFAULT_MASTER_SOCKET_TIMEOUT = 60000;
    private static final int DEFAULT_SLAVE_SOCKET_TIMEOUT = 60000;
    private static final String SET_NAMES_UTF8MB4 = "SET NAMES utf8mb4";
    private static Logger log = LoggerFactory.getLogger(MasterSlavePooledDataSource.class);
    private PrintWriter logWriter;
    private HikariDataSource masterDataSource;
    private Properties masterDriverProps = new Properties();
    private HikariDataSource slaveDataSource;
    private Properties slaveDriverProps = new Properties();
    private MetricRegistry metricRegistry;
    private String name;
    private String jdbcDataSourceClass;
    private String jdbcDataSourceType;
    private boolean instrumentPreparedStatements = false;
    private long slowStatementThreshold = 500L;
    private boolean supportFullUtf8;

    public MasterSlavePooledDataSource() {
    }

    public void open() {
        this.masterDataSource = this.getMasterDataSource();
        this.slaveDataSource = this.getSlaveDataSource();
    }

    public void close() {
        try {
            this.masterDataSource.close();
        } catch (Exception var3) {
            this.logWriter.write("Couldn't close master pool: " + var3.getMessage());
        }

        try {
            this.slaveDataSource.close();
        } catch (Exception var2) {
            this.logWriter.write("Couldn't close slave pool: " + var2.getMessage());
        }

    }

    private HikariDataSource getMasterDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(this.getMasterMaximumPoolSize());
        ds.setMinimumIdle(this.getMasterMinimumIdle());
        ds.setConnectionTimeout((long)this.getMasterBorrowTimeout());
        if (this.isSupportFullUtf8()) {
            ds.setConnectionInitSql("SET NAMES utf8mb4");
        }

        if (this.name != null && !this.name.isEmpty()) {
            ds.setPoolName(this.name + "-master");
        }

        if (this.metricRegistry != null) {
            ds.setMetricRegistry(this.metricRegistry);
        }

        ds.setDataSourceClassName(this.getJdbcDataSourceClass());
        ds.addDataSourceProperty("url", this.masterDriverProps.get("url").toString());
        ds.addDataSourceProperty("user", this.getMasterUser());
        ds.addDataSourceProperty("password", this.getMasterPassword());
        if (this.getJdbcDataSourceType().equalsIgnoreCase("mysql")) {
            ds.addDataSourceProperty("cachePrepStmts", true);
            ds.addDataSourceProperty("prepStmtCacheSize", 250);
            ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            ds.addDataSourceProperty("useServerPrepStmts", true);
            ds.addDataSourceProperty("connectTimeout", this.getMasterConnectTimeout());
            ds.addDataSourceProperty("socketTimeout", this.getMasterSocketTimeout());
        }

        return ds;
    }

    private HikariDataSource getSlaveDataSource() {
        HikariDataSource ds = new HikariDataSource();
        ds.setMaximumPoolSize(this.getSlaveMaximumPoolSize());
        ds.setMinimumIdle(this.getSlaveMinimumIdle());
        ds.setConnectionTimeout((long)this.getSlaveBorrowTimeout());
        if (this.isSupportFullUtf8()) {
            ds.setConnectionInitSql("SET NAMES utf8mb4");
        }

        if (this.name != null && !this.name.isEmpty()) {
            ds.setPoolName(this.name + "-slave");
        }

        if (this.metricRegistry != null) {
            ds.setMetricRegistry(this.metricRegistry);
        }

        ds.setDataSourceClassName(this.getJdbcDataSourceClass());
        ds.addDataSourceProperty("url", this.slaveDriverProps.get("url").toString());
        ds.addDataSourceProperty("user", this.getSlaveUser());
        ds.addDataSourceProperty("password", this.getSlavePassword());
        if (this.getJdbcDataSourceType().equalsIgnoreCase("mysql")) {
            ds.addDataSourceProperty("cachePrepStmts", true);
            ds.addDataSourceProperty("prepStmtCacheSize", 250);
            ds.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
            ds.addDataSourceProperty("useServerPrepStmts", true);
            ds.addDataSourceProperty("connectTimeout", this.getSlaveConnectTimeout());
            ds.addDataSourceProperty("socketTimeout", this.getSlaveSocketTimeout());
        }

        return ds;
    }

    public Connection getConnection() throws SQLException {
        return new MasterSlaveConnection(this);
    }

    public Connection getConnection(String user, String password) throws SQLException {
        return this.getConnection();
    }

    public PrintWriter getLogWriter() throws SQLException {
        return this.logWriter;
    }

    public void setLogWriter(PrintWriter printWriter) throws SQLException {
        this.logWriter = printWriter;
    }

    public void setLoginTimeout(int i) {
        DriverManager.setLoginTimeout(i);
    }

    public int getLoginTimeout() throws SQLException {
        return 0;
    }

    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    public <T> T unwrap(Class<T> tClass) throws SQLException {
        return null;
    }

    public boolean isWrapperFor(Class<?> aClass) throws SQLException {
        return false;
    }

    public void setMasterUser(String user) {
        this.masterDriverProps.put("user", user);
    }

    public void setMasterPassword(String password) {
        this.masterDriverProps.put("password", password);
    }

    public void setMasterUrl(String url) {
        this.masterDriverProps.put("url", url);
    }

    public void setMasterMaximumPoolSize(int size) {
        this.masterDriverProps.put("maximumPoolSize", String.valueOf(size));
    }

    public void setMasterMaxActive(int size) {
        this.setMasterMaximumPoolSize(size);
    }

    public void setSlaveMaximumPoolSize(int size) {
        this.slaveDriverProps.put("maximumPoolSize", String.valueOf(size));
    }

    public void setSlaveMaxActive(int size) {
        this.setSlaveMaximumPoolSize(size);
    }

    public int getMasterMaximumPoolSize() {
        return Integer.valueOf(this.masterDriverProps.get("maximumPoolSize").toString());
    }

    public int getSlaveMaximumPoolSize() {
        return Integer.valueOf(this.slaveDriverProps.get("maximumPoolSize").toString());
    }

    public void setMasterMinimumIdle(int size) {
        this.masterDriverProps.put("minimumIdle", String.valueOf(size));
    }

    public int getMasterMinimumIdle() {
        return this.masterDriverProps.get("minimumIdle") != null ? Integer.valueOf(this.masterDriverProps.get("minimumIdle").toString()) : 10;
    }

    public void setSlaveMinimumIdle(int size) {
        this.slaveDriverProps.put("minimumIdle", String.valueOf(size));
    }

    public int getSlaveMinimumIdle() {
        return this.slaveDriverProps.get("minimumIdle") != null ? Integer.valueOf(this.slaveDriverProps.get("minimumIdle").toString()) : 10;
    }

    public void setMasterBorrowTimeout(int timeoutInMs) {
        this.masterDriverProps.put("borrowTimeout", String.valueOf(timeoutInMs));
    }

    public int getMasterBorrowTimeout() {
        return this.masterDriverProps.get("borrowTimeout") != null ? Integer.valueOf(this.masterDriverProps.get("borrowTimeout").toString()) : 2000;
    }

    public void setMasterConnectTimeout(int timeoutInMs) {
        this.masterDriverProps.put("connectTimeout", String.valueOf(timeoutInMs));
    }

    public int getMasterConnectTimeout() {
        return this.masterDriverProps.get("connectTimeout") != null ? Integer.valueOf(this.masterDriverProps.get("connectTimeout").toString()) : 30000;
    }

    public void setMasterSocketTimeout(int timeoutInMs) {
        this.masterDriverProps.put("socketTimeout", String.valueOf(timeoutInMs));
    }

    public int getMasterSocketTimeout() {
        return this.masterDriverProps.get("socketTimeout") != null ? Integer.valueOf(this.masterDriverProps.get("socketTimeout").toString()) : '\uea60';
    }

    public void setSlaveBorrowTimeout(int timeoutInMs) {
        this.slaveDriverProps.put("borrowTimeout", String.valueOf(timeoutInMs));
    }

    public int getSlaveBorrowTimeout() {
        return this.slaveDriverProps.get("borrowTimeout") != null ? Integer.valueOf(this.slaveDriverProps.get("borrowTimeout").toString()) : 2000;
    }

    public void setSlaveConnectTimeout(int timeoutInMs) {
        this.slaveDriverProps.put("connectTimeout", String.valueOf(timeoutInMs));
    }

    public int getSlaveConnectTimeout() {
        return this.slaveDriverProps.get("connectTimeout") != null ? Integer.valueOf(this.slaveDriverProps.get("connectTimeout").toString()) : 30000;
    }

    public void setSlaveSocketTimeout(int timeoutInMs) {
        this.slaveDriverProps.put("socketTimeout", String.valueOf(timeoutInMs));
    }

    public int getSlaveSocketTimeout() {
        return this.slaveDriverProps.get("socketTimeout") != null ? Integer.valueOf(this.slaveDriverProps.get("socketTimeout").toString()) : '\uea60';
    }

    public String getMasterUser() {
        return this.masterDriverProps.getProperty("user");
    }

    public String getMasterPassword() {
        return this.masterDriverProps.getProperty("password");
    }

    public String getMasterUrl() {
        return this.masterDriverProps.getProperty("url");
    }

    public void setSlaveUser(String user) {
        this.slaveDriverProps.put("user", user);
    }

    public void setSlavePassword(String password) {
        this.slaveDriverProps.put("password", password);
    }

    public void setSlaveUrl(String url) {
        this.slaveDriverProps.put("url", url);
    }

    public String getSlaveUser() {
        return this.slaveDriverProps.getProperty("user");
    }

    public String getSlavePassword() {
        return this.slaveDriverProps.getProperty("password");
    }

    public String getSlaveUrl() {
        return this.slaveDriverProps.getProperty("url");
    }

    public MetricRegistry getMetricRegistry() {
        return this.metricRegistry;
    }

    public void setMetricRegistry(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJdbcDataSourceClass() {
        return this.jdbcDataSourceClass;
    }

    public void setJdbcDataSourceClass(String jdbcDataSourceClass) {
        this.jdbcDataSourceClass = jdbcDataSourceClass;
    }

    public String getJdbcDataSourceType() {
        return this.jdbcDataSourceType;
    }

    public void setJdbcDataSourceType(String jdbcDataSourceType) {
        this.jdbcDataSourceType = jdbcDataSourceType;
    }

    public Connection getSlaveConnection() throws SQLException {
        long start = System.currentTimeMillis();

        Connection var4;
        try {
            Connection temp = this.slaveDataSource.getConnection();
            temp.setReadOnly(true);
            var4 = temp;
        } finally {
            this.countTime(MasterSlavePooledDataSource.StatsEnum.SLAVE_CONNECTION, System.currentTimeMillis() - start);
        }

        return var4;
    }

    public Connection getMasterConnection() throws SQLException {
        long start = System.currentTimeMillis();

        Connection var3;
        try {
            var3 = this.masterDataSource.getConnection();
        } finally {
            this.countTime(MasterSlavePooledDataSource.StatsEnum.MASTER_CONNECTION, System.currentTimeMillis() - start);
        }

        return var3;
    }

    protected void countTime(MasterSlavePooledDataSource.StatsEnum statsEnum, long elapsed) {
    }

    protected void registerSql(String sql, long elapsed) {
        if (elapsed > this.slowStatementThreshold) {
            log.info("Slow query (" + elapsed + " ms): " + sql);
        }

    }

    public boolean isInstrumentPreparedStatements() {
        return this.instrumentPreparedStatements;
    }

    public void setInstrumentPreparedStatements(boolean instrumentPreparedStatements) {
        this.instrumentPreparedStatements = instrumentPreparedStatements;
    }

    public void setSupportFullUtf8(boolean supportFullUtf8) {
        this.supportFullUtf8 = supportFullUtf8;
    }

    public boolean isSupportFullUtf8() {
        return this.supportFullUtf8;
    }

    public static enum StatsEnum {
        SLAVE_CONNECTION,
        MASTER_CONNECTION,
        PREPARE_CALL,
        NATIVE_SQL,
        ROLLBACK,
        COMMIT,
        PREPARE_STATEMENT;

        private StatsEnum() {
        }
    }
}
