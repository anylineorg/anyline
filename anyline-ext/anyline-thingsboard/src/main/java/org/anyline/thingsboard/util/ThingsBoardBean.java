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


package org.anyline.thingsboard.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.thingsboard.load.bean")
public class ThingsBoardBean implements InitializingBean {

    @Value("${anyline.thingsboard.account:}")
    public String ACCOUNT		  ;
    @Value("${anyline.thingsboard.password:}")
    public String PASSWORD        ;
    @Value("${anyline.thingsboard.host:}")
    public String HOST		      ;
    @Value("${anyline.thingsboard.tenant.id:}")
    public String TENANT	      ;

    @Override
    public void afterPropertiesSet()  {
        ACCOUNT = BasicUtil.evl(ACCOUNT, ThingsBoardConfig.DEFAULT_ACCOUNT);
        if(BasicUtil.isEmpty(ACCOUNT)){
            return;
        }
        DataRow row = new DataRow();
        row.put("ACCOUNT", BasicUtil.evl(ACCOUNT, ThingsBoardConfig.DEFAULT_ACCOUNT));
        row.put("PASSWORD", BasicUtil.evl(PASSWORD, ThingsBoardConfig.DEFAULT_PASSWORD));
        row.put("HOST", BasicUtil.evl(HOST, ThingsBoardConfig.DEFAULT_HOST));
        row.put("TENANT", BasicUtil.evl(TENANT, ThingsBoardConfig.DEFAULT_TENANT));
        ThingsBoardConfig.register(row);
    }
    @Bean("anyline.thingsboard.init.client")
    public ThingsBoardClient instance(){
        return ThingsBoardClient.getInstance();
    }

}
