package org.anyline.wechat.mp.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SHA1Util;
import org.anyline.wechat.entity.WechatAuthInfo;
import org.anyline.wechat.entity.WechatTemplateMessage;
import org.anyline.wechat.entity.WechatTemplateMessageResult;
import org.anyline.wechat.entity.WechatUserInfo;
import org.anyline.wechat.mp.entity.Menu;
import org.anyline.wechat.util.WechatConfig;
import org.anyline.wechat.util.WechatConfig.SNSAPI_SCOPE;
import org.anyline.wechat.util.WechatUtil;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class WechatMPUtil extends WechatUtil {
	private static DataSet jsapiTickets = new DataSet();

	private WechatMPConfig config = null;
 
	private static Hashtable<String,WechatMPUtil> instances = new Hashtable<String,WechatMPUtil>(); 
	public static WechatMPUtil getInstance(){ 
		return getInstance("default"); 
	} 
	public WechatMPUtil(WechatMPConfig config){ 
		this.config = config; 
	} 
	public WechatMPUtil(String key, DataRow config){ 
		WechatMPConfig conf = WechatMPConfig.parse(key, config); 
		this.config = conf; 
		instances.put(key, this); 
	} 
	public static WechatMPUtil reg(String key, DataRow config){ 
		WechatMPConfig conf = WechatMPConfig.reg(key, config); 
		WechatMPUtil util = new WechatMPUtil(conf); 
		instances.put(key, util); 
		return util; 
	} 
	public static WechatMPUtil getInstance(String key){ 
		if(BasicUtil.isEmpty(key)){ 
			key = "default"; 
		} 
		WechatMPUtil util = instances.get(key); 
		if(null == util){ 
			WechatMPConfig config = WechatMPConfig.getInstance(key);
			if(null != config) {
				util = new WechatMPUtil(config);
				instances.put(key, util);
			}
		} 
		return util; 
	} 
	 
	public WechatMPConfig getConfig() { 
		return config;
	} 

	 
	public String getAccessToken(){ 
		return WechatUtil.getAccessToken(config);
	}

	public String getJsapiTicket(){ 
		String result = ""; 
		DataRow row = jsapiTickets.getRow("APP_ID", config.APP_ID); 
		if(null == row){ 
			String accessToken = getAccessToken(); 
			row = newJsapiTicket(accessToken); 
		}else if(row.isExpire()){ 
			jsapiTickets.remove(row); 
			String accessToken = getAccessToken(); 
			row = newJsapiTicket(accessToken); 
		} 
		if(null != row){ 
			result = row.getString("TICKET"); 
		} 
		return result; 
	} 
	public DataRow newJsapiTicket(String accessToken){
		DataRow row = new DataRow();
		if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
			log.warn("[CREATE NEW JSAPI TICKET][token:{}]",accessToken); 
		}
		if(BasicUtil.isNotEmpty(accessToken)){
			row.put("APP_ID", config.APP_ID);
			String url = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi";
			String text = HttpUtil.get(url,"UTF-8").getText();
			log.warn("[CREATE NEW JSAPI TICKET][txt:{}]",text);
			DataRow json = DataRow.parseJson(text);
			if(json.containsKey("ticket")){
				row.put("TICKET", json.getString("ticket"));
				row.setExpires(json.getInt("expires_in", 0)*1000);
				row.setExpires(1000*60*5); //5分钟内有效
				if(ConfigTable.isDebug() && log.isWarnEnabled()){
					log.warn("[CREATE NEW JSAPI TICKET][TICKET:{}]",row.get("TICKET"));
				}
			}else{
				log.warn("[CREATE NEW JSAPI TICKET][FAIL]");
				return null;
			}
			jsapiTickets.addRow(row);
		}
		return row; 
	} 
	/** 
	 * 参与签名的字段包括 
	 * noncestr（随机字符串）,  
	 * jsapi_ticket 
	 * timestamp（时间戳 
	 * url（当前网页的URL，不包含#及其后面部分） 
	 * 对所有待签名参数按照字段名的ASCII 码从小到大排序（字典序）后， 
	 * 使用URL键值对的格式（即key1=value1&amp;key2=value2…）拼接成字符串string1。 
	 * @param params  params
	 * @return return
	 */ 
	public String jsapiSign(Map<String,Object> params){ 
		String sign = ""; 
		sign = BeanUtil.map2string(params);
		sign = SHA1Util.sign(sign); 
		return sign; 
	} 
	 
	public Map<String,Object> jsapiSign(String url){ 
		Map<String,Object> params = new HashMap<String,Object>(); 
		params.put("noncestr", BasicUtil.getRandomLowerString(32)); 
		params.put("jsapi_ticket", getJsapiTicket()); 
		params.put("timestamp", System.currentTimeMillis()/1000+""); 
		params.put("url", url); 
		String sign = jsapiSign(params); 
		params.put("sign", sign); 
		params.put("appid", config.APP_ID); 
		return params; 
	}

	public WechatAuthInfo getAuthInfo(String code){
		return WechatUtil.getAuthInfo(config, code);
	}
	public String getOpenId(String code){
		WechatAuthInfo info = getAuthInfo(code);
		if(null != info && info.isResult()){
			return info.getOpenid();
		}
		return null;
	}
	public WechatUserInfo getUserInfo(String openid){
		return WechatUtil.getUserInfo(config,openid);
	}
	public String getUnionId(String openid) {
		WechatUserInfo info = getUserInfo(openid);
		if (null != info && info.isResult()) {
			return info.getUnionid();
		}
		return null;
	}

	/** 
	 * 是否已关注 
	 * @param openid  openid
	 * @return return
	 */ 
	public boolean isSubscribe(String openid){
		WechatUserInfo info = getUserInfo(openid);
		if(null == info){ 
			return false; 
		}
		if("1".equals(info.getSubscribe())){
			return true; 
		} 
		return false; 
	}

	/**
	 * 创建登录连接
	 * @param key 配置文件的key默认default
	 * @param redirect redirect 登录成功后得定向地址
	 * @param scope scope 获取信息范围
	 * @param state state 原样返回
	 * @return String
	 */
	public static String ceateAuthUrl(String key, String redirect, SNSAPI_SCOPE scope, String state){
		String url = null;
		try{
			WechatConfig config = WechatMPConfig.getInstance(key);
			String appid = config.APP_ID;
			if(BasicUtil.isEmpty(scope)){
				scope = SNSAPI_SCOPE.BASE;
			}
			if(BasicUtil.isEmpty(redirect)){
				redirect = config.OAUTH_REDIRECT_URL;
			}
			if(BasicUtil.isEmpty(redirect)){
				redirect = WechatMPConfig.getInstance().OAUTH_REDIRECT_URL;
			}
			redirect = URLEncoder.encode(redirect, "UTF-8");
			url =  WechatConfig.URL_OAUTH + "?appid="+appid+"&redirect_uri="+redirect+"&response_type=code&scope="
					+scope.getCode()+"&state="+state+",app:"+key+"#wechat_redirect";
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
		return url;
	}


	/**
	 * 发送样模板消息
	 * @param msg  msg
	 * @return return
	 */
	public WechatTemplateMessageResult sendTemplateMessage(WechatTemplateMessage msg){
		WechatTemplateMessageResult result = null;
		String token = getAccessToken();
		String url = WechatConfig.API_URL_SEND_TEMPLATE_MESSAGE + "?access_token=" + token;
		if(null != msg) {
			String json = BeanUtil.object2json(msg);
			log.warn("[send template message][data:{}]", json);
			HttpEntity entity = new StringEntity(json, "UTF-8");
			String txt = HttpUtil.post(url, "UTF-8", entity).getText();
			log.warn("[send template message][result:{}]", txt);
			result = BeanUtil.json2oject(txt, WechatTemplateMessageResult.class);
		}
		return result;
	}
	public WechatTemplateMessageResult sendTemplateMessage(String openId, WechatTemplateMessage msg){
		if(null != msg) {
			msg.setUser(openId);
		}
		return sendTemplateMessage(msg);
	}
	//生成场景二维码
	public DataRow createQrCode(String code){
		String token = getAccessToken();
		String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+token;
		Map<String,String> params = new HashMap<String,String>();
		String param = "{\"action_name\": \"QR_LIMIT_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \""+code+"\"}}}";
		String result = HttpUtil.post(url,"UTF-8", new StringEntity(param,"UTF-8")).getText();
		return DataRow.parseJson(result);
	}

    /**
     * 生成临时二维码
	 * @param code 场景值
	 * @param sec 有效时间(秒)
	 * @return DataRow
	 */
	public DataRow createQrCode(String code, int sec){
		String token = getAccessToken();
		String url = "https://api.weixin.qq.com/cgi-bin/qrcode/create?access_token="+token;
		Map<String,String> params = new HashMap<String,String>();
		String param = "{\"expire_seconds\":"+sec+", \"action_name\": \"QR_STR_SCENE\", \"action_info\": {\"scene\": {\"scene_str\": \""+code+"\"}}}";
		String result = HttpUtil.post(url,"UTF-8", new StringEntity(param,"UTF-8")).getText();
		return DataRow.parseJson(result);
	}

    /**
     * 为用户添加标签
	 * @param users 用户openid列表
	 * @param tag 标签id
	 * @return DataRow
	 */
	public DataRow addUserTag(List<String> users, int tag){
		String token = getAccessToken();
		String url = "https://api.weixin.qq.com/cgi-bin/tags/members/batchuntagging?access_token="+token;
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("openid_list", users);
		params.put("tagid", tag);
		String result = HttpUtil.post(url,"UTF-8", new StringEntity(BeanUtil.map2json(params),"UTF-8")).getText();
		return DataRow.parseJson(result);
	}

	/**
	 * 创建菜单
	 * @param menu 菜单内容
	 * @return 菜单id
	 */
	public String createMenu(Menu menu){
		String url = "https://api.weixin.qq.com/cgi-bin/menu/addconditional?access_token="+getAccessToken();
		if(null == menu.getMatchrule()){
			url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token="+getAccessToken();
		}
		String result = HttpUtil.post(url, "UTF-8", new StringEntity(menu.toJson(),"UTF-8")).getText();
		DataRow row = DataRow.parse(result);
		String id = row.getString("menuid");
		menu.setMenuid(id);
		return id;
	}

	/**
	 * 删除所以菜单
	 * @return DataRow
	 */
	public DataRow deleteMenu(){
		String url = "https://api.weixin.qq.com/cgi-bin/menu/delete?access_token="+getAccessToken();
		String result = HttpUtil.get(url).getText();
		DataRow row = DataRow.parse(result);
		return row;
	}

	/**
	 * 删除指定菜单
	 * @param menu 菜单id
	 * @return DataRow
	 */
	public DataRow deleteMenu(String menu){
		String param = "{\"menuid\":\""+menu+"\"}";
		String url = "https://api.weixin.qq.com/cgi-bin/menu/delconditional?access_token="+getAccessToken();
		String result = HttpUtil.post(url, "UTF-8", new StringEntity(param,"UTF-8")).getText();
		DataRow row = DataRow.parse(result);
		return row;
	}

	public DataRow getMenu(){
		String url = "https://api.weixin.qq.com/cgi-bin/menu/get?access_token="+getAccessToken();
		String result = HttpUtil.get(url).getText();
		DataRow row = DataRow.parseJson(result);
		return row;
	}
}