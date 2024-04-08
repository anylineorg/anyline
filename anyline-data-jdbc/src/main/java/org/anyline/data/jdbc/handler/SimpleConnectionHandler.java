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
    public SimpleConnectionHandler(){

    }
    public SimpleConnectionHandler(DataSource datasource, Connection connection, Statement statement, ResultSet result){
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
        if(null != result && !result.isClosed()){
            result.close();
        }
        if(null != statement && !statement.isClosed()){
            statement.close();
        }
        if(null != connection){
            worker.releaseConnection(null, null, connection, datasource);
        }
        return false;
    }
}
