

package org.anyline.tag.des;

import org.anyline.tag.ComponentTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.WebUtil;
import org.apache.log4j.Logger;
/**
 * 加密
 * @author Administrator
 *
 */
public class HTMLInput extends ComponentTag{
	private static final long serialVersionUID = 1L;

	public void createHead(Object obj){
		setEncryptKey(true);
		if("hidden".equalsIgnoreCase(type)){
			setEncryptValue(true);
		}
		builder.append("\t\t\t<input ");
		createAttribute();
		createValue(null);
		builder.append(">");
	}
	public void createBody(Object obj){
		
	}
	public void createEnd(){
		builder.append("</input>");
		if("checkbox".equalsIgnoreCase(type) || "radio".equals(type)){
			if(BasicUtil.isNotEmpty(body)){
				builder.append("<label for=\"");
				builder.append(WebUtil.encryptHttpRequestParamKey(id));
				builder.append("\">");
				builder.append(body);
				builder.append("</label>");
			}
		}
	}
	private void createValue(Object data){
		if(null != data && null != property){
			try{
				Object v = BeanUtil.getFieldValue(data, property);
				if(null != v){
					value = v.toString();
				}
			}catch(Exception e){
				LOG.error(e);
			}
		}
		if(isEncryptValue){
			value = WebUtil.encryptHttpRequestParamValue(value);
		}
		value = BasicUtil.nvl(value,"").toString();
		builder.append(" value=\"").append(value).append("\"");
		
	}
}