package org.anyline.amap.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.ampa.load.bean")
public class AmapBean implements InitializingBean {

    @Value("${anyline.amap.host:}")
    private String HOST		;
    @Value("${anyline.amap.key:}")
    private String KEY		;
    @Value("${anyline.amap.secret:}")
    private String SECRET 	;
    @Value("${anyline.amap.table:}")
    private String TABLE 	;

    @Override
    public void afterPropertiesSet()  {
        KEY = BasicUtil.evl(KEY, AmapConfig.DEFAULT_KEY);
        if(BasicUtil.isEmpty(KEY)){
            return;
        }
        AmapConfig config = AmapConfig.register(KEY, BasicUtil.evl(SECRET, AmapConfig.DEFAULT_SECRET)
                , BasicUtil.evl(TABLE, AmapConfig.DEFAULT_TABLE));
        config.HOST = this.HOST;
    }
    @Bean("anyline.amap.init.client")
    public AmapClient instance(){
        return AmapClient.getInstance();
    }
}
