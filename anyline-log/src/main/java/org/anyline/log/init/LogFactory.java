package org.anyline.log.init;

import org.anyline.annotation.Component;
import org.anyline.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class LogFactory implements org.anyline.log.LogFactory{
    @Override
    public Log get(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new DefaultLog(logger);
    }

    @Override
    public Log get(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        return new DefaultLog(logger);
    }
}
