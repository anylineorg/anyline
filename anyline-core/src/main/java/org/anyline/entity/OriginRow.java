/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.entity;

import org.anyline.adapter.KeyAdapter.KEY_CASE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OriginRow extends DataRow {
    private static final long serialVersionUID = -2098827041540802316L;
    private static final Logger log = LoggerFactory.getLogger(OriginRow.class);
    protected KEY_CASE keyCase 				        = KEY_CASE.SRC      ; // 列名格式

    public OriginRow(){
        String pk = keyAdapter.key(DEFAULT_PRIMARY_KEY);
        if (null != pk) {
            primaryKeys.add(DEFAULT_PRIMARY_KEY);
        }
        parseKeyCase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }
    public KEY_CASE keyCase(){
        return this.keyCase;
    }
    @Override
    public Object put(String key, Object value) {
        put(keyCase, key, value, false, true);
        return this;
    }

    public DataRow put(String key){
        DataRow row = new OriginRow();
        put(key, row);
        return row;
    }
    public DataRow set(String key, Object value) {
        put(keyCase, key, value, false, true);
        return this;
    }
    public Object get(String key) {
        return super.get(key);
    }

}