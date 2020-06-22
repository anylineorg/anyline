/* 
 * Copyright 2006-2020 www.anyline.org
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


package org.anyline.web.tag;


import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.qq.mp.util.QQMPConfig;
import org.anyline.qq.util.QQConfig;
import org.anyline.util.BasicUtil;
import org.anyline.util.CodeUtil;
import org.anyline.wechat.mp.util.WechatMPConfig;
import org.anyline.wechat.mp.util.WechatMPUtil;
import org.anyline.wechat.util.WechatConfig;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Auth extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private String key = "default";
	private String appid;
	private String type;	//wechat:微信 qq:QQ
	private String redirect;
	private String state;
	private boolean encode;//是否将state编码后存储到servlet中
	private String scope;
	private boolean auto;
	private String id;
	private String params;
	 
	public int doEndTag() {
		boolean result = true; 
		String html = "";
		String url = "";
		if(BasicUtil.isEmpty(id)){
			id = BasicUtil.getRandomLowerString(10);
		}
		 
		try {
			log.warn("[第三方登录][type:{}]",type);
			if(encode){
				String stateValue = state;
				state = BasicUtil.getRandomLowerString(20);
				DataSet states = (DataSet)pageContext.getServletContext().getAttribute("auth_states");
				if(null == states){
					states = new DataSet();
					pageContext.getServletContext().setAttribute("auth_states", states);
				}else{
					//清空过期
					int size = states.size();
					for(int i=size-1;i>=0; i--){
						DataRow item = states.getRow(i);
						if(item.isExpire(1000*60*5)){
							states.remove(item);
						}
					}
				}
				DataRow row = new DataRow();
				row.put("value", stateValue);
				row.put("key", state);
				states.add(row);
				
			}
			
			if("wx".equalsIgnoreCase(type) || "wechat".equalsIgnoreCase(type) || "weixin".equalsIgnoreCase(type)){
				WechatConfig wechatConfig = WechatMPConfig.getInstance(key);
				if(null == wechatConfig){
					log.warn("[第三方登录][微信配置文件不存在][key:{}]",key);
					result = false;
				}else{
					WechatConfig.SNSAPI_SCOPE apiScope = WechatConfig.SNSAPI_SCOPE.BASE;
					if(WechatConfig.SNSAPI_SCOPE.USERINFO.getCode().equals(scope) || "info".equals(scope)){
						apiScope = WechatConfig.SNSAPI_SCOPE.USERINFO;
					}
					url = WechatMPUtil.ceateAuthUrl(key, redirect, apiScope, state);
//					if(BasicUtil.isEmpty(appid)){
//						appid = WechatConfig.APP_ID;
//					}
//					Map<String,String> map = new HashMap<String,String>();
//					if(null != params){
//						String[] items = params.split(",");
//						for(String item:items){
//							String[] kv = item.split(":");
//							if(kv.length ==2){
//								map.put(kv[0], kv[1]);
//							}
//						}
//					}
//					if(BasicUtil.isEmpty(scope)){
//						scope = "snsapi_base";
//					}
//					if(BasicUtil.isEmpty(redirect)){
//						redirect = WechatConfig.OAUTH_REDIRECT_URL;
//					}
//					if(BasicUtil.isEmpty(redirect)){
//						redirect = WechatProgrameConfig.getInstance().OAUTH_REDIRECT_URL;
//					}
//					redirect = URLEncoder.encode(redirect, "UTF-8");
//					url =  WechatConfig.URL_OAUTH + "?appid="+appid+"&redirect_uri="+redirect+"&response_type=code&scope="+scope+"&state="+state+",app:"+key+"#wechat_redirect";
				}
			}else if("qq".equalsIgnoreCase(type)){
				QQMPConfig qqconfig = QQMPConfig.getInstance(key);
				if(null == qqconfig){
					log.warn("[第三方登录][QQ配置文件不存在][key:{}]",key);
					result = false;
				}else{
					if(BasicUtil.isEmpty(appid)){
						appid = qqconfig.APP_ID;
					}
					Map<String,String> map = new HashMap<String,String>();
					if(null != params){
						String[] items = params.split(",");
						for(String item:items){
							String[] kv = item.split(":");
							if(kv.length ==2){
								map.put(kv[0], kv[1]);
							}
						}
					}
					String response_type = "code";
					if(BasicUtil.isEmpty(scope)){
						scope = "get_user_info";
					}
					if(BasicUtil.isEmpty(redirect)){
						redirect = qqconfig.OAUTH_REDIRECT_URL;
					}
					if(BasicUtil.isEmpty(redirect)){
						redirect = QQMPConfig.getInstance().OAUTH_REDIRECT_URL;
					}
					redirect = CodeUtil.urlEncode(redirect, "UTF-8");
					url =  QQConfig.URL_OAUTH + "?client_id="+appid+"&response_type="+response_type+"&redirect_uri="+redirect+"&scope="+scope+"&state="+state+",app:"+key;
				}
			}
			log.warn("[第三方登录][result:{}][url:{}]",result,url);
			if(result){
				html = "<a href=\""+url+"\" id=\""+id+"\">";
				if(BasicUtil.isNotEmpty(body)){
					html += body;
				}
				html += "</a>";
				if(auto){
					//((HttpServletResponse)pageContext.getResponse()).sendRedirect(url);
					html += "<script>location.href = \""+url+"\";</script>";
					//return EVAL_PAGE;

				}
			}else{
				log.error("[第三方登录][登录配置异常]");
				html = "登录配置异常";
			}
			log.warn("[第三方登录][result:{}][url:{}][html:{}]",result,url,html);
            JspWriter out = pageContext.getOut();
            out.print(html);
        } catch (Exception e) {
			e.printStackTrace(); 
		}finally{ 
			release(); 
		} 
		return EVAL_PAGE;// 标签执行完毕之后继续执行下面的内容 
	} 
	@Override 
	public void release() { 
		super.release(); 
		type = null;
		appid = null;
		redirect = null;
		params = null;
		body = null;
		scope = null;
		state = null;
		auto = false;
		id = null;
		encode = false;
		key = null; 
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getAppid() {
		return appid;
	}

	public void setAppid(String appid) {
		this.appid = appid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public boolean isAuto() {
		return auto;
	}

	public void setAuto(boolean auto) {
		this.auto = auto;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public boolean isEncode() {
		return encode;
	}

	public void setEncode(boolean encode) {
		this.encode = encode;
	} 
	 
} 
