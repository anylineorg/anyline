package org.anyline.data.listener.init;

import org.anyline.dao.AnylineDao;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component("anyline.jdbc.listener.dd.default")
public class DefaultDDListener implements DDListener {

    protected Logger log = LoggerFactory.getLogger(DefaultDDListener.class);
    /**
     * ddl异常触发
     * @param table 表
     * @param column 修改的列
     * @param exception 异常
     * @return boolean 如果返回true(如处理完异常数据后),dao中会再执行一次ddl
     */
    @Override
    public ACTION.SWITCH afterAlterColumnException(DataRuntime runtime, String random, Table table, Column column, Exception exception) {
        AnylineDao dao = runtime.getDao();
        if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION ==  0){
            return ACTION.SWITCH.CONTINUE;
        }
        ACTION.SWITCH swt = ACTION.SWITCH.CONTINUE;
        if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION == 1){
            exeAfterException(runtime,table, column, exception);
        }else{
            // 根据行数
            RunPrepare prepare = new DefaultTablePrepare();
            prepare.setDataSource(table.getName());
            long rows = dao.count(prepare);
            if(rows > ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION){
                swt = afterAlterColumnException(runtime, random, table, column, rows, exception);
            }else{
                swt = exeAfterException(runtime,table, column, exception);
            }
        }
        return swt;
    }

    public ACTION.SWITCH exeAfterException(DataRuntime runtime, Table table, Column column, Exception exception){
        DriverAdapter adapter = runtime.getAdapter();
        AnylineDao dao = runtime.getDao();
        Column update = column.getUpdate();
        boolean isNum = adapter.isNumberColumn(update);
        if(adapter.isCharColumn(column) && !adapter.isCharColumn(update)){
            // 原来是String类型 修改成 boolean或number类型 失败
            int page = 1;
            int vol = 100;
            PageNavi navi = new DefaultPageNavi();
            navi.setPageRows(vol);
            List<Column> pks = table.primarys();
            if(pks.size() == 0){
                if(null == table.getColumn(DataRow.DEFAULT_PRIMARY_KEY)){
                    // 没有主键
                    return ACTION.SWITCH.SKIP;
                }
            }
            List<String> keys = new ArrayList<>();
            for (Column pk:pks){
                keys.add(pk.getName());
            }

            while (true){
                navi.setCurPage(page);
                RunPrepare prepare = new DefaultTablePrepare();
                prepare.setDataSource(table.getName());
                ConfigStore configs = new DefaultConfigStore();
                configs.setPageNavi(navi);
                DataSet set = dao.querys(prepare, configs);
                if(set.size() ==0){
                    break;
                }
                set.setPrimaryKey(true, keys);
                for(DataRow row:set){
                    String value = row.getString(column.getName()+"_TMP_UPDATE_TYPE");
                    if(null == value){
                        value = row.getString(column.getName());
                    }
                    if(null != value){
                        Object convert = null;
                        if(isNum) {
                            value = char2number(value);
                        }
                        RunValue run = new RunValue();
                        run.setValue(value);
                        adapter.convert(update, run);
                        convert = run.getValue();
                        row.put(column.getName(), convert);
                        log.warn("[after exception][数据修正][{}>{}]", value, convert);
                        dao.update(table.getName(), row, column.getName());
                    }
                }
                if(set.size() <  vol){
                    break;
                }
                page ++;
            }
        }
        return ACTION.SWITCH.CONTINUE;
    }
    private String char2number(String value){
        value = value.replaceAll("\\s","");
        try {
            value = RegularUtil.fetchNumber(value);
        }catch (Exception e){
            e.printStackTrace();
        }
        return value;
    }

}