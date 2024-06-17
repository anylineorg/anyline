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



package org.anyline.data.runtime.init;

import org.anyline.data.runtime.RuntimeHolder;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractRuntimeHolder implements RuntimeHolder {
    /**
     * 临时数据源
     */
    protected static Map<String, Object> temporary = new HashMap<>();

    public static String parseAdapterKey(String url) {
        return parseParamValue(url, "adapter");
    }
    public static String parseCatalog(String url) {
        return parseParamValue(url, "catalog");
    }
    public static String parseSchema(String url) {
        return parseParamValue(url, "schema");
    }
    public static String parseParamValue(String url, String key) {
        String value = null;
        if(null != url && url.contains(key)) {
            value = RegularUtil.cut(url, key+"=", "&");
            if(BasicUtil.isEmpty(value)) {
                value = RegularUtil.cut(url, key+"=", RegularUtil.TAG_END);
            }
        }
        return value;
    }
}