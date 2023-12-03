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


package org.anyline.mimio.util;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component("anyline.minio.load.bean")
public class MinioBean implements InitializingBean {

    @Value("${anyline.minio.key:}")
    public String ACCESS_KEY		;
    @Value("${anyline.minio.secret:}")
    public String ACCESS_SECRET 	;
    @Value("${anyline.minio.endpoint:}")
    public String ENDPOINT			;
    @Value("${anyline.minio.bucket:}")
    public String BUCKET			;
    @Value("${anyline.minio.part:1}")
    public int PART_SIZE			;
    @Value("${anyline.minio.dir:}")
    public String DIR				;
    @Value("${anyline.minio.expire:3600}")
    public int EXPIRE_SECOND 		;

    @Override
    public void afterPropertiesSet()  {
        ACCESS_KEY = BasicUtil.evl(ACCESS_KEY, MinioConfig.DEFAULT_ACCESS_KEY);
        if(BasicUtil.isEmpty(ACCESS_KEY)){
            return;
        }
        DataRow row = new DataRow();
        row.put("ACCESS_KEY",BasicUtil.evl(ACCESS_KEY, MinioConfig.DEFAULT_ACCESS_KEY));
        row.put("ACCESS_SECRET",BasicUtil.evl(ACCESS_SECRET, MinioConfig.DEFAULT_ACCESS_SECRET));
        row.put("ENDPOINT",BasicUtil.evl(ENDPOINT, MinioConfig.DEFAULT_ENDPOINT));
        row.put("BUCKET",BasicUtil.evl(BUCKET, MinioConfig.DEFAULT_BUCKET));
        row.put("PART_SIZE",BasicUtil.evl(PART_SIZE, MinioConfig.DEFAULT_PART_SIZE));
        row.put("DIR",BasicUtil.evl(DIR, MinioConfig.DEFAULT_DIR));
        row.put("EXPIRE_SECOND",BasicUtil.evl(EXPIRE_SECOND, MinioConfig.DEFAULT_EXPIRE_SECOND));
        MinioConfig.register(row);
    }

    @Bean("anyline.minio.init.util")
    public MinioUtil instance(){
        return MinioUtil.getInstance();
    }
}
