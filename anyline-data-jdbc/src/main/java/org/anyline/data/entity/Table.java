package org.anyline.data.entity;

import org.anyline.data.adapter.JDBCAdapter;
import org.anyline.data.jdbc.ds.DataSourceHolder;
import org.anyline.data.listener.DDListener;
import org.anyline.data.listener.init.DefaultDDListener;
import org.anyline.data.util.ThreadConfig;
import org.anyline.exception.AnylineException;
import org.anyline.service.AnylineService;
import org.anyline.util.BasicUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;
import org.anyline.util.SpringContextUtil;

import java.util.*;

public class Table implements org.anyline.entity.data.Table{

    static {
        ClassUtil.regImplement(org.anyline.entity.data.Table.class, Table.class);
    }

    protected String keyword = "TABLE"            ;
    protected String catalog                      ;
    protected String schema                       ;
    protected String name                         ;
    protected String type                         ;
    protected String comment                      ;
    protected int srid                            ;

    protected String typeCat                      ;
    protected String typeSchema                   ;
    protected String typeName                     ;
    protected String selfReferencingColumn        ;
    protected String refGeneration                ;

    protected String engine                       ;
    protected String charset                      ;
    protected String collate                      ;
    protected Long ttl                            ;
    protected Date checkSchemaTime                ;


    protected PrimaryKey primaryKey;
    protected LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
    protected LinkedHashMap<String, Tag> tags       = new LinkedHashMap<>();
    protected LinkedHashMap<String, Index> indexs   = new LinkedHashMap<>();
    protected LinkedHashMap<String, Constraint> constraints = new LinkedHashMap<>();
    protected Table update;
    protected transient DDListener listener                 ;
    protected boolean autoDropColumn = ConfigTable.IS_DDL_AUTO_DROP_COLUMN;     //执行alter时是否删除 数据库中存在 但table 中不存在的列


    public Table(){
        this(null);
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

        DDListener listener = null;
        ConfigTable config = ThreadConfig.check(DataSourceHolder.curDataSource());
        if(config instanceof ThreadConfig){
            listener = ((ThreadConfig)config).getDdListener();
        }
        if(null == listener) {
            listener = SpringContextUtil.getBean(DDListener.class);
        }
        if(null == listener){
            listener = new DefaultDDListener();
        }
        this.listener = listener;
    }

    public List<Column> primarys(){
        List<Column> pks = new ArrayList<>();
        for(Column column:columns.values()){
            if(column.isPrimaryKey() == 1){
                pks.add(column);
            }
        }
        Collections.sort(pks, new Comparator<Column>() {
            @Override
            public int compare(Column o1, Column o2) {
                Integer p1 = o1.getPosition();
                Integer p2 = o2.getPosition();
                if(null == p1){
                    return -1;
                }
                if(null == p2){
                    return 1;
                }
                return p1 > p2 ? 1:-1;
            }
        });
        return pks;
    }
    public Column primary(){
        List<Column> pks = primarys();
        if(pks.isEmpty()){
            return null;
        }
        return pks.get(0);
    }
    public Table clone(){
        Table table = new Table();
        table.catalog = catalog;
        table.schema = schema;
        table.name = name;
        table.comment = comment;
        table.type = type;
        table.typeCat = typeCat;
        table.typeSchema = typeSchema;
        table.typeName = typeName;
        table.selfReferencingColumn = selfReferencingColumn;
        table.refGeneration = refGeneration;
        table.engine = engine;
        table.charset = charset;
        table.collate = collate;
        table.ttl = ttl;
        table.checkSchemaTime = checkSchemaTime;
        table.primaryKey = primaryKey;
        table.columns = columns;
        table.tags = tags;
        table.indexs = indexs;
        table.constraints = constraints;
        table.listener = listener;
        table.autoDropColumn = autoDropColumn;
        table.update = update;
        return table;
    }
    public Table update(){
        update = clone();
        update.setUpdate(null);
        return update;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }

    public Table getUpdate() {
        return update;
    }

    public Table setUpdate(Table update) {
        this.update = update;
        return this;
    }


    public Table addColumn(Column column){
        column.setTable(this);
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        columns.put(column.getName().toUpperCase(), column);
        return this;
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
        addColumn(column);
        return column;
    }
    public Table setPrimaryKey(String ... keys){
        if(null != columns){
            for(String key:keys){
                Column column = columns.get(key.toUpperCase());
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

    public Table setPrimaryKey(PrimaryKey primaryKey){
        this.primaryKey = primaryKey;
        if(null != primaryKey){
            primaryKey.setTable(this);
        }
        return this;
    }

    public Table addTag(Tag tag){
        tag.setTable(this);
        if(null == tags){
            tags = new LinkedHashMap<>();
        }
        tags.put(tag.getName(), tag);
        return this;
    }
    public Tag addTag(String name, String type){
        return addTag(name, type, true, null);
    }
    public Tag addTag(String name, String type, Object value){
        Tag tag = new Tag(name, type, value);
        addTag(tag);
        return tag;
    }
    public Tag addTag(String name, String type, boolean nullable, Object def){
        Tag tag = new Tag();
        tag.setName(name);
        tag.setNullable(nullable);
        tag.setDefaultValue(def);
        tag.setTypeName(type);
        addTag(tag);
        return tag;
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
    public String getName(boolean greedy){
        String result = "";
        if(greedy){
            if(BasicUtil.isNotEmpty(catalog)){
                result = catalog+".";
            }
            if(BasicUtil.isNotEmpty(schema)){
                result = result + schema + ".";
            }
            result = result + name;
        }else{
            result = name;
        }
        return result;
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
        if(null == columns){
            columns = new LinkedHashMap<>();
        }
        return columns;
    }

    public Table setColumns(LinkedHashMap<String, Column> columns) {
        this.columns = columns;
        if(null != columns) {
            for (Column column : columns.values()) {
                column.setTable(this);
            }
        }
        return this;
    }

    public LinkedHashMap<String, Tag> getTags() {
        if(null == tags){
            tags = new LinkedHashMap<>();
        }
        return tags;
    }

    public Table setTags(LinkedHashMap<String, Tag> tags) {
        this.tags = tags;
        if(null != tags) {
            for (Column tag : tags.values()) {
                tag.setTable(this);
            }
        }
        return this;
    }
    public LinkedHashMap<String, Index> getIndexs() {
        if(null == indexs){
            indexs = new LinkedHashMap<>();
        }
        return indexs;
    }
    public PrimaryKey getPrimaryKey(){
        if(null == primaryKey){
            for(Column column: columns.values()){
                if(column.isPrimaryKey() ==1){
                    if(null == primaryKey){
                        primaryKey = new PrimaryKey();
                        primaryKey.setName(getName()+"_PK");
                        primaryKey.setTable(this);
                    }
                    primaryKey.addColumn(column);
                }
            }
        }
        if(null == primaryKey){
            for(Index index: indexs.values()){
                if(index.isPrimary()){
                    primaryKey = new PrimaryKey();
                    primaryKey.setName(index.getName());
                    primaryKey.setTable(this);
                    primaryKey.setColumns(index.getColumns());
                }
            }
        }
        return primaryKey;
    }

    public Table setIndexs(LinkedHashMap<String, Index> indexs) {
        this.indexs = indexs;
        return this;
    }

    public LinkedHashMap<String, Constraint> getConstraints() {
        if(null == constraints){
            constraints = new LinkedHashMap<>();
        }
        return constraints;
    }

    public Table setConstraints(LinkedHashMap<String, Constraint> constraints) {
        this.constraints = constraints;
        return this;
    }

    public Column getColumn(String name){
        if(null == columns){
            return null;
        }
        return columns.get(name.toUpperCase());
    }
    public Column getTag(String name){
        return tags.get(name.toUpperCase());
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
    public AnylineService getService(){
        if(null != listener){
            return listener.getService();
        }
        return null;
    }
    public Table setService(AnylineService service){
        if(null != listener){
            listener.setService(service);
        }
        return this;
    }
    public Table setCreater(JDBCAdapter adapter){
        if(null != listener){
            listener.setAdapter(adapter);
        }
        return this;
    }

    public int getSrid() {
        return srid;
    }

    public void setSrid(int srid) {
        this.srid = srid;
    }

    public Date getCheckSchemaTime() {
        return checkSchemaTime;
    }

    public void setCheckSchemaTime(Date checkSchemaTime) {
        this.checkSchemaTime = checkSchemaTime;
    }

    public String getKeyword() {
        return keyword;
    }

    public String toString(){
        return this.keyword+":"+name;
    }

    public boolean isAutoDropColumn() {
        return autoDropColumn;
    }

    public void setAutoDropColumn(boolean autoDropColumn) {
        this.autoDropColumn = autoDropColumn;
    }
}
