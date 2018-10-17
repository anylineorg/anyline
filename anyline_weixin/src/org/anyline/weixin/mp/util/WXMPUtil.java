package org.anyline.weixin.mp.util;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.HttpClientUtil;
import org.anyline.util.HttpUtil;
import org.anyline.util.SHA1Util;
import org.anyline.util.SimpleHttpUtil;
import org.anyline.weixin.WXBasicConfig;
import org.anyline.weixin.entity.TemplateMessage;
import org.anyline.weixin.entity.TemplateMessageResult;
import org.anyline.weixin.mp.entity.WXMPPayRefund;
import org.anyline.weixin.mp.entity.WXMPPayRefundResult;
import org.anyline.weixin.mp.entity.WXMPPayTradeOrder;
import org.anyline.weixin.mp.entity.WXMPPayTradeResult;
import org.anyline.weixin.mp.entity.WXMPRedpack;
import org.anyline.weixin.mp.entity.WXMPRedpackResult;
import org.anyline.weixin.util.WXUtil;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;

public class WXMPUtil {
	private static Logger log = Logger.getLogger(WXMPUtil.class);
	private DataSet accessTokens = new DataSet();
	private DataSet jsapiTickets = new DataSet();
	private WXMPConfig config = null;

	private static Hashtable<String,WXMPUtil> instances = new Hashtable<String,WXMPUtil>();
	public static WXMPUtil getInstance(){
		return getInstance("default");
	}
	public WXMPUtil(WXMPConfig config){
		this.config = config;
	}
	public static WXMPUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		WXMPUtil util = instances.get(key);
		if(null == util){
			WXMPConfig config = WXMPConfig.getInstance(key);
			util = new WXMPUtil(config);
			instances.put(key, util);
		}
		return util;
	}
	
	public WXMPConfig getConfig() {
		return config;
	}

	/**
	 * 统一下单
	 * @param order
	 * @return
	 */
	public WXMPPayTradeResult unifiedorder(WXMPPayTradeOrder order) {
		WXMPPayTradeResult result = null;
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
		order.setTrade_type(WXBasicConfig.TRADE_TYPE.JSAPI);
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = WXUtil.paySign(config.PAY_API_SECRECT,map);
		map.put("sign", sign);
		if(ConfigTable.isDebug()){
			log.warn("统一下单SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("统一下单XML:" + xml);
		}
		String rtn = SimpleHttpUtil.post(WXBasicConfig.UNIFIED_ORDER_URL, xml);

		if(ConfigTable.isDebug()){
			log.warn("统一下单RETURN:" + rtn);
		}
		result = BeanUtil.xml2object(rtn, WXMPPayTradeResult.class);

		if(ConfigTable.isDebug()){
			log.warn("统一下单PREPAY ID:" + result.getPrepay_id());
		}
		return result;
	}
	/**
	 * 退款申请
	 * @param refund
	 * @return
	 */
	public WXMPPayRefundResult refund(WXMPPayRefund refund){
		WXMPPayRefundResult result = null;
		refund.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(refund.getAppid())){
			refund.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(refund.getMch_id())){
			refund.setMch_id(config.PAY_MCH_ID);
		}
		Map<String, Object> map = BeanUtil.toMap(refund);
		String sign = WXUtil.paySign(config.PAY_API_SECRECT,map);
		
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
			return new WXMPPayRefundResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXMPPayRefundResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpClientUtil.ceateSSLClient(keyStoreFile, HttpClientUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml);
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpClientUtil.post(httpclient, WXBasicConfig.REFUND_URL, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug()){
    			log.warn("退款申请调用结果:" + txt);
    		}
            result = BeanUtil.xml2object(txt, WXMPPayRefundResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WXMPPayRefundResult(false,e.getMessage());
		}
		return result;
	}
	/**
	 * 发送红包
	 * @param pack
	 * @return
	 */
	public WXMPRedpackResult sendredpack(WXMPRedpack pack){
		WXMPRedpackResult result = new WXMPRedpackResult();
		pack.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(pack.getWxappid())){
			pack.setWxappid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(pack.getMch_id())){
			pack.setMch_id(config.PAY_MCH_ID);
		}
		Map<String, Object> map = BeanUtil.toMap(pack);
		String sign = WXUtil.paySign(config.PAY_API_SECRECT,map);
		
		map.put("sign", sign);
		
		if(ConfigTable.isDebug()){
			log.warn("发送红包SIGN:" + sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug()){
			log.warn("发送红包XML:" + xml);
			log.warn("证书:"+config.PAY_KEY_STORE_FILE);
		}

		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("密钥文件不存在:"+config.PAY_KEY_STORE_FILE);
			return new WXMPRedpackResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXMPRedpackResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpClientUtil.ceateSSLClient(keyStoreFile, HttpClientUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml);
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpClientUtil.post(httpclient, WXBasicConfig.SEND_REDPACK_URL, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug()){
    			log.warn("发送红包调用结果:" + txt);
    		}
            result = BeanUtil.xml2object(txt, WXMPRedpackResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WXMPRedpackResult(false,e.getMessage());
		}
		return result;
	}
	/**
	 * APP调起支付所需参数
	 * @return
	 */
	public DataRow jsapiParam(String prepayid){
		String timestamp = System.currentTimeMillis()/1000+"";
		String random = BasicUtil.getRandomLowerString(20);
		String pkg = "prepay_id="+prepayid;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("package", pkg);
		params.put("timeStamp", timestamp);
		params.put("appId", getConfig().APP_ID);
		params.put("nonceStr", random);
		params.put("signType", "MD5");
		String sign = WXUtil.paySign(getConfig().PAY_API_SECRECT, params);
		params.put("paySign", sign);
		
		DataRow row = new DataRow(params);
		if(ConfigTable.isDebug()){
			log.warn("APP调起微信支付参数:" + row.toJSON());
		}
		return row;
	}

	
	public String getAccessToken(){
		return getAccessToken(config.APP_ID, config.APP_SECRECT);
	}
	public String getAccessToken(String appid, String secret){
		String result = "";
		DataRow row = accessTokens.getRow("APP_ID", appid);
		if(null == row){
			row = newAccessToken(appid, secret);
		}else if(row.isExpire()){
			accessTokens.remove(row);
			row = newAccessToken(appid, secret);
		}
		if(null != row){
			result = row.getString("ACCESS_TOKEN");
		}
		return result;
	}
	private DataRow newAccessToken(String appid, String secret){
		if(ConfigTable.isDebug()){
			log.warn("[CREATE NEW ACCESS TOKEN][appid:"+appid+", secret:"+secret+"]");
		}
		DataRow row = new DataRow();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret;
		String text = HttpClientUtil.post(url,"UTF-8").getText();
		if(ConfigTable.isDebug()){
			log.warn("[CREATE NEW ACCESS TOKEN][result:"+text+"]");
		}
		JSONObject json = JSONObject.fromObject(text);
		row = new DataRow();
		if(json.has("access_token")){
			row.put("APP_ID", appid);
			row.put("ACCESS_TOKEN", json.getString("access_token"));
			row.setExpires(json.getInt("expires_in")*800);
			if(ConfigTable.isDebug()){
				log.warn("[CREATE NEW ACCESS TOKEN][ACCESS_TOKEN:"+row.getString("ACCESS_TOKEN")+"]");
			}
		}else{
			if(ConfigTable.isDebug()){
				log.warn("[CREATE NEW ACCESS TOKEN][FAIL]");
			}
			return null;
		}
		accessTokens.addRow(row);
		return row;
	}
	
	public String getJsapiTicket(){
		String result = "";
		DataRow row = jsapiTickets.getRow("APP_ID", config.APP_ID);
		if(null == row){
			String accessToken = getAccessToken();
			row = newJsapiTicket(accessToken);
		}else if(row.isExpire()){
			jsapiTickets.remove(row);
			String accessToken = getAccessToken();
			row = newJsapiTicket(accessToken);
		}
		if(null != row){
			result = row.getString("TICKET");
		}
		return result;
	}
	public DataRow newJsapiTicket(String accessToken){
		if(ConfigTable.isDebug()){
			log.warn("[CREATE NEW JSAPI TICKET][token:"+accessToken+"]");
		}
		DataRow row = new DataRow();
		row.put("APP_ID", config.APP_ID);
		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi";
		String text = HttpUtil.get(url,"UTF-8").getText();
		log.warn("[CREATE NEW JSAPI TICKET][txt:"+text+"]");
		JSONObject json = JSONObject.fromObject(text);
		if(json.has("ticket")){
			row.put("TICKET", json.getString("ticket"));
			row.setExpires(json.getInt("expires_in")*1000);
			if(ConfigTable.isDebug()){
				log.warn("[CREATE NEW JSAPI TICKET][TICKET:"+row.get("TICKET")+"]");
			}
		}else{
			log.warn("[CREATE NEW JSAPI TICKET][FAIL]");
			return null;
		}
		jsapiTickets.addRow(row);
		return row;
	}
	/**
	 * 参与签名的字段包括
	 * noncestr（随机字符串）, 
	 * jsapi_ticket
	 * timestamp（时间戳
	 * url（当前网页的URL，不包含#及其后面部分）
	 * 对所有待签名参数按照字段名的ASCII 码从小到大排序（字典序）后，
	 * 使用URL键值对的格式（即key1=value1&key2=value2…）拼接成字符串string1。
	 * @param params
	 * @param encode
	 * @return
	 */
	public String jsapiSign(Map<String,Object> params){
		String sign = "";
		sign = BasicUtil.joinBySort(params);
		sign = SHA1Util.sign(sign);
		return sign;
	}
	
	public Map<String,Object> jsapiSign(String url){
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("noncestr", BasicUtil.getRandomLowerString(32));
		params.put("jsapi_ticket", getJsapiTicket());
		params.put("timestamp", System.currentTimeMillis()/1000+"");
		params.put("url", url);
		String sign = jsapiSign(params);
		params.put("sign", sign);
		params.put("appid", config.APP_ID);
		return params;
	}
	public DataRow getOpenId(String code){
		DataRow row = new DataRow();
		String url = WXBasicConfig.AUTH_ACCESS_TOKEN_URL + "?appid="+config.APP_ID+"&secret="+config.APP_SECRECT+"&code="+code+"&grant_type=authorization_code";
		String txt = HttpUtil.get(url);
		log.warn("[get openid][txt:"+txt+"]");
		row = DataRow.parseJson(txt);
		return row;
	}
	public DataRow getUnionId(String code){
		return getOpenId(code);
	}
	/**
	 * 发送样模板消息
	 * @param msg
	 * @return
	 */
	public TemplateMessageResult sendTemplateMessage(TemplateMessage msg){
		TemplateMessageResult result = null;
		String token = getAccessToken();
		String url = WXBasicConfig.SEND_TEMPLATE_MESSAGE_URL + "?access_token=" + token;
		String json = BeanUtil.object2json(msg);
		log.warn("[send template message][data:"+json+"]");
		HttpEntity entity = new StringEntity(json, "UTF-8");
		String txt = HttpClientUtil.post(url, "UTF-8", entity).getText();
		log.warn("[send template message][result:"+txt+"]");
		result = BeanUtil.json2oject(txt, TemplateMessageResult.class);
		return result;
	}
	public TemplateMessageResult sendTemplateMessage(String openId, TemplateMessage msg){
		msg.setUser(openId);
		return sendTemplateMessage(msg);
	}
}
