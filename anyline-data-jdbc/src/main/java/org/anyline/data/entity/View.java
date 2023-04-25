package org.anyline.data.entity;

import org.anyline.data.listener.init.DefaultDDListener;

public class View extends Table implements org.anyline.entity.data.View{
    protected String keyword = "VIEW"            ;
    protected View update;
    protected String definition;

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }


    public View(){
        this.listener = new DefaultDDListener();
    }
    public View(String name){
        this(null, name);
    }
    public View(String schema, String table){
        this(null, schema, table);
    }
    public View(String catalog, String schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.listener = new DefaultDDListener();
    }

    public View clone(){
        View view = new View();
        view.catalog = catalog;
        view.schema = schema;
        view.name = name;
        view.comment = comment;
        view.type = type;
        view.typeCat = typeCat;
        view.typeSchema = typeSchema;
        view.typeName = typeName;
        view.selfReferencingColumn = selfReferencingColumn;
        view.refGeneration = refGeneration;
        view.engine = engine;
        view.charset = charset;
        view.collate = collate;
        view.ttl = ttl;
        view.checkSchemaTime = checkSchemaTime;
        view.primaryKey = primaryKey;
        view.columns = columns;
        view.tags = tags;
        view.indexs = indexs;
        view.constraints = constraints;
        view.listener = listener;
        view.autoDropColumn = autoDropColumn;
        view.update = update;
        view.definition = definition;
        return view;
    }
    public View update(){
        update = clone();
        update.setUpdate(null);
        return update;
    }

    public String toString(){
        return this.keyword+":"+name;
    }
}
