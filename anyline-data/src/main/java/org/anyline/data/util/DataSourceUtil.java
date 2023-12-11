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

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.BaseMetadata;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BasicUtil;

import java.util.Collection;

public class DataSourceUtil {

    /**
     * 解析数据源,并返回修改后的SQL
     * &lt;mysql_ds&gt;crm_user
     * @param src  src
     * @return String
     */
    public static String parseDataSource(String src){
        if(null != src && src.startsWith("<")){
            int fr = src.indexOf("<");
            int to = src.indexOf(">");
            if(fr != -1){
                //String ds = src.substring(fr+1,to);
                src = src.substring(to+1);
                //不要切换,在service中切换到另一个service
                //ClientHolder.setDataSource(ds, true);
            }
        }
        return src;
    }
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
                runtime = src.substring(fr+1,to);
                src = src.substring(to+1);
                result[0] = runtime;
                result[1] = src;
            }
        }
        return result;
    }

    public static String parseDataSource(String dest, Object obj){
        if(BasicUtil.isNotEmpty(dest) || null == obj){
            return parseDataSource(dest);
        }
        String result = "";
        if(obj instanceof DataRow){
            DataRow row = (DataRow)obj;
            String link = row.getDataLink();
            if(BasicUtil.isNotEmpty(link)){
                //DatasourceHolder.setDataSource(link, true);
            }
            result = row.getDataSource();
        }else if(obj instanceof DataSet){
            DataSet set = (DataSet)obj;
            if(set.size()>0){
                result = parseDataSource(dest, set.getRow(0));
            }
        } else if (obj instanceof Collection) {
            Object first = ((Collection)obj).iterator().next();
            if(first instanceof DataRow){
                result = parseDataSource(dest, first);
            }else {
                result = EntityAdapterProxy.table(first.getClass(), true);
            }
        } else{
            result = EntityAdapterProxy.table(obj.getClass(), true);
        }
        result = parseDataSource(result);
        return result;
    }
}
