package org.anyline.weixin.wap.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpUtil;
import org.anyline.util.SimpleHttpUtil;
import org.anyline.weixin.WXBasicConfig;
import org.anyline.weixin.util.WXUtil;
import org.anyline.weixin.wap.entity.WXWapPayTradeOrder;
import org.anyline.weixin.wap.entity.WXWapPayTradeResult;
import org.apache.log4j.Logger;

public class WXWapUtil {
	private static Logger log = Logger.getLogger(WXWapUtil.class);
	private static Hashtable<String,WXWapUtil> instances = new Hashtable<String,WXWapUtil>();
	private WXWapConfig config;
	

	public WXWapUtil(WXWapConfig config){
		this.config = config;
	}
	
	
	public static WXWapUtil getInstance(){
		return getInstance("default");
	}
	public static WXWapUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		WXWapUtil util = instances.get(key);
		if(null == util){
			WXWapConfig config = WXWapConfig.getInstance(key);
			util = new WXWapUtil(config);
			instances.put(key, util);
		}
		return util;
	}
	public WXWapConfig getConfig(){
		return config;
	}
	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public WXWapPayTradeResult unifiedorder(WXWapPayTradeOrder order) {
		WXWapPayTradeResult result = null;
		order.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(order.getAppid())){
			order.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(order.getMch_id())){
			order.setMch_id(config.MCH_ID);
		}
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(config.PAY_NOTIFY);
		}
		order.setTrade_type(WXBasicConfig.TRADE_TYPE.MWEB);
		
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = WXUtil.sign(config.API_SECRECT,map);
		map.put("sign", sign);
		if(ConfigTable.isDebug()){
			log.warn("统一下单SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("统一下单XML:" + xml);
		}
		String rtn = SimpleHttpUtil.post(WXBasicConfig.API_URL_UNIFIED_ORDER, xml);

		if(ConfigTable.isDebug()){
			log.warn("统一下单RETURN:" + rtn);
		}
		result = BeanUtil.xml2object(rtn, WXWapPayTradeResult.class);

		if(ConfigTable.isDebug()){
			log.warn("统一下单PREID:" + result.getPrepay_id());
		}
		return result;
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
		String sign = WXUtil.sign(config.API_SECRECT,params);
		params.put("sign", sign);
		DataRow row = new DataRow(params);
		row.put("packagevalue", row.get("package"));
		row.remove("package");
		if(ConfigTable.isDebug()){
			log.warn("APP调起微信支付参数:" + row.toJSON());
		}
		return row;
	}
	public DataRow getOpenId(String code){
		DataRow row = new DataRow();
		String url = WXBasicConfig.API_URL_AUTH_ACCESS_TOKEN + "?appid="+config.APP_ID+"&secret="+config.APP_SECRECT+"&code="+code+"&grant_type=authorization_code";
		String txt = HttpUtil.get(url);
		row = DataRow.parseJson(txt);
		return row;
	}
	public DataRow getUnionId(String code){
		return getOpenId(code);
	}
}
