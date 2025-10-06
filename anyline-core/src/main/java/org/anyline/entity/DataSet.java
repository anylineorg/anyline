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

package org.anyline.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import ognl.Ognl;
import ognl.OgnlContext;
import org.anyline.adapter.KeyAdapter.KEY_CASE;
import org.anyline.entity.geometry.Point;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.*;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import javax.xml.crypto.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

public class DataSet<E extends DataRow> implements Collection<E>, Serializable, AnyData<DataSet> {
    private static final long serialVersionUID = 6443551515441660101L;
    protected static final Log log = LogProxy.get(DataSet.class);
    private LinkedHashMap<String, Column>  metadatas= null  ; // 数据类型相关(需要开启ConfigTable.IS_AUTO_CHECK_METADATA)
    protected LinkedHashMap<String, Object> origin  = new LinkedHashMap<>() ; // 从数据库中查询的未处理的原始数据
    private Boolean override                        = null  ; //如果数据库中存在相同数据(根据主键判断)是否覆盖 true或false会检测数据库null不检测
    private Boolean overrideSync                    = null  ;
    protected Boolean unicode                       = null  ; //插入数据库时是否Unicode编码
    private boolean result                          = true  ; // 执行结果
    private String code                             = null  ; // code
    private Exception exception                     = null  ; // 异常
    private String message                          = null  ; // 提示信息
    private PageNavi navi                           = null  ; // 分页
    private List<String> head                       = null  ; // 表头
    private List<E> rows                            = null  ; // 数据
    private List<String> primaryKeys                = null  ; // 主键
    private String datalink                         = null  ; // 数据连接
    private String dataSource                       = null  ; // 数据源(表|视图|XML定义SQL)
    private Catalog catalog                         = null  ;
    private Schema schema                           = null  ; //
    private LinkedHashMap<String, Table> tables     = new LinkedHashMap<>()  ;
    private long createTime                         = 0     ; // 创建时间
    private long expires                            = -1    ; // 过期时间(毫秒) 从创建时刻计时expires毫秒后过期
    private boolean isFromCache                     = false ; // 是否来自缓存
    private Map<String, Object> tags                = new HashMap<>()       ; // 标签
    protected DataRow attributes                    = null                  ; // 属性
    protected boolean autoCheckElValue              = true                  ; // 检测el value
    protected KEY_CASE keyCase 				        = KEY_CASE.CONFIG       ; // 列名格式
 
    /**
     * 创建索引
     *
     * @param key key
     * @return DataSet
     * crateIndex("ID");
     * crateIndex("ID:ASC");
     */
    public DataSet<E> creatIndex(String key) {
        return this;
    }

    public DataSet() {
        rows = new ArrayList<>();
        createTime = System.currentTimeMillis();
    }
    public DataSet(KEY_CASE kc) {
        this.keyCase = kc;
        rows = new ArrayList<>();
        createTime = System.currentTimeMillis();
    }

    public DataSet(List<Map<String, Object>> list) {
        rows = new ArrayList<>();
        if (null == list) {
            return;
        }
        for (Map<String, Object> map : list) {
            E row = (E)new DataRow(map);
            rows.add(row);
        }
    }
    public DataSet(KEY_CASE keyCase, List<Map<String, Object>> list) {
        rows = new ArrayList<>();
        if (null == list) {
            return;
        }
        for (Map<String, Object> map : list) {
            E row = (E)new DataRow(keyCase, map);
            rows.add(row);
        }
    }
    public static DataSet<DataRow> build(Collection<?> list, String ... fields) {
        return parse(list, fields);
    }

    /**
     * list解析成DataSet
     * @param list list
     * @param fields 如果list是二维数据
     *               fields 下标对应的属性(字段/key)名称 如"ID","CODE","NAME"
     *               如果不输入则以下标作为DataRow的key 如row.put("0","100").put("1","A01").put("2","张三");
     *               如果属性数量超出list长度, 取null值存入DataRow
     *
     *               如果list是一组数组
     *               fileds对应条目的属性值 如果不输入 则以条目的属性作DataRow的key 如"USER_ID:id","USER_NM:name"
     *
     * @return DataSet
     */
    public static DataSet<DataRow> parse(Collection<?> list, String ... fields) {
        DataSet<DataRow> set = new DataSet();
        if (null != list) {
            for (Object obj : list) {
                DataRow row = null;
                if(obj instanceof DataRow) {
                    row = (DataRow)obj;
                }else if(obj instanceof Collection) {
                    row = DataRow.parseList(KEY_CASE.SRC, (Collection)obj, fields);
                }else {
                    row = DataRow.parse(obj, fields);
                }
                set.add(row);
            }
        }
        return set;
    }
    public static DataSet<DataRow> parse(KEY_CASE keyCase, Collection<?> list, String ... fields) {
        DataSet<DataRow> set = new DataSet();
        if (null != list) {
            for (Object obj : list) {
                DataRow row = null;
                if(obj instanceof DataRow) {
                    row = (DataRow)obj;
                }else if(obj instanceof Collection) {
                    row = DataRow.parseList(keyCase, (Collection)obj, fields);
                }else {
                    row = DataRow.parse(keyCase, obj, fields);
                }
                set.add(row);
            }
        }
        return set;
    }

    public static DataSet<DataRow> parseJson(KEY_CASE keyCase, String json) {
        if (null != json) {
            try {
                return parseJson(keyCase, BeanUtil.JSON_MAPPER.readTree(json));
            } catch (Exception e) {
                log.error("parse json exception:", e);
            }
        }
        return null;
    }

    public static DataSet<DataRow> parseJson(String json) {
        return parseJson(KEY_CASE.CONFIG, json);
    }

    public static DataSet<DataRow> parseJson(KEY_CASE keyCase, JsonNode json) {
        DataSet<DataRow> set = new DataSet();
        if (null != json) {
            if (json.isArray()) {
                Iterator<JsonNode> items = json.iterator();
                while (items.hasNext()) {
                    JsonNode item = items.next();
                    set.add(DataRow.parseJson(keyCase, item));
                }
            }
        }
        return set;
    }

    public static DataSet<DataRow> parseJson(JsonNode json) {
        return parseJson(KEY_CASE.CONFIG, json);
    }

    public Boolean getOverride() {
        return override;
    }
    public Boolean getOverrideSync() {
        return overrideSync;
    }


    public Boolean getUnicode() {
        return unicode;
    }

    public void setUnicode(Boolean unicode) {
        this.unicode = unicode;
    }
    public void setOverride(Boolean override) {
        this.override = override;
        for(DataRow row:rows) {
            row.setOverride(override);
        }
    }
    public void setOverride(Boolean override, Boolean sync) {
        this.override = override;
        this.overrideSync = sync;
        for(DataRow row:rows) {
            row.setOverride(override, sync);
        }
    }
    public DataSet<E> attr(String key, Object value) {
        return setAttribute(key, value);
    }

    public DataSet<E> setAttribute(String key, Object value) {
        if(null == attributes) {
            attributes = new DataRow();
        }
        attributes.put(key, value);
        return this;
    }

    public Object attr(String key) {
        return getAttribute(key);
    }

    public Object getAttribute(String key) {
        if(null == attributes) {
            attributes = new DataRow();
        }
        return attributes.get(key);
    }
    public DataRow getAttributes() {
        if(null == attributes) {
            attributes = new DataRow();
        }
        return attributes;
    }
    public DataSet<E> setAttributes(DataRow attributes) {
        this.attributes = attributes;
        return this;
    }

    public LinkedHashMap<String, Object> getOrigin() {
        return this.origin;
    }
    public Object getOrigin(String key) {
        if(null != origin) {
            return origin.get(key);
        }
        return null;
    }
    public DataSet<E> putOrigin(String key, Object value) {
        if(null == origin) {
            origin = new LinkedHashMap<>();
        }
        origin.put(key, value);
        return this;
    }
    public DataSet<E> setMetadata(LinkedHashMap<String, Column> metadatas) {
        this.metadatas = metadatas;
        return this;
    }

    public DataSet<E> setMetadata(String name, Column column) {
        if(null == metadatas) {
            metadatas = new LinkedHashMap<>();
        }
        metadatas.put(name.toUpperCase(), column);
        return this;
    }
    public DataSet<E> setMetadata(Column column) {
        if(null != column) {
            return setMetadata(column.getName(), column);
        }
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

    /**
     * 下划线转大驼峰
     * @return DataSet
     */
    public DataSet<E> Camel() {
        for(DataRow row:rows) {
            row.Camel();
        }
        return this;
    }

    /**
     * 驼峰转下划线
     * @return DataSet
     */
    public DataSet<E> camel_() {
        for(DataRow row:rows) {
            row.camel_();
        }
        return this;
    }

    /**
     * 下划线转小驼峰
     * @return DataSet
     */
    public DataSet<E> camel() {
        return camel(false);
    }

    /**
     * 下划线转小驼峰
     * @param lower 是否先转换成小写 遇到全大写但没有下划线的情况 false:不处理 true:全部转成小写
     * @return DataSet
     */
    public DataSet<E> camel(boolean lower) {
        for(DataRow row:rows) {
            row.camel(lower);
        }
        return this;
    }
    public DataSet<E> setIsNew(boolean bol) {
        for (DataRow row : rows) {
            row.setIsNew(bol);
        }
        return this;
    }

    /**
     * 移除每个条目中指定的key
     *
     * @param keys keys
     * @return DataSet
     */
    public DataSet<E> remove(String... keys) {
        for (DataRow row : rows) {
            for (String key : keys) {
                row.remove(key);
            }
        }
        return this;
    }

    /**
     * 多个空白压缩成一个空格
     * @return DataSet
     */
    public DataSet<E> compress() {
        for(DataRow row:rows) {
            row.compress();
        }
        return this;
    }
    public DataSet<E> compress(String ... keys) {
        for(DataRow row:rows) {
            row.compress(keys);
        }
        return this;
    }

    /**
     * 所有String类型的值执行trim
     * @return this
     */
    public DataSet<E> trim() {
        for(DataRow row:rows) {
            row.trim();
        }
        return this;
    }
    public DataSet<E> trim(String ... keys) {
        for(DataRow row:rows) {
            row.trim(keys);
        }
        return this;
    }

    /**
     * 全角转半角
     * @return this
     */
    public DataSet<E> sbc2dbc() {
        for(DataRow row:rows) {
            row.sbc2dbc();
        }
        return this;
    }
    public DataSet<E> sbc2dbc(String ... keys) {
        for(DataRow row:rows) {
            row.sbc2dbc(keys);
        }
        return this;
    }

    /**
     * 添加主键
     *
     * @param applyItem 是否应用到集合中的DataRow 默认true
     * @param pks       pks
     * @return DataSet
     */
    public DataSet<E> addPrimaryKey(boolean applyItem, String... pks) {
        if (null != pks) {
            List<String> list = new ArrayList<>();
            for (String pk : pks) {
                list.add(pk);
            }
            addPrimaryKey(applyItem, list);
        }
        return this;
    }

    public DataSet<E> addPrimaryKey(String... pks) {
        return addPrimaryKey(true, pks);
    }

    public DataSet<E> addPrimaryKey(boolean applyItem, Collection<String> pks) {
        if (null == primaryKeys) {
            primaryKeys = new ArrayList<>();
        }
        if (null == pks) {
            return this;
        }
        for (String pk : pks) {
            if (BasicUtil.isEmpty(pk)) {
                continue;
            }
            pk = key(pk);
            if (!primaryKeys.contains(pk)) {
                primaryKeys.add(pk);
            }
        }
        if (applyItem) {
            for (DataRow row : rows) {
                row.setPrimaryKey(false, primaryKeys);
            }
        }
        return this;
    }

    public DataSet<E> addPrimaryKey(Collection<String> pks) {
        return addPrimaryKey(true, pks);
    }

    /**
     * 设置主键
     *
     * @param applyItem applyItem
     * @param pks       pks
     * @return DataSet
     */
    public DataSet<E> setPrimaryKey(boolean applyItem, String... pks) {
        if (null != pks) {
            List<String> list = new ArrayList<>();
            for (String pk : pks) {
                list.add(pk);
            }
            setPrimaryKey(applyItem, list);
        }
        return this;
    }

    public DataSet<E> setPrimaryKey(String... pks) {
        return setPrimaryKey(true, pks);
    }

    public DataSet<E> setPrimaryKey(boolean applyItem, Collection<String> pks) {
        if (null == pks) {
            return this;
        }
        this.primaryKeys = new ArrayList<>();
        addPrimaryKey(applyItem, pks);
        return this;
    }

    public DataSet<E> setPrimaryKey(Collection<String> pks) {
        return setPrimaryKey(true, pks);
    }

    public DataSet<E> set(int index, E item) {
        rows.set(index, item);
        return this;
    }

    /**
     * 条目设置下标
     * @param key 属性
     * @param start 开始 默认0
     * @return this
     */
    public DataSet<E> setIndex(String key, int start){
        for(DataRow row:rows){
            row.put(key, start++);
        }
        return this;
    }
    public DataSet<E> setIndex(String key){
        return setIndex(key, 0);
    }
    
    /**
     * 是否有主键
     *
     * @return boolean
     */
    public boolean hasPrimaryKeys() {
        if (null != primaryKeys && !primaryKeys.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public Column getPrimaryColumn() {
        LinkedHashMap<String, Column> columns = getPrimaryColumns();
        if(!columns.isEmpty()) {
            return columns.values().iterator().next();
        }
        String pk = null;
        List<String> pks = getPrimaryKeys();
        if(!pks.isEmpty()) {
            pk = pks.get(0);
        }
        if(null == pk) {
            pk = DataRow.DEFAULT_PRIMARY_KEY;
        }
        Column column = null;
        if(null != metadatas) {
            column = metadatas.get(pk.toUpperCase());
        }else{
            column = new Column(pk);
        }
        return column;
    }
    public LinkedHashMap<String, Column> getPrimaryColumns() {
        LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
        if(null != metadatas) {
            for(Column column:metadatas.values()) {
                if(column.isPrimaryKey()) {
                    columns.put(column.getName().toUpperCase(), column);
                }
            }
        }
        if(columns.isEmpty()) {
            List<String> pks = getPrimaryKeys();
            if(null != pks) {
                for(String pk:pks) {
                    Column column = null;
                    if(null != metadatas) {
                        column = metadatas.get(pk.toUpperCase());
                    }
                    if(null == column) {
                        column = new Column(pk);
                    }
                    columns.put(pk.toUpperCase(), column);
                }
            }
        }
        return columns;
    }

    /**
     * 提取主键
     *
     * @return List
     */
    public List<String> getPrimaryKeys() {
        if (null == primaryKeys) {
            primaryKeys = new ArrayList<>();
        }
        return primaryKeys;
    }

    public DataSet<E> tag(String key, Object value) {
        tags.put(key, value);
        return this;
    }

    public DataSet<E> setTag(String key, Object value) {
        tags.put(key, value);
        return this;
    }

    public Object tag(String key) {
        return tags.get(key);
    }

    public Object getTag(String key) {
        return tags.get(key);
    }

    public Map<String, Object> getTags() {
        return tags;
    }

    /**
     * 添加表头
     *
     * @param col col
     * @return DataSet
     */
    public DataSet<E> addHead(String col) {
        if (null == head) {
            head = new ArrayList<>();
        }
        if ("ROW_NUMBER".equals(col)) {
            return this;
        }
        if (head.contains(col)) {
            return this;
        }
        head.add(col);
        return this;
    }

    /**
     * 表头
     *
     * @return List
     */
    public List<String> getHead() {
        return head;
    }

    public int indexOf(Object obj) {
        return rows.indexOf(obj);
    }

    /**
     * 超长部分忽略
     * @param length 最长显示长度
     * @param columns 检测列
     * @return this
     */
    public DataSet<E> ellipsis(int length, String ... columns) {
        for(DataRow row:rows) {
            row.ellipsis(length, columns);
        }
        return this;
    }

    /**
     * 从begin开始截断到end, 方法执行将改变原DataSet长度
     *
     * @param begin 开始位置
     * @param end   结束位置
     * @return DataSet
     */
    public DataSet<E> truncates(int begin, int end) {
        if (!rows.isEmpty()) {
            if (begin < 0) {
                begin = 0;
            }
            if (end >= rows.size()) {
                end = rows.size() - 1;
            }
            if (begin >= rows.size()) {
                begin = rows.size() - 1;
            }
            if (end <= 0) {
                end = 0;
            }
            rows = rows.subList(begin, end);
        }
        return this;
    }

    /**
     * 从begin开始截断到最后一个
     *
     * @param begin 开始位置
     * @return DataSet
     */
    public DataSet<E> truncates(int begin) {
        if (begin < 0) {
            begin = rows.size() + begin;
            int end = rows.size() - 1;
            return truncates(begin, end);
        } else {
            return truncates(begin, rows.size() - 1);
        }
    }

    /**
     * 从begin开始截断到最后一个并返回其中第一个DataRow
     *
     * @param begin 开始位置
     * @return DataRow
     */
    public DataRow truncate(int begin) {
        return truncate(begin, rows.size() - 1);
    }

    /**
     * 从begin开始截断到end位置并返回其中第一个DataRow
     *
     * @param begin 开始位置
     * @param end   结束位置
     * @return DataRow
     */
    public DataRow truncate(int begin, int end) {
        truncates(begin, end);
        if (!rows.isEmpty()) {
            return rows.get(0);
        } else {
            return null;
        }
    }

    /**
     * 从begin开始截取到最后一个
     *
     * @param begin 开始位置
     *              如果输入负数则取后n个, 如果造成数量不足, 则取全部
     * @return DataSet
     */
    public DataSet<E> cuts(int begin) {
        if (begin < 0) {
            begin = rows.size() + begin;
            int end = rows.size() - 1;
            return cuts(begin, end);
        } else {
            return cuts(begin, rows.size() - 1);
        }
    }

    /**
     * 从begin开始截取到end位置, 方法执行时会创建新的DataSet并不改变原有set长度
     *
     * @param begin 开始位置
     * @param end   结束位置
     * @return DataSet
     */
    public DataSet<E> cuts(int begin, int end) {
        DataSet<E> result = new DataSet();
        if (rows.isEmpty()) {
            return result;
        }
        if (begin < 0) {
            begin = 0;
        }
        if (end >= rows.size()) {
            end = rows.size() - 1;
        }
        if (begin >= rows.size()) {
            begin = rows.size() - 1;
        }
        if (end <= 0) {
            end = 0;
        }
        for (int i = begin; i <= end; i++) {
            result.add(rows.get(i));
        }
        return result;
    }

    /**
     * 从begin开始截取到最后一个, 并返回其中第一个DataRow
     *
     * @param begin 开始位置
     * @return DataSet
     */
    public DataRow cut(int begin) {
        return cut(begin, rows.size() - 1);
    }

    /**
     * 从begin开始截取到end位置, 并返回其中第一个DataRow, 方法执行时会创建新的DataSet并不改变原有set长度
     *
     * @param begin 开始位置
     * @param end   结束位置
     * @return DataSet
     */
    public DataRow cut(int begin, int end) {
        DataSet<E> result = cuts(begin, end);
        if (!result.isEmpty()) {
            return result.getRow(0);
        }
        return null;
    }

    /**
     * 记录数量
     *
     * @return int
     */
    public int size() {
        int result = 0;
        if (null != rows) {
            result = rows.size();
        }
        return result;
    }

    /**
     * 总行数 如果没有分页则与size()一致, 否则取navi的total row
     * @return int
     */
    public long total() {
        if(null != navi) {
            return navi.getTotalRow();
        }
        return rows.size();
    }

    /**
     * 计算行数
     * @param empty 空值是否参与计算
     * @param key 判断空值的属性
     * @return int
     */
    public int count(boolean empty, String key) {
        int result = 0;
        if(empty || BasicUtil.isEmpty(key)) {
            result = rows.size();
        }else {
            for (DataRow row:rows) {
                if(row.isNotEmpty(key)) {
                    result ++;
                }
            }
        }
        return result;
    }

    public int getSize() {
        return size();
    }

    /**
     * 是否出现异常
     *
     * @return boolean
     */
    public boolean isException() {
        return null != exception;
    }

    public boolean isFromCache() {
        return isFromCache;
    }

    public DataSet<E> setIsFromCache(boolean bol) {
        this.isFromCache = bol;
        return this;
    }

    /**
     * 返回数据是否为空
     *
     * @return boolean
     */
    public boolean isEmpty() {
        return null == rows || rows.isEmpty();
    }
    public boolean isNotEmpty(){
        return !isEmpty();
    }

    /**
     * 读取一行数据
     *
     * @param index 索引
     * @return DataRow
     */
    public E getRow(int index) {
        E row = null;
        if (null != rows && index < rows.size()) {
            row = rows.get(index);
        }
        if (null != row) {
            row.setContainer(this);
        }
        if(null == row && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            row = (E)new DataRow();
        }
        return row;
    }
    public E getFirstRow(){
        if(rows.isEmpty()){
            if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
                return (E)new DataRow();
            }
            return null;
        }
        return getRow(0);
    }
    public E getLastRow(){
        if(rows.isEmpty()){
            if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
                return (E)new DataRow();
            }
            return null;
        }
        return getRow(rows.size()-1);
    }

    public boolean exists(String ... params) {
        DataRow row = getRow(0, params);
        return row != null;
    }
    public DataRow getRow(Compare compare, String... params) {
        return getRow(compare, 0, params);
    }
    public DataRow getRow(String... params) {
        return getRow(null, 0, params);
    }
    public DataRow getRow(Compare compare, DataRow params) {
        return getRow(compare, 0, params);
    }
    public DataRow getRow( DataRow params) {
        return getRow(Compare.EQUAL, 0, params);
    }
    public DataRow getRow(Compare compare, List<String> params) {
        String[] kvs = BeanUtil.list2array(params);
        return getRow(compare, 0, kvs);
    }

    public DataRow getRow(List<String> params) {
        return getRow(Compare.EQUAL, params);
    }

    public DataRow getRow(Compare compare, int begin, String... params) {
        DataSet<E> set = getRows(compare, begin, 1, params);
        if (!set.isEmpty()) {
            return set.getRow(0);
        }
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }

    public DataSet<E> getRows(Map<String, String> kvs) {
        return getRows(0, -1, kvs);
    }
    public DataSet<E> getRows(Compare compare, Map<String, String> kvs) {
        return getRows(compare, 0, -1, kvs);
    }
    public DataRow getRow(int begin, String... params) {
        return getRow(Compare.EQUAL, begin, params);
    }
    public DataRow getRow(Compare compare, int begin, DataRow params) {
        DataSet<E> set = getRows(compare, begin, 1, params);
        if (!set.isEmpty()) {
            return set.getRow(0);
        }
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }
    public DataRow getRow(int begin, DataRow params) {
        return getRow(Compare.EQUAL, begin, params);
    }
    public DataRow getRowById(String value) {
        return getRow("ID", value);
    }
    public DataRow getRowByPrimvaryValue(String value) {
        return getRow(DataRow.DEFAULT_PRIMARY_KEY, value);
    }

    public DataSet<E> distinct(boolean extract, String... keys) {
        return distinct(extract, BeanUtil.array2list(keys));
    }
    public DataSet<E> distinct(String... keys) {
        return distinct(true, BeanUtil.array2list(keys));
    }
    
    /**
     * 根据keys去重
     *
     * @param extract 是否只保留keys列
     * @param keys keys
     * @return DataSet
     */
    public DataSet<E> distinct(boolean extract, List<String> keys) {
        DataSet<E> result = new DataSet();
        if (null != rows) {
            for (E row:rows) {
                // 查看result中是否已存在
                String[] params = packParam(row, keys);
                DataRow chk = result.getRow(params);
                if (chk == null || chk.isEmpty()) {
                    if(extract){
                        E tmp = (E)new DataRow(row.keyCase());
                        for (String key : keys) {
                            tmp.put(key, row.get(key));
                        }
                    result.addRow(tmp);
                    }else{
                        result.addRow(row);
                    }
                }
            }
        }
        result.copyProperty(this);
        return result;
    }
    public DataSet<E> distinct(List<String> keys) {
        return distinct(true, keys);
    }

    public DataSet<E> clone() {
        DataSet<E> clone = null;
        try{
            clone = (DataSet) super.clone();
        }catch (Exception ignored) {
            clone = new DataSet();
        }
        List<E> rows = new ArrayList<>();
        for (E row : this.rows) {
            rows.add((E)row.clone());
        }
        clone.setRows(rows);
        clone.copyProperty(this);
        return clone;
    }

    private DataSet<E> copyProperty(DataSet<E> from) {
        return copyProperty(from, this);
    }

    public static <T extends DataRow> DataSet<T> copyProperty(DataSet<T> from, DataSet<T> to) {
        if (null != from && null != to) {
            to.exception = from.exception;
            to.message = from.message;
            to.navi = from.navi;
            to.head = from.head;
            to.primaryKeys = from.primaryKeys;
            to.dataSource = from.dataSource;
            to.datalink = from.datalink;
            to.schema = from.schema;
            to.tables = from.tables;
            to.metadatas = from.metadatas;
        }
        return to;
    }

    /**
     * 指定key转换成number
     * @param keys keys
     * @return DataRow
     */
    public DataSet<E> convertNumber(String ... keys) {
        for(DataRow row:rows) {
            row.convertNumber(keys);
        }
        return this;
    }
    public DataSet<E> convertString(String ... keys) {
        for(DataRow row:rows) {
            row.convertString(keys);
        }
        return this;
    }
    public DataSet<E> convertInt(Integer def, String ... keys) {
        for(DataRow row:rows) {
            row.convertInt(def, keys);
        }
        return this;
    }
    public DataSet<E> convertInt(String ... keys) {
        Integer def = null;
        return convertInt(def, keys);
    }
    public DataSet<E> convertLong(String ... keys) {
        Long def = null;
        return convertLong(def, keys);
    }
    public DataSet<E> convertLong(Long def, String ... keys) {
        for(DataRow row:rows) {
            row.convertLong(def, keys);
        }
        return this;
    }
    public DataSet<E> convertDouble(String ... keys) {
        Double def = null;
        return convertDouble(def, keys);
    }
    public DataSet<E> convertDouble(Double def, String ... keys) {
        for(DataRow row:rows) {
            row.convertDouble(def, keys);
        }
        return this;
    }
    public DataSet<E> convertDecimal(String ... keys) {
        BigDecimal def = null;
        return convertDecimal(def, keys);
    }
    public DataSet<E> convertDecimal(BigDecimal def, String ... keys) {
        for(DataRow row:rows) {
            row.convertDecimal(def, keys);
        }
        return this;
    }
    public DataSet<E> skip(boolean skip) {
        for(DataRow row:rows) {
            row.skip = skip;
        }
        return this;
    }

    /**
     * 把k,v,k,v转换成map格式
     *
     * @param params k,v,k,v或k:v,k:v(只能二选一，只要有一个不带:就按第一种)
     * @return map
     */
    private Map<String, String> kvs(String... params) {
        Map<String, String> kvs = new HashMap<>();
        int len = params.length;
        boolean ignoreSplit = false;
        for(String param:params) {
            if(null == param || !param.contains(":")) {
                ignoreSplit = true;
                break;
            }
        }
        if(ignoreSplit) {
            for(int i=0; i<len; i+=2) {
                String k = params[i];
                String v = params[i+1];
                kvs.put(k, v);
            }
            return kvs;
        }
        int i = 0;
        String srcFlagTag = "srcFlag"; // 参数含有${}的 在kvs中根据key值+tag 放入一个新的键值对, 如时间格式TIME:{10:10}

        while (i < len) {
            String p1 = params[i];
            if (BasicUtil.isEmpty(p1)) {
                i++;
                continue;
            } else if (p1.contains(":")) {
                String ks[] = BeanUtil.parseKeyValue(p1);
                kvs.put(ks[0], ks[1]);
                i++;
                continue;
            } else {
                if (i + 1 < len) {
                    String p2 = params[i + 1];
                    if (BasicUtil.isEmpty(p2) || !p2.contains(":")) {
                        kvs.put(p1, p2);
                        i += 2;
                        continue;
                    //} else if (p2.startsWith("${") && p2.endsWith("}")) {
                    } else if (BasicUtil.checkEl(p2)) {
                        p2 = p2.substring(2, p2.length() - 1);
                        kvs.put(p1, p2);
                        kvs.put(p1 + srcFlagTag, "true");
                        i += 2;
                        continue;
                    } else {
                        String ks[] = BeanUtil.parseKeyValue(p2);
                        kvs.put(ks[0], ks[1]);
                        i += 2;
                        continue;
                    }
                }
            }
            i++;
        }
        return kvs;
    }

    /**
     * 筛选符合条件的集合
     * 注意如果String类型 1与1.0比较不相等, 可以先调用convertNumber转换一下数据类型
     * @param params key1, value1, key2:value2, key3, value3
     *               "NM:zh%","AGE:&gt;20","NM","%zh%"
     * @param begin  begin
     * @param qty    最多筛选多少个 0表示不限制
     * @return DataSet
     */
    public DataSet<E> getRows(int begin, int qty, String... params) {
        return getRows(begin, qty, kvs(params));
    }
    public DataSet<E> getRows(PageNavi navi, String ... params) {
        return getRows((int)navi.getFirstRow(), (int)navi.getLastRow(), params);
    }
    public DataSet<E> getRows(Compare compare, int begin, int qty, String... params) {
        return getRows(compare, begin, qty, kvs(params));
    }
    public DataSet<E> getRows(Compare compare, int begin, int qty, DataRow kvs) {
        Map<String, String> map = new HashMap<String, String>();
        for(String k:kvs.keySet()) {
            map.put(k, kvs.getString(k));
        }
        return getRows(compare, begin, qty, map);
    }

    public DataSet<E> getRows(int begin, int qty, DataRow kvs) {
        return getRows(Compare.EQUAL, begin, qty, kvs);
    }
    public DataSet<E> getRows(int begin, int qty, Map<String, String> kvs) {
        return getRows(Compare.EQUAL, begin, qty, kvs);
    }

    /**
     *
     * @param compare 对比方式, 如果不指定则根据k:v解析 如 k:%v%
     * @param begin 开始
     * @param qty 结果最大数量
     * @param kvs 条件
     * @return DataSet
     */
    public DataSet<E> getRows(Compare compare, int begin, int qty, Map<String, String> kvs) {
        DataSet<E> set = new DataSet();
        if(rows.isEmpty()) {
            return set;
        }
        String srcFlagTag = "srcFlag"; // 参数含有{}的 在kvs中根据key值+tag 放入一个新的键值对

        Map<String, Compare> compares = new HashMap<>();
        Map<String, String> compareKvs = new HashMap<>();
        if(null == compare || compare == Compare.AUTO) {
            for (String k : kvs.keySet()) {
                // k(ID), v(>=10)(%A%)
                String v = kvs.get(k);
                if (null != v) {
                    // 与SQL.TYPE保持一致
                    Compare cmp = Compare.EQUAL;
                    if(v.isEmpty()){
                        cmp = Compare.EMPTY;
                    }else if (v.startsWith("=")) {
                        v = v.substring(1);
                        cmp = Compare.EQUAL;
                    } else if (v.startsWith(">")) {
                        v = v.substring(1);
                        cmp = Compare.GREAT;
                    } else if (v.startsWith(">=")) {
                        v = v.substring(2);
                        cmp = Compare.GREAT_EQUAL;
                    } else if (v.startsWith("<")) {
                        v = v.substring(1);
                        cmp = Compare.LESS;
                    } else if (v.startsWith("<=")) {
                        v = v.substring(2);
                        cmp = Compare.LESS_EQUAL;
                    } else if (v.startsWith("%") && v.endsWith("%")) {
                        v = v.substring(1, v.length() - 1);
                        cmp = Compare.LIKE;
                    } else if (v.endsWith("%")) {
                        v = v.substring(0, v.length() - 1);
                        cmp = Compare.LIKE_PREFIX;
                    } else if (v.startsWith("%")) {
                        v = v.substring(1);
                        cmp = Compare.LIKE_SUFFIX;
                    }
                    compareKvs.put(k, v);
                    compares.put(k, cmp);
                }else{
                    compareKvs.put(k, v);
                    compares.put(k, Compare.NULL);
                }
            }
            kvs = compareKvs;
        }
        int size = rows.size();
        for (int i=begin; i<size; i++) {
            E row = rows.get(i);
            if(row.skip) {
                continue;
            }
            boolean chk = true;//对比结果
            for (String k : kvs.keySet()) {
                boolean srcFlag = false;
                if (k.endsWith(srcFlagTag)) {
                    continue;
                } else {
                    String srcFlagValue = kvs.get(k + srcFlagTag);
                    if (BasicUtil.isNotEmpty(srcFlagValue)) {
                        srcFlag = true;
                    }
                }
                Object value = row.get(k);
                String v = kvs.get(k);
                if(!row.containsKey(k) && null == value) {
                    // 注意这里有可能是个复合key
                    chk = false;
                    break;
                }

                if (null == v) {
                    if (null != value) {
                        chk = false;
                        break;
                    }else{
                        continue;
                    }
                } else {
                    if (null == value) {
                        chk = false;
                        break;
                    }
                    Compare cmp = null;
                    if(null != compare && Compare.AUTO != compare) {
                        cmp = compare;
                    }else{
                        cmp = compares.get(k);
                    }
                    if(null != cmp) {
                        if (srcFlag) {
                            v = "${" + v + "}";
                        }
                        int code = cmp.getCode();
                        if(code == 60 || code == 61 || code == 62) {
                            if (!cmp.compare(v, value)) {
                                chk = false;
                                break;
                            }
                        }else {
                            if (!cmp.compare(value, v)) {
                                chk = false;
                                break;
                            }
                        }
                    }
                }
            }

            if (chk) {
                set.add(row);
                if (qty > 0 && set.size() >= qty) {
                    break;
                }
            }
        }//end for rows

        set.copyProperty(this);
        return set;
    }

    public DataSet<E> getRows(Compare compare, int begin, String... params) {
        return getRows(compare, begin, -1, params);
    }

    public DataSet<E> getRows(int begin, String... params) {
        return getRows(Compare.EQUAL, begin, -1, params);
    }
    public DataSet<E> getRows(Compare compare, String... params) {
        return getRows(compare, 0, params);
    }
    public DataSet<E> getRows(String... params) {
        return getRows(Compare.AUTO, 0, params);
    }

    public DataSet<E>  getRows(Compare compare, DataSet<E>  set, String key) {
        String kvs[] = new String[set.size()];
        int i = 0;
        for (E row : set) {
            String value = row.getString(key);
            if (BasicUtil.isNotEmpty(value)) {
                kvs[i++] = key + ":" + value;
            }
        }
        return getRows(compare, kvs);
    }

    public DataSet<E> getRows(DataSet<E> set, String key) {
        return getRows(Compare.EQUAL, set, key);
    }
    public DataSet<E> getRows(Compare compare, DataRow row, String... keys) {
        List<String> list = new ArrayList<>();
        int i = 0;
        for (String key : keys) {
            String value = row.getString(key);
            if (BasicUtil.isNotEmpty(value)) {
                list.add(key + ":" + value);
            }
        }
        String[] kvs = BeanUtil.list2array(list);
        return getRows(compare, kvs);
    }

    public DataSet<E> getRows(DataRow row, String... keys) {
        return getRows(Compare.EQUAL, row, keys);
    }

    /**
     * 提取符合指定属性值的集合
     *
     * @param begin begin
     * @param end   end
     * @param key   key
     * @param value value
     * @return DataSet
     */
    public DataSet<E> filter(int begin, int end, String key, String value) {
        DataSet<E> set = new DataSet();
        String tmpValue;
        int size = size();
        if (begin < 0) {
            begin = 0;
        }
        for (int i = begin; i < size && i <= end; i++) {
            tmpValue = getString(i, key, "");
            if ((null == value && null == tmpValue)
                    || (null != value && value.equals(tmpValue))) {
                set.add(getRow(i));
            }
        }
        set.copyProperty(this);
        return set;
    }

    public DataSet<E> getRows(int fr, int to) {
        DataSet<E> set = new DataSet();
        int size = this.size();
        if (fr < 0) {
            fr = 0;
        }
        for (int i = fr; i < size && i <= to; i++) {
            set.addRow(getRow(i));
        }
        return set;
    }

    /**
     * 合计(如果提供多个key则多列合计一个值)
     * @param begin 开始
     * @param end 结束
     * @param keys 列
     * @return BigDecimal
     */
    public BigDecimal sum(int begin, int end, String ... keys) {
        BigDecimal result = BigDecimal.ZERO;
        int size = rows.size();
        if (begin <= 0) {
            begin = 0;
        }
        for (int i = begin; i < size && i <= end; i++) {
            for(String key:keys) {
                BigDecimal tmp = getDecimal(i, key, 0);
                if (null != tmp) {
                    result = result.add(tmp);
                }
            }
        }
        return result;
    }

    public BigDecimal sum(String ... keys) {
        BigDecimal result = BigDecimal.ZERO;
        result = sum(0, size() - 1, keys);
        return result;
    }

    /**
     * 多列合计
     * @param result 保存合计结果
     * @param keys keys
     * @return DataRow
     */
    public DataRow sums(DataRow result, List<String> keys) {
        if(null == result) {
            result = new DataRow();
        }
        if (size() > 0)  {
            if(keys.isEmpty()) {
                keys = getRow(0).numberKeys();
            }
            for (String key : keys) {
                result.put(key, sum(key));
            }
        }
        return result;
    }
    public DataRow sums(DataRow result, String... keys) {
        return sums(result, BeanUtil.array2list(keys));
    }
    public DataRow sums(String ... keys) {
        return sums(new DataRow(), BeanUtil.array2list(keys));
    }

    /**
     * 多列平均值
     *
     * @param result 保存合计结果
     * @param keys keys
     * @return DataRow
     */
    public DataRow avgs(DataRow result, List<String>  keys) {
        if(null == result) {
            result = new DataRow();
        }
        if (size() > 0) {
            if(keys.isEmpty()) {
                keys = getRow(0).numberKeys();
            }
            for (String key : keys) {
                result.put(key, avg(key));
            }
        }
        return result;
    }

    public DataRow avgs(DataRow result, String... keys) {
        return avgs(result, BeanUtil.array2list(keys));
    }
    public DataRow avgs(String ... keys) {
        return avgs(new DataRow(), BeanUtil.array2list(keys));
    }

    /**
     * 多列平均值
     * @param result 保存合计结果
     * @param scale 小数位
     * @param keys keys
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return DataRow
     */
    public DataRow avgs(DataRow result, boolean empty, int scale, int round, List<String> keys) {
        if(null == result) {
            result = new DataRow();
        }
        if (size() > 0) {
            if(keys.isEmpty()) {
                keys = getRow(0).numberKeys();
            }
            for (String key : keys) {
                result.put(key, avg(empty, scale, round, key));
            }
        }
        return result;
    }

    public DataRow avgs(DataRow result, boolean empty, int scale, int round, String... keys) {
        return avgs(result, empty, scale, round, BeanUtil.array2list(keys));
    }
    public DataRow avgs(boolean empty, int scale, int round, String ... keys) {
        return avgs(new DataRow(), empty, scale, round, BeanUtil.array2list(keys));
    }

    /**
     * 最大值
     *
     * @param top 多少行
     * @param key key
     * @return BigDecimal
     */
    public BigDecimal maxDecimal(int top, String key) {
        BigDecimal result = null;
        int size = rows.size();
        if (size > top) {
            size = top;
        }
        for (int i = 0; i < size; i++) {
            BigDecimal tmp = getDecimal(i, key, 0);
            if (null != tmp && (null == result || tmp.compareTo(result) > 0)) {
                result = tmp;
            }
        }
        return result;
    }

    public BigDecimal maxDecimal(String key) {
        return maxDecimal(size(), key);
    }

    public int maxInt(int top, String key) {
        BigDecimal result = maxDecimal(top, key);
        if (null == result) {
            return 0;
        }
        return result.intValue();
    }

    public int maxInt(String key) {
        return maxInt(size(), key);
    }

    public double maxDouble(int top, String key) {
        BigDecimal result = maxDecimal(top, key);
        if (null == result) {
            return 0;
        }
        return result.doubleValue();
    }
    public double maxDouble(String key) {
        return maxDouble(size(), key);
    }
    public long maxLong(int top, String key) {
        BigDecimal result = maxDecimal(top, key);
        if (null == result) {
            return 0;
        }
        return result.longValue();
    }
    public long maxLong(String key) {
        return maxLong(size(), key);
    }
    public double maxFloat(int top, String key) {
        BigDecimal result = maxDecimal(top, key);
        if (null == result) {
            return 0;
        }
        return result.floatValue();
    }

    public double maxFloat(String key) {
        return maxFloat(size(), key);
    }

//	public BigDecimal max(int top, String key) {
//		BigDecimal result = maxDecimal(top, key);
//		return result;
//	}
//	public BigDecimal max(String key) {
//		return maxDecimal(size(), key);
//	}

    /**
     * 最小值
     *
     * @param top 多少行
     * @param key key
     * @return BigDecimal
     */
    public BigDecimal minDecimal(int top, String key) {
        BigDecimal result = null;
        int size = rows.size();
        if (size > top) {
            size = top;
        }
        for (int i = 0; i < size; i++) {
            BigDecimal tmp = getDecimal(i, key, 0);
            if (null != tmp && (null == result || tmp.compareTo(result) < 0)) {
                result = tmp;
            }
        }
        return result;
    }

    public BigDecimal minDecimal(String key) {
        return minDecimal(size(), key);
    }

    public int minInt(int top, String key) {
        BigDecimal result = minDecimal(top, key);
        if (null == result) {
            return 0;
        }
        return result.intValue();
    }

    public int minInt(String key) {
        return minInt(size(), key);
    }

    public double minDouble(int top, String key) {
        BigDecimal result = minDecimal(top, key);
        if (null == result) {
            return 0;
        }
        return result.doubleValue();
    }

    public double minDouble(String key) {
        return minDouble(size(), key);
    }
    public double minFloat(int top, String key) {
        BigDecimal result = minDecimal(top, key);
        if (null == result) {
            return 0;
        }
        return result.floatValue();
    }

    public double minFloat(String key) {
        return minFloat(size(), key);
    }

    /**
     * key对应的value最大的一行
     *
     * @param key key
     * @return DataRow
     */
    public DataRow max(String key) {
        int size = size();
        if (size == 0) {
            return null;
        }
        asc(key);
        return getRow(size - 1);
    }

    public DataRow min(String key) {
        int size = size();
        if (size == 0) {
            return null;
        }
        asc(key);
        return getRow(0);
    }

    /**
     * 平均值 空数据不参与加法但参与除法
     *
     * @param empty 空值是否参与计算
     * @param top 多少行
     * @param keys key
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return BigDecimal
     */
    public BigDecimal avg(boolean empty, int scale, int round, int top, String  ... keys) {
        BigDecimal result = BigDecimal.ZERO;
        int size = rows.size();
        if (size > top) {
            size = top;
        }
        int count = 0;
        for (int i = 0; i < size; i++) {
            for(String key:keys) {
                BigDecimal tmp = getDecimal(i, key, 0);
                if (null != tmp) {
                    result = result.add(tmp);
                }
                if (null != tmp || empty) {
                    count++;
                }
            }
        }
        if (count > 0) {
            result = result.divide(new BigDecimal(count), scale, round);
        }
        return result;
    }

    public BigDecimal avg(int scale, int round, String  ... keys) {
        return avg(true, scale, round, size(), keys);
    }
    public BigDecimal avg(boolean empty, int scale, int round, String  ... keys) {
        return avg(empty, scale, round, size(), keys);
    }

    public BigDecimal avg(boolean empty, String  ... keys) {
        return avg(empty, 2, BigDecimal.ROUND_HALF_UP, size(), keys);
    }

    public BigDecimal avg(String ... keys) {
        return avg(true, keys);
    }

    /**
     * 计算key列值在整个集合中占比
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @param keys keys
     */
    public void percent(int scale, int round, String ... keys) {
        for(String key:keys) {
            BigDecimal sum = sum(key);
            if(null != sum && sum.compareTo(BigDecimal.ZERO) > 0) {
                for(DataRow row:rows) {
                    BigDecimal value = row.getDecimal(key, (BigDecimal) null);
                    if(null != value) {
                        BigDecimal percent = value.divide(sum, scale, round);
                        row.put(key+"_percent", percent);
                    }
                }
            }
        }
    }
    public void percent(String ... keys) {
        percent(2, 4, keys);
    }

    /**
     * 加权均值
     * @param factor 权重计算列
     * @param key 值计算列
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     */
    public BigDecimal wac(int scale, int round, String factor, String key) {
        BigDecimal result = null;
        BigDecimal sum_value = BigDecimal.ZERO;
        BigDecimal sum_factor = sum(factor);
        for(DataRow row:rows) {
            sum_value = sum_value.add(row.multiply(factor, key));
        }
        if(null != sum_value && null != sum_factor && sum_factor.compareTo(BigDecimal.ZERO)>0) {
            result = sum_value.divide(sum_factor, scale, round);
        }
        return result;
    }
    public BigDecimal wac(String factor, String key) {
        return wac(2, BigDecimal.ROUND_HALF_UP, factor, key);
    }
    
    /**
     * 多列的乘积
     * @param target 结果存放位置
     * @param empty 是否计算空列 如果计算会算出0
     * @param keys keys
     */
    public void multiply(String target, boolean empty, String ... keys) {
        if(BasicUtil.isEmpty(target)) {
            target = BeanUtil.concat(keys);
        }
        for(DataRow row:rows) {
            BigDecimal multiply = row.multiply(empty, keys);
            row.put(target, multiply);
        }
    }
    public void multiply(boolean empty, String ... keys) {
        multiply(null, empty, keys);
    }
    public void multiply(String ... keys) {
        multiply(null, true, keys);
    }
    
    /**
     * 中位数
     *
     * @param key key
     * @return BigDecimal
     */
    public BigDecimal median(String key) {
        List<BigDecimal> numbers = getDecimals(key, BigDecimal.ZERO);
        Collections.sort(numbers);
        int size = numbers.size();
        int middle =size / 2;
        if(size > 0) {
            if (size % 2 == 1) {
                // 如果数组长度是奇数，直接返回中间的数
                return numbers.get(middle);
            } else {
                // 如果数组长度是偶数，返回中间两个数的平均值
                return numbers.get(middle - 1).add(numbers.get(middle)).divide(new BigDecimal(2));
            }
        }
        return null;
    }
    public Double median(String key, Double def) {
        BigDecimal median = median(key);
        if(null == median) {
            return def;
        }
        return median.doubleValue();
    }
    public Float median(String key, Float def) {
        BigDecimal median = median(key);
        if(null == median) {
            return def;
        }
        return median.floatValue();
    }
    public Long median(String key, Long def) {
        BigDecimal median = median(key);
        if(null == median) {
            return def;
        }
        return median.longValue();
    }
    public Integer median(String key, Integer def) {
        BigDecimal median = median(key);
        if(null == median) {
            return def;
        }
        return median.intValue();
    }

    /**
     * 求和
     * [
     *  {NM:部门1, USERS:[{LVL:1, SCORE:6}, {LVL:1, SCORE:7}, {LVL:2, SCORE:8}]}
     *, {NM:部门2, USERS:[{LVL:1, SCORE:60}, {LVL:3, SCORE:70}, {LVL:2, SCORE:80}]}
     *, {NM:部门3, USERS:[{LVL:1, SCORE:600}, {LVL:5, SCORE:700}, {LVL:2, SCORE:800}]}
     * ]
     * sum("TOTAL","USERS","SCORE","LVL&gt;1") 计算每个部门中 LVL大于1部分的用户子集 的SCORE合计 计算结果存储在TOTAL属性中
     * [
     *  {NM:部门1, TOTAL:8, USERS:[{LVL:1, SCORE:6}, {LVL:1, SCORE:7}, {LVL:2, SCORE:8}]}
     *, {NM:部门2, TOTAL:150, USERS:[{LVL:1, SCORE:60}, {LVL:3, SCORE:70}, {LVL:2, SCORE:80}]}
     *, {NM:部门3, TOTAL:2100, USERS:[{LVL:6, SCORE:600}, {LVL:5, SCORE:700}, {LVL:2, SCORE:800}]}
     * ]
     * @param result 合计结果存储
     * @param compare 条件过滤对比方式
     * @param items 计算条目中的 items 集合
     * @param field 根据field列 求和
     * @param conditions 查询条件 支持k:v k:v::type 以及原生sql形式(包含ORDER、GROUP、HAVING)默认忽略空值条件
     * @return DataSet
     */
    public DataSet<E> sum(String result, String items, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.sum(field));
        }
        return this;
    }

    public DataSet<E> avg(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.avg(scale, round, field));
        }
        return this;
    }

    public DataSet<E> var(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.var(scale, round, field));
        }
        return this;
    }
    public DataSet<E> min(String result, String items, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.min(field));
        }
        return this;
    }
    public DataSet<E> max(String result, String items, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.max(field));
        }
        return this;
    }
    public DataSet<E> count(String result, String items, boolean empty, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.count(empty, field));
        }
        return this;
    }
    public DataSet<E> vara(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.vara(scale, round, field));
        }
        return this;
    }

    public DataSet<E> varp(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.varp(scale, round, field));
        }
        return this;
    }

    public DataSet<E> varpa(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.varpa(scale, round, field));
        }
        return this;
    }
    public DataSet<E> stdev(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.stdev(scale, round, field));
        }
        return this;
    }

    public DataSet<E> stdeva(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.stdeva(scale, round, field));
        }
        return this;
    }

    public DataSet<E> stdevp(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.stdevp(scale, round, field));
        }
        return this;
    }

    public DataSet<E> stdevpa(String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.stdevpa(scale, round, field));
        }
        return this;
    }

    public DataSet<E> agg(Aggregation agg, String result, String items, int scale, int round, String field, Compare compare, String ... conditions) {
        for(DataRow row:rows) {
            DataSet<DataRow> set = row.getSet(items);
            if(null != conditions && conditions.length>0) {
                set = set.getRows(compare, conditions);
            }
            row.put(result, set.agg(agg, scale, round, field));
        }
        return this;
    }
    public DataSet<E> sum(String result, String items, String field, String ... conditions) {
        return sum(result, items, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> avg(String result, String items, int scale, int round, String field, String ... conditions) {
        return avg(result, items, scale, round, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> var(String result, String items, int scale, int round, String field, String ... conditions) {
        return var(result, items, scale, round, field, Compare.EQUAL, conditions);
    }
    public DataSet<E> min(String result, String items, String field, String ... conditions) {
        return min(result, items, field, Compare.EQUAL, conditions);
    }
    public DataSet<E> max(String result, String items, String field, String ... conditions) {
        return max(result, items, field, Compare.EQUAL, conditions);
    }
    public DataSet<E> count(String result, String items, boolean empty, String field, String ... conditions) {
        return count(result, items, empty, field, Compare.EQUAL, conditions);
    }
    public DataSet<E> vara(String result, String items, int scale, int round, String field, String ... conditions) {
        return vara(result, items, scale, round, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> varp(String result, String items, int scale, int round, String field, String ... conditions) {
        return varp(result, items, scale, round, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> varpa(String result, String items, int scale, int round, String field, String ... conditions) {
        return varpa(result, items, scale, round, field, Compare.EQUAL, conditions);
    }
    public DataSet<E> stdev(String result, String items, int scale, int round, String field, String ... conditions) {
        return stdev(result, items, scale, round, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> stdeva(String result, String items, int scale, int round, String field, String ... conditions) {
        return stdeva(result, items, scale, round, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> stdevp(String result, String items, int scale, int round, String field, String ... conditions) {
        return stdevp(result, items, scale, round, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> agg(Aggregation agg, String result, String items, int scale, int round, String field, String ... conditions) {
        return agg(agg, result, items, scale, round, field, Compare.EQUAL, conditions);
    }

    public DataSet<E> addRow(E row) {
        if (null != row) {
            rows.add(row);
        }
        return this;
    }

    public DataSet<E> addRow(int idx, E row) {
        if (null != row) {
            rows.add(idx, row);
        }
        return this;
    }

    /**
     * 合并key例的值 以connector连接
     *
     * @param key       key
     * @param connector connector
     * @return String v1, v2, v3
     */
    public String concat(String key, String connector) {
        return BasicUtil.concat(getStrings(key), connector);
    }

    public String concatNvl(String key, String connector) {
        return BasicUtil.concat(getNvlStrings(key), connector);
    }

    /**
     * 合并key例的值 以connector连接(不取null值)
     *
     * @param key       key
     * @param connector connector
     * @return String v1, v2, v3
     */
    public String concatWithoutNull(String key, String connector) {
        return BasicUtil.concat(getStringsWithoutNull(key), connector);
    }

    /**
     * 合并key例的值 以connector连接(不取空值)
     *
     * @param key       key
     * @param connector connector
     * @return String v1, v2, v3
     */
    public String concatWithoutEmpty(String key, String connector) {
        return BasicUtil.concat(getStringsWithoutEmpty(key), connector);
    }

    public String concatNvl(String key) {
        return BasicUtil.concat(getNvlStrings(key), ",");
    }

    public String concatWithoutNull(String key) {
        return BasicUtil.concat(getStringsWithoutNull(key), ",");
    }

    public String concatWithoutEmpty(String key) {
        return BasicUtil.concat(getStringsWithoutEmpty(key), ",");
    }

    public String concat(String key) {
        return concat(false, key);
    }

    public String concat(boolean distinct, String key) {
        List<String> values = null;
        if(distinct){
            values = getDistinctStrings(key);
        }else{
            values = getStrings(key);
        }
        return BasicUtil.concat(values, ",");
    }

    /**
     * 提取单列值
     *
     * @param key key
     * @return List
     */
    public List<Object> fetchValues(String key) {
        List<Object> result = new ArrayList<Object>();
        for (int i = 0; i < size(); i++) {
            result.add(get(i, key));
        }
        return result;
    }

    /**
     * 取单列不重复的值
     *
     * @param key key
     * @return List
     */
    public List<String> fetchDistinctValue(String key) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < size(); i++) {
            String value = getString(i, key, "");
            if (result.contains(value)) {
                continue;
            }
            result.add(value);
        }
        return result;
    }

    public List<String> fetchDistinctValues(String key) {
        return fetchDistinctValue(key);
    }

    /**
     * 分页
     *
     * @param link link
     * @return String
     */
    public String displayNavi(String link) {
        String result = "";
        if (null != navi) {
            result = navi.getHtml();
        }
        return result;
    }

    public String navi(String link) {
        return displayNavi(link);
    }

    public String displayNavi() {
        return displayNavi(null);
    }

    public String navi() {
        return displayNavi(null);
    }

    public DataSet<E> put(int idx, String key, Object value) {
        DataRow row = getRow(idx);
        if (null != row) {
            row.put(key, value);
        }
        return this;
    }

    public DataSet<E> removes(String... keys) {
        for (DataRow row : rows) {
            row.removes(keys);
        }
        return this;
    }

    /**
     * String
     *
     * @param index 索引
     * @param key   key
     * @return String
     * @throws Exception 异常 Exception
     */
    public String getString(int index, String key) throws Exception {
        return getRow(index).getString(key);
    }

    public String getString(int index, String key, String def) {
        try {
            return getString(index, key);
        } catch (Exception e) {
            return def;
        }
    }
    public Point getPoint(int index, String key, Point def) {
        try {
            return getPoint(index, key);
        } catch (Exception e) {
            return def;
        }
    }
    public Point getPoint(int index, String key) throws Exception {
        return getRow(index).getPoint(key);
    }

    public String getString(String key) throws Exception {
        return getString(0, key);
    }

    public String getString(String key, String def) {
        return getString(0, key, def);
    }

    public Object get(int index, String key) {
        DataRow row = getRow(index);
        if (null != row) {
            return row.get(key);
        }
        return null;
    }

    public List<Object> gets(String key) {
        List<Object> list = new ArrayList<Object>();
        for (DataRow row : rows) {
            list.add(row.get(key));
        }
        return list;
    }

    public List<DataSet> getSets(String key) {
        List<DataSet> list = new ArrayList<DataSet>();
        for (DataRow row : rows) {
            DataSet<DataRow> set = row.getSet(key);
            if (null != set) {
                list.add(set);
            }
        }
        return list;
    }

    public List<String> getStrings(String key) {
        List<String> result = new ArrayList<>();
        for (DataRow row : rows) {
            result.add(row.getString(key));
        }
        return result;
    }

    public List<String> getStrings(String key, String ... defs) {
        List<String> result = new ArrayList<>();
        for (DataRow row : rows) {
            result.add(row.getStringNvl(key, defs));
        }
        return result;
    }

    /**
     * 根据keys返回数组列表
     * [
     * {120.1, 36.1}
     *, {120.2, 36.2}
     * ]
     * @param keys keys 如 lng, lat
     * @return List
     */
    public List<String[]> getStringArrays(String ... keys) {
        List<String[]> result = new ArrayList<>();
        for(DataRow row:rows) {
            int len = keys.length;
            String[] item = new String[len];
            for(int i=0; i<len; i++) {
                String key = keys[i];
                item[i] = row.getString(key);
            }
            result.add(item);
        }
        return result;
    }

    public List<BigDecimal> getDecimals(String key, BigDecimal def) {
        List<BigDecimal> result = new ArrayList<>();
        for(DataRow row:rows) {
            result.add(row.getDecimal(key, def));
        }
        return result;
    }
    public List<Integer> getInts(String key) throws Exception {
        List<Integer> result = new ArrayList<Integer>();
        for (DataRow row : rows) {
            result.add(row.getInt(key));
        }
        return result;
    }
    public List<Integer> getDistinctInts(String key) throws Exception {
        List<Integer> result = new ArrayList<Integer>();
        for (DataRow row : rows) {
            int item = row.getInt(key);
            if(!result.contains(row)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<Integer> getInts(String key, int def) {
        List<Integer> result = new ArrayList<Integer>();
        for (DataRow row : rows) {
            result.add(row.getInt(key, def));
        }
        return result;
    }

    public List<Integer[]> getIntArrays(String ... keys) throws Exception {
        List<Integer[]> result = new ArrayList<>();
        for(DataRow row:rows) {
            int len = keys.length;
            Integer[] item = new Integer[len];
            for(int i=0; i<len; i++) {
                String key = keys[i];
                item[i] = row.getInt(key);
            }
            result.add(item);
        }
        return result;
    }
    public List<Integer[]> getIntArrays(Integer def, List<String> keys) {
        List<Integer[]> result = new ArrayList<>();
        for(DataRow row:rows) {
            int len = keys.size();
            Integer[] item = new Integer[len];
            for(int i=0; i<len; i++) {
                String key = keys.get(i);
                item[i] = row.getInt(key, def);
            }
            result.add(item);
        }
        return result;
    }
    public List<Integer[]> getIntArrays(Integer def, String ... keys) {
        return getIntArrays(def, BeanUtil.array2list(keys));
    }

    public List<Long> getLongs(String key) throws Exception {
        List<Long> result = new ArrayList<>();
        for (DataRow row : rows) {
            result.add(row.getLong(key));
        }
        return result;

    }

    public List<Long> getLongs(String key, Long def) {
        List<Long> result = new ArrayList<>();
        for (DataRow row : rows) {
            result.add(row.getLong(key, def));
        }
        return result;
    }
    public List<Long> getDistinctLongs(String key, Long def) {
        List<Long> result = new ArrayList<>();
        for (DataRow row : rows) {
            long item = row.getLong(key, def);
            if(!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<Long[]> getLongArrays(String ... keys) throws Exception {
        List<Long[]> result = new ArrayList<>();
        for(DataRow row:rows) {
            int len = keys.length;
            Long[] item = new Long[len];
            for(int i=0; i<len; i++) {
                String key = keys[i];
                item[i] = row.getLong(key);
            }
            result.add(item);
        }
        return result;
    }
    public List<Long[]> getLongArrays(Long def, List<String> keys) {
        List<Long[]> result = new ArrayList<>();
        for(DataRow row:rows) {
            int len = keys.size();
            Long[] item = new Long[len];
            for(int i=0; i<len; i++) {
                String key = keys.get(i);
                item[i] = row.getLong(key, def);
            }
            result.add(item);
        }
        return result;
    }
    public List<Long[]> getLongArrays(Long def, String ... keys) {
        return getLongArrays(def, BeanUtil.array2list(keys));
    }

    public List<Double> getDoubles(String key) throws Exception {
        List<Double> result = new ArrayList<>();
        for (DataRow row : rows) {
            result.add(row.getDouble(key));
        }
        return result;
    }

    public List<Double> getDistinctDoubles(String key) throws Exception {
        List<Double> result = new ArrayList<>();
        for (DataRow row : rows) {
            double item = row.getDouble(key);
            if(!result.contains(item)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<Double> getDoubles(String key, Double def) {
        List<Double> result = new ArrayList<>();
        for (DataRow row : rows) {
            result.add(row.getDouble(key, def));
        }
        return result;

    }
    public List<Double[]> getDoubleArrays(String ... keys) throws Exception {
        List<Double[]> result = new ArrayList<>();
        for(DataRow row:rows) {
            int len = keys.length;
            Double[] item = new Double[len];
            for(int i=0; i<len; i++) {
                String key = keys[i];
                item[i] = row.getDouble(key);
            }
            result.add(item);
        }
        return result;
    }

    public List<Double[]> getDoubleArrays(Double def, List<String> keys) {
        List<Double[]> result = new ArrayList<>();
        for(DataRow row:rows) {
            int len = keys.size();
            Double[] item = new Double[len];
            for(int i=0; i<len; i++) {
                String key = keys.get(i);
                item[i] = row.getDouble(key, def);
            }
            result.add(item);
        }
        return result;
    }

    public List<Double[]> getDoubleArrays(Double def, String ... keys) {
        return getDoubleArrays(def, BeanUtil.array2list(keys));
    }

    public List<Object> getObjects(String key) {
        List<Object> result = new ArrayList<Object>();
        for (DataRow row : rows) {
            result.add(row.get(key));
        }
        return result;

    }

    public List<String> getDistinctStrings(String key) {
        return fetchDistinctValue(key);
    }

    public List<String> getNvlStrings(String key) {
        List<String> result = new ArrayList<>();
        List<Object> list = fetchValues(key);
        for (Object val : list) {
            if (null != val) {
                result.add(val.toString());
            } else {
                result.add("");
            }
        }
        return result;
    }

    public List<String> getStringsWithoutEmpty(String key) {
        List<String> result = new ArrayList<>();
        List<Object> list = fetchValues(key);
        for (Object val : list) {
            if (BasicUtil.isNotEmpty(val)) {
                result.add(val.toString());
            }
        }
        return result;
    }

    public List<String> getStringsWithoutNull(String key) {
        List<String> result = new ArrayList<>();
        List<Object> list = fetchValues(key);
        for (Object val : list) {
            if (null != val) {
                result.add(val.toString());
            }
        }
        return result;
    }

    public BigDecimal getDecimal(int idx, String key) throws Exception {
        return getRow(idx).getDecimal(key);
    }

    public BigDecimal getDecimal(int idx, String key, double def) {
        return getDecimal(idx, key, new BigDecimal(def));
    }

    public BigDecimal getDecimal(int idx, String key, BigDecimal def) {
        try {
            BigDecimal val = getDecimal(idx, key);
            if (null == val) {
                return def;
            }
            return val;
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 抽取指定列生成新的DataSet<E> 新的DataSet只包括指定列的值与分页信息,不包含其他附加信息(如来源表)
     * @param regex 是否开启正则匹配
     * @param keys keys
     * @return DataSet
     */
    public DataSet<E> extract(boolean regex, String ... keys) {
        return extract(regex, BeanUtil.array2list(keys));
    }
    public DataSet<E> extract(boolean regex, List<String> keys) {
        DataSet<E> result = new DataSet();
        for(E row:rows) {
            E item = (E)row.extract(regex, keys);
            result.add(item);
        }
        result.navi = navi;
        return result;
    }

    public DataSet<E> extract(String ... keys) {
        return extract(false,keys);
    }

    /**
     * escape String
     *
     * @param index 索引
     * @param key   key
     * @return String
     * @throws Exception 异常 Exception
     */
    public String getEscapeString(int index, String key) throws Exception {
        return EscapeUtil.escape(getString(index, key)).toString();
    }

    public String getEscapeString(int index, String key, String def) {
        try {
            return getEscapeString(index, key);
        } catch (Exception e) {
            return EscapeUtil.escape(def).toString();
        }
    }

    public String getDoubleEscapeString(int index, String key) throws Exception {
        return EscapeUtil.escape2(getString(index, key));
    }

    public String getDoubleEscapeString(int index, String key, String def) {
        try {
            return getDoubleEscapeString(index, key);
        } catch (Exception e) {
            return EscapeUtil.escape2(def);
        }

    }

    public String getEscapeString(String key) throws Exception {
        return getEscapeString(0, key);
    }

    public String getDoubleEscapeString(String key) throws Exception {
        return getDoubleEscapeString(0, key);
    }

    /**
     * int
     *
     * @param index 索引
     * @param key   key
     * @return int
     * @throws Exception 异常 Exception
     */
    public int getInt(int index, String key) throws Exception {
        return getRow(index).getInt(key);
    }

    public int getInt(int index, String key, int def) {
        try {
            return getInt(index, key);
        } catch (Exception e) {
            return def;
        }
    }

    public int getInt(String key) throws Exception {
        return getInt(0, key);
    }

    public int getInt(String key, int def) {
        return getInt(0, key, def);
    }

    /**
     * double
     *
     * @param index 索引
     * @param key   key
     * @return double
     * @throws Exception 异常 Exception
     */
    public double getDouble(int index, String key) throws Exception {
        return getRow(index).getDouble(key);
    }

    public double getDouble(int index, String key, double def) {
        try {
            return getDouble(index, key);
        } catch (Exception e) {
            return def;
        }
    }

    public double getDouble(String key) throws Exception {
        return getDouble(0, key);
    }

    public double getDouble(String key, double def) {
        return getDouble(0, key, def);
    }

    /**
     * 在key列基础上 +value,如果原来没有key列则默认0并put到target
     * 如果target与key一致则覆盖原值
     * @param target 计算结果key
     * @param key key
     * @param value value
     * @return this
     */
    public DataSet<E> add(String target, String key, int value) {
        for(DataRow row:rows) {
            row.add(target, key, value);
        }
        return this;
    }

    public DataSet<E> add(String target, String key, double value) {
        for(DataRow row:rows) {
            row.add(target, key, value);
        }
        return this;
    }
    public DataSet<E> add(String target, String key, short value) {
        for(DataRow row:rows) {
            row.add(target, key, value);
        }
        return this;
    }
    public DataSet<E> add(String target, String key, float value) {
        for(DataRow row:rows) {
            row.add(target, key, value);
        }
        return this;
    }
    public DataSet<E> add(String target, String key, BigDecimal value) {
        for(DataRow row:rows) {
            row.add(target, key, value);
        }
        return this;
    }

    public DataSet<E> add(String key, int value) {
        return  add(key, key, value);
    }

    public DataSet<E> add(String key, double value) {
        return  add(key, key, value);
    }
    public DataSet<E> add(String key, short value) {
        return  add(key, key, value);
    }
    public DataSet<E> add(String key, float value) {
        return  add(key, key, value);
    }
    public DataSet<E> add(String key, BigDecimal value) {
        return  add(key, key, value);
    }

    public DataSet<E> subtract(String target, String key, int value) {
        for(DataRow row:rows) {
            row.subtract(target, key, value);
        }
        return this;
    }

    public DataSet<E> subtract(String target, String key, double value) {
        for(DataRow row:rows) {
            row.subtract(target, key, value);
        }
        return this;
    }
    public DataSet<E> subtract(String target, String key, short value) {
        for(DataRow row:rows) {
            row.subtract(target, key, value);
        }
        return this;
    }
    public DataSet<E> subtract(String target, String key, float value) {
        for(DataRow row:rows) {
            row.subtract(target, key, value);
        }
        return this;
    }
    public DataSet<E> subtract(String target, String key, BigDecimal value) {
        for(DataRow row:rows) {
            row.subtract(target, key, value);
        }
        return this;
    }

    public DataSet<E> subtract(String key, int value) {
        return  subtract(key, key, value);
    }

    public DataSet<E> subtract(String key, double value) {
        return  subtract(key, key, value);
    }
    public DataSet<E> subtract(String key, short value) {
        return  subtract(key, key, value);
    }
    public DataSet<E> subtract(String key, float value) {
        return  subtract(key, key, value);
    }
    public DataSet<E> subtract(String key, BigDecimal value) {
        return  subtract(key, key, value);
    }

    public DataSet<E> multiply(String target, String key, int value) {
        for(DataRow row:rows) {
            row.multiply(target, key, value);
        }
        return this;
    }

    public DataSet<E> multiply(String target, String key, double value) {
        for(DataRow row:rows) {
            row.multiply(target, key, value);
        }
        return this;
    }
    public DataSet<E> multiply(String target, String key, short value) {
        for(DataRow row:rows) {
            row.multiply(target, key, value);
        }
        return this;
    }
    public DataSet<E> multiply(String target, String key, float value) {
        for(DataRow row:rows) {
            row.multiply(target, key, value);
        }
        return this;
    }
    public DataSet<E> multiply(String target, String key, BigDecimal value) {
        for(DataRow row:rows) {
            row.multiply(target, key, value);
        }
        return this;
    }

    public DataSet<E> multiply(String key, int value) {
        return multiply(key,key,value);
    }

    public DataSet<E> multiply(String key, double value) {
        return multiply(key,key,value);
    }
    public DataSet<E> multiply(String key, short value) {
        return multiply(key,key,value);
    }
    public DataSet<E> multiply(String key, float value) {
        return multiply(key,key,value);
    }
    public DataSet<E> multiply(String key, BigDecimal value) {
        return multiply(key,key,value);
    }

    public DataSet<E> divide(String target, String key, int value) {
        for(DataRow row:rows) {
            row.divide(target, key, value);
        }
        return this;
    }

    public DataSet<E> divide(String target, String key, double value) {
        for(DataRow row:rows) {
            row.divide(target, key, value);
        }
        return this;
    }
    public DataSet<E> divide(String target, String key, short value) {
        for(DataRow row:rows) {
            row.divide(target, key, value);
        }
        return this;
    }
    public DataSet<E> divide(String target, String key, float value) {
        for(DataRow row:rows) {
            row.divide(target, key, value);
        }
        return this;
    }
    public DataSet<E> divide(String target, String key, BigDecimal value, int mode) {
        for(DataRow row:rows) {
            row.divide(target, key, value, mode);
        }
        return this;
    }

    public DataSet<E> divide(String key, int value) {
        return divide(key,key, value);
    }

    public DataSet<E> divide(String key, double value) {
        return divide(key,key, value);
    }
    public DataSet<E> divide(String key, short value) {
        return divide(key,key, value);
    }
    public DataSet<E> divide(String key, float value) {
        return divide(key,key, value);
    }
    public DataSet<E> divide(String key, BigDecimal value, int mode) {
        return divide(key,key, value, mode);
    }

    public DataSet<E> round(String target, String key, int scale, int mode) {
        for (DataRow row:rows) {
            row.round(target, key, scale, mode);
        }
        return this;
    }

    /**
     * 舍入
     * @param key 属性
     * @param scale 小数位
     * @param mode 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return DataSet
     */
    public DataSet<E> round(String key, int scale, int mode) {
        return round(key, key, scale, mode);
    }

    /**
     * 每页最少1行,最少分1页,最多分DataSet.size()页
     * 多余的从第1页开始追加
     * 5行分2页:共分成2页(3+2)
     * 5行分3页:共分成3页(2+2+1)
     * 10行分3页:共分成3页(4+3+3)
     * 10行分6页:共分成6页(2+2+2+2+1+1)
     * 5行分0页:共分成1页(5)
     * 2行分3页:共分成2页(1+1)
     *
     * DataSet拆分成size部分
     * @param page 拆成多少部分
     * @return list
     */
    public List<DataSet> split(int page) {
        List<DataSet> list = new ArrayList<>();
        int size = size();
        if(page <=0 ) {
            page = 1;
        }
        if(page > size) {
            page = size;
        }
        int vol = size / page;//每页多少行
        int dif = size - vol*page;
        int fr = 0;
        int to = 0;
        for(int i=0; i<page; i++) {
            to = fr + vol-1;
            if(dif > 0) {
                to ++;
                dif --;
            }
            if(to >= size) {
                to = size-1;
            }
            DataSet<E> set = cuts(fr, to);
            list.add(set);
            fr = to +1;
        }
        return list;
    }

    /**
     * 分页
     * @param vol 每页多少行
     * @return List
     */
    public List<DataSet> page(int vol) {
        List<DataSet> list = new ArrayList<>();
        if(vol <= 0) {
            vol = 1;
        }
        int size = size();
        int page = (size-1) / vol + 1;
        for(int i=0; i<page; i++) {
            int fr = i*vol;
            int to = (i+1)*vol-1;
            if(i == page-1) {
                to = size-1;
            }
            DataSet<E> set = cuts(fr, to);
            list.add(set);
        }
        return list;
    }

    public DataSet<E> string2object(){
        for(DataRow row:rows){
            row.string2object();
        }
        return this;
    }
    
    /**
     * rows 列表中的数据格式化成json格式   不同与toJSON
     * map.put("type","list");
     * map.put("result", result);
     * map.put("message", message);
     * map.put("rows", rows);
     * map.put("success", result);
     * map.put("navi", navi);
     */
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put("type","list");
        map.put("result", result);
        map.put("message", message);
        map.put("rows", rows);
        map.put("success", result);
        if(null != navi) {
            Map<String,Object> navi_ = new HashMap<>();
            navi_.put("page", navi.getCurPage());
            navi_.put("pages", navi.getTotalPage());
            navi_.put("rows", navi.getTotalRow());
            navi_.put("vol", navi.getPageRows());
            map.put("navi", navi_);
        }
        return BeanUtil.map2json(map);
    }

    /**
     * rows 列表中的数据格式化成json格式   不同与toString
     *
     * @return String
     */
    public String toJson() {
        return BeanUtil.object2json(this);
    }

    public String getJson() {
        return toJSON();
    }

    public String toJSON() {
        return toJson();
    }

    public String toJson(JsonInclude.Include include) {
        return BeanUtil.object2json(this,include);
    }

    public String getJson(JsonInclude.Include include) {
        return toJSON(include);
    }

    public String toJSON(JsonInclude.Include include) {
        return toJson(include);
    }

    /**
     * 根据指定列生成map
     *
     * @param key ID,{ID}_{NM}
     * @return Map
     */
    public Map<String, DataRow> toMap(String key) {
        Map<String, DataRow> maps = new HashMap<String, DataRow>();
        for (DataRow row : rows) {
            maps.put(row.getString(key), row);
        }
        return maps;
    }

    /**
     * 子类
     *
     * @param idx idx
     * @return Object
     */
  /*  public Object getChildren(int idx) {
        DataRow row = getRow(idx);
        if (null != row) {
            return row.getChildren();
        }
        return null;
    }
*/
   // public Object getChildren() {
    //    return getChildren(0);
   // }
/*
    public DataSet<E> setChildren(int idx, Object children) {
        DataRow row = getRow(idx);
        if (null != row) {
            row.setChildren(children);
        }
        return this;
    }*/
/*
    public DataSet<E> setChildren(Object children) {
        setChildren(0, children);
        return this;
    }*/

    /**
     * 父类
     *
     * @param idx idx
     * @return Object
     */
   /* Object getParent(int idx) {
        DataRow row = getRow(idx);
        if (null != row) {
            return row.getParent();
        }
        return null;
    }*/
/*
    public Object getParent() {
        return getParent(0);
    }

    public DataSet<E> setParent(int idx, Object parent) {
        DataRow row = getRow(idx);
        if (null != row) {
            row.setParent(parent);
        }
        return this;
    }*/
/*
    public DataSet<E> setParent(Object parent) {
        setParent(0, parent);
        return this;
    }*/

    /**
     * 转换成对象
     *
     * @param <T>   T
     * @param index 索引
     * @param clazz clazz
     * @param configs 属性对应关系  name:USER_NAME
     * @return T
     */
    public <T> T entity(int index, Class<T> clazz, String ... configs) {
        DataRow row = getRow(index);
        if (null != row) {
            return row.entity(clazz,configs);
        }
        return null;
    }

    /**
     * 转换成对象集合
     *
     * @param <T>   T
     * @param clazz clazz
     * @return List
     */
    public <T> List<T> entity(Class<T> clazz) {
        List<T> list = new ArrayList<T>();
        if (null != rows) {
            for (E row : rows) {
                list.add(row.entity(clazz));
            }
        }
        return list;
    }

    public <T> T entity(Class<T> clazz, int idx) {
        DataRow row = getRow(idx);
        if (null != row) {
            return row.entity(clazz);
        }
        return null;
    }
    public <T> EntitySet<T> entitys(Class<T> clazz) {
        return null;
    }
    public DataSet<E> setDest(String dest) {
        if (null == dest) {
            return this;
        }
        Catalog catalog = null;
        Schema schema = null;
        Table table = null;
        if (dest.contains(".") && !dest.contains(":")) {
            String[] tmps = dest.split("\\.");
            if(tmps.length == 2) {
                schema = new Schema(tmps[0]);
                table = new Table(tmps[1]);
                table.setSchema(schema);
            }else if(tmps.length == 3) {
                catalog = new Catalog(tmps[0]);
                schema = new Schema(tmps[1]);
                schema.setCatalog(catalog);
                table = new Table(tmps[2]);
                table.setSchema(schema);
                table.setCatalog(catalog);
            }
            setCatalog(catalog);
            setSchema(schema);
            setTable(table);
        }else{
            table = new Table(dest);
            setTable(table);
        }
        for (DataRow row : rows) {
            if (BasicUtil.isEmpty(row.getDest())) {
                row.setCatalog(catalog);
                row.setSchema(schema);
                row.setTable(table);
            }
        }
        return this;
    }

    /**
     * 合并
     * @param set DataSet
     * @param keys 根据keys去重
     * @return DataSet
     */
    public DataSet<E> union(DataSet<E> set, List<String> keys) {
        DataSet<E> result = new DataSet();
        if (null != rows) {
            int size = rows.size();
            for (int i = 0; i < size; i++) {
                result.add(rows.get(i));
            }
        }
        if (keys.isEmpty()) {
            keys.add(ConfigTable.DEFAULT_PRIMARY_KEY);
        }
        int size = set.size();
        for (int i = 0; i < size; i++) {
            E item = set.getRow(i);
            if (! result.contains(item, keys)) {
                result.add(item);
            }
        }
        return result;
    }

    public DataSet<E> union(DataSet<E> set, String... keys) {
        return union(set, BeanUtil.array2list(keys));
    }

    /**
     * 合并合并不去重
     *
     * @param set set
     * @return DataSet
     */
    public DataSet<E> unionAll(DataSet<E> set) {
        DataSet<E> result = new DataSet();
        if (null != rows) {
            int size = rows.size();
            for (int i = 0; i < size; i++) {
                result.add(rows.get(i));
            }
        }
        int size = set.size();
        for (int i = 0; i < size; i++) {
            E item = set.getRow(i);
            result.add(item);
        }
        return result;
    }

    /**
     * 是否包含这一行
     *
     * @param row  row
     * @param keys keys
     * @return boolean
     */
    public boolean contains(DataRow row, List<String> keys) {
        if (null == rows || rows.isEmpty() || null == row) {
            return false;
        }
        if (keys.isEmpty()) {
            keys.add(ConfigTable.DEFAULT_PRIMARY_KEY);
        }
        String params[] = packParam(row, keys);
        return exists(params);
    }

    public boolean contains(DataRow row, String... keys) {
        return contains(row, BeanUtil.array2list(keys));
    }

    public String[] packParam(DataRow row, String... keys) {
        return packParam(row, BeanUtil.array2list(keys));
    }

    /**
     * 根据数据与属性列表 封装kvs
     *  ["ID","1","CODE","A01"]
     * @param row 数据 DataRow
     * @param keys 属性 ID,CODE
     * @return kvs
     */
    public String[] packParam(DataRow row, List<String> keys) {
        if (null == keys || null == row) {
            return null;
        }
        String params[] = new String[keys.size() * 2];
        int idx = 0;
        for (String key : keys) {
            if (null == key) {
                continue;
            }
            String ks[] = BeanUtil.parseKeyValue(key);
            params[idx++] = ks[0];
            params[idx++] = row.getString(ks[1]);
        }
        return params;
    }
    public DataSet<E> setParent(String key, DataRow parent) {
        for(DataRow row:rows) {
            row.setParent(key, parent);
        }
        return this;
    }

    public boolean isAutoCheckElValue() {
        return autoCheckElValue;
    }
    public DataSet<E> setAutoCheckElValue(boolean autoCheckElValue) {
        for(DataRow row:rows) {
            row.setAutoCheckElValue(autoCheckElValue);
        }
        this.autoCheckElValue = autoCheckElValue;
        return this;
    }
    
    /**
     * 从items中按相应的key提取数据 存入
     * dispatch("children",items, "DEPT_CD")
     * dispatchs("children",items, "CD:BASE_CD")
     *
     * @param compare   匹配方式 默认=
     * @param field     默认"items"
     * @param unique    是否只分配一次(同一个条目不能分配到多个组中)
     * @param recursion 是否递归 所有子级以相同条件执行dispatchs
     * @param items     items默认this
     * @param keys     ID:DEPT_ID或ID
     * @return DataSet
     */
    public DataSet<E> dispatchs(Compare compare, String field, boolean unique, boolean recursion, DataSet<E> items, List<String> keys) {
        if (null == items) {
            return this;
        }
        if(keys.isEmpty()) {
            throw new RuntimeException("未指定对应关系");
        }
        if (BasicUtil.isEmpty(field)) {
            field = "items";
        }
        for (E row : rows) {
            if (null == row.get(field)) {
                String[] kvs = packParam(row, reverseKey(keys));
                DataSet<E> set = items.getRows(compare, kvs(kvs));
                //避免无限递归
                //引用自己
                int index = set.indexOf(row);
                if(index != -1){
                    set.remove(row);
                    E copy = (E)row.clone();
                    if(set.isEmpty()){
                        set.add(copy);
                    }else{
                        set.addRow(index, copy);
                    }
                }
                //检测相互引用
                DataSet<DataRow> parents = row.getAllParent(field);

                int size = set.size();
                for(DataRow parent:parents) {
                    for(int i=0; i<size; i++) {
                        DataRow chk = set.getRow(i);
                        if(parent == chk) {
                            E copy = (E)new DataRow();
                            copy.copy(chk);
                            copy.put(false, field, new DataSet());
                            set.set(i, copy);
                            break;
                        }
                    }
                }

                if (recursion) {
                    set.dispatchs(compare, field, unique, recursion, items, keys);
                }
                if(unique) {
                    set.skip(true);
                }
                row.put(false, field, set);
                set.setParent(field, row);
            }
        }
        items.skip(false);
        return this;
    }
    public DataSet<E> dispatchs(Compare compare, String field, boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatchs(compare, field, unique, recursion, items, BeanUtil.array2list(keys));
    }

    public DataSet<E> dispatchs(Compare compare, boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatchs(compare, "items", unique, recursion, items, keys);
    }

    public DataSet<E> dispatchs(Compare compare, String field, DataSet<E> items, String... keys) {
        return dispatchs(compare, field,false, false, items, keys);
    }

    public DataSet<E> dispatchs(Compare compare, DataSet<E> items, String... keys) {
        return dispatchs(compare, "items", items, keys);
    }
    public DataSet<E> dispatchs(Compare compare, boolean unique, boolean recursion, String... keys) {
        return dispatchs(compare, "items", unique, recursion, this, keys);
    }

    public DataSet<E> dispatchs(Compare compare, String field, boolean unique, boolean recursion, String... keys) {
        return dispatchs(compare, field, unique, recursion, this, keys);
    }

    /**
     * 没有匹配成功的情况下，field依然会保留，如果需要清空可以调用set.removeEmptyRow(field)
     * @param compare 对比方式
     * @param field file
     * @param unique 是否唯一 items中同一个条目只能匹配成功一次
     * @param recursion 是否递归
     * @param items items
     * @param keys 匹配条件
     * @return this
     */
    public DataSet<E> dispatch(Compare compare, String field, boolean unique, boolean recursion, DataSet<E> items, List<String> keys) {
        if (null == items) {
            return this;
        }
        if(keys.isEmpty()) {
            throw new RuntimeException("未指定对应关系");
        }
        if (BasicUtil.isEmpty(field)) {
            field = "items";
        }
        for (DataRow row : rows) {
            if (null == row.get(field)) {
                String[] params = packParam(row, reverseKey(keys));
                DataRow result = items.getRow(compare, params);
                if(null != result && !result.isEmpty()) {
                    if (unique) {
                        result.skip = true;
                    }
                }
                //result为空时 需要保留 field
                row.put(field, result);
            }
        }
        items.skip(false);
        return this;
    }

    public DataSet<E> dispatch(Compare compare, String field, boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatch(compare, field, unique, recursion, items, BeanUtil.array2list(keys));
    }
    public DataSet<E> dispatch(Compare compare, String field, DataSet<E> items, String... keys) {
        return dispatch(compare, field, false, false, items, keys);
    }
    public DataSet<E> dispatch(Compare compare, DataSet<E> items, String... keys) {
        return dispatch(compare, "ITEM", false, false, items, BeanUtil.array2list(keys));
    }
    public DataSet<E> dispatch(Compare compare, boolean unique, boolean recursion, String... keys) {
        return dispatch(compare, "ITEM", unique, recursion, this, keys);
    }
    public DataSet<E> dispatch(Compare compare, String field, boolean unique, boolean recursion, String... keys) {
        return dispatch(compare, field, unique, recursion, this, keys);
    }
    public DataSet<E> dispatchs(String field, boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatchs(Compare.EQUAL, field, unique, recursion, items, BeanUtil.array2list(keys));
    }

    public DataSet<E> dispatchs(boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatchs(Compare.EQUAL, "items", unique, recursion, items, keys);
    }
    public DataSet<E> dispatchs(String field, DataSet<E> items, String... keys) {
        return dispatchs(Compare.EQUAL, field,false, false, items, keys);
    }
    public DataSet<E> dispatchs(DataSet<E> items, String... keys) {
        return dispatchs(Compare.EQUAL, "items", items, keys);
    }
    public DataSet<E> dispatchs(boolean unique, boolean recursion, String... keys) {
        return dispatchs(Compare.EQUAL, "items", unique, recursion, this, keys);
    }

    public DataSet<E> dispatchs(String field, boolean unique, boolean recursion, String... keys) {
        return dispatchs(Compare.EQUAL, field, unique, recursion, this, keys);
    }
    public DataSet<E> dispatch(String field, boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatch(Compare.EQUAL, field, unique, recursion, items, BeanUtil.array2list(keys));
    }
    public DataSet<E> dispatch(String field, DataSet<E> items, String... keys) {
        return dispatch(Compare.EQUAL, field, false, false, items, keys);
    }
    public DataSet<E> dispatch(DataSet<E> items, String... keys) {
        return dispatch(Compare.EQUAL, "ITEM", false, false, items, BeanUtil.array2list(keys));
    }
    public DataSet<E> dispatch(boolean unique, boolean recursion, String... keys) {
        return dispatch(Compare.EQUAL, "ITEM", unique, recursion, this, keys);
    }
    public DataSet<E> dispatch(String field, boolean unique, boolean recursion, String... keys) {
        return dispatch(Compare.EQUAL, field, unique, recursion, this, keys);
    }

    /**
     * 直接调用dispatchs
     * @param field     默认"items"
     * @param unique    是否只分配一次(同一个条目不能分配到多个组中)
     * @param recursion 是否递归
     * @param items     items
     * @param keys       ID:DEPT_ID或ID
     * @return DataSet
     */
    @Deprecated
    public DataSet<E> dispatchItems(String field, boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatchs(field, unique, recursion, items, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItems(boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatchs( unique, recursion, items, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItems(String field, DataSet<E> items, String... keys) {
        return dispatchs(field, items, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItems(DataSet<E> items, String... keys) {
        return dispatchs(items, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItems(boolean unique, boolean recursion, String... keys) {
        return dispatchs( unique, recursion, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItems(String field, boolean unique, boolean recursion, String... keys) {
        return dispatchs(field, unique, recursion, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItem(String field, boolean unique, boolean recursion, DataSet<E> items, String... keys) {
        return dispatch(field, unique, recursion, items, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItem(String field, DataSet<E> items, String... keys) {
        return dispatch(field, items, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItem(DataSet<E> items, String... keys) {
        return dispatch(items, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItem(boolean unique, boolean recursion, String... keys) {
        return dispatch(unique, recursion, keys);
    }

    @Deprecated
    public DataSet<E> dispatchItem(String field, boolean unique, boolean recursion, String... keys) {
        return dispatch(field, unique, recursion, keys);
    }

    /**
     * 根据keys列建立关联,并将关联出来的结果拼接到集合的条目上,如果有重复则覆盖条目
     *
     * @param items 被查询的集合
     * @param keys  关联条件列
     * @return DataSet
     */
    public DataSet<E> join(DataSet<E> items, List<String> keys) {
        if (null == items || keys.isEmpty()) {
            return this;
        }
        for (DataRow row : rows) {
            String[] params = packParam(row, reverseKey(keys));
            DataRow result = items.getRow(params);
            if (null != result && !result.isEmpty()) {
                row.copy(result, result.keys());
            }
        }
        return this;
    }

    /**
     * 外键关联<br/>
     * 如果要合并条目(如把对应user的全部数据合并到当前集合条目)应该调用join<br/>
     * 如果要把set中的条目作为一个整体放到当前集合条目中应该调用dispatch(如果需要一对多关系调用dispatchs)
     * @param fk 关联列 如 user_id
     * @param set 关联数据集 如 users
     * @param pk 关联数据集.关联列 如 id
     * @param intent 关联数据集.关联结果列 如 name
     * @param alias 关联结果存储列  如 user_name
     * @return this
     */
    public DataSet<E> foreign(String fk, DataSet<E> set, String pk, String intent, String alias){
        for(DataRow row:rows){
            DataRow data = set.getRow(pk, row.getString(fk));
            if(null != data){
                row.put(alias, data.get(intent));
            }
        }
        return this;
    }

    /**
     * 外键关联(码值)
     * @param set users
     * @param foreignKey USER_ID this.get("USER_ID") == user.get("ID")
     * @param foreignText USER_NAME this.put("USER_NAME", user.get("NAME"))
     * @param primaryKey ID this.get("USER_ID") == user.get("ID")
     * @param primaryText NAME user.get("NAME")
     * @param append USER this.put("USER", user)
     * @return this
     */
    public DataSet<E> foreign(DataSet<DataRow> set, String foreignKey, String foreignText, String primaryKey, String primaryText, String append){
        for(DataRow row:rows){
            row.foreign(set, foreignKey, foreignText, primaryKey, primaryText, append);
        }
        return this;
    }
    public DataSet<E> foreign(DataSet<DataRow> set, String foreignKey, String foreignText, String primaryKey, String primaryText){
        return foreign(set, foreignKey, foreignText, primaryKey, primaryText, null);
    }

    /**
     * 外键关联<br/>
     * 如果要合并条目(如把对应user的全部数据合并到当前集合条目)应该调用join<br/>
     * 如果要把set中的条目作为一个整体放到当前集合条目中应该调用dispatch(如果需要一对多关系调用dispatchs)
     * @param fk 关联列 如 user_id
     * @param set 关联数据集 如 users
     * @param pk 关联数据集.关联列 如 id
     * @param intents 关联数据集.关联结果列 如 name:user_name
     * @return this
     */
    public DataSet<E> foreign(String fk, DataSet<E> set, String pk, Map<String, String> intents){
        for(DataRow row:rows){
            DataRow data = set.getRow(pk, row.getString(fk));
            if(null != data){
                for(String intent:intents.keySet()) {
                    row.put(intents.get(intent), data.get(intent));
                }
            }
        }
        return this;
    }
    
    /**
     * 全部条目keys合集
     * @return List
     */
    public List<String> keys() {
        List<String> keys = new ArrayList<>();
        Map<String,String> map = new LinkedHashMap<>();
        for(DataRow row:rows) {
            for(String key:row.keySet()) {
                map.put(key, key);
            }
        }
        keys.addAll(map.keySet());
        if(keys.isEmpty() && null != metadatas) {
            for(Column column:metadatas.values()) {
                keys.add(column.getName());
            }
        }
        return keys;
    }
    public DataSet<E> join(DataSet<E> items, String... keys) {
        return join(items, BeanUtil.array2list(keys));
    }

    @Override
    public DataSet<E> toLowerKey(boolean recursion, String... keys) {
        for (DataRow row : rows) {
            row.toLowerKey(recursion, keys);
        }
        return this;
    }

    @Override
    public DataSet<E> toUpperKey(boolean recursion, String... keys) {
        for (DataRow row : rows) {
            row.toUpperKey(recursion, keys);
        }
        return this;
    }
    public DataSet<E> toLowerValue() {
        for (DataRow row : rows) {
            row.toLowerValue();
        }
        return this;
    }

    public DataSet<E> toUpperValue() {
        for (DataRow row : rows) {
            row.toUpperValue();
        }
        return this;
    }

    /**
     * 设置是否更新null列
     * @param updateNullColumn updateNullColumn
     * @return DataRow
     */
    public DataSet<E> setUpdateNullColumn(boolean updateNullColumn) {
        for(DataRow row:rows) {
            row.setUpdateNullColumn(updateNullColumn);
        }
        return this;
    }

    /**
     * 设置是否更新null列
     * @param updateEmptyColumn updateEmptyColumn
     * @return DataRow
     */
    public DataSet<E> setUpdateEmptyColumn(boolean updateEmptyColumn) {
        for(DataRow row:rows) {
            row.setUpdateEmptyColumn(updateEmptyColumn);
        }
        return this;
    }

    /**
     * 按keys分组
     *
     * @param keys 分组依据列
     * @param extract 分组结果是否只保留keys列
     * @return DataSet
     */
    public DataSet<E> group(boolean extract, String field, Compare compare, String... keys) {
        DataSet<E> groups = distinct(extract, keys);
        groups.dispatchs(compare, field, true, false, this, keys);
        return groups;
    }

    public DataSet<E> group(String field, Compare compare, String... keys) {
        return group(true, field, compare, keys);
    }
    public DataSet<E> group(boolean extract, String... keys) {
        return group(extract, "items", Compare.EQUAL, keys);
    }

    public DataSet<E> group(String... keys) {
        return group(true, keys);
    }
    
    /**
     * 分组聚合
     * @param extract 分组结果是否只保留keys列
     * @param items 是否保留条目 如果为空则不保留 否则保留会在每个分组中添加items属性用来保存当前分组中的条件
     * @param alias 聚合结果保存属性 如果不指定则以 factor_agg命名 如 age_avg
     * @param field 计算因子属性 取条目中的factor属性的值参与计算
     * @param agg 聚合公式 参考Aggregation枚举
     * @param groups 分组条件 指定属性相同的条目合成一组
     * @param scale 精度(小数位)
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return DataSet
     */
    public DataSet<E> group(boolean extract, String items, String alias, String field, Aggregation agg, int scale, int round, String ... groups) {
        String items_key = "items";
        if(BasicUtil.isNotEmpty(items)) {
            items_key = items;
        }
        DataSet<E>  gps = group(extract, items_key, Compare.EQUAL, groups);
        for(E group:gps) {
            group.put(alias, group.getItems().agg(agg, scale, round, field));
            if(BasicUtil.isEmpty(items)) {
                group.remove(items_key);
            }
        }
        return gps;
    }

    public DataSet<E> group(String items, String alias, String field, Aggregation agg, int scale, int round, String ... groups) {
        return group(true, items, alias, field, agg, scale, round, groups);
    }
    
    /**
     * 同一规则分组后,多次聚合
     * @param items 是否保留条目 如果为空则不保留 否则保留会在每个分组中添加items属性用来保存当前分组中的条件
     * @param aggs 聚合规则
     * @param groups 分组条件 指定属性相同的条目合成一组
     * @return DataSet
     */
    public DataSet<E> group(boolean extract, String items, List<AggregationConfig> aggs, String ... groups) {
        String items_key = "items";
        if(BasicUtil.isNotEmpty(items)) {
            items_key = items;
        }
        DataSet<E>  gps = group(extract, items_key, Compare.EQUAL, groups);
        for(E group:gps) {
            for(AggregationConfig config:aggs) {
                group.put(config.getAlias(), group.getItems().agg(config.getAggregation(), config.getScale(), config.getRound(), config.getField()));
            }
            if(BasicUtil.isEmpty(items)) {
                group.remove(items_key);
            }
        }
        return gps;
    }
    public DataSet<E> group(String items, List<AggregationConfig> aggs, String ... groups) {
        return group(true, items, aggs, groups);
    }

    public DataSet<E> group(boolean extract, String field, Aggregation agg, String ... groups) {
        String alias = agg.code();
        if(BasicUtil.isNotEmpty(field)) {
            alias = field + "_" + alias;
        }
        return group(extract, null, alias, field, agg, 0, 0, groups);
    }

    public DataSet<E> group(String field, Aggregation agg, String ... groups) {
        return group(true, field, agg, groups);
    }
    public DataSet<E> group(boolean extract, Aggregation agg, String ... fields) {
        return group(extract, null, agg.code(), null, agg, 0, 0, fields);
    }
    public DataSet<E> group(Aggregation agg, String ... fields) {
        return group(true, agg, fields);
    }
    public Object agg(String type, String field) {
        Aggregation agg = Aggregation.valueOf(field);
        return agg(agg, field);
    }

    public Object agg(Aggregation agg, String field) {
        return agg(agg, 2, BigDecimal.ROUND_HALF_UP, field);
    }

    /**
     * 聚合计算
     * @param agg 公式
     * @param field 计算因子属性
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return Object
     */
    public Object agg(Aggregation agg, int scale, int round, String field) {
        Object result = null;
        switch (agg) {
            case COUNT:
                result = count(false, field);
                break;
            case SUM:
                result = sum(field);
                break;
            case AVG:
                result = avg(false, scale, round, field);
                break;
            case AVGA:
                result = avg(true, scale, round, field);
                break;
            case MEDIAN:
                result = median(field);
                break;
            case MAX:
                result = max(field);
                break;
            case MAX_DECIMAL:
                result = maxDecimal(field);
                break;
            case MAX_DOUBLE:
                result = maxDouble(field);
                break;
            case MAX_FLOAT:
                result = maxFloat(field);
                break;
            case MAX_INT:
                result = maxInt(field);
                break;
            case MIN:
                result = min(field);
                break;
            case MIN_DECIMAL:
                result = minDecimal(field);
                break;
            case MIN_DOUBLE:
                result = minDouble(field);
                break;
            case MIN_FLOAT:
                result = minFloat(field);
                break;
            case MIN_INT:
                result = minInt(field);
                break;
            case STDEV:
                result = stdev(scale, round, field);
                break;
            case STDEVP:
                result = stdevp(scale, round, field);
                break;
            case STDEVA:
                result = stdeva(scale, round, field);
                break;
            case STDEVPA:
                result = stdevpa(scale, round, field);
                break;
            case VAR:
                result = var(scale, round, field);
                break;
            case VARA:
                result = vara(scale, round, field);
                break;
            case VARP:
                result = varp(scale, round, field);
                break;
            case VARPA:
                result = varpa(scale, round, field);
                break;
        }
        return result;
    }

    /**
     * 抽样标准差
     * 抽样标准差σ=sqrt(s^2)，即标准差=方差的平方根
     * @param field 取值属性
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return 标准差
     */
    public BigDecimal stdev(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.stdev(values, scale, round);
    }
    public BigDecimal stdeva(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.stdeva(values, scale, round);
    }

    /**
     * 总体标准差
     * 总体标准差σ=sqrt(s^2)，即标准差=方差的平方根
     * @param field 取值属性
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return 标准差
     */
    public BigDecimal stdevp(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.stdevp(values, scale, round);
    }
    public BigDecimal stdevpa(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.stdevpa(values, scale, round);
    }

    /**
     * 抽样方差
     * s^2=[（x1-x）^2+（x2-x）^2+......（xn-x）^2]/(n-1)（x为平均数）
     * @param field 取值属性
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return 标准差
     */
    public BigDecimal var(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.var(values, scale, round);
    }
    public BigDecimal vara(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.vara(values, scale, round);
    }

    /**
     * 总体方差
     * s^2=[（x1-x）^2+（x2-x）^2+......（xn-x）^2]/n（x为平均数）
     * @param field 取值属性
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1,即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_UP 相同 如果为负,则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正,则舍入行为与 ROUND_DOWN 相同 如果为负,则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数,则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数,则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2,因为5前面的1是奇数;1.25 转成 1.2,因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return 标准差
     */
    public BigDecimal varp(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.varp(values, scale, round);
    }

    public BigDecimal varpa(int scale, int round, String field) {
        List<BigDecimal> values = getDecimals(field, null);
        return NumberUtil.varpa(values, scale, round);
    }
    public DataSet<E> or(DataSet<E> set, String... keys) {
        return this.union(set, keys);
    }

    /**
     * 多个集合的交集
     *
     * @param distinct 是否根据keys抽取不重复的集合
     * @param sets     集合
     * @param fields     判断依据
     * @return DataSet
     */
    public static <T extends DataRow> DataSet<T> intersection(boolean distinct, List<DataSet<T>> sets, String... fields) {
        DataSet<T> result = null;
        if (null != sets && !sets.isEmpty()) {
            for (DataSet<T> set : sets) {
                if (null == result) {
                    result = set;
                } else {
                    result = result.intersection(distinct, set, fields);
                }
            }
        }
        if (null == result) {
            result = new DataSet();
        }
        return result;
    }

    public static <T extends DataRow> DataSet<T> intersection(List<DataSet<T>> sets, String... keys) {
        return intersection(false, sets, keys);
    }

    /**
     * 交集
     *
     * @param distinct 是否根据keys抽取不重复的集合(根据keys去重)
     * @param set      set
     * @param keys     根据keys列比较是否相等,如果列名不一致"ID:USER_ID",ID表示当前DataSet的列,USER_ID表示参数中DataSet的列
     * @return DataSet
     */
    public DataSet<E> intersection(boolean distinct, DataSet<E> set, String... keys) {
        DataSet<E> result = new DataSet();
        if (null == set) {
            return result;
        }
        for (E row : rows) {
            String[] kv = reverseKey(keys);
            if (set.contains(row, kv)) {// 符合交集
                if(!result.contains(row, kv)) {//result中没有
                    result.add((E) row.clone());
                }else {
                    if(!distinct) {//result中有但不要求distinct
                        result.add((E) row.clone());
                    }
                }
            }
        }
        return result;
    }

    public DataSet<E> intersection(DataSet<E> set, String... keys) {
        return intersection(false, set, keys);
    }

    public DataSet<E> and(boolean distinct, DataSet<E> set, String... keys) {
        return intersection(distinct, set, keys);
    }

    public DataSet<E> and(DataSet<E> set, String... keys) {
        return intersection(false, set, keys);
    }

    /**
     * 补集
     * 在this中,但不在set中
     * this作为超集 set作为子集
     *
     * @param distinct 是否根据keys抽取不重复的集合
     * @param set      set
     * @param keys     keys
     * @return DataSet
     */
    public DataSet<E> complement(boolean distinct, DataSet<E> set, String... keys) {
        DataSet<E> result = new DataSet();
        for (DataRow row : rows) {
            String[] kv = reverseKey(keys);
            if (null == set || !set.contains(row, kv)) {
                if (!distinct || !result.contains(row, kv)) {
                    result.add((E) row.clone());
                }
            }
        }
        return result;
    }

    public DataSet<E> complement(DataSet<E> set, String... keys) {
        return complement(false, set, keys);
    }

    /**
     * 差集
     * 从当前集合中删除set中存在的row,生成新的DataSet并不修改当前对象
     * this中有 set中没有的
     *
     * @param distinct 是否根据keys抽取不重复的集合
     * @param set      set
     * @param keys     CD,"CD:WORK_CD"
     * @return DataSet
     */
    public DataSet<E> difference(boolean distinct, DataSet<E> set, String... keys) {
        DataSet<E> result = new DataSet();
        for (DataRow row : rows) {
            String[] kv = reverseKey(keys);
            if (null == set || !set.contains(row, kv)) {
                if (!distinct || !result.contains(row, kv)) {
                    result.add((E) row.clone());
                }
            }
        }
        return result;
    }

    public DataSet<E> difference(DataSet<E> set, String... keys) {
        return difference(false, set, keys);
    }

    /**
     * 颠倒kv-vk
     *
     * @param keys kv
     * @return String[]
     */
    private String[] reverseKey(String[] keys) {
        return reverseKey(BeanUtil.array2list(keys));
    }
    private String[] reverseKey(List<String> keys) {
        if (null == keys) {
            return new String[0];
        }
        int size = keys.size();
        String result[] = new String[size];
        for (int i = 0; i < size; i++) {
            String key = keys.get(i);
            if (BasicUtil.isNotEmpty(key) && key.contains(":")) {
                String ks[] = BeanUtil.parseKeyValue(key);
                key = ks[1] + ":" + ks[0];
            }
            result[i] = key;
        }
        return result;
    }

    /**
     * 清除指定列全为空的行,如果不指定keys,则清除所有列都为空的行
     *
     * @param keys keys
     * @return DataSet
     */
    public DataSet<E> removeEmptyRow(String... keys) {
        int size = this.size();
        for (int i = size - 1; i >= 0; i--) {
            DataRow row = getRow(i);
            if (null == keys || keys.length == 0) {
                if (row.isEmpty()) {
                    this.remove(row);
                }
            } else {
                boolean isEmpty = true;
                for (String key : keys) {
                    if (row.isNotEmpty(key)) {
                        isEmpty = false;
                        break;
                    }
                }
                if (isEmpty) {
                    this.remove(row);
                }
            }
        }
        return this;
    }

    /**
     * 修改key
     * @param key key
     * @param target 修改后key
     * @param remove 修改后是否把来的key删除
     * @return this
     */
    public DataSet<E> changeKey(String key, String target, boolean remove) {
        if(null == key || null == target) {
            return this;
        }
        if(key.equals(target)) {
            return this;
        }
        for(DataRow row:rows) {
            row.changeKey(key, target, remove);
        }
        return this;
    }
    public DataSet<E> changeKey(String key, String target) {
        return changeKey(key, target, true);
    }

    /**
     * 删除rows中的columns列
     *
     * @param columns 检测的列,如果不输入则检测所有列
     * @return DataSet
     */
    public DataSet<E> removeColumn(String... columns) {
        if (null != columns) {
            for (String column : columns) {
                for (DataRow row : rows) {
                    row.remove(column);
                }
            }
        }
        return this;
    }

    /**
     * 删除rows中值为空(null|'')的列
     *
     * @param columns 检测的列,如果不输入则检测所有列
     * @return DataSet
     */
    public DataSet<E> removeEmptyColumn(String... columns) {
        for (DataRow row : rows) {
            row.removeEmpty(columns);
        }
        return this;
    }


    public DataSet<E> omit(int left, int right, String ... columns) {
        return omit("*", left, right, columns);
    }

    /**
     *
     * @param columns 需要执行的列，如果不指定则执行全部列
     * @param vol 每个段最大长度,超出 vol 的拆成多段(vol大于1时有效)
     * @param left 每段左侧保留原文长度
     * @param right 每段右侧保留原文长度
     * @param ellipsis 省略符号
     * @return DataSet
     */
    public DataSet<E> omit(String ellipsis, int vol, int left, int right, String ... columns) {
        for(DataRow row:rows){
            row.omit(ellipsis, vol, left, right, columns);
        }
        return this;
    }
    public DataSet<E> omit(int vol, int left, int right, String ... columns) {
        return omit("*", vol, left, right, columns);
    }
    public DataSet<E> omit(String ellipsis, int left, int right, String ... columns) {
        return omit("*", left, right, columns);
    }
    public boolean equals(DataRow row, String ... columns) {
        if(null == row || null == columns || columns.length == 0) {
            return false;
        }
        for(String column:columns) {
            try {
                String v1 = getString(column);
                String v2 = row.getString(column);
                if(!BasicUtil.equals(v1, v2)) {
                    return false;
                }
            }catch (Exception ignore){
                return false;
            }
        }
        return true;
    }

    /**
     * NULL &gt; ""
     *
     * @return DataSet
     */
    public DataSet<E> nvl() {
        for (DataRow row : rows) {
            row.nvl();
        }
        return this;
    }
    public E add(KEY_CASE cs) {
        E row = (E)new DataRow(cs);
        add(row);
        return row;
    }
    public E add() {
        E row = (E)new DataRow(keyCase);
        add(row);
        return row;
    }
    /* ********************************************** 实现接口 *********************************************************** */
    public boolean add(E e) {
        if(null != e && null == e.getContainer()) {
            e.setContainer(this);
        }
        return rows.add(e);
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    public boolean addAll(Collection c) {
        return rows.addAll(c);
    }

    public void clear() {
        rows.clear();
    }

    public boolean contains(Object o) {
        return rows.contains(o);
    }

    public boolean containsAll(Collection<?> c) {
        return rows.containsAll(c);
    }

    public Iterator<E> iterator() {
        return rows.iterator();
    }

    public boolean remove(Object o) {
        boolean result = false;
        int size = rows.size();
        for(int i=size-1; i>=0; i--) {
            DataRow item = rows.get(i);
            if(item == o) {
                rows.remove(item);
                result = true;
            }
        }
        return result;
    }

    public boolean removeAll(Collection<?> c) {
        return rows.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return rows.retainAll(c);
    }

    public Object[] toArray() {
        return rows.toArray();
    }

    @SuppressWarnings("unchecked")
    public Object[] toArray(Object[] a) {
        return rows.toArray(a);
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public String getCatalogName() {
        if(null != catalog) {
            return catalog.getName();
        }
        return null;
    }

    public DataSet<E> setCatalog(Catalog catalog) {
        this.catalog = catalog;
        return this;
    }
    public DataSet<E> setCatalog(String catalog) {
        if(BasicUtil.isNotEmpty(catalog)) {
            this.catalog = new Catalog(catalog);
        }else{
            this.catalog = null;
        }
        return this;
    }

    public Schema getSchema() {
        return schema;
    }

    public String getSchemaName() {
        if(null != schema) {
            return schema.getName();
        }
        return null;
    }

    public DataSet<E> setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }
    public DataSet<E> setSchema(String schema) {
        if(BasicUtil.isNotEmpty(schema)) {
            this.schema = new Schema(schema);
        }else{
            this.schema = null;
        }
        return this;
    }

    public Table getTable(boolean checkItem) {
        Table table = null;
        if(null != tables && !tables.isEmpty()) {
            table = tables.values().iterator().next();
        }
        if(null == table && checkItem) {
            if(!rows.isEmpty()) {
                return rows.get(0).getTable(false);
            }
        }
        return table;
    }
    public Table getTable() {
        return getTable(true);
    }
    public String getTableName() {
        Table tab = getTable();
        if(null != tab) {
            return tab.getName();
        }
        return null;
    }
    public String getTableFullName() {
        Table table = getTable();
        if(null != table) {
            return table.getFullName();
        }
        return null;
    }
    public LinkedHashMap<String, Table> getTables(boolean checkItem) {
        if(null != tables && !tables.isEmpty()) {
            return tables;
        }else if(checkItem) {
            if(!rows.isEmpty()) {
                return rows.get(0).getTables(false);
            }
        }
        return tables;
    }
    public DataSet<E> setTables(List<Table> tables) {
        this.tables = new LinkedHashMap<>();
        if(null != tables) {
            for(Table table:tables) {
                addTable(table);
            }
        }
        return this;
    }
    public DataSet<E> setTables(LinkedHashMap<String, Table> tables) {
        this.tables = tables;
        return this;
    }
    public DataSet<E> setTable(Table table) {
        tables = new LinkedHashMap<>();
        addTable(table);
        return this;
    }
    public DataSet<E> addTable(Table table) {
        if(null == tables) {
            tables = new LinkedHashMap<>();
        }
        if(null != table) {
            String name = table.getName();
            if(null != name) {
                tables.put(name.toUpperCase(), table);
            }
        }
        return this;
    }
    public DataSet<E> setTable(String table) {
        if(null != table) {
            if (table.contains(".")) {
                String[] tbs = table.split("\\.");
                if (tbs.length == 2) {
                    setTable(new Table(tbs[1]));
                    this.schema = new Schema(tbs[0]);
                } else if (tbs.length == 3) {
                    setTable(new Table(tbs[2]));
                    this.schema = new Schema(tbs[1]);
                    this.catalog = new Catalog(tbs[0]);
                }
            } else {
                setTable(new Table(table));
            }
        }else{
            this.tables = new LinkedHashMap<>();
        }
        return this;
    }

    /**
     * 验证是否过期
     * 根据当前时间与创建时间对比
     * 过期返回 true
     *
     * @param millisecond 过期时间(毫秒) millisecond	过期时间(毫秒)
     * @return boolean
     */
    public boolean isExpire(int millisecond) {
        if (System.currentTimeMillis() - createTime > millisecond) {
            return true;
        }
        return false;
    }

    public boolean isExpire(long millisecond) {
        if (System.currentTimeMillis() - createTime > millisecond) {
            return true;
        }
        return false;
    }

    public boolean isExpire() {
        if (getExpires() == -1) {
            return false;
        }
        if (System.currentTimeMillis() - createTime > getExpires()) {
            return true;
        }
        return false;
    }

    public long getCreateTime() {
        return createTime;
    }

    public List<E> getRows() {
        return rows;
    }

    /************************** getter setter ***************************************/

    /**
     * 过期时间(毫秒)
     *
     * @return long
     */
    public long getExpires() {
        return expires;
    }

    public DataSet<E> setExpires(long millisecond) {
        this.expires = millisecond;
        return this;
    }

    public DataSet<E> setExpires(int millisecond) {
        this.expires = millisecond;
        return this;
    }

    public boolean isResult() {
        return result;
    }

    public boolean isSuccess() {
        return result;
    }

    public DataSet<E> setResult(boolean result) {
        this.result = result;
        return this;
    }

    public Exception getException() {
        return exception;
    }

    public DataSet<E> setException(Exception exception) {
        this.exception = exception;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public DataSet<E> setMessage(String message) {
        this.message = message;
        return this;
    }

    public PageNavi getNavi() {
        return navi;
    }

    public DataSet<E> setNavi(PageNavi navi) {
        this.navi = navi;
        if(null != navi) {
            navi.setDataSize(this.size());
        }
        return this;
    }

    public DataSet<E>  setRows(List<E> rows) {
        this.rows = rows;
        return this;
    }

    public String getDest() {
        String dest = null;
        String catalogName = getCatalogName();
        String schemaName = getSchemaName();
        String tableName = getTableName();
        if(BasicUtil.isNotEmpty(catalogName)) {
            dest = catalogName;
        }
        if(BasicUtil.isNotEmpty(schemaName)) {
            if(null == dest) {
                dest = schemaName;
            }else{
                dest += "." + schemaName;
            }
        }
        if(BasicUtil.isNotEmpty(tableName)) {
            if(null == dest) {
                dest = tableName;
            }else{
                dest += "." + tableName;
            }
        }
        return dest;
    }

    public DataSet<E> order(String... keys) {
        return asc(keys);
    }

    public DataSet<E> put(String key, Object value, boolean pk, boolean override) {
        for (DataRow row : rows) {
            row.put(key, value, pk, override);
        }
        return this;
    }
    public DataSet<E> putWithoutNull(String key, Object value) {
        if(null != value) {
            put(key, value);
        }
        return this;
    }
    public DataSet<E> putWithoutEmpty(String key, Object value) {
        if(BasicUtil.isNotEmpty(value)) {
            put(key, value);
        }
        return this;
    }
    public DataSet<E> putIfNull(String key, Object value) {
        for (DataRow row : rows) {
            row.putIfNull(key, value);
        }
        return this;
    }
    public DataSet<E> putIfEmpty(String key, Object value) {
        for (DataRow row : rows) {
            row.putIfEmpty(key, value);
        }
        return this;
    }
    public DataSet<E> put(String key, Object value, boolean pk) {
        for (DataRow row : rows) {
            row.put(key, value, pk);
        }
        return this;
    }
    public DataSet<E> put(String key, Object value) {
        for(DataRow row:rows) {
            row.put(key, value);
        }
        return this;
    }

    /**
     * 通过origin派生新列<br/>
     * derive("ADDRESS","${PROVINCE_NAME}-%{CITY_NAME}"),执行完成后每个条目上会添加一个新列ADDRESS
     * @param key 新列名
     * @param origin 原列名,以${列名1}${列名2}格式提供，${}之外的原样输出
     * @return DataSet
     */
    public DataSet<E> derive(String key, Object origin) {
        return putVar(key, origin);
    }
    public DataSet<E> putVar(String key, Object value) {
        int regex = 0;
        if(value instanceof String) {
            String str = (String)value;
            int idx = str.indexOf("${");
            if(idx != -1) {
                if(str.startsWith("$") && str.endsWith("}")) { // ${ID}
                    regex = 1; // 一个表达式
                    if (str.indexOf("${", idx + 2) > 0) {
                        if(idx != -1) {
                            regex = 2; // 多个表达式 ${CODE}-${NM}
                        }
                    }
                }else{
                    regex = 2;  // 123${ID}ABC
                }
            }
        }
        if(regex ==0) {
            for(DataRow row:rows) {
                row.put(key,value);
            }
        }else if(regex ==1) {
            for(DataRow row:rows) {
                String k = (String)value;
                k = k.substring(2, k.length()-1);
                row.put(key,row.get(k));
            }
        }else if(regex ==2) {
            for(DataRow row:rows) {
                row.put(key, row.getString((String)value));
            }
        }
        return this;
    }

    /**
     * 行转列
     * 表结构(编号, 姓名, 年度, 科目, 分数, 等级)
     * @param pks       唯一标识key(如编号,姓名)
     * @param classKeys 分类key(如年度,科目)
     * @param valueKeys 取值key(如分数,等级),如果不指定key则将整行作为value
     * @param extract   是否删除逻辑主键(pks)之外的其他列
     * @return DataSet
     * 如果指定key
     * 返回结构 [
     *      {编号:01,姓名:张三,2010-数学-分数:100},
     *      {编号:01,姓名:张三,2010-数学-等级:A},
     *      {编号:01,姓名:张三,2010-物理-分数:100}
     *  ]
     *  如果只有一个valueKey则返回[
     *      {编号:01,姓名:张三,2010-数学:100},
     *      {编号:01,姓名:张三,2010-物理:90}
     *  ]
     * 不指定valuekey则返回 [
     *      {编号:01,姓名:张三,2010-数学:{分数:100,等级:A}},
     *      {编号:01,姓名:张三,2010-物理:{分数:100,等级:A}}
     *  ]
     */
    public DataSet<E>  pivot(boolean extract, List<String> pks, List<String> classKeys, List<String> valueKeys) {
        DataSet<E>  result = distinct(extract, pks);
        DataSet<E>  classValues = distinct(classKeys);  // [{年度:2010,科目:数学},{年度:2010,科目:物理},{年度:2011,科目:数学}]
        for (E row : result) {
            for (E classValue : classValues) {
                DataRow params = new DataRow();
                params.copy(row, pks).copy(classValue);
                DataRow valueRow = getRow(params);
                if(null != valueRow && !valueRow.isEmpty()) {
                    valueRow.skip = true;
                }
                String finalKey = concatValue(classValue,"-");//2010-数学
                if(null != valueKeys && !valueKeys.isEmpty()) {
                    if(valueKeys.size() == 1) {
                        if (null != valueRow) {
                            row.put(finalKey, valueRow.get(valueKeys.get(0)));
                        } else {
                            row.put(finalKey, null);
                        }
                    }else {
                        for (String valueKey : valueKeys) {
                            // {2010-数学-分数:100;2010-数学-等级:A}
                            if (null != valueRow) {
                                row.put(finalKey + "-" + valueKey, valueRow.get(valueKey));
                            } else {
                                row.put(finalKey + "-" + valueKey, null);
                            }
                        }
                    }
                }else{
                    if (null != valueRow) {
                        row.put(finalKey, valueRow);
                    }else{
                        row.put(finalKey, null);
                    }
                }
            }
        }
        skip(false);
        return result;
    }
    public DataSet<E> pivot(List<String> pks, List<String> classKeys, List<String> valueKeys) {
        return pivot(true, pks, classKeys, valueKeys);
    }

    public DataSet<E> pivot(boolean extract, String[] pks, String[] classKeys, String[] valueKeys) {
        return pivot(extract, BeanUtil.array2list(pks),BeanUtil.array2list(classKeys),BeanUtil.array2list(valueKeys));
    }

    public DataSet<E> pivot(String[] pks, String[] classKeys, String[] valueKeys) {
        return pivot(true, pks, classKeys, valueKeys);
    }
    
    /**
     * 行转列
     * @param pk       唯一标识key(如姓名)多个key以,分隔如(编号,姓名)
     * @param classKey 分类key(如科目)多个key以,分隔如(科目,年度)
     * @param valueKey 取值key(如分数)多个key以,分隔如(分数,等级)
     * @return DataSet
     *  表结构(姓名,科目,分数)
     *  返回结构 [{姓名:张三,数学:100,物理:90,英语:80},{姓名:李四,数学:100,物理:90,英语:80}]
     */
    public DataSet<E> pivot(boolean extract, String pk, String classKey, String valueKey) {
        List<String> pks = BeanUtil.array2list(pk.trim().split(","));
        List<String> classKeys = BeanUtil.array2list(classKey.trim().split(","));
        List<String> valueKeys = BeanUtil.array2list(valueKey.trim().split(","));
        return pivot(extract, pks, classKeys, valueKeys);
    }
    public DataSet<E> pivot(String pk, String classKey, String valueKey) {
        return pivot(true, pk, classKey, valueKey);
    }
    public DataSet<E> pivot(boolean extract, String pk, String classKey) {
        List<String> pks = BeanUtil.array2list(pk.trim().split(","));
        List<String> classKeys = BeanUtil.array2list(classKey.trim().split(","));
        List<String> valueKeys = new ArrayList<>();
        return pivot(extract, pks, classKeys, valueKeys);
    }
    public DataSet<E> pivot(String pk, String classKey) {
        return pivot(true, pk, classKey);
    }

    public DataSet<E> pivot(boolean extract, List<String> pks, List<String> classKeys, String ... valueKeys) {
        List<String> list = new ArrayList<>();
        if(null != valueKeys) {
            for(String item:valueKeys) {
                list.add(item);
            }
        }
        return pivot(extract, pks, classKeys, valueKeys);
    }
    public DataSet<E> pivot(List<String> pks, List<String> classKeys, String ... valueKeys) {
        return pivot(true, pks, classKeys, valueKeys);
    }

    /**
     * [{code:"A", type:"A1"},{code:"B", type:"B1"}] map("code","type") 转换成{A:"A1",B:"B1"}<br/>
     * 如果需要排序、去重 可以先调用asc/desc distinct
     * @param key 作为key的列
     * @param value 作为value的列
     * @return LinkedHashMap
     */
    public Map map(String key, String value) {
        Map map = new LinkedHashMap();
        for(DataRow row:rows) {
            map.put(row.get(key), row.get(value));
        }
        return map;
    }

    /**
     * 将key列的值作为新map的key,this作为value
     * @param key key
     * @return map
     */
    public Map map(String key) {
        Map map = new LinkedHashMap();
        for(DataRow row:rows) {
            map.put(row.get(key), row);
        }
        return map;
    }
    public Map map(int key) {
        Map map = new LinkedHashMap();
        if(null ==rows || rows.isEmpty()) {
            return map;
        }
        String k = rows.get(0).keys().get(0);
        for(DataRow row:rows) {
            map.put(row.get(k), row);
        }
        return map;
    }

    /**
     * @param key 作为key的列的下标
     * @param value 作为value的列的下标
     * @return LinkedHashMap
     */
    public Map map(int key, int value) {
        if(null != rows && !rows.isEmpty()) {
            List<String> keys = rows.get(0).keys();
            if(keys.size()>key && keys.size()>value) {
                return map(keys.get(key), keys.get(value));
            }
        }
        return new LinkedHashMap();
    }

    /**
     * 默认第0列值作为key,第1列值作为value
     * @return LinkedHashMap
     */
    public Map map() {
        if(null != rows && !rows.isEmpty()) {
            List<String> keys = rows.get(0).keys();
            if(keys.size()>1) {
                return map(keys.get(0), keys.get(1));
            }
        }
        return new LinkedHashMap();
    }

    /**
     * [{code:"A", type:"A1"},{code:"B", type:"B1"}] map("code","type") 转换成{A:"A1",B:"B1"}<br/>
     * 如果需要排序、去重 可以先调用asc/desc distinct
     * @param key 作为key的列
     * @param value 作为value的列
     * @return DataRow
     */
    public DataRow row(String key, String value) {
        DataRow result = new DataRow();
        for(DataRow row:rows) {
            result.put(row.getString(key), row.get(value));
        }
        return result;
    }

    /**
     * @param key 作为key的列的下标
     * @param value 作为value的列的下标
     * @return DataRow
     */
    public DataRow row(int key, int value) {
        if(null != rows && !rows.isEmpty()) {
            List<String> keys = rows.get(0).keys();
            if(keys.size()>key && keys.size()>value) {
                return row(keys.get(key), keys.get(value));
            }
        }
        return new DataRow();
    }

    /**
     * 默认第0列值作为key,第1列值作为value
     * @return DataRow
     */
    public DataRow row() {
        if(null != rows && !rows.isEmpty()) {
            List<String> keys = rows.get(0).keys();
            if(keys.size()>1) {
                return row(keys.get(0), keys.get(1));
            }
        }
        return new DataRow();
    }

    private String concatValue(DataRow row, String split) {
        StringBuilder builder = new StringBuilder();
        List<String> keys = row.keys();
        for(String key:keys) {
            if(builder.length() > 0) {
                builder.append(split);
            }
            builder.append(row.getString(key));
        }
        return builder.toString();
    }
    private String[] kvs(DataRow row) {
        List<String> keys = row.keys();
        int size = keys.size();
        String[] kvs = new String[size*2];
        for(int i=0; i<size; i++) {
            String k = keys.get(i);
            String v = row.getStringNvl(k);
            kvs[i*2] = k;
            kvs[i*2+1] = v;
        }
        return kvs;
    }

    /**
     * 排序(正序)
     * @param keys 参与排序的列
     * @return this
     */
    public DataSet<E> asc(final String... keys) {
        sort(1, keys);
        return this;
    }

    /**
     * 排序(倒序)
     * @param keys 参与排序的列
     * @return this
     */
    public DataSet<E> desc(final String... keys) {
        sort(-1, keys);
        return this;
    }

    /**
     * 排序默认根据元数据类型，如果没有设置的一律按String执行
     * @param factor 1:正序 -1:倒序
     * @param keys 参与排序的列
     * @return this
     */
    public DataSet<E> sort(int factor,final String ... keys) {
        synchronized (this){
            Collections.sort(rows, new Comparator<DataRow>() {
                public int compare(DataRow r1, DataRow r2) {
                    int result = 0;
                    for (String key : keys) {
                        TypeMetadata.CATEGORY_GROUP type = null;
                        Column column = DataSet.this.getMetadata(key);
                        if(null != column) {
                            type = column.getTypeMetadata().getCategoryGroup();
                        }
                        Object v1 = r1.get(key);
                        Object v2 = r2.get(key);
                        if (null == v1) {
                            if (null == v2) {
                                continue;
                            }
                            return -factor;
                        } else {
                            if (null == v2) {
                                return factor;
                            }
                        }
                        if(type == TypeMetadata.CATEGORY_GROUP.NUMBER) {
                            BigDecimal num1 = new BigDecimal(v1.toString());
                            BigDecimal num2 = new BigDecimal(v2.toString());
                            result = num1.compareTo(num2);
                        }else if(type == TypeMetadata.CATEGORY_GROUP.DATETIME) {
                            Date date1 = DateUtil.parse(v1);
                            Date date2 = DateUtil.parse(v2);
                            result = date1.compareTo(date2);
                        }else{
                            result = v1.toString().compareTo(v2.toString());
                        }
                        if(result != 0) {
                            if (result > 0) {
                                return factor;
                            } else {
                                return -factor;
                            }
                        }
                    }
                    return result;
                }
            });
        }
        return this;
    }
    public DataSet<E> addAllUpdateColumns() {
        for (DataRow row : rows) {
            row.addAllUpdateColumns();
        }
        return this;
    }

    public DataSet<E> clearUpdateColumns() {
        for (DataRow row : rows) {
            row.clearUpdateColumns();
        }
        return this;
    }

    public DataSet<E> removeNull(String... keys) {
        for (DataRow row : rows) {
            row.removeNull(keys);
        }
        return this;
    }

    private static String key(String key) {
        if (null != key && ConfigTable.IS_UPPER_KEY) {
            key = key.toUpperCase();
        }
        return key;
    }

    /**
     * 替换所有NULL值
     *
     * @param replace replace
     * @param keys 需要替换的key 如果不指定则替换全部key
     * @return DataSet
     */
    public DataSet<E> replaceNull(String replace, String ... keys) {
        for (DataRow row : rows) {
            row.replaceNull(replace, keys);
        }
        return this;
    }

    /**
     * 替换所有空值
     *
     * @param replace replace
     * @param keys 需要替换的key 如果不指定则替换全部key
     * @return DataSet
     */
    public DataSet<E> replaceEmpty(String replace, String ... keys) {
        for (DataRow row : rows) {
            row.replaceEmpty(replace, keys);
        }
        return this;
    }

    public DataSet<E> replaces(String oldChar, String replace, String ... keys) {
        if (null == oldChar) {
            return this;
        }
        for (DataRow row : rows) {
            row.replaces(oldChar, replace, keys);
        }
        return this;
    }

    public DataSet<E> replaces(boolean regex, String oldChar, String replace, String ... keys) {
        if (regex) {
            return replaceRegex(oldChar, replace, keys);
        }else{
            return replaces(oldChar, replace, keys);
        }
    }

    public DataSet<E> replaceRegex(String regex, String replace, String ... keys) {
        for (DataRow row : rows) {
            row.replaceRegex(regex, replace, keys);
        }
        return this;
    }
    /* ************************* 类sql操作 ************************************** */

    /**
     * 随机取一行
     * @return DataRow
     */
    public DataRow random() {
        DataRow row = null;
        int size = size();
        if (size > 0) {
            row = getRow(BasicUtil.getRandomNumber(0, size - 1));
        }
        return row;
    }

    /**
     * 随机取qty行
     * @param qty 行数
     * @return DataSet
     */
    public DataSet<E> randoms(int qty) {
        DataSet<E> set = new DataSet();
        int size = size();
        if (qty < 0) {
            qty = 0;
        }
        if (qty > size) {
            qty = size;
        }
        for (int i = 0; i < qty; i++) {
            while (true) {
                int idx = BasicUtil.getRandomNumber(0, size - 1);
                E row = getRow(idx);
                if (!set.contains(row)) {
                    set.add(row);
                    break;
                }
            }
        }
        set.copyProperty(this);
        return set;
    }

    /**
     * 根据ognl表达式 设置集合中每个DataRow.key的值
     * @param key key
     * @param formula ognl表达式
     * @param values 运行时值
     * @param strategy 发生异常时执行策略 0:忽略异常继续下一个 1:全部回滚
     * @param exception 发生异常时是否抛出
     * @return DataSet
     * @throws  Exception Exception
     */
    public DataSet<E> ognl(String key, String formula, Object values, int strategy, boolean exception) throws Exception {
        if(strategy == 0) {
            for(DataRow row:rows) {
                try {
                    row.ognl(key, formula, values);
                }catch (Exception e) {
                    if(exception) {
                        throw e;
                    }
                }
            }
        }else{
            List<Object> results = new ArrayList<>();
            for(DataRow row:rows) {
                try {
                    results.add(row.ognl(formula, values));
                }catch (Exception e) {
                    results.add(null);
                    if(exception) {
                        throw e;
                    }
                }
            }
            int size = results.size();
            for(int i=0; i<size; i++) {
                Object result = results.get(i);
                if(null != result) {
                    rows.get(i).put(key,result);
                }
            }
        }
        return this;
    }

    public DataSet<E> ognl(String key, String formula, int strategy, boolean exception) throws Exception {
        return ognl(key, formula, null, strategy, exception);
    }
    public DataSet<E> ognl(String key, String formula) throws Exception {
        return ognl(key, formula, null, 0, false);
    }

    /**
     * 随机取min到max行
     * @param min min
     * @param max max
     * @return DataSet
     */
    public DataSet<E> randoms(int min, int max) {
        int qty = BasicUtil.getRandomNumber(min, max);
        return randoms(qty);
    }

    public DataSet<E> unique(String... keys) {
        return distinct(keys);
    }

    /**
     * 根据正则提取集合
     * @param key key
     * @param regex 正则
     * @param mode 匹配方式
     * @return DataSet
     */
    public DataSet<E> regex(String key, String regex, Regular.MATCH_MODE mode) {
        DataSet<E> set = new DataSet();
        String tmpValue;
        for (E row : this) {
            tmpValue = row.getString(key);
            if (RegularUtil.match(tmpValue, regex, mode)) {
                set.add(row);
            }
        }
        set.copyProperty(this);
        return set;
    }

    public DataSet<E> regex(String key, String regex) {
        return regex(key, regex, Regular.MATCH_MODE.MATCH);
    }

    public boolean checkRequired(String... keys) {
        for (DataRow row : rows) {
            if (!row.checkRequired(keys)) {
                return false;
            }
        }
        return true;
    }

    public String getDatalink() {
        return datalink;
    }

    public void setDatalink(String datalink) {
        this.datalink = datalink;
    }

    public DataSet<E> copy(boolean regex, DataRow data, String... keys) {
        if (null == data) {
            return this;
        }
        for(DataRow row:rows) {
            row.copy(regex, data, keys);
        }
        return this;

    }
    public DataSet<E> copy(boolean regex, DataRow data, List<String> keys) {
        if (null == data || data.isEmpty()) {
            return this;
        }
        for(DataRow row:rows) {
            row.copy(regex, data, keys);
        }
        return this;
    }

    public DataSet<E> copy(DataRow data, String... keys) {
        return copy(false, data, keys);
    }

    /**
     * 复制String类型数据
     * @param data data
     * @param keys keys
     * @return DataSet
     */
    public DataSet<E> copyString(DataRow data, String... keys) {
        for(DataRow row:rows) {
            row.copyString(data, keys);
        }
        return this;
    }

    public class Select implements Serializable {
        private static final long serialVersionUID = 1L;
        private boolean ignoreCase = true;    // 是否忽略大小写
        /**
         * 是否忽略NULL 如果设置成true 在执行equal notEqual like contains进 null与null比较返回false
         * 左右出现NULL时直接返回false
         * true会导致一行数据 equal notEqual都筛选不到
         */
        private boolean ignoreNull = true;

        public DataSet<E> setIgnoreCase(boolean bol) {
            this.ignoreCase = bol;
            return DataSet.this;
        }

        public DataSet<E> setIgnoreNull(boolean bol) {
            this.ignoreNull = bol;
            return DataSet.this;
        }

        /**
         * 筛选key=value的子集
         *
         * @param key   key
         * @param value value
         * @return DataSet
         */
        public DataSet<E> equals(String key, Object value) {
            return equals(DataSet.this, key, value);
        }

        private DataSet<E>  equals(DataSet<E>  src, String key, Object value) {
            DataSet<E>  set = new DataSet();
            value = BeanUtil.first(value);
            Object tmpValue;
            for (E row : src) {
                tmpValue = row.get(key);
                if (ignoreNull) {
                    if (null == tmpValue || null == value) {
                        continue;
                    }
                } else {
                    if (null == tmpValue && null == value) {
                        set.add(row);
                        continue;
                    }
                }

                if (null != tmpValue) {
                    boolean chk = false;
                    boolean tmpNumber = BasicUtil.isNumber(tmpValue);
                    if (ignoreCase) {
                        if(null == value) {
                            chk = false;
                        }else {
                            boolean valueNumber = BasicUtil.isNumber(value);
                            if(valueNumber && tmpNumber) {
                                chk = new BigDecimal(tmpValue.toString()).compareTo(new BigDecimal(value.toString())) == 0;
                            }else {
                                chk = tmpValue.toString().equalsIgnoreCase(value.toString());
                            }
                        }
                    } else {
                        boolean valueNumber = BasicUtil.isNumber(value);
                        if(valueNumber && tmpNumber) {
                            chk = new BigDecimal(tmpValue.toString()).compareTo(new BigDecimal(value.toString())) == 0;
                        }else {
                            chk = tmpValue.equals(value);
                        }
                    }
                    if (chk) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * 筛选key ！= value的子集
         *
         * @param key   key
         * @param value value
         * @return DataSet
         */
        public DataSet<E>  notEquals(String key, Object value) {
            return notEquals(DataSet.this, key, value);
        }

        private DataSet<E>  notEquals(DataSet<E> src, String key, Object value) {
            DataSet<E>  set = new DataSet();
            value = BeanUtil.first(value);
            String tmpValue;
            for (E row : src) {
                tmpValue = row.getString(key);
                if (ignoreNull) {
                    if (null == tmpValue || null == value) {
                        continue;
                    }
                } else {
                    if (null == tmpValue && null == value) {
                        set.add(row);
                        continue;
                    }
                }

                if (null != tmpValue) {
                    boolean chk = false;
                    if (ignoreCase) {
                        if(null == value) {
                            chk = false;
                        }else {
                            chk = !tmpValue.equalsIgnoreCase(value.toString());
                        }
                    } else {
                        chk = !tmpValue.equals(value);
                    }
                    if (chk) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * 筛选key列的值是否包含value的子集
         *
         * @param key   key
         * @param value value
         * @return DataSet
         */
        public DataSet<E> contains(String key, Object value) {
            return contains(DataSet.this, key, value);
        }

        private DataSet<E> contains(DataSet<E> src, String key, Object value) {
            DataSet<E> set = new DataSet();
            value = BeanUtil.first(value);
            String tmpValue;
            for (E row : src) {
                tmpValue = row.getString(key);
                if (ignoreNull) {
                    if (null == tmpValue || null == value) {
                        continue;
                    }
                } else {
                    if (null == tmpValue && null == value) {
                        set.add(row);
                        continue;
                    }
                }

                if (null != tmpValue) {
                    if (null == value) {
                        continue;
                    }
                    if (ignoreCase) {
                        tmpValue = tmpValue.toLowerCase();
                        value = value.toString().toLowerCase();
                    }
                    if (tmpValue.contains(value.toString())) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * 筛选key列的值like pattern的子集,pattern遵循sql通配符的规则,%表示任意个字符,_表示一个字符
         * key值like value
         * @param key     列
         * @param pattern 表达式
         * @return DataSet
         */
        public DataSet<E> like(String key, String pattern) {
            return like(DataSet.this, key, pattern);
        }

        private DataSet<E> like(DataSet<E> src, String key, String pattern) {
            DataSet<E> set = new DataSet();
            if (null != pattern) {
                pattern = pattern.replace("!","^").replace("_","\\s|\\S").replace("%","(\\s|\\S)*");
            }
            String tmpValue;
            for (E row : src) {
                tmpValue = row.getString(key);
                if (ignoreNull) {
                    if (null == tmpValue || null == pattern) {
                        continue;
                    }
                } else {
                    if (null == tmpValue && null == pattern) {
                        set.add(row);
                        continue;
                    }
                }
                if (null != tmpValue) {
                    if (null == pattern) {
                        continue;
                    }
                    if (ignoreCase) {
                        pattern = "(?i)"+pattern;
                    }
                    if (RegularUtil.match(tmpValue, pattern, Regular.MATCH_MODE.MATCH)) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        public DataSet<E> notLike(String key, String pattern) {
            return notLike(DataSet.this, key, pattern);
        }

        private DataSet<E> notLike(DataSet<E> src, String key, String pattern) {
            DataSet<E> set = new DataSet();
            if (null == pattern) {
                return set;
            }
            pattern = pattern.replace("!","^").replace("_","\\s|\\S").replace("%","(\\s|\\S)*");
            String tmpValue;
            for (E row : src) {
                tmpValue = row.getString(key);
                if (ignoreNull) {
                    if (null == tmpValue || null == pattern) {
                        continue;
                    }
                } else {
                    if (null == tmpValue && null == pattern) {
                        set.add(row);
                        continue;
                    }
                }
                if (null != tmpValue) {
                    if (null == pattern) {
                        continue;
                    }
                    if (ignoreCase) {
                        pattern = "(?i)"+pattern;
                    }
                    if (!RegularUtil.match(tmpValue, pattern, Regular.MATCH_MODE.MATCH)) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * key值startWith prefix
         * @param key key
         * @param prefix prefix
         * @return DataSet
         */
        public DataSet<E> startWith(String key, String prefix) {
            return startWith(DataSet.this, key, prefix);
        }

        private DataSet<E> startWith(DataSet<E> src, String key, String prefix) {
            DataSet<E> set = new DataSet();
            String tmpValue;
            for (E row : src) {
                tmpValue = row.getString(key);
                if (ignoreNull) {
                    if (null == tmpValue || null == prefix) {
                        continue;
                    }
                } else {
                    if (null == tmpValue && null == prefix) {
                        set.add(row);
                        continue;
                    }
                }

                if (null != tmpValue) {
                    if (null == prefix) {
                        continue;
                    }
                    if (ignoreCase) {
                        tmpValue = tmpValue.toLowerCase();
                        prefix = prefix.toLowerCase();
                    }
                    if (tmpValue.startsWith(prefix)) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * key值 endWith suffix
         * @param key key
         * @param suffix suffix
         * @return DataSet
         */
        public DataSet<E> endWith(String key, String suffix) {
            return endWith(DataSet.this, key, suffix);
        }

        private DataSet<E> endWith(DataSet<E> src, String key, String suffix) {
            DataSet<E> set = new DataSet();
            String tmpValue;
            for (E row : src) {
                tmpValue = row.getString(key);
                if (ignoreNull) {
                    if (null == tmpValue || null == suffix) {
                        continue;
                    }
                } else {
                    if (null == tmpValue && null == suffix) {
                        set.add(row);
                        continue;
                    }
                }

                if (null != tmpValue) {
                    if (null == suffix) {
                        continue;
                    }
                    if (ignoreCase) {
                        tmpValue = tmpValue.toLowerCase();
                        suffix = suffix.toLowerCase();
                    }
                    if (tmpValue.endsWith(suffix)) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * key值 in values
         * @param key key
         * @param values values
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> in(String key, T... values) {
            return in(DataSet.this, key, BeanUtil.array2list(values));
        }

        public <T> DataSet<E> in(String key, Collection<T> values) {
            return in(DataSet.this, key, values);
        }

        private <T> DataSet<E> in(DataSet<E> src, String key, Collection<T> values) {
            DataSet<E> set = new DataSet();
            for (E row : src) {
                if (BasicUtil.containsString(ignoreNull, ignoreCase, values, row.getString(key))) {
                    set.add(row);
                }
            }
            set.copyProperty(src);
            return set;
        }

        public <T> DataSet<E> notIn(String key, T... values) {
            return notIn(DataSet.this, key, BeanUtil.array2list(values));
        }

        public <T> DataSet<E> notIn(String key, Collection<T> values) {
            return notIn(DataSet.this, key, values);
        }

        private <T> DataSet<E> notIn(DataSet<E> src, String key, Collection<T> values) {
            DataSet<E> set = new DataSet();
            if (null != values) {
                String tmpValue = null;
                for (E row : src) {
                    tmpValue = row.getString(key);
                    if (ignoreNull && null == tmpValue) {
                        continue;
                    }
                    if (!BasicUtil.containsString(ignoreNull, ignoreCase, values, tmpValue)) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * values中有一个存在于item中即可
         * @param key item.key
         * @param values values
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> findInSetOr(String key, T... values) {
            return findInSetOr(DataSet.this, key, BeanUtil.array2list(values));
        }

        public <T> DataSet<E> findInSetOr(String key, Collection<T> values) {
            return findInSetOr(DataSet.this, key, values);
        }

        private <T> DataSet<E> findInSetOr(DataSet<E> src, String key, Collection<T> values) {
            DataSet<E> set = new DataSet();
            if (null != values) {
                for (E row : src) {
                    Map<String, String> map = BeanUtil.value2map(ignoreCase, row.get(key));
                    for(T value:values) {
                        if(null != value) {
                            String k = value.toString();
                            if(ignoreCase) {
                                k = k.toUpperCase();
                            }
                            if(map.containsKey(k)) {
                                set.add(row);
                                break;
                            }
                        }
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * values中每一个都存在于item中才返回
         * @param key item.key
         * @param values values
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> findInSetAnd(String key, T... values) {
            return findInSetAnd(DataSet.this, key, BeanUtil.array2list(values));
        }

        public <T> DataSet<E> findInSetAnd(String key, Collection<T> values) {
            return findInSetAnd(DataSet.this, key, values);
        }

        private <T> DataSet<E> findInSetAnd(DataSet<E> src, String key, Collection<T> values) {
            DataSet<E> set = new DataSet();
            if (null != values) {
                for (E row : src) {
                    Map<String, String> map = BeanUtil.value2map(ignoreCase, row.get(key));
                    boolean chk = true;
                    for(T value:values) {
                        if(null == value) {
                            chk = false;
                            break;
                        }
                        String k = value.toString();
                        if(ignoreCase) {
                            k = k.toUpperCase();
                        }
                        if(!map.containsKey(k)) {
                            chk = false;
                            break;
                        }
                    }
                    if(chk) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * values中有一个存在于item中即可
         * @param key item.key
         * @param values values
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> findInSet(String key, T... values) {
            return findInSet(DataSet.this, key, BeanUtil.array2list(values));
        }

        public <T> DataSet<E> findInSet(String key, Collection<T> values) {
            return findInSet(DataSet.this, key, values);
        }

        private <T> DataSet<E> findInSet(DataSet<E> src, String key, Collection<T> values) {
            return findInSetOr(src, key, values);
        }

        public DataSet<E> isNull(String... keys) {
            return isNull(DataSet.this, keys);
        }

        private DataSet<E> isNull(DataSet<E> src, String... keys) {
            DataSet<E> set = src;
            if (null != keys) {
                for (String key : keys) {
                    set = isNull(set, key);
                }
            }
            return set;
        }

        private DataSet<E> isNull(DataSet<E> src, String key) {
            DataSet<E> set = new DataSet();
            for(E row:src) {
                if(null == row.get(key)) {
                    set.add(row);
                }
            }
            return set;
        }
        public DataSet<E> isNotNull(String... keys) {
            return isNotNull(DataSet.this, keys);
        }

        private DataSet<E> isNotNull(DataSet<E> src, String... keys) {
            DataSet<E> set = src;
            if (null != keys) {
                for (String key : keys) {
                    set = isNotNull(set, key);
                }
            }
            return set;
        }

        private DataSet<E> isNotNull(DataSet<E> src, String key) {
            DataSet<E> set = new DataSet();
            for(E row:src) {
                if(null != row.get(key)) {
                    set.add(row);
                }
            }
            return set;
        }
        public DataSet<E> notNull(String... keys) {
            return isNotNull(keys);
        }

        /**
         * 提取指定列都为空的集合
         * @param keys keys
         * @return DataSet
         */
        public DataSet<E> empty(String... keys) {
            return empty(DataSet.this, keys);
        }

        private DataSet<E> empty(DataSet<E> src, String... keys) {
            DataSet<E> set = src;
            if (null != keys) {
                for (String key : keys) {
                    set = empty(set, key);
                }
            }
            return set;
        }

        private DataSet<E> empty(DataSet<E> src, String key) {
            DataSet<E> set = new DataSet();
            for(E row:src) {
                if(row.isEmpty(key)) {
                    set.add(row);
                }
            }
            return set;
        }

        public DataSet<E> notEmpty(String... keys) {
            return notEmpty(DataSet.this, keys);
        }

        private DataSet<E> notEmpty(DataSet<E> src, String... keys) {
            DataSet<E> set = src;
            if (null != keys) {
                for (String key : keys) {
                    set = notEmpty(set, key);
                }
            }
            return set;
        }

        private DataSet<E> notEmpty(DataSet<E> src, String key) {
            DataSet<E> set = new DataSet();
            for(E row:src) {
                if(row.isNotEmpty(key)) {
                    set.add(row);
                }
            }
            return set;
        }

        /**
         * key值 小于 value
         * @param key DataRow取key值
         * @param value value
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> less(String key, T value) {
            return less(DataSet.this, key, value);
        }

        private <T> DataSet<E> less(DataSet<E> src, String key, T value) {
            DataSet<E> set = new DataSet();
            if (null == value) {
                return set;
            }
            Object first = BeanUtil.first(value);
            if(null == first) {
                return set;
            }
            if (BasicUtil.isNumber(first)) {
                BigDecimal number = new BigDecimal(first.toString());
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getDecimal(key, 0).compareTo(number) < 0) {
                        set.add(row);
                    }
                }
            } else if (BasicUtil.isDate(first) || BasicUtil.isDateTime(first)) {
                try {
                    Date date = DateUtil.parse(first);
                    for (E row : src) {
                        if (null == row.get(key)) {
                            continue;
                        }
                        if (row.isNotEmpty(key) &&
                            DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key, new Date())) < 0) {
                            set.add(row);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getString(key).compareTo(first.toString()) < 0) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * key值 小于等于 value
         * @param key DataRow取key值
         * @param value value
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> lessEqual(String key, T value) {
            return lessEqual(DataSet.this, key, value);
        }

        private <T> DataSet<E> lessEqual(DataSet<E> src, String key, T value) {
            DataSet<E> set = new DataSet();
            if (null == value) {
                return set;
            }
            Object first = BeanUtil.first(value);
            if(null == first) {
                return set;
            }
            if (BasicUtil.isNumber(first)) {
                BigDecimal number = new BigDecimal(first.toString());
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getDecimal(key, 0).compareTo(number) <= 0) {
                        set.add(row);
                    }
                }
            } else if (BasicUtil.isDate(first) || BasicUtil.isDateTime(first)) {
                try {
                    Date date = DateUtil.parse(first.toString());
                    for (E row : src) {
                        if (null == row.get(key)) {
                            continue;
                        }
                        if (row.isNotEmpty(key) &&
                            DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key, new Date())) <= 0) {
                            set.add(row);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getString(key).compareTo(first.toString()) >= 0) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * key值 大于 value
         * @param key DataRow取key值
         * @param value value
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> greater(String key, T value) {
            return greater(DataSet.this, key, value);
        }

        private <T> DataSet<E> greater(DataSet<E> src, String key, T value) {
            DataSet<E> set = new DataSet();
            if (null == value) {
                return set;
            }
            Object first = BeanUtil.first(value);
            if(null == first) {
                return set;
            }
            if (BasicUtil.isNumber(first)) {
                BigDecimal number = new BigDecimal(first.toString());
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getDecimal(key, 0).compareTo(number) > 0) {
                        set.add(row);
                    }
                }
            } else if (BasicUtil.isDate(first) || BasicUtil.isDateTime(first)) {
                try {
                    Date date = DateUtil.parse(first.toString());
                    for (E row : src) {
                        if (null == row.get(key)) {
                            continue;
                        }
                        if (row.isNotEmpty(key) &&
                            DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key, new Date())) > 0) {
                            set.add(row);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getString(key).compareTo(first.toString()) > 0) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }

        /**
         * key值 大于等于 value
         * @param key DataRow取key值
         * @param value value
         * @return DataSet
         * @param <T> T
         */
        public <T> DataSet<E> greaterEqual(String key, T value) {
            return greaterEqual(DataSet.this, key, value);
        }

        private <T> DataSet<E> greaterEqual(DataSet<E> src, String key, T value) {
            DataSet<E> set = new DataSet();
            if (null == value) {
                return set;
            }
            Object first = BeanUtil.first(value);
            if(null == first) {
                return set;
            }
            if (BasicUtil.isNumber(first)) {
                BigDecimal number = new BigDecimal(first.toString());
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getDecimal(key, 0).compareTo(number) >= 0) {
                        set.add(row);
                    }
                }
            } else if (BasicUtil.isDate(first) || BasicUtil.isDateTime(first)) {
                try {
                    Date date = DateUtil.parse(first);
                    for (E row : src) {
                        if (null == row.get(key)) {
                            continue;
                        }
                        if (row.isNotEmpty(key) &&
                            DateUtil.diff(DateUtil.DATE_PART_MILLISECOND, date, row.getDate(key, new Date())) >= 0) {
                            set.add(row);
                        }
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                for (E row : src) {
                    if (null == row.get(key)) {
                        continue;
                    }
                    if (row.getString(key).compareTo(first.toString()) >= 0) {
                        set.add(row);
                    }
                }
            }
            set.copyProperty(src);
            return set;
        }
        /**
         * key值 大于等于min 小于等于 max
         * @param key DataRow取key值
         * @param min min
         * @param max max
         * @return DataSet
         * @param <T> T
         */

        public <T> DataSet<E> between(String key, T min, T max) {
            return between(DataSet.this, key, min, max);
        }

        private <T> DataSet<E> between(DataSet<E> src, String key, T min, T max) {
            DataSet<E> set = greaterEqual(src, key, min);
            set = lessEqual(set, key, max);
            return set;
        }
        public DataSet<E> ognl(String formula) throws Exception {
            return ognl(DataSet.this, formula);
        }
        private DataSet<E> ognl(DataSet<E> src, String formula) throws Exception {
            DataSet<E> set = new DataSet();
            for(E row:src) {
                OgnlContext context = new OgnlContext(null, null, new DefaultOgnlMemberAccess(true));
                Object value = Ognl.getValue(formula, context, row);
                if(BasicUtil.parseBoolean(value, false)) {
                    set.add(row);
                }
            }
            return set;
        }
        public DataSet<E> filter(Compare compare, String key, Object values) {
            DataSet<E> set = DataSet.this;
            if(compare == Compare.EQUAL) {
                set = equals(key, values);
            }else if(compare == Compare.NOT_EQUAL) {
                set = notEquals(key, values);
            }else if(compare == Compare.GREAT) {
                set = greater(key, values);
            }else if(compare == Compare.GREAT_EQUAL) {
                set = greaterEqual(key, values);
            }else if(compare == Compare.LESS) {
                set = less(key, values);
            }else if(compare == Compare.LESS_EQUAL) {
                set = lessEqual(key, values);
            }else if(compare == Compare.IN) {
                set = in(key, values);
            }else if(compare == Compare.NOT_IN) {
                set = notIn(key, values);
            }else if(compare == Compare.EMPTY) {
                set = empty(key);
            }else if(compare == Compare.NOT_EMPTY) {
                set = notEmpty(key);
            }else if(compare == Compare.NULL) {
                set = isNull(key);
            }else if(compare == Compare.NOT_NULL) {
                set = notNull(key);
            }else if(compare == Compare.LIKE) {
                set = like(key, string(values));
            }else if(compare == Compare.NOT_LIKE) {
                set = notLike(key, string(values));
            }else if(compare == Compare.START_WITH) {
                set = startWith(key, string(values));
            }else if(compare == Compare.END_WITH) {
                set = endWith(key, string(values));
            }else if(compare == Compare.FIND_IN_SET) {
                set = findInSet(key, values);
            }else if(compare == Compare.FIND_IN_SET_OR) {
                set = findInSetOr(key, values);
            }else if(compare == Compare.FIND_IN_SET_AND) {
                set = findInSetAnd(key, values);
            }
            return set;
        }
    }

    private String string(Object object) {
        Object first = BeanUtil.first(object);
        if(null != first) {
            return first.toString();
        }
        return null;
    }

    public class Format implements Serializable{
        private static final long serialVersionUID = 1L;
        /**
         * 根据列名日期格式化,如果失败 默认 ""
         * @param format 日期格式
         * @param cols 参考格式化的列(属性)如果不指定则不执行(避免传参失败)<br/>
         *             支持date(format, "SRC:TAR:DEF")表示把SRC列的值格式华后存入TAR列,SRC列保持不变,如果格式化失败使用默认值DEF<br/>
         *             如果需要根据数据烦劳确定参与格式化的列参考date(format,Class)<br/>
         *             如果需要格式化所有的日期类型的列(类型中出现date关键字)参考 date(greedy, format)
         * @return DataSet
         */
        public DataSet<E> date(String format, String ... cols) {
            for(DataRow row:rows) {
                row.format.date(format, cols);
            }
            return DataSet.this;
        }
        /**
         * 根据数据类型日期格式化,如果失败 默认 ""<br/>
         * 如set.format.date("yyyy-MM-dd", Date.class);
         * @param format 日期格式
         * @param classes 数据类型,不指定则不执行(避免传参失败)<br/>
         *             如果需要根据列名确定参与格式化的列参考date(format, cols)<br/>
         *             如果需要格式化所有的日期类型的列(类型中出现date关键字)参考date(greedy, format)
         * @return DataSet
         */
        public DataSet<E> date(String format, Class ... classes) {
            for(DataRow row:rows) {
                row.format.date(format, classes);
            }
            return DataSet.this;
        }

        /**
         * 格式化所有日期类型列(类型或列名中出现date关键字)
         * @param greedy false:只检查JAVA和SQL数据类型, true:在以上基础上检测列名
         * @param format 日期格式
         * @param def 默认值
         * @return DataSet
         */
        public DataSet<E> date(boolean greedy, String format, String def) {
            for(DataRow row:rows) {
                row.format.date(greedy, format, def);
            }
            return DataSet.this;
        }
        /**
         * 格式化所有日期类型列(类型或列名中出现date关键字)
         * @param greedy false:只检查JAVA和SQL数据类型, true:在以上基础上检测列名
         * @param format 日期格式
         * @param def 默认值
         * @return DataSet
         */
        public DataSet<E> date(boolean greedy, String format, Date def) {
            for(DataRow row:rows) {
                row.format.date(greedy, format, def);
            }
            return DataSet.this;
        }
        /**
         * 格式化所有日期类型列(类型或列名中出现date关键字),如果失败 默认 ""
         * @param greedy false:只检查JAVA和SQL数据类型, true:在以上基础上检测列名
         * @param format 日期格式
         * @return DataSet
         */
        public DataSet<E> date(boolean greedy, String format) {
            for(DataRow row:rows) {
                row.format.date(greedy, format);
            }
            return DataSet.this;
        }
        /**
         * 根据列名数字格式化,如果失败 默认 ""
         * @param format 数字格式
         * @param cols 参考格式化的列(属性)如果不指定则不执行(避免传参失败)<br/>
         *             支持number(format, "SRC:TAR:DEF")表示把SRC列的值格式华后存入TAR列,SRC列保持不变,如果格式化失败使用默认值DEF<br/>
         *             如果需要根据数据烦劳确定参与格式化的列参考number(format,Class)<br/>
         *             如果需要格式化所有的数字类型的列参考number(greedy, format)
         * @return DataSet
         */
        public DataSet<E> number(String format, String ... cols) {
            for(DataRow row:rows) {
                row.format.number(format, cols);
            }
            return DataSet.this;
        }
        /**
         * 根据数据类型数字格式化,如果失败 默认 ""<br/>
         * 如set.format.number("##.00", Date.class);
         * @param format 数字格式
         * @param classes 数据类型,不指定则不执行(避免传参失败)<br/>
         *             如果需要根据列名确定参与格式化的列参考number(format, cols)<br/>
         *             如果需要格式化所有的数字类型的列参考number(greedy, format)
         * @return DataSet
         */
        public DataSet<E> number(String format, Class ... classes) {
            for(DataRow row:rows) {
                row.format.number(format, classes);
            }
            return DataSet.this;
        }

        /**
         * 格式化所有数字类型列
         * @param greedy 传入true时执行
         * @param format 数字格式
         * @param def 默认值
         * @return DataSet
         */
        public DataSet<E> number(boolean greedy, String format, String def) {
            for(DataRow row:rows) {
                row.format.number(greedy, format, def);
            }
            return DataSet.this;
        }
        /**
         * @param greedy 传入true时执行
         * @param format 数字格式
         * @return DataRow
         */
        public DataSet<E> number(boolean greedy, String format) {
            return number(greedy, format, "");
        }
    }

    public class Parse implements Serializable{
        private static final long serialVersionUID = 1L;
        /**
         * 根据列名日期格式化,如果失败 默认 ""
         * @param cols 参考格式化的列(属性)如果不指定则不执行(避免传参失败)<br/>
         *             支持date(format, "SRC:TAR:2020-01-01 10:10:10")表示把SRC列的值格式华后存入TAR列,SRC列保持不变,如果格式化失败使用默认值2020-01-01 10:10:10<br/>
         *             如果需要根据数据烦劳确定参与格式化的列参考date(format,Class)<br/>
         *             如果需要格式化所有的日期类型的列(类型中出现date关键字)参考date(greedy, format)
         * @return DataRow
         */
        public DataSet<E> date(String ... cols) {
            for(DataRow row:rows) {
                row.parse.date(cols);
            }
            return DataSet.this;
        }
        public DataSet<E> number(String ... cols) {
            for(DataRow row:rows) {
                row.parse.number(cols);
            }
            return DataSet.this;
        }
    }
    public transient Select select = new Select();
    public transient Format format = new Format();
    public transient Parse parse = new Parse();
}