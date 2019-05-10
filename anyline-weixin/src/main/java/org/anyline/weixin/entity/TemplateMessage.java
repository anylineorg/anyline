package org.anyline.weixin.entity;

import java.util.HashMap;
import java.util.Map;

public class TemplateMessage {
	private String touser;
	private String template_id;
	private String url;
	private String topcolor;
	Map<String,Map<String,String>> data = new HashMap<String,Map<String,String>>();
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
		Map<String,String> dt = data.get(key);
		if(dt == null){
			dt = new HashMap<String,String>();
		}
		dt.put("value", value);
		dt.put("color", color);
		data.put(key, dt);
		return this;
	}
	public TemplateMessage addData(String key, String value){
		return addData(key, value, "#173177");
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
	public Map<String, Map<String, String>> getData() {
		return data;
	}
}
