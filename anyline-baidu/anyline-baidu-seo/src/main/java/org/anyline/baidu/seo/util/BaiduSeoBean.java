package org.anyline.baidu.seo.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.baidu.seo.load.bean")
public class BaiduSeoBean implements InitializingBean {

    @Value("${anyline.baidu.seo.site:}")
    private String SITE		; //站点URL如 http://www.anyline.org
    @Value("${anyline.baidu.seo.token:}")
    private String TOKEN 	;


    @Override
    public void afterPropertiesSet()  {
        SITE = BasicUtil.evl(SITE, BaiduSeoConfig.DEFAULT_SITE);
        if(BasicUtil.isEmpty(SITE)){
            return;
        }
        BaiduSeoConfig.register(SITE, BasicUtil.evl(TOKEN, BaiduSeoConfig.DEFAULT_TOKEN));
    }
    @Bean("anyline.baidu.seo.init.client")
    public BaiduSeoClient instance(){
        return BaiduSeoClient.getInstance();
    }
}
