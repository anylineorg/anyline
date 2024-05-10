/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.data.transaction.init;

import org.anyline.data.datasource.ApplicationConnectionHolder;
import org.anyline.data.datasource.ThreadConnectionHolder;
import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionManage;
import org.anyline.data.transaction.TransactionState;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
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
    public TransactionState start(TransactionDefine define)  throws SQLException {
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
                log.info("[开启跨线程事务][name:{}]", name);
            }else{
                log.info("[加入跨线程事务][name:{}]", name);
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
    public void commit(TransactionState state) throws SQLException {
        DataSource ds = state.getDataSource();
        Connection con = state.getConnection();
        con.commit();
        con.setAutoCommit(true);
        con.close();
        String name = state.getName();
        log.info("[提交事务][name:{}]", name);
        TransactionDefine.MODE mode = state.getMode();
        if(TransactionDefine.MODE.THREAD == mode){
            ThreadConnectionHolder.remove(ds);
        }else if(TransactionDefine.MODE.APPLICATION == mode){
            ApplicationConnectionHolder.remove(ds, name);
        }
        TransactionManage.records.remove(state);
    }

    @Override
    public void rollback(TransactionState state) throws SQLException {
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
        log.info("[回滚事务][name:{}]", name);
        TransactionDefine.MODE mode = state.getMode();
        if(TransactionDefine.MODE.THREAD == mode){
            ThreadConnectionHolder.remove(ds);
        }else if(TransactionDefine.MODE.APPLICATION == mode){
            ApplicationConnectionHolder.remove(ds, name);
        }
        TransactionManage.records.remove(state);
    }
}
