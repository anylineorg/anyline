package org.anyline.alipay.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class AliPayUtil {

	public static String createAppOrder(String subject, String body, String price, String order) {
		String result = "partner=" + "\"" + AliPayConfig.PARTNER + "\""			; // 签约合作者身份ID
		result += "&seller_id=" + "\"" + AliPayConfig.SELLER_ID + "\""			; // 签约卖家支付宝账号
		result += "&out_trade_no=" + "\"" + order + "\""						; // 商家网站唯一订单号
		result += "&subject=" + "\"" + subject + "\""							; // 商品名称
		result += "&body=" + "\"" + body + "\""									; // 商品详情
		result += "&total_fee=" + "\"" + price + "\""							; // 商品金额
		result += "&notify_url=" + "\"" + AliPayConfig.NOTIFY_URL + "\""		; // 服务器异步通知页面路径
		result += "&service=\"mobile.securitypay.pay\""							; // 服务接口名称， 固定值
		result += "&payment_type=\"1\""											; // 支付类型， 固定值
		result += "&_input_charset=\"utf-8\""									; // 参数编码， 固定值
		result += "&it_b_pay=\"30m\""											; // 有效支付时间
		result += "&return_url=\""+AliPayConfig.CALLBACK_URL+"\""				; // 支付宝处理完请求后，当前页面跳转到商家指定页面的路径，可空
		String sign = SignUtils.sign(result, AliPayConfig.APP_PRIVATE_KEY);
		try {
			sign = URLEncoder.encode(sign, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		result = result + "&sign=\"" + sign + "\"&sign_type=\"RSA\"";
		return result;
	}
	public static String createHtmlOrder(String subject, String body, String price, String order){
		String result ="";
		return result;
	}

}
