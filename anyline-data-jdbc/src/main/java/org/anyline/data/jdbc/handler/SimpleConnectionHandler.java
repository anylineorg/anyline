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



package org.anyline.data.jdbc.handler;

import org.anyline.data.adapter.DriverWorker;
import org.anyline.data.handler.ConnectionHandler;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class SimpleConnectionHandler implements ConnectionHandler {
    private DriverWorker worker;

    public DriverWorker getWorker() {
        return worker;
    }

    public void setWorker(DriverWorker worker) {
        this.worker = worker;
    }

    private DataSource datasource;
    private Connection connection;
    private Statement statement;
    private ResultSet result;
    public SimpleConnectionHandler() {

    }
    public SimpleConnectionHandler(DataSource datasource, Connection connection, Statement statement, ResultSet result) {
        this.datasource = datasource;
        this.connection = connection;
        this.statement = statement;
        this.result = result;
    }

    public DataSource getDataSource() {
        return datasource;
    }

    public void setDataSource(DataSource datasource) {
        this.datasource = datasource;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Statement getStatement() {
        return statement;
    }

    public void setStatement(Statement statement) {
        this.statement = statement;
    }

    public ResultSet getResult() {
        return result;
    }

    public void setResult(ResultSet result) {
        this.result = result;
    }

    @Override
    public boolean close()  throws Exception{
        if(null != result && !result.isClosed()) {
            result.close();
        }
        if(null != statement && !statement.isClosed()) {
            statement.close();
        }
        if(null != connection) {
            worker.releaseConnection(null, null, connection, datasource);
        }
        return false;
    }
}
