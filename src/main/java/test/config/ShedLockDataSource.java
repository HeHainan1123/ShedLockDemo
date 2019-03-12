package test.config;

import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Wrapping with a decorator so that Shedlock can use data source from box persistence
 * and always work on master connection.
 */
@Component
public class ShedLockDataSource extends MasterSlavePooledDataSource {

    private final DataSource dataSource;

    public ShedLockDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (this.dataSource instanceof MasterSlavePooledDataSource) {
            return ((MasterSlavePooledDataSource) this.dataSource).getMasterConnection();
        } else {
            return this.dataSource.getConnection();
        }
    }
}
