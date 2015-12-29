
package org.anyline.tag.des;

import org.anyline.tag.ComponentTag;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.WebUtil;
/**
 * 加密textarea
 * @author Administrator
 *
 */
public class HTMLTextarea extends ComponentTag{
	private static final long serialVersionUID = 1L;

	public void createHead(Object obj){
		setEncrypt(true);
		builder.append("\t\t\t<textarea ");
		createAttribute();
		createValue(null);
		builder.append("/>");
	}
	public void createBody(Object obj){
		value = BasicUtil.nvl(body,value,"").toString();
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
				e.printStackTrace();
			}
		}
		if(!"text".equalsIgnoreCase(type)){
			value = WebUtil.encryptHttpRequestParamValue(value);
		}
		
	}
}