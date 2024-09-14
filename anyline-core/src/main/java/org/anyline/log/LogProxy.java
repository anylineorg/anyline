package org.anyline.log;

import java.util.ArrayList;
import java.util.List;

public class LogProxy {
    private static final List<LogFactory> factors = new ArrayList<>();
    public static void addFactory(LogFactory factory){
        LogProxy.factors.add(factory);
    }
    public static Log get(String name) {
        Group group = new Group();
        for(LogFactory factory:factors){
            group.add(factory.get(name));
        }
        return group;
    }
    public static Log get(Class<?> clazz){
        Group group = new Group();
        for(LogFactory factory:factors){
            group.add(factory.get(clazz));
        }
        return group;
    }
}
