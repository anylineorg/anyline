package org.anyline.entity.metadata;

public interface Convert {

    public Class getOrigin();
    public Class getTarget();
    public Object exe(Object value, Object def) throws ConvertException;
}
