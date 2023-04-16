package org.anyline.entity.adapter.init;

import org.anyline.entity.adapter.KeyAdapter;

public class UpperKeyAdapter implements KeyAdapter {

    private static KeyAdapter instance = new UpperKeyAdapter();
    @Override
    public String key(String key) {
        if(null != key){
            return key.toUpperCase();
        }
        return null;
    }

    @Override
    public KEY_CASE getKeyCase() {
        return KEY_CASE.UPPER;
    }

    public static KeyAdapter getInstance() {
        return instance;
    }
}
