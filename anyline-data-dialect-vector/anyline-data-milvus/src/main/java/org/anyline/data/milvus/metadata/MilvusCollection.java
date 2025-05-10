package org.anyline.data.milvus.metadata;

import org.anyline.metadata.Column;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.TypeMetadata;

import java.util.LinkedHashMap;

public class MilvusCollection extends Table<MilvusCollection> {
    protected MilvusSchema schema = new MilvusSchema();

    public void schema(MilvusSchema schema){
        this.schema = schema;
    }
    public MilvusSchema schema(){
        return schema;
    }
    public Column primary() {
        return schema.primary();
    }

    public Table addColumn(Column column) {
        schema.addColumn(column);
        return this;
    }
    public Column addColumn(String name, String type, int precision, int scale) {
        return schema.addColumn(name, type, precision, scale);
    }
    public Column addColumn(String name, String type, int precision) {
        return schema.addColumn(name, type, precision);
    }
    public Column addColumn(String name, String type) {
        return schema.addColumn(name, type);
    }
    public Column addColumn(String name, String type, String comment) {
        return schema.addColumn(name, type, comment);
    }
    public Column addColumn(String name, TypeMetadata type) {
        return schema.addColumn(name, type);
    }
    public Column addColumn(String name, String type, boolean nullable, Object def) {
        return schema.addColumn(name, type, nullable, def);
    }
    public Column addColumn(String name, TypeMetadata type, boolean nullable, Object def) {
        return schema.addColumn(name, type, nullable, def);
    }
    public Column getColumn(String name) {
        return schema.getColumn(name);
    }

}
