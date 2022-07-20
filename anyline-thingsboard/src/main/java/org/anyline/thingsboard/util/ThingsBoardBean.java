package org.anyline.thingsboard.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.thingsboard.load.bean")
public class ThingsBoardBean implements InitializingBean {

    @Value("${anyline.thingsboard.account:}")
    public String ACCOUNT		  ;
    @Value("${anyline.thingsboard.password:}")
    public String PASSWORD        ;
    @Value("${anyline.thingsboard.host:}")
    public String HOST		      ;
    @Value("${anyline.thingsboard.tenant:}")
    public String TENANT	      ;

    @Override
    public void afterPropertiesSet() throws Exception {
        ACCOUNT = BasicUtil.evl(ACCOUNT, ThingsBoardConfig.DEFAULT_ACCOUNT);
        if(BasicUtil.isEmpty(ACCOUNT)){
            return;
        }
        DataRow row = new DataRow();
        row.put("ACCOUNT", BasicUtil.evl(ACCOUNT, ThingsBoardConfig.DEFAULT_ACCOUNT));
        row.put("PASSWORD", BasicUtil.evl(PASSWORD, ThingsBoardConfig.DEFAULT_PASSWORD));
        row.put("HOST", BasicUtil.evl(HOST, ThingsBoardConfig.DEFAULT_HOST));
        row.put("TENANT", BasicUtil.evl(TENANT, ThingsBoardConfig.DEFAULT_TENANT));
        ThingsBoardConfig.register(row);
    }
    @Bean("anyline.thingsboard.init.client")
    public ThingsBoardClient instance(){
        return ThingsBoardClient.getInstance();
    }

}
