package org.anyline.entity.data;

import org.anyline.util.BeanUtil;

import java.util.ArrayList;
import java.util.List;

public class Function {
    private String catalog;
    private String schema;
    private String name;
    private List<Parameter> parameters = new ArrayList<>();
    private String definition;
    protected Function update;
    protected boolean setmap = false              ;  //执行了upate()操作后set操作是否映射到update上(除了catalog,schema,name,drop,action)
    protected boolean getmap = false              ;  //执行了upate()操作后get操作是否映射到update上(除了catalog,schema,name,drop,action)



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
        update.update = null;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
