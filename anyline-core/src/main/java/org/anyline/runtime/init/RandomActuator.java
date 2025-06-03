package org.anyline.runtime.init;

import org.anyline.annotation.AnylineComponent;
import org.anyline.runtime.Context;
import org.anyline.runtime.ExpressionActuator;
import org.anyline.util.BasicUtil;

import java.util.LinkedHashMap;

@AnylineComponent
public class RandomActuator implements ExpressionActuator {

    @Override
    public String tag() {
        return "timestamp";
    }

    /**
     * 随机字符
     * @param context context
     * @param namespace namespace
     * @param tag 标签名 有可能是多重如 random:8(默认8位)
     * @param attributes 标签属性 len="10"
     * @param body 标题体 空
     * @return value
     */
    @Override
    public Object run(Context context, String namespace, String tag, LinkedHashMap<String, String> attributes, String body) {
        int length = 8;
        if(tag.contains((":"))){
            String[] tags = tag.split(":");
            if(tags.length >= 2){
                length = BasicUtil.parseInt(tags[1], 13);
            }
            if(attributes.containsKey("length")){
                String len = attributes.get("length");
                length = BasicUtil.parseInt(len, 13);
            }else if(attributes.containsKey("len")){
                String len = attributes.get("len");
                length = BasicUtil.parseInt(len, 13);
            }
        }
        return BasicUtil.getRandomString(length);
    }
}
