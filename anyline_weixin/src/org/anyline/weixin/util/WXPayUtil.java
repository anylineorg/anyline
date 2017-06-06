package org.anyline.weixin.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.anyline.util.SimpleHttpUtil;
import org.anyline.weixin.entity.PayOrder;
import org.anyline.weixin.entity.PayOrderResult;
import org.apache.log4j.Logger;

public class WXPayUtil {
	private static Logger log = Logger.getLogger(WXPayUtil.class);
	private WXUtil util = null;
	private static Hashtable<String,WXPayUtil> instances = new Hashtable<String,WXPayUtil>();
	public static WXPayUtil getInstance(){
		return getInstance("default");
	}
	public static WXPayUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		WXPayUtil util = instances.get(key);
		if(null == util){
			util = new WXPayUtil();
			util.util = WXUtil.getInstance(key);
			instances.put(key, util);
		}
		return util;
	}
	public WXConfig getConfig(){
		return util.getConfig();
	}
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public PayOrderResult unifiedorder(PayOrder order) {
		PayOrderResult result = null;
		order.setNonce_str(BasicUtil.getRandomString(20));
		if(BasicUtil.isEmpty(order.getAppid())){
			order.setAppid(util.getConfig().APP_ID);
		}
		if(BasicUtil.isEmpty(order.getMch_id())){
			order.setMch_id(util.getConfig().PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(util.getConfig().PAY_NOTIFY_URL);
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


	/**
	 * 签名
	 * 
	 * @param params
	 * @return
	 */
	public String sign(Map<String, Object> params) {
		String sign = "";
		sign = BasicUtil.joinBySort(params);
		sign += "&key=" + util.getConfig().PAY_API_SECRECT;
		sign = MD5Util.crypto(sign).toUpperCase();
		return sign;
	}

	/**
	 * APP 调用起支付签名
	 * @param nonce	随机串
	 * @param prepayid 预支付ID
	 * @return
	 */
	public String appSign(String prepayid, String nonce){
		String sign = "";
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("appid", util.getConfig().APP_ID);
		params.put("noncestr", nonce);
		params.put("package", "Sign=WXPay");
		params.put("partnerid", util.getConfig().PAY_MCH_ID);
		params.put("prepayid", prepayid);
		params.put("timestamp", System.currentTimeMillis()/1000+"");
		sign = sign(params);
		return sign;
	}
	/**
	 * APP调起支付所需参数
	 * @return
	 */
	public DataRow appParam(String prepayid){
		DataRow row = new DataRow();
		String nonce = BasicUtil.getRandomLowerString(32);
		String sign = appSign(prepayid, nonce);
		row.put("APPID", util.getConfig().APP_ID);
		row.put("PARTNERID", util.getConfig().PAY_MCH_ID);
		row.put("PREPAYID", prepayid);
		row.put("PACKAGEVALUE", "Sign=WXPay");
		String noncestr = BasicUtil.getRandomString(32);
		row.put("NONCESTR", noncestr);
		row.put("SIGN", sign);
		return row;
	}
}
