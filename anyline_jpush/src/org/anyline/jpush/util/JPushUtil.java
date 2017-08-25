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
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		boolean result = true;
		int size = tags.length;
		int cnt = (size-1) / 1000+1;
		for(int c=0; c<cnt; c++){
			int fr = c * 1000;
			int to = (c+1)*1000-1;
			if(to > size-1){
				to = size-1;
			}
			String[] args = new String[to-fr+1];
			for(int i=0; i<= to-fr; i++){
				args[i] = tags[fr+i];
			}
			result = sendByTag(type, title, msg, extras, args) && result;
		}
		return result;
	}
	
	public boolean pushByTag(String type, String title, String msg, Map<String,String> extras, List<String> tags){
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		boolean result = true;
		int size = tags.size();
		int cnt = (size-1) / 1000+1;
		for(int c=0; c<cnt; c++){
			int fr = c * 1000;
			int to = (c+1)*1000-1;
			if(to > size-1){
				to = size-1;
			}
			String[] args = new String[to-fr+1];
			for(int i=0; i<= to-fr; i++){
				args[i] = tags.get(fr+i);
			}
			result = sendByTag(type, title, msg, extras, args) && result;
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
		boolean result = true;
		int size = alias.length;
		int cnt = (size-1) / 1000+1;
		for(int c=0; c<cnt; c++){
			int fr = c * 1000;
			int to = (c+1)*1000-1;
			if(to > size-1){
				to = size-1;
			}
			String[] args = new String[to-fr+1];
			for(int i=0; i<= to-fr; i++){
				args[i] = alias[fr+i];
			}
			result = sendByAlias(type, title, msg, extras, args) && result;
		}
		return result;
	}

	public boolean pushByAlias(String type, String title, String msg, Map<String,String> extras, List<String> alias){
		boolean result = true;
		int size = alias.size();
		int cnt = (size-1) / 1000+1;
		for(int c=0; c<cnt; c++){
			int fr = c * 1000;
			int to = (c+1)*1000-1;
			if(to > size-1){
				to = size-1;
			}
			String[] args = new String[to-fr+1];
			for(int i=0; i<= to-fr; i++){
				args[i] = alias.get(fr+i);
			}
			result = sendByAlias(type, title, msg, extras, args) && result;
		}
		return result;
	}
	private boolean sendByAlias(String type, String title, String msg, Map<String,String> extras, String[] alias){
		boolean result = false;
		try {
			PushPayload pl = buildPushObject_Alias(type, title, msg, extras, alias);
			PushResult pr = client.sendPush(pl);
			result = pr.isResultOK();
			
//			extras.put("MESSAGE", msg);
//			pl = buildPushObject_Alias_IOS(type, title, extras, alias);
//			pr = client.sendPush(pl);
//			result = pr.isResultOK() && result;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}

	private boolean sendByTag(String type, String title, String msg, Map<String,String> extras, String[] tags){
		boolean result = false;
		try {
			extras.put("MESSAGE", msg);
			extras.put("TITLE", title);
//			
			PushPayload pl = buildPushObjec_Tag_Android(type, title, msg, extras, tags);
			PushResult pr = client.sendPush(pl);
			result = pr.isResultOK();
////			
//			System.out.println(extras);
//			pl = buildPushObjec_Tag_IOS(type, title, extras, tags);
//			pr = client.sendPush(pl);
//			result = pr.isResultOK() && result;
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	private PushPayload buildPushObjec_Tag(String type, String title, String msg, Map<String, String> extras,String[] tags) {
		if(null == extras){
			extras = new HashMap<String,String>();
		}
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
								.setNotification(Notification.android(msg, title, extras).ios(title, extras))
								.build();
	}

	private PushPayload buildPushObject_Alias(String type, String title, String msg, Map<String, String> extras,String[] alias) {
		if(null == extras){
			extras = new HashMap<String,String>();
		}
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
								.setNotification(Notification.android(msg, title, extras).ios(title, extras))
								.build();
	}
	
	private PushPayload buildPushObjec_Tag_Android(String type, String title, String msg, Map<String, String> extras,String[] tags) {
		if(null == extras){
			extras = new HashMap<String,String>();
		}
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
								.setNotification(Notification.android(msg, title, extras))
								.build();
	}
	private PushPayload buildPushObject_Alias_Android(String type, String title, String msg, Map<String, String> extras,String[] alias) {
		if(null == extras){
			extras = new HashMap<String,String>();
		}
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
								.setNotification(Notification.android(msg, title, extras))
								.build();
	}
	private PushPayload buildPushObjec_Tag_IOS(String type, String title,Map<String, String> extras,String[] tags) {
		if(null == extras){
			extras = new HashMap<String,String>();
		}
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
								.setNotification(Notification.ios(title, extras))
								.build();
	}
	private PushPayload buildPushObject_Alias_IOS(String type, String title,Map<String, String> extras,String[] alias) {
		if(null == extras){
			extras = new HashMap<String,String>();
		}
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
								.setNotification(Notification.ios(title, extras))
								.build();
	}
}
