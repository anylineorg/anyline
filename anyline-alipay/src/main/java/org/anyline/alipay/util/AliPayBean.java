package org.anyline.alipay.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.alipay.load.bean")
public class AliPayBean implements InitializingBean {

    @Value("${anyline.alipay.appId:}")
    private String APP_ID;
    @Value("${anyline.alipay.appPrivateKey:}")
    private String APP_PRIVATE_KEY;
    @Value("${anyline.alipay.platformPublicKey:}")
    private String PLATFORM_PUBLIC_KEY;
    @Value("${anyline.alipay.dataFormat:}")
    private String DATA_FORMAT;
    @Value("${anyline.alipay.encode:}")
    private String ENCODE;
    @Value("${anyline.alipay.signType:}")
    private String SIGN_TYPE;
    @Value("${anyline.alipay.returnUrl:}")
    private String RETURN_URL;
    @Value("${anyline.alipay.notifyUrl:}")
    private String NOTIFY_URL;

    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID = BasicUtil.evl(APP_ID, AlipayConfig.DEFAULT_APP_ID);
        if(BasicUtil.isEmpty(APP_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("APP_ID",BasicUtil.evl(APP_ID, AlipayConfig.DEFAULT_APP_ID));
        row.put("APP_PRIVATE_KEY",BasicUtil.evl(APP_PRIVATE_KEY, AlipayConfig.DEFAULT_APP_PRIVATE_KEY));
        row.put("PLATFORM_PUBLIC_KEY",BasicUtil.evl(PLATFORM_PUBLIC_KEY, AlipayConfig.DEFAULT_PLATFORM_PUBLIC_KEY));
        row.put("DATA_FORMAT", BasicUtil.evl(DATA_FORMAT, AlipayConfig.DEFAULT_DATA_FORMAT));
        row.put("ENCODE", BasicUtil.evl(ENCODE, AlipayConfig.DEFAULT_ENCODE));
        row.put("SIGN_TYPE", BasicUtil.evl(SIGN_TYPE, AlipayConfig.DEFAULT_SIGN_TYPE));
        row.put("DATA_FORMAT", BasicUtil.evl(DATA_FORMAT, AlipayConfig.DEFAULT_DATA_FORMAT));
        row.put("RETURN_URL", BasicUtil.evl(RETURN_URL, AlipayConfig.DEFAULT_RETURN_URL));
        row.put("NOTIFY_URL", BasicUtil.evl(NOTIFY_URL, AlipayConfig.DEFAULT_NOTIFY_URL));
        AlipayConfig.register(AlipayConfig.DEFAULT_INSTANCE_KEY, row);
    }
    @Bean("anyline.alipay.init.util")
    public AlipayUtil instance(){
        return AlipayUtil.getInstance();
    }
}
