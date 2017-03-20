package org.anyline.alipay.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;

public class AliPayUtil {
	public static AlipayClient client = null;
	static {
		client = new DefaultAlipayClient(
				"https://openapi.alipay.com/gateway.do", AliPayConfig.APP_ID,
				AliPayConfig.APP_PRIVATE_KEY, AliPayConfig.DATA_FORMAT,
				AliPayConfig.ENCODE, AliPayConfig.ALIPAY_PUBLIC_KEY,
				AliPayConfig.SIGN_TYPE);
	}

	/**
	 * app支付
	 * 
	 * @param subject
	 *            支付标题
	 * @param body
	 *            支付明细
	 * @param price
	 *            支付价格(元)
	 * @param order
	 *            系统订单号
	 * @return
	 */
	public static String createAppOrder(String subject, String body,
			String price, String order) {
		String result = "";
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody(body);
		model.setSubject(subject);
		model.setOutTradeNo(order);
		model.setTimeoutExpress("30m");
		model.setTotalAmount(price);
		request.setBizModel(model);
		request.setNotifyUrl(AliPayConfig.NOTIFY_URL);
		try {
			AlipayTradeAppPayResponse response = client.sdkExecute(request);
			result = response.getBody();
		} catch (AlipayApiException e) {
			e.printStackTrace();
		}

		return result;
	}

	/**
	 * html支付
	 * 
	 * @param subject
	 * @param body
	 * @param price
	 * @param order
	 * @return
	 */
	public static String createHtmlOrder(String subject, String body,
			String price, String order) {
		String result = "";
		return result;
	}
}
