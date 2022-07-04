package org.anyline.compatible;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public interface Compatible {
    public String table(Class clazz);
    public List<String> columns(Class clazz);
    public String column(Class clazz, Field field);
    public Field field(Class clazz, String column);
    public String primary(Class clazz);
    public List<String> primarys(Class clazz);
    public <T> T entity(Class<T> clazz, Map<String,Object> map);
}
