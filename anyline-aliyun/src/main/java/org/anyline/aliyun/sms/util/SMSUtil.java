package org.anyline.aliyun.sms.util; 

import java.util.*;

import org.anyline.util.BasicUtil; 
import org.anyline.util.BeanUtil; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
 
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
 * @author zh 
 *  
 */ 
public class SMSUtil { 
	private static final Logger log = LoggerFactory.getLogger(SMSUtil.class); 
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

	/**
	 * 发送短信
	 * @param sign 签名(如果不指定则使用配置文件中默认签名)
	 * @param template 模板code(SMS_88550009,注意不要写成工单号)
	 * @param mobile 手机号，多个以逗号分隔
	 * @param params 参数
	 * @return SMSResult
	 */
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
 
		    //hint 此处可能会抛出异常,注意catch 
	        SendSmsResponse response = client.getAcsResponse(request);
	        result.setCode(response.getCode());
	        result.setMsg(response.getMessage());
	        result.setResult(true); 
		} catch (ClientException e) { 
			e.printStackTrace();
			result.setResult(false);
			result.setCode(e.getErrCode()); 
			result.setMsg(e.getErrMsg()); 
		} 
		return result; 
	}

	/**
	 * 发送短信
	 * send("sign","SMS_000000","15800000000", new User()/new DataRow(), ["id","name:userNmae","age:userAge"])
	 * @param sign 签名(如果不指定则使用配置文件中默认签名)
	 * @param template 模板code(SMS_88550009,注意不要写成工单号)
	 * @param mobile 手机号，多个以逗号分隔
	 * @param entity 实体对象
	 * @param keys 对象属性(根据keys从entity中取值生成短信参数),如果参数名与属性名不一致通过 短信参数名:属性名 转换
	 * @return SMSResult
	 */
	public SMSResult send(String sign, String template, String mobile, Object entity, List<String> keys) {
		return send(sign, template, mobile, object2map(entity, keys));
	}

	public SMSResult send(String sign, String template, String mobile, Object entity, String ... keys) {
		return send(sign, template, mobile, object2map(entity, keys));
	}
		/**
         * 发送短信
         * @param sign 签名(如果不指定则使用配置文件中默认签名)
         * @param template 模板code(SMS_88550009,注意不要写成工单号)
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
	public SMSResult send(String sign, String template, List<String> mobiles, Object entity, List<String> keys) {
		return send(sign, template, mobiles, object2map(entity, keys));
	}
	public SMSResult send(String sign, String template, List<String> mobiles, Object entity, String ... keys) {
		return send(sign, template, mobiles, object2map(entity, keys));
	}
		/**
         * 发送短信(使用配置文件中的默认签名)
         * @param template 模板code(SMS_88550009,注意不要写成工单号)
         * @param mobile 手机号
         * @param params 参数
         * @return SMSResult
         */
	public SMSResult send(String template, String mobile, Map<String, String> params) {
		return send(config.SMS_SIGN, template, mobile, params);
	}
	public SMSResult send(String template, String mobile, Object entity, List<String> keys) {
		return send(config.SMS_SIGN, template, mobile, object2map(entity, keys));
	}
	public SMSResult send(String template, String mobile, Object entity, String ... keys) {
		return send(config.SMS_SIGN, template, mobile, object2map(entity, keys));
	}
	/**
	 * 发送短信(使用配置文件中的默认签名)
	 * @param template 模板code(SMS_88550009,注意不要写成工单号)
	 * @param mobiles 手机号
	 * @param params 参数
	 * @return SMSResult
	 */
	public SMSResult send(String template, List<String> mobiles, Map<String, String> params) {
		return send(config.SMS_SIGN, template, mobiles, params);
	}
	public SMSResult send(String template, List<String> mobiles, Object entity, List<String> keys) {
		return send(config.SMS_SIGN, template, mobiles, object2map(entity, keys));
	}
	public SMSResult send(String template, List<String> mobiles, Object entity, String ... keys) {
		return send(config.SMS_SIGN, template, mobiles, object2map(entity, keys));
	}
	public SMSConfig getConfig() { 
		return config; 
	}

	private Map<String,String> object2map(Object entity, List<String> keys){
		Map<String,String> params = new HashMap<>();
		if(null != keys){
			for(String key:keys){
				String field = key;
				if(key.contains(":")){
					String[] tmps = key.split(":");
					key = tmps[0];
					field = tmps[1];
				}
				Object value = BeanUtil.getFieldValue(entity, field);
				if(null != value){
					params.put(key, value.toString());
				}
			}
		}
		return params;
	}
	private Map<String,String> object2map(Object entity, String ... keys){
		return object2map(entity, BeanUtil.array2list(keys));
	}
}
