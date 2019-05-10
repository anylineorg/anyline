package org.anyline.aliyun.sms.util;

import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.apache.log4j.Logger;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.profile.IClientProfile;

/**
 * 短信服务
 * 
 * @author Administrator
 * 
 */
public class SMSUtil {
	private static final Logger log = Logger.getLogger(SMSUtil.class);
	private SMSConfig config = null;
	private static Hashtable<String,SMSUtil> instances = new Hashtable<String,SMSUtil>();
	
	private IAcsClient client = null;
	private SendSmsRequest request = new SendSmsRequest();

    //产品名称:云通信短信API产品,开发者无需替换
    static final String product = "Dysmsapi";
    //产品域名,开发者无需替换
    static final String domain = "dysmsapi.aliyuncs.com";
    
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
				System.setProperty("sun.net.client.defaultConnectTimeout", "10000");
		        System.setProperty("sun.net.client.defaultReadTimeout", "10000");

		        //初始化acsClient,暂不支持region化
		        IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", config.ACCESS_KEY, config.ACCESS_SECRET);
		        DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", product, domain);
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
			 	request.setPhoneNumbers(mobile);
		        //必填:短信签名-可在短信控制台中找到
		        request.setSignName(sign);
		        //必填:短信模板-可在短信控制台中找到
		        request.setTemplateCode(template);
		        //可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
		        request.setTemplateParam(BeanUtil.map2json(params));

		        //hint 此处可能会抛出异常，注意catch
		        SendSmsResponse response = client.getAcsResponse(request);
		        result.setResult(true);
		} catch (ClientException e) {
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