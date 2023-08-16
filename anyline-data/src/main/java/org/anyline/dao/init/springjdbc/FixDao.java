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


package org.anyline.dao.init.springjdbc;


import org.anyline.data.runtime.DataRuntime;

public class FixDao<E> extends DefaultDao<E>{

    private String datasource;

    public String getDatasource() {
        return datasource;
    }

    public void setDatasource(String datasource) {
        this.datasource = datasource;
    }

    /**
     * 是否固定数据源
     * @return boolean
     */
    public boolean isFix(){
        return true;
    }
    public DataRuntime runtime(){
        if(null != runtime){
            return runtime;
        }else{
            throw new RuntimeException("未设置运行环境");
        }/*
        runtime = RuntimeHolder.getRuntime(datasource);
        return runtime;*/
    }
}
