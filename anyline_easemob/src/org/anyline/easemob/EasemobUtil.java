package org.anyline.easemob;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpUtils;

import net.sf.json.JSONObject;

import org.anyline.util.BeanUtil;
import org.anyline.util.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.log4j.Logger;

public class EasemobUtil {
	private static Logger log = Logger.getLogger(EasemobUtil.class);
	private static final String orgName = EasemobConfig.ORG_NAME;
	private static final String appName = EasemobConfig.APP_NAME;
	private static final String clientId= EasemobConfig.CLIENT_ID;
	private static final String clientSecret = EasemobConfig.CLIENT_SECRET;
	private static final String host = "https://a1.easemob.com";
	private static String access_token = null;
	private static long access_token_expires = 0;
	/**
	 * 注册用户
	 * @param user
	 * @param password
	 * @param nick 昵称
	 * @return
	 */
	public static boolean reg(String user, String password, String nick){
		boolean result = false;
		String url = host + "/"+orgName+"/"+appName+"/users";
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		Map<String,String> map = new HashMap<String,String>();
		map.put("username", user);
		map.put("password", password);
		map.put("nickname", nick);
		try {
			HttpEntity entity = new StringEntity(BeanUtil.map2json(map));
			String txt = HttpClientUtil.post(HttpClientUtil.defaultClient(), headers, url, "UTF-8", entity).getText();
			log.warn("[REG USER][RESULT:"+txt+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
	public static boolean reg(String user, String password){
		return reg(user,password, user);
	}
	/**
	 * 修改密码
	 * @param user
	 * @param password
	 * @return
	 */
	public static boolean resetPassword(String user, String password){
		boolean result = false;
		String url = host + "/"+orgName+"/"+appName+"/users/"+user+"/password";
		Map<String,String> map = new HashMap<String,String>();
		map.put("newpassword", password);
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Authorization", "Bearer " + getAccessToken());
		try {
			String txt = HttpClientUtil.put(headers, url,"UTF-8", new StringEntity(BeanUtil.map2json(map))).getText();
			log.warn("[RESET PASSWOROD][RESULT:"+txt+"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
	/**
	 * 删除用户
	 * @param user
	 * @return
	 */
	public static boolean delete(String user){
		boolean result = false;
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Authorization", "Bearer " + getAccessToken());
		String url = host + "/"+orgName+"/"+appName+"/users/" + user;
		try {
			String txt = HttpClientUtil.delete(headers,url, "UTF-8").getText();
			log.warn("[DELETE USER][RESULT:"+ txt +"]");
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
	private static String getAccessToken(){
		String token = null;
		if(System.currentTimeMillis()/1000 > access_token_expires){
			token = createNewAccessToken();
		}else{
			token = access_token;
		}
		return token;
	}
	
	/**
	 * 创建新token 
	 * @return
	 */
	private static String createNewAccessToken(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		Map<String,String> map = new HashMap<String,String>();
		map.put("grant_type", "client_credentials");
		map.put("client_id", clientId);
		map.put("client_secret", clientSecret);
		try {
			String url = host + "/"+orgName+"/"+appName+"/token";
			String txt = HttpClientUtil.post(headers, url, "UTF-8", new StringEntity(BeanUtil.map2json(map))).getText();
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("access_token")){
				access_token = json.getString("access_token");
			}
			if(json.has("expires_in")){
				access_token_expires = System.currentTimeMillis()/1000 + json.getLong("expires_in");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return access_token;
	}
}
