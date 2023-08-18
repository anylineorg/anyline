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

import org.anyline.util.BeanUtil;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Function  implements Serializable {
    private String catalog;
    private String schema;
    private String name;
    private List<Parameter> parameters = new ArrayList<>();
    private String definition;
    private List<String> ddls;
    protected Function update;
    protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了catalog, schema,name,drop,action)
    protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了catalog, schema,name,drop,action)



    public Function getUpdate() {
        return update;
    }

    public Function setNewName(String newName){
        return setNewName(newName, true, true);
    }

    public Function setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update){
            update(setmap, getmap);
        }
        update.setName(newName);
        return update;
    }

    public Function update(){
        return update(true, true);
    }

    public Function update(boolean setmap, boolean getmap){
        this.setmap = setmap;
        this.getmap = getmap;
        update = clone();
        update.update = null;
        return update;
    }

    public Function setUpdate(Function update, boolean setmap, boolean getmap) {
        this.update = update;
        this.setmap = setmap;
        this.getmap = getmap;
        if(null != update) {
            update.update = null;
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public List<String> getDdls() {
        return ddls;
    }

    public void setDdls(List<String> ddl) {
        this.ddls = ddl;
    }
    public List<String> ddls() {
        return ddls;
    }
    public List<String> ddls(boolean init) {
        return ddls;
    }
    public List<String> getDdls(boolean init) {
        return ddls;
    }

    public String ddl() {
        if(null != ddls && ddls.size()>0){
            return ddls.get(0);
        }
        return null;
    }
    public String ddl(boolean init) {
        if(null != ddls && ddls.size()>0){
            return ddls.get(0);
        }
        return null;
    }
    public String getDdl(boolean init) {
        if(null != ddls && ddls.size()>0){
            return ddls.get(0);
        }
        return null;
    }

    public List<Parameter> getParameters() {
        if(getmap && null != update){
            return update.parameters;
        }
        return parameters;
    }

    public Function setParameters(List<Parameter> parameters) {
        if(setmap && null != update){
            update.definition = definition;
            return this;
        }
        this.parameters = parameters;
        return this;
    }

    public String getDefinition() {
        if(getmap && null != update){
            return update.definition;
        }
        return definition;
    }

    public Function setDefinition(String definition) {
        if(setmap && null != update){
            update.definition = definition;
            return this;
        }
        this.definition = definition;
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public void setCatalog(String catalog) {
        this.catalog = catalog;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }


    public Function clone(){
        Function copy = new Function();
        BeanUtil.copyFieldValue(copy, this);

        List<Parameter> pms = new ArrayList<>();
        for(Parameter parameter:parameters){
            pms.add(parameter.clone());
        }
        copy.parameters = pms;

        copy.update = null;
        copy.setmap = false;
        copy.getmap = false;
        return copy;
    }
}
