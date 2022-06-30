package org.anyline.aliyun.sms.util;

import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.*;
import com.aliyun.tea.TeaConverter;
import com.aliyun.tea.TeaException;
import com.aliyun.tea.TeaPair;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Executable;
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

	public Template template= new SMSUtil.Template();

 
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
	public class Sign{
		public void request(String name, int source, String remark, List<String> files){
		}
	}

	public class Template{
		/**
		 * 申请短信模板
		 * @param name 名称
		 * @param type 0:验证码 1:通知短信 2:推广短信
		 * @param content 内容,如果type=1内容中需要有变量,如:${code}
		 * @param remark
		 * @return 返回模板编号 SMS_000000
		 */
		public String request(String name, int type, String content, String remark) throws Exception {
			AddSmsTemplateRequest req = new AddSmsTemplateRequest()
					.setTemplateType(type)
					.setTemplateName(name)
					.setTemplateContent(content)
					.setRemark(remark);
			AddSmsTemplateResponse response = SMSUtil.this.client.addSmsTemplate(req);
			String code = null;
			if("OK".equalsIgnoreCase(response.getBody().getCode())){
				code = response.getBody().getTemplateCode();
			}else{
				throw new Exception("模板申请失败:"+response.getBody().getCode());
			}
			return code;
		}

		/**
		 * 修改未审核通过的模板
		 * @param code request()返回的编号
		 * @param name 名称
		 * @param type 0:验证码 1:通知短信 2:推广短信
		 * @param content 内容,如果type=1内容中需要有变量,如:${code}
		 * @param remark
		 * @return boolean
		 */
		public boolean update(String code, String name, int type, String content, String remark) throws Exception {
			ModifySmsTemplateRequest req = new ModifySmsTemplateRequest()
					.setTemplateType(type)
					.setTemplateName(name)
					.setTemplateCode(code)
					.setTemplateContent(content)
					.setRemark(remark);
			ModifySmsTemplateResponse resp = client.modifySmsTemplate(req);
			if("OK".equalsIgnoreCase(resp.getBody().getCode())){
				throw new Exception("修改失败:"+resp.getBody().getMessage());
			}
			return true;
		}
		/**
		 * 查询全部模板列表
		 * @return list
		 * @throws Exception Exception
		 */
		public List<SMSTemplate> list() throws Exception{
			List<SMSTemplate> templates = new ArrayList<>();
			int page = 1;
			while (true){
				List<SMSTemplate> list = list(page, 10);
				if(null == list || list.size() ==0){
					break;
				}
				templates.addAll(list);
				page ++;
			}
			return templates;
		}

		/**
		 * 根据状态查询模板列表
		 * @param status 状态
		 * @return List
		 * @throws Exception Exception
		 */
		public List<SMSTemplate> list(SMSTemplate.STATUS status) throws Exception{
			List<SMSTemplate> all = list();
			List<SMSTemplate> templates = new ArrayList<>();
			for(SMSTemplate item:all){
				if(status.getCode().equals(item.getStatus())){
					templates.add(item);
				}
			}
			return templates;
		}

		/**
		 * 根据可用状态查询模板列表
		 * @param enable 是否可用
		 * @return List
		 * @throws Exception Exception
		 */
		public List<SMSTemplate> list(boolean enable) throws Exception{
			List<SMSTemplate> all = list();
			List<SMSTemplate> templates = new ArrayList<>();
			for(SMSTemplate item:all){
				if(enable){
					if(SMSTemplate.STATUS.AUDIT_STATE_PASS.getCode().equals(item.getStatus())) {
						templates.add(item);
					}
				}else{
					if(!SMSTemplate.STATUS.AUDIT_STATE_PASS.getCode().equals(item.getStatus())) {
						templates.add(item);
					}
				}
			}
			return templates;
		}

		/**
		 * 分页查询模板列表
		 * @param page 当前第几页
		 * @param vol 每页多少条
		 * @return list
		 * @throws Exception Exception
		 */
		public List<SMSTemplate> list(int page, int vol) throws Exception{
			List<SMSTemplate> templates = new ArrayList<>();

				QuerySmsTemplateListRequest req = new QuerySmsTemplateListRequest()
						.setPageIndex(page)
						.setPageSize(vol);
				QuerySmsTemplateListResponse resp = SMSUtil.this.client.querySmsTemplateList(req);

				if(null == resp || 200 != resp.getStatusCode()){
					throw new Exception("查询失败:"+resp.getStatusCode());

				}
				QuerySmsTemplateListResponseBody body = resp.getBody();
				if(null == body || !"OK".equalsIgnoreCase(body.getCode())){
					throw new Exception("查询失败:"+body.getMessage());
				}
				List<QuerySmsTemplateListResponseBody.QuerySmsTemplateListResponseBodySmsTemplateList> list = body.getSmsTemplateList();
				if(null == list){
					return templates;
				}
				for(QuerySmsTemplateListResponseBody.QuerySmsTemplateListResponseBodySmsTemplateList item:list){
					SMSTemplate template = new SMSTemplate();
					template.setCode(item.getTemplateCode());
					template.setContent(item.getTemplateContent());
					template.setStatus(item.getAuditStatus());
					template.setCreateTime(item.getCreateDate());
					template.setName(item.getTemplateName());
					template.setType(item.getTemplateType());
					QuerySmsTemplateListResponseBody.QuerySmsTemplateListResponseBodySmsTemplateListReason reason = item.getReason();
					if(null != reason){
						template.setRejectInfo(reason.getRejectInfo());
						template.setRejectTime(reason.getRejectDate());
						template.setRejectSubInfo(reason.getRejectSubInfo());
					}
					templates.add(template);
				}
			return templates;
		}

		public List<SMSTemplate> list(int page) throws Exception{
			return list(page, 10);
		}

		/**
		 * 删除模板
		 * @param code 模板编号
		 * @return boolean
		 * @throws Exception
		 */
		public boolean delete(String code) throws Exception{
			DeleteSmsTemplateRequest req = new DeleteSmsTemplateRequest()
					.setTemplateCode(code);
			DeleteSmsTemplateResponse resp = client.deleteSmsTemplate(req);
			if(!"OK".equalsIgnoreCase(resp.getBody().getCode())){
				throw new Exception("删除失败:"+resp.getBody().getMessage());
			}
			return true;
		}
	}

}
