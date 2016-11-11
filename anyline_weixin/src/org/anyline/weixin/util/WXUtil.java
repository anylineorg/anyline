package org.anyline.weixin.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.HttpUtil;
import org.anyline.util.SHA1Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WXUtil {

	private static Logger log = LoggerFactory.getLogger(WXUtil.class);
	private static DataSet accessTokens = new DataSet();
	private static DataSet jsapiTickets = new DataSet();

//	public static void main(String args[]){
//		String token = getAccessToken();
//		System.out.println(token); 
//		String ticket = getJsapiTicket();
//		System.out.println(token);
//		
//	}
	public static String getAccessToken(){
		return getAccessToken(WXConfig.APP_ID, WXConfig.APP_SECRECT);
	}
	public static String getAccessToken(String appid, String secret){
		String result = "";
		DataRow row = accessTokens.getRow("APP_ID", appid);
		if(null == row){
			row = createNewAccessToken(appid, secret);
		}else if(row.isExpire()){
			accessTokens.remove(row);
			row = createNewAccessToken(appid, secret);
		}
		if(null != row){
			result = row.getString("ACCESS_TOKEN");
		}
		return result;
	}
	private static DataRow createNewAccessToken(String appid, String secret){
		DataRow row = new DataRow();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret;
		String text = HttpUtil.get(url,"UTF-8").getText();
		JSONObject json = JSONObject.fromObject(text);
		row = new DataRow();
		if(json.has("access_token")){
			row.put("APP_ID", appid);
			row.put("ACCESS_TOKEN", json.getString("access_token"));
			row.setExpires(json.getInt("expires_in"));
		}else{
			return null;
		}
		accessTokens.addRow(row);
		return row;
	}
	
	public static String getJsapiTicket(){
		String result = "";
		DataRow row = jsapiTickets.getRow("APP_ID", WXConfig.APP_ID);
		String accessToken = getAccessToken();
		if(null == row){
			row = createNewJsapiTicket(accessToken);
		}else if(row.isExpire()){
			jsapiTickets.remove(row);
			row = createNewJsapiTicket(accessToken);
		}
		if(null != row){
			result = row.getString("TICKET");
		}
		return result;
	}
	public static DataRow createNewJsapiTicket(String accessToken){
		DataRow row = new DataRow();
		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi";
		String text = HttpUtil.get(url,"UTF-8").getText();
		JSONObject json = JSONObject.fromObject(text);
		if(json.has("ticket")){
			row.put("TICKET", json.getString("ticket"));
			row.setExpires(json.getInt("expires_in"));
		}else{
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
	public static String createJsapiSign(Map<String,String> params,String encode){
		String builder = "";
		SortedMap<String,String> sort = new TreeMap<String,String>(params);  
		Set es = sort.entrySet();
		Iterator it = es.iterator();
		while(it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			String k = (String)entry.getKey();
			Object v = entry.getValue();
			if(!"".equals(builder)){
				builder += "&";
			}
			builder += k + "=" + v;
		}
		return SHA1Util.sign(builder);
	}
	
	public static Map<String,String> createJsapiSign(String url){
		Map<String,String> params = new HashMap<String,String>();
		params.put("noncestr", BasicUtil.getRandomLowerString(32));
		params.put("jsapi_ticket", getJsapiTicket());
		params.put("timestamp", System.currentTimeMillis()+"");
		params.put("url", url);
		String sign = createJsapiSign(params, "UTF-8");
		params.put("signature", sign);
		params.put("appid", WXConfig.APP_ID);
		return params;
	}
	
	
	
}
