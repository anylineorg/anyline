/*
 * Copyright 2006-2023 www.anyline.org
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
	public void afterPropertiesSet()  {
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
