package org.anyline.jpush.util;

import java.util.HashMap;
import java.util.Map;

import cn.jiguang.commom.ClientConfig;
import cn.jpush.api.JPushClient;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;
import cn.jpush.api.push.model.audience.AudienceTarget;
import cn.jpush.api.push.model.notification.Notification;

public class JPushUtil {

	private static final String appKey = JPushConfig.APP_KEY;
	private static final String masterSecret = JPushConfig.MASTER_SECRET;
	static ClientConfig config;
	static JPushClient jpush;
	static {
		config = ClientConfig.getInstance();
		config.setApnsProduction(false); 
		config.setTimeToLive(60 * 60 * 24);
		jpush = new JPushClient(masterSecret, appKey, null, config);
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
	public static boolean pushByTag(String type, String title, String msg, Map<String,String> extras, String ... tags){
		boolean result = false;
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		PushPayload pl = buildPushObject_ios_audienceMore_messageWithExtras(type, title, msg, extras, tags);
		try {
			PushResult pr = jpush.sendPush(pl);
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
	public static boolean pushByAlias(String type, String title, String msg, Map<String,String> extras, String ... alias){
		boolean result = false;
		if(null == extras){
			extras = new HashMap<String,String>();
		}
		PushPayload pl = buildPushObject_ios_audienceMore_messageWithExtrasAils(type, title, msg, extras, alias);
		try {
			PushResult pr = jpush.sendPush(pl);
			result = pr.isResultOK();
		} catch (Exception e) {
			e.printStackTrace();
			result = false;
		}
		return result;
	}
	
	public static PushPayload buildPushObject_ios_audienceMore_messageWithExtras(String type, String title, String msg, Map<String, String> extras,String ... tags) {
		return PushPayload.newBuilder()
				.setPlatform(Platform.android_ios())
				.setAudience(Audience.newBuilder()
						.addAudienceTarget(AudienceTarget.tag_and(tags))
						.build())
						.setMessage(Message.newBuilder()
								.setMsgContent(type)
								.addExtras(extras)
								.build())
								.setNotification(Notification.android(msg, title, null))
								.build();
	}
	public static PushPayload buildPushObject_ios_audienceMore_messageWithExtrasAils(String type, String title, String msg, Map<String, String> extras,String ... alias) {
		return PushPayload.newBuilder()
				.setPlatform(Platform.android_ios())
				.setAudience(Audience.newBuilder()
						.addAudienceTarget(AudienceTarget.alias(alias))
						.build())
						.setMessage(Message.newBuilder()
								.setMsgContent(type)
								.addExtras(extras)
								.build())
								.setNotification(Notification.android(msg, title, null))
								.build();
	}

}
