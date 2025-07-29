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

package org.anyline.data.jdbc.adapter.init.writer;

import org.anyline.adapter.DataWriter;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.BeanUtil;

import java.lang.reflect.Method;

public enum PostgresGenusWriter {

    JSONWriter(new Object[]{"JSON", "JSONB"}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            Object result = value;
            //把value转换成 java默认类型 或 数据库驱动中提供的类型
            try{
                String json = BeanUtil.object2json(value);
                Class clazz = Class.forName("org.postgresql.util.PGobject");
                Object pgobject = clazz.newInstance();
                Method method = clazz.getMethod("setType", String.class);
                method.invoke(pgobject, "json");
                method = clazz.getMethod("setValue", String.class);
                method.invoke(pgobject, json);
                result = pgobject;
            }catch (Exception e){
                e.printStackTrace();
            }
            return result;
        }
    })

    ;
    public Object[] supports() {
        return supports;
    }
    public DataWriter writer() {
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    PostgresGenusWriter(Object[] supports, DataWriter writer) {
        this.supports = supports;
        this.writer = writer;
    }
}
