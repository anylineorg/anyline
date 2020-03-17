package org.anyline.wechat.util;
 
import java.io.File; 
import java.util.HashMap; 
import java.util.Map;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.net.HttpUtil;
import org.anyline.net.RSAUtil;
import org.anyline.net.SimpleHttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil; 
import org.anyline.util.ConfigTable; 
import org.anyline.util.MD5Util;
import org.anyline.wechat.entity.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
public class WechatUtil {
	protected static final Logger log = LoggerFactory.getLogger(WechatUtil.class);
	private static DataSet accessTokens = new DataSet();
	/** 
	 * 参数签名 
	 *  
	 * @param secret  secret
	 * @param params  params
	 * @return return
	 */ 
	public static String sign(String secret, Map<String, Object> params) { 
		String sign = ""; 
		sign = HttpUtil.param(params); 
		sign += "&key=" + secret; 
		sign = MD5Util.crypto(sign).toUpperCase(); 
		return sign; 
	} 
	public static boolean validateSign(String secret, Map<String,Object> map){ 
		String sign = (String)map.get("sign"); 
		if(BasicUtil.isEmpty(sign)){ 
			return false; 
		} 
		map.remove("sign"); 
		String chkSign = sign(secret, map); 
		return chkSign.equals(sign); 
	} 
	public static boolean validateSign(String secret, String xml){ 
		return validateSign(secret,BeanUtil.xml2map(xml)); 
	} 
	/** 
	 * 获取RSA公钥 
	 * @param mch  mch
	 * @param apiSecret  apiSecret
	 * @param keyStoreFile  keyStoreFile
	 * @param keyStorePassword  keyStorePassword
	 * @return return
	 */ 
	public static String getPublicKey(String mch, String apiSecret, File keyStoreFile, String keyStorePassword) { 
		Map<String, Object> parameters = new HashMap<String, Object>(); 
		parameters.put("mch_id", mch); 
		parameters.put("nonce_str", BasicUtil.getRandomLowerString(20)); 
		parameters.put("sign_type", "MD5"); 
		String sign = WechatUtil.sign(apiSecret, parameters);
		parameters.put("sign", sign); 
		String xml = BeanUtil.map2xml(parameters); 
		CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword); 
		StringEntity reqEntity = new StringEntity(xml, "UTF-8"); 
		reqEntity.setContentType("application/x-www-form-urlencoded"); 
		String txt = HttpUtil.post(httpclient, WechatConfig.API_URL_GET_PUBLIC_SECRET, "UTF-8", reqEntity).getText();
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[获取RSA公钥][\n{}\n]",txt); 
		} 
		return txt; 
	}

	/**
	 * 退款申请
	 * @param config  config
	 * @param refund  refund
	 * @return return WechatRefundResult
	 */
	public static WechatRefundResult refund(WechatConfig config, WechatRefund refund){
		WechatRefundResult result = null;
		refund.setNonce_str(BasicUtil.getRandomLowerString(20));
		if(BasicUtil.isEmpty(refund.getAppid())){
			refund.setAppid(config.APP_ID);
		}
		if(BasicUtil.isEmpty(refund.getMch_id())){
			refund.setMch_id(config.PAY_MCH_ID);
		}
		Map<String, Object> map = BeanUtil.toMap(refund);
		String sign = WechatUtil.sign(config.PAY_API_SECRET,map);

		map.put("sign", sign);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[退款申请][sign:{}]", sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[退款申请][xml:{}]", xml);
			log.warn("[退款申请][证书:{}]", config.PAY_KEY_STORE_FILE);
		}
		File keyStoreFile = new File(config.PAY_KEY_STORE_FILE);
		if(!keyStoreFile.exists()){
			log.warn("[密钥文件不存在][file:{}]",config.PAY_KEY_STORE_FILE);
			return new WechatRefundResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WechatRefundResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
			StringEntity  reqEntity  = new StringEntity(xml);
			reqEntity.setContentType("application/x-www-form-urlencoded");
			String txt = HttpUtil.post(httpclient, WechatConfig.API_URL_REFUND, "UTF-8", reqEntity).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[退款申请调用][result:{}", txt);
			}
			result = BeanUtil.xml2object(txt, WechatRefundResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WechatRefundResult(false,e.getMessage());
		}
		return result;
	}

	/**
	 * 统一下单
	 * @param config  config
	 * @param type  type
	 * @param order  order
	 * @return return
	 * @throws Exception Exception
	 */
	public static WechatPrePayResult unifiedorder(WechatConfig config, WechatConfig.TRADE_TYPE type, WechatPrePayOrder order) throws Exception{
		WechatPrePayResult result = null;
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
		//	order.setNotify_url(WechatProgrameConfig.getInstance().PAY_NOTIFY_URL);
		}
		if(BasicUtil.isEmpty(order.getOut_trade_no())){
			throw new Exception("未设置交易单号");
		}
		order.setTrade_type(type);
		Map<String, Object> map = BeanUtil.toMap(order);
		String sign = WechatUtil.sign(config.PAY_API_SECRET,map);
		map.put("sign", sign);
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][sign:{}}", sign);
		}
		String xml = BeanUtil.map2xml(map);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][xml:{}]", xml);
		}
		String rtn = SimpleHttpUtil.post(WechatConfig.API_URL_UNIFIED_ORDER, xml);

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][return:{}]", rtn);
		}
		result = BeanUtil.xml2object(rtn, WechatPrePayResult.class);
		if(BasicUtil.isNotEmpty(result.getPrepay_id())){
			result.setResult(true);
		}

		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[统一下单][prepay id:{}]", result.getPrepay_id());
		}
		return result;
	}

	/**
	 * 发送红包
	 * @param config  config
	 * @param pack  pack
	 * @return return
	 */
	public static WechatRedpackResult sendRedpack(WechatConfig config, WechatRedpack pack){
		WechatRedpackResult result = new WechatRedpackResult();
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
		String sign = WechatUtil.sign(config.PAY_API_SECRET,map);

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
			return new WechatRedpackResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WechatRedpackResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
			StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
			reqEntity.setContentType("application/x-www-form-urlencoded");
			String txt = HttpUtil.post(httpclient, WechatConfig.API_URL_SEND_REDPACK, "UTF-8", reqEntity).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[发送红包调用][result:{}]", txt);
			}
			result = BeanUtil.xml2object(txt, WechatRedpackResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WechatRedpackResult(false,e.getMessage());
		}
		return result;
	}

	/**
	 * 发送裂变红包
	 * @param pack  pack
	 * @param config  config
	 * @return return
	 */
	public static WechatFissionRedpackResult sendRedpack(WechatConfig config, WechatFissionRedpack pack){
		WechatFissionRedpackResult result = new WechatFissionRedpackResult();
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
		String sign = WechatUtil.sign(config.PAY_API_SECRET,map);

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
			return new WechatFissionRedpackResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WechatFissionRedpackResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
			StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
			reqEntity.setContentType("application/x-www-form-urlencoded");
			String txt = HttpUtil.post(httpclient, WechatConfig.API_URL_SEND_GROUP_REDPACK, "UTF-8", reqEntity).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[发送裂变红包调用][result:{}]", txt);
			}
			result = BeanUtil.xml2object(txt, WechatFissionRedpackResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WechatFissionRedpackResult(false,e.getMessage());
		}
		return result;
	}
	/**
	 * 企业付款
	 * @param config  config
	 * @param transfer  transfer
	 * @return return
	 */
	public static WechatEnterpriseTransferResult transfer(WechatConfig config, WechatEnterpriseTransfer transfer){
		WechatEnterpriseTransferResult result = new WechatEnterpriseTransferResult();
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
		String sign = WechatUtil.sign(config.PAY_API_SECRET,map);

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
			return new WechatEnterpriseTransferResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WechatEnterpriseTransferResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
			StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
			reqEntity.setContentType("application/x-www-form-urlencoded");
			String txt = HttpUtil.post(httpclient, WechatConfig.API_URL_COMPANY_TRANSFER, "UTF-8", reqEntity).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[付款调用][result:{}]", txt);
			}
			result = BeanUtil.xml2object(txt, WechatEnterpriseTransferResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WechatEnterpriseTransferResult(false,e.getMessage());
		}
		return result;
	}
	/**
	 * 企业付款到银行卡
	 * @param config  config
	 * @param transfer  transfer
	 * @return return
	 */
	public static WechatEnterpriseTransferBankResult transfer(WechatConfig config, WechatEnterpriseTransferBank transfer){
		WechatEnterpriseTransferBankResult result = new WechatEnterpriseTransferBankResult();
		transfer.setNonce_str(BasicUtil.getRandomLowerString(20));
		String enc_bank_no = transfer.getEnc_bank_no();
		String enc_true_name = transfer.getEnc_true_name();
		if(BasicUtil.isEmpty(enc_bank_no)){
			log.warn("未提供收款卡号");
			return new WechatEnterpriseTransferBankResult(false,"未提供收款卡号");
		}
		if(BasicUtil.isEmpty(enc_true_name)){
			log.warn("未提供收款人姓名");
			return new WechatEnterpriseTransferBankResult(false,"未提供收款人姓名");
		}
		try {
			enc_bank_no = RSAUtil.publicEncrypt(enc_bank_no, RSAUtil.getPublicKey(new File(config.PAY_BANK_RSA_PUBLIC_KEY_FILE)));
		}catch(Exception e){
			e.printStackTrace();
		}
		if(BasicUtil.isEmpty(transfer.getMch_id())){
			transfer.setMch_id(config.PAY_MCH_ID);
		}
		if(BasicUtil.isEmpty(transfer.getPartner_trade_no())){
			transfer.setPartner_trade_no(BasicUtil.getRandomLowerString(20));
		}
		Map<String, Object> map = BeanUtil.toMap(transfer);
		String sign = WechatUtil.sign(config.PAY_API_SECRET,map);

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
			return new WechatEnterpriseTransferBankResult(false,"密钥文件不存在");
		}
		String keyStorePassword = config.PAY_KEY_STORE_PASSWORD;
		if(BasicUtil.isEmpty(keyStorePassword)){
			log.warn("未设置密钥文件密码");
			return new WechatEnterpriseTransferBankResult(false,"未设置密钥文件密码");
		}
		try{
			CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
			StringEntity  reqEntity  = new StringEntity(xml,"UTF-8");
			reqEntity.setContentType("application/x-www-form-urlencoded");
			String txt = HttpUtil.post(httpclient, WechatConfig.API_URL_COMPANY_TRANSFER_BANK, "UTF-8", reqEntity).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[付款调用][result:{}]", txt);
			}
			result = BeanUtil.xml2object(txt, WechatEnterpriseTransferBankResult.class);
		}catch(Exception e){
			e.printStackTrace();
			return new WechatEnterpriseTransferBankResult(false,e.getMessage());
		}
		return result;
	}

	/**
	 * 获取RSA公钥
	 * @param config  config
	 * @return return
	 */
	public static String getPublicKey(WechatConfig config) {
		String txt = WechatUtil.getPublicKey(config.PAY_MCH_ID, config.PAY_API_SECRET, new File(config.PAY_KEY_STORE_FILE), config.PAY_KEY_STORE_PASSWORD);
		Map<String,?> map = BeanUtil.xml2map(txt);
		return (String)map.get("pub_key");
	}

	public static String getAccessToken(WechatConfig config){
		return getAccessToken(config.APP_ID, config.APP_SECRET);
	}
	public static String getAccessToken(String appid, String secret){
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
	private static DataRow newAccessToken(String appid, String secret){
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[CREATE NEW ACCESS TOKEN][appid:{}][secret:{}]",appid, secret);
		}
		DataRow row = new DataRow();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+appid+"&secret="+secret;
		String text = HttpUtil.post(url,"UTF-8").getText();
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[CREATE NEW ACCESS TOKEN][result:{}]",text);
		}
		DataRow json = DataRow.parseJson(text);
		row = new DataRow();
		if(null != json && json.containsKey("access_token")){
			row.put("APP_ID", appid);
			row.put("ACCESS_TOKEN", json.getString("access_token"));
			row.setExpires(json.getInt("expires_in", 0)*800);
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
	/**
	 * 用户授权信息 主要包含openid
	 * @param config config
	 * @param code code
	 * @return AuthInfo
	 */
	public static WechatAuthInfo getAuthInfo(WechatConfig config, String code){
		WechatAuthInfo result = null;
		String url = WechatConfig.API_URL_GET_AUTH_INFO + "?appid="+config.APP_ID+"&secret="+config.APP_SECRET+"&code="+code+"&grant_type=authorization_code";
		String txt = HttpUtil.get(url).getText();
		log.warn("[get auth info][txt:{}]",txt);
		result = BeanUtil.json2oject(txt, WechatAuthInfo.class);
		if(BasicUtil.isNotEmpty(result.getOpenid())){
			result.setResult(true);
		}
		return result;
	}

	/**
	 * 用户详细信息 主要包括用户昵称 头像 unionid
	 * @param config config
	 * @param openid openid
	 * @return UserInfo
	 */
	public static WechatUserInfo getUserInfo(WechatConfig config, String openid){
		WechatUserInfo result = null;
		String url = WechatConfig.API_URL_GET_USER_INFO + "?access_token="+getAccessToken(config)+"&openid="+openid+"&lang=zh_CN";
		String txt = HttpUtil.get(url).getText();
		log.warn("[wechar get user info][result:{}]",txt);
		result = BeanUtil.json2oject(txt, WechatUserInfo.class);
		if(BasicUtil.isNotEmpty(result.getOpenid())){
			result.setResult(true);
		}
		return result;
	}

	public static void profit(){

    }
} 
