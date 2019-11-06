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



package org.anyline.web.tag;


import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.anyline.util.BasicUtil;
import org.anyline.util.WebUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ComponentTag extends BaseBodyTag{
	private static final long serialVersionUID = 1L;
	protected static final Logger log = LoggerFactory.getLogger(ComponentTag.class);
	
	protected HttpServletRequest request; 
	protected HttpSession session;
	protected ServletContext servlet;

	protected String title;			//标题
	protected String property;		//KEY
	protected int index;
	protected String parentId;
	protected StringBuilder builder;
	
	protected boolean encryptKey;	//id name是否加密
	protected boolean encryptValue; //value 是否加密  主要针对有默认值 的hidden
	
	public int doStartTag() throws JspException {
		request = (HttpServletRequest)pageContext.getRequest();
		session = pageContext.getSession();
		servlet = pageContext.getServletContext();
        return super.doStartTag();
    }
	 public int doEndTag() throws JspException {
		try{
			createTag(null);
			//输出
			JspWriter out = pageContext.getOut();
			try{
				out.print(builder);
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try{
					//out.clear();
					//out.close();
				}catch(Exception ex){}
				release();
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			release();
		}
        return super.doEndTag();   
	 }
		public String createTag(Object data){
			builder = new StringBuilder();
			createHead(data);
			createBody(data);
			createEnd();
			return builder.toString();
		}
		/**
		 * 创建标签头
		 * @return
		 */
		protected abstract void createHead(Object data);
		/**
		 * 创建标签体
		 * @param data
		 */
		protected abstract void createBody(Object data);
		/**
		 * 创建标签尾
		 * @return
		 */
		protected abstract void createEnd();
		public void release() {
		 accesskey					= null; //设置或获取对象的快捷键。										
		 atomicselection			= null; //指定元素及其内容是否可以一不可见单位统一选择。										
		 autocomplete				= null; //设置或获取对象的自动完成状态。
		 begin						= null; //设置或获取时间线在该元素上播放前的延迟时间。
		 clazz						= null; //设置或获取对象的类。
		 contenteditable			= null; //设置或获取表明用户是否可编辑对象内容的字符串。
		 datafld					= null; //设置或获取由datasrc属性指定的绑定到指定对象的给定数据源的字段。
		 datasrc					= null; //设置或获取用于数据绑定的数据源。
		 dir						= null; //设置或获取对象的阅读顺序。
		 disabled					= null; //设置或获取控件的状态。
		 end						= null; //设置或获取表明元素结束时间的值，或者元素设置为重复的简单持续终止时间。
		 hidefocus					= null; //设置或获取表明对象是否显式标明焦点的值。
		 id							= null; //获取标识对象的字符串。
		 lang						= null; //设置或获取要使用的语言。
		 checked 					= null;	//选中
		 language					= null; //设置或获取当前脚本编写用的语言。
		 maxlength					= null; //设置或获取用户可在文本控件中输入的最多字符数。
		 name						= null; //设置或获取对象的名称。
		 readonly					= null; //设置或获取表明对象内容是否为只读的值。
		 size						= null; //设置或获取控件的大小。
		 style						= null; //为该设置元素设置内嵌样式。
		 syncmaster					= null; //设置或获取时间容器是否必须在此元素上同步回放。
		 systembitrate				= null; //获取系统中大约可用带宽的bps。
		 systemcaption				= null; //表明是否要显示文本来代替演示的的音频部分。
		 systemlanguage				= null; //表明是否在用户计算机上的选项设置中选中了给定语言。
		 systemoverduborsubtitle	= null; //指定针对那些正在观看演示但对被播放的音频所使用的语言并不熟悉的用户来说是否要渲染配音或字幕。
		 tabindex					= null; //设置或获取定义对象的tab	顺序的索引。
		 timecontainer				= null; //设置或获取与元素关联的时间线类型。
		 type						= null; //获取或初始设置对象所代表的input控件类型。
		 unselectable				= null; //指定该元素不可被选中。
		 vcard_name					= null; //设置或获取对象的vcard值，以便用于自动完成框。
		 width						= null; //设置或获取对象的计算宽度。
		 onactivate					= null; //当对象设置为活动元素时触发。
		 onafterupdate				= null; //当成功更新数据源对象中的关联对象后在数据绑定对象上触发。
		 onbeforeactivate			= null; //对象要被设置为当前元素前立即触发。
		 onbeforecut				= null; //当选中区从文档中删除之前在源对象触发。
		 onbeforedeactivate			= null; //在activeElement从当前对象变为父文档其它对象之前立即触发。
		 onbeforeeditfocus			= null; //在包含于可编辑元素内的对象进入用户界面激活状态前或可编辑容器变成控件选中区前触发。
		 onbeforepaste				= null; //在选中区从系统剪贴板粘贴到文档前在目标对象上触发。
		 onbeforeupdate				= null; //当成功更新数据源对象中的关联对象前在数据绑定对象上触发。
		 onblur						= null; //在对象失去输入焦点时触发。
		 onchange					= null; //当对象或选中区的内容改变时触发。
		 onclick					= null; //在用户用鼠标左键单击对象时触发。
		 oncontextmenu				= null; //在用户使用鼠标右键单击客户区打开上下文菜单时触发。
		 oncontrolselect			= null; //当用户将要对该对象制作一个控件选中区时触发。
		 oncut						= null; //当对象或选中区从文档中删除并添加到系统剪贴板上时在源元素上触发。
		 ondblclick					= null; //当用户双击对象时触发。
		 ondeactivate				= null; //当activeElement从当前对象变为父文档其它对象时触发。
		 ondrag						= null; //当进行拖曳操作时在源对象上持续触发。
		 ondragend					= null; //当用户在拖曳操作结束后释放鼠标时在源对象上触发。
		 ondragenter				= null; //当用户拖曳对象到一个合法拖曳目标时在目标元素上触发。
		 ondragleave				= null; //当用户在拖曳操作过程中将鼠标移出合法拖曳目标时在目标对象上触发。
		 ondragover					= null; //当用户拖曳对象划过合法拖曳目标时持续在目标元素上触发。
		 ondragstart				= null; //当用户开始拖曳文本选中区或选中对象时在源对象上触发。
		 ondrop						= null; //当鼠标按钮在拖曳操作过程中释放时在目标对象上触发。
		 onerrorupdate				= null; //更新数据源对象中的关联数据出错时在数据绑定对象上触发。
		 onfilterchange				= null; //当可视滤镜更改状态或完成转换时触发。
		 onfocus					= null; //当对象获得焦点时触发。
		 onfocusin					= null; //当元素将要被设置为焦点之前触发。
		 onfocusout					= null; //在���动焦点到其它元素之后立即触发于当前拥有焦点的元��上触发��
		 onhelp						= null; //当用户在浏览器为当前窗口时按F1键时触发。
		 onkeydown					= null; //当用户按下键盘按键时触发。
		 onkeypress					= null; //当用户按下字面键时触发。
		 onkeyup					= null; //当用户释放键盘按键时触发。
		 onlosecapture				= null; //当对象失去鼠标捕捉时触发。
		 onmousedown				= null; //当用户用任何鼠标按钮单击对象时触发。
		 onmouseenter				= null; //当用户将鼠标指针移动到对象内时触发。
		 onmouseleave				= null; //当用户将鼠标指针移出对象边界时触发。
		 onmousemove				= null; //当用户将鼠标划过对象时触发。
		 onmouseout					= null; //当用户将鼠标指针移出对象边界时触发。
		 onmouseover				= null; //当用户将鼠标指针移动到对象内时触发。
		 onmouseup					= null; //当用户在鼠标位于对象之上时释放鼠标按钮时触发。
		 onmousewheel				= null; //当鼠标滚轮按钮旋转时触发。
		 onmove						= null; //当对象移动时触发。
		 onmoveend					= null; //当对象停止移动时触发。
		 onmovestart				= null; //当对象开始移动时触发。
		 onpaste					= null; //当用户粘贴数据以便从系统剪贴板向文档传送数据时在目标对象上触发。
		 onpropertychange			= null; //当在对象上发生对象上发生属性更改时触发。
		 onreadyTRAIN_STAGEchange			= null; //当对象状态变更时触发。
		 onresize					= null; //当对象的大小将要改变时触发。
		 onresizeend				= null; //当用户更改完控件选中区中对象的尺寸时触发。
		 onresizestart				= null; //当用户开始更改控件选中区中对象的尺寸时触发。
		 onselect					= null; //当当前选中区改变时触发。
		 onselectstart				= null; //对象将要被选中时触发。
		 ontimeerror				= null; //当特定时间错误发生时无条件触发，通常由将属性设置为无效值导致。
	}

	/**
	 * 添加标签属性
	 */
	protected void createAttribute(){
		String runId = id;
		if(BasicUtil.isNotEmpty(parentId)){
			runId = parentId + "_" + id + "_" + index;
		}
		if(encryptKey){
			runId = WebUtil.encryptHttpRequestParamKey(runId);
		}
		if(null != id						){builder.append(" id=\""						).append(runId					).append("\"");}//获取标识对象的字符串。
		if(null != accesskey				){builder.append(" accesskey=\""				).append(accesskey				).append("\"");}//设置或获取对象的快捷键。										
		if(null != atomicselection			){builder.append(" atomicselection=\""			).append(atomicselection		).append("\"");}//指定元素及其内容是否可以一不可见单位统一选择。										
		if(null != autocomplete				){builder.append(" autocomplete=\""				).append(autocomplete			).append("\"");}//设置或获取对象的自动完成状态。
		if(null != begin					){builder.append(" begin=\""					).append(begin					).append("\"");}//设置或获取时间线在该元素上播放前的延迟时间。
		if(null != clazz					){builder.append(" class=\""					).append(clazz					).append("\"");}//设置或获取对象的类。
		if(null != contenteditable			){builder.append(" contenteditable=\""			).append(contenteditable		).append("\"");}//设置或获取表明用户是否可编辑对象内容的字符串。
		if(null != datafld					){builder.append(" datafld=\""					).append(datafld				).append("\"");}//设置或获取由datasrc属性指定的绑定到指定对象的给定数据源的字段。
		if(null != datasrc					){builder.append(" datasrc=\""					).append(datasrc				).append("\"");}//设置或获取用于数据绑定的数据源。
		if(null != dir						){builder.append(" dir=\""						).append(dir					).append("\"");}//设置或获取对象的阅读顺序。
		if(null != disabled					){builder.append(" disabled=\""					).append(disabled				).append("\"");}//设置或获取控件的状态。
		if(null != end						){builder.append(" end=\""						).append(end					).append("\"");}//设置或获取表明元素结束时间的值，或者元素设置为重复的简单持续终止时间。
		if(null != hidefocus				){builder.append(" hidefocus=\""				).append(hidefocus				).append("\"");}//设置或获取表明对象是否显式标明焦点的值。
		if(null != lang						){builder.append(" lang=\""						).append(lang					).append("\"");}//设置或获取要使用的语言。
		if(null != language					){builder.append(" language=\""					).append(language				).append("\"");}//设置或获取当前脚本编写用的语言。
		if(null != maxlength				){builder.append(" maxlength=\""				).append(maxlength				).append("\"");}//设置或获取用户可在文本控件中输入的最多字符数。
		String runName = name;
		if(encryptKey){
			runName = WebUtil.encryptHttpRequestParamKey(runName);
		}
		if(null != name						){builder.append(" name=\""						).append(runName				).append("\"");}//设置或获取对象的名称。
		if(null != checked					){builder.append(" checked=\"checked\"");													   }//选中
		if(null != readonly					){builder.append(" readonly=\""					).append(readonly				).append("\"");}//设置或获取表明对象内容是否为只读的值。
		if(null != size						){builder.append(" size=\""						).append(size					).append("\"");}//设置或获取控件的大小。
		if(null != style					){builder.append(" style=\""					).append(style					).append("\"");}//为该设置元素设置内嵌样式。
		if(null != syncmaster				){builder.append(" syncmaster=\""				).append(syncmaster				).append("\"");}//设置或获取时间容器是否必须在此元素上同步回放。
		if(null != systembitrate			){builder.append(" systembitrate=\""			).append(systembitrate			).append("\"");}//获取系统中大约可用带宽的bps。
		if(null != systemcaption			){builder.append(" systemcaption=\""			).append(systemcaption			).append("\"");}//表明是否要显示文本来代替演示的的音频部分。
		if(null != systemlanguage			){builder.append(" systemlanguage=\""			).append(systemlanguage			).append("\"");}//表明是否在用户计算机上的选项设置中选中了给定语言。
		if(null != systemoverduborsubtitle	){builder.append(" systemoverduborsubtitle=\""	).append(systemoverduborsubtitle).append("\"");}//指定针对那些正在观看演示但对被播放的音频所使用的语言并不熟悉的用户来说是否要渲染配音或字幕。
		if(null != tabindex					){builder.append(" tabindex=\""					).append(tabindex				).append("\"");}//设置或获取定义对象的tab	顺序的索引。
		if(null != timecontainer			){builder.append(" timecontainer=\""			).append(timecontainer			).append("\"");}//设置或获取与元素关联的时间线类型。
		if(null != title					){builder.append(" title=\""					).append(title					).append("\"");}//设置或获取对象的咨询信息(工具提示)。
		if(null != type						){builder.append(" type=\""						).append(type					).append("\"");}//获取或初始设置对象所代表的input控件类型。
		if(null != unselectable				){builder.append(" unselectable=\""				).append(unselectable			).append("\"");}//指定该元素不可被选中。
		//if(null != value					){builder.append(" value=\"").append(value					).append("\"");}//设置或获取控件对象的显示值。当控件对象提交时此值将返回给服务器。
		if(null != vcard_name				){builder.append(" vcard_name=\""				).append(vcard_name				).append("\"");}//设置或获取对象的vcard值，以便用于自动完成框。
		if(null != width					){builder.append(" width=\""					).append(width					).append("\"");}//设置或获取对象的计算宽度。
		if(null != onactivate				){builder.append(" onactivate=\""				).append(onactivate				).append("\"");}//当对象设置为活动元素时触发。
		if(null != onafterupdate			){builder.append(" onafterupdate=\""			).append(onafterupdate			).append("\"");}//当成功更新数据源对象中的关联对象后在����绑定对象上触发。
		if(null != onbeforeactivate			){builder.append(" onbeforeactivate=\""			).append(onbeforeactivate		).append("\"");}//对象要被设置为当前元素前立即触发。
		if(null != onbeforecut				){builder.append(" onbeforecut=\""				).append(onbeforecut			).append("\"");}//当选中区从文档中删除之前在源对象触发。
		if(null != onbeforedeactivate		){builder.append(" onbeforedeactivate=\""		).append(onbeforedeactivate		).append("\"");}//在activeElement从当前对象变为父文档其它对象之前立即触发。
		if(null != onbeforeeditfocus		){builder.append(" onbeforeeditfocus=\""		).append(onbeforeeditfocus		).append("\"");}//在包���于可编辑元素内的对象进入用户界面激活状态前或可编辑容器变成控件选中区前触发。
		if(null != onbeforepaste			){builder.append(" onbeforepaste=\""			).append(onbeforepaste			).append("\"");}//在选中区从系统剪贴板粘贴到文档前在目标对象上触发。
		if(null != onbeforeupdate			){builder.append(" onbeforeupdate=\""			).append(onbeforeupdate			).append("\"");}//当成功更新数据源对象中的关联对象前在数据绑定对象上触发。
		if(null != onblur					){builder.append(" onblur=\""					).append(onblur					).append("\"");}//在对象失去输入焦点时触发。
		if(null != onchange					){builder.append(" onchange=\""					).append(onchange				).append("\"");}//当对象或选中区的内容改变时触发。
		if(null != onclick					){builder.append(" onclick=\""					).append(onclick				).append("\"");}//在用户用鼠标左键单击对象时触发。
		if(null != oncontextmenu			){builder.append(" oncontextmenu=\""			).append(oncontextmenu			).append("\"");}//在用户使用鼠标右键单击客户区打开上下文菜单时触发。
		if(null != oncontrolselect			){builder.append(" oncontrolselect=\""			).append(oncontrolselect		).append("\"");}//当用户将要对该对象制作一个控件选中区时触发。
		if(null != oncut					){builder.append(" oncut=\""					).append(oncut					).append("\"");}//当对象或选中区从文档中删除并添加到系统剪贴板上时在源元素上触发。
		if(null != ondblclick				){builder.append(" ondblclick=\""				).append(ondblclick				).append("\"");}//当用户双击对象时触发。
		if(null != ondeactivate				){builder.append(" ondeactivate=\""				).append(ondeactivate			).append("\"");}//当activeElement从当前对象变为父文档其它对象时触发。
		if(null != ondrag					){builder.append(" ondrag=\""					).append(ondrag					).append("\"");}//当进行拖曳操作时在源对象上持续触发。
		if(null != ondragend				){builder.append(" ondragend=\""				).append(ondragend				).append("\"");}//当用户在拖曳操作结束后释放鼠标时在源对象上触发。
		if(null != ondragenter				){builder.append(" ondragenter=\""				).append(ondragenter			).append("\"");}//当用户拖曳对象到一个合法拖曳目标时在目标元素上触发。
		if(null != ondragleave				){builder.append(" ondragleave=\""				).append(ondragleave			).append("\"");}//当用户在拖曳操作过程中将鼠标移出合法拖曳目标时在目标对象上触发。
		if(null != ondragover				){builder.append(" ondragover=\""				).append(ondragover				).append("\"");}//当用户拖曳对象划过合法拖曳目标时持续在目标元素上触发。
		if(null != ondragstart				){builder.append(" ondragstart=\""				).append(ondragstart			).append("\"");}//当用户开始拖曳文本选中区或选中对象时在源对象上触发。
		if(null != ondrop					){builder.append(" ondrop=\""					).append(ondrop					).append("\"");}//当鼠标按钮在拖曳操作过程中释放时在目标对象上触发。
		if(null != onerrorupdate			){builder.append(" onerrorupdate=\""			).append(onerrorupdate			).append("\"");}//更新数据源对象中的关联数据出错时在数据绑定对象上触发。
		if(null != onfilterchange			){builder.append(" onfilterchange=\""			).append(onfilterchange			).append("\"");}//当可视滤镜更改状态或完成转换时触发。
		if(null != onfocus					){builder.append(" onfocus=\""					).append(onfocus				).append("\"");}//当对象获得焦点时触发。
		if(null != onfocusin				){builder.append(" onfocusin=\""				).append(onfocusin				).append("\"");}//当元素将要被设置为焦点之前触发。
		if(null != onfocusout				){builder.append(" onfocusout=\""				).append(onfocusout				).append("\"");}//在移动焦点到其它元素之后立即触发于当前拥有焦点的元素上触发。
		if(null != onhelp					){builder.append(" onhelp=\""					).append(onhelp					).append("\"");}//当用户在浏览器为当前窗口时按F1键时触发。
		if(null != onkeydown				){builder.append(" onkeydown=\""				).append(onkeydown				).append("\"");}//当用户按下键盘按键时触发。
		if(null != onkeypress				){builder.append(" onkeypress=\""				).append(onkeypress				).append("\"");}//当用户按下字面键时触发。
		if(null != onkeyup					){builder.append(" onkeyup=\""					).append(onkeyup				).append("\"");}//当用户释放键盘按键时触发。
		if(null != onlosecapture			){builder.append(" onlosecapture=\""			).append(onlosecapture			).append("\"");}//当对象失去鼠标捕捉时触发。
		if(null != onmousedown				){builder.append(" onmousedown=\""				).append(onmousedown			).append("\"");}//当用户用任何鼠标按钮单击对象时触发。
		if(null != onmouseenter				){builder.append(" onmouseenter=\""				).append(onmouseenter			).append("\"");}//当用户将鼠标指针移动到对象内时触发。
		if(null != onmouseleave				){builder.append(" onmouseleave=\""				).append(onmouseleave			).append("\"");}//当用户将鼠标指针移出对象边界时触发。
		if(null != onmousemove				){builder.append(" onmousemove=\""				).append(onmousemove			).append("\"");}//当用户将鼠标划过对象时触发。
		if(null != onmouseout				){builder.append(" onmouseout=\""				).append(onmouseout				).append("\"");}//当用户将鼠标指针移出对象边界时触发。
		if(null != onmouseover				){builder.append(" onmouseover=\""				).append(onmouseover			).append("\"");}//当用户将鼠标指针移动到对象内时触发。
		if(null != onmouseup				){builder.append(" onmouseup=\""				).append(onmouseup				).append("\"");}//当用户在鼠标位于对象之上时释放鼠标按钮时触发。
		if(null != onmousewheel				){builder.append(" onmousewheel=\""				).append(onmousewheel			).append("\"");}//当鼠标滚轮按钮旋转时触发。
		if(null != onmove					){builder.append(" onmove=\""					).append(onmove					).append("\"");}//当对象移动时触发。
		if(null != onmoveend				){builder.append(" onmoveend=\""				).append(onmoveend				).append("\"");}//当对象停止移动时触发。
		if(null != onmovestart				){builder.append(" onmovestart=\""				).append(onmovestart			).append("\"");}//当对象开始移动时触发。
		if(null != onpaste					){builder.append(" onpaste=\""					).append(onpaste				).append("\"");}//当用户粘贴数据以便从系统剪贴板向文档传送数据时在目标对象上触发。
		if(null != onpropertychange			){builder.append(" onpropertychange=\""			).append(onpropertychange		).append("\"");}//当在对象上发生对象上发生属性更改时触发。
		if(null != onreadyTRAIN_STAGEchange	){builder.append(" onreadyTRAIN_STAGEchange=\""		).append(onreadyTRAIN_STAGEchange		).append("\"");}//当对象状态变更时触发。
		if(null != onresize					){builder.append(" onresize=\""					).append(onresize				).append("\"");}//当对象的大小将要改变时触发。
		if(null != onresizeend				){builder.append(" onresizeend=\""				).append(onresizeend			).append("\"");}//当用户更改完控件选中区中对象的尺寸时触发。
		if(null != onresizestart			){builder.append(" onresizestart=\""			).append(onresizestart			).append("\"");}//当用户开始更改控件选中区中对象的尺寸时触发。
		if(null != onselect					){builder.append(" onselect=\""					).append(onselect				).append("\"");}//当当前选中区改变时触发。
		if(null != onselectstart			){builder.append(" onselectstart=\""			).append(onselectstart			).append("\"");}//对象将要被选中时触发。
		if(null != ontimeerror				){builder.append(" ontimeerror=\""				).append(ontimeerror			).append("\"");}//当特定时间错误发生时无条件触发，通常由将属性设置为无效值导致。
	}
	protected String accesskey					; //设置或获取对象的快捷键。										
	protected String atomicselection			; //指定元素及其内容是否可以一不可见单位统一选择。										
	protected String autocomplete				; //设置或获取对象的自动完成状态。
	protected String begin						; //设置或获取时间线在该元素上播放前的延迟时间。
	protected String clazz						; //设置或获取对象的类。
	protected String contenteditable			; //设置或获取表明用户是否可编辑对象内容的字符串。
	protected String datafld					; //设置或获取由datasrc属性指定的绑定到指定对象的给定数据源的字段。
	protected String datasrc					; //设置或获取用于数据绑定的数据源。
	protected String dir						; //设置或获取对象的阅读顺序。
	protected String disabled					; //设置或获取控件的状态。
	protected String end						; //设置或获取表明元素结束时间的值，或者元素设置为重复的简单持续终止时间。
	protected String hidefocus					; //设置或获取表明对象是否显式标明焦点的值。
	protected String id							; //获取标识对象的字符串。
	protected String lang						; //设置或获取要使用的语言。
	protected String language					; //设置或获取当前脚本编写用的语言。
	protected String maxlength					; //设置或获取用户可在文本控件中输入的最多字符数。
	protected String name						; //设置或获取对象的名称。
	protected String checked					; //选中
	protected String readonly					; //设置或获取表明对象内容是否为只读的值。
	protected String size						; //设置或获取控件的大小。
	protected String style						; //为该设置元素设置内嵌样式。
	protected String syncmaster					; //设置或获取时间容器是否必须在此元素上同步回放。
	protected String systembitrate				; //获取系统中大约可用带宽的bps。
	protected String systemcaption				; //表明是否要显示文本来代替演示的的音频部分。
	protected String systemlanguage				; //表明是否在用户计算机上的选项设置中选中了给定语言。
	protected String systemoverduborsubtitle	; //指定针对那些正在观看演示但对被播放的音频所使用的语言并不熟悉的用户来说是否要渲染配音或字幕。
	protected String tabindex					; //设置或获取定义对象的tab	顺序的索引。
	protected String timecontainer				; //设置或获取与元素关联的时间线类型。
	protected String type						; //获取或初始设置对象所代表的input控件类型。
	protected String unselectable				; //指定该元素不可被选中。
	protected String vcard_name					; //设置或获取对象的vcard值，以便用于自动完成框。
	protected String width						; //设置或获取对象的计算宽度。
	protected String onactivate					; //当对象设置为活动元素时触发。
	protected String onafterupdate				; //当成功更新数据源对象中的关联对象后在数据绑定对象上触发。
	protected String onbeforeactivate			; //对象要被设置为当前元素前立即触发。
	protected String onbeforecut				; //当选中区从文档中删除之前在源对象触发。
	protected String onbeforedeactivate			; //在activeElement从当前对象变为父文档其它对象之前立即触发。
	protected String onbeforeeditfocus			; //在包含于可编辑元素内的对象进入用户界面激活状态前或可编辑容器变成控件选中区前触发。
	protected String onbeforepaste				; //在选中区从系统剪贴板粘贴到文档前在目标对象上触发。
	protected String onbeforeupdate				; //当成功更新数据源对象中的关联对象前在数据绑定对象上触发。
	protected String onblur						; //在对象失去输入焦点时触发。
	protected String onchange					; //当对象或选中区的内容改变时触发。
	protected String onclick					; //在用户用鼠标左键单击对象时触发。
	protected String oncontextmenu				; //在用户使用鼠标右键单击客户区打开上下文菜单时触发。
	protected String oncontrolselect			; //当用户将要对该对象制作一个控件选中区时触发。
	protected String oncut						; //当对象或选中区从文档中删除并添加到系统剪贴板上时在源元素上触发。
	protected String ondblclick					; //当用户双击对象时触发。
	protected String ondeactivate				; //当activeElement从当前对象变为父文档其它对象时触发。
	protected String ondrag						; //当进行拖曳操作时在源对象上持续触发。
	protected String ondragend					; //当用户在拖曳操作结束后释放鼠标时在源对象上触发。
	protected String ondragenter				; //当用户拖曳对象到一个合法拖曳目标时在目标元素上触发。
	protected String ondragleave				; //当用户在拖曳操作过程中将鼠标移出合法拖曳目标时在目标对象上触发。
	protected String ondragover					; //当用户拖曳对象划过合法拖曳目标时持续在目标元素上触发。
	protected String ondragstart				; //当用户开始拖曳文本选中区或选中对象时在源对象上触发。
	protected String ondrop						; //当鼠标按钮在拖曳操作过程中释放时在目标对象上触发。
	protected String onerrorupdate				; //更新数据源对象中的关联数据出错时在数据绑定对象上触发。
	protected String onfilterchange				; //当可视滤镜更改状态或完成转换时触发。
	protected String onfocus					; //当对象获得焦点时触发。
	protected String onfocusin					; //当元素将要被设置为焦点之前触发。
	protected String onfocusout					; //在移动焦点到其它元素之后立即触发于当前拥有焦点的元素上触发。
	protected String onhelp						; //当用户在浏览器为当前窗口时按F1键时触发。
	protected String onkeydown					; //当用户按下键盘按键时触发。
	protected String onkeypress					; //当用户按下字面键时触发。
	protected String onkeyup					; //当用户释放键盘按键时触发。
	protected String onlosecapture				; //当对象失去鼠标捕捉时触发。
	protected String onmousedown				; //当用户用任何鼠标按钮单击对象时触发。
	protected String onmouseenter				; //当用户将鼠标指针移动到对象内时触发。
	protected String onmouseleave				; //当用户将鼠标指针移出对象边界时触发。
	protected String onmousemove				; //当用户将鼠标划过对象时触发。
	protected String onmouseout					; //当用户将鼠标指针移出对象边界时触发。
	protected String onmouseover				; //当用户将鼠标指针移动到对象内时触发。
	protected String onmouseup					; //当用户在鼠标位于对象之上时释放鼠标按钮时触发。
	protected String onmousewheel				; //当鼠标滚轮按钮旋转时触发。
	protected String onmove						; //当对象移动时触发。
	protected String onmoveend					; //当对象停止移动时触发。
	protected String onmovestart				; //当对象开始移动时触发。
	protected String onpaste					; //当用户粘贴数据以便从系统剪贴板向文档传送数据时在目标对象上触发。
	protected String onpropertychange			; //当在对象上发生对象上发生属性更改时触发。
	protected String onreadyTRAIN_STAGEchange			; //当对象状态变更时触发。
	protected String onresize					; //当对象的大小将要改变时触发。
	protected String onresizeend				; //当用户更改完控��选��区中对象的尺寸时触发。
	protected String onresizestart				; //当用户开始更改控件选中区中对象的尺寸时触发。
	protected String onselect					; //当当前选中区改变时触发。
	protected String onselectstart				; //对象将要被���中时触发。
	protected String ontimeerror				; //当特定时间错误发生时无条件触发，通常由将属性设置为无效值导致。

	public HttpServletRequest getRequest() {
		return request;
	}
	public void setRequest(HttpServletRequest request) {
		this.request = request;
	}
	public HttpSession getSession() {
		return session;
	}
	public void setSession(HttpSession session) {
		this.session = session;
	}
	public ServletContext getServlet() {
		return servlet;
	}
	public void setServlet(ServletContext servlet) {
		this.servlet = servlet;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}

	public StringBuilder getBuilder() {
		return builder;
	}
	public void setBuilder(StringBuilder builder) {
		this.builder = builder;
	}
	public String getAccesskey() {
		return accesskey;
	}
	public void setAccesskey(String accesskey) {
		this.accesskey = accesskey;
	}
	public String getAtomicselection() {
		return atomicselection;
	}
	public void setAtomicselection(String atomicselection) {
		this.atomicselection = atomicselection;
	}
	public String getAutocomplete() {
		return autocomplete;
	}
	public void setAutocomplete(String autocomplete) {
		this.autocomplete = autocomplete;
	}
	public String getBegin() {
		return begin;
	}
	public void setBegin(String begin) {
		this.begin = begin;
	}
	public String getClazz() {
		return clazz;
	}
	public void setClass(String clazz) {
		this.clazz = clazz;
	}
	public void setClazz(String clazz) {
		this.clazz = clazz;
	}
	public String getContenteditable() {
		return contenteditable;
	}
	public void setContenteditable(String contenteditable) {
		this.contenteditable = contenteditable;
	}
	public String getDatafld() {
		return datafld;
	}
	public void setDatafld(String datafld) {
		this.datafld = datafld;
	}
	public String getDatasrc() {
		return datasrc;
	}
	public void setDatasrc(String datasrc) {
		this.datasrc = datasrc;
	}
	public String getDir() {
		return dir;
	}
	public void setDir(String dir) {
		this.dir = dir;
	}
	public String getDisabled() {
		return disabled;
	}
	public void setDisabled(String disabled) {
		this.disabled = disabled;
	}
	public String getEnd() {
		return end;
	}
	public void setEnd(String end) {
		this.end = end;
	}
	public String getHidefocus() {
		return hidefocus;
	}
	public void setHidefocus(String hidefocus) {
		this.hidefocus = hidefocus;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getLang() {
		return lang;
	}
	public void setLang(String lang) {
		this.lang = lang;
	}
	public String getLanguage() {
		return language;
	}
	public void setLanguage(String language) {
		this.language = language;
	}
	public String getMaxlength() {
		return maxlength;
	}
	public void setMaxlength(String maxlength) {
		this.maxlength = maxlength;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getReadonly() {
		return readonly;
	}
	public void setReadonly(String readonly) {
		this.readonly = readonly;
	}
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	public String getStyle() {
		return style;
	}
	public void setStyle(String style) {
		this.style = style;
	}
	public String getSyncmaster() {
		return syncmaster;
	}
	public void setSyncmaster(String syncmaster) {
		this.syncmaster = syncmaster;
	}
	public String getSystembitrate() {
		return systembitrate;
	}
	public void setSystembitrate(String systembitrate) {
		this.systembitrate = systembitrate;
	}
	public String getSystemcaption() {
		return systemcaption;
	}
	public void setSystemcaption(String systemcaption) {
		this.systemcaption = systemcaption;
	}
	public String getSystemlanguage() {
		return systemlanguage;
	}
	public void setSystemlanguage(String systemlanguage) {
		this.systemlanguage = systemlanguage;
	}
	public String getSystemoverduborsubtitle() {
		return systemoverduborsubtitle;
	}
	public void setSystemoverduborsubtitle(String systemoverduborsubtitle) {
		this.systemoverduborsubtitle = systemoverduborsubtitle;
	}
	public String getTabindex() {
		return tabindex;
	}
	public void setTabindex(String tabindex) {
		this.tabindex = tabindex;
	}
	public String getTimecontainer() {
		return timecontainer;
	}
	public void setTimecontainer(String timecontainer) {
		this.timecontainer = timecontainer;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getUnselectable() {
		return unselectable;
	}
	public void setUnselectable(String unselectable) {
		this.unselectable = unselectable;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public String getVcard_name() {
		return vcard_name;
	}
	public void setVcard_name(String vcard_name) {
		this.vcard_name = vcard_name;
	}
	public String getWidth() {
		return width;
	}
	public void setWidth(String width) {
		this.width = width;
	}
	public String getOnactivate() {
		return onactivate;
	}
	public void setOnactivate(String onactivate) {
		this.onactivate = onactivate;
	}
	public String getOnafterupdate() {
		return onafterupdate;
	}
	public void setOnafterupdate(String onafterupdate) {
		this.onafterupdate = onafterupdate;
	}
	public String getOnbeforeactivate() {
		return onbeforeactivate;
	}
	public void setOnbeforeactivate(String onbeforeactivate) {
		this.onbeforeactivate = onbeforeactivate;
	}
	public String getOnbeforecut() {
		return onbeforecut;
	}
	public void setOnbeforecut(String onbeforecut) {
		this.onbeforecut = onbeforecut;
	}
	public String getOnbeforedeactivate() {
		return onbeforedeactivate;
	}
	public void setOnbeforedeactivate(String onbeforedeactivate) {
		this.onbeforedeactivate = onbeforedeactivate;
	}
	public String getOnbeforeeditfocus() {
		return onbeforeeditfocus;
	}
	public void setOnbeforeeditfocus(String onbeforeeditfocus) {
		this.onbeforeeditfocus = onbeforeeditfocus;
	}
	public String getOnbeforepaste() {
		return onbeforepaste;
	}
	public void setOnbeforepaste(String onbeforepaste) {
		this.onbeforepaste = onbeforepaste;
	}
	public String getOnbeforeupdate() {
		return onbeforeupdate;
	}
	public void setOnbeforeupdate(String onbeforeupdate) {
		this.onbeforeupdate = onbeforeupdate;
	}
	public String getOnblur() {
		return onblur;
	}
	public void setOnblur(String onblur) {
		this.onblur = onblur;
	}
	public String getOnchange() {
		return onchange;
	}
	public void setOnchange(String onchange) {
		this.onchange = onchange;
	}
	public String getOnclick() {
		return onclick;
	}
	public void setOnclick(String onclick) {
		this.onclick = onclick;
	}
	public String getOncontextmenu() {
		return oncontextmenu;
	}
	public void setOncontextmenu(String oncontextmenu) {
		this.oncontextmenu = oncontextmenu;
	}
	public String getOncontrolselect() {
		return oncontrolselect;
	}
	public void setOncontrolselect(String oncontrolselect) {
		this.oncontrolselect = oncontrolselect;
	}
	public String getOncut() {
		return oncut;
	}
	public void setOncut(String oncut) {
		this.oncut = oncut;
	}
	public String getOndblclick() {
		return ondblclick;
	}
	public void setOndblclick(String ondblclick) {
		this.ondblclick = ondblclick;
	}
	public String getOndeactivate() {
		return ondeactivate;
	}
	public void setOndeactivate(String ondeactivate) {
		this.ondeactivate = ondeactivate;
	}
	public String getOndrag() {
		return ondrag;
	}
	public void setOndrag(String ondrag) {
		this.ondrag = ondrag;
	}
	public String getOndragend() {
		return ondragend;
	}
	public void setOndragend(String ondragend) {
		this.ondragend = ondragend;
	}
	public String getOndragenter() {
		return ondragenter;
	}
	public void setOndragenter(String ondragenter) {
		this.ondragenter = ondragenter;
	}
	public String getOndragleave() {
		return ondragleave;
	}
	public void setOndragleave(String ondragleave) {
		this.ondragleave = ondragleave;
	}
	public String getOndragover() {
		return ondragover;
	}
	public void setOndragover(String ondragover) {
		this.ondragover = ondragover;
	}
	public String getOndragstart() {
		return ondragstart;
	}
	public void setOndragstart(String ondragstart) {
		this.ondragstart = ondragstart;
	}
	public String getOndrop() {
		return ondrop;
	}
	public void setOndrop(String ondrop) {
		this.ondrop = ondrop;
	}
	public String getOnerrorupdate() {
		return onerrorupdate;
	}
	public void setOnerrorupdate(String onerrorupdate) {
		this.onerrorupdate = onerrorupdate;
	}
	public String getOnfilterchange() {
		return onfilterchange;
	}
	public void setOnfilterchange(String onfilterchange) {
		this.onfilterchange = onfilterchange;
	}
	public String getOnfocus() {
		return onfocus;
	}
	public void setOnfocus(String onfocus) {
		this.onfocus = onfocus;
	}
	public String getOnfocusin() {
		return onfocusin;
	}
	public void setOnfocusin(String onfocusin) {
		this.onfocusin = onfocusin;
	}
	public String getOnfocusout() {
		return onfocusout;
	}
	public void setOnfocusout(String onfocusout) {
		this.onfocusout = onfocusout;
	}
	public String getOnhelp() {
		return onhelp;
	}
	public void setOnhelp(String onhelp) {
		this.onhelp = onhelp;
	}
	public String getOnkeydown() {
		return onkeydown;
	}
	public void setOnkeydown(String onkeydown) {
		this.onkeydown = onkeydown;
	}
	public String getOnkeypress() {
		return onkeypress;
	}
	public void setOnkeypress(String onkeypress) {
		this.onkeypress = onkeypress;
	}
	public String getOnkeyup() {
		return onkeyup;
	}
	public void setOnkeyup(String onkeyup) {
		this.onkeyup = onkeyup;
	}
	public String getOnlosecapture() {
		return onlosecapture;
	}
	public void setOnlosecapture(String onlosecapture) {
		this.onlosecapture = onlosecapture;
	}
	public String getOnmousedown() {
		return onmousedown;
	}
	public void setOnmousedown(String onmousedown) {
		this.onmousedown = onmousedown;
	}
	public String getOnmouseenter() {
		return onmouseenter;
	}
	public void setOnmouseenter(String onmouseenter) {
		this.onmouseenter = onmouseenter;
	}
	public String getOnmouseleave() {
		return onmouseleave;
	}
	public void setOnmouseleave(String onmouseleave) {
		this.onmouseleave = onmouseleave;
	}
	public String getOnmousemove() {
		return onmousemove;
	}
	public void setOnmousemove(String onmousemove) {
		this.onmousemove = onmousemove;
	}
	public String getOnmouseout() {
		return onmouseout;
	}
	public void setOnmouseout(String onmouseout) {
		this.onmouseout = onmouseout;
	}
	public String getOnmouseover() {
		return onmouseover;
	}
	public void setOnmouseover(String onmouseover) {
		this.onmouseover = onmouseover;
	}
	public String getOnmouseup() {
		return onmouseup;
	}
	public void setOnmouseup(String onmouseup) {
		this.onmouseup = onmouseup;
	}
	public String getOnmousewheel() {
		return onmousewheel;
	}
	public void setOnmousewheel(String onmousewheel) {
		this.onmousewheel = onmousewheel;
	}
	public String getOnmove() {
		return onmove;
	}
	public void setOnmove(String onmove) {
		this.onmove = onmove;
	}
	public String getOnmoveend() {
		return onmoveend;
	}
	public void setOnmoveend(String onmoveend) {
		this.onmoveend = onmoveend;
	}
	public String getOnmovestart() {
		return onmovestart;
	}
	public void setOnmovestart(String onmovestart) {
		this.onmovestart = onmovestart;
	}
	public String getOnpaste() {
		return onpaste;
	}
	public void setOnpaste(String onpaste) {
		this.onpaste = onpaste;
	}
	public String getOnpropertychange() {
		return onpropertychange;
	}
	public void setOnpropertychange(String onpropertychange) {
		this.onpropertychange = onpropertychange;
	}
	public String getOnreadyTRAIN_STAGEchange() {
		return onreadyTRAIN_STAGEchange;
	}
	public void setOnreadyTRAIN_STAGEchange(String onreadyTRAIN_STAGEchange) {
		this.onreadyTRAIN_STAGEchange = onreadyTRAIN_STAGEchange;
	}
	public String getOnresize() {
		return onresize;
	}
	public void setOnresize(String onresize) {
		this.onresize = onresize;
	}
	public String getOnresizeend() {
		return onresizeend;
	}
	public void setOnresizeend(String onresizeend) {
		this.onresizeend = onresizeend;
	}
	public String getOnresizestart() {
		return onresizestart;
	}
	public void setOnresizestart(String onresizestart) {
		this.onresizestart = onresizestart;
	}
	public String getOnselect() {
		return onselect;
	}
	public void setOnselect(String onselect) {
		this.onselect = onselect;
	}
	public String getOnselectstart() {
		return onselectstart;
	}
	public void setOnselectstart(String onselectstart) {
		this.onselectstart = onselectstart;
	}
	public String getOntimeerror() {
		return ontimeerror;
	}
	public void setOntimeerror(String ontimeerror) {
		this.ontimeerror = ontimeerror;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public boolean isEncryptKey() {
		return encryptKey;
	}

	public void setEncryptKey(boolean isEncryptKey) {
		this.encryptKey = isEncryptKey;
	}

	public String getChecked() {
		return checked;
	}

	public void setChecked(String checked) {
		this.checked = checked;
	}
	public boolean isEncryptValue() {
		return encryptValue;
	}
	public void setEncryptValue(boolean isEncryptValue) {
		this.encryptValue = isEncryptValue;
	}

}