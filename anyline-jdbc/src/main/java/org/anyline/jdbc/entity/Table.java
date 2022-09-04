package org.anyline.jdbc.entity;

import java.util.ArrayList;
import java.util.List;

public class Table {
    /* TABLE_CAT String => 表类别（可为 null）
     * TABLE_SCHEMA String => 表模式（可为 null）
     * TABLE_NAME String => 表名称
     * TABLE_TYPE String => 表类型。"TABLE"、"VIEW"、"SYSTEM TABLE"、"GLOBAL TEMPORARY"、"LOCAL TEMPORARY"、"ALIAS" 和 "SYNONYM"。
     * REMARKS String => 表的解释性注释
     * TYPE_CAT String => 类型的类别（可为 null）
     * TYPE_SCHEMA String => 类型模式（可为 null）
     * TYPE_NAME String => 类型名称（可为 null）
     * SELF_REFERENCING_COL_NAME String => 有类型表的指定 "identifier" 列的名称（可为 null）
     * REF_GENERATION String  => 指定在 SELF_REFERENCING_COL_NAME 中创建值的方式。这些值为 "SYSTEM"、"USER" 和 "DERIVED"。（可能为 null）
     */
    private String cat;
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

    public Table addColumn(Column column){
        columns.add(column);
        return this;
    }
    public String getCat() {
        return cat;
    }

    public void setCat(String cat) {
        this.cat = cat;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getTypeCat() {
        return typeCat;
    }

    public void setTypeCat(String typeCat) {
        this.typeCat = typeCat;
    }

    public String getTypeSchema() {
        return typeSchema;
    }

    public void setTypeSchema(String typeSchema) {
        this.typeSchema = typeSchema;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getSelfReferencingColumn() {
        return selfReferencingColumn;
    }

    public void setSelfReferencingColumn(String selfReferencingColumn) {
        this.selfReferencingColumn = selfReferencingColumn;
    }

    public String getRefGeneration() {
        return refGeneration;
    }

    public void setRefGeneration(String refGeneration) {
        this.refGeneration = refGeneration;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
}
