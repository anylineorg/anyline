package org.anyline.data.transaction;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Savepoint;

public interface TransactionState {
    Object getOrigin();
    void setOrigin(Object origin);
    boolean hasSavepoint();
    Savepoint getPoint();
    void setPoint(Savepoint point);
    boolean isNewTransaction();
    void setRollbackOnly();
    boolean isRollbackOnly();
    boolean isCompleted();
    Object createSavepoint() throws Exception;
    void rollbackToSavepoint(Object savepoint) throws Exception;
    void releaseSavepoint(Object savepoint) throws Exception;
    void setDataSource(DataSource datasource);
    DataSource getDataSource();
    void setConnection(Connection connection);
    Connection getConnection();
}
