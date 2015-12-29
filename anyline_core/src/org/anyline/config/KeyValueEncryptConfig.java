
package org.anyline.config;

public class KeyValueEncryptConfig{
	private boolean required;				//是否必须
	private String field;					//属性名 :之前
	private String key;						//KEY	:之后
	private boolean isKeyEncrypt;
 private boolean isValueEncrypt;
	private boolean setEncrypt = false;		//是否已指定加密方式
	
	public boolean isSetEncrypt() {
		return setEncrypt;
	}
	public void setSetEncrypt(boolean setEncrypt) {
		this.setEncrypt = setEncrypt;
	}
	/**
	 * +CD:cd++
	 * @param config
	 * @param defKeyEncrypt		默认设置
	 * @param defValueEncrypt	默认设置
	 */
	public KeyValueEncryptConfig(String config, boolean defKeyEncrypt, boolean defValueEncrypt){
		if(null == config) return;
		isKeyEncrypt = defKeyEncrypt;
		isValueEncrypt = defValueEncrypt;
		field = config;
		key = config;
		if(config.contains(":")){
			String[] tmp = config.split(":");
			field = config.substring(0,config.indexOf(":"));
			key = config.substring(config.indexOf(":")+1);
		}
		//必须配置
		if(field.startsWith("+")){
			setRequired(true);
			field = field.substring(1);
		}
		//加密配置
		if(key.endsWith("+") || key.endsWith("-")){
			setEncrypt = true;
			String paramEncrypt = key.substring(key.length()-2,key.length()-1);
			String valueEncrypt = key.substring(key.length()-1);
			if("+".equals(paramEncrypt)){
				isKeyEncrypt = true;
			}
			if("+".equals(valueEncrypt)){
				isValueEncrypt = true;
			}
			key = key.replace("+", "").replace("-", "");
		}
	}
	public KeyValueEncryptConfig(String config){
		this(config,false,false);
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public boolean isKeyEncrypt() {
		return isKeyEncrypt;
	}
	public void setKeyEncrypt(boolean isKeyEncrypt) {
		this.isKeyEncrypt = isKeyEncrypt;
	}
	public boolean isValueEncrypt() {
		return isValueEncrypt;
	}
	public void setValueEncrypt(boolean isValueEncrypt) {
		this.isValueEncrypt = isValueEncrypt;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public boolean isRequired() {
		return required;
	}
	public void setRequired(boolean required) {
		this.required = required;
	}
	
}