package org.anyline.jdbc.entity;

import org.anyline.util.BeanUtil;

public class Tag extends Column{
    protected Tag update = null;
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

    public Object clone(){
        Tag copy = new Tag();
        copy.setName(name);
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
}
