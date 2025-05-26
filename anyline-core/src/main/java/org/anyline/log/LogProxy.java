/*
 * Copyright 2006-2025 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    public static void append(LogFactory factory) {
        LogProxy.factors.add(factory);
        enrich();
    }
    private static void enrich() {
        for(Group group:caches) {
            for(LogFactory factory:factors) {
                if(factory.disabled()){
                    continue;
                }
                String name = group.getName();
                Class<?> clazz = group.getClazz();
                if(null != name) {
                    group.add(factory.get(name));
                }else if(null != clazz) {
                    group.add(factory.get(clazz));
                }
            }
        }
    }
    public static Log get(String name) {
        Group group = new Group();
        group.setName(name);
        for(LogFactory factory:factors) {
            if(factory.disabled()){
                continue;
            }
            group.add(factory.get(name));
        }
        caches.add(group);
        return group;
    }
    public static Log get(Class<?> clazz) {
        Group group = new Group();
        group.setClazz(clazz);
        for(LogFactory factory:factors) {
            if(factory.disabled()){
                continue;
            }
            group.add(factory.get(clazz));
        }
        caches.add(group);
        return group;
    }
}
