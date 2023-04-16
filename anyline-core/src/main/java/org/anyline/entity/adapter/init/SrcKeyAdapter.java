package org.anyline.entity.adapter.init;

import org.anyline.entity.adapter.KeyAdapter;

public class SrcKeyAdapter implements KeyAdapter {
    private static KeyAdapter instance = new SrcKeyAdapter();
    @Override
    public String key(String key) {
        return key;
    }

    public static KeyAdapter getInstance() {
        return instance;
    }

    @Override
    public KEY_CASE getKeyCase() {
        return KEY_CASE.SRC;
    }
}
