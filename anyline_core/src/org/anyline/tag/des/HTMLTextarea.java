

package org.anyline.tag.des;

import org.anyline.tag.ComponentTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.WebUtil;
import org.apache.log4j.Logger;
/**
 * 加密textarea
 * @author Administrator
 *
 */
public class HTMLTextarea extends ComponentTag{
	private static final long serialVersionUID = 1L;

	public void createHead(Object obj){
		setEncryptKey(true);
		builder.append("\t\t\t<textarea ");
		createAttribute();
		createValue(null);
		builder.append("/>");
	}
	public void createBody(Object obj){
		value = BasicUtil.nvl(body,value,"").toString();
		if(isEncryptValue){
			value = WebUtil.encryptValue(value);
		}
		builder.append(value);
	}
	public void createEnd(){
		builder.append("</textarea>");
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
		if(!"text".equalsIgnoreCase(type)){
			value = WebUtil.encryptHttpRequestParamValue(value);
		}
		
	}
}