package org.anyline.weixin;

import org.anyline.util.BasicConfig;

public class WXBasicConfig extends BasicConfig{
	//支付方式
	public static enum TRADE_TYPE{
		JSAPI			{public String getCode(){return "JSAPI";} 		public String getName(){return "公从号";}},
		APP				{public String getCode(){return "APP";} 		public String getName(){return "APP";}},
		NATIVE			{public String getCode(){return "NATIVE";} 		public String getName(){return "原生扫码";}},
		MICROPAY		{public String getCode(){return "MICROPAY";} 	public String getName(){return "刷卡";}},
		MWEB			{public String getCode(){return "MWEB";} 		public String getName(){return "WAP";}};
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
	//发送模板消息
	public final static String API_URL_SEND_TEMPLATE_MESSAGE	= "https://api.weixin.qq.com/cgi-bin/message/template/send";
	//oauth2.0授权
	public final static String API_URL_AUTH_ACCESS_TOKEN		= "https://api.weixin.qq.com/sns/oauth2/access_token"; 
	
	

}
