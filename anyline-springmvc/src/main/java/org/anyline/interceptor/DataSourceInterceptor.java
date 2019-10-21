package org.anyline.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.anyline.config.db.ds.DataSourceHolder;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


public class DataSourceInterceptor  extends HandlerInterceptorAdapter{
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		DataSourceHolder.setDefaultDataSource();
		return true;
	}  
      
}  