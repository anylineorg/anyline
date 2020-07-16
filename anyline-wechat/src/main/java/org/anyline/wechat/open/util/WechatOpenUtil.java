package org.anyline.wechat.open.util;
 
import java.io.File; 
import java.util.HashMap; 
import java.util.Hashtable; 
import java.util.Map; 
 
import org.anyline.entity.DataRow; 
import org.anyline.net.HttpUtil; 
import org.anyline.net.SimpleHttpUtil; 
import org.anyline.util.BasicUtil; 
import org.anyline.util.BeanUtil; 
import org.anyline.util.ConfigTable;
import org.anyline.wechat.entity.*;
import org.anyline.wechat.util.WechatConfig;
import org.anyline.wechat.util.WechatUtil;
import org.apache.http.entity.StringEntity; 
import org.apache.http.impl.client.CloseableHttpClient; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class WechatOpenUtil {
	private static final Logger log = LoggerFactory.getLogger(WechatOpenUtil.class);
	private static Hashtable<String, WechatOpenUtil> instances = new Hashtable<String, WechatOpenUtil>();
	private WechatOpenConfig config;
 
	public WechatOpenUtil(WechatOpenConfig config){
		this.config = config; 
	} 
 
	public WechatOpenUtil(String key, DataRow config){
		WechatOpenConfig conf = WechatOpenConfig.parse(key, config);
		this.config = conf; 
		instances.put(key, this); 
	} 
 
	public static WechatOpenUtil reg(String key, DataRow config){
		WechatOpenConfig conf = WechatOpenConfig.parse(key, config);
		WechatOpenUtil util = new WechatOpenUtil(conf);
		instances.put(key, util); 
		return util; 
	} 
	 
	public static WechatOpenUtil getInstance(){
		return getInstance("default"); 
	} 
	public static WechatOpenUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		WechatOpenUtil util = instances.get(key);
		if(null == util){ 
			WechatOpenConfig config = WechatOpenConfig.getInstance(key);
			util = new WechatOpenUtil(config);
			instances.put(key, util); 
		} 
		return util; 
	} 
	public WechatOpenConfig getConfig(){
		return config; 
	} 


	public WechatAuthInfo getAuthInfo(String code){
		return WechatUtil.getAuthInfo(config, code);
	}
	public String getOpenId(String code){
		WechatAuthInfo info = getAuthInfo(code);
		if(null != info && info.isResult()){
			return info.getOpenid();
		}
		return null;
	}
	public WechatUserInfo getUserInfo(String openid){
		return WechatUtil.getUserInfo(config,openid);
	}
	public String getUnionId(String openid) {
		WechatUserInfo info = getUserInfo(openid);
		if (null != info && info.isResult()) {
			return info.getUnionid();
		}
		return null;
	}
} 
