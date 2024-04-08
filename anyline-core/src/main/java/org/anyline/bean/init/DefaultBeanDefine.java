package org.anyline.bean.init;

import org.anyline.bean.BeanDefine;

import java.util.LinkedHashMap;

public class DefaultBeanDefine implements BeanDefine {
    public DefaultBeanDefine(){}
    public DefaultBeanDefine(Class type){
        this.type = type;
    }
    public DefaultBeanDefine(String type){
        this.typeName = type;
    }
    public DefaultBeanDefine(Class type, boolean lazy){
        this.type = type;
        this.lazy = lazy;
    }
    public DefaultBeanDefine(String type, boolean lazy){
        this.typeName = type.trim();
        this.lazy = lazy;
    }
    private String typeName;
    private Class type;
    private boolean lazy = true;
    private boolean primary = false;
    private LinkedHashMap<String, Object> values = new LinkedHashMap();

    public String getTypeName() {
        if(null == typeName && null != type){
            typeName = type.getName();
        }
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName.trim();
    }

    @Override
    public Class getType() {
        if(null == type && null != typeName){
            try {
                type = Class.forName(typeName);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return type;
    }

    @Override
    public BeanDefine setType(Class type) {
        this.type = type;
        return this;
    }

    public boolean isPrimary() {
        return primary;
    }

    public BeanDefine setPrimary(boolean primary) {
        this.primary = primary;
        return this;
    }
    public boolean isLazy() {
        return lazy;
    }

    public BeanDefine setLazy(boolean lazy) {
        this.lazy = lazy;
        return this;
    }

    public LinkedHashMap<String, Object> getValues() {
        return values;
    }

    public BeanDefine setValues(LinkedHashMap<String, Object> values) {
        this.values = values;
        return this;
    }

    @Override
    public BeanDefine addValue(String name, Object value) {
        values.put(name, value);
        return this;
    }
    @Override
    public BeanDefine addReferenceValue(String name, String value) {
        values.put(name, new DefaultValueReference(value));
        return this;
    }
}
