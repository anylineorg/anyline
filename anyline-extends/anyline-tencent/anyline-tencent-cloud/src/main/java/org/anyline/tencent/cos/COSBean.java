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


package org.anyline.tencent.cos;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("anyline.tencent.cos.load.bean")
public class COSBean implements InitializingBean {

    @Value("${anyline.tencent.cos.accessId:}")
    public String ACCESS_ID					; // 
    @Value("${anyline.tencent.cos.accessSecret :}")
    public String ACCESS_SECRET 			; // 
    @Value("${anyline.tencent.cos.endpoint:}")
    public String ENDPOINT					; // 
    @Value("${anyline.tencent.cos.bucket:}")
    public String BUCKET					; // 
    @Value("${anyline.tencent.cos.dir:}")
    public String DIR						; // 
    @Value("${anyline.tencent.cos.expire:3600}")
    public int EXPIRE_SECOND 				; // 

    @Override
    public void afterPropertiesSet()  {
        ACCESS_ID = BasicUtil.evl(ACCESS_ID, COSConfig.DEFAULT_ACCESS_ID);
        if(BasicUtil.isEmpty(ACCESS_ID)){
            return;
        }
        DataRow row = new DataRow();
        row.put("ACCESS_ID", BasicUtil.evl(ACCESS_ID, COSConfig.DEFAULT_ACCESS_ID));
        row.put("ACCESS_SECRET", BasicUtil.evl(ACCESS_SECRET, COSConfig.DEFAULT_ACCESS_SECRET));
        row.put("ENDPOINT", BasicUtil.evl(ENDPOINT, COSConfig.DEFAULT_ENDPOINT));
        row.put("BUCKET", BasicUtil.evl(BUCKET, COSConfig.DEFAULT_BUCKET));
        row.put("DIR", BasicUtil.evl(DIR, COSConfig.DEFAULT_DIR));
        row.put("EXPIRE_SECOND", BasicUtil.evl(EXPIRE_SECOND, COSConfig.DEFAULT_EXPIRE_SECOND));
        COSConfig.register(row);
    }

    /*@Bean("anyline.tencent.cos.init.bean")
    public COSUtil instance(){
        return COSUtil.getInstance();
    }*/
}
