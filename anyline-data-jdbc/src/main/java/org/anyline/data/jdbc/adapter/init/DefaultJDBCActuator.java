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

package org.anyline.data.jdbc.adapter.init;

import org.anyline.adapter.EntityAdapter;
import org.anyline.annotation.Component;
import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.adapter.DriverActuator;
import org.anyline.data.datasource.ApplicationConnectionHolder;
import org.anyline.data.datasource.ThreadConnectionHolder;
import org.anyline.data.handler.ConnectionHandler;
import org.anyline.data.handler.DataHandler;
import org.anyline.data.handler.ResultSetHandler;
import org.anyline.data.handler.StreamHandler;
import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.jdbc.handler.SimpleConnectionHandler;
import org.anyline.data.jdbc.util.JDBCUtil;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.util.*;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.*;

@Component("anyline.environment.data.driver.actuator.jdbc")
public class DefaultJDBCActuator implements DriverActuator {
    private Log log = LogProxy.get(DefaultJDBCActuator.class);

    /**
     * 根据类型注入到DriverAdapter中
     * @return Class
     */
    @Override
    public Class<? extends DriverAdapter> supportAdapterType() {
        return JDBCAdapter.class;
    }

    @Override
    public DataSource getDataSource(DriverAdapter adapter, DataRuntime runtime) {
        DataSource datasource = datasource(runtime);
        return datasource;
    }

    @Override
    public Connection getConnection(DriverAdapter adapter, DataRuntime runtime, DataSource datasource) {
        Connection con = null;
        try{
            //当前线程中已经开启了事务的 用事务连接
            con = ThreadConnectionHolder.get(datasource);
            if(null == con) {
                con = datasource.getConnection();
            }
        }catch (Exception e) {
            log.error("连接 异常:", e);
        }
        return con;
    }

    @Override
    public void releaseConnection(DriverAdapter adapter, DataRuntime runtime, Connection connection, DataSource datasource) {
        try {
            if(ThreadConnectionHolder.contains(datasource, connection)
            || ApplicationConnectionHolder.contains(datasource, connection)) {
                //有事务不要关闭，在事务管理器中关闭
                return;
            }
            connection.close();
        } catch (SQLException e) {
            log.error("释放连接 异常:", e);
        }
    }

    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, DataSource ds, T meta) {
        if(null == meta || null != meta.getCheckSchemaTime()) {
            return;
        }
        String catalog = meta.getCatalogName();
        if(null== catalog) {
            catalog = runtime.getCatalog();
        }
        String schema = meta.getSchemaName();
        if(null == schema) {
            schema = runtime.getSchema();
        }

        if(null == catalog && null == schema) {
            Connection con = null;
            try {
                if (adapter.empty(meta.getCatalog()) || adapter.empty(meta.getSchema())) {
                    con = getConnection(adapter, runtime, ds);
                    checkSchema(adapter, runtime, con, meta);
                }
            } catch (Exception e) {
                log.warn("[check schema][fail:{}]", e.toString());
            } finally {
                releaseConnection(adapter, runtime, con, ds);
            }
        }else{
            meta.setCatalog(catalog);
            meta.setSchema(schema);
        }
    }

    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, T meta) {
        if(null != meta) {
            String catalog = meta.getCatalogName();
            if(null == catalog) {
                catalog = runtime.getCatalog();
            }
            String schema = meta.getSchemaName();
            if(null == schema) {
                schema = runtime.getSchema();
            }

            if(null == catalog && null == schema) {
                checkSchema(adapter, runtime, datasource(runtime), meta);
            }else{
                meta.setCatalog(catalog);
                meta.setSchema(schema);
            }
        }
    }

    @Override
    public <T extends Metadata> void checkSchema(DriverAdapter adapter, DataRuntime runtime, Connection con, T meta) {
        if(null == meta) {
            return;
        }
        String catalog = meta.getCatalogName();
        if(null== catalog) {
            catalog = runtime.getCatalog();
        }
        String schema = meta.getSchemaName();
        if(null == schema) {
            schema = runtime.getSchema();
        }

        if(null == catalog && null == schema) {
            try {
                //这一步 不要 检测是否支持catalog/schema, 因为这一步返回结果有可能是颠倒的 到correctSchemaFromJDBC中再检测
                if (adapter.empty(meta.getCatalog())) {
                    catalog = con.getCatalog();
                }
            } catch (Exception e) {
                log.warn("[check catalog][result:fail][exception:{}]", e.toString());
            }
            try {
                if (adapter.empty(meta.getSchema())) {
                    schema = con.getSchema();
                }
            } catch (Exception e) {
                log.warn("[check schema][result:fail][exception:{}]", e.toString());
            }
            adapter.correctSchemaFromJDBC(runtime, meta, catalog, schema, true, true);
            meta.setCheckSchemaTime(new Date());
        }else{
            meta.setCatalog(catalog);
            meta.setSchema(schema);
        }
    }

    private DataSource datasource(DataRuntime runtime) {
        return (DataSource) runtime.getProcessor();
    }

    @Override
    public DataSet select(DriverAdapter adapter, DataRuntime runtime, String random, boolean system, ACTION.DML action, Table table, ConfigStore configs, Run run, String sql, List<Object> values, LinkedHashMap<String,Column> columns) throws Exception {
        DataSet set = new DataSet();
        long fr = System.currentTimeMillis();
        final DataRuntime rt = runtime;
        final long[] mid = {System.currentTimeMillis()};
        final boolean[] process = {false};
        final LinkedHashMap<String, Column> metadatas = new LinkedHashMap<>();
        if(null != columns) {
            metadatas.putAll(columns);
        }
        set.setMetadata(metadatas);
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return set;
        }

        StreamHandler _handler = null;
        if(null != configs) {
            DataHandler handler = configs.handler();
            if(handler instanceof StreamHandler) {
                _handler = (StreamHandler) handler;
            }
        }
        final StreamHandler handler = _handler;
        Connection con = getConnection(adapter, runtime, datasource);
        PreparedStatement ps = null;
        ResultSet rs = null;
        long[] count = new long[]{0};

        boolean keep = false;
        //read(ResultSet result)之后 是否保存ResultSet连接状态，如果保持则需要在调用方关闭
        try {
            ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            if(null != handler) {
                keep = handler.keep();
                ps.setFetchSize(handler.size());
            }
            ps.setFetchDirection(ResultSet.FETCH_FORWARD);
            JDBCUtil.queryTimeout(ps, configs);
            if (null != values && !values.isEmpty()) {
                int idx = 0;
                for (Object value : values) {
                    ps.setObject(++idx, value);
                }
            }
            rs = ps.executeQuery();
            if(null != handler) {
                if(keep && handler instanceof ResultSetHandler) {
                    ConnectionHandler ch = new SimpleConnectionHandler(datasource, con, ps, rs);
                    ch.setActuator(this);
                    handler.handler(ch);
                    ((ResultSetHandler)handler).read(rs);
                }else {
                    while (rs.next()) {
                        count[0] ++;
                        boolean next = JDBCUtil.stream(adapter, handler, rs, configs, true, runtime, null);
                        if(!next) {
                            break;
                        }
                    }
                }
            }else{
                while (rs.next()) {
                    count[0] ++;
                    DataRow row = JDBCUtil.row(adapter, system, rt, metadatas, configs, rs);
                    set.add(row);
                }
            }
        }finally {
            if(!keep) {//保持连接的由调用方关闭
                if(null != rs && !rs.isClosed()) {
                    rs.close();
                }
                if(null != ps && !ps.isClosed()) {
                    ps.close();
                }
                releaseConnection(adapter, runtime, con, datasource);
            }
        }
        //end stream handler

        if(!process[0]) {
            mid[0] = System.currentTimeMillis();
        }
        configs.setLastPackageTime(System.currentTimeMillis() - mid[0]);
        configs.setLastExecuteTime(mid[0] - fr);
        return set;
    }

    /**
     * query procedure [调用入口]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param procedure 存储过程
     * @param navi 分页
     * @return DataSet
     */
    @Override
    public DataSet querys(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, PageNavi navi) throws Exception {
        final List<Parameter> inputs = procedure.getInputs();
        final List<Parameter> outputs = procedure.getOutputs();
        final String rdm = random;
        final DataRuntime rt = runtime;
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return new DataSet();
        }
        Connection conn = getConnection(adapter, runtime, datasource);
        String sql = "{call " +procedure.getName()+"(";
        final int sizeIn = inputs.size();
        final int sizeOut = outputs.size();
        final int size = sizeIn + sizeOut;
        for(int i=0; i<size; i++) {
            sql += "?";
            if(i < size-1) {
                sql += ",";
            }
        }
        sql += ")}";

        CallableStatement cs = conn.prepareCall(sql);
        for(int i=1; i<=sizeIn; i++) {
            Parameter param = inputs.get(i-1);
            Object value = param.getValue();
            if(null == value || "NULL".equalsIgnoreCase(value.toString())) {
                value = null;
            }
            cs.setObject(i, value, param.getType());
        }
        for(int i=1; i<=sizeOut; i++) {
            Parameter param = outputs.get(i-1);
            if(null == param.getValue()) {
                cs.registerOutParameter(i+sizeIn, param.getType());
            }else{
                cs.setObject(i, param.getValue(), param.getType());
            }

        }
        JDBCUtil.queryTimeout(cs, null);

        ResultSet rs = cs.executeQuery();
        DataSet set = new DataSet();
        ResultSetMetaData rsmd = rs.getMetaData();
        int cols = rsmd.getColumnCount();
        for(int i=1; i<=cols; i++) {
            String name = rsmd.getColumnLabel(i);
            if(null == name) {
                name = rsmd.getColumnName(i);
            }
            set.addHead(name);
        }
        long mid = System.currentTimeMillis();
        int index = 0;
        long first = -1;
        long last = -1;
        if(null != navi) {
            first = navi.getFirstRow();
            last = navi.getLastRow();
        }
        while(rs.next()) {
            if(first ==-1 || (index >= first && index <= last)) {
                DataRow row = new DataRow();
                for(int i=1; i<=cols; i++) {
                    row.put(false, rsmd.getColumnLabel(i), rs.getObject(i));
                }
                set.addRow(row);
            }
            index ++;
            if(first != -1) {
                if(index > last) {
                    break;
                }
                if(first ==0 && last==0) {// 只取一行
                    break;
                }
            }
        }
        if(null != navi) {
            navi.setTotalRow(index);
            set.setNavi(navi);
            navi.setDataSize(set.size());
        }

        set.setDatalink(rt.datasource());
        if(ConfigTable.IS_LOG_SQL_TIME && log.isInfoEnabled()) {
            log.info("{}[封装耗时:{}][封装行数:{}]", rdm, DateUtil.format(System.currentTimeMillis() - mid), set.size());
        }
        if(ConfigTable.IS_LOG_QUERY_RESULT && log.isInfoEnabled()) {
            log.info("{}[查询结果]{}", random, LogUtil.table(set));
        }
        return set;
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return maps
     */
    @Override
    public List<Map<String, Object>> maps(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        List<Map<String, Object>> maps = new ArrayList<>();
        String sql = run.getFinalQuery();
        List<Object> values = run.getValues();
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return maps;
        }
        StreamHandler _handler = null;
        if(null != configs) {
            DataHandler handler = configs.handler();
            if(handler instanceof StreamHandler) {
                _handler = (StreamHandler) handler;
            }
        }
        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long[] count = new long[]{0};
        final boolean[] process = {false};
        final StreamHandler handler = _handler;
        long fr = System.currentTimeMillis();
        final long[] mid = {System.currentTimeMillis()};
        boolean keep = false;
        if(null != handler) {
            //read(ResultSet result)之后 是否保存ResultSet连接状态，如果保持则需要在调用方关闭
            keep = handler.keep();
        }
            try {
                con = getConnection(adapter, runtime, datasource);
                ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
                if(null != handler) {
                    ps.setFetchSize(handler.size());
                }
                ps.setFetchDirection(ResultSet.FETCH_FORWARD);
                JDBCUtil.queryTimeout(ps, configs);
                if (null != values && !values.isEmpty()) {
                    int idx = 0;
                    for (Object value : values) {
                        ps.setObject(++idx, value);
                    }
                }
                rs = ps.executeQuery();
                if(null != handler) {
                    if (keep && handler instanceof ResultSetHandler) {
                        ConnectionHandler ch = new SimpleConnectionHandler(datasource, con, ps, rs);
                        ch.setActuator(this);
                        handler.handler(ch);
                        ((ResultSetHandler) handler).read(rs);
                    } else {
                        while (rs.next()) {
                            count[0]++;
                            boolean next = JDBCUtil.stream(adapter, handler, rs, configs, true, runtime, null);
                            if (!next) {
                                break;
                            }
                        }
                    }
                }else{
                    while (rs.next()) {
                        count[0]++;
                        LinkedHashMap<String, Object> map = JDBCUtil.map(adapter, false, runtime, null, configs, rs);
                        maps.add(map);
                    }
                }
            }finally {
                if(!keep) {//保持连接的由调用方关闭
                    if(null != rs && !rs.isClosed()) {
                        rs.close();
                    }
                    releaseConnection(adapter, runtime, con, datasource);
                }
            }

            mid[0] = System.currentTimeMillis();
            count[0] = maps.size();

        boolean slow = false;
        if(ConfigStore.SLOW_SQL_MILLIS(configs) > 0) {
            if(mid[0]-fr > ConfigStore.SLOW_SQL_MILLIS(configs)) {
                slow = true;
                log.warn("{}[slow cmd][action:select][执行耗时:{}]{}", random, DateUtil.format(mid[0]-fr), run.log(ACTION.DML.SELECT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
                if(null != adapter.getDMListener()) {
                    adapter.getDMListener().slow(runtime, random, ACTION.DML.SELECT, null, sql, values, null, true, maps, mid[0]-fr);
                }
            }
        }
        if(!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
            log.info("{}[action:select][执行耗时:{}]", random, DateUtil.format(mid[0] - fr));
        }
        if(!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
            log.info("{}[action:select][封装耗时:{}][封装行数:{}]", random, DateUtil.format(System.currentTimeMillis() - mid[0]), count[0]);
        }
        return maps;
    }

    /**
     * select [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return map
     */
    @Override
    public Map<String, Object> map(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        Map<String, Object> map = null;
        String sql = run.getFinalExists();
        List<Object> values = run.getValues();
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return new HashMap<>();
        }

        Connection con = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        long fr = System.currentTimeMillis();

        try {
            con = getConnection(adapter, runtime, datasource);
            ps = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            ps.setFetchDirection(ResultSet.FETCH_FORWARD);
            JDBCUtil.queryTimeout(ps, configs);
            if (null != values && !values.isEmpty()) {
                int idx = 0;
                for (Object value : values) {
                    ps.setObject(++idx, value);
                }
            }
            rs = ps.executeQuery();

                while (rs.next()) {
                    map = JDBCUtil.map(adapter, false, runtime, null, configs, rs);
                    break;
                }

        }finally {
            if(null != rs && !rs.isClosed()) {
                rs.close();
            }
            if(null != ps && !ps.isClosed()) {
                ps.close();
            }
            releaseConnection(adapter, runtime, con, datasource);

        }

        long time = System.currentTimeMillis() - fr;
        fr = System.currentTimeMillis();
        boolean slow = false;
        if(ConfigStore.SLOW_SQL_MILLIS(configs) > 0) {
            if(time > ConfigStore.SLOW_SQL_MILLIS(configs)) {
                slow = true;
                log.warn("{}[slow cmd][action:select][执行耗时:{}]{}", random, DateUtil.format(time), run.log(ACTION.DML.SELECT,ConfigStore.IS_SQL_LOG_PLACEHOLDER(configs)));
                if(null != adapter.getDMListener()) {
                    adapter.getDMListener().slow(runtime, random, ACTION.DML.SELECT, null, sql, values, null, true, map, time);
                }
            }
        }
        if(!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
            log.info("{}[action:select][执行耗时:{}]", random, DateUtil.format(time));
        }
        if(!slow && log.isInfoEnabled() &&ConfigStore.IS_LOG_SQL_TIME(configs)) {
            log.info("{}[action:select][封装耗时:{}][封装行数:{}]", random, DateUtil.format(System.currentTimeMillis() - fr), 1);
        }
        return map;
    }

    @Override
    public long insert(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, Run run, String generatedKey, String[] pks) throws Exception {
        long cnt = -1;
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return -1;
        }
        Connection con = getConnection(adapter, runtime, datasource);
        String cmd = run.getFinalInsert();
        int batch = run.getBatch();
        List<Object> values = run.getValues();
        if(batch > 1) {
            cnt = batch(adapter, runtime, datasource, cmd, batch, run.getVol(), values);
        }else {
            boolean keyHolder = adapter.supportKeyHolder(runtime, configs);

            if(data instanceof RunPrepare) {
                // insert into a select * from b
                keyHolder = false;
            }
            PreparedStatement ps = null;
            //是否支持返回自增值
            if(keyHolder) {
                //需要返回自增
                if (null != pks && pks.length > 0) {
                    //返回多个值
                    ps = con.prepareStatement(cmd, pks);
                } else {
                    ps = con.prepareStatement(cmd, Statement.RETURN_GENERATED_KEYS);
                }

            }else{
                ps = con.prepareStatement(cmd);
            }

            int idx = 0;
            if (null != values) {
                for (Object obj : values) {
                    ps.setObject(++idx, obj);
                }
            }
            JDBCUtil.updateTimeout(ps, configs);
            cnt = ps.executeUpdate();
            if(keyHolder) {
                ResultSet rs = ps.getGeneratedKeys();
                try {
                    identity(adapter, runtime, random, data, configs, rs, generatedKey);
                }finally {
                    rs.close();
                }
            }
            releaseConnection(adapter, runtime, con, datasource);
        }

        return cnt;
    }

    /**
     * insert[命令执行后]
     * insert执行后 通过KeyHolder获取主键值赋值给data
     * @param random log标记
     * @param data data
     * @param rs  ResultSet
     * @return boolean
     */
    public boolean identity(DriverAdapter adapter, DataRuntime runtime, String random, Object data, ConfigStore configs, ResultSet rs, String generatedKey) {
        try {
            if(null == rs) {
                return false;
            }
            if(!adapter.supportKeyHolder(runtime, configs)) {
                return false;
            }
            Map<String, Integer> ks = JDBCUtil.keys(rs);
            List<Map<String,Object>> pvs = new ArrayList<>();
            while (rs.next()) {
                Map<String, Object> pv = new HashMap<>();
                for(String k:ks.keySet()) {
                    Integer idx = ks.get(k);
                    pv.put(k, rs.getObject(idx));
                }
                pvs.add(pv);
            }
            //如果没有generatedKey取结果集中第一列 列名
            if(null == generatedKey && !pvs.isEmpty()) {
                Map<String,Object> key = pvs.get(0);
                generatedKey = key.keySet().iterator().next();
            }
            if(data instanceof Collection) {
                //批量插入
                List<Object> ids = new ArrayList<>();
                Collection list = (Collection) data;
                //检测是否有主键值
                for(Object item:list) {
                    if(BasicUtil.isNotEmpty(true, EntityAdapter.getPrimaryValue(item))) {
                        //已经有主键值了
                        return true;
                    }
                    break;
                }
                if(BasicUtil.isEmpty(generatedKey)) {
                    return false;
                }
                int i = 0;
                int data_size = list.size();
                if(list.size() == pvs.size()) {
                    //返回主键数量 插入数据 数量一致
                    for (Object item : list) {
                        Map<String, Object> key = pvs.get(i);
                        Object id = key.get(generatedKey);
                        ids.add(id);
                        EntityAdapter.setPrimaryValue(item, id);
                        i++;
                    }
                }else{
                    if(!pvs.isEmpty()) {
                        Object last = pvs.get(0).get(generatedKey);
                        if (last instanceof Number) {
                            Long num = BasicUtil.parseLong(last.toString(), null);
                            if (null != num) {
                                num = num - data_size + 1;
                                for (Object item : list) {
                                    EntityAdapter.setPrimaryValue(item, num++);
                                }
                            }
                        }
                    }
                }
                if(ConfigStore.IS_LOG_SQL(configs) && log.isWarnEnabled()) {
                    log.info("{}[exe insert][生成主键:{}]", random, ids);
                }
            }else{
                if(!pvs.isEmpty()) {
                    if(BasicUtil.isEmpty(true, EntityAdapter.getPrimaryValue(data))) {
                        Object id = pvs.get(0).get(generatedKey);
                        EntityAdapter.setPrimaryValue(data, id);
                        if (ConfigStore.IS_LOG_SQL(configs) && log.isWarnEnabled()) {
                            log.info("{}[exe insert][生成主键:{}]", random, id);
                        }
                    }
                }
            }
        }catch (Exception e) {
            if(ConfigStore.IS_PRINT_EXCEPTION_STACK_TRACE(configs)) {
                log.error("identity 异常:", e);
            }
            if(ConfigStore.IS_LOG_SQL_WHEN_ERROR(configs)) {
                log.warn("{}[exe insert][返回主键失败]", random);
            }
            return false;
        }
        return true;
    }
    public long batch(DriverAdapter adapter, DataRuntime runtime, DataSource datasource, String sql, int batch, int vol, List<Object> values) {
        int size = values.size(); //一共多少参数
        int line = size/vol; //一共多少行
        if(null == datasource) {
            return line;
        }
        Connection con = getConnection(adapter, runtime, datasource);
        PreparedStatement ps = null;
        //batch insert保持SQL一致,如果不一致应该调用save方法
        //返回每个SQL的影响行数
        try {
            con.setAutoCommit(false);
            ps = con.prepareStatement(sql);
            for (int r = 1; r <= line; r++) {
                for(int c=1; c<=vol; c++) {
                    ps.setObject(c, values.get(vol*r+c-1));
                }
                //1."攒"sql
                ps.addBatch();

                if (r % batch == 0) {
                    ps.executeBatch();
                    ps.clearBatch();
                }

            }
            con.commit();
        }catch (Exception e) {

        }finally {
            releaseConnection(adapter, runtime, con, datasource);
        }
        return line;
    }

    public long update(DriverAdapter adapter, DataRuntime runtime, String random, Table dest, Object data, ConfigStore configs, Run run) throws Exception {
        long result = 0;
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return -1;
        }
        String cmd = run.getFinalUpdate();
        List<Object> values = run.getValues();
        int batch = run.getBatch();

        if(batch > 1) {
            result = batch(adapter, runtime, datasource, cmd, batch, run.getVol(), values);
        }else {
            Connection con = getConnection(adapter, runtime, datasource);
            PreparedStatement ps = null;
            try {
                ps = con.prepareStatement(cmd);
                int idx = 0;
                if (null != values) {
                    for (Object obj : values) {
                        ps.setObject(++idx, obj);
                    }
                }
                result = ps.executeUpdate();
            }finally {
                releaseConnection(adapter, runtime, con, datasource);
            }
        }
        return result;
    }

    /**
     * procedure [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param procedure 存储过程
     * @param random  random
     * @return 输出参数
     */
    public List<Object> execute(DriverAdapter adapter, DataRuntime runtime, String random, Procedure procedure, String sql, List<Parameter> inputs, List<Parameter> outputs) throws Exception {
        List<Object> list = new ArrayList<>();
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return list;
        }
        Connection con = getConnection(adapter, runtime, datasource);
        final int sizeIn = inputs.size();
        final int sizeOut = outputs.size();
        final int size = sizeIn + sizeOut;
        CallableStatement cs = con.prepareCall(sql);
        // 带有返回参数
        int returnIndex = 0;
        if (procedure.hasReturn()) {
            returnIndex = 1;
            cs.registerOutParameter(1, Types.VARCHAR);
        }
        for (int i = 1; i <= sizeIn; i++) {
            Parameter param = inputs.get(i - 1);
            Object value = param.getValue();
            if (null == value || "NULL".equalsIgnoreCase(value.toString())) {
                value = null;
            }
            cs.setObject(i + returnIndex, value, param.getType());
        }
        for (int i = 1; i <= sizeOut; i++) {
            Parameter param = outputs.get(i - 1);
            if (null == param.getValue()) {
                cs.registerOutParameter(i + sizeIn + returnIndex, param.getType());
            } else {
                cs.setObject(i + sizeIn + returnIndex, param.getValue(), param.getType());
            }
        }
        cs.execute();
        if (procedure.hasReturn()) {
            list.add(cs.getObject(1));
        }
        if (sizeOut > 0) {
            // 注册输出参数
            for (int i = 1; i <= sizeOut; i++) {
                Object output = cs.getObject(sizeIn + returnIndex + i);
                list.add(output);
            }
        }

        return list;
    }

    /**
     * execute [命令执行]<br/>
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param random 用来标记同一组命令
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @return 影响行数
     */
    public long execute(DriverAdapter adapter, DataRuntime runtime, String random, ConfigStore configs, Run run) throws Exception {
        long result = -1;
        DataSource datasource = datasource(runtime);
        if(null == datasource) {
            return -1;
        }
        int batch = run.getBatch();
        String sql = run.getFinalExecute();
        List<Object> values = run.getValues();
        if(batch>1) {
            result = batch(adapter, runtime, datasource, sql, batch, run.getVol(), values);
        }else {
            Connection con = getConnection(adapter, runtime, datasource);
            PreparedStatement ps = null;
            try{
                ps = con.prepareStatement(sql);
                int idx = 0;
                if (null != values) {
                    for (Object obj : values) {
                        ps.setObject(++idx, obj);
                    }
                }
                result = ps.executeUpdate();
            }finally {
                releaseConnection(adapter, runtime, con, datasource);
            }
        }
        return result;
    }

    /**
     * 根据结果集对象获取列结构,如果有表名应该调用metadata().columns(table);或metadata().table(table).getColumns()
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param run 构建最终执行命令的全部参数，包含表（或视图｜函数｜自定义SQL)查询条件 排序 分页等
     * @param comment 是否需要查询列注释
     * @return LinkedHashMap
     */
    public LinkedHashMap<String, Column> metadata(DriverAdapter adapter, DataRuntime runtime, String random, Run run, boolean comment) {
        LinkedHashMap<String, Column> columns = null;
        DataSource datasource = datasource(runtime);
        String sql = run.getFinalQuery(false);
        if (ConfigTable.IS_LOG_SQL && log.isInfoEnabled()) {
            log.info("{}[action:metadata][cmd:\n{}\n]", random, sql);
        }
        if(null == datasource) {
            return new LinkedHashMap<>();
        }
        Connection con = getConnection(adapter, runtime, datasource);
        PreparedStatement ps = null;
        try {
            ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();/*
            columns = adapter.columns(adapter, runtime, true, null, null, rs);*/
        }catch (Exception e) {
            log.error("metadata 异常:", e);
        }finally {
            releaseConnection(adapter, runtime, con, datasource);
        }
        return columns;
    }

    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 product
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param product 上一步查询结果
     * @return product
     * @throws Exception 异常
     */
    public String product(DriverAdapter adapter, DataRuntime runtime, boolean create, String product) {
        DataSource datasource = null;
        Connection con = null;
        try {
            datasource = datasource(runtime);
            if(null == datasource) {
                return null;
            }
            con = getConnection(adapter, runtime, datasource);
            product = con.getMetaData().getDatabaseProductName();
        }catch (Exception e) {
            log.warn("[check product][fail:{}]", e.toString());
        }finally {
            releaseConnection(adapter, runtime, con, datasource);
        }
        return product;
    }

    /**
     * database[结果集封装]<br/>
     * 根据JDBC内置接口 version
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param version 上一步查询结果
     * @return version
     * @throws Exception 异常
     */
    public String version(DriverAdapter adapter, DataRuntime runtime, boolean create, String version) {
        Connection con = null;
        DataSource datasource = null;
        try {
            datasource = datasource(runtime);
            if(null == datasource) {
                return null;
            }
            con = getConnection(adapter, runtime, datasource);
            version = con.getMetaData().getDatabaseProductVersion();
        }catch (Exception e) {
            log.warn("[check version][fail:{}]", e.toString());
        }finally {
            releaseConnection(adapter, runtime, con, datasource);
        }
        return version;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> LinkedHashMap<String, T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> previous, Table query, int types) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        String pattern = query.getName();
        DataSource datasource = null;
        Connection con = null;
        try{
            datasource = datasource(runtime);
            if(null == datasource) {
                return null;
            }
            con = getConnection(adapter, runtime, datasource);
            DatabaseMetaData dbmd = con.getMetaData();
            String catalogName = null;
            String schemaName = null;
            if(null != catalog) {
                catalogName = catalog.getName();
            }
            if(null != schema) {
                schemaName = schema.getName();
            }
            String[] tmp = adapter.correctSchemaFromJDBC(catalogName, schemaName);
            String[] tps = BeanUtil.list2array(adapter.names(Table.types(types)));
            ResultSet set = dbmd.getTables(tmp[0], tmp[1], pattern, tps);
            previous = JDBCUtil.tables(adapter, runtime, create, previous, set);
        }finally {
            releaseConnection(adapter, runtime, con, datasource);
        }
        return previous;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return tables
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> List<T> tables(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous,  Table query, int types) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        String pattern = query.getName();
        DataSource datasource = null;
        Connection con = null;
        try{
            datasource = datasource(runtime);
            if(null == datasource) {
                return null;
            }
            con = getConnection(adapter, runtime, datasource);
            DatabaseMetaData dbmd = con.getMetaData();
            String catalogName = null;
            String schemaName = null;
            if(null != catalog) {
                catalogName = catalog.getName();
            }
            if(null != schema) {
                schemaName = schema.getName();
            }

            String[] tmp = adapter.correctSchemaFromJDBC(catalogName, schemaName);
            String[] tps = BeanUtil.list2array(adapter.names(Table.types(types)));
            ResultSet set = dbmd.getTables(tmp[0], tmp[1], pattern, tps);
            previous = JDBCUtil.tables(adapter, runtime, create, previous, set);
        }finally {
            releaseConnection(adapter, runtime, con, datasource);
        }
        return previous;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return views
     * @throws Exception 异常
     */
    @Override
    public <T extends View> LinkedHashMap<String, T> views(DriverAdapter adapter, DataRuntime runtime, boolean create,  LinkedHashMap<String, T> previous, View query, int types) throws Exception {
        Catalog catalog = query.getCatalog();
        Schema schema = query.getSchema();
        String pattern = query.getName();
        DataSource datasource = null;
        Connection con = null;
        try{
            datasource = datasource(runtime);
            if(null == datasource) {
                return null;
            }
            con = getConnection(adapter, runtime, datasource);
            DatabaseMetaData dbmd = con.getMetaData();

            String catalogName = null;
            String schemaName = null;
            if(null != catalog) {
                catalogName = catalog.getName();
            }
            if(null != schema) {
                schemaName = schema.getName();
            }
            String[] tmp = adapter.correctSchemaFromJDBC(catalogName, schemaName);
            ResultSet set = dbmd.getTables(tmp[0], tmp[1], pattern, new String[]{"VIEW"});
            previous = JDBCUtil.views(adapter, runtime, create, previous, set);
        }finally {
            releaseConnection(adapter, runtime, con, datasource);
        }
        return previous;
    }

    /**
     * table[结果集封装]<br/>
     * 根据驱动内置方法补充
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param query 查询条件 根据metadata属性
     * @param types 查询的类型 参考 Table.TYPE 多个类型相加算出总和
     * @return views
     * @throws Exception 异常
     */
    @Override
    public <T extends Table> List<T> views(DriverAdapter adapter, DataRuntime runtime, boolean create, List<T> previous, View query, int types) throws Exception {

        return previous;
    }

    /**
     * 根据sql查询列结构
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param previous 上一步查询结果
     * @param table 表
     * @param sql sql
     * @return columns
     * @param <T> Column
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> columns(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> previous, Table table, String sql) throws Exception {
        return previous;
    }

    /**
     * 根方法(3)根据根据驱动内置元数据接口补充表结构
     * @param adapter DriverAdapter
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @return columns
     * @param <T> Column
     */
    @Override
    public <T extends Column> LinkedHashMap<String, T> metadata(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> columns, Column query) throws Exception {
        Table table = query.getTable();
        String pattern = query.getName();
        DataSource ds = null;
        Connection con = null;
        DatabaseMetaData metadata = null;
        try {
            ds = datasource(runtime);
            con = getConnection(adapter, runtime, ds);
            metadata = con.getMetaData();
            columns = JDBCUtil.metadata(adapter, runtime, true, columns, metadata, table, pattern);
        } catch (Exception e) {
            if(ConfigTable.IS_PRINT_EXCEPTION_STACK_TRACE) {
                log.error("metadata 异常:", e);
            }
        }finally {
            releaseConnection(adapter, runtime, con, ds);
        }
        return columns;
    }

    /**
     * index[结果集封装]<br/>
     * 根据驱动内置接口
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param create 上一步没有查到的,这一步是否需要新创建
     * @param query 查询条件 根据metadata属性
     * @return indexes indexes
     * @throws Exception 异常
     */
    @Override
    public <T extends Index> LinkedHashMap<String, T> indexes(DriverAdapter adapter, DataRuntime runtime, boolean create, LinkedHashMap<String, T> indexes, Index query) throws Exception {
        Table table = query.getTable();
        boolean unique = query.isUnique();
        boolean approximate = query.isApproximate();
        DataSource ds = null;
        Connection con = null;
        if(null == indexes) {
            indexes = new LinkedHashMap<>();
        }
        ds = datasource(runtime);
        try{
            con = getConnection(adapter, runtime, ds);
            DatabaseMetaData dbmd = con.getMetaData();
            adapter.checkName(runtime, null, table);
            String[] tmp = adapter.correctSchemaFromJDBC(table.getCatalogName(), table.getSchemaName());
            ResultSet set = dbmd.getIndexInfo(tmp[0], tmp[1], table.getName(), unique, approximate);
            Map<String, Integer> keys = JDBCUtil.keys(set);
            LinkedHashMap<String, Column> columns = null;
            while (set.next()) {
                String name = JDBCUtil.string(keys, "INDEX_NAME", set);
                if(null == name) {
                    continue;
                }
                T index = indexes.get(name.toUpperCase());
                if(null == index) {
                    if(create) {
                        index = (T)new Index();
                        indexes.put(name.toUpperCase(), index);
                    }else{
                        continue;
                    }
                    index.setName(JDBCUtil.string(keys, "INDEX_NAME", set));
                    //index.setType(integer(keys, "TYPE", set, null));
                    index.setUnique(!JDBCUtil.bool(keys, "NON_UNIQUE", set, false));
                    String catalog = BasicUtil.evl(JDBCUtil.string(keys, "TABLE_CATALOG", set), JDBCUtil.string(keys, "TABLE_CAT", set));
                    String schema = BasicUtil.evl(JDBCUtil.string(keys, "TABLE_SCHEMA", set), JDBCUtil.string(keys, "TABLE_SCHEM", set));
                    adapter.correctSchemaFromJDBC(runtime, index, catalog, schema);
                    if(!adapter.equals(table.getCatalog(), index.getCatalog()) || !adapter.equals(table.getSchema(), index.getSchema())) {
                        continue;
                    }
                    index.setTable(JDBCUtil.string(keys, "TABLE_NAME", set));
                    indexes.put(name.toUpperCase(), index);
                    columns = new LinkedHashMap<>();
                    index.setColumns(columns);
                    if(name.equalsIgnoreCase("PRIMARY")) {
                        index.setCluster(true);
                        index.setPrimary(true);
                    }else if(name.equalsIgnoreCase("PK_"+table.getName())) {
                        index.setCluster(true);
                        index.setPrimary(true);
                    }
                }else {
                    columns = index.getColumns();
                }
                String columnName = JDBCUtil.string(keys, "COLUMN_NAME", set);
                Column col = table.getColumn(columnName.toUpperCase());
                Column column = null;
                if(null != col) {
                    column = (Column) col.clone();
                }else{
                    column = new Column();
                    column.setName(columnName);
                }
                String order = JDBCUtil.string(keys, "ASC_OR_DESC", set);
                if(null != order && order.startsWith("D")) {
                    order = "DESC";
                }else{
                    order = "ASC";
                }
                column.setOrder(order);
                column.setPosition(JDBCUtil.integer(keys,"ORDINAL_POSITION", set, null));
                columns.put(column.getName().toUpperCase(), column);
            }
        }finally{
            releaseConnection(adapter, runtime, con, ds);
        }
        return indexes;
    }

}
