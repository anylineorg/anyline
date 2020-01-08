/* 
 * Copyright 2006-2015 www.anyline.org
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


package org.anyline.weixin.mp.tag; 
 
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.net.HttpUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.FileUtil;
import org.anyline.web.tag.BaseBodyTag;
import org.anyline.weixin.mp.util.WXMPUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * 微信 wx.config
 *
 */ 
public class Config extends BaseBodyTag { 
	private static final long serialVersionUID = 1L; 
	private boolean debug = false;
	private String apis= "";
	private String key = "";
	private String server = ""; 
	public int doEndTag() throws JspException { 
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		try{
			WXMPUtil util = WXMPUtil.getInstance(key);
			if(null != util){
				String url = "";
				if(BasicUtil.isEmpty(server)){
					server = util.getConfig().WEB_SERVER;
				}
				if(BasicUtil.isEmpty(server)){
					server = HttpUtil.parseHost(request.getHeader("Referer"));
				}
				url =  FileUtil.mergePath(server , BasicUtil.evl(request.getAttribute("javax.servlet.forward.request_uri"),"")+"");
				String param = request.getQueryString();
				if(BasicUtil.isNotEmpty(param)){
					url += "?" + param;
				}
				if(ConfigTable.isDebug() && log.isWarnEnabled()){
					log.warn("[config init][url:{}]",url);
				}
				Map<String,Object> map = util.jsapiSign(url);
				
				String config = "<script language=\"javascript\">\n";
				if(debug){
					String alert = "请注意url,经过代理的应用有可能造成域名不符(如localhost,127.0.0.1等),请在anyline-weixin-mp.xml中配置WEB_SERVER=http://www.xx.com\\n,并在微信后台设置服务器IP白名单";
					alert += "SIGN SRC: appId=" + util.getConfig().APP_ID + ",noncestr="+map.get("noncestr")
							+",jsapi_ticket="+map.get("jsapi_ticket")+",url="+url+",timestamp="+map.get("timestamp");
					config += "alert(\""+alert+"\");\n";
				}
				config += "";
				config += "wx.config({\n";
				config += "debug:"+debug+",\n";
				config += "appId:'"+util.getConfig().APP_ID+"',\n";
				config += "timestamp:"+map.get("timestamp")+",\n";
				config += "nonceStr:'"+map.get("noncestr") + "',\n";
				config += "signature:'"+map.get("sign")+"',\n";
				config += "jsApiList:[";
				String apiList[] = apis.split(",");
				int size = apiList.length;
				for(int i=0; i<size; i++){
					String api = apiList[i];
					api = api.replace("'", "").replace("\"", "");
					if(i>0){
						config += ",";
					}
					config += "'" + api + "'";
				}
				config += "]\n";
				config += "});\n";
				config += "</script>";
				JspWriter out = pageContext.getOut();
				out.println(config);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(ConfigTable.isDebug() && log.isWarnEnabled()){
				e.printStackTrace();
			} 
		} finally { 
			release(); 
		} 
		return EVAL_PAGE; 
	}
	public boolean isDebug() {
		return debug;
	}
	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	public String getApis() {
		return apis;
	}
	public void setApis(String apis) {
		this.apis = apis;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	 
}
