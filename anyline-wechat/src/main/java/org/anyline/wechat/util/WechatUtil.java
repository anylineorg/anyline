/*
 * Copyright 2015-2022 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.anyline.wechat.util;
 
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.anyline.wechat.entity.WechatAuthInfo;
import org.anyline.wechat.entity.WechatUserInfo;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
 
public class WechatUtil {
	protected static final Logger log = LoggerFactory.getLogger(WechatUtil.class);
	private static DataSet accessTokens = new DataSet();
	/** 
	 * 参数签名 
	 *  
	 * @param secret  secret
	 * @param params  params
	 * @return return
	 */ 
	public static String sign(String secret, Map<String, Object> params) { 
		String sign = ""; 
		sign = BeanUtil.map2string(params);
		sign += "&key=" + secret; 
		sign = MD5Util.crypto(sign).toUpperCase(); 
		return sign; 
	} 
	public static boolean validateSign(String secret, Map<String,Object> map){ 
		String sign = (String)map.get("sign"); 
		if(BasicUtil.isEmpty(sign)){ 
			return false; 
		} 
		map.remove("sign"); 
		String chkSign = sign(secret, map); 
		return chkSign.equals(sign); 
	} 
	public static boolean validateSign(String secret, String xml){ 
		return validateSign(secret,BeanUtil.xml2map(xml)); 
	} 
	/** 
	 * 获取RSA公钥 
	 * @param mch  mch
	 * @param apiSecret  apiSecret
	 * @param keyStoreFile  keyStoreFile
	 * @param keyStorePassword  keyStorePassword
	 * @return return
	 */ 
	public static String getPublicKey(String mch, String apiSecret, File keyStoreFile, String keyStorePassword) { 
		Map<String, Object> parameters = new HashMap<String, Object>(); 
		parameters.put("mch_id", mch); 
		parameters.put("nonce_str", BasicUtil.getRandomLowerString(20)); 
		parameters.put("sign_type", "MD5"); 
		String sign = WechatUtil.sign(apiSecret, parameters);
		parameters.put("sign", sign); 
		String xml = BeanUtil.map2xml(parameters); 
		CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword); 
		StringEntity reqEntity = new StringEntity(xml, "UTF-8"); 
		reqEntity.setContentType("application/x-www-form-urlencoded"); 
		String txt = HttpUtil.post(httpclient, WechatConfig.API_URL_GET_PUBLIC_SECRET, "UTF-8", reqEntity).getText();
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[获取RSA公钥][\n{}\n]",txt);
		}
		return txt;
	}





	public static String getAccessToken(WechatConfig config){
		if(BasicUtil.isNotEmpty(config.SERVER_WHITELIST)){
			try{
				String ip = InetAddress.getLocalHost().getHostAddress();
				if(!config.SERVER_WHITELIST.contains(ip)){
					log.warn("[白名单验证失败][白名单:{}][本机IP:{}]", config.SERVER_WHITELIST, ip);
					return null;
				}
			}catch (Exception e){
				log.warn("[白名单验证异常]");
			}
		}
		String result = "";
		DataRow row = accessTokens.getRow("APP_ID", config.APP_ID);
		if(null == row || row.isExpire()){
			accessTokens.remove(row);
			row = newAccessToken(config);
		}
		if(null != row){
			result = row.getString("ACCESS_TOKEN");
		}
		return result;
	}

	/**
	 * 新建access_token,
	 * 经常有多个应用使用同一个公众号，多个应用应该通过一个中心服务器创建access token
	 * 不应该每个应用单独创建access token
	 * @param config config
	 * @return DataRow
	 */
	private static DataRow newAccessToken(WechatConfig config){
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[CREATE NEW ACCESS TOKEN][appid:{}][secret:{}]",config.APP_ID, config.APP_SECRET);
		}
		String appid = config.APP_ID;
		String secret = config.APP_SECRET;
		DataRow row = new DataRow();
		String url = null;
		if(BasicUtil.isEmpty(config.ACCESS_TOKEN_SERVER)){
			url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret;
		}else{
			url = config.ACCESS_TOKEN_SERVER+ "?grant_type=client_credential&appid="+appid+"&secret="+secret;
		}
		String text = HttpUtil.post(url,"UTF-8").getText();
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[CREATE NEW ACCESS TOKEN][result:{}]",text);
		}
		DataRow json = DataRow.parseJson(text);
		row = new DataRow();
		if(null != json && json.containsKey("access_token")){
			row.put("APP_ID", appid);
			row.put("ACCESS_TOKEN", json.getString("access_token"));
			row.setExpires(json.getInt("expires_in", 0)*800);
			row.setExpires(1000*60*5); //5分钟内有效
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[CREATE NEW ACCESS TOKEN][ACCESS_TOKEN:{}]",row.getString("ACCESS_TOKEN"));
			}
		}else{
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[CREATE NEW ACCESS TOKEN][FAIL]");
			}
			return null;
		}
		accessTokens.addRow(row);
		return row;
	}
	/**
	 * 用户授权信息 主要包含openid
	 * @param config config
	 * @param code code
	 * @return AuthInfo
	 */
	public static WechatAuthInfo getAuthInfo(WechatConfig config, String code){
		WechatAuthInfo result = null;
		String url = WechatConfig.API_URL_GET_AUTH_INFO + "?appid="+config.APP_ID+"&secret="+config.APP_SECRET+"&code="+code+"&grant_type=authorization_code";
		String txt = HttpUtil.get(url).getText();
		log.warn("[get auth info][txt:{}]",txt);
		result = BeanUtil.json2oject(txt, WechatAuthInfo.class);
		if(BasicUtil.isNotEmpty(result.getOpenid())){
			result.setResult(true);
		}
		return result;
	}

	/**
	 * 用户详细信息 主要包括用户昵称 头像 unionid
	 * @param config config
	 * @param openid openid
	 * @return UserInfo
	 */
	public static WechatUserInfo getUserInfo(WechatConfig config, String openid){
		WechatUserInfo result = null;
		String url = WechatConfig.API_URL_GET_USER_INFO + "?access_token="+getAccessToken(config)+"&openid="+openid+"&lang=zh_CN";
		String txt = HttpUtil.get(url).getText();
		log.warn("[wechar get user info][result:{}]",txt);
		result = BeanUtil.json2oject(txt, WechatUserInfo.class);
		if(BasicUtil.isNotEmpty(result.getOpenid())){
			result.setResult(true);
		}
		return result;
	}

	public static void profit(){

    }
} 
