package org.anyline.easemob.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.easemob.load.bean")
public class EasemobBean implements InitializingBean {

    @Value("${anyline.easemob.host:}")
    private String HOST 			;
    @Value("${anyline.easemob.client:}")
    private String CLIENT_ID 		;
    @Value("${anyline.easemob.secret:}")
    private String CLIENT_SECRET 	;
    @Value("${anyline.easemob.org:}")
    private String ORG_NAME 		;
    @Value("${anyline.easemob.app:}")
    private String APP_NAME 		;


    @Override
    public void afterPropertiesSet()  {
        HOST = BasicUtil.evl(HOST, EasemobConfig.DEFAULT_HOST);
        if(BasicUtil.isEmpty(HOST)){
            return;
        }
        DataRow row = new DataRow();
        row.put("HOST", BasicUtil.evl(HOST,EasemobConfig.DEFAULT_HOST));
        row.put("CLIENT_ID", BasicUtil.evl(CLIENT_ID,EasemobConfig.DEFAULT_CLIENT_ID));
        row.put("CLIENT_SECRET", BasicUtil.evl(CLIENT_SECRET,EasemobConfig.DEFAULT_CLIENT_SECRET));
        row.put("ORG_NAME", BasicUtil.evl(ORG_NAME,EasemobConfig.DEFAULT_ORG_NAME));
        row.put("APP_NAME", BasicUtil.evl(APP_NAME,EasemobConfig.DEFAULT_APP_NAME));
        EasemobConfig.register(row);
    }

    @Bean("anyline.easemob.init.util")
    public EasemobUtil instance(){
        return EasemobUtil.getInstance();
    }
}
