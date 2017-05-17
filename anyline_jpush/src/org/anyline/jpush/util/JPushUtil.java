package org.anyline.jpush.util;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.anyline.util.BasicUtil;

import cn.jiguang.commom.ClientConfig;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Options;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.Notification;

public class JPushUtil {

	private JPushConfig config = null;
	private static Hashtable<String,JPushUtil> instances = new Hashtable<String,JPushUtil>();
	private JPushClient client;
	
	public static JPushUtil getInstance(){
		return getInstance("default");
	}
	public static JPushUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default";
		}
		JPushUtil util = instances.get(key);
		if(null == util){
			util = new JPushUtil();
			JPushConfig config = JPushConfig.getInstance(key);
			util.config = config;

			ClientConfig clientConfig;
			clientConfig = ClientConfig.getInstance();
			clientConfig.setApnsProduction(false); 
			clientConfig.setTimeToLive(60 * 60 * 24);
			util.client = new JPushClient(config.MASTER_SECRET, config.APP_KEY, null, clientConfig);
			
			instances.put(key, util);
		}
		return util;
	}
	
	public JPushConfig getConfig() {
		return config;
	}
	/**
	 * 
	 * @param type 消息类别
	 * @param title 标题
	 * @param msg 详细信息
	 * @param extras 参数
	 * @param tags 接收人
	 * @return
	 */
	public boolean pushByTag(String type, String title, String msg, Map<String,String> extras, String ... tags){
		boolean result = false;
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		PushPayload pl = buildPushObjectWithExtras(type, title, msg, extras, tags);
		try {
			PushResult pr = client.sendPush(pl);
			result = pr.isResultOK();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	public boolean pushByTag(String type, String title, String msg, Map<String,String> extras, List<String> tags){
		boolean result = false;
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		PushPayload pl = buildPushObjectWithExtras(type, title, msg, extras, tags);
		try {
			PushResult pr = client.sendPush(pl);
			result = pr.isResultOK();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	/**
	 * 
	 * @param type 消息类别
	 * @param title 标题
	 * @param msg 详细信息
	 * @param extras 参数
	 * @param tags 接收人
	 * @return
	 */
	public boolean pushByAlias(String type, String title, String msg, Map<String,String> extras, String ... alias){
		boolean result = false;
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		PushPayload pl = buildPushObjectWithExtrasAils(type, title, msg, extras, alias);
		try {
			PushResult pr = client.sendPush(pl);
			result = pr.isResultOK();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	public boolean pushByAlias(String type, String title, String msg, Map<String,String> extras, List<String> alias){
		boolean result = false;
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		PushPayload pl = buildPushObjectWithExtrasAils(type, title, msg, extras, alias);
		try {
			PushResult pr = client.sendPush(pl);
			result = pr.isResultOK();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	private PushPayload buildPushObjectWithExtras(String type, String title, String msg, Map<String, String> extras,String ... tags) {
		return PushPayload.newBuilder()
				.setPlatform(Platform.all())
				.setAudience(Audience.newBuilder()
						.addAudienceTarget(AudienceTarget.tag_and(tags))
						.build())
						.setMessage(Message.newBuilder()
								.setMsgContent(type)
								.addExtras(extras)
								.build()).setOptions(Options.newBuilder()
				                         .setApnsProduction(true)
				                         .build())
								.setNotification(Notification.android(msg, title, null))
								.build();
	}
	private PushPayload buildPushObjectWithExtrasAils(String type, String title, String msg, Map<String, String> extras,String ... alias) {
		return PushPayload.newBuilder()
				.setPlatform(Platform.all())
				.setAudience(Audience.newBuilder()
						.addAudienceTarget(AudienceTarget.alias(alias))
						.build())
						.setMessage(Message.newBuilder()
								.setMsgContent(type)
								.addExtras(extras)
								.build()).setOptions(Options.newBuilder()
				                         .setApnsProduction(true)
				                         .build())
								.setNotification(Notification.android(msg, title, null))
								.build();
	}
	
	private PushPayload buildPushObjectWithExtras(String type, String title, String msg, Map<String, String> extras,List<String> tags) {
		return PushPayload.newBuilder()
				.setPlatform(Platform.all())
				.setAudience(Audience.newBuilder()
						.addAudienceTarget(AudienceTarget.tag_and(tags))
						
						.build())
						.setMessage(Message.newBuilder()
								.setMsgContent(type)
								.addExtras(extras)
								.build()).setOptions(Options.newBuilder()
				                         .setApnsProduction(true)
				                         .build())
								.setNotification(Notification.android(msg, title, null))
								.build();
	}
	private PushPayload buildPushObjectWithExtrasAils(String type, String title, String msg, Map<String, String> extras,List<String> alias) {
		return PushPayload.newBuilder()
				.setPlatform(Platform.all())
				.setAudience(Audience.newBuilder()
						.addAudienceTarget(AudienceTarget.alias(alias))
						.build())
						.setMessage(Message.newBuilder()
								.setMsgContent(type)
								.addExtras(extras)
								.build()).setOptions(Options.newBuilder()
				                         .setApnsProduction(true)
				                         .build())
								.setNotification(Notification.android(msg, title, null))
								.build();
	}
}
