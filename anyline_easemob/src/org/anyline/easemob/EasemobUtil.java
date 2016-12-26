package org.anyline.easemob;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONObject;

import org.apache.http.HttpResponse;
import org.apache.http.util.ByteArrayBuffer;
import org.apache.log4j.Logger;

public class EasemobUtil {
	private static Logger log = Logger.getLogger(EasemobUtil.class);
	private static final String appkey = "1118161112115170#aisousuo";
	private static final String orgName = "1118161112115170";
	private static final String appName = "aisousuo";
	private static final String clientId= "YXA6x6A9oKpyEea9rcNB35LujQ";
	private static final String clisentSecret ="YXA6vW-waLkDUv3nSCilUFKxP2jl-wE";
	private static final String host = "https://a1.easemob.com";
	private static String access_token = null;
	private static long access_token_expires = 0;

	public static void main(String args[]){
		//delete("3317");
		
		//resetPassword("14803904","123456");
		reg("zhangsan","12311","张三三");
	}
	/**
	 * 注册用户
	 * @param user
	 * @param password
	 * @param nick 昵称
	 * @return
	 */
	public static boolean reg(String user, String password, String nick){
		boolean result = false;
		String url = "/"+orgName+"/"+appName+"/users";
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		String body = "{\"username\": \""+user+"\",\"password\": \""+password+"\",\"nickname\": \""+nick+"\"}";
		try {
			HttpResponse response = HttpUtils.doPost(host, url, "POST", headers, null, body);
			log.warn("[REG USER][RESULT:"+readResponse(response)+"]");
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
		String url = "/"+orgName+"/"+appName+"/users/"+user+"/password";
		String body= "{\"newpassword\":\""+password+"\"}";
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Authorization", "Bearer " + getAccessToken());
		try {
			HttpResponse response = HttpUtils.doPut(host, url, "PUT", headers, null, body);
			log.warn("[RESET PASSWOROD][RESULT:"+readResponse(response)+"]");
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
		try {
			HttpResponse response = HttpUtils.doDelete(host, "/"+orgName+"/"+appName+"/users/" + user, "DELETE", headers, null);
			log.warn("[DELETE USER][RESULT:"+readResponse(response)+"]");
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
		String body = "{\"grant_type\": \"client_credentials\",\"client_id\": \""+clientId+"\",\"client_secret\": \""+clisentSecret+"\"}";
		try {
			HttpResponse response = HttpUtils.doPost(host, "/"+orgName+"/"+appName+"/token", "POST", headers, null, body);
			JSONObject json = JSONObject.fromObject(readResponse(response));
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
	public static String readResponse(HttpResponse response) {
		InputStream is = null;
		try {
			is = response.getEntity().getContent();
		} catch (UnsupportedOperationException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		if (is == null) {
			return null;
		}
		ByteArrayBuffer bab = new ByteArrayBuffer(0);
		byte[] b = new byte[1024];
		int len = 0;
		try {
			while ((len = is.read(b)) != -1) {
				bab.append(b, 0, len);
			}
			return new String(bab.toByteArray(), "utf-8");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
