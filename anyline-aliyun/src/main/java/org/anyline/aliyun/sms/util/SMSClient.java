package org.anyline.aliyun.sms.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 短信服务
 * 
 * @author Administrator
 * 
 */
public class SMSClient {
	private static final Logger log = LoggerFactory.getLogger(SMSClient.class);
    private SMSConfig config = null;
	private static Hashtable<String,SMSClient> instances = new Hashtable<String,SMSClient>();
	
//	private static String sms_server = SMSConfig.SMS_SERVER;
//	private String app = SMSConfig.CLIENT_APP;
//	private String secret = SMSConfig.CLIENT_SECRET;

	public static SMSClient getInstance(){
		return getInstance("default");
	}
	public static SMSClient getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		SMSClient client = instances.get(key);
		if(null == client){
			client = new SMSClient();
			SMSConfig config = SMSConfig.getInstance(key);
			client.config = config;
			instances.put(key, client);
		}
		return client;
	}
	public SMSResult send(String sign, String template, String mobile, Map<String, String> params) {
		SMSResult result = null;
		try{
			Map<String,Object> map = new HashMap<String,Object>();
			map.put("_client_app", config.CLIENT_APP);
			map.put("_client_secret", config.CLIENT_SECRET);
			map.put("_sms_sign", sign);
			map.put("_sms_template", template);
			map.put("_sms_mobile", mobile);
			map.put("_sms_param", BeanUtil.map2json(params));
			String txt = HttpUtil.get(config.SMS_SERVER, "UTF-8", map).getText();
			result = BeanUtil.json2oject(txt, SMSResult.class);
			if(ConfigTable.isDebug()){
				log.warn("[SMS SEND][mobile:{}][result:{}]",mobile,txt);
			}
		}catch(Exception e){
			result = new SMSResult();
			result.setMsg(e.getMessage());
			e.printStackTrace();
		}
		return result;
	}
	public SMSResult send(String sign, String template, List<String> mobiles, Map<String, String> params) {
		String mobile = "";
		for(String item:mobiles){
			if("".equals(mobile)){
				mobile = item;
			}else{
				mobile += "," + item;
			}
		}
		return send(sign, template, mobile, params);
	}

	public SMSResult send(String template, String mobile, Map<String, String> params) {
		return send(config.SMS_SIGN, template, mobile, params);
	}
	public SMSResult send(String template, List<String> mobile, Map<String, String> params) {
		return send(config.SMS_SIGN, template, mobile, params);
	}

}