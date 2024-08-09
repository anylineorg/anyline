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

import org.anyline.metadata.type.DataType;
import org.anyline.metadata.type.init.DefaultJavaType;

import java.util.Hashtable;
import java.util.Map;
public class JavaTypeAdapter {
    public static Map<Class, DataType> types = new Hashtable<>();
    public JavaTypeAdapter() {

    }
    public static DataType type(Class clazz) {
        if(null != clazz) {
            return types.get(clazz);
        }else{
            return null;
        }
    }
    public static void reg(Class clazz, DataType type) {
        types.put(clazz, type);
    }
    static {
        //支持的数据类型
        for(DefaultJavaType type:DefaultJavaType.values()) {
            Class clazz = type.supportClass();
            reg(clazz, type);
        }
    }
}
