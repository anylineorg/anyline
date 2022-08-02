package org.anyline.entity.adapter;

import org.anyline.util.BeanUtil;

public class CamelAdapter implements KeyAdapter{
    private static KeyAdapter instance = new CamelAdapter();
    @Override
    public String key(String key) {
        if(null != key){
            return BeanUtil.Camel(key);
        }
        return null;
    }

    @Override
    public KEY_CASE getKeyCase() {
        return KEY_CASE.Camel;
    }

    public static KeyAdapter getInstance() {
        return instance;
    }

}
