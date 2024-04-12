package org.anyline.data.transaction.init;

import org.anyline.data.datasource.ConnectionHolder;
import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.TransactionState;

import javax.sql.DataSource;
import java.sql.Connection;
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
    public TransactionState start(TransactionDefine define)  throws Exception{
        //TODO 检测现有事务 与 point
        Connection con = ConnectionHolder.get(datasource);
        if(null == con) {
            con = datasource.getConnection();
        }
        TransactionState state = new DefaultTransactionState();
        state.setConnection(con);
        state.setDataSource(datasource);
        con.setAutoCommit(false);
        ConnectionHolder.set(datasource, con);
        TransactionManage.records.put(state, this);
        return state;
    }

    @Override
    public void commit(TransactionState state) throws Exception{
        DataSource ds = state.getDataSource();
        Connection con = state.getConnection();
        con.commit();
        con.setAutoCommit(true);
        con.close();
        ConnectionHolder.remove(ds);
        TransactionManage.records.remove(state);
    }

    @Override
    public void rollback(TransactionState state) throws Exception{
        DataSource ds = state.getDataSource();
        Connection con = state.getConnection();
        Savepoint point = state.getPoint();
        if(null != point) {
            con.rollback(point);
        }else{
            con.rollback();
        }
        con.setAutoCommit(true);
        con.close();
        ConnectionHolder.remove(ds);
        TransactionManage.records.remove(state);
    }
}
