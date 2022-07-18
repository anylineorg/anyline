package org.anyline.p10ss.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.p100s.load.bean")
public class P10sBean implements InitializingBean {

    @Value("${anyline.p100s.app:}")
    public String APP_ID                   ;
    @Value("${anyline.p100s.secret:}")
    public String APP_SECRET               ;
    @Value("${anyline.p100s.app:type:}")
    public String TYPE                     ; //0:自用 1:开放
    @Value("${anyline.p100s.server:}")
    public String ACCESS_TOKEN_SERVER      ;


    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID = BasicUtil.evl(APP_ID, P10ssConfig.DEFAULT_APP_ID);
        if(BasicUtil.isEmpty(APP_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("APP_ID", BasicUtil.evl(APP_ID, P10ssConfig.DEFAULT_APP_ID));
        row.put("APP_SECRET", BasicUtil.evl(APP_SECRET, P10ssConfig.DEFAULT_APP_SECRET));
        row.put("TYPE", BasicUtil.evl(TYPE, P10ssConfig.DEFAULT_TYPE));
        row.put("ACCESS_TOKEN_SERVER", BasicUtil.evl(ACCESS_TOKEN_SERVER, P10ssConfig.DEFAULT_ACCESS_TOKEN_SERVER));
    }
    @Bean("anyline.p10s.init.util")
    public P10ssUtil instance(){
        return P10ssUtil.getInstance();
    }
}
