package org.anyline.jdbc.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Table {
    private String catalog;
    private String schema;
    private String name;
    private String type;
    private String remarks;

    private String typeCat;
    private String typeSchema;
    private String typeName;
    private String selfReferencingColumn;
    private String refGeneration;
    private List<Column> columns = new ArrayList<>();

    private Table update;

    private List<Column> getPrimaryKeys(){
        List<Column> pks = new ArrayList<>();
        for(Column column:columns){
            if(column.isPrimaryKey()){
                pks.add(column);
            }
        }
        Collections.sort(pks, new Comparator<Column>() {
            @Override
            public int compare(Column o1, Column o2) {
                return o1.getPrimaryKeyIndex() > o2.getPrimaryKeyIndex() ? 1:-1;
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
        columns.add(column);
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

    public String getRemarks() {
        return remarks;
    }

    public Table setRemarks(String remarks) {
        this.remarks = remarks;
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

    public List<Column> getColumns() {
        return columns;
    }

    public Table setColumns(List<Column> columns) {
        this.columns = columns;
        return this;
    }
}
