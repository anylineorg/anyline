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
