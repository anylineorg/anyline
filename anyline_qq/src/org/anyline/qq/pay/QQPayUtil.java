package org.anyline.qq.pay;

import java.util.Hashtable;
import java.util.Map;

import org.anyline.qq.pay.entity.QQPayOrder;
import org.anyline.qq.pay.entity.QQPayOrderResult;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.anyline.util.SimpleHttpUtil;
import org.apache.log4j.Logger;

public class QQPayUtil {
	private static Logger log = Logger.getLogger(QQPayUtil.class);
	private static Hashtable<String,QQPayUtil> instances = new Hashtable<String,QQPayUtil>();
	private QQPayConfig config = null;
	public static QQPayUtil getInstance(){
		return getInstance("default");
	}
	public static QQPayUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		QQPayUtil util = instances.get(key);
		if(null == util){
			util = new QQPayUtil();
			util.config = QQPayConfig.getInstance(key);
			instances.put(key, util);
		}
		return util;
	}
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public QQPayOrderResult unifiedorder(QQPayOrder order) {
		QQPayOrderResult result = null;
		order.setNonce_str(BasicUtil.getRandomString(20));
		if(BasicUtil.isEmpty(order.getAppid())){
			order.setAppid(config.getString("APP_ID"));
		}
		if(BasicUtil.isEmpty(order.getMch_id())){
			order.setMch_id(config.getString("MCH_ID"));
		}
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(config.getString("PAY_NOTIFY_URL"));
		}
		
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
		String rtn = SimpleHttpUtil.post(QQPayConfig.UNIFIED_ORDER_URL, xml);

		if(ConfigTable.isDebug()){
			log.warn("统一下单RETURN:" + rtn);
		}
		result = BeanUtil.xml2object(rtn, QQPayOrderResult.class);

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
	public String sign(Map<String, Object> params) {
		String sign = "";
		sign = BasicUtil.joinBySort(params);
		sign += "&key=" + config.getString("API_SECRECT");
		sign = MD5Util.crypto(sign).toUpperCase();
		return sign;
	}
}
