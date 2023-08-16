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


package org.anyline.amap.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.ampa.load.bean")
public class AmapBean implements InitializingBean {

    @Value("${anyline.amap.host:}")
    private String HOST		;
    @Value("${anyline.amap.key:}")
    private String KEY		;
    @Value("${anyline.amap.secret:}")
    private String SECRET 	;
    @Value("${anyline.amap.table:}")
    private String TABLE 	;

    @Override
    public void afterPropertiesSet()  {
        KEY = BasicUtil.evl(KEY, AmapConfig.DEFAULT_KEY);
        if(BasicUtil.isEmpty(KEY)){
            return;
        }
        AmapConfig config = AmapConfig.register(KEY, BasicUtil.evl(SECRET, AmapConfig.DEFAULT_SECRET)
                , BasicUtil.evl(TABLE, AmapConfig.DEFAULT_TABLE));
        if(BasicUtil.isNotEmpty(this.HOST)) {
            config.HOST = this.HOST;
        }
    }
    @Bean("anyline.amap.init.client")
    public AmapClient instance(){
        return AmapClient.getInstance();
    }
}
