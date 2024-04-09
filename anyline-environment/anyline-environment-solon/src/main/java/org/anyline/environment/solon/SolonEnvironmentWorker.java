package org.anyline.environment.solon;

import org.anyline.adapter.EnvironmentWorker;
import org.anyline.util.CharUtil;
import org.anyline.util.ClassUtil;
import org.anyline.util.ConfigTable;
import org.noear.solon.Solon;
import org.noear.solon.SolonProps;
import org.noear.solon.core.AppContext;
import org.noear.solon.core.Plugin;

import java.util.HashSet;
import java.util.Map;

public class SolonEnvironmentWorker implements Plugin, EnvironmentWorker {
    private AppContext context;
    private SolonProps props;
    @Override
    public void start(AppContext context) throws Throwable {
        this.context = context;
        ConfigTable.worker = this;
        this.props = Solon.cfg();
    }

    @Override
    public Object get(String key) {
        return context.getBean(key);
    }

    @Override
    public String string(String prefixes, String keys) {
        String value = null;
        if(null == keys){
            return value;
        }
        if(null == prefixes){
            prefixes = "";
        }
        String[] ps= prefixes.split(",");
        String[] kss = keys.split(",");
        for(String p:ps){
            for(String key:kss){
                key = p + key;
                value = props.get(key);
                if(null != value){
                    return value;
                }
                //以中划线分隔的配置文件
                String[] ks = key.split("-");
                String sKey = null;
                for(String k:ks){
                    if(null == sKey){
                        sKey = k;
                    }else{
                        sKey = sKey + CharUtil.toUpperCaseHeader(k);
                    }
                }
                value = props.get(sKey);
                if(null != value){
                    return value;
                }

                //以下划线分隔的配置文件
                ks = key.split("_");
                sKey = null;
                for(String k:ks){
                    if(null == sKey){
                        sKey = k;
                    }else{
                        sKey = sKey + CharUtil.toUpperCaseHeader(k);
                    }
                }
                value = props.get(sKey);
                if(null != value){
                    return value;
                }

                ks = key.toLowerCase().split("_");
                sKey = null;
                for(String k:ks){
                    if(null == sKey){
                        sKey = k;
                    }else{
                        sKey = sKey + CharUtil.toUpperCaseHeader(k);
                    }
                }
                value = props.get(sKey);
                if(null != value){
                    return value;
                }

                //中划线
                sKey = key.replace("_","-");
                value = props.get(sKey);
                if(null != value){
                    return value;
                }

                //小写中划线
                sKey = key.toLowerCase().replace("_","-");
                value = props.get(sKey);
                if(null != value){
                    return value;
                }
            }
        }
        return value;
    }

    @Override
    public boolean destroyBean(String bean) {
        return false;
    }

    @Override
    public Object getBean(String name) {
        return context.getBean(name);
    }

    @Override
    public <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    @Override
    public <T> T getBean(String name, Class<T> clazz) {
        Object bean = context.getBean(name);
        if(null != bean && ClassUtil.isInSub(bean.getClass(), clazz)){
            return (T) bean;
        }
        return null;
    }

    @Override
    public <T> Map<String, T> getBeans(Class<T> clazz) {
        return context.getBeansMapOfType(clazz);
    }

    @Override
    public boolean regBean(String name, Object bean) {
        return false;
    }

    @Override
    public boolean containsSingleton(String name) {
        return false;
    }

    @Override
    public Object getSingleton(String name) {
        return context.getBean(name);
    }

    @Override
    public Map<String, Object> inject(String id, String prefix, Map params, Map<String, HashSet<String>> alias, Class clazz) throws Exception {
        return null;
    }

    @Override
    public Map<String, Object> inject(String id, Map params, Class clazz) throws Exception {
        return null;
    }
}
