package org.anyline.weixin.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.anyline.util.BasicUtil;
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
//		String src = "appid=wx67bc8dd44fdb345f&body=秒降堂-在线支付&mch_id=1385679002&nonce_str=tgtbnynecerlgisvmnyo&notify_url=http://www.3rwz.com/pay/ntf/wx&openid=oFQdkwbBHpQG60AuwSwchDonYsXw&out_trade_no=TC0000000192_OI0000000043&spbill_create_ip=27.219.60.170&total_fee=0&trade_type=JSAPI&key=jE74fQqltm4PmIpsBh2ScJN8mqiwvZj2";
//		System.out.println(MD5Util.crypto(src).toUpperCase());
		PayOrder payOrder = new PayOrder();
		String nonce_str = BasicUtil.getRandomLowerString(20);
		payOrder.setNonce_str(nonce_str);
		payOrder.setOut_trade_no("TC0000000181_"+BasicUtil.getRandomNumberString(10));
		payOrder.setBody("秒降堂-在线支付");
		payOrder.setTotal_fee("1");
		payOrder.setSpbill_create_ip("27.219.60.170");
		payOrder.setTrade_type("JSAPI");
		payOrder.setOpenid("oFQdkwbBHpQG60AuwSwchDonYsXw");
		PayOrderResult result = WXPayUtil.unifiedorder(payOrder);
		System.out.println(result.getErr_code_des());
	}
	
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public static PayOrderResult unifiedorder(PayOrder order) {
		PayOrderResult result = null;
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = WXUtil.sign(map);
		map.put("sign", sign);
		if(ConfigTable.isDebug()){
			log.warn("统一下单SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("统一下单XML:" + xml);
		}
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


	
}
