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

package org.anyline.metadata.refer;

import org.anyline.util.BasicUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 读取元数据结果集依据(元数据属性与列名对应关系)
 */
public class MetadataFieldRefer {
    private Map<String, String[]> map = new HashMap<>();
    private final Class<?> metadata;
    public MetadataFieldRefer(Class<?> metadata) {
        this.metadata = metadata;
    }
    public Class<?> metadata() {
        return metadata;
    }
    public String[] maps(String field) {
        return map.get(field.toUpperCase());
    }

    public String map(String field) {
        String[] refers = map.get(field.toUpperCase());
        if(null != refers && refers.length > 0) {
            return refers[0];
        }
        return null;
    }

    /**
     * 属性与列名对应关系
     * @param field metadata属性
     * @param refers 列名 如果有多种情况 提供多个
     * @return MetadataFieldRefer
     */
    public MetadataFieldRefer map(String field, String[] refers) {
        map.put(field.toUpperCase(), refers);
        return this;
    }

    /**
     * 属性与列名对应关系
     * @param field metadata属性
     * @param refer 列名 如果有多种情况 用逗号分隔
     * @return MetadataFieldRefer
     */
    public MetadataFieldRefer map(String field, String refer) {
        String[] refers = null;
        if(BasicUtil.isNotEmpty(refer)) {
            refers = refer.split(",");
        }else{
            refers = null;
        }
        map.put(field.toUpperCase(), refers);
        return this;
    }
    public MetadataFieldRefer copy(MetadataFieldRefer copy) {
        if(null != copy) {
            map.putAll(copy.map);
        }
        return this;
    }

}
