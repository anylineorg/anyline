package org.anyline.data.transaction.init;

import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.TransactionState;

import javax.sql.DataSource;
import java.sql.Savepoint;

public class DefaultTransactionManage implements TransactionManage {
    private DataSource datasource;
    public DefaultTransactionManage(){}
    public DefaultTransactionManage(DataSource datasource){
        this.datasource = datasource;
    }

    public DataSource getDatasource() {
        return datasource;
    }

    public void setDatasource(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public void setAutoCommit(boolean auto) {
        
    }

    @Override
    public boolean getAutoCommit() {
        return false;
    }

    @Override
    public void setTransactionIsolation(int level) {

    }

    @Override
    public int getTransactionIsolation() {
        return 0;
    }

    @Override
    public Savepoint setSavepoint() {
        return null;
    }

    @Override
    public Savepoint setSavepoint(String name) {
        return null;
    }

    @Override
    public void releaseSavepoint(Savepoint savepoint) {

    }

    @Override
    public TransactionState start(TransactionDefine define) {
        return null;
    }

    @Override
    public void commit(TransactionState state) {

    }

    @Override
    public void rollback(TransactionState state) {

    }
}
