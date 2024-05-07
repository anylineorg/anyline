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

import org.anyline.data.transaction.TransactionDefine;
import org.anyline.data.transaction.TransactionState;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Savepoint;

public class DefaultTransactionState implements TransactionState {
    private TransactionDefine.MODE mode = TransactionDefine.MODE.THREAD;
    private String name;
    private DataSource datasource;
    private Connection connection;
    private Savepoint point;
    private Object origin;
    private boolean isNewTransaction = false;

    @Override
    public Savepoint getPoint() {
        return point;
    }

    @Override
    public void setPoint(Savepoint point) {
        this.point = point;
    }

    @Override
    public Object getOrigin() {
        return origin;
    }

    @Override
    public void setOrigin(Object origin) {
        this.origin = origin;
    }

    @Override
    public boolean hasSavepoint() {
        return null != point;
    }

    @Override
    public boolean isNewTransaction() {
        return isNewTransaction;
    }

    @Override
    public void setRollbackOnly() {

    }

    @Override
    public boolean isRollbackOnly() {
        return false;
    }

    @Override
    public boolean isCompleted() {
        return false;
    }

    @Override
    public Object createSavepoint() throws Exception {
        return null;
    }

    @Override
    public void rollbackToSavepoint(Object savepoint) throws Exception {

    }

    @Override
    public void releaseSavepoint(Object savepoint) throws Exception {

    }

    @Override
    public void setDataSource(DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    public DataSource getDataSource() {
        return this.datasource;
    }

    @Override
    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public TransactionDefine.MODE getMode() {
        return mode;
    }

    public void setMode(TransactionDefine.MODE mode) {
        this.mode = mode;
    }
}
