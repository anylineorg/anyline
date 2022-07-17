package org.anyline.aliyun.sms.util;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class SMSBean {

    @Value("${anyline.aliyun.sms.key:}")
    private String ACCESS_KEY;
    @Value("${anyline.aliyun.sms.secret:}")
    private String ACCESS_SECRET;
    @Value("${anyline.aliyun.sms.sign:}")
    private String SIN;

    @PostConstruct
    private void init(){
        SMSConfig.register(
                 BasicUtil.evl(ACCESS_KEY, SMSConfig.DEFAULT_ACCESS_KEY)
                , BasicUtil.evl(ACCESS_SECRET, SMSConfig.DEFAULT_ACCESS_SECRET)
        );
    }
    @Bean
    public SMSUtil instance(){
        return SMSUtil.getInstance();
    }


}
