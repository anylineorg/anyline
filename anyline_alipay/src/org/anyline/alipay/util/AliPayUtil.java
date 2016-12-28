package org.anyline.alipay.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AliPayUtil {

	/**
	 * app支付
	 * @param subject 支付标题
	 * @param body 支付明细
	 * @param price 支付价格
	 * @param order 系统订单号
	 * @return
	 */
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
	/**
	 * html支付
	 * @param subject
	 * @param body
	 * @param price
	 * @param order
	 * @return
	 */
	public static String createHtmlOrder(String subject, String body, String price, String order){
		String result ="";
		Map<String,String> params = createDefaultParam();
		params.put("out_trade_no", order);
		params.put("subject", subject);
		params.put("total_fee", price);
		params.put("body", body);	
		params = signParam(params);
		
		List<String> keys = new ArrayList<String>(params.keySet());
        StringBuffer sbHtml = new StringBuffer();
        sbHtml.append("<form id=\"alipaysubmit\" name=\"alipaysubmit\" action=\"" + AliPayConfig.ALIPAY_GATEWAY_NEW + "_input_charset=" + AliPayConfig.INPUT_CHARSET + "\" method=\"POST\">");
        for (int i = 0; i < keys.size(); i++) {
            String name = (String) keys.get(i);
            String value = (String) params.get(name);

            sbHtml.append("<input type=\"hidden\" name=\"" + name + "\" value=\"" + value + "\"/>");
        }
        sbHtml.append("<input type=\"submit\" value=\"Pay\" style=\"display:none;\"></form>");
        sbHtml.append("<script>document.forms['alipaysubmit'].submit();</script>");

		return result;
	}
	 private static Map<String, String> signParam(Map<String, String> params) {
	        //除去数组中的空值和签名参数
	        Map<String, String> result = AlipayCore.paraFilter(params);
	        //生成签名结果
	        String sign = buildRequestMysign(result);
	        //签名结果与签名方式加入请求提交参数组中
	        result.put("sign", sign);
	        params.put("sign", sign);
	        result.put("sign_type", AliPayConfig.SIGN_TYPE);

	        return result;
	    }
	  /**
	     * 生成签名结果
	     * @param sPara 要签名的数组
	     * @return 签名结果字符串
	     */
		public static String buildRequestMysign(Map<String, String> sPara) {
	    	String prestr = AlipayCore.createLinkString(sPara); //把数组所有元素，按照“参数=参数值”的模式用“&”字符拼接成字符串
	        String mysign = "";
	        if(AliPayConfig.SIGN_TYPE.equalsIgnoreCase("MD5") ) {
	        	mysign = MD5.sign(prestr, AliPayConfig.KEY, AliPayConfig.INPUT_CHARSET);
	        }
	        return mysign;
	    }
	/**
	 * 封装默认参数
	 * @return
	 */
	public static Map<String,String> createDefaultParam(){
		return createDefaultParam(null);
	}
	public static Map<String,String> createDefaultParam(Map<String,String> params){
		if(null == params){
			params = new HashMap<String,String>();
		}
    	params.put("partner", AliPayConfig.PARTNER);
    	params.put("seller_id", AliPayConfig.SELLER_ID);
    	params.put("_input_charset", AliPayConfig.INPUT_CHARSET);
    	params.put("payment_type", AliPayConfig.PAYMENT_TYPE);
    	params.put("notify_url", AliPayConfig.NOTIFY_URL);
    	params.put("return_url", AliPayConfig.CALLBACK_URL);
    	params.put("anti_phishing_key", AliPayConfig.ANTI_PHISHING_KEY);
    	params.put("exter_invoke_ip", AliPayConfig.EXTER_INVOKE_IP);
    	return params;
	}
}
