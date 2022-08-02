package org.anyline.aliyun.sms.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.sms.load.bean")
public class SMSBean implements InitializingBean {

    @Value("${anyline.aliyun.sms.key:}")
    private String ACCESS_KEY;
    @Value("${anyline.aliyun.sms.secret:}")
    private String ACCESS_SECRET;
    @Value("${anyline.aliyun.sms.sign:}")
    private String SIGN;


    public static SMSListener listener;

    @Autowired(required=false)
    public void setListener(SMSListener listener){
        SMSBean.listener = listener;
    }
    public static SMSListener getListener(){
        return listener;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ACCESS_KEY = BasicUtil.evl(ACCESS_KEY, SMSConfig.DEFAULT_ACCESS_KEY);
        if(BasicUtil.isEmpty(ACCESS_KEY)) {
            return;
        }
        SMSConfig.register(
                BasicUtil.evl(ACCESS_KEY, SMSConfig.DEFAULT_ACCESS_KEY)
                , BasicUtil.evl(ACCESS_SECRET, SMSConfig.DEFAULT_ACCESS_SECRET)
        );
    }
    @Bean("anyline.aliyun.sms.init.util")
    public SMSUtil instance(){
        return SMSUtil.getInstance();
    }

}
