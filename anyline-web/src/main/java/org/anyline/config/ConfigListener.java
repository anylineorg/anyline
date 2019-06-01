package org.anyline.config;

	import java.util.Map;

import javax.servlet.ServletContext;

import org.anyline.util.ConfigTable;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
	@Component("al.config.listener")
	public class ConfigListener implements ApplicationListener<ContextRefreshedEvent> {
		@Override
		public void onApplicationEvent(ContextRefreshedEvent event) {
			if(event.getApplicationContext().getParent() != null){
				return;
			}
//			ServletContext servlet = null;
//			Map<String,String> configs = ConfigTable.getConfigs();
//			servlet.setAttribute("al", configs);
			
		}
	}
