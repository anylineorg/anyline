package org.anyline.sms;

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

	private static String access_key = SMSConfig.ACCESS_KEY;
	private static String access_secret = SMSConfig.ACCESS_SECRET;
	private static IAcsClient client = null;
	private static SingleSendSmsRequest request = new SingleSendSmsRequest();
	static {
		try {
			IClientProfile profile = DefaultProfile.getProfile("cn-hangzhou", access_key, access_secret);
			DefaultProfile.addEndpoint("cn-hangzhou", "cn-hangzhou", "Sms", "sms.aliyuncs.com");
			client = new DefaultAcsClient(profile);
		}
        catch (Exception e) {
			log.error(e);
        	e.printStackTrace();
        }
	}

	public static SMSResult send(String sign, String template, String mobile, Map<String, String> params) {
		SMSResult result = new SMSResult();
		try {
			if(BasicUtil.isEmpty(sign)){
				sign = SMSConfig.SMS_SIGN;
			}
			request.setSignName(sign);
			request.setTemplateCode(template);
			request.setParamString(BeanUtil.map2json(params));
			request.setRecNum(mobile);
			SingleSendSmsResponse response = client.getAcsResponse(request);
			response.getModel();
			result.setResult(true);
		} catch (ClientException e) {
			log.error(e);
			e.printStackTrace();
			result.setCode(e.getErrCode());
			result.setMsg(e.getErrMsg());
		}
		return result;
	}

	public static SMSResult send(String sign, String template, List<String> mobiles, Map<String, String> params) {
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

	public static SMSResult send(String template, String mobile, Map<String, String> params) {
		return send(SMSConfig.SMS_SIGN, template, mobile, params);
	}
	public static SMSResult send(String template, List<String> mobile, Map<String, String> params) {
		return send(SMSConfig.SMS_SIGN, template, mobile, params);
	}

}