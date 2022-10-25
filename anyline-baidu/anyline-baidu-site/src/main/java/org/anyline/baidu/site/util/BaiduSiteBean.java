package org.anyline.baidu.site.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.baidu.site.load.bean")
public class BaiduSiteBean implements InitializingBean {

    @Value("${anyline.baidu.site.site:}")
    private String SITE		;
    @Value("${anyline.baidu.site.token:}")
    private String TOKEN 	;


    @Override
    public void afterPropertiesSet() throws Exception {
        SITE = BasicUtil.evl(SITE, BaiduSiteConfig.DEFAULT_SITE);
        if(BasicUtil.isEmpty(SITE)){
            return;
        }
        BaiduSiteConfig.register(SITE, BasicUtil.evl(TOKEN, BaiduSiteConfig.DEFAULT_TOKEN));
    }
    @Bean("anyline.baidu.site.init.client")
    public BaiduSiteClient instance(){
        return BaiduSiteClient.getInstance();
    }
}
