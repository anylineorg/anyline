package org.anyline.jdbc.entity;

import java.util.*;

public class Table {
    private String catalog                      ;
    private String schema                       ;
    private String name                         ;
    private String type                         ;
    private String comment                      ;

    private String typeCat                      ;
    private String typeSchema                   ;
    private String typeName                     ;
    private String selfReferencingColumn        ;
    private String refGeneration                ;

    private String engine                       ;
    private String charset                      ;
    private String collate                      ;

    private LinkedHashMap<String,Column> columns;
    private LinkedHashMap<String,Index> indexs  ;
    private Table update;


    public Table(){
    }
    public Table(String name){
        this.name = name;
    }
    public Table(String catalog, String schema){
        this.catalog = catalog;
        this.schema = schema;
    }
    public Table(String catalog, String schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }

    public List<Column> getPrimaryKeys(){
        List<Column> pks = new ArrayList<>();
        for(Column column:columns.values()){
            if(column.isPrimaryKey()){
                pks.add(column);
            }
        }
        Collections.sort(pks, new Comparator<Column>() {
            @Override
            public int compare(Column o1, Column o2) {
                return o1.getPosition() > o2.getPosition() ? 1:-1;
            }
        });
        return pks;
    }
    public Table update(){
        update = new Table();
        return update;
    }

    public Table getUpdate() {
        return update;
    }

    public Table setUpdate(Table update) {
        this.update = update;
        return this;
    }


    public Table addColumn(Column column){
        columns.put(column.getName(), column);
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

    public Table setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public String getName() {
        return name;
    }

    public Table setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public Table setType(String type) {
        this.type = type;
        return this;
    }

    public String getComment() {
        return comment;
    }

    public Table setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public String getTypeCat() {
        return typeCat;
    }

    public Table setTypeCat(String typeCat) {
        this.typeCat = typeCat;
        return this;
    }

    public String getTypeSchema() {
        return typeSchema;
    }

    public Table setTypeSchema(String typeSchema) {
        this.typeSchema = typeSchema;
        return this;
    }

    public String getTypeName() {
        return typeName;
    }

    public Table setTypeName(String typeName) {
        this.typeName = typeName;
        return this;
    }

    public String getSelfReferencingColumn() {
        return selfReferencingColumn;
    }

    public Table setSelfReferencingColumn(String selfReferencingColumn) {
        this.selfReferencingColumn = selfReferencingColumn;
        return this;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public Table setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
        return this;
    }

    public LinkedHashMap<String, Column> getColumns() {
        return columns;
    }

    public void setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
    }

    public LinkedHashMap<String, Index> getIndexs() {
        return indexs;
    }

    public void setIndexs(LinkedHashMap<String, Index> indexs) {
        this.indexs = indexs;
    }
    public Column getColumn(String name){
        return columns.get(name);
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }
}
