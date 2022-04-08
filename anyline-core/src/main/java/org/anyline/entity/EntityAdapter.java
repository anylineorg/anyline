package org.anyline.entity;

public interface EntityAdapter {
    public <T> T entity(Class<T> clazz, DataRow row);
    public DataRow parse(Object obj, String ... keys);
}
