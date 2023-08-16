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


package org.anyline.baidu.map.util;

import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.baidu.map.load.bean")
public class BaiduMapBean implements InitializingBean {

    @Value("${anyline.baidu.map.ak:}")
    private String AK		;
    @Value("${anyline.baidu.map.sk:}")
    private String SK 	;


    @Override
    public void afterPropertiesSet()  {
        AK = BasicUtil.evl(AK, BaiduMapConfig.DEFAULT_AK);
        if(BasicUtil.isEmpty(AK)){
            return;
        }
        BaiduMapConfig.register(AK, BasicUtil.evl(SK, BaiduMapConfig.DEFAULT_SK));
    }
    @Bean("anyline.baidu.map.init.client")
    public BaiduMapClient instance(){
        return BaiduMapClient.getInstance();
    }
}
