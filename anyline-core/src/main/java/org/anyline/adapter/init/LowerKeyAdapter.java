package org.anyline.adapter.init;

import org.anyline.adapter.KeyAdapter;

public class LowerKeyAdapter implements KeyAdapter {
    private static KeyAdapter instance = new LowerKeyAdapter();
    @Override
    public String key(String key) {
        if(null != key){
            return key.toLowerCase();
        }
        return null;
    }

    @Override
    public KEY_CASE getKeyCase() {
        return KEY_CASE.LOWER;
    }

    public static KeyAdapter getInstance() {
        return instance;
    }

}
