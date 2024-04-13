package org.anyline.data.transaction.init;

import org.anyline.data.datasource.ApplicationConnectionHolder;
import org.anyline.data.datasource.ThreadConnectionHolder;
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
        Connection con = null;
        String name = define.getName();
        TransactionDefine.MODE mode = define.getMode();
        boolean isNew = false;
        //TODO 检测现有事务 与 point
        if(TransactionDefine.MODE.THREAD == mode) {
            //线程内事务
            con = ThreadConnectionHolder.get(datasource);
            if(null == con || con.isClosed()) {
                con = datasource.getConnection();
                isNew = true;
                ThreadConnectionHolder.set(datasource, con);
            }
        }else if(TransactionDefine.MODE.APPLICATION == mode){
            //应用内事务
            con = ApplicationConnectionHolder.get(datasource, name);
            if(null == con) {
                con = datasource.getConnection();
                isNew = true;
                ApplicationConnectionHolder.set(datasource, name, con);
            }
            //放到线程中 获取连接时统一从线程中获取
            ThreadConnectionHolder.set(datasource, con);
        }
        TransactionState state = new DefaultTransactionState();
        state.setConnection(con);
        state.setDataSource(datasource);
        state.setName(name);
        state.setMode(mode);
        if(isNew) {
            con.setAutoCommit(false);
        }
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
        String name = state.getName();
        TransactionDefine.MODE mode = state.getMode();
        if(TransactionDefine.MODE.THREAD == mode){
            ThreadConnectionHolder.remove(ds);
        }else if(TransactionDefine.MODE.APPLICATION == mode){
            ApplicationConnectionHolder.remove(ds, name);
        }
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
        String name = state.getName();
        TransactionDefine.MODE mode = state.getMode();
        if(TransactionDefine.MODE.THREAD == mode){
            ThreadConnectionHolder.remove(ds);
        }else if(TransactionDefine.MODE.APPLICATION == mode){
            ApplicationConnectionHolder.remove(ds, name);
        }
        TransactionManage.records.remove(state);
    }
}
