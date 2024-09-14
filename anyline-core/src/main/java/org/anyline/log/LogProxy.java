package org.anyline.log;

public class LogProxy {
    private static LogFactory factory;
    public static void setFactory(LogFactory factory){
        LogProxy.factory = factory;
    }
    public static Log get(String name){
        if(null != factory){
            return factory.getLog(name);
        }
        return null;
    }
    public static Log get(Class<?> clazz){
        if(null != factory){
            return factory.getLog(clazz);
        }
        return null;
    }
}
