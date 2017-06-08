package org.anyline.weixin.mp.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpUtil;
import org.anyline.util.SHA1Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WXMPUtil {

	private static Logger log = LoggerFactory.getLogger(WXMPUtil.class);
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
}
