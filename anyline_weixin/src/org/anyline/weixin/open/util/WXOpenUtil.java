package org.anyline.weixin.open.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.anyline.util.SimpleHttpUtil;
import org.anyline.weixin.mp.util.WXMPConfig;
import org.anyline.weixin.mp.util.WXMPUtil;
import org.anyline.weixin.open.entity.WXPayOrder;
import org.anyline.weixin.open.entity.WXPayOrderResult;
import org.apache.log4j.Logger;

public class WXOpenUtil {
	private static Logger log = Logger.getLogger(WXOpenUtil.class);
	private static Hashtable<String,WXOpenUtil> instances = new Hashtable<String,WXOpenUtil>();
	private WXOpenConfig config;
	public static WXOpenUtil getInstance(){
		return getInstance("default");
	}
	public static WXOpenUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		WXOpenUtil util = instances.get(key);
		if(null == util){
			util = new WXOpenUtil();
			WXOpenConfig config = WXOpenConfig.getInstance(key);
			util.config = config;
			instances.put(key, util);
		}
		return util;
	}
	public WXOpenConfig getConfig(){
		return config;
	}
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public WXPayOrderResult unifiedorder(WXPayOrder order) {
		WXPayOrderResult result = null;
		order.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(order.getAppid())){
			order.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(order.getMch_id())){
			order.setMch_id(config.MCH_ID);
		}
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(config.NOTIFY_URL);
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
		String rtn = SimpleHttpUtil.post(WXOpenConfig.UNIFIED_ORDER_URL, xml);

		if(ConfigTable.isDebug()){
			log.warn("统一下单RETURN:" + rtn);
		}
		result = BeanUtil.xml2object(rtn, WXPayOrderResult.class);

		if(ConfigTable.isDebug()){
			log.warn("统一下单PREID:" + result.getPrepay_id());
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
		sign += "&key=" + config.API_SECRECT;
		sign = MD5Util.crypto(sign).toUpperCase();
		return sign;
	}

	/**
	 * APP调起支付所需参数
	 * @return
	 */
	public DataRow appParam(String prepayid){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("appid", config.APP_ID);
		params.put("partnerid", config.MCH_ID);
		params.put("prepayid", prepayid);
		params.put("package", "Sign=WXPay");
		params.put("noncestr", BasicUtil.getRandomUpperString(32));
		params.put("timestamp", System.currentTimeMillis()/1000+"");
		String sign = sign(params);
		params.put("sign", sign);
		DataRow row = new DataRow(params);
		row.put("packagevalue", row.get("package"));
		row.remove("package");
		if(ConfigTable.isDebug()){
			log.warn("APP调起微信支付参数:" + row.toJSON());
		}
		return row;
	}
}
