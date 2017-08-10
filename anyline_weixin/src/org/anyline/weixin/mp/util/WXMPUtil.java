package org.anyline.weixin.mp.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpUtil;
import org.anyline.util.SHA1Util;
import org.anyline.util.SimpleHttpUtil;
import org.anyline.weixin.WXMPConfig;
import org.anyline.weixin.mp.entity.WXMPPayTradeOrder;
import org.anyline.weixin.mp.entity.WXMPPayTradeResult;
import org.anyline.weixin.util.WXUtil;
import org.apache.log4j.Logger;


public class WXMPUtil {
	private static Logger log = Logger.getLogger(WXMPUtil.class);
	private DataSet accessTokens = new DataSet();
	private DataSet jsapiTickets = new DataSet();
	private WXMPConfig config = null;

	private static Hashtable<String,WXMPUtil> instances = new Hashtable<String,WXMPUtil>();
	public static WXMPUtil getInstance(){
		return getInstance("default");
	}
	public static WXMPUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		WXMPUtil util = instances.get(key);
		if(null == util){
			util = new WXMPUtil();
			WXMPConfig config = WXMPConfig.getInstance(key);
			util.config = config;
			instances.put(key, util);
		}
		return util;
	}
	
	public WXMPConfig getConfig() {
		return config;
	}

	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public WXMPPayTradeResult unifiedorder(WXMPPayTradeOrder order) {
		WXMPPayTradeResult result = null;
		order.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(order.getAppid())){
			order.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(order.getMch_id())){
			order.setMch_id(config.MCH_ID);
		}
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(config.NOTIFY_URL);
		}
		
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = WXUtil.paySign(config.API_SECRECT,map);
		map.put("sign", sign);
		if(ConfigTable.isDebug()){
			log.warn("统一下单SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("统一下单XML:" + xml);
		}
		String rtn = SimpleHttpUtil.post(WXMPConfig.UNIFIED_ORDER_URL, xml);

		if(ConfigTable.isDebug()){
			log.warn("统一下单RETURN:" + rtn);
		}
		result = BeanUtil.xml2object(rtn, WXMPPayTradeResult.class);

		if(ConfigTable.isDebug()){
			log.warn("统一下单PREPAY ID:" + result.getPrepay_id());
		}
		return result;
	}


	/**
	 * APP调起支付所需参数
	 * @return
	 */
	public DataRow jsapiParam(String prepayid){
		String timestamp = System.currentTimeMillis()/1000+"";
		String random = BasicUtil.getRandomLowerString(20);
		String pkg = "prepay_id="+prepayid;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("package", pkg);
		params.put("timeStamp", timestamp);
		params.put("appId", getConfig().APP_ID);
		params.put("nonceStr", random);
		params.put("signType", "MD5");
		String sign = WXUtil.paySign(getConfig().API_SECRECT, params);
		params.put("paySign", sign);
		
		DataRow row = new DataRow(params);
		if(ConfigTable.isDebug()){
			log.warn("APP调起微信支付参数:" + row.toJSON());
		}
		return row;
	}

	
	public String getAccessToken(){
		return getAccessToken(config.APP_ID, config.APP_SECRECT);
	}
	public String getAccessToken(String appid, String secret){
		String result = "";
		DataRow row = accessTokens.getRow("APP_ID", appid);
		if(null == row){
			row = newAccessToken(appid, secret);
		}else if(row.isExpire()){
			accessTokens.remove(row);
			row = newAccessToken(appid, secret);
		}
		if(null != row){
			result = row.getString("ACCESS_TOKEN");
		}
		return result;
	}
	private DataRow newAccessToken(String appid, String secret){
		if(ConfigTable.isDebug()){
			log.warn("[CREATE NEW ACCESS TOKEN][appid:"+appid+", secret:"+secret+"]");
		}
		DataRow row = new DataRow();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret;
		String text = HttpUtil.get(url,"UTF-8").getText();
		JSONObject json = JSONObject.fromObject(text);
		row = new DataRow();
		if(json.has("access_token")){
			row.put("APP_ID", appid);
			row.put("ACCESS_TOKEN", json.getString("access_token"));
			row.setExpires(json.getInt("expires_in"));
			if(ConfigTable.isDebug()){
				log.warn("[CREATE NEW ACCESS TOKEN][ACCESS_TOKEN:"+row.getString("ACCESS_TOKEN")+"]");
			}
		}else{
			if(ConfigTable.isDebug()){
				log.warn("[CREATE NEW ACCESS TOKEN][FAIL]");
			}
			return null;
		}
		accessTokens.addRow(row);
		return row;
	}
	
	public String getJsapiTicket(){
		String result = "";
		DataRow row = jsapiTickets.getRow("APP_ID", config.APP_ID);
		String accessToken = getAccessToken();
		if(null == row){
			row = newJsapiTicket(accessToken);
		}else if(row.isExpire()){
			jsapiTickets.remove(row);
			row = newJsapiTicket(accessToken);
		}
		if(null != row){
			result = row.getString("TICKET");
		}
		return result;
	}
	public DataRow newJsapiTicket(String accessToken){
		if(ConfigTable.isDebug()){
			log.warn("[CREATE NEW JSAPI TICKET][token:"+accessToken+"]");
		}
		DataRow row = new DataRow();
		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi";
		String text = HttpUtil.get(url,"UTF-8").getText();
		JSONObject json = JSONObject.fromObject(text);
		if(json.has("ticket")){
			row.put("TICKET", json.getString("ticket"));
			row.setExpires(json.getInt("expires_in"));
			if(ConfigTable.isDebug()){
				log.warn("[CREATE NEW JSAPI TICKET][TICKET:"+row.get("TICKET")+"]");
			}
		}else{
			log.warn("[CREATE NEW JSAPI TICKET][FAIL]");
			return null;
		}
		jsapiTickets.addRow(row);
		return row;
	}
	/**
	 * 参与签名的字段包括
	 * noncestr（随机字符串）, 
	 * jsapi_ticket
	 * timestamp（时间戳
	 * url（当前网页的URL，不包含#及其后面部分）
	 * 对所有待签名参数按照字段名的ASCII 码从小到大排序（字典序）后，
	 * 使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串string1。
	 * @param params
	 * @param encode
	 * @return
	 */
	public String jsapiSign(Map<String,Object> params){
		String sign = "";
		sign = BasicUtil.joinBySort(params);
		sign = SHA1Util.sign(sign);
		return sign;
	}
	
	public Map<String,Object> jsapiSign(String url){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("noncestr", BasicUtil.getRandomLowerString(32));
		params.put("jsapi_ticket", getJsapiTicket());
		params.put("timestamp", System.currentTimeMillis()/1000+"");
		params.put("url", url);
		String sign = jsapiSign(params);
		params.put("sign", sign);
		params.put("appid", config.APP_ID);
		return params;
	}
	public DataRow getOpenId(String code){
		DataRow row = new DataRow();
		String url ="https://api.weixin.qq.com/sns/oauth2/access_token?appid="+config.APP_ID+"&secret="+config.APP_SECRECT+"&code="+code+"&grant_type=authorization_code";
		String txt = HttpUtil.get(url);
		row = DataRow.parseJson(txt);
		return row;
	}
	public DataRow getUnionId(String code){
		return getOpenId(code);
	}
}
