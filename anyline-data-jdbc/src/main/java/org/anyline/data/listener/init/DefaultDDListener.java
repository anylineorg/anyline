package org.anyline.data.listener.init;

import org.anyline.data.entity.*;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.entity.DefaultPageNavi;
import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.run.RunValue;
import org.anyline.data.listener.DDListener;
import org.anyline.service.AnylineService;
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
    protected AnylineService service;
    protected JDBCAdapter adapter;


    @Override
    public boolean beforeCreate(Table table) {
        return true;
    }

    @Override
    public void afterCreate(Table table, boolean result) {

    }

    @Override
    public boolean beforeCreate(View view) {
        return true;
    }

    @Override
    public void afterCreate(View view, boolean result) {

    }

    @Override
    public boolean beforeAdd(Column column) {
        return true;
    }

    @Override
    public void afterAdd(Column column, boolean result) {

    }

    @Override
    public boolean beforeAlter(Column column) {
        return true;
    }

    @Override
    public void afterAlter(Column column, boolean result) {

    }

    /**
     * ddl异常触发
     * @param table 表
     * @param column 修改的列
     * @param exception 异常
     * @return boolean 如果返回true(如处理完异常数据后),dao中会再执行一次ddl
     */
    @Override
    public boolean afterAlterColumnException(Table table, Column column, Exception exception) {
        if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION ==  0){
            return false;
        }
        boolean result = false;
        if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION == 1){
            exeAfterException(table, column, exception);
        }else{
            // 根据行数
            int rows = service.count(table.getName());
            if(rows > ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION){
                result = afterAlterColumnException(table, column, rows, exception);
            }else{
                result = exeAfterException(table, column, exception);
            }
        }
        return result;
    }

    @Override
    public boolean afterAlterColumnException(Table table, Column column, int rows, Exception exception) {
        return false;
    }
    public boolean exeAfterException(Table table, Column column, Exception exception){
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
                    return false;
                }
            }
            List<String> keys = new ArrayList<>();
            for (Column pk:pks){
                keys.add(pk.getName());
            }

            while (true){
                navi.setCurPage(page);
                DataSet set = service.querys(table.getName(), navi);
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
                        service.update(table.getName(), row, column.getName());
                    }
                }
                if(set.size() <  vol){
                    break;
                }
                page ++;
            }
        }
        return true;
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

    @Override
    public boolean beforeDrop(Column column) {
        return true;
    }

    @Override
    public void afterDrop(Column column, boolean result) {

    }

    @Override
    public boolean beforeAlter(Table table) {
        return true;
    }

    @Override
    public boolean beforeAlter(Table table, List<Column> columns){
        return true;
    }
    @Override
    public void afterAlter(Table table, boolean result) {

    }
    public void afterAlter(Table table, List<Column> columns, boolean result){}

    @Override
    public boolean beforeAlter(View view) {
        return true;
    }

    @Override
    public void afterAlter(View view, boolean result) {

    }

    @Override
    public boolean beforeDrop(Table table) {
        return true;
    }

    @Override
    public void afterDrop(Table table, boolean result) {

    }

    @Override
    public boolean beforeDrop(View view) {
        return true;
    }

    @Override
    public void afterDrop(View view, boolean result) {

    }

    @Override
    public boolean beforeRename(Table table) {
        return true;
    }

    @Override
    public void afterRename(Table table, boolean result) {

    }

    public AnylineService getService() {
        return service;
    }

    public JDBCAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(JDBCAdapter adapter) {
        this.adapter = adapter;
    }

    @Override
    public boolean beforeAdd(Index index) {
        return true;
    }

    @Override
    public void afterAdd(Index index, boolean result) {

    }

    @Override
    public boolean beforeAlter(Index index) {
        return true;
    }

    @Override
    public void afterAlter(Index index, boolean result) {

    }

    @Override
    public boolean beforeDrop(Index index) {
        return true;
    }

    @Override
    public void afterDrop(Index index, boolean result) {

    }

    @Override
    public boolean beforeAdd(Constraint constraint) {
        return true;
    }

    @Override
    public void afterAdd(Constraint constraint, boolean result) {

    }

    @Override
    public boolean beforeAlter(Constraint constraint) {
        return true;
    }

    @Override
    public void afterAlter(Constraint constraint, boolean result) {

    }

    @Override
    public boolean beforeDrop(Constraint constraint) {
        return true;
    }

    @Override
    public void afterDrop(Constraint constraint, boolean result) {

    }

    public void setService(AnylineService service){
        this.service = service;
    }
    public AnylineService setService(){
        return service;
    }
}
