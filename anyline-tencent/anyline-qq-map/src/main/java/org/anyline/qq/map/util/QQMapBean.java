package org.anyline.qq.map.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.QQ.map.load.bean")
public class QQMapBean implements InitializingBean {

    @Value("${anyline.qq.map.key:}")
    private String KEY		;
    @Value("${anyline.qq.map.secret:}")
    private String SECRET 	;


    @Override
    public void afterPropertiesSet()  {
        KEY = BasicUtil.evl(KEY, QQMapConfig.DEFAULT_KEY);
        if(BasicUtil.isEmpty(KEY)){
            return;
        }
        QQMapConfig.register(KEY, BasicUtil.evl(SECRET, QQMapConfig.DEFAULT_SECRET));
    }
    @Bean("anyline.qq.map.init.client")
    public QQMapClient instance(){
        return QQMapClient.getInstance();
    }
}
