package org.anyline.alipay.util;

import java.util.Hashtable;

import org.anyline.util.BasicUtil;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradeAppPayModel;
import com.alipay.api.request.AlipayTradeAppPayRequest;
import com.alipay.api.response.AlipayTradeAppPayResponse;

public class AlipayUtil {
	private AlipayClient client = null;
	private AlipayConfig config = null;
	private static Hashtable<String,AlipayUtil> instances = new Hashtable<String,AlipayUtil>();
	public static AlipayUtil getInstance(){
		return getInstance("default");
	}
	public static AlipayUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		AlipayUtil util = instances.get(key);
		if(null == util){
			util = new AlipayUtil();
			AlipayConfig config = AlipayConfig.getInstance(key);
			util.config = config;
			util.client = new DefaultAlipayClient(
					"https://openapi.alipay.com/gateway.do", 
					config.getString("APP_ID"),
					config.getString("APP_PRIVATE_KEY"), 
					config.getString("DATA_FORMAT"),
					config.getString("ENCODE"), 
					config.getString("ALIPAY_PUBLIC_KEY"),
					config.getString("SIGN_TYPE"));
			instances.put(key, util);
		}
		
		return util;
	}
	/**
	 * app支付
	 * 
	 * @param subject 支付标题
	 * @param body 支付明细
	 * @param price 支付价格(元)
	 * @param order 系统订单号
	 * @return
	 */
	public String createAppOrder(String subject, String body, String price, String order) {
		String result = "";
		AlipayTradeAppPayRequest request = new AlipayTradeAppPayRequest();
		AlipayTradeAppPayModel model = new AlipayTradeAppPayModel();
		model.setBody(body);
		model.setSubject(subject);
		model.setOutTradeNo(order);
		model.setTimeoutExpress("30m");
		model.setTotalAmount(price);
		request.setBizModel(model);
		request.setNotifyUrl(config.getString("NOTIFY_URL"));
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
	public String createHtmlOrder(String subject, String body, String price, String order) {
		String result = "";
		return result;
	}
}
