package org.anyline.data.jdbc.cassandra;


import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.adapter.init.SQLAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.type.DatabaseType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

@Repository("anyline.data.jdbc.adapter.cassandra")
public class CassandraAdapter extends SQLAdapter implements JDBCAdapter, InitializingBean {
    
    public DatabaseType type(){
        return DatabaseType.Cassandra;
    }

    public CassandraAdapter(){
        delimiterFr = "";
        delimiterTo = "";
    }

    @Value("${anyline.data.jdbc.delimiter.cassandra:}")
    private String delimiter;

    @Override
    public void afterPropertiesSet()  {
        setDelimiter(delimiter);
    }

    /* *****************************************************************************************************
     *
     * 											DML
     *
     * ****************************************************************************************************/
    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run){
        String sql = run.getBaseQuery();
        String cols = run.getQueryColumns();
        if(!"*".equals(cols)){
            String reg = "(?i)^select[\\s\\S]+from";
            sql = sql.replaceAll(reg,"SELECT "+cols+" FROM ");
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
        sql = sql.replaceAll("WHERE\\s*1=1\\s*AND", "WHERE");
        return sql;
    }

    public String concat(DataRuntime runtime, String ... args){
        return concatFun(args);
    }

}
