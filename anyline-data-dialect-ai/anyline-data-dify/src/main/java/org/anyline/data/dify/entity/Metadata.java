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

package org.anyline.data.dify.entity;

import org.anyline.entity.DataRow;
import org.anyline.entity.OriginRow;
import org.anyline.metadata.Column;
import org.anyline.util.BeanUtil;

import java.io.Serializable;

public class Metadata extends Column implements Serializable {
    private static final long serialVersionUID = 1L;
    private String type;

    public Metadata(){}

    /**
     * 知识库添加元数据定义时用到
     * @param name 名称
     * @param type 类型 string number datetime
     */
    public Metadata(String name, String type){
        this.name = name;
        this.type = type;
    }

    /**
     * 文档添加元数据值时用到
     * @param id 与知识库添加元数据定义时返回的id保持一致
     * @param name 名称
     * @param value 值
     */
    public Metadata(String id, String name, Object value){
        this.id = id;
        this.name = name;
        this.value = value;
    }


    public DataRow map(){
        return map(true);
    }
    public DataRow map(boolean empty) {
        DataRow row = new OriginRow();
        row.put("id", id);
        row.put("name", name);
        row.put("value", value);
        row.put("type", type);
        return row;
    }
    public String json(){
        return BeanUtil.map2string(map());
    }
    public String json(boolean empty){
        return BeanUtil.map2string(map(empty));
    }

    public String getType() {
        return type;
    }

    public Column setType(String type) {
        this.type = type;
        return this;
    }
}
