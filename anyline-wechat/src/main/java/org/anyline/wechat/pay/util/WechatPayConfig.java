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
package org.anyline.wechat.pay.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.AnylineConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;

import java.util.Hashtable;


public class WechatPayConfig extends AnylineConfig{
	public static String CONFIG_NAME = "anyline-wechat-pay.xml";
	private static Hashtable<String,AnylineConfig> instances = new Hashtable<String,AnylineConfig>();
	public String MCH_ID 					= "" ; //商户号
	public String SP_MCHID 					= "" ; //服务商商户号(服务商模式)
	public String SUB_MCHID 				= "" ; //子商户商户号(服务商模式)
	public String API_SECRET 				= "" ; //微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->API密钥设置
	public String API_SECRET_V3				= "" ; //微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->APIv3密钥设置
	public String MCH_PRIVATE_SECRET_FILE	= "" ; //商户API私钥(保存在apiclient_key.pem也可以通过p12导出)
	public String CERTIFICATE_SERIAL        = "" ; //证书序号 微信商家平台(pay.weixin.qq.com)-->账户设置-->API安全-->API证书-->查看证书
	public String KEY_STORE_FILE 			= "" ; //证书文件
	public String KEY_STORE_PASSWORD 		= "" ; //证书密码
	public String NOTIFY_URL				= "" ; //微信支付统一接口的回调action
	public String CALLBACK_URL 				= "" ; //微信支付成功支付后跳转的地址
	public String BANK_RSA_PUBLIC_KEY_FILE 	= "" ;

	/**
	 * 微信支付接口地址
	 */
	//微信支付统一接口(POST)
	public final static String API_URL_UNIFIED_ORDER 			= "https://api.mch.weixin.qq.com/pay/unifiedorder";
	//微信退款接口(POST)
	public final static String API_URL_REFUND 					= "https://api.mch.weixin.qq.com/secapi/pay/refund";
	//订单查询接口(POST)
	public final static String API_URL_CHECK_ORDER 				= "https://api.mch.weixin.qq.com/pay/orderquery";
	//关闭订单接口(POST)
	public final static String API_URL_CLOSE_ORDER 				= "https://api.mch.weixin.qq.com/pay/closeorder";
	//退款查询接口(POST)
	public final static String API_URL_CHECK_REFUND 			= "https://api.mch.weixin.qq.com/pay/refundquery";
	//对账单接口(POST)
	public final static String API_URL_DOWNLOAD_BILL 			= "https://api.mch.weixin.qq.com/pay/downloadbill";
	//短链接转换接口(POST)
	public final static String API_URL_SHORT 					= "https://api.mch.weixin.qq.com/tools/shorturl";
	//接口调用上报接口(POST)
	public final static String API_URL_REPORT 					= "https://api.mch.weixin.qq.com/payitil/report";
	//发送红包
	public final static String API_URL_SEND_REDPACK				= "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack";
	//发送裂变红包
	public final static String API_URL_SEND_GROUP_REDPACK		= "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendgroupredpack";
	//付款到微信钱包
	public final static String API_URL_COMPANY_TRANSFER			= "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers";
	//付款到银行卡
	public final static String API_URL_COMPANY_TRANSFER_BANK	= "https://api.mch.weixin.qq.com/mmpaysptrans/pay_bank";

	//支付方式
	public static enum TRADE_TYPE{
		JSAPI			{public String getCode(){return "JSAPI";} 		public String getApi(){return "jsapi";} 	public String getName(){return "公从号";}},
		APP				{public String getCode(){return "APP";}  		public String getApi(){return "app";}		public String getName(){return "APP";}},
		NATIVE			{public String getCode(){return "NATIVE";} 		public String getApi(){return "native";} 	public String getName(){return "原生扫码";}},
		MICROPAY		{public String getCode(){return "MICROPAY";}  	public String getApi(){return "";}			public String getName(){return "刷卡";}},
		MWEB			{public String getCode(){return "MWEB";}  		public String getApi(){return "h5";}		public String getName(){return "WAP";}};
		public abstract String getName();
		public abstract String getCode();
		public abstract String getApi();
	};

	//支付方式
	public static enum URL3{
		UNIFIED_ORDER_JSPAI	        {public String getCode(){return "https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi";} public String getName(){return "直连模式JSAPI下单";}},
		UNIFIED_ORDER_PARTNER_JSPAI	{public String getCode(){return "https://api.mch.weixin.qq.com/v3/pay/partner/transactions/jsapi";} public String getName(){return "服务商模式JSAPI下单";}},
		APP				{public String getCode(){return "APP";}  		public String getApi(){return "app";}		public String getName(){return "APP";}},
		NATIVE			{public String getCode(){return "NATIVE";} 		public String getApi(){return "native";} 	public String getName(){return "原生扫码";}},
		MICROPAY		{public String getCode(){return "MICROPAY";}  	public String getApi(){return "";}			public String getName(){return "刷卡";}},
		MWEB			{public String getCode(){return "MWEB";}  		public String getApi(){return "h5";}		public String getName(){return "WAP";}};
		public abstract String getName();
		public abstract String getCode();
	};
	static{
		init();
		debug();
	}
	/**
	 * 解析配置文件内容
	 * @param content 配置文件内容
	 */
	public static void parse(String content){
		parse(WechatPayConfig.class, content, instances ,compatibles);
	}
	/**
	 * 初始化默认配置文件
	 */
	public static void init() {
		//加载配置文件
		load();
	}
	public static WechatPayConfig getInstance(){
		return getInstance("default");
	}
	public static WechatPayConfig getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}

		if(ConfigTable.getReload() > 0 && (System.currentTimeMillis() - WechatPayConfig.lastLoadTime)/1000 > ConfigTable.getReload() ){
			//重新加载
			load();
		}
		return (WechatPayConfig)instances.get(key);
	}

	public static WechatPayConfig reg(String key, DataRow row){
		return parse(WechatPayConfig.class, key, row, instances,compatibles);
	}
	public static WechatPayConfig parse(String key, DataRow row){
		return parse(WechatPayConfig.class, key, row, instances,compatibles);
	}
	public static Hashtable<String,AnylineConfig> parse(String column, DataSet set){
		for(DataRow row:set){
			String key = row.getString(column);
			parse(key, row);
		}
		return instances;
	}
	/**
	 * 加载配置文件
	 * 首先加载anyline-config.xml
	 * 然后加载anyline开头的xml文件并覆盖先加载的配置
	 */
	private synchronized static void load() {
		load(instances, WechatPayConfig.class,CONFIG_NAME ,compatibles);
		WechatPayConfig.lastLoadTime = System.currentTimeMillis();
	}
	private static void debug(){
	}
}
