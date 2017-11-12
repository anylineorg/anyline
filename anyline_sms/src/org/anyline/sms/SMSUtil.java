package org.anyline.sms;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.apache.log4j.Logger;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;
import com.aliyuncs.sms.model.v20160927.SingleSendSmsRequest;
import com.aliyuncs.sms.model.v20160927.SingleSendSmsResponse;

/**
 * 短信服务
 * 
 * @author Administrator
 * 
 */
public class SMSUtil {
	private static Logger log = Logger.getLogger(SMSUtil.class);
	private SMSConfig config = null;
	private static Hashtable<String,SMSUtil> instances = new Hashtable<String,SMSUtil>();
	
	private IAcsClient client = null;
	private SingleSendSmsRequest request = new SingleSendSmsRequest();
	
	public static SMSUtil getInstance(){
		return getInstance("default");
	}
	public static SMSUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		SMSUtil util = instances.get(key);
		if(null == util){
			util = new SMSUtil();
			SMSConfig config = SMSConfig.getInstance(key);
			util.config = config;
			try {
				IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", config.ACCESS_KEY, config.ACCESS_SECRET);
				DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Sms", "sms.aliyuncs.com");
				util.client = new DefaultAcsClient(profile);
			}
	        catch (Exception e) {
				e.printStackTrace();
	        	e.printStackTrace();
	        }
			instances.put(key, util);
		}
		return util;
	}
	public SMSResult send(String sign, String template, String mobile, Map<String, String> params) {
		SMSResult result = new SMSResult();
		try {
			if(BasicUtil.isEmpty(sign)){
				sign = config.SMS_SIGN;
			}
			request.setSignName(sign);
			request.setTemplateCode(template);
			request.setParamString(BeanUtil.map2json(params));
			request.setRecNum(mobile);
			SingleSendSmsResponse response = client.getAcsResponse(request);
			response.getModel();
			result.setResult(true);
		} catch (ClientException e) {
			e.printStackTrace();
			e.printStackTrace();
			result.setCode(e.getErrCode());
			result.setMsg(e.getErrMsg());
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
	public SMSConfig getConfig() {
		return config;
	}
}