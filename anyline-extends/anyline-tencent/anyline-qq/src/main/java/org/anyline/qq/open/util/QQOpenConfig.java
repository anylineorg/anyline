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


package org.anyline.qq.open.util;

import org.anyline.entity.DataRow;
import org.anyline.qq.util.QQConfig;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;
 
 
public class QQOpenConfig extends QQConfig{
	public static String CONFIG_NAME = "anyline-qq-open.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<>();

	public static String DEFAULT_APP_ID 				= ""	; // AppID(应用ID)
	public static String DEFAULT_APP_KEY 				= ""	; // APPKEY(应用密钥)
	public static String DEFAULT_APP_SECRET 			= ""	; // AppSecret(应用密钥)
	public static String DEFAULT_SIGN_TYPE 				= ""	; // 签名加密方式
	public static String DEFAULT_SERVER_TOKEN 			= ""	; // 服务号的配置token

	public static String DEFAULT_PAY_API_SECRET 		= ""	; // 商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	public static String DEFAULT_PAY_MCH_ID 			= ""	; // 商家号
	public static String DEFAULT_PAY_NOTIFY_URL 		= ""	; // 支付统一接口的回调action
	public static String DEFAULT_PAY_CALLBACK_URL 		= ""	; // 支付成功支付后跳转的地址
	public static String DEFAULT_PAY_KEY_STORE_FILE		 = ""	; // 支付证书存放路径地址


	/** 
	 * 服务号相关信息 
	 */ 
	public String APP_ID 				= DEFAULT_APP_ID				; // AppID(应用ID)
	public String APP_KEY 				= DEFAULT_APP_KEY				; // APPKEY(应用密钥)
	public String APP_SECRET 			= DEFAULT_APP_SECRET			; // AppSecret(应用密钥)
	public String SIGN_TYPE 			= DEFAULT_SIGN_TYPE				; // 签名加密方式
	public String SERVER_TOKEN 			= DEFAULT_SERVER_TOKEN			; // 服务号的配置token
	 
	public String PAY_API_SECRET 		= DEFAULT_PAY_API_SECRET		; // 商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置
	public String PAY_MCH_ID 			= DEFAULT_PAY_MCH_ID			; // 商家号
	public String PAY_NOTIFY_URL 		= DEFAULT_PAY_NOTIFY_URL		; // 支付统一接口的回调action
	public String PAY_CALLBACK_URL 		= DEFAULT_PAY_CALLBACK_URL		; // 支付成功支付后跳转的地址
	public String PAY_KEY_STORE_FILE 	= DEFAULT_PAY_KEY_STORE_FILE	; // 支付证书存放路径地址


	public static Hashtable<String,AnylineConfig>getInstances(){
		return instances;
	}
	static{
		init(); 
		debug(); 
	} 
	public static void init() {
		// 加载配置文件
		load(); 
	} 
 
	public static QQOpenConfig getInstance(){
		return getInstance(DEFAULT_INSTANCE_KEY);
	} 
	public static QQOpenConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = DEFAULT_INSTANCE_KEY;
		} 
 
		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - QQOpenConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			// 重新加载
			load(); 
		} 
		return (QQOpenConfig)instances.get(key); 
	} 
	/** 
	 * 加载配置文件 
	 */ 
	private synchronized static void load() {
		load(instances, QQOpenConfig.class, CONFIG_NAME);
		QQOpenConfig.lastLoadTime = System.currentTimeMillis(); 
	} 
	private static void debug(){
	}
	public static QQOpenConfig register(String instance, DataRow row){
		QQOpenConfig config = parse(QQOpenConfig.class, instance, row, instances, compatibles);
		QQOpenUtil.getInstance(instance);
		return config;
	}
	public static QQOpenConfig register(DataRow row){
		return register(DEFAULT_INSTANCE_KEY, row);
	}
} 
