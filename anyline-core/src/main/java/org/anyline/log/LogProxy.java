package org.anyline.log;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class LogProxy {
    private static final List<LogFactory> factors = new ArrayList<>();
    private static final Vector<Group> caches = new Vector<>();
    public static int size() {
        return factors.size();
    }
    public static void addFactory(LogFactory factory) {
        LogProxy.factors.add(factory);
        compensate();
    }
    private static void compensate() {
        Vector<Group> removes = new Vector<>();
        for(Group group:caches) {
            for(LogFactory factory:factors) {
                String name = group.getName();
                Class<?> clazz = group.getClazz();
                if(null != name) {
                    group.add(factory.get(name));
                }else if(null != clazz) {
                    group.add(factory.get(clazz));
                }
            }
            removes.add(group);
        }
        caches.removeAll(removes);
    }
    public static Log get(String name) {
        Group group = new Group();
        group.setName(name);
        for(LogFactory factory:factors) {
            group.add(factory.get(name));
        }
        if(factors.isEmpty()) {
            caches.add(group);
        }
        return group;
    }
    public static Log get(Class<?> clazz) {
        Group group = new Group();
        group.setClazz(clazz);
        for(LogFactory factory:factors) {
            group.add(factory.get(clazz));
        }
        if(factors.isEmpty()) {
            caches.add(group);
        }
        return group;
    }
}
