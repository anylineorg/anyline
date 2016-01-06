/* 
 * Copyright 2006-2015 the original author or authors.
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
 */

package org.anyline.listener;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.anyline.service.AnylineService;
import org.anyline.util.FileUtil;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

@WebListener
public class AnylineListener implements ServletContextListener,HttpSessionListener{
	Logger log = Logger.getLogger(this.getClass());
	/**
	 * session创建
	 */
	public void sessionCreated(HttpSessionEvent event) {
		
	}
	/**
	 * session关闭
	 */
	public void sessionDestroyed(HttpSessionEvent event) {
	}
	/**
	 * 系统启动
	 */
	public void contextInitialized(ServletContextEvent event) {
//		ServletContext servletContext = event.getServletContext();
//		//缓存设置
//		ApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext); //获取spring上下文
//		AnylineService service = (AnylineService)springContext.getBean(AnylineService.class);
//		try{
//			String path =  AnylineListener.class.getProtectionDomain().getCodeSource().getLocation().getPath();
//			if(path.startsWith("/")){
//				path = path.substring(1);
//			}
//			path = path.substring(0,path.indexOf("org")+3);
//			List<File> files = FileUtil.getAllChildrenFile(new File(path),"CashUtil.class");
//			for(File file:files){
//				path = file.getAbsolutePath();
//				path = path.substring(path.indexOf("\\classes\\"));
//				path = path.replace("\\", ".");
//				path = path.replace(".classes.", "").replace(".class", "");
//				Class clazz = Class.forName(path);
//				Method methodSetService = clazz.getMethod("setService", AnylineService.class);
//				if(null != methodSetService){
//					//methodSetService.invoke(clazz, service);
//				}
//				Method methodInit = clazz.getMethod("initServletCache", ServletContext.class);
//				if(null != methodInit){
//					methodInit.invoke(clazz, servletContext);
//				}
//				Method methodRun = clazz.getMethod("run");
//				if(null != methodRun){
//					methodRun.invoke(clazz);
//				}
//			}
//		}catch(Exception e){
//		}
	}	
	/**
	 * 系统关闭
	 */
	public void contextDestroyed(ServletContextEvent event) {}
}
