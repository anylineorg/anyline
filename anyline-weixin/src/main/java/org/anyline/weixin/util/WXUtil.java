package org.anyline.weixin.util;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.MD5Util;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WXUtil {
	private static final Logger log = LoggerFactory.getLogger(WXUtil.class);
	/**
	 * 参数签名
	 * 
	 * @param apisecret
	 * @param params
	 * @return
	 */
	public static String sign(String secret, Map<String, Object> params) {
		String sign = "";
		sign = BasicUtil.joinParamBySort(params);
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
	 * @param mch
	 * @param apiSecret
	 * @param keyStoreFile
	 * @param keyStorePassword
	 * @return
	 */
	public static String getPublicKey(String mch, String apiSecret, File keyStoreFile, String keyStorePassword) {
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put("mch_id", mch);
		parameters.put("nonce_str", BasicUtil.getRandomLowerString(20));
		parameters.put("sign_type", "MD5");
		String sign = WXUtil.sign(apiSecret, parameters);
		parameters.put("sign", sign);
		String xml = BeanUtil.map2xml(parameters);
		CloseableHttpClient httpclient = HttpUtil.ceateSSLClient(keyStoreFile, HttpUtil.PROTOCOL_TLSV1, keyStorePassword);
		StringEntity reqEntity = new StringEntity(xml, "UTF-8");
		reqEntity.setContentType("application/x-www-form-urlencoded");
		String txt = HttpUtil.post(httpclient, WXConfig.API_URL_GET_PUBLIC_SECRET, "UTF-8", reqEntity).getText();
		if(ConfigTable.isDebug() && log.isWarnEnabled()){
			log.warn("[获取RSA公钥][\n{}\n]",txt);
		}
		return txt;
	}
}
