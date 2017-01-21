package org.anyline.sms;

import java.util.List;
import java.util.Map;

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
 * 高德云图
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

	public static boolean send(String sign, String template, String mobile, Map<String, String> params) {
		boolean result = false;
		try {
			request.setSignName(sign);
			request.setTemplateCode(template);
			request.setParamString(BeanUtil.map2json(params));
			request.setRecNum(mobile);
			SingleSendSmsResponse response = client.getAcsResponse(request);
			response.getModel();
			result = true;
		} catch (ClientException e) {
			log.error(e);
			e.printStackTrace();
		}
		return result;
	}

	public static boolean send(String sign, String template, List<String> mobiles, Map<String, String> params) {
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
	

}