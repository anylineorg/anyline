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

package org.anyline.data.util;

import org.anyline.data.param.ConfigStore;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.BaseMetadata;
import org.anyline.metadata.Table;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import java.util.Collection;

public class DataSourceUtil {

    public static String[] parseRuntime(BaseMetadata meta){
        if(null != meta){
            return parseRuntime(meta.getName());
        }
        return new String[2];
    }
    public static String[] parseRuntime(String src){
        String result[] = new String[2];
        result[1] = src;
        String runtime = null;
        if(null != src && src.startsWith("<")){
            int fr = src.indexOf("<");
            int to = src.indexOf(">");
            if(fr != -1){
                runtime = src.substring(fr+1, to);
                src = src.substring(to+1);
                result[0] = runtime;
                result[1] = src;
            }
        }
        return result;
    }

    /**
     * 解析数据源, 并返回修改后的SQL
     * &lt;mysql_ds&gt;crm_user
     * @param src  src
     * @return String
     */
    public static Table parseDest(String src, ConfigStore configs){
        if(null == src){
            return null;
        }
        Table result = new Table();
        //<sso>pw_user
        if(src.startsWith("<")){
            int fr = src.indexOf("<");
            int to = src.indexOf(">");
            if(fr != -1){
                String datasource = src.substring(fr+1, to);
                src = src.substring(to+1);
                result.setDataSource(datasource);
            }
        }
        //pw_user<id, code>
        if(src.endsWith(">")){
            int fr = src.lastIndexOf("<");
            if(fr != -1) {
                String[] keys = src.substring(fr + 1, src.length() - 1).split(",");
                src = src.substring(0, fr);
                result.setPrimaryKey(keys);
            }
        }
        if(src.contains(" ")){
            result.setText(src);
        }else if(src.contains(":")){
            result.setId(src);
        }
        result.setName(src);
        return result;
    }
    public static Table parseDest(String dest, Object obj, ConfigStore configs){
        Table table = null;
        //有表的根据表解析
        if(BasicUtil.isNotEmpty(dest) || null == obj){
            return parseDest(dest, configs);
        }
        //没有表的根据 对象解析

        if(obj instanceof DataRow){
            DataRow row = (DataRow)obj;
            table = parseDest(row.getDest(), configs);
        }else if(obj instanceof DataSet){
            DataSet set = (DataSet)obj;
            if(set.size()>0){
                table = parseDest(set.getRow(0).getDest(), configs);
            }
        } else if (obj instanceof Collection) {
            Collection list = (Collection)obj;
            if(!list.isEmpty()){
                Object first = list.iterator().next();
                if(first instanceof DataRow){
                    table = parseDest(((DataRow)first).getDest(), configs);
                }else {
                    String tableName = EntityAdapterProxy.table(first.getClass(), true);
                    table = parseDest(tableName, configs);
                }
            }
        } else{
            table = EntityAdapterProxy.table(obj.getClass());
        }
        return table;
    }

    public static String parseAdapterKey(String url){
        return parseParamValue(url, "adapter");
    }
    public static String parseCatalog(String url){
        return parseParamValue(url, "catalog");
    }
    public static String parseSchema(String url){
        return parseParamValue(url, "schema");
    }
    public static String parseParamValue(String url, String key){
        String value = null;
        if(null != url && url.contains(key)){
            value = RegularUtil.cut(url, key+"=", "&");
            if(BasicUtil.isEmpty(value)){
                value = RegularUtil.cut(url, key+"=", RegularUtil.TAG_END);
            }
        }
        return value;
    }
}
