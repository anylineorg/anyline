package org.anyline.data.jdbc.ds;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.util.SQLAdapterUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Hashtable;
import java.util.Map;

public class RuntimeHolder {
    public static Logger log = LoggerFactory.getLogger(RuntimeHolder.class);
    private static Map<String, JDBCRuntime> runtimes = new Hashtable();

    public static void reg(String datasource, JdbcTemplate template, JDBCAdapter adapter){
        log.warn("[create jdbc runtime][key:{}]", datasource);
        JDBCRuntime runtime = new JDBCRuntime(datasource, template, adapter);
        runtimes.put(datasource, runtime);
    }
    public static JDBCRuntime getRuntime(){
        return getRuntime(DataSourceHolder.getDataSource());
    }
    public static JDBCRuntime getRuntime(String datasource){
        if(null == datasource){
            return getRuntime("default");
        }
        JDBCRuntime runtime = runtimes.get(datasource);
        if(null != runtime){
            if(null == runtime.getAdapter()){
                runtime.setAdapter(SQLAdapterUtil.getAdapter(runtime.getTemplate()));
            }
        }else{
            throw new RuntimeException("未注册数据源:"+datasource);
        }
        return runtime;
    }
}
