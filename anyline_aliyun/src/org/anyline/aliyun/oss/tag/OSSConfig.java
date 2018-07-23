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


package org.anyline.aliyun.oss.tag;

import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.aliyun.oss.util.OSSUtil;
import org.anyline.tag.BaseBodyTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.DateUtil;
import org.apache.log4j.Logger;
public class OSSConfig extends BaseBodyTag {
	private static final long serialVersionUID = 1L;
	private static Logger log = Logger.getLogger(OSSConfig.class);
	private boolean debug = false;
	private int expire = 0; 
	private String dir = "";
	private String key = "default";
	private String var = "aliyun_oss_data";
	public int doEndTag() throws JspException {
		try{
			OSSUtil util = OSSUtil.getInstance(key);
			if(BasicUtil.isNotEmpty(dir)){
				String yyyy = DateUtil.format("yyyy");
				String yy = DateUtil.format("yy");
				String mm = DateUtil.format("MM");
				String dd = DateUtil.format("dd");
				dir = dir.replace("{yyyy}", yyyy);
				dir = dir.replace("{yy}", yy);
				dir = dir.replace("{MM}", mm);
				dir = dir.replace("{mm}", mm);
				dir = dir.replace("{dd}", dd);
				dir = dir.replace("{y}", yyyy);
				dir = dir.replace("{m}", mm);
				dir = dir.replace("{d}", dd);
			}
			Map<String,String> map = util.signature(dir, expire);
			if(BasicUtil.isEmpty(var)){
				var = "aliyun_oss_data";
			}
			StringBuffer script = new StringBuffer();
			script.append("<script>\n");
			script.append("var ").append(var).append(" = ").append(BeanUtil.map2json(map)).append(";\n");
//			script.append("var " + var + " = new FormData();\n");
//			script.append(var).append(".append('policy',").append("aliyun_oss_config['policy']);\n");
//			script.append(var).append(".append('OSSAccessKeyId',").append("aliyun_oss_config['accessid']);\n");
//			script.append(var).append(".append('signature',").append("aliyun_oss_config['signature']);\n");
//			script.append(var).append(".append('dir',").append("aliyun_oss_config['dir']);\n");
//			script.append(var).append(".append('policy',").append("aliyun_oss_config['policy']);\n");
//			script.append(var).append(".append('policy',").append("aliyun_oss_config['policy']);\n");
//			script.append(var).append(".append('success_action_status','200');\n");
			script.append("</script>\n");
			JspWriter out = pageContext.getOut();
			out.println(script);
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
	public String getKey() {
		return key;
	}
	
	public int getExpire() {
		return expire;
	}
	public void setExpire(int expire) {
		this.expire = expire;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getVar() {
		return var;
	}
	public void setVar(String var) {
		this.var = var;
	}
	
}