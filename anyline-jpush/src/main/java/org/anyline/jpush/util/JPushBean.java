package org.anyline.jpush.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.jpush.load.bean")
public class JPushBean implements InitializingBean {

	@Value("${anyline.jpush.key:}")
	private String APP_KEY 			;
	@Value("${anyline.jpush.secret:}")
	private String MASTER_SECRET 	;


	@Override
	public void afterPropertiesSet() throws Exception {
		APP_KEY = BasicUtil.evl(APP_KEY, JPushConfig.DEFAULT_APP_KEY);
		if(BasicUtil.isEmpty(APP_KEY)){
			return;
		}
		JPushConfig.register(APP_KEY, BasicUtil.evl(MASTER_SECRET, JPushConfig.DEFAULT_MASTER_SECRET));
	}

	@Bean("anyline.jpush.init.util")
	public JPushUtil instance(){
		return JPushUtil.getInstance();
	}
}
