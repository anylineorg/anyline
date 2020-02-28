package org.anyline.wechat.wap.util;
 
import java.util.HashMap; 
import java.util.Hashtable; 
import java.util.Map; 
 
import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.wechat.entity.WechatAuthInfo;
import org.anyline.wechat.entity.WechatUserInfo;
import org.anyline.wechat.entity.WechatPrePayOrder;
import org.anyline.wechat.entity.WechatPrePayResult;
import org.anyline.wechat.util.WechatConfig;
import org.anyline.wechat.util.WechatUtil;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class WechatWapUtil {
	private static final Logger log = LoggerFactory.getLogger(WechatWapUtil.class);
	private static Hashtable<String, WechatWapUtil> instances = new Hashtable<String, WechatWapUtil>();
	private WechatWapConfig config;
	 
 
	public WechatWapUtil(WechatWapConfig config){
		this.config = config; 
	} 
	 
 
	public WechatWapUtil(String key, DataRow config){
		WechatWapConfig conf = WechatWapConfig.parse(key, config);
		this.config = conf; 
		instances.put(key, this); 
	} 
	public static WechatWapUtil getInstance(){
		return getInstance("default"); 
	} 
	public static WechatWapUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default"; 
		} 
		WechatWapUtil util = instances.get(key);
		if(null == util){ 
			WechatWapConfig config = WechatWapConfig.getInstance(key);
			util = new WechatWapUtil(config);
			instances.put(key, util); 
		} 
		return util; 
	} 
	public WechatWapConfig getConfig(){
		return config; 
	} 
	/** 
	 * 统一下单 
	 * @param order  order
	 * @return return
	 */ 
	public WechatPrePayResult unifiedorder(WechatPrePayOrder order) throws Exception{
		return WechatUtil.unifiedorder(config,WechatConfig.TRADE_TYPE.MWEB,order);
	} 
 
 
 
	/** 
	 * H5调起支付所需参数
     * @param prepayid 预支付id(由统一下单接口返回)
	 * @return return
	 */ 
	public DataRow pay(String prepayid){
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("appid", config.APP_ID); 
		params.put("partnerid", config.PAY_MCH_ID); 
		params.put("prepayid", prepayid); 
		params.put("package", "Sign=WXPay"); 
		params.put("noncestr", BasicUtil.getRandomUpperString(32)); 
		params.put("timestamp", System.currentTimeMillis()/1000+""); 
		String sign = WechatUtil.sign(config.PAY_API_SECRET,params);
		params.put("sign", sign); 
		DataRow row = new DataRow(params); 
		row.put("packagevalue", row.get("package")); 
		row.remove("package"); 
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[WAP调起微信支付][参数:{}]", row.toJSON());
		} 
		return row; 
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
