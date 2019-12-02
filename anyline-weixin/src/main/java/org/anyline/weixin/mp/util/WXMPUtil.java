package org.anyline.weixin.mp.util;

import java.io.File;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.net.HttpUtil;
import org.anyline.net.RSAUtil;
import org.anyline.net.SimpleHttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SHA1Util;
import org.anyline.weixin.entity.TemplateMessage;
import org.anyline.weixin.entity.TemplateMessageResult;
import org.anyline.weixin.mp.entity.WXMPGroupRedpack;
import org.anyline.weixin.mp.entity.WXMPGroupRedpackResult;
import org.anyline.weixin.mp.entity.WXMPPayRefund;
import org.anyline.weixin.mp.entity.WXMPPayRefundResult;
import org.anyline.weixin.mp.entity.WXMPPrePayOrder;
import org.anyline.weixin.mp.entity.WXMPPrePayResult;
import org.anyline.weixin.mp.entity.WXMPRedpack;
import org.anyline.weixin.mp.entity.WXMPRedpackResult;
import org.anyline.weixin.mp.entity.WXMPTransfer;
import org.anyline.weixin.mp.entity.WXMPTransferBank;
import org.anyline.weixin.mp.entity.WXMPTransferBankResult;
import org.anyline.weixin.mp.entity.WXMPTransferResult;
import org.anyline.weixin.util.WXConfig;
import org.anyline.weixin.util.WXConfig.SNSAPI_SCOPE;
import org.anyline.weixin.util.WXUtil;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WXMPUtil extends WXUtil{
	private static final Logger log = LoggerFactory.getLogger(WXMPUtil.class);
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
	public WXMPUtil(String key, DataRow config){
		WXMPConfig conf = WXMPConfig.parse(key, config);
		this.config = conf;
		instances.put(key, this);
	}
	public static WXMPUtil reg(String key, DataRow config){
		WXMPConfig conf = WXMPConfig.reg(key, config);
		WXMPUtil util = new WXMPUtil(conf);
		instances.put(key, util);
		return util;
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
	public WXMPPrePayResult unifiedorder(WXMPPrePayOrder order) throws Exception{
		WXMPPrePayResult result = null;
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
		if(BasicUtil.isEmpty(order.getNotify_url())){
			order.setNotify_url(WXMPConfig.getInstance().PAY_NOTIFY_URL);
		}
		if(BasicUtil.isEmpty(order.getOut_trade_no())){
			throw new Exception("未设置交易单号");
		}
		order.setTrade_type(WXConfig.TRADE_TYPE.JSAPI);
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		map.put("sign", sign);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][sign:{}}", sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][xml:{}]", xml);
		}
		String rtn = SimpleHttpUtil.post(WXConfig.API_URL_UNIFIED_ORDER, xml);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][return:{}]", rtn);
		}
		result = BeanUtil.xml2object(rtn, WXMPPrePayResult.class);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][prepay id:{}]", result.getPrepay_id());
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
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		
		map.put("sign", sign);
		
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[退款申请][sign:{}]", sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[退款申请][xml:{}]", xml);
			log.warn("[退款申请][证书:{}]",config.PAY_KEY_STORE_FILE);
		}

		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("[密钥文件不存在][file:{}]",config.PAY_KEY_STORE_FILE);
			return new WXMPPayRefundResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXMPPayRefundResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpUtil.post(httpclient, WXConfig.API_URL_REFUND, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug() && log.isWarnEnabled()){
    			log.warn("[退款申请调用][result:{}]", txt);
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
	public WXMPRedpackResult sendRedpack(WXMPRedpack pack){
		WXMPRedpackResult result = new WXMPRedpackResult();
		pack.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(pack.getWxappid())){
			pack.setWxappid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(pack.getMch_id())){
			pack.setMch_id(config.PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(pack.getMch_billno())){
			pack.setMch_billno(BasicUtil.getRandomLowerString(20));
		}
		Map<String, Object> map = BeanUtil.toMap(pack);
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		
		map.put("sign", sign);
		
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[发送红包[sign:{}]", sign);
		}
		String xml = BeanUtil.map2xml(map);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[发送红包][xml:{}]", xml);
			log.warn("[发送红包][证书:{}]", config.PAY_KEY_STORE_FILE);
		}

		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("[密钥文件不存在][file:{}]",config.PAY_KEY_STORE_FILE);
			return new WXMPRedpackResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXMPRedpackResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpUtil.post(httpclient, WXConfig.API_URL_SEND_REDPACK, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug() && log.isWarnEnabled()){
    			log.warn("[发送红包调用][result:{}]", txt);
    		}
            result = BeanUtil.xml2object(txt, WXMPRedpackResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WXMPRedpackResult(false,e.getMessage());
		}
		return result;
	}

	/**
	 * 发送裂变红包
	 * @param pack
	 * @return
	 */
	public WXMPGroupRedpackResult sendGroupRedpack(WXMPGroupRedpack pack){
		WXMPGroupRedpackResult result = new WXMPGroupRedpackResult();
		pack.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(pack.getWxappid())){
			pack.setWxappid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(pack.getMch_id())){
			pack.setMch_id(config.PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(pack.getMch_billno())){
			pack.setMch_billno(BasicUtil.getRandomLowerString(20));
		}
		Map<String, Object> map = BeanUtil.toMap(pack);
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		
		map.put("sign", sign);
		
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[发送裂变红包][sign:{}]", sign);
		}
		String xml = BeanUtil.map2xml(map);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[发送裂变红包][xml:{}]", xml);
			log.warn("[发送裂变红包][证书:{}]", config.PAY_KEY_STORE_FILE);
		}

		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("[密钥文件不存在][file:{}]", config.PAY_KEY_STORE_FILE);
			return new WXMPGroupRedpackResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXMPGroupRedpackResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpUtil.post(httpclient, WXConfig.API_URL_SEND_GROUP_REDPACK, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug() && log.isWarnEnabled()){
    			log.warn("[发送裂变红包调用][result:{}]", txt);
    		}
            result = BeanUtil.xml2object(txt, WXMPGroupRedpackResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WXMPGroupRedpackResult(false,e.getMessage());
		}
		return result;
	}
	/**
	 * 企业付款
	 * @param transfer
	 * @return
	 */
	public WXMPTransferResult transfer(WXMPTransfer transfer){
		WXMPTransferResult result = new WXMPTransferResult();
		transfer.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(transfer.getMch_appid())){
			transfer.setMch_appid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(transfer.getMchid())){
			transfer.setMchid(config.PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(transfer.getPartner_trade_no())){
			transfer.setPartner_trade_no(BasicUtil.getRandomLowerString(20));
		}
		Map<String, Object> map = BeanUtil.toMap(transfer);
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		
		map.put("sign", sign);
		
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[付款][sign:{}]", sign);
		}
		String xml = BeanUtil.map2xml(map);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[付款][xml:{}]", xml);
			log.warn("[付款][证书:{}]", config.PAY_KEY_STORE_FILE);
		}

		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("[密钥文件不存在][file:{}]",config.PAY_KEY_STORE_FILE);
			return new WXMPTransferResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXMPTransferResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpUtil.post(httpclient, WXConfig.API_URL_COMPANY_TRANSFER, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug() && log.isWarnEnabled()){
    			log.warn("[付款调用][result:{}]", txt);
    		}
            result = BeanUtil.xml2object(txt, WXMPTransferResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WXMPTransferResult(false,e.getMessage());
		}
		return result;
	}
	/**
	 * 企业付款到银行卡
	 * @param transfer
	 * @return
	 */
	public WXMPTransferBankResult transfer(WXMPTransferBank transfer){
		WXMPTransferBankResult result = new WXMPTransferBankResult();
		transfer.setNonce_str(BasicUtil.getRandomLowerString(20));
		String enc_bank_no = transfer.getEnc_bank_no();
		String enc_true_name = transfer.getEnc_true_name();
		if(BasicUtil.isEmpty(enc_bank_no)){
			log.warn("未提供收款卡号");
			return new WXMPTransferBankResult(false,"未提供收款卡号");
		}
		if(BasicUtil.isEmpty(enc_true_name)){
			log.warn("未提供收款人姓名");
			return new WXMPTransferBankResult(false,"未提供收款人姓名");
		}
		enc_bank_no = RSAUtil.publicEncrypt(enc_bank_no, RSAUtil.getPublicKey(new File(config.PAY_BANK_RSA_PUBLIC_KEY_FILE)));
		if(BasicUtil.isEmpty(transfer.getMch_id())){
			transfer.setMch_id(config.PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(transfer.getPartner_trade_no())){
			transfer.setPartner_trade_no(BasicUtil.getRandomLowerString(20));
		}
		Map<String, Object> map = BeanUtil.toMap(transfer);
		String sign = WXUtil.sign(config.PAY_API_SECRET,map);
		
		map.put("sign", sign);
		
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[付款][sign:{}]", sign);
		}
		String xml = BeanUtil.map2xml(map);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[付款][xml:{}]", xml);
			log.warn("[付款][证书:{}]", config.PAY_KEY_STORE_FILE);
		}

		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("[密钥文件不存在][file:{}]",config.PAY_KEY_STORE_FILE);
			return new WXMPTransferBankResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WXMPTransferBankResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
            StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
            reqEntity.setContentType("application/x-www-form-urlencoded"); 
            String txt = HttpUtil.post(httpclient, WXConfig.API_URL_COMPANY_TRANSFER_BANK, "UTF-8", reqEntity).getText();
    		if(ConfigTable.isDebug() && log.isWarnEnabled()){
    			log.warn("[付款调用][result:{}]", txt);
    		}
            result = BeanUtil.xml2object(txt, WXMPTransferBankResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WXMPTransferBankResult(false,e.getMessage());
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
		String sign = WXUtil.sign(getConfig().PAY_API_SECRET, params);
		params.put("paySign", sign);
		
		DataRow row = new DataRow(params);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[APP调起微信支付][参数:{}]", row.toJSON());
		}
		return row;
	}

	
	public String getAccessToken(){
		return getAccessToken(config.APP_ID, config.APP_SECRET);
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
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[CREATE NEW ACCESS TOKEN][appid:{}][secret:{}]",appid, secret);
		}
		DataRow row = new DataRow();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret;
		String text = HttpUtil.post(url,"UTF-8").getText();
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[CREATE NEW ACCESS TOKEN][result:{}]",text);
		}
		JSONObject json = JSONObject.fromObject(text);
		row = new DataRow();
		if(json.has("access_token")){
			row.put("APP_ID", appid);
			row.put("ACCESS_TOKEN", json.getString("access_token"));
			row.setExpires(json.getInt("expires_in")*800);
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[CREATE NEW ACCESS TOKEN][ACCESS_TOKEN:{}]",row.getString("ACCESS_TOKEN"));
			}
		}else{
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
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
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[CREATE NEW JSAPI TICKET][token:{}]",accessToken);
		}
		DataRow row = new DataRow();
		row.put("APP_ID", config.APP_ID);
		String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi";
		String text = HttpUtil.get(url,"UTF-8").getText();
		log.warn("[CREATE NEW JSAPI TICKET][txt:{}]",text);
		JSONObject json = JSONObject.fromObject(text);
		if(json.has("ticket")){
			row.put("TICKET", json.getString("ticket"));
			row.setExpires(json.getInt("expires_in")*1000);
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[CREATE NEW JSAPI TICKET][TICKET:{}]",row.get("TICKET"));
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
		sign = HttpUtil.param(params);
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
		DataRow row = null;
		String url = WXConfig.API_URL_AUTH_ACCESS_TOKEN + "?appid="+config.APP_ID+"&secret="+config.APP_SECRET+"&code="+code+"&grant_type=authorization_code";
		String txt = HttpUtil.get(url).getText();
		log.warn("[get openid][txt:{}]",txt);
		row = DataRow.parseJson(txt);
		return row;
	}
	public DataRow getUnionId(String code){
		return getOpenId(code);
	}
	/**
	 * 获取用户基本信息
	 * {"subscribe":1, 1:已关注
	 * "openid":"obZk6wq-38hnl4bx2NSOtU12b6fY",
	 * "nickname":"ZHANG",
	 * "sex":1,  1:男 2:女
	 * "language":"zh_CN",
	 * "city":"青岛","province":"山东","country":"中国",
	 * "headimgurl":"http:\/\/thirdwx.qlogo.cn\/mmopen\/OYSz",
	 * "subscribe_time":1540301692,"remark":"","groupid":0,"tagid_list":[],
	 * "subscribe_scene":"ADD_SCENE_SEARCH","qr_scene":0,"qr_scene_str":""}
	 * @param openid
	 * @return
	 */
	public DataRow getUserInfo(String openid){
		DataRow row = null;
		String url = WXConfig.API_URL_GET_USER_INFO + "?access_token="+getAccessToken()+"&openid="+openid;
		String txt = HttpUtil.get(url).getText();
		log.warn("[get openid][txt:{}]",txt);
		row = DataRow.parseJson(txt);
		return row;
	}
	/**
	 * 是否已关注
	 * @param openid
	 * @return
	 */
	public boolean isSubscribe(String openid){
		DataRow info = getUserInfo(openid);
		if(null == info){
			return false;
		}
		if(info.getInt("subscribe") ==1){
			return true;
		}
		return false;
	}
	/**
	 * 发送样模板消息
	 * @param msg
	 * @return
	 */
	public TemplateMessageResult sendTemplateMessage(TemplateMessage msg){
		TemplateMessageResult result = null;
		String token = getAccessToken();
		String url = WXConfig.API_URL_SEND_TEMPLATE_MESSAGE + "?access_token=" + token;
		String json = BeanUtil.object2json(msg);
		log.warn("[send template message][data:{}]",json);
		HttpEntity entity = new StringEntity(json, "UTF-8");
		String txt = HttpUtil.post(url, "UTF-8", entity).getText();
		log.warn("[send template message][result:{}]",txt);
		result = BeanUtil.json2oject(txt, TemplateMessageResult.class);
		return result;
	}
	public TemplateMessageResult sendTemplateMessage(String openId, TemplateMessage msg){
		msg.setUser(openId);
		return sendTemplateMessage(msg);
	}

	public static String ceateAuthUrl(String key, String redirect, SNSAPI_SCOPE scope, String state){
		String url = null;
		try{
			WXConfig config = WXMPConfig.getInstance(key);
			String appid = config.APP_ID;
			if(BasicUtil.isEmpty(scope)){
				scope = SNSAPI_SCOPE.BASE;
			}
			if(BasicUtil.isEmpty(redirect)){
				redirect = config.OAUTH_REDIRECT_URL;
			}
			if(BasicUtil.isEmpty(redirect)){
				redirect = WXMPConfig.getInstance().OAUTH_REDIRECT_URL;
			}
			redirect = URLEncoder.encode(redirect, "UTF-8");
			url =  WXConfig.URL_OAUTH + "?appid="+appid+"&redirect_uri="+redirect+"&response_type=code&scope="
					+scope.getCode()+"&state="+state+",app:"+key+"#wechat_redirect";
		}catch(Exception e){
			return null;
		}
		return url;
	}
	
	/**
	 * 获取RSA公钥
	 * @param mch
	 * @param apiSecret
	 * @param keyStoreFile
	 * @param keyStorePassword
	 * @return
	 */
	public String getPublicKey() {
		String txt = WXUtil.getPublicKey(config.PAY_MCH_ID, config.PAY_API_SECRET, new File(config.PAY_KEY_STORE_FILE), config.PAY_KEY_STORE_PASSWORD);
		Map<String,?> map = BeanUtil.xml2map(txt);
		return (String)map.get("pub_key");
	}
	
}
