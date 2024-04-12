package org.anyline.data.jdbc.adapter.init;

import org.anyline.data.runtime.DataRuntime;
import org.anyline.util.ConfigTable;
public class CommonJDBCAdapter extends AbstractJDBCAdapter {
    /**
     * 验证运行环境与当前适配器是否匹配<br/>
     * 默认不连接只根据连接参数<br/>
     * 只有同一个库区分不同版本(如mmsql2000/mssql2005)或不同模式(如kingbase的oracle/pg模式)时才需要单独实现
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param compensate 是否补偿匹配，第一次失败后，会再匹配一次，第二次传入true
     * @return boolean
     */
    @Override
    public boolean match(DataRuntime runtime, boolean compensate) {
        if(!ConfigTable.IS_ENABLE_COMMON_JDBC_ADAPTER){
            return false;
        }
        String feature = runtime.getFeature();
        //第二次匹配时再执行
        if(compensate && null != feature && feature.contains(":jdbc:")){
            return true;
        }
        return false;
    }
}
