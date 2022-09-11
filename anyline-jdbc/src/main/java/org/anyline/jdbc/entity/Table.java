package org.anyline.jdbc.entity;

import org.anyline.exception.AnylineException;
import org.anyline.jdbc.config.db.SQLCreater;
import org.anyline.listener.DDListener;
import org.anyline.listener.impl.DefaulDDtListener;
import org.anyline.service.AnylineService;

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
    private DDListener listener             ;


    public Table(){
        this.listener = new DefaulDDtListener();
    }
    public Table(String name){
        this(null, name);
    }
    public Table(String schema, String table){
        this(null, schema, table);
    }
    public Table(String catalog, String schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
        this.listener = new DefaulDDtListener();
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


    public Column addColumn(Column column){
        return columns.put(column.getName(), column);
    }
    public Column addColumn(String name, String type){
        return addColumn(name, type, true, null);
    }
    public Column addColumn(String name, String type, boolean nullable, Object def){
        Column column = new Column();
        column.setName(name);
        column.setNullable(nullable);
        column.setDefaultValue(def);
        column.setTypeName(type);
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        columns.put(name, column);
        return column;
    }
    public Table setPrimaryKey(String ... keys){
        if(null != columns){
            for(String key:keys){
                Column column = columns.get(key);
                if(null != column){
                    column.setPrimaryKey(true);
                }else{
                    throw new AnylineException("未匹配到"+key+",请诜添加到columns");
                }
            }
        }else{
            throw new AnylineException("请先设置columns");
        }
        return this;
    }

    public String getCatalog() {
        return catalog;
    }

    public Table setCatalog(String catalog) {
        this.catalog = catalog;
        return this;
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

    public Table setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
        return this;
    }

    public LinkedHashMap<String, Index> getIndexs() {
        return indexs;
    }

    public Table setIndexs(LinkedHashMap<String, Index> indexs) {
        this.indexs = indexs;
        return this;
    }
    public Column getColumn(String name){
        return columns.get(name);
    }

    public String getEngine() {
        return engine;
    }

    public Table setEngine(String engine) {
        this.engine = engine;
        return this;
    }

    public String getCharset() {
        return charset;
    }

    public Table setCharset(String charset) {
        this.charset = charset;
        return this;
    }

    public String getCollate() {
        return collate;
    }

    public Table setCollate(String collate) {
        this.collate = collate;
        return this;
    }
    public DDListener getListener() {
        return listener;
    }

    public Table setListener(DDListener listener) {
        this.listener = listener;
        return this;
    }
    public Table setService(AnylineService service){
        if(null != listener){
            listener.setService(service);
        }
        return this;
    }
    public Table setCreater(SQLCreater creater){
        if(null != listener){
            listener.setCreater(creater);
        }
        return this;
    }
}
