package org.anyline.seo.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.bing.seo.load.bean")
public class BingSeoBean implements InitializingBean {

    @Value("${anyline.bing.seo.site:}")
    private String SITE		;   //站点URL如 http://www.anyline.org
    @Value("${anyline.bing.seo.key:}")
    private String KEY 	;


    @Override
    public void afterPropertiesSet()  {
        SITE = BasicUtil.evl(SITE, BingSeoConfig.DEFAULT_SITE);
        if(BasicUtil.isEmpty(SITE)){
            return;
        }
        BingSeoConfig.register(SITE, BasicUtil.evl(KEY, BingSeoConfig.DEFAULT_KEY));
    }
    @Bean("anyline.bing.seo.init.client")
    public BingSeoClient instance(){
        return BingSeoClient.getInstance();
    }
}
