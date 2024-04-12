package org.anyline.bean;

import java.util.LinkedHashMap;

public interface BeanDefine {
    Class getType();
    String getTypeName();

    BeanDefine setType(Class type);

    boolean isPrimary();
    BeanDefine setPrimary(boolean lazy);
    boolean isLazy();
    BeanDefine setLazy(boolean lazy);
    LinkedHashMap<String, Object> getValues();
    BeanDefine setValues(LinkedHashMap<String, Object> values);
    BeanDefine addValue(String name, Object value);
    BeanDefine addReferenceValue(String name, String value);

}
