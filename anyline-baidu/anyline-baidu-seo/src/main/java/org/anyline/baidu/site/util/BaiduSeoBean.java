package org.anyline.baidu.site.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.baidu.site.load.bean")
public class BaiduSeoBean implements InitializingBean {

    @Value("${anyline.baidu.site.site:}")
    private String SITE		;
    @Value("${anyline.baidu.site.token:}")
    private String TOKEN 	;


    @Override
    public void afterPropertiesSet()  {
        SITE = BasicUtil.evl(SITE, BaiduSeoConfig.DEFAULT_SITE);
        if(BasicUtil.isEmpty(SITE)){
            return;
        }
        BaiduSeoConfig.register(SITE, BasicUtil.evl(TOKEN, BaiduSeoConfig.DEFAULT_TOKEN));
    }
    @Bean("anyline.baidu.site.init.client")
    public BaiduSeoClient instance(){
        return BaiduSeoClient.getInstance();
    }
}
