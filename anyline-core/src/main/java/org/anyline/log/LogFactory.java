package org.anyline.log;

public interface LogFactory {
    Log getLog(Class<?> clazz);
    Log getLog(String name);
}
