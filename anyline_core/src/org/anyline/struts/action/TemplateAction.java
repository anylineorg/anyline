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
package org.anyline.struts.action;

import org.anyline.util.HttpUtil;
import org.anyline.util.WebUtil;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Actions;
import org.apache.struts2.convention.annotation.Namespace;
import org.apache.struts2.convention.annotation.ParentPackage;
import org.apache.struts2.convention.annotation.Result;
import org.springframework.context.annotation.Scope;
@ParentPackage("anyline-default")
@Namespace("/al/tmp")
@Scope("prototype")
public class TemplateAction extends AnylineAction {
	@Actions({
		@Action(value = "l", results = { @Result(type="json")})
	})
	public String load(){
		String path = getParam("path", false, true);
		String html = "";
		try{
			html = WebUtil.parseJsp(request, response, path);
		}catch(Exception e){
			
		}
		html = HttpUtil.escape(html);
		return success(html);
	}
}
