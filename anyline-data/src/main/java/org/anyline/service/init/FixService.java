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


package org.anyline.service.init;

import org.anyline.service.AnylineService;

public class FixService extends DefaultService{
    private String datasource;
    public AnylineService datasource(String datasource){
        //不可切换
        //ClientHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService datasource(){
        //不可切换
        //ClientHolder.setDefaultDataSource();
        return this;
    }
    public AnylineService setDataSource(String datasource){
        //不可切换
        //ClientHolder.setDataSource(datasource);
        return this;
    }
    public AnylineService setDataSource(String datasource, boolean auto){
        //不可切换
        //ClientHolder.setDataSource(datasource, auto);
        return this;
    }
    public AnylineService setDefaultDataSource(){
        //不可切换
        //ClientHolder.setDefaultDataSource();
        return this;
    }
    // 恢复切换前数据源
    public AnylineService recoverDataSource(){
        //不可切换
        //ClientHolder.recoverDataSource();
        return this;
    }
    public String getDataSource(){
        return datasource;
    }

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }
}
