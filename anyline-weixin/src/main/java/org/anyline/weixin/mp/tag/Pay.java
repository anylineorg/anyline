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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.ConfigTable;
import org.anyline.web.tag.BaseBodyTag;
import org.anyline.weixin.mp.util.WXMPUtil;
import org.anyline.weixin.util.WXUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * 微信 wx.config
 *
 */
public class Pay extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static final Logger log = LoggerFactory.getLogger(Pay.class);
	private boolean debug = false;
	private String prepay= "";
	private String success = null;
	private String fail = null;
	private String key;
	public int doEndTag() throws JspException {
		try{
			WXMPUtil util = WXMPUtil.getInstance(key);
			String timestamp = System.currentTimeMillis()/1000+"";
			String random = BasicUtil.getRandomLowerString(20);
			String pkg = "prepay_id="+prepay;
			Map<String,Object> params = new HashMap<String,Object>();
			params.put("package", pkg);
			params.put("timeStamp", timestamp);
			params.put("appId", util.getConfig().APP_ID);
			params.put("nonceStr", random);
			params.put("signType", "MD5");
			String sign = WXUtil.sign(util.getConfig().PAY_API_SECRET, params);
			StringBuilder builder = new StringBuilder();
			
			builder.append("<script language=\"javascript\">\n");
			builder.append("	function onBridgeReady() {\n");
			builder.append("		WeixinJSBridge.invoke('getBrandWCPayRequest', {\n");
			builder.append("			'appId':'").append(util.getConfig().APP_ID).append("',\n");
			builder.append("			'timeStamp':'").append(timestamp).append("',\n");
			builder.append("			'nonceStr':'").append(random).append("',\n");
			builder.append("			'package':'").append(pkg).append("',\n");
			builder.append("			'signType':'MD5',\n");
			builder.append("			'paySign':'").append(sign).append("'\n");
			builder.append("		}, function(res) {\n");
			builder.append("			if(res.err_msg == \"get_brand_wcpay_request:ok\") {\n");
			if(null != success){
			builder.append("				").append(success).append("(res);\n");
			}
			builder.append("			}else{\n");
			if(null != fail){
			builder.append("				").append(fail).append("(res);\n");
			}
			builder.append("			}");
			builder.append("		});\n");
			builder.append("	}\n");
			builder.append("	if (typeof WeixinJSBridge == 'undefined') {\n");
			builder.append("		if (document.addEventListener) {\n");
			builder.append("			document.addEventListener('WeixinJSBridgeReady', onBridgeReady, false);\n");
			builder.append("		} else if (document.attachEvent) {\n");
			builder.append("			document.attachEvent('WeixinJSBridgeReady', onBridgeReady);\n");
			builder.append("			document.attachEvent('onWeixinJSBridgeReady', onBridgeReady);\n");
			builder.append("		}\n");
			builder.append("	} else {\n");
			builder.append("		onBridgeReady();\n");
			builder.append("	}\n");
			builder.append("</script>");
			JspWriter out = pageContext.getOut();
			out.println(builder.toString());
		} catch (Exception e) {
			e.printStackTrace();
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
	public String getPrepay() {
		return prepay;
	}
	public void setPrepay(String prepay) {
		this.prepay = prepay;
	}
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getFail() {
		return fail;
	}
	public void setFail(String fail) {
		this.fail = fail;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	
}