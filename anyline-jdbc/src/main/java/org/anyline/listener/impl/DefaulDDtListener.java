package org.anyline.listener.impl;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.PageNavi;
import org.anyline.entity.PageNaviImpl;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.listener.DDListener;
import org.anyline.service.AnylineService;
import org.anyline.util.ConfigTable;
import org.anyline.util.regular.RegularUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component("anyline.jdbc.listener.dd.default")
public class DefaulDDtListener implements DDListener {

    protected AnylineService service;
    protected SQLCreater creater;


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
    public boolean afterAlterException(Table table, Column column, Exception exception) {
        if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION ==  0){
            return false;
        }
        if(ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION == 1){
            exeAfterException(table, column, exception);
        }else{
            //根据行数
            int rows = service.count(table.getName());
            if(rows > ConfigTable.AFTER_ALTER_COLUMN_EXCEPTION_ACTION){
                afterAlterException(table, column, rows, exception);
            }else{
                exeAfterException(table, column, exception);
            }
        }
        return false;
    }

    @Override
    public boolean afterAlterException(Table table, Column column, int rows, Exception exception) {
        return false;
    }
    public boolean exeAfterException(Table table, Column column, Exception exception){

        Column update = column.getUpdate();
        if(creater.isCharColumn(column) && !creater.isCharColumn(update)){
            //原来是String类型 修改成 boolean或number类型 失败
            int page = 1;
            int vol = 100;
            PageNavi navi = new PageNaviImpl();
            List<Column> pks = table.getPrimaryKeys();
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
                    String value = row.getString(column.getName());
                    if(null != value){
                        value = char2number(value);
                        row.put(column.getName(), value);
                        service.update(table.getName(), row, column.getName());
                    }
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
    public void afterAlter(Table table, boolean result) {

    }

    @Override
    public boolean beforeDrop(Table table) {
        return true;
    }

    @Override
    public void afterDrop(Table table, boolean result) {

    }

    public AnylineService getService() {
        return service;
    }

    public SQLCreater getCreater() {
        return creater;
    }

    public void setCreater(SQLCreater creater) {
        this.creater = creater;
    }

    public void setService(AnylineService service){
        this.service = service;
    }
}
