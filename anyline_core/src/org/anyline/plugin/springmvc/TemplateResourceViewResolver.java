package org.anyline.plugin.springmvc;

import java.util.Locale;

import org.springframework.beans.BeanUtils;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.InternalResourceView;
import org.springframework.web.servlet.view.UrlBasedViewResolver;

public class TemplateResourceViewResolver extends UrlBasedViewResolver {

	private Boolean alwaysInclude;
	private Boolean exposeContextBeansAsAttributes;
	private String[] exposedContextBeanNames;

	public TemplateResourceViewResolver() {
		Class viewClass = requiredViewClass();
		if (viewClass.equals(InternalResourceView.class))
			viewClass = TemplateView.class;

		setViewClass(viewClass);
	}

	protected Class requiredViewClass() {
		return InternalResourceView.class;
	}

	public void setAlwaysInclude(boolean alwaysInclude) {
		this.alwaysInclude = Boolean.valueOf(alwaysInclude);
	}

	public void setExposeContextBeansAsAttributes(boolean exposeContextBeansAsAttributes) {
		this.exposeContextBeansAsAttributes = Boolean.valueOf(exposeContextBeansAsAttributes);
	}

	public void setExposedContextBeanNames(String[] exposedContextBeanNames) {
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
