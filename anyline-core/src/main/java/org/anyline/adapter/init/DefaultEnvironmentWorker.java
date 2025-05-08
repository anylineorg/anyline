/*
 * Copyright 2006-2023 www.anyline.org
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

package org.anyline.adapter.init;

import org.anyline.adapter.EnvironmentWorker;
import org.anyline.annotation.AnylineAutowired;
import org.anyline.annotation.AnylineComponent;
import org.anyline.bean.BeanDefine;
import org.anyline.listener.LoadListener;
import org.anyline.bean.ValueReference;
import org.anyline.bean.init.DefaultBeanDefine;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.proxy.ConvertProxy;
import org.anyline.util.*;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class DefaultEnvironmentWorker implements EnvironmentWorker {
    protected static Log log = LogProxy.get(DefaultEnvironmentWorker.class);
    private static final Map<String, Object> factory = new HashMap<>();

    public static EnvironmentWorker start(File config) {
        if(null == ConfigTable.environment) {
            ConfigTable.setEnvironment(new DefaultEnvironmentWorker());
        }

        if(null != config) {
            ConfigTable.parseEnvironment(FileUtil.read(config, StandardCharsets.UTF_8).toString(), config.getName());
        }

        try {
            loadBean();
        }catch (Exception e) {
            e.printStackTrace();
        }

        Map<String, LoadListener> listeners = ConfigTable.environment().getBeans(LoadListener.class);

        for(LoadListener listener:listeners.values()) {
            listener.start();
        }
        for(LoadListener listener:listeners.values()) {
            listener.finish();
        }
        for(LoadListener listener:listeners.values()) {
            listener.after();
        }
        return ConfigTable.environment();
    }
    public static EnvironmentWorker start() {
        return start(null);
    }
    public static void loadBean() throws Exception {
        //加载当前jar中的配置文件
        //file:/D:/jA.jar!/org/anyline/util/
        try {
            String path = null;
            URL uri = ConfigTable.class.getResource("");
            if(null != uri) {
                path = uri.getPath();
                path = ConfigTable.path(path);
                loadBean(new JarFile(path));
            }
            //1. 加载包含org.anyline包的所有目录
            //file:/D:/sso/target/classes/org/anyline/
            //jar:file:/D:/A.jar!/org/anyline/
            //D:\A.jar!/BOOT-INF/lib/B.jar
            Enumeration<URL> urls = ConfigTable.class.getClassLoader().getResources("org/anyline/");
            while (urls.hasMoreElements()) {
                URL url = urls.nextElement();
                String protocol = url.getProtocol().toLowerCase();
                if ("file".equals(protocol)) {
                    loadBean(new File(url.getFile()));
                } else if ("jar".equals(protocol)) {
                    JarFile jar = ((JarURLConnection) url.openConnection()).getJarFile();
                    log.debug("[load bean form jar][path:{}]", jar.getName());
                    loadBean(jar);
                }
            }
        }catch (Exception e) {
            log.warn("配置文件加载异常", e);
        }
        //2.项目中的配置文件
        String type = ConfigTable.getProjectProtocol();
        if ("jar".equals(type)) {
            // 遍历jar
            JarFile jFile = new JarFile(System.getProperty("java.class.path"));
            loadBean(jFile);
        }else{
            loadBean(new File(ConfigTable.getClassPath(), "bean.imports"));
        }
        //3.加载jar文件同目录的config
        File file = new File(FileUtil.merge(ConfigTable.getClassPath(), "config/bean.imports"));
        if(file.exists()) {
            loadBean(FileUtil.read(file, "UTF-8").toString());
        }
    }

    public static void loadBean(File file) {
        if(null != file && file.exists()) {
            if(file.isDirectory()) {
                List<File> files = FileUtil.getAllChildrenFile(file, "bean.imports", ".jar");
                for(File item:files) {
                    loadBean(item);
                }
            }else{
                String name = file.getName().toLowerCase();
                if(name.endsWith(".jar")) {
                    try {
                        loadBean(new JarFile(file));
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    loadBean(FileUtil.read(file, "UTF-8").toString());
                }
            }
        }
    }
    public static void loadBean(JarFile jar) {
        Enumeration<JarEntry> jarEntrys = jar.entries();
        while (jarEntrys.hasMoreElements()) {
            JarEntry entry = jarEntrys.nextElement();
            String name = entry.getName();
            if (name.contains("bean.imports")) {
                try {
                    InputStream in = jar.getInputStream(entry);
                    String txt = FileUtil.read(in, StandardCharsets.UTF_8).toString();
                    loadBean(txt);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }else if(name.contains("jar")) {
                //二级jar
            }
        }

        //读取所有带注解的类
        List<String> classes = ZipUtil.classes(jar, "org.anyline", false);
        ClassLoader loader = ConfigTable.class.getClassLoader();
        Class component = null;
        for(String clazz:classes) {
            try {
                //log.warn("load class:{}", clazz);
                if(clazz.contains(".web.") || clazz.contains(".net.")) {
                    continue;
                }
                Class<?> c = loader.loadClass(clazz);
                if(null == component) {
                    component = loader.loadClass(AnylineComponent.class.getName());
                }
                Annotation annotation = c.getAnnotation(component);
                if(null != annotation) {
                    Method methods[] = annotation.annotationType().getMethods();
                    String beanName = null;
                    for (Method method : methods) {
                        String name = method.getName();
                        if (name.equalsIgnoreCase("value") || name.equalsIgnoreCase("name")) {
                            Object value = method.invoke(annotation);
                            if (null != value) {
                                beanName = value.toString();
                            }
                        }
                    }
                    if(null == beanName) {
                        beanName = c.getName();
                    }
                    Object bean = c.newInstance();
                    autowired(bean);
                    ConfigTable.environment().regBean(beanName, bean);
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 属性(方法)赋值
     * @param object object
     * @throws Exception Exception
     */
    public static void autowired(Object object) throws Exception {
        Class clazz = object.getClass();
        //属性赋值
        List<Field> fields = ClassUtil.getFields(clazz, false, false);
        for(Field field:fields) {
            Annotation annotation = field.getAnnotation(AnylineAutowired.class);
            if(null != annotation) {
                Object val = BeanUtil.getFieldValue(object, field);
                if(null != val) {
                    continue;
                }
                Method methods[] = annotation.annotationType().getMethods();
                String beanName = null; //需要注入给属性的的bean name
                for(Method method:methods) {
                    String name = method.getName();
                    if(name.equalsIgnoreCase("value") || name.equalsIgnoreCase("name")) {
                        Object value = method.invoke(annotation);
                        if(null != value) {
                            beanName = value.toString();
                        }
                    }
                }
                if(BasicUtil.isNotEmpty(beanName)) {
                    val = ConfigTable.environment().getBean(beanName);
                }else{
                    Class beanClass = field.getType();
                    val = ConfigTable.environment().getBean(beanClass);
                }
                BeanUtil.setFieldValue(object, field, val);
            }
        }
        //方法赋值
        List<Method> methods = ClassUtil.getMethods(clazz, true);
        for(Method method:methods) {
            Annotation annotation = method.getAnnotation(AnylineAutowired.class);
            if(null != annotation) {
                Type[] types = method.getGenericParameterTypes();
                for(Type type:types) {
                    //如果泛型参数是参数化类型
                    if (type instanceof ParameterizedType) {
                        //强转参数化类型
                        ParameterizedType pt = (ParameterizedType)type;
                        Type[] arguments = pt.getActualTypeArguments();//获得真实参数化信息
                        Class pclass = Class.forName(pt.getRawType().getTypeName());
                        if(ClassUtil.isInSub(pclass, Map.class)) {
                            //map参数
                            Type kt = arguments[0];
                            Type vt = arguments[1];
                            Class kc = Class.forName(kt.getTypeName());
                            Class vc = Class.forName(vt.getTypeName());
                            Map<String, Object> beans = ConfigTable.environment().getBeans(vc);
                            method.invoke(object, beans);
                        }else if(ClassUtil.isInSub(pclass, Collection.class)) {
                            //集合参数
                            Type ct = arguments[0];
                            Class cc = Class.forName(ct.getTypeName());
                            Map<String, Object> beans = ConfigTable.environment().getBeans(cc);
                            List<Object> values = new ArrayList<>();
                            for(Object bean:beans.values()) {
                                values.add(bean);
                            }
                            method.invoke(object, values);
                        }

                    }
                }
            }
        }
    }
    public static void loadBean(String config) {
        String[] lines = config.split("\n");
        for(String line:lines) {
            String[] kv = line.split("=");
            if(kv.length == 2) {
                try {
                    ConfigTable.environment().regBean(kv[0], new DefaultBeanDefine(kv[1], false));
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public Object getBean(String name) {
        Object bean = factory.get(name);
        if(bean instanceof BeanDefine) {
            bean = instance(name, (BeanDefine) bean);
        }
        return bean;
    }
    public <T> Map<String, T> getBeans(Class<T> clazz) {
        Map<String, T> map = new HashMap<>();
        for(String key:factory.keySet()) {
            Object bean = factory.get(key);
            if(null != bean) {
                if(ClassUtil.isInSub(bean.getClass(), clazz)) {
                    map.put(key, (T) bean);
                }else if(bean instanceof BeanDefine) {
                    BeanDefine define = (BeanDefine) bean;
                    if(ClassUtil.isInSub(bean.getClass(), define.getType())) {
                        map.put(key, (T) instance(key, define));
                    }
                }
            }
        }
        return map;
    }
    public Object instance(BeanDefine define) {
        Object bean = null;
        Class type = define.getType();
        try {
            bean = type.newInstance();
            LinkedHashMap<String, Object> values = define.getValues();
            for (String key : values.keySet()) {
                Object value = values.get(key);
                if (value instanceof ValueReference) {
                    value = getBean(((ValueReference) value).getName());
                    if(value instanceof BeanDefine) {
                        value = instance(key, (BeanDefine) value);
                    }
                }
                BeanUtil.setFieldValue(bean, key, value, true,false);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
        return bean;
    }
    public Object instance(String name, BeanDefine define) {
        Object bean = ConfigTable.environment().instance(define);
        if(null != name) {
            factory.put(name, bean);
        }
        return bean;
    }

    public boolean regBean(String name, Object bean) {
        return reg(name, bean);
    }
    public boolean regBean(String name, BeanDefine bean) {
        return reg(name, bean);
    }

    public void regAlias(String name, String alias) {
        Object bean = getBean(name);
        regBean(alias, bean);
    }
    public boolean reg(String name, Object bean) {
        Object type = bean;
        if(bean instanceof BeanDefine) {
            BeanDefine define = (BeanDefine)bean;
            if(name.endsWith("default")) {
                define.setPrimary(true);
            }
            if(define.isLazy()) {
                factory.put(name, define);
                type = define.getTypeName();
            }else {
                Object insance = instance(name, define);
                factory.put(name, insance);
                type = insance;
            }
        }else if(bean instanceof Class) {
            Class clazz = (Class) bean;
            try {
                factory.put(name, clazz.newInstance());
            }catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            factory.put(name, bean);
        }
        log.debug("[reg bean][name:{}][instance:{}]", name, type);
        return true;
    }
    public boolean destroyBean(String bean) {
        return destroy(bean);
    }
    public static boolean destroy(String bean) {
        factory.remove(bean);
        return true;
    }
    public <T> T getBean(Class<T> clazz) {
        Map<String, T> beans = getBeans(clazz);
        T bean = null;
        if(null != beans && !beans.isEmpty()) {
            bean = beans.values().iterator().next();
        }
        if(null != bean) {
            try {
                if(bean instanceof BeanDefine) {
                    bean = (T)instance(null, (BeanDefine) bean);
                }
                autowired(bean);
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        return bean;
    }
    public <T> T getBean(String name, Class<T> clazz) {
        Object bean = factory.get(name);
        if(bean instanceof BeanDefine) {
            bean = instance(name, (BeanDefine) bean);
        }
        if(null != bean && ClassUtil.isInSub(bean.getClass(), clazz)) {
            try{
                autowired(bean);
            }catch (Exception e) {
                e.printStackTrace();
            }
            return (T) bean;
        }
        return null;
    }

    public boolean containsBean(String name) {
        return factory.containsKey(name);
    }
    public Object getSingletonBean(String name) {
        return factory.get(name);
    }

    public boolean containsSingleton(String name) {
        return containsBean(name);
    }
    public <T> T getSingletonBean(String name, Class<T> clazz) {
        return getBean(name, clazz);
    }

    @Override
    public Object get(String key) {
        return null; //ConfigTable.get(key);递归
    }
    public String getString(String key) {
        return ConfigTable.getString(key);
    }

    /**
     * 根据配置文件提取指定key的值
     * @param prefixes 前缀 多个以,分隔
     * @param keys key 多个以,分隔 第一个有值的key生效
     * @return String
     */
    public String string(String prefixes, String keys) {
        String value = null;
        if(null == keys) {
            return value;
        }
        if(null == prefixes) {
            prefixes = "";
        }
        String[] ps= prefixes.split(",");
        String[] kss = keys.split(",");
        for(String p:ps) {
            for(String key:kss) {
                key = p + key;
                value = getString(key);
                if(null != value) {
                    return value;
                }
                //以中划线分隔的配置文件
                String[] ks = key.split("-");
                String sKey = null;
                for(String k:ks) {
                    if(null == sKey) {
                        sKey = k;
                    }else{
                        sKey = sKey + CharUtil.toUpperCaseHeader(k);
                    }
                }
                value = getString(sKey);
                if(null != value) {
                    return value;
                }

                //以下划线分隔的配置文件
                ks = key.split("_");
                sKey = null;
                for(String k:ks) {
                    if(null == sKey) {
                        sKey = k;
                    }else{
                        sKey = sKey + CharUtil.toUpperCaseHeader(k);
                    }
                }
                value = getString(sKey);
                if(null != value) {
                    return value;
                }

                ks = key.toLowerCase().split("_");
                sKey = null;
                for(String k:ks) {
                    if(null == sKey) {
                        sKey = k;
                    }else{
                        sKey = sKey + CharUtil.toUpperCaseHeader(k);
                    }
                }
                value = getString(sKey);
                if(null != value) {
                    return value;
                }

                //中划线
                sKey = key.replace("_","-");
                value = getString(sKey);
                if(null != value) {
                    return value;
                }

                //小写中划线
                sKey = key.toLowerCase().replace("_","-");
                value = getString(sKey);
                if(null != value) {
                    return value;
                }
            }
        }
        return value;
    }

    /**
     * 根据params与配置文件创建数据源, 同时注入到spring上下文
     * @param id bean id
     * @param prefix 配置文件前缀 如 anyline.datasource.sso
     * @param params map格式参数
     * @return bean.id
     * @throws Exception Exception
     */
    public Map<String, Object> inject(String id, String prefix, Map params, Map<String, HashSet<String>> alias, Class clazz) throws Exception {
        Map<String, Object> cache = new HashMap<>();
        BeanDefine define = new DefaultBeanDefine(clazz);
        //List<Field> fields = ClassUtil.getFields(poolClass, false, false);
        List<Method> methods = ClassUtil.getMethods(clazz, true);
        for(Method method:methods) {
            if (method.getParameterCount() == 1 && Modifier.isPublic(method.getModifiers())) {
                String name = method.getName();
                // public void setMaximumPoolSize(int maxPoolSize) {this.maxPoolSize = maxPoolSize;}
                if(name.startsWith("set")) {
                    //根据方法名
                    name = name.substring(3, 4).toLowerCase() + name.substring(4);
                    Class paramType = method.getParameters()[0].getType();
                    Object value = BeanUtil.value(params, name, alias, paramType, null);
                    if(null == value) {
                        value = value(prefix, name, alias, paramType, null);
                    }
                    if(null != value) {
                        cache.put(name, value);
                        define.addValue(name, value);

                    }
                }
            }
        }
        for(Object key:params.keySet()) {
            define.addValue(key.toString(), params.get(key));
        }
         regBean(id, define);
        log.info("[inject bean][type:{}][bean:{}]", clazz, id);

        return cache;
    }

    /**
     * 根据params与配置文件创建数据源, 同时注入到spring上下文
     * @param id bean id
     * @param params map格式参数
     * @return bean.id
     * @throws Exception Exception
     */
    public Map<String, Object> inject(String id, Map params, Class clazz) throws Exception {
        Map<String, Object> cache = new HashMap<>();
        BeanDefine define = new DefaultBeanDefine(clazz);
        List<Method> methods = ClassUtil.getMethods(clazz, true);
        for(Method method:methods) {
            if (method.getParameterCount() == 1 && Modifier.isPublic(method.getModifiers())) {
                String name = method.getName();
                if(name.startsWith("set")) {
                    //根据方法名
                    name = name.substring(3, 4).toLowerCase() + name.substring(4);
                    Object value = BeanUtil.value(params, name, null, Object.class, null);
                    if(null != value) {
                        Class tp = method.getParameters()[0].getType();
                        value = ConvertProxy.convert(value, tp, false);
                        if (null != value) {
                            cache.put(name, value);
                            define.addValue(name, value);
                        }
                    }
                }
            }
        }
        for(Object key:params.keySet()) {
            define.addValue(key.toString(), params.get(key));
        }
        regBean(id, define);
        log.info("[inject bean][type:{}][bean:{}]", clazz, id);

        return cache;
    }
}
