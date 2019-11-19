package org.anyline.easemob.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.apache.http.HttpEntity;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EasemobUtil {
	private static final Logger log = LoggerFactory.getLogger(EasemobUtil.class);
	private static Hashtable<String,EasemobUtil> instances = new Hashtable<String,EasemobUtil>();
	private EasemobConfig config = null;
	private static long access_token_expires = 0;

	
//	private static final String orgName = EasemobConfig.ORG_NAME;
//	private static final String appName = EasemobConfig.APP_NAME;
//	private static final String clientId= EasemobConfig.CLIENT_ID;
//	private static final String clientSecret = EasemobConfig.CLIENT_SECRET;
//	private static final String host = EasemobConfig.HOST;
//	
	private String baseUrl ="";
	private String access_token = null;


	public static EasemobUtil getInstance(){
		return getInstance("default");
	}
	public static EasemobUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		EasemobUtil util = instances.get(key);
		if(null == util){
			util = new EasemobUtil();
			EasemobConfig config = EasemobConfig.getInstance(key);
			util.config = config;
			util.baseUrl = config.HOST + "/" + config.ORG_NAME + "/" + config.APP_NAME;
			instances.put(key, util);
		}
		return util;
	}
	
	public EasemobConfig getConfig() {
		return config;
	}


	/**
	 * 注册用户
	 * @param user
	 * @param password
	 * @param nick 昵称
	 * @return
	 */
	public DataRow reg(String user, String password, String nickname){
		DataRow result = null;
		String url = baseUrl + "/users";
		Map<String,String> map = new HashMap<String,String>();
		map.put("username", user);
		map.put("password", password);
		map.put("nickname", nickname);
		Map<String,String> headers = defaultHeader();
		headers.put("Content-Type", "application/json");
		try {
			HttpEntity entity = new StringEntity(BeanUtil.map2json(map), "UTF-8");
			String txt = HttpUtil.post(defaultHeader(), url, "UTF-8", entity).getText();
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				DataSet set = row.getSet("entities");
				if(set.size() > 0){
					result = set.getRow(0);
				}
			}
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[reg user][result:{}]",txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public DataRow reg(String user, String password){
		return reg(user,password, user);
	}
	/**
	 * 批量注册
	 * @param list
	 * @return
	 */
	public DataSet regs(List<Map<String,String>> list){
		DataSet result = new DataSet();
		StringBuilder json = new StringBuilder();
		json.append("[");
		if(null != list){
			int size = list.size();
			for(int i=0; i<size; i++){
				Map<String,String> map = list.get(i);
				if(i > 0){
					json.append(",");
				}
				String nickname = map.get("nickname");
				json.append("\"username\":\"").append(map.get("username"))
				.append("\",\"password\":\"").append(map.get("password")).append("\"");
				if(BasicUtil.isNotEmpty(nickname)){
					json.append("\"nickname\":\"").append(nickname).append("\"");
				}
				json.append("}");
			}
		}
		json.append("]");
		String url = baseUrl + "/users";
		Map<String,String> headers = defaultHeader();
		headers.put("Content-Type", "application/json");
		try{
			String txt = HttpUtil.post(headers, url,"UTF-8", new StringEntity(json.toString(), "UTF-8")).getText();
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				result = row.getSet("entities");
			}
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[reg users][result:{}]",txt);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 修改密码
	 * @param user
	 * @param password
	 * @return
	 */
	public boolean resetPassword(String user, String password){
		boolean result = false;
		String url = baseUrl + "/users/"+user+"/password";
		Map<String,String> map = new HashMap<String,String>();
		map.put("newpassword", password);
		String json = BeanUtil.map2json(map);
		try {

			Map<String,String> headers = new HashMap<String,String>();
			headers.put("Authorization", "Bearer " + getAccessToken());
			String txt = HttpUtil.put(headers, url,"UTF-8", new StringEntity(json, "UTF-8")).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[reset passworod][json:{}][result:{}]",json,txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
	/**
	 * 修改昵称
	 * @param user
	 * @param nickname
	 * @return
	 */
	public DataRow resetNickname(String user, String nickname){
		DataRow result = null;
		String url = baseUrl + "/users/"+user;
		Map<String,String> map = new HashMap<String,String>();
		map.put("nickname", nickname);
		try {
			String txt = HttpUtil.put(defaultHeader(), url,"UTF-8", new StringEntity(BeanUtil.map2json(map), "UTF-8")).getText();
			result = parseUser(txt);
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[reset nickname][result:{}]",txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除用户
	 * @param user
	 * @return
	 */
	public boolean delete(String user){
		boolean result = false;
		String url = baseUrl + "/users/" + user;
		try {
			String txt = HttpUtil.delete(defaultHeader(),url, "UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[delete user][result:{}]",txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		result = true;
		return result;
	}
	/**
	 * 用户信息
	 * @param user
	 * @return
	 */
	public DataRow getUser(String user){
		DataRow result = new DataRow();
		String url = baseUrl +  "/users/" + user;
		try{
			String txt = HttpUtil.get(defaultHeader(),url, "UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[get user][result:{}]",txt);
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				DataSet set = row.getSet("entities");
				if(set.size() > 0){
					result = set.getRow(0);
				}
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 批量用户信息
	 * @param limit
	 * @param cursor 分页游标
	 * @return
	 */
	public DataSet getUsers(int limit, String cursor){
		DataSet set = new DataSet();
		String url = baseUrl +  "/users/";
		Map<String,String> params = new HashMap<String,String>();
		params.put("limit", limit+"");
		if(BasicUtil.isNotEmpty(cursor)){
			params.put("cursor", cursor);
		}
		try{
			String txt = HttpUtil.get(defaultHeader(),url, "UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[get user list][result:{}]",txt);
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.containsKey("entities")){
				set = row.getSet("entities");
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return set;
	}
	public DataSet getUsers(int limit){
		return getUsers(limit, null);
	}
	/**
	 * 添加好友
	 * @param user
	 * @param friend
	 * @return
	 */
	public DataRow addFriend(String user, String friend){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/contacts/users/" + friend;
		try {
			String txt = HttpUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[add friend][result:{}]",txt);
			}
			result = parseUser(txt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 好友列表(只查username)
	 * @param user
	 * @return
	 */
	public DataSet getFriends(String user){
		DataSet result = new DataSet();
		String url = baseUrl + "/users/" + user + "/contacts/users";
		try {
			String txt = HttpUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[get friend list][result:{}]",txt);
			}
			DataRow json = DataRow.parseJson(txt);
			if(null != json && json.has("data")){
				List<?> datas = json.getList("data");
				for(Object data:datas){
					DataRow row = new DataRow();
					row.put("username", data);
					if(ConfigTable.isDebug() && log.isWarnEnabled()){
						log.warn("[get friend][friend username:{}]",data);
					}
					result.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除好友
	 * @param user
	 * @param friend
	 * @return 返回被删除的好友数据
	 */
	public DataRow deleteFriend(String user, String friend){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/contacts/users/" + friend;
		try {
			String txt = HttpUtil.delete(defaultHeader(), url,"UTF-8").getText();
			result = parseUser(txt);
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[delete friend][result:{}]",txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 添加黑名单
	 * @param user
	 * @param block
	 * @return
	 */
	public DataRow addBlock(String user, String block){
		DataRow result = null;
		//删除好友
		deleteFriend(user, block);
		String url = baseUrl + "/users/" + user + "/blocks/users";
		try {
			String params = "{\"usernames\":[\""+block+"\"]} ";
			String txt = HttpUtil.post(defaultHeader(), url,"UTF-8", new StringEntity(params, "UTF-8")).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[add blocks][result:{}]",txt);
			}
			//封装添加成功的用户username
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 好友列表(只查username)
	 * @param user
	 * @return
	 */
	public DataSet getBlocks(String block){
		DataSet result = new DataSet();
		String url = baseUrl + "/users/" + block + "/blocks/users";
		try {
			String txt = HttpUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[get block list][result:{}]",txt);
			}
			DataRow json = DataRow.parseJson(txt);
			if(null != json && json.has("data")){
				List<?> datas = json.getList("data");
				for(Object data:datas){
					DataRow row = new DataRow();
					row.put("username", data);
					if(ConfigTable.isDebug() && log.isWarnEnabled()){
						log.warn("[get block][block username:{}]",data);
					}
					result.add(row);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 删除黑名单 
	 * @param user
	 * @param friend
	 * @return 返回被删除的黑名单数据
	 */
	public DataRow deleteBlock(String user, String block){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/blocks/users/" + block;
		try {
			String txt = HttpUtil.delete(defaultHeader(), url,"UTF-8").getText();
			result = parseUser(txt);
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[delete block][result:{}]",txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 在线状态  1:在线 0:离线
	 * @param user
	 * @return
	 */
	public String status(String user){
		String result = "0";
		String url = baseUrl + "/users/" + user + "/status";
		try {
			String txt = HttpUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[get user status][result:{}]",txt);
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				String status = row.getString(user);
				log.warn("[get user status][status:{}]",status);
				if("online".equals(status)){
					result = "1";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 离线消息数量
	 * @param user
	 * @return
	 */
	public int offlineMsgCount(String user){
		int result = 0;
		String url = baseUrl + "/users/" + user + "/offline_msg_count";
		try {
			String txt = HttpUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[get user status][result:{}]",txt);
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				result = row.getInt(user);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 离线消息状态
	 * @param user
	 * @return
	 * deliverd:表示此用户的该条离线消息已经收到过了 undelivered:表示此用户的该条离线消息还未收到
	 */
	public String offlineMsgStatus(String user, String msg){
		String result = "";
		String url = baseUrl + "/users/" + user + "/offline_msg_status/" + msg;
		try {
			String txt = HttpUtil.get(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[get user status][result:{}]",txt);
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				result = row.getString(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 禁用帐号
	 * @param user
	 * @return
	 */
	public DataRow deactivate(String user){
		DataRow result = null;
		String url = baseUrl + "/users/" + user + "/deactivate";
		try {
			String txt = HttpUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[deactivate user][result:{}]",txt);
			}
			result = parseUser(txt);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	

	/**
	 * 激活已禁用帐号
	 * @param user
	 * @return
	 */
	public void activate(String user){
		String url = baseUrl + "/users/" + user + "/activate";
		try {
			String txt = HttpUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[activate user][result:{}]",txt);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 强制用户下线 
	 * @param user
	 * @return
	 */
	public boolean disconnect(String user){
		boolean result = false;
		String url = baseUrl + "/users/" + user + "/disconnect";
		try {
			String txt = HttpUtil.post(defaultHeader(), url,"UTF-8").getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[disconnect user][result:{}]",txt);
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				if(null != row && row.has("result") && row.getBoolean("result", false)){
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 消息发送
	 * @param from
	 * @param msg
	 * @param to
	 * @return
	 */
	public boolean send(String from, String msg, String to){
		boolean result = false;
		String json = "{\"target_type\":\"users\","
				+ "\"target\":[\"" + to + "\"],"
				+ "\"msg\":{\"type\":\"txt\",\"msg\":\"" + msg + "\"},"
				+ "\"from\":\""+from+"\"}";
		String url = baseUrl + "/messages";
		try {
			String txt = HttpUtil.post(defaultHeader(), url,"UTF-8", new StringEntity(json, "UTF-8")).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[send message][result:" + txt + "]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				if(null != row && row.has(to) && "success".equals(row.getString(to))){
					result = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 * 创建群
	 * @param name 名称
	 * @param des 描述
	 * @param pub 是否公开
	 * @param max 最大人数
	 * @param approve 是否需要审核
	 * @param owner 群主
	 * @return
	 */
	public String createGroup(String name, String des, boolean pub, int max, boolean approve, String owner){
		String result = "";
		String url = baseUrl + "/chatgroups";
		try {
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("groupname", name);
			params.put("desc", des);
			params.put("public", ""+pub);
			params.put("maxusers", ""+max);
			params.put("approval", ""+approve);
			params.put("owner", owner);
			String txt = HttpUtil.post(defaultHeader(), url, "UTF-8", params).getText();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				log.warn("[create group][result:" + txt + "]");
			}
			DataRow row = DataRow.parseJson(txt);
			if(null != row && row.has("data")){
				row = row.getRow("data");
				if(null != row && row.has("groupid")){
					result = row.getString("groupid");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	/**
	 * 根据返回值解析用户数据 
	 * @param txt
	 * @return
	 */
	private static DataRow parseUser(String txt){
		DataRow user = null;
		DataRow row = DataRow.parseJson(txt);
		if(null != row && row.containsKey("entities")){
			Object entitys = row.get("entities");
			if(null != entitys && entitys instanceof List){
				List<DataRow> list = (List<DataRow>)entitys;
				if(list.size()>0){
					user = list.get(0);
				}
			}
		}
		return user;
	}
	
	
	
	
	
	private  Map<String,String> defaultHeader(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Authorization", "Bearer " + getAccessToken());
		headers.put("Content-Type", "application/json");
		return headers;
	}
	private String getAccessToken(){
		String token = null;
		if(System.currentTimeMillis()/1000 > access_token_expires){
			token = createNewAccessToken();
		}else{
			token = access_token;
		}
		return token;
	}
	
	/**
	 * 创建新token 
	 * @return
	 */
	private String createNewAccessToken(){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("Content-Type", "application/json");
		Map<String,String> map = new HashMap<String,String>();
		map.put("grant_type", "client_credentials");
		map.put("client_id", config.CLIENT_ID);
		map.put("client_secret", config.CLIENT_SECRET);
		try {
			String url = baseUrl + "/token";
			String txt = HttpUtil.post(headers, url, "UTF-8", new StringEntity(BeanUtil.map2json(map), "UTF-8")).getText();
			JSONObject json = JSONObject.fromObject(txt);
			if(json.has("access_token")){
				access_token = json.getString("access_token");
			}
			if(json.has("expires_in")){
				access_token_expires = System.currentTimeMillis()/1000 + json.getLong("expires_in");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return access_token;
	}
}
