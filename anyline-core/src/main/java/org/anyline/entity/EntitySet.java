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

package org.anyline.entity;

import org.anyline.metadata.Column;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.BeanUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class EntitySet<T> implements Collection<T>, Serializable {
    private static final long serialVersionUID = 6443551515441660102L;
    protected static final Log log = LogProxy.get(EntitySet.class);
    private LinkedHashMap<String, Column>  metadatas= null  ; // 数据类型相关(需要开启ConfigTable.IS_AUTO_CHECK_METADATA)
    private boolean result = true;              // 执行结果
    private String code = null;
    private Exception exception = null;         // 异常
    private String message = null;              // 提示信息
    private PageNavi navi = null;               // 分页
    private List<String> head = null;           // 表头
    private List<T> datas = new ArrayList<>();   // 数据
    private List<String> primaryKeys = null;    // 主键
    private String datalink = null;             // 数据连接
    private String datasource = null;           // 数据源(表|视图|XML定义SQL)
    private String schema = null;
    private String table = null;
    private long createTime = 0;                // 创建时间
    private long expires = -1;                  // 过期时间(毫秒) 从创建时刻计时expires毫秒后过期
    private boolean isFromCache = false;        // 是否来自缓存
    private boolean isAsc = false;
    private boolean isDesc = false;

    public EntitySet() {
        createTime = System.currentTimeMillis();
    }

    public EntitySet setMetadatas(LinkedHashMap metadatas) {
        this.metadatas = metadatas;
        return this;
    }
    public LinkedHashMap<String, Column> getMetadatas() {
        return metadatas;
    }
    public Column getMetadata(String column) {
        if(null == metadatas) {
            return null;
        }
        return metadatas.get(column.toUpperCase());
    }
    public String getMetadataTypeName(String column) {
        Column col = getMetadata(column);
        if(null != col) {
            return col.getTypeName();
        }
        return null;
    }
    public Integer getMetadataType(String column) {
        Column col = getMetadata(column);
        if(null != col) {
            return col.getType();
        }
        return null;
    }
    public String getMetadataFullType(String column) {
        Column col = getMetadata(column);
        if(null != col) {
            return col.getFullType();
        }
        return null;
    }
    public String getMetadataClassName(String column) {
        Column col = getMetadata(column);
        if(null != col) {
            return col.getClassName();
        }
        return null;
    }
    public EntitySet<T> gets(String key, Object value) {
        EntitySet<T> result = new EntitySet<>();
        for(T entity:datas) {
            Object v = BeanUtil.getFieldValue(entity, key, true);
            if(null != v && v.equals(value)) {
                result.add(entity);
            }
        }
        return result;
    }
    public EntitySet<T> gets(Field field, Object value) {
        EntitySet<T> result = new EntitySet<>();
        for(T entity:datas) {
            Object v = BeanUtil.getFieldValue(entity, field);
            if(null != v && v.equals(value)) {
                result.add(entity);
            }
        }
        return result;
    }
    public T get(int index) {
        return datas.get(index);
    }

    public DataSet set(String ... keys) {
        return EntityAdapterProxy.set(this, keys);
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public PageNavi getNavi() {
        return navi;
    }

    public void setNavi(PageNavi navi) {
        this.navi = navi;
    }

    public List<String> getHead() {
        return head;
    }

    public void setHead(List<String> head) {
        this.head = head;
    }

    public List<T> getDatas() {
        return datas;
    }

    public void setDatas(List<T> datas) {
        this.datas = datas;
    }

    public List<String> getPrimaryKeys() {
        return primaryKeys;
    }

    public void setPrimaryKeys(List<String> primaryKeys) {
        this.primaryKeys = primaryKeys;
    }

    public String getDatalink() {
        return datalink;
    }

    public void setDatalink(String datalink) {
        this.datalink = datalink;
    }

    public String getDataSource() {
        return datasource;
    }

    public void setDataSource(String datasource) {
        this.datasource = datasource;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getExpires() {
        return expires;
    }

    public void setExpires(long expires) {
        this.expires = expires;
    }

    public boolean isFromCache() {
        return isFromCache;
    }

    public void setFromCache(boolean fromCache) {
        isFromCache = fromCache;
    }

    public boolean isAsc() {
        return isAsc;
    }

    public void setAsc(boolean asc) {
        isAsc = asc;
    }

    public boolean isDesc() {
        return isDesc;
    }

    public void setDesc(boolean desc) {
        isDesc = desc;
    }

    @Override
    public int size() {
        return datas.size();
    }

    @Override
    public boolean isEmpty() {
        return datas.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return datas.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return datas.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        Collection.super.forEach(action);
    }

    @Override
    public Object[] toArray() {
        return new Object[0];
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        return null;
    }

    @Override
    public boolean add(T t) {
        return datas.add(t);
    }

    @Override
    public boolean remove(Object o) {
        return datas.remove(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return datas.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return datas.addAll(c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        return datas.removeAll(c);
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {
        return Collection.super.removeIf(filter);
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        return datas.retainAll(c);
    }

    @Override
    public void clear() {
        datas.clear();
    }

    @Override
    public Spliterator<T> spliterator() {
        return Collection.super.spliterator();
    }

    @Override
    public Stream<T> stream() {
        return Collection.super.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return Collection.super.parallelStream();
    }

}