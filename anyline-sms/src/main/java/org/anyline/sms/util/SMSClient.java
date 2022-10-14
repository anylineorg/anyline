package org.anyline.sms.util;

import org.anyline.net.HttpUtil;
import org.anyline.sms.entity.SMSResult;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
 
/** 
 * 短信服务client
 *  
 * @author zh 
 *  
 */ 
public class SMSClient { 
	private static final Logger log = LoggerFactory.getLogger(SMSClient.class);
    private SMSConfig config = null; 
	private static Hashtable<String,SMSClient> instances = new Hashtable<String,SMSClient>(); 

 
	public static SMSClient getInstance(){ 
		return getInstance(SMSConfig.DEFAULT_INSTANCE_KEY);
	} 
	public static SMSClient getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){
			key = SMSConfig.DEFAULT_INSTANCE_KEY;
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

	/**
	 * 发送短信
	 * @param platform 短信平台
	 * @param tenant 租户
	 * @param sign 签名
	 * @param template 模板
	 * @param mobile 手机号
	 * @param params 参数
	 * @return SMSResult
	 */
	public SMSResult send(String platform, String tenant, String sign, String template, String mobile, Map<String, String> params) {
		SMSResult result = null; 
		try{ 
			Map<String,Object> map = new HashMap<String,Object>(); 
			map.put("a", config.APP_KEY);
			map.put("k", config.APP_SECRET);
			map.put("s", sign);
			map.put("t", template);
			map.put("tt", BasicUtil.nvl(tenant, config.TENANT_CODE));
			map.put("pl", BasicUtil.nvl(platform, config.PLATFORM_CODE));
			map.put("m", mobile);
			map.put("p", BeanUtil.map2json(params));

			String txt = HttpUtil.post(config.SERVER_HOST, "UTF-8", map).getText();
			result = BeanUtil.json2oject(txt, SMSResult.class); 
			if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
				log.warn("[SMS SEND][mobile:{}][result:{}]",mobile,txt); 
			} 
		}catch(Exception e){ 
			result = new SMSResult(); 
			result.setMsg(e.getMessage()); 
			e.printStackTrace(); 
		} 
		return result; 
	}

	public SMSResult send(String sign, String template, String mobile, Map<String, String> params) {
		return send(null, null, sign, template, mobile, params);
	}
	/**
	 * 发送短信
	 * @param sign 签名
	 * @param template 配置
	 * @param mobiles 手机号
	 * @param params 参数
	 * @return SMSResult
	 */
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
		return send(config.SIGN, template, mobile, params);
	} 
	public SMSResult send(String template, List<String> mobile, Map<String, String> params) { 
		return send(config.SIGN, template, mobile, params);
	} 
 
}
