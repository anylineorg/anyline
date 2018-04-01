package org.anyline.weixin.entity;

import java.util.HashMap;
import java.util.Map;

public class TemplateMessage {
	private String touser;
	private String template_id;
	private String url;
	private String topcolor;
	Map<String,Map<String,String>> datas = new HashMap<String,Map<String,String>>();
	public TemplateMessage setUser(String user){
		this.touser = user;
		return this;
	}
	public TemplateMessage setTemplate(String template){
		this.template_id = template;
		return this;
	}
	public TemplateMessage setUrl(String url){
		this.url = url;
		return this;
	}
	public TemplateMessage setTopColor(String color){
		this.topcolor = color;
		return this;
	}
	public TemplateMessage addData(String key, String value, String color){
		Map<String,String> data = datas.get(key);
		if(data == null){
			data = new HashMap<String,String>();
		}
		data.put("value", value);
		data.put("color", color);
		datas.put(key, data);
		return this;
	}
	
	public String getTouser() {
		return touser;
	}
	public String getTemplate_id() {
		return template_id;
	}
	public String getUrl() {
		return url;
	}
	public String getTopcolor() {
		return topcolor;
	}
	public Map<String, Map<String, String>> getDatas() {
		return datas;
	}
}
