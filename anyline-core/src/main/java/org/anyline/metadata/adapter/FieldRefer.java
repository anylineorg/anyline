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



package org.anyline.metadata.adapter;

import org.anyline.util.BasicUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 读取元数据结果集依据(列名)
 */
public class FieldRefer {
    private Map<String, String[]> map = new HashMap<>();
    private final Class<?> metadata;
    public FieldRefer(Class<?> metadata){
        this.metadata = metadata;
    }
    public Class<?> metadata(){
        return metadata;
    }
    public String[] getRefers(String field) {
        return map.get(field.toUpperCase());
    }

    public String getRefer(String field) {
        String[] refers = map.get(field.toUpperCase());
        if(null != refers && refers.length > 0) {
            return refers[0];
        }
        return null;
    }
    public FieldRefer setRefer(String field, String[] refers) {
        map.put(field.toUpperCase(), refers);
        return this;
    }
    public FieldRefer setRefer(String field, String refer) {
        String[] refers = null;
        if(BasicUtil.isNotEmpty(refer)) {
            refers = refer.split(",");
        }else{
            refers = null;
        }
        map.put(field.toUpperCase(), refers);
        return this;
    }
    public FieldRefer copy(FieldRefer copy){
        if(null != copy){
            map.putAll(copy.map);
        }
        return this;
    }

}
