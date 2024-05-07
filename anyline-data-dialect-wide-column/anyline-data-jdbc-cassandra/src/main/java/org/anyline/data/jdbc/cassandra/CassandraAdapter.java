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



package org.anyline.data.jdbc.cassandra;

import org.anyline.annotation.Component;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.AbstractJDBCAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.DMListener;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataSet;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Database;
import org.anyline.metadata.Schema;
import org.anyline.metadata.type.DatabaseType;

import java.util.LinkedHashMap;
import java.util.List;


@Component("anyline.data.jdbc.adapter.cassandra")
public class CassandraAdapter extends AbstractJDBCAdapter implements JDBCAdapter {
    
    public DatabaseType type(){
        return DatabaseType.Cassandra;
    }

    @Override
    public void setListener(DDListener listener) {

    }

    @Override
    public void setListener(DMListener listener) {

    }

    public CassandraAdapter(){
        delimiterFr = "";
        delimiterTo = "";
    }

    private String delimiter;

    /* *****************************************************************************************************
     *
     * 											DML
     *
     * ****************************************************************************************************/
    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run){
        String sql = run.getBaseQuery();
        String cols = run.getQueryColumn();
        if(!"*".equals(cols)){
            String reg = "(?i)^select[\\s\\S]+from";
            sql = sql.replaceAll(reg, "SELECT "+cols+" FROM ");
        }
        OrderStore orders = run.getOrderStore();
        if(null != orders){
            sql += orders.getRunText(getDelimiterFr()+getDelimiterTo());
        }
        PageNavi navi = run.getPageNavi();
        if(null != navi){
            long limit = navi.getLastRow() - navi.getFirstRow() + 1;
            if(limit < 0){
                limit = 0;
            }
            sql += " LIMIT " + navi.getFirstRow() + "," + limit;
        }
        sql = compressCondition(runtime, sql);
        return sql;
    }

    @Override
    public List<Run> buildQueryDatabaseRun(DataRuntime runtime, boolean greedy) throws Exception {
        return null;
    }

    @Override
    public LinkedHashMap<String, Database> databases(DataRuntime runtime, int index, boolean create, LinkedHashMap<String, Database> databases, Catalog catalog, Schema schema, DataSet set) throws Exception {
        return null;
    }

    public String concat(DataRuntime runtime, String ... args){
        return concatFun(runtime, args);
    }

}
