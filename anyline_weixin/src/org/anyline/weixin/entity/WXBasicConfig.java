package org.anyline.weixin.entity;

public class WXBasicConfig {
	public static String TRADE_TYPE_JSAPI 	= "JSAPI";												//支付方式
	public static String TRADE_TYPE_APP		= "APP";
	public static String TRADE_TYPE_WAP		= "MWEB";
	public static String UNIFIED_ORDER_URL 	= "https://api.mch.weixin.qq.com/pay/unifiedorder";		//统一下单
	public static String REFUND_URL			= "https://api.mch.weixin.qq.com/secapi/pay/refund";	//退款
}
