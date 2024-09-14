package org.anyline.log;

public interface LogFactory {
    Log get(Class<?> clazz);
    Log get(String name);
}
