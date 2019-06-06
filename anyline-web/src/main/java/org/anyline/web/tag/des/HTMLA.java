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


package org.anyline.web.tag.des;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
import org.anyline.web.tag.BaseBodyTag;

/**
 * 加密
 * 
 * @author Administrator
 * 
 */
public class HTMLA extends BaseBodyTag {
	private static final long serialVersionUID = 1L;

	private String id;
	private String name;
	private String href;
	private String clazz;
	private String style;
	private String title;
	private String target;
	private String params;
	private String shape;
	private String onmouseover;
	private String onmouseout;
	private String onclick;
	private boolean union = true;		//key value合并加密
	private boolean encryptKey = true;
	private boolean encryptValue = true;

	public int doEndTag() throws JspException {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("<a");
			if (null != id) {
				buffer.append(" id = \"" + id + "\"");
			}
			if (null != name) {
				buffer.append(" name = \"" + name + "\"");
			}
			if (null != href) {
				String url = href;
				if(BasicUtil.isNotEmpty(params)){
					if(url.contains("?")){
						url = url + "&" + params;
					}else{
						url = url + "?" + params;
					}
				}
				buffer.append(" href = \"" + WebUtil.encryptUrl(url, union, encryptKey, encryptValue) + "\"");
			}
			if (null != clazz) {
				buffer.append(" class = \"" + clazz + "\"");
			}
			if (null != style) {
				buffer.append(" style = \"" + style + "\"");
			}
			if (null != title) {
				buffer.append(" title = \"" + title + "\"");
			}
			if (null != target) {
				buffer.append(" target = \"" + target + "\"");
			}
			if (null != shape) {
				buffer.append(" shape = \"" + shape + "\"");
			}
			if (null != onmouseover) {
				buffer.append(" onmouseover = \"" + onmouseover + "\"");
			}
			if (null != onmouseout) {
				buffer.append(" onmouseout id = \"" + onmouseout + "\"");
			}
			if (null != onclick) {
				buffer.append(" onclick = \"" + onclick + "\"");
			}
			buffer.append(">");
			if (null != body) {
				buffer.append(body);
			}
			buffer.append("</a>");
			JspWriter out = pageContext.getOut();
			out.print(buffer.toString());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			release();
		}
		return EVAL_PAGE;
	}

	@Override
	public void release() {
		super.release();
		id = null;
		name = null;
		href = null;
		clazz = null;
		style = null;
		title = null;
		target = null;
		shape = null;
		onmouseover = null;
		onmouseout = null;
		onclick = null;
		body = null;
		params = null;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getClazz() {
		return clazz;
	}

	public void setClazz(String clazz) {
		this.clazz = clazz;
	}

	public String getStyle() {
		return style;
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getShape() {
		return shape;
	}

	public void setShape(String shape) {
		this.shape = shape;
	}

	public String getOnmouseover() {
		return onmouseover;
	}

	public void setOnmouseover(String onmouseover) {
		this.onmouseover = onmouseover;
	}

	public String getOnmouseout() {
		return onmouseout;
	}

	public void setOnmouseout(String onmouseout) {
		this.onmouseout = onmouseout;
	}

	public String getOnclick() {
		return onclick;
	}

	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}

	public boolean isEncryptKey() {
		return encryptKey;
	}

	public void setEncryptKey(boolean encryptKey) {
		this.encryptKey = encryptKey;
	}

	public boolean isEncryptValue() {
		return encryptValue;
	}

	public void setEncryptValue(boolean encryptValue) {
		this.encryptValue = encryptValue;
	}

	public boolean isUnion() {
		return union;
	}

	public void setUnion(boolean union) {
		this.union = union;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

}