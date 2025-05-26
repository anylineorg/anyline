/*
 * Copyright 2006-2025 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.anyline.adapter.init;

import org.anyline.adapter.KeyAdapter;
import org.anyline.util.BeanUtil;

/**
 * 下划线转大驼峰
 */
public class UpperCamelAdapter implements KeyAdapter {
    private static KeyAdapter instance = new UpperCamelAdapter();
    @Override
    public String key(String key) {
        if(null != key) {
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
