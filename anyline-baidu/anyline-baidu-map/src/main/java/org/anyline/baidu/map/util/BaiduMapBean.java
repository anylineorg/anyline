package org.anyline.baidu.map.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.baidu.map.load.bean")
public class BaiduMapBean implements InitializingBean {

    @Value("${anyline.baidu.map.ak:}")
    private String AK		;
    @Value("${anyline.baidu.map.sk:}")
    private String SK 	;


    @Override
    public void afterPropertiesSet() throws Exception {
        AK = BasicUtil.evl(AK, BaiduMapConfig.DEFAULT_AK);
        if(BasicUtil.isEmpty(AK)){
            return;
        }
        BaiduMapConfig.register(AK, BasicUtil.evl(SK, BaiduMapConfig.DEFAULT_SK));
    }
    @Bean("anyline.baidu.map.init.client")
    public BaiduMapClient instance(){
        return BaiduMapClient.getInstance();
    }
}
