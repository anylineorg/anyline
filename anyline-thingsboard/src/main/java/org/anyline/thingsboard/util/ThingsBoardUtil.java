package org.anyline.thingsboard.util;

import com.sun.org.apache.xalan.internal.xsltc.compiler.Template;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Hashtable;
import java.util.Map;

public class ThingsBoardUtil {

    private static final Logger log = LoggerFactory.getLogger(ThingsBoardUtil.class);
    private ThingsBoardConfig config = null;
    private static Hashtable<String,ThingsBoardUtil> instances = new Hashtable<String,ThingsBoardUtil>();




    public static ThingsBoardUtil getInstance(){
        return getInstance("default");
    }
    public static ThingsBoardUtil getInstance(String key){
        if(BasicUtil.isEmpty(key)){
            key = "default";
        }
        ThingsBoardUtil util = instances.get(key);
        if(null == util){
            util = new ThingsBoardUtil();
            ThingsBoardConfig config = ThingsBoardConfig.getInstance(key);
            util.config = config;

            try {
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            instances.put(key, util);
        }
        return util;
    }


}
