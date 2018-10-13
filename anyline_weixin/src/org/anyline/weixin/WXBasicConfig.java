package org.anyline.weixin;

public class WXBasicConfig {
	//支付方式
	public static enum TRADE_TYPE{
		JSAPI			{public String getCode(){return "JSAPI";} 	public String getName(){return "公从号";}},
		APP				{public String getCode(){return "APP";} 	public String getName(){return "APP";}},
		MWEB			{public String getCode(){return "MWEB";} 	public String getName(){return "WAP";}};
		public abstract String getName();
		public abstract String getCode();
	}
	public static String UNIFIED_ORDER_URL 			= "https://api.mch.weixin.qq.com/pay/unifiedorder"							; //统一下单
	public static String REFUND_URL					= "https://api.mch.weixin.qq.com/secapi/pay/refund"							; //退款
	public static String SEND_REDPACK_URL			= "https://api.mch.weixin.qq.com/mmpaymkttransfers/sendredpack"				; //发送红包
	public static String SEND_TEMPLATE_MESSAGE_URL	= "https://api.weixin.qq.com/cgi-bin/message/template/send"					; //发送模板消息
	public static String AUTH_ACCESS_TOKEN_URL		= "https://api.weixin.qq.com/sns/oauth2/access_token"						; //oauth2.0授权
}
