package org.anyline.dao.init.springjdbc;

import org.anyline.data.jdbc.ds.JDBCRuntime;
import org.anyline.data.jdbc.ds.RuntimeHolder;

public class FixDao<E> extends DefaultDao<E>{

    private String datasource;

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    protected JDBCRuntime runtime(){
        if(null != runtime){
            return runtime;
        }
        runtime = RuntimeHolder.getRuntime(datasource);
        return runtime;
    }
}
