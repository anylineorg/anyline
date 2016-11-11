package org.anyline.weixin.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.anyline.util.SimpleHttpUtil;
import org.apache.log4j.Logger;

public class WXPayUtil {
	private static Logger log = Logger.getLogger(WXPayUtil.class);
	public static void main(String args[]) {
//		String nonce_str = BasicUtil.getRandomLowerString(20);
//		String out_trade_no = BasicUtil.getRandomLowerString(20);
//
//
//		PayOrder order = new PayOrder();
//		order.setNonce_str(nonce_str);
//		order.setBody("手机1065");
//		order.setOut_trade_no(out_trade_no);
//		order.setTotal_fee("1");
//		order.setSpbill_create_ip("60.58.123.25");
//		order.setTrade_type("JSAPI");
//		order.setOpenid("oFQdkwbBHpQG60AuwSwchDonYsXw");
//		PayOrderResult result = unifiedorder(order);
//		System.out.println(result.getPrepay_id());
	}
	
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public static PayOrderResult unifiedorder(PayOrder order) {
		PayOrderResult result = null;
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = sign(map);
		map.put("sign", sign);
		if(ConfigTable.isDebug()){
			log.warn("统一下单SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("统一下单XML:" + xml);
		}
		System.out.println("xml:"+xml);
		String rtn = SimpleHttpUtil.post("https://api.mch.weixin.qq.com/pay/unifiedorder", xml);

		if(ConfigTable.isDebug()){
			log.warn("统一下单RETURN:" + rtn);
		}
		result = BeanUtil.xml2object(rtn, PayOrderResult.class);

		if(ConfigTable.isDebug()){
			log.warn("统一下单PREPAY_ID:" + result.getPrepay_id());
		}
		return result;
	}


	/**
	 * 签名
	 * 
	 * @param params
	 * @return
	 */
	private static String sign(Map<String, Object> params) {
		String sign = "";
		SortedMap<String, Object> sort = new TreeMap<String, Object>(params);
		Set es = sort.entrySet();
		Iterator it = es.iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String k = (String) entry.getKey();
			Object v = entry.getValue();
			if ("".equals(v)) {
				params.remove(k);
				continue;
			}
			if (!"".equals(sign)) {
				sign += "&";
			}
			sign += k + "=" + v;
		}
		sign += "&key=" + WXConfig.API_SECRECT;
		sign = MD5Util.crypto(sign).toUpperCase();
		return sign;
	}
}
