package org.anyline.data.entity;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

public class Tag extends Column{
    protected String keyword = "TAG"            ;
    protected Tag update = null;

    public Tag(){
    }
    public Tag(String name, String type, Object value){
        this.name = name;
        this.typeName = type;
        this.value = value;
    }
    public Tag(String name, Object value){
        this.name = name;
        this.value = value;
    }


    public Tag update(){
        update = (Tag) this.clone();
        return update;
    }

    public Tag getUpdate() {
        return update;
    }

    public Tag setUpdate(Tag update) {
        BeanUtil.copyFieldValueNvl(update, this);
        this.update = update;
        return this;
    }


    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ").append(typeName);
        if(null != precision && precision > 0){
            builder.append("(").append(precision);
            if(null != scale && scale > 0){
                builder.append(",").append(scale);
            }
            builder.append(")");
        }
        if(BasicUtil.isNotEmpty(value)){
            builder.append(" value: ").append(value);
        }
        return builder.toString();
    }
    public Tag clone(){
        Tag copy = new Tag();
        copy.setName(name);
        copy.setOriginalName(originalName);
        copy.setCatalog(catalog);
        copy.setClassName(className);
        copy.setSchema(schema);
        copy.setTableName(tableName);
        copy.setDisplaySize(displaySize);
        copy.setComment(comment);
        copy.setType(type);
        copy.setTypeName(typeName);
        copy.setPrecision(precision);
        copy.setScale(scale);
        copy.setNullable(nullable);
        copy.setCaseSensitive(caseSensitive);
        copy.setCurrency(isCurrency);
        copy.setSigned(isSigned);
        copy.setPrimaryKey(isPrimaryKey);
        copy.setGenerated(isGenerated);
        copy.setDefaultValue(defaultValue);
        copy.setPosition(position);
        copy.setOrder(order);
        copy.setBefore(before);
        copy.setAfter(after);
        copy.setCharset(charset);
        copy.setCollate(collate);
        return copy;
    }
    public String getKeyword() {
        return this.keyword;
    }
}
