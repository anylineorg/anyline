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
	public final static String UNIFIED_ORDER_URL 			= "https://api.mch.weixin.qq.com/pay/unifiedorder";
	//微信退款接口(POST)
	public final static String REFUND_URL 					= "https://api.mch.weixin.qq.com/secapi/pay/refund";
	//订单查询接口(POST)
	public final static String CHECK_ORDER_URL 				= "https://api.mch.weixin.qq.com/pay/orderquery";
	//关闭订单接口(POST)
	public final static String CLOSE_ORDER_URL 				= "https://api.mch.weixin.qq.com/pay/closeorder";
	//退款查询接口(POST)
	public final static String CHECK_REFUND_URL 			= "https://api.mch.weixin.qq.com/pay/refundquery";
	//对账单接口(POST)
	public final static String DOWNLOAD_BILL_URL 			= "https://api.mch.weixin.qq.com/pay/downloadbill";
	//短链接转换接口(POST)
	public final static String SHORT_URL 					= "https://api.mch.weixin.qq.com/tools/shorturl";
	//接口调用上报接口(POST)
	public final static String REPORT_URL 					= "https://api.mch.weixin.qq.com/payitil/report";
	//发送红包
	public final static String SEND_REDPACK_URL				= "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack"; 
	//发送模板消息
	public final static String SEND_TEMPLATE_MESSAGE_URL	= "https://api.weixin.qq.com/cgi-bin/message/template/send"; 
	//oauth2.0授权
	public final static String AUTH_ACCESS_TOKEN_URL		= "https://api.weixin.qq.com/sns/oauth2/access_token"; 
	
	

}
