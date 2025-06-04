package org.anyline.runtime;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public interface ExpressionActuator {
    default String namespace() {
        return "aov";
    }
    default List<String> namespaces() {
        List<String> namespaces = new ArrayList<>();
        namespaces.add(namespace());
        namespaces.add("alv");
        return namespaces;
    }

    /**
     * 支持的标签名
     * @return tag name
     */
    String tag();
    default List<String> tags(){
        List<String> tags = new ArrayList<>();
        tags.add(tag());
        return tags;
    }
    /**
     * 解析value
     * @param context context
     * @param namespace namespace
     * @param tag 标签名 有可能是多重如 timestamp:10
     * @param attributes 标签属性 aov:date format="yyyy-MM-dd"
     * @param body 标题体 ${create_time}
     * @return value
     */
    Object run(Context context, String namespace, String tag, LinkedHashMap<String, String> attributes, String body);
}
