package org.anyline.compatible;

import org.anyline.util.BeanUtil;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component("anyline.compatible.default")
public class DefaultCompatible implements Compatible{
    @Override
    public String table(Class clazz) {
        return "hr_"+clazz.getSimpleName();
    }

    @Override
    public List<String> columns(Class clazz) {
        List<String> columns = new ArrayList<>();
        List<Field> fields = BeanUtil.getFields(clazz);
        for(Field field:fields){
            String name = field.getName();
            String column = BeanUtil.camel_(name);
            columns.add(column);
        }
        return columns;
    }

    @Override
    public String column(Class clazz, Field field) {
        return null;
    }

    @Override
    public Field field(Class clazz, String column) {
        return null;
    }

    @Override
    public String primary(Class clazz) {
        return null;
    }

    @Override
    public List<String> primarys(Class clazz) {
        return null;
    }

    @Override
    public <T> T entity(Class<T> clazz, Map<String, Object> map) {
        T entity = BeanUtil.map2object(map, clazz, false, true, true);
        return entity;
    }
}
