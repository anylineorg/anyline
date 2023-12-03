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


package org.anyline.baidu.seo.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.baidu.seo.load.bean")
public class BaiduSeoBean implements InitializingBean {

    @Value("${anyline.baidu.seo.site:}")
    private String SITE		; //站点URL如 http://www.anyline.org
    @Value("${anyline.baidu.seo.token:}")
    private String TOKEN 	;


    @Override
    public void afterPropertiesSet()  {
        SITE = BasicUtil.evl(SITE, BaiduSeoConfig.DEFAULT_SITE);
        if(BasicUtil.isEmpty(SITE)){
            return;
        }
        BaiduSeoConfig.register(SITE, BasicUtil.evl(TOKEN, BaiduSeoConfig.DEFAULT_TOKEN));
    }
    @Bean("anyline.baidu.seo.init.client")
    public BaiduSeoClient instance(){
        return BaiduSeoClient.getInstance();
    }
}
