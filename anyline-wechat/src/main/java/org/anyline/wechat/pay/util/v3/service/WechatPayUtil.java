package org.anyline.wechat.pay.util.v3.service;

import org.anyline.entity.DataRow;
import org.anyline.net.HttpResult;
import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.wechat.entity.v3.WechatPrePayOrder;
import org.anyline.wechat.pay.util.WechatPayConfig;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

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
            if(null != config) {
                util = new WechatPayUtil(config);
                instances.put(key, util);
            }
        }
        return util;
    }

    public WechatPayConfig getConfig() {
        return config;
    }

    public String transactions(WechatPrePayOrder order){
        String prepay_id = null;
        String url = "https://api.mch.weixin.qq.com/v3/pay/partner/transactions/jsapi";
        api(url,BeanUtil.object2json(order));
        return prepay_id;
    }
    private String[] api(String url, String json){
        DataRow row = new DataRow();
        Map<String,String> headers = new HashMap<String,String>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        Map<String,Object> params = new HashMap<String,Object>();
        HttpResult result = HttpUtil.post(headers, url, "UTF-8", new StringEntity(BeanUtil.map2json(params), "UTF-8"));
        headers = result.getHeaders();
        String requestId = headers.get("Request-ID");
        System.out.print("txt:"+result.getText());
        return new String[]{result.getStatus()+"",requestId};
    }
}
