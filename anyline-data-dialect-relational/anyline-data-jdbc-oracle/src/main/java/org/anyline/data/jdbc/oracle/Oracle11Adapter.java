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


package org.anyline.data.jdbc.oracle;

import org.anyline.data.jdbc.adapter.JDBCAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;

import java.util.List;


/**
 * 11及以下版本
 */
@Repository("anyline.data.jdbc.adapter.oracle.11")
public class Oracle11Adapter extends OracleAdapter implements JDBCAdapter, InitializingBean {
    public String version(){
        return "11";
    }

    @Override
    public boolean match(DataRuntime runtime) {
        List<String> keywords = type().keywords(); //关键字+jdbc-url前缀+驱动类
        String feature = runtime.getFeature();//数据源特征中包含上以任何一项都可以通过
        boolean chk = match(feature, keywords);
        if(chk) {
            String version = runtime.getVersion();
            //Oracle Database 11g Enterprise Edition Release 11.2.0.1.0 - 64bit Production With the Partitioning, OLAP, Data Mining and Real Application Testing options*//*
            if(null != version ){
                version = RegularUtil.cut(version, "release","-");
                if(null != version){
                    //11.2.0.1.0
                    version = version.split("\\.")[0];
                }
                double v = BasicUtil.parseDouble(version, 0d);
                if(v < 12.0){
                    return true;
                }
            }
        }
        return false;
    }
    @Override
    public String mergeFinalQuery(DataRuntime runtime, Run run){
        StringBuilder builder = new StringBuilder();
        String cols = run.getQueryColumn();
        PageNavi navi = run.getPageNavi();
        String sql = run.getBaseQuery();
        OrderStore orders = run.getOrderStore();
        long first = 0;
        long last = 0;
        String order = "";
        if(null != orders){
            order = orders.getRunText(getDelimiterFr()+getDelimiterTo());
        }
        if(null != navi){
            first = navi.getFirstRow();
            last = navi.getLastRow();
        }
        if(null == navi){
            builder.append(sql).append("\n").append(order);
        }else{
            // 分页
            builder.append("SELECT "+cols+" FROM( \n");
            builder.append("SELECT TAB_I.* ,ROWNUM AS PAGE_ROW_NUMBER_ \n");
            builder.append("FROM( \n");
            builder.append(sql);
            builder.append("\n").append(order);
            builder.append(")  TAB_I \n");
            builder.append(")  TAB_O WHERE PAGE_ROW_NUMBER_ >= "+(first+1)+" AND PAGE_ROW_NUMBER_ <= "+(last+1));

        }

        return builder.toString();

    }
}
