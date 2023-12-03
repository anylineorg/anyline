/*
 * Copyright 2006-2023 www.anyline.org
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
 */


package org.anyline.wechat.util;
 
import org.anyline.util.AnylineConfig;

public class WechatConfig extends AnylineConfig{
	// 登录获取身份信息类别
	public static enum SNSAPI_SCOPE{
		BASE		{public String getCode(){return "snsapi_base";} 	public String getName(){return "基础信息";}},
		USERINFO	{public String getCode(){return "snsapi_userinfo";} public String getName(){return "详细信息";}};
		public abstract String getName();
		public abstract String getCode();
	};
	public static enum BANK{
		工商银行	{public String getCode(){return "1002";}public String getName(){return "工商银行";}},
		农业银行	{public String getCode(){return "1005";}public String getName(){return "农业银行";}},
		中国银行	{public String getCode(){return "1026";}public String getName(){return "中国银行";}},
		建设银行	{public String getCode(){return "1003";}public String getName(){return "建设银行";}},
		招商银行	{public String getCode(){return "1001";}public String getName(){return "招商银行";}},
		邮储银行	{public String getCode(){return "1066";}public String getName(){return "邮储银行";}},
		交通银行	{public String getCode(){return "1020";}public String getName(){return "交通银行";}},
		浦发银行	{public String getCode(){return "1004";}public String getName(){return "浦发银行";}},
		民生银行	{public String getCode(){return "1006";}public String getName(){return "民生银行";}},
		兴业银行	{public String getCode(){return "1009";}public String getName(){return "兴业银行";}},
		平安银行	{public String getCode(){return "1010";}public String getName(){return "平安银行";}},
		中信银行	{public String getCode(){return "1021";}public String getName(){return "中信银行";}},
		华夏银行	{public String getCode(){return "1025";}public String getName(){return "华夏银行";}},
		广发银行	{public String getCode(){return "1027";}public String getName(){return "广发银行";}},
		光大银行	{public String getCode(){return "1022";}public String getName(){return "光大银行";}},
		北京银行	{public String getCode(){return "1032";}public String getName(){return "北京银行";}},
		宁波银行	{public String getCode(){return "1056";}public String getName(){return "宁波银行";}};
		public abstract String getName();
		public abstract String getCode();
	}

	protected static String[] compatibles = {
			"PAY_API_SECRET:API_SECRET"
			,"PAY_MCH_ID:MCH_ID"
			,"PAY_NOTIFY_URL:PAY_NOTIFY"
			,"PAY_KEY_STORE_FILE:KEY_STORE_FILE"
			,"PAY_KEY_STORE_PASSWORD:KEY_STORE_PASSWORD"};
	// 获取RSA公钥
	public final static String API_URL_GET_PUBLIC_SECRET		= "https://fraud.mch.weixin.qq.com/risk/getpublickey";
	// 发送模板消息
	public final static String API_URL_SEND_TEMPLATE_MESSAGE	= "https://api.weixin.qq.com/cgi-bin/message/template/send";
	// oauth2.0授权
	public final static String API_URL_GET_AUTH_INFO			= "https://api.weixin.qq.com/sns/oauth2/access_token";
	// 用户基本信息
	public final static String API_URL_GET_USER_INFO			= "https://api.weixin.qq.com/cgi-bin/user/info";//?access_token=ACCESS_TOKEN&openid=OPENID&lang=zh_CN

	public final static String URL_OAUTH						= "https://open.weixin.qq.com/connect/oauth2/authorize";

	public static String DEFAULT_APP_ID 						= "" ; // AppID(应用ID)
	public static String DEFAULT_APP_SECRET 					= "" ; // AppSecret(应用密钥)
	public static String DEFAULT_SIGN_TYPE 						= "" ; // 签名加密方式
	public static String DEFAULT_SERVER_TOKEN 					= "" ; // 服务号的配置token
	public static String DEFAULT_OAUTH_REDIRECT_URL 			= "" ; // oauth2授权时回调action
	public static String DEFAULT_WEB_SERVER 					= "" ;
	public static String DEFAULT_ACCESS_TOKEN_SERVER			= "" ;
	public static String DEFAULT_SERVER_WHITELIST			    = null; // 白名单IP(如果设置了并且当前服务器不在白名单内,则跳过需要白名单才能调用的接口)

	public String APP_ID 						= DEFAULT_APP_ID 				; // AppID(应用ID)
	public String APP_SECRET 					= DEFAULT_APP_SECRET 			; // AppSecret(应用密钥)
	public String SIGN_TYPE 					= DEFAULT_SIGN_TYPE 			; // 签名加密方式
	public String SERVER_TOKEN 					= DEFAULT_SERVER_TOKEN 			; // 服务号的配置token
	public String OAUTH_REDIRECT_URL 			= DEFAULT_OAUTH_REDIRECT_URL	; // oauth2授权时回调action
	public String WEB_SERVER 					= DEFAULT_WEB_SERVER 			;
	public String ACCESS_TOKEN_SERVER			= DEFAULT_ACCESS_TOKEN_SERVER 	;
	public String SERVER_WHITELIST			    = DEFAULT_SERVER_WHITELIST		; // 白名单IP(如果设置了并且当前服务器不在白名单内,则跳过需要白名单才能调用的接口)


} 
