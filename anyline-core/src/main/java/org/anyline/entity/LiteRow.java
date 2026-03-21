/*
 * Copyright 2006-2026 www.anyline.org
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

import java.io.Serializable;

public class LiteRow extends DataRow implements Serializable {
    private static final long serialVersionUID = -2098827041540802313L;
    private Object[] values = null;
    private LiteRow() {}
    public LiteRow(Object[] values) {
        this.values = values;
    }
    private int index(String key){
        int index = -1;
        DataSet container = getContainer();
        if(null != container){
            container.getMetadatas();
        }
        return index;
    }
    public Object put(String key, Object value) {
        int index = index(key);
        if(index != -1){
            values[index] = value;
        }else{
            throw new ArrayIndexOutOfBoundsException("Index out of bounds");
        }
        return this;
    }
    public void put(Object[] values) {
        this.values = values;
    }
    @Override
    public Object get(String key){
        int index = index(key);
        if(index != -1){
            return values[index];
        }
        return null;
    }
    @Override
    public String getString(String key) {
        return getString(new String[]{key});
    }
    public String getString(String ... keys) {
        if(null == keys) {
            return null;
        }
        String result = null;
        for(String key:keys) {
            if (null == key) {
                continue;
            }
            Object value = get(key);
            if (null != value) {
                if(value instanceof byte[]) {
                    result = new String((byte[])value);
                }else {
                    result = value.toString();
                }
            }
        }
        return result;
    }
}
