package org.anyline.weixin;

public class WXBasicConfig {
	public static String TRADE_TYPE_JSAPI 			= "JSAPI"																	; //支付方式
	public static String TRADE_TYPE_APP				= "APP"																		;
	public static String TRADE_TYPE_WAP				= "MWEB"																	;
	public static String UNIFIED_ORDER_URL 			= "https://api.mch.weixin.qq.com/pay/unifiedorder"							; //统一下单
	public static String REFUND_URL					= "https://api.mch.weixin.qq.com/secapi/pay/refund"							; //退款
	public static String SEND_TEMPLATE_MESSAGE_URL	= "https://api.weixin.qq.com/cgi-bin/message/template/send"					; //发送模板消息
	public static String AUTH_ACCESS_TOKEN_URL		= "https://api.weixin.qq.com/sns/oauth2/access_token"						; //oauth2.0授权
}
