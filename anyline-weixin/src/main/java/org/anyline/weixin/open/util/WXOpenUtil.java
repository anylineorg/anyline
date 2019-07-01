package org.anyline.weixin.open.util;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpClientUtil;
import org.anyline.util.HttpUtil;
import org.anyline.util.SimpleHttpUtil;
import org.anyline.weixin.open.entity.WXOpenPayRefund;
import org.anyline.weixin.open.entity.WXOpenPayRefundResult;
import org.anyline.weixin.open.entity.WXOpenPrePayOrder;
import org.anyline.weixin.open.entity.WXOpenPrePayResult;
import org.anyline.weixin.util.WXConfig;
import org.anyline.weixin.util.WXUtil;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;

public class WXOpenUtil {
	private static final Logger log = Logger.getLogger(WXOpenUtil.class);
	private static Hashtable<String,WXOpenUtil> instances = new Hashtable<String,WXOpenUtil>();
	private WXOpenConfig config;

	public WXOpenUtil(WXOpenConfig config){
		this.config = config;
	}

	public WXOpenUtil(String key, DataRow config){
		WXOpenConfig conf = WXOpenConfig.parse(key, config);
		this.config = conf;
		instances.put(key, this);
	}

	public static WXOpenUtil reg(String key, DataRow config){
		WXOpenConfig conf = WXOpenConfig.parse(key, config);
		WXOpenUtil util = new WXOpenUtil(conf);
		instances.put(key, util);
		return util;
	}
	
	public static WXOpenUtil getInstance(){
		return getInstance("default");
	}
	public static WXOpenUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		WXOpenUtil util = instances.get(key);
		if(null == util){
			WXOpenConfig config = WXOpenConfig.getInstance(key);
			util = new WXOpenUtil(config);
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
	public WXOpenPrePayResult unifiedorder(WXOpenPrePayOrder order) {
		WXOpenPrePayResult result = null;
		order.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(order.getAppid())){
			order.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(order.getMch_id())){
			order.setMch_id(config.PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(config.PAY_NOTIFY_URL);
		}

		order.setTrade_type(WXConfig.TRADE_TYPE.APP);
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		map.put("sign", sign);
		if(ConfigTable.isDebug()){
			log.warn("统一下单SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("统一下单XML:" + xml);
		}
		String rtn = SimpleHttpUtil.post(WXConfig.API_URL_UNIFIED_ORDER, xml);

		if(ConfigTable.isDebug()){
			log.warn("统一下单RETURN:" + rtn);
		}
		result = BeanUtil.xml2object(rtn, WXOpenPrePayResult.class);

		if(ConfigTable.isDebug()){
			log.warn("统一下单PREID:" + result.getPrepay_id());
		}
		return result;
	}


	/**
	 * 退款申请
	 * @param refund
	 * @return
	 */
	public WXOpenPayRefundResult refund(WXOpenPayRefund refund){
		WXOpenPayRefundResult result = null;
		refund.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(refund.getAppid())){
			refund.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(refund.getMch_id())){
			refund.setMch_id(config.PAY_MCH_ID);
		}
		Map<String, Object> map = BeanUtil.toMap(refund);
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		
		map.put("sign", sign);
		
		if(ConfigTable.isDebug()){
			log.warn("退款申请SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("退款申请XML:" + xml);
			log.warn("证书:"+config.PAY_KEY_STORE_FILE);
		}
		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("密钥文件不存在:"+config.PAY_KEY_STORE_FILE);
			return new WXOpenPayRefundResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXOpenPayRefundResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpClientUtil.ceateSSLClient(keyStoreFile, HttpClientUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml);
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpClientUtil.post(httpclient, WXConfig.API_URL_REFUND, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug()){
    			log.warn("退款申请调用结果:" + txt);
    		}
            result = BeanUtil.xml2object(txt, WXOpenPayRefundResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WXOpenPayRefundResult(false,e.getMessage());
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
		params.put("partnerid", config.PAY_MCH_ID);
		params.put("prepayid", prepayid);
		params.put("package", "Sign=WXPay");
		params.put("noncestr", BasicUtil.getRandomUpperString(32));
		params.put("timestamp", System.currentTimeMillis()/1000+"");
		String sign = WXUtil.sign(config.PAY_API_SECRET,params);
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
		String url = WXConfig.API_URL_AUTH_ACCESS_TOKEN + "?appid="+config.APP_ID+"&secret="+config.APP_SECRET+"&code="+code+"&grant_type=authorization_code";
		String txt = HttpUtil.get(url).getText();
		row = DataRow.parseJson(txt);
		return row;
	}
	public DataRow getUnionId(String code){
		return getOpenId(code);
	}
}
