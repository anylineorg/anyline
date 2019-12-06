package org.anyline.config; 
 
import java.util.Map; 
 
import javax.servlet.ServletContext; 
 
import org.anyline.util.ConfigTable; 
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.context.ApplicationContext; 
import org.springframework.context.ApplicationListener; 
import org.springframework.context.event.ContextRefreshedEvent; 
import org.springframework.stereotype.Component; 
import org.springframework.web.context.WebApplicationContext; 
 
@Component("anyline.config.listener") 
public class ConfigListener implements ApplicationListener<ContextRefreshedEvent> { 
	private Logger log = LoggerFactory.getLogger(ConfigListener.class); 
 
	@Override 
	public void onApplicationEvent(ContextRefreshedEvent event) { 
		if (event.getApplicationContext().getParent() != null) { 
			return; 
		} 
		ApplicationContext app = event.getApplicationContext(); 
		if (app instanceof WebApplicationContext) { 
			WebApplicationContext webApplicationContext = (WebApplicationContext) app; 
			ServletContext servlet = webApplicationContext.getServletContext(); 
			Map<String, String> configs = ConfigTable.getConfigs(); 
			String key = ConfigTable.getString("SERVLET_ATTRIBUTE_KEY", "al"); 
			servlet.setAttribute(key, configs); 
			log.warn("[配置文件加载至servlet context][key:{}]",key); 
		} 
	} 
} 
