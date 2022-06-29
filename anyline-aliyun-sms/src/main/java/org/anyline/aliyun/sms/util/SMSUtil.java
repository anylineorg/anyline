package org.anyline.aliyun.sms.util;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/** 
 * 短信服务 
 *  
 * @author zh 
 *  
 */ 
public class SMSUtil { 
	private static final Logger log = LoggerFactory.getLogger(SMSUtil.class); 
	private SMSConfig config = null;
	private Client client;
	private static Hashtable<String,SMSUtil> instances = new Hashtable<String,SMSUtil>(); 

 
    //产品名称:云通信短信API产品,开发者无需替换 
    static final String product = "Dysmsapi"; 
    //产品域名,开发者无需替换 
    static final String endpoint = "dysmsapi.aliyuncs.com";
     
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
				Config cfg = new Config();
				cfg.setAccessKeyId(config.ACCESS_KEY);
				cfg.setAccessKeySecret(config.ACCESS_SECRET);
				cfg.endpoint = endpoint;
		        util.client = new Client(cfg);
			} 
	        catch (Exception e) { 
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
			SendSmsRequest request = new SendSmsRequest()
					//必填:短信签名-可在短信控制台中找到
					.setSignName(sign)
					//必填:短信模板-可在短信控制台中找到
					.setTemplateCode(template)
					.setPhoneNumbers(mobile)
					//可选:模板中的变量替换JSON串,如模板内容为"亲爱的${name},您的验证码为${code}"时,此处的值为
					.setTemplateParam(BeanUtil.map2json(params));
			RuntimeOptions runtime = new RuntimeOptions();
			SendSmsResponse response = client.sendSmsWithOptions(request, runtime);
	        result.setStatus(response.getStatusCode());
			SendSmsResponseBody body = response.getBody();
			result.setCode(body.getCode());
	        result.setMsg(body.getMessage());
			result.setBiz(body.getBizId());
	        result.setResult(true);
		} catch (Exception e) {
			e.printStackTrace();
			result.setResult(false);
			result.setMsg(e.getMessage());
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

	/**
	 * 查询发送状态,有可能查出多个发送记录，按时间倒序
	 * @param mobile 手机号
	 * @param biz 回执号
	 * @param date 发送日期
	 * @param vol 每页多少条
	 * @param page 当前第几页
	 * @return List 根据 SMSMResult.status 1:等待回执 2:发送失败 3:发送成功。
	 * @throws Exception Exception
	 */
	public List<SMSResult> status(String mobile, String biz, String date, int vol, int page) throws Exception{
		List<SMSResult> results = new ArrayList<>();
		QuerySendDetailsRequest query = new QuerySendDetailsRequest()
				.setPhoneNumber(mobile)
				.setSendDate(date)
				.setPageSize((long)vol)
				.setCurrentPage((long)page);

				if(BasicUtil.isNotEmpty(biz)){
					query.setBizId(biz);
				}
			QuerySendDetailsResponse queryResp = client.querySendDetails(query);
			List<QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO> list = queryResp.getBody().getSmsSendDetailDTOs().getSmsSendDetailDTO();
			for (QuerySendDetailsResponseBody.QuerySendDetailsResponseBodySmsSendDetailDTOsSmsSendDetailDTO item : list) {
				SMSResult result = new SMSResult();
				result.setStatus(item.getSendStatus().intValue());
				result.setContent(item.getContent());
				result.setCode(item.getErrCode());
				result.setMobile(item.getPhoneNum());
				result.setOut(item.getOutId());
				result.setReceiveTime(item.getReceiveDate());
				result.setSendTime(item.getSendDate());
				result.setTemplate(item.getTemplateCode());
				results.add(result);
			}

		return results;
	}

	/**
	 * 最后一次发送状态
	 * @param mobile 手机号
	 * @param biz 回执号
	 * @param date 发送日期
	 * @return SMSResult 根据 SMSMResult.status 1:等待回执 2:发送失败 3:发送成功。
	 * @throws Exception Exception
	 */
	public SMSResult status(String mobile, String biz, String date) throws Exception{
		List<SMSResult> results = status(mobile, biz, date, 1,1);
		if(results.size()>0){
			return results.get(0);
		}
		return null;
	}

	public SMSResult status(String mobile, String biz) throws Exception{
		return status(mobile, biz, DateUtil.format("yyyyMMdd"));
	}
	public SMSResult status(String mobile) throws Exception{
		return status(mobile, null, DateUtil.format("yyyyMMdd"));
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
