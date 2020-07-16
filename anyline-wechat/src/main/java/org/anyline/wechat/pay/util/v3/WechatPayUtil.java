package org.anyline.wechat.pay.util.v3;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.wechat.entity.v3.WechatPrePayOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;

public class WechatPayUtil {
    protected static final Logger log = LoggerFactory.getLogger(WechatPayUtil.class);

    private WechatPayConfig config = null;

    private static Hashtable<String, WechatPayUtil> instances = new Hashtable<String, WechatPayUtil>();
    public static WechatPayUtil getInstance(){
        return getInstance("default");
    }
    public WechatPayUtil(WechatPayConfig config){
        this.config = config;
    }
    public WechatPayUtil(String key, DataRow config){
        WechatPayConfig conf = WechatPayConfig.parse(key, config);
        this.config = conf;
        instances.put(key, this);
    }
    public static WechatPayUtil reg(String key, DataRow config){
        WechatPayConfig conf = WechatPayConfig.reg(key, config);
        WechatPayUtil util = new WechatPayUtil(conf);
        instances.put(key, util);
        return util;
    }
    public static WechatPayUtil getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = "default";
        }
        WechatPayUtil util = instances.get(key);
        if(null == util){
            WechatPayConfig config = WechatPayConfig.getInstance(key);
            util = new WechatPayUtil(config);
            instances.put(key, util);
        }
        return util;
    }

    public WechatPayConfig getConfig() {
        return config;
    }

    public String transactions(WechatPrePayOrder order){
        String prepay_id = null;
        String url = "https://api.mch.weixin.qq.com/v3/pay/partner/transactions/jsapi";
        return prepay_id;
    }
}
