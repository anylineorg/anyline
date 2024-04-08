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
package org.anyline.metadata;

import org.anyline.util.BasicUtil;

import java.io.Serializable;

public class Catalog extends BaseMetadata<Catalog> implements Serializable {
    protected String keyword = "CATALOG"           ;
    public Catalog(){

    }
    public Catalog(String name){
        this.name = name;
    }
    public boolean isEmpty(){
        if(null == name || name.trim().isEmpty()){
            return true;
        }
        return false;
    }

    public String getKeyword() {
        return this.keyword;
    }
    public boolean equals(Catalog catalog){
        return equals(catalog, true);
    }
    public boolean equals(Catalog catalog, boolean ignoreCase){
        if(null == catalog){
            return false;
        }
        return BasicUtil.equals(this.name, catalog.getName(), ignoreCase);
    }
}
