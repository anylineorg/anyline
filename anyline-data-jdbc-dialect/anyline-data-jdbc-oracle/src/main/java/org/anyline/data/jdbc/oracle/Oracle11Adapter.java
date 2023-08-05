package org.anyline.data.jdbc.oracle;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.run.Run;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.OrderStore;
import org.anyline.entity.PageNavi;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Repository;


/**
 * 11及以下版本
 */
@Repository("anyline.data.jdbc.adapter.oracle.11")
public class Oracle11Adapter extends OracleAdapter implements JDBCAdapter, InitializingBean {
    public String version(){
        return "11";
    }

    @Override
    public String parseFinalQuery(DataRuntime runtime, Run run){
        StringBuilder builder = new StringBuilder();
        String cols = run.getQueryColumns();
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
