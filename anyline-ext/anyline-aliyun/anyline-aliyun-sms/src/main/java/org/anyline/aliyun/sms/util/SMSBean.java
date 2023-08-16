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


package org.anyline.aliyun.sms.util;

import org.anyline.sms.util.SMSListener;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.sms.load.bean")
public class SMSBean implements InitializingBean {

    @Value("${anyline.aliyun.sms.accesskey:}")
    private String ACCESS_KEY;
    @Value("${anyline.aliyun.sms.accessSecret:}")
    private String ACCESS_SECRET;
    @Value("${anyline.aliyun.sms.sign:}")
    private String SIGN;


    public static SMSListener listener;

    @Autowired(required=false)
    public void setListener(SMSListener listener){
        SMSBean.listener = listener;
    }
    public static SMSListener getListener(){
        return listener;
    }

    @Override
    public void afterPropertiesSet()  {
        ACCESS_KEY = BasicUtil.evl(ACCESS_KEY, SMSConfig.DEFAULT_ACCESS_KEY);
        if(BasicUtil.isEmpty(ACCESS_KEY)) {
            return;
        }
        SMSConfig.register(
                BasicUtil.evl(ACCESS_KEY, SMSConfig.DEFAULT_ACCESS_KEY)
                , BasicUtil.evl(ACCESS_SECRET, SMSConfig.DEFAULT_ACCESS_SECRET)
        );
    }
    @Bean("anyline.aliyun.sms.init.util")
    public SMSUtil instance(){
        return SMSUtil.getInstance();
    }

}
