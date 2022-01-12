/*
 * Copyright 2015-2022 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.anyline.wechat.wap.util;
 
import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.anyline.wechat.entity.WechatAuthInfo;
import org.anyline.wechat.entity.WechatUserInfo;
import org.anyline.wechat.util.WechatUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
 
public class WechatWapUtil {
	private static final Logger log = LoggerFactory.getLogger(WechatWapUtil.class);
	private static Hashtable<String, WechatWapUtil> instances = new Hashtable<String, WechatWapUtil>();
	private WechatWapConfig config;
	 
 
	public WechatWapUtil(WechatWapConfig config){
		this.config = config; 
	} 
	 
 
	public WechatWapUtil(String key, DataRow config){
		WechatWapConfig conf = WechatWapConfig.parse(key, config);
		this.config = conf; 
		instances.put(key, this); 
	} 
	public static WechatWapUtil getInstance(){
		return getInstance("default"); 
	} 
	public static WechatWapUtil getInstance(String key){
		if(BasicUtil.isEmpty(key)){
			key = "default"; 
		} 
		WechatWapUtil util = instances.get(key);
		if(null == util){ 
			WechatWapConfig config = WechatWapConfig.getInstance(key);
			if(null != config) {
				util = new WechatWapUtil(config);
				instances.put(key, util);
			}
		} 
		return util; 
	} 
	public WechatWapConfig getConfig(){
		return config; 
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
} 
