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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.weixin.tag;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.weixin.util.WXConfig;
import org.anyline.weixin.util.WXUtil;
import org.apache.log4j.Logger;
/**
 * 
 * 微信 wx.config
 *
 */
public class Config extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(Config.class);
	private boolean debug = false;
	private String apis= "";
/*
 * wx.config({
    debug: true, // 开启调试模式,调用的所有api的返回值会在客户端alert出来，若要查看传入的参数，可以在pc端打开，参数信息会通过log打出，仅在pc端时才会打印。
    appId: '', // 必填，公众号的唯一标识
    timestamp: , // 必填，生成签名的时间戳
    nonceStr: '', // 必填，生成签名的随机串
    signature: '',// 必填，签名，见附录1
    jsApiList: [] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
});
 * */
	public int doEndTag() throws JspException {
		HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
		try{
			String url = request.getScheme() + "://"+ request.getServerName()+ request.getAttribute("javax.servlet.forward.request_uri");
			String param = request.getQueryString();
			if(BasicUtil.isNotEmpty(param)){
				url += "?" + param;
			}
			Map<String,String> map = WXUtil.jsapiSign(url);
			
			String config = "<script language=\"javascript\">\n"; 
			config += "wx.config({\n";
			config += "debug:"+debug+",\n";
			config += "appId:'"+WXConfig.APP_ID+"',\n";
			config += "timestamp:"+map.get("timestamp")+",\n";
			config += "noncestr:'"+map.get("noncestr") + "',\n";
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
		} catch (Exception e) {
			log.error(e);
			if(ConfigTable.isDebug()){
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
	
}