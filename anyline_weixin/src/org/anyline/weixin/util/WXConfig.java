package org.anyline.weixin.util;

import org.anyline.util.BasicConfig;

public class WXConfig extends BasicConfig{
	//支付方式
	public static enum TRADE_TYPE{
		JSAPI			{public String getCode(){return "JSAPI";} 		public String getName(){return "公从号";}},
		APP				{public String getCode(){return "APP";} 		public String getName(){return "APP";}},
		NATIVE			{public String getCode(){return "NATIVE";} 		public String getName(){return "原生扫码";}},
		MICROPAY		{public String getCode(){return "MICROPAY";} 	public String getName(){return "刷卡";}},
		MWEB			{public String getCode(){return "MWEB";} 		public String getName(){return "WAP";}};
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
	//获取RSA公钥
	public final static String API_URL_GET_PUBLIC_SECRET		= "https://fraud.mch.weixin.qq.com/risk/getpublickey";
	//发送模板消息
	public final static String API_URL_SEND_TEMPLATE_MESSAGE	= "https://api.weixin.qq.com/cgi-bin/message/template/send";
	//oauth2.0授权
	public final static String API_URL_AUTH_ACCESS_TOKEN		= "https://api.weixin.qq.com/sns/oauth2/access_token"; 
	
	

}
