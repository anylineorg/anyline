package org.anyline.log.init;

import org.anyline.log.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogFactory implements org.anyline.log.LogFactory{
    @Override
    public Log getLog(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new org.anyline.log.init.Log(logger);
    }

    @Override
    public Log getLog(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        return new org.anyline.log.init.Log(logger);
    }
}
