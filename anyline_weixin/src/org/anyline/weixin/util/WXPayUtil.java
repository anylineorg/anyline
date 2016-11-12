package org.anyline.weixin.util;

import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SimpleHttpUtil;
import org.anyline.weixin.entity.PayOrder;
import org.anyline.weixin.entity.PayOrderResult;
import org.apache.log4j.Logger;

public class WXPayUtil {
	private static Logger log = Logger.getLogger(WXPayUtil.class);
	
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public static PayOrderResult unifiedorder(PayOrder order) {
		PayOrderResult result = null;
		order.setNonce_str(BasicUtil.getRandomString(20));
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
		String rtn = SimpleHttpUtil.post(WXConfig.UNIFIED_ORDER_URL, xml);

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
