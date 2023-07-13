package org.anyline.qq.mp.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.qq.mp.load.bean")
public class QQMPBean implements InitializingBean {

    @Value("${anyline.qq.mp.app:}")
    private String APP_ID			 	; // AppID(应用ID)
    @Value("${anyline.qq.mp.key:}")
    private String API_KEY 			 	; // APPKEY(应用密钥)
    @Value("${anyline.qq.mp.redirect:}")
    private String OAUTH_REDIRECT_URL 	; // 登录成功回调URL
    @Override
    public void afterPropertiesSet()  {
        APP_ID = BasicUtil.evl(APP_ID, QQMPConfig.DEFAULT_APP_ID);
        if(BasicUtil.isEmpty(APP_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("APP_ID", BasicUtil.evl(APP_ID, QQMPConfig.DEFAULT_APP_ID));
        row.put("API_KEY", BasicUtil.evl(API_KEY, QQMPConfig.DEFAULT_API_KEY));
        row.put("OAUTH_REDIRECT_URL", BasicUtil.evl(OAUTH_REDIRECT_URL, QQMPConfig.DEFAULT_OAUTH_REDIRECT_URL));
        QQMPConfig.register(row);
    }

    @Bean("anyline.qq.mp.init.bean")
    public QQMPUtil instance(){
        return QQMPUtil.getInstance();
    }
}
