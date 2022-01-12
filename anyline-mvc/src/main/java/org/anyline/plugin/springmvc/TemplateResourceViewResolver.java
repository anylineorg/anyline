/* 
 * Copyright 2006-2022 www.anyline.org
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
package org.anyline.plugin.springmvc; 
 
import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

import java.util.Locale;
 
public class TemplateResourceViewResolver extends UrlBasedViewResolver { 
 
	private Boolean alwaysInclude; 
	private Boolean exposeContextBeansAsAttributes; 
	private String[] exposedContextBeanNames; 
 
	@SuppressWarnings("rawtypes")
	public TemplateResourceViewResolver() { 
		Class viewClass = requiredViewClass(); 
		if (viewClass.equals(InternalResourceView.class)) 
			viewClass = TemplateView.class; 
 
		setViewClass(viewClass); 
	} 
 
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Class requiredViewClass() { 
		return InternalResourceView.class; 
	} 
 
	public void setAlwaysInclude(boolean alwaysInclude) { 
		this.alwaysInclude = Boolean.valueOf(alwaysInclude); 
	} 
 
	public void setExposeContextBeansAsAttributes(boolean exposeContextBeansAsAttributes) { 
		this.exposeContextBeansAsAttributes = Boolean.valueOf(exposeContextBeansAsAttributes); 
	} 
 
	public void setExposedContextBeanNames(String ... exposedContextBeanNames) { 
		this.exposedContextBeanNames = exposedContextBeanNames; 
	} 
 
	protected View loadView(String viewName, Locale locale) throws Exception { 
		AbstractUrlBasedView view = buildView(viewName); 
		return ((View) getApplicationContext().getAutowireCapableBeanFactory().initializeBean(view, viewName)); 
	} 
	protected AbstractUrlBasedView buildView(String viewName) throws Exception { 
		TemplateView view = (TemplateView) BeanUtils.instantiateClass(getViewClass()); 
		if (!viewName.contains(getPrefix())  
				&& !viewName.startsWith("/")) { 
			viewName = getPrefix() + viewName; 
		} 
		if (!viewName.endsWith(getSuffix())) { 
			viewName = viewName + getSuffix(); 
		} 
		view.setUrl(viewName); 
		String contentType = getContentType(); 
		if (contentType != null) 
			view.setContentType(contentType); 
 
		view.setRequestContextAttribute(getRequestContextAttribute()); 
		view.setAttributesMap(getAttributesMap()); 
		if (this.alwaysInclude != null) 
			view.setAlwaysInclude(this.alwaysInclude.booleanValue()); 
 
		if (this.exposeContextBeansAsAttributes != null) 
			view.setExposeContextBeansAsAttributes(this.exposeContextBeansAsAttributes.booleanValue()); 
 
		if (this.exposedContextBeanNames != null) 
			view.setExposedContextBeanNames(this.exposedContextBeanNames); 
 
		view.setPreventDispatchLoop(true); 
		return view; 
	} 
 
} 
