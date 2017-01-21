package org.anyline.sms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.anyline.util.BeanUtil;
import org.anyline.util.HttpClientUtil;
import org.apache.log4j.Logger;

/**
 * 短信服务
 * 
 * @author Administrator
 * 
 */
public class SMSClient {
	private static Logger log = Logger.getLogger(SMSClient.class);
    
	private static String sms_server = SMSConfig.SMS_SERVER;
	private String app = SMSConfig.CLIENT_APP;
	private String secret = SMSConfig.CLIENT_SECRET;
	private static SMSClient defaultClient = null;
	public synchronized static SMSClient defaultClient(){
		if(null == defaultClient){
			defaultClient = new SMSClient();
		}
		return defaultClient;
	}
	
	public SMSResult send(String sign, String template, String mobile, Map<String, String> params) {
		SMSResult result = null;
		try{
			Map<String,String> map = new HashMap<String,String>();
			map.put("_client_app", app);
			map.put("_client_secret", secret);
			map.put("_sms_sign", sign);
			map.put("_sms_template", template);
			map.put("_sms_mobile", mobile);
			map.put("_sms_param", BeanUtil.map2json(params));
			String txt = HttpClientUtil.get(sms_server, "UTF-8", map).getText();
			result = BeanUtil.json2oject(txt, SMSResult.class);
		}catch(Exception e){
			result = new SMSResult();
			result.setMsg(e.getMessage());
			log.error(e);
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
		return send(SMSConfig.SMS_SIGN, template, mobile, params);
	}
	public SMSResult send(String template, List<String> mobile, Map<String, String> params) {
		return send(SMSConfig.SMS_SIGN, template, mobile, params);
	}

}