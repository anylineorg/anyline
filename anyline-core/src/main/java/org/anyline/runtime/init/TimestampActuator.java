package org.anyline.runtime.init;

import org.anyline.annotation.AnylineComponent;
import org.anyline.runtime.Context;
import org.anyline.runtime.ExpressionActuator;
import org.anyline.util.BasicUtil;

import java.util.LinkedHashMap;
@AnylineComponent
public class TimestampActuator implements ExpressionActuator {

    @Override
    public String tag() {
        return "timestamp";
    }

    /**
     * 当前时间戳
     * @param context context
     * @param namespace namespace
     * @param tag 标签名 有可能是多重如 timestamp:10
     * @param attributes 标签属性 len="10"
     * @param body 标题体 空
     * @return value
     */
    @Override
    public Object run(Context context, String namespace, String tag, LinkedHashMap<String, String> attributes, String body) {
        long timestamp = System.currentTimeMillis();
        if(tag.contains((":"))){
            String[] tags = tag.split(":");
            int length = 13;
            if(tags.length >= 2){
                length = BasicUtil.parseInt(tags[1], 13);
            }
            if(attributes.containsKey("length")){
                String len = attributes.get("length");
                length = BasicUtil.parseInt(len, 13);
            }
            if(attributes.containsKey("len")){
                String len = attributes.get("len");
                length = BasicUtil.parseInt(len, 13);
            }
            for(int i=0; i<13-length; i++){
                timestamp = timestamp/10;
            }
        }
        return timestamp;
    }
}
