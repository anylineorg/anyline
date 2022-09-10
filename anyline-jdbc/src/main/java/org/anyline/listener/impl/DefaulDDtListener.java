package org.anyline.listener.impl;

import org.anyline.jdbc.entity.Column;
import org.anyline.jdbc.entity.Table;
import org.anyline.listener.DDListener;
import org.anyline.service.AnylineService;
import org.springframework.stereotype.Component;


@Component("anyline.jdbc.listener.dd.default")
public class DefaulDDtListener implements DDListener {

    protected AnylineService service;


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

    public void setService(AnylineService service){
        this.service = service;
    }
}
