package org.anyline.metadata.type;

public interface Convert {

    public Class getOrigin();
    public Class getTarget();
    public Object exe(Object value, Object def) throws ConvertException;
}
