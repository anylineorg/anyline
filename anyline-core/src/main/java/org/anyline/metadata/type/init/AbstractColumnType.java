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


package org.anyline.metadata.type.init;

import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.ColumnType;
import org.anyline.util.BasicUtil;

import java.lang.reflect.Field;

public class AbstractColumnType implements ColumnType {
    private boolean array;
    private DatabaseType[] dbs;
    private String name;
    private Class transfer                  ; //中间转换类型 转换成其他格式前先转换成transfer类型
    private Class compatible                ; //从数据库中读写数据的类型
    private Boolean ignorePrecision;
    private Boolean ignoreScale;
    public AbstractColumnType(String name, DatabaseType db, Class transfer, Class compatible, Boolean ignorePrecision, Boolean ignoreScale){
        this.name = name;
        this.dbs = new DatabaseType[]{db};
        this.transfer = transfer;
        this.compatible = compatible;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    public AbstractColumnType(String name, DatabaseType db, Class compatible, Boolean ignorePrecision, Boolean ignoreScale){
        this.name = name;
        this.dbs = new DatabaseType[]{db};
        this.compatible = compatible;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    @Override
    public Object convert(Object value, Object def){
        return convert(value, null, def);
    }

    @Override
    public Object convert(Object value, Class target){
        Object def = null;
        return convert(value, target, def);
    }

    @Override
    public Object convert(Object value, Class target, boolean array){
        Object def = null;
        return convert(value, target, array, def);
    }

    @Override
    public Object convert(Object value, Class target, boolean array, Object def) {
        if(null == target){
            target = compatible;
        }
        if(null != value){
            if(value.getClass() == target){
                return value;
            }
            if(null != transfer) {
                value = ConvertAdapter.convert(value, transfer, array, def);
            }
            value = ConvertAdapter.convert(value, target, array, def);
        }
        return value;
    }

    @Override
    public Object convert(Object value, Object obj, Field field) {
        return convert(value, field.getType());
    }

    @Override
    public Object read(Object value, Object def, Class clazz) {
        return read(value, def, clazz, false);
    }
    @Override
    public Object read(Object value, Object def, Class clazz, boolean array) {
        if(null == clazz){
            clazz = transfer;
        }
        if(null == clazz){
            clazz = compatible;
        }
        value = ConvertAdapter.convert(value, clazz, array, def);
        return value;
    }

    @Override
    public Object write(Object value, Object def, boolean placeholder) {
        return write(value, def, false, placeholder);
    }
    @Override
    public Object write(Object value, Object def, boolean array, boolean placeholder) {
        if(null != value){
            if(value.getClass() != compatible){
                if(null != transfer) {
                    value = ConvertAdapter.convert(value, transfer, array, def);
                }
                value = ConvertAdapter.convert(value, compatible, array, def);
            }
        }
        if(null != value){
            if(!BasicUtil.isNumber(value)){
                value = "'" + value + "'";
            }
        }
        return value;
    }


    @Override
    public boolean isArray() {
        return array;
    }

    @Override
    public void setArray(boolean array) {
        this.array = array;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean ignorePrecision() {
        return ignorePrecision;
    }

    @Override
    public boolean ignoreScale() {
        return ignoreScale;
    }

    @Override
    public boolean support() {
        return true;
    }

    @Override
    public Class compatible() {
        return compatible;
    }

    @Override
    public Class transfer() {
        return transfer;
    }

    @Override
    public DatabaseType[] dbs() {
        return dbs;
    }
}
