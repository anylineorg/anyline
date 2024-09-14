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

package org.anyline.data.listener.init;

import org.anyline.data.adapter.DriverAdapter;
import org.anyline.data.listener.DDListener;
import org.anyline.data.param.ConfigStore;
import org.anyline.data.param.init.DefaultConfigStore;
import org.anyline.data.prepare.RunPrepare;
import org.anyline.data.prepare.auto.init.DefaultTablePrepare;
import org.anyline.data.run.RunValue;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.DefaultPageNavi;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.ACTION;
import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.RegularUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DefaultDDListener implements DDListener {

    protected Log log = LogProxy.get(DefaultDDListener.class);
    /**
     * ddl异常触发
     * @param table 表
     * @param column 修改的列
     * @param exception 异常
     * @return boolean 如果返回true(如处理完异常数据后), dao中会再执行一次ddl
     */
    @Override
    public ACTION.SWITCH afterAlterColumnException(DataRuntime runtime, String random, Table table, Column column, Exception exception) {
         if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION ==  0) {
            return ACTION.SWITCH.CONTINUE;
        }
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION == 1) {
            exeAfterException(runtime, table, column, exception);
        }else{
            // 根据行数
            RunPrepare prepare = new DefaultTablePrepare();
            prepare.setDest(table);
            long rows = runtime.getAdapter().count(runtime, random, prepare, null);
            if(rows > ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION) {
                swt = afterAlterColumnException(runtime, random, table, column, rows, exception);
            }else{
                swt = exeAfterException(runtime, table, column, exception);
            }
        }
        return swt;
    }

    public ACTION.SWITCH exeAfterException(DataRuntime runtime, Table table, Column column, Exception exception) {
        DriverAdapter adapter = runtime.getAdapter();
         Column update = column.getUpdate();
        boolean isNum = adapter.isNumberColumn(runtime, update);
        if(adapter.isCharColumn(runtime, column) && !adapter.isCharColumn(runtime, update)) {
            // 原来是String类型 修改成 boolean或number类型 失败
            int page = 1;
            int vol = 100;
            PageNavi navi = new DefaultPageNavi();
            navi.setPageRows(vol);
            LinkedHashMap<String, Column> pks = table.primarys();
            if(pks.isEmpty()) {
                if(null == table.getColumn(DataRow.DEFAULT_PRIMARY_KEY)) {
                    // 没有主键
                    return ACTION.SWITCH.SKIP;
                }
            }
            List<String> keys = new ArrayList<>();
            for (Column pk:pks.values()) {
                keys.add(pk.getName());
            }

            while (true) {
                navi.setCurPage(page);
                RunPrepare prepare = new DefaultTablePrepare();
                prepare.setDest(table.getName());
                ConfigStore configs = new DefaultConfigStore();
                configs.setPageNavi(navi);
                DataSet set = runtime.getAdapter().querys(runtime, null, prepare, configs);
                if(set.isEmpty()) {
                    break;
                }
                set.setPrimaryKey(true, keys);
                for(DataRow row:set) {
                    String value = row.getString(column.getName()+ConfigTable.ALTER_COLUMN_TYPE_SUFFIX);
                    if(null == value) {
                        value = row.getString(column.getName());
                    }
                    if(null != value) {
                        Object convert = null;
                        if(isNum) {
                            value = char2number(value);
                        }
                        RunValue run = new RunValue();
                        run.setValue(value);
                        adapter.convert(runtime, update, run);
                        convert = run.getValue();
                        row.put(column.getName(), convert);
                        log.warn("[after exception][数据修正][{}>{}]", value, convert);
                        runtime.getAdapter().update(runtime, null, table.getName(), row, new DefaultConfigStore(), column.getName());
                    }
                }
                if(set.size() <  vol) {
                    break;
                }
                page ++;
            }
        }
        return ACTION.SWITCH.CONTINUE;
    }
    private String char2number(String value) {
        value = value.replaceAll("\\s","");
        try {
            value = RegularUtil.fetchNumber(value);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

}
