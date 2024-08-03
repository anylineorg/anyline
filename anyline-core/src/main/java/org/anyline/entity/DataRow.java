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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import ognl.Ognl;
import ognl.OgnlContext;
import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.KeyAdapter.KEY_CASE;
import org.anyline.adapter.init.LowerKeyAdapter;
import org.anyline.adapter.init.SrcKeyAdapter;
import org.anyline.adapter.init.UpperKeyAdapter;
import org.anyline.metadata.Catalog;
import org.anyline.metadata.Column;
import org.anyline.entity.geometry.Point;
import org.anyline.metadata.Schema;
import org.anyline.metadata.Table;
import org.anyline.proxy.EntityAdapterProxy;
import org.anyline.util.*;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class DataRow extends LinkedHashMap<String, Object> implements Serializable, AnyData<DataRow> {
    private static final long serialVersionUID = -2098827041540802313L;
    private static final Logger log = LoggerFactory.getLogger(DataRow.class);

    //public static String KEY_PARENT             = "PARENT"              ; // 上级
    //public static String KEY_ALL_PARENT         = "ALL_PARENT"          ; // 所有上级
    public static String KEY_CHILDREN           = "CHILDREN"            ; // 子级
    public static String KEY_ALL_CHILDREN       = "ALL_CHILDREN"        ; // 所有子级
    public static String KEY_ITEMS              = "ITEMS"               ; // items
    public static KEY_CASE DEFAULT_KEY_CASE     = KEY_CASE.CONFIG       ; // key case
    public static String DEFAULT_PRIMARY_KEY    = ConfigTable.DEFAULT_PRIMARY_KEY;

    protected Boolean override                    = null                  ; //如果数据库中存在相同数据(根据主键判断)是否覆盖 true或false会检测数据库null不检测

    protected boolean updateNullColumn            = ConfigTable.IS_UPDATE_NULL_COLUMN;
    protected boolean updateEmptyColumn           = ConfigTable.IS_UPDATE_EMPTY_COLUMN;
    protected boolean insertNullColumn            = ConfigTable.IS_INSERT_NULL_COLUMN;
    protected boolean insertEmptyColumn           = ConfigTable.IS_INSERT_EMPTY_COLUMN;
    protected boolean replaceEmptyNull            = ConfigTable.IS_REPLACE_EMPTY_NULL;
    protected Boolean ignoreCase                  = ConfigTable.IS_KEY_IGNORE_CASE;

    /*
     * 相当于Class Name 如User/Department
     * 在关系型数据库场景中 也相当于表名
     * 主要应用在在非关系型数据库场景中 如Neo4j中的Node名 MongonDB中的Document名
     */
    protected String category                               = null                  ; // 分类
    protected LinkedHashMap<String, Column> metadatas       = null                  ; // 数据类型相关(需要开启ConfigTable.IS_AUTO_CHECK_METADATA)
    protected LinkedHashMap<String, Object> origin          = new LinkedHashMap<>() ; // 从数据库中查询的未处理的原始数据
    protected transient DataSet container                   = null                  ; // 包含当前对象的容器
    protected transient Map<String, DataSet> containers     = new HashMap<>()       ; // 包含当前对象的容器s
    protected transient Map<String, DataRow> parents        = new Hashtable<>()     ; // 上级
    protected List<String> primaryKeys                      = new ArrayList<>()     ; // 主键
    protected List<String> updateColumns                    = new ArrayList<>()     ; // 需要参与update insert操作
    protected List<String> ignoreUpdateColumns              = new ArrayList<>()     ; // 不参与update insert操作
    protected String datalink                               = null                  ; // 超链接
    protected String datasource                             = null                  ; // 数据源(表|视图|XML定义SQL)
    protected Catalog catalog                               = null                  ; // catalog
    protected Schema schema                                 = null                  ; // schema
    protected transient LinkedHashMap<String, Table> tables = new LinkedHashMap<>() ; // 数据来源表(图数据库可能来自多个表) //TODO 解析sql中多个表(未实现)
    protected DataRow attributes                            = null                  ; // 属性
    protected LinkedHashMap<String, Object> tags            = null                  ; // 标签
    protected DataRow relations                             = null                  ; // 对外关系
    protected long createTime                               = 0                     ; // 创建时间(毫秒)
    protected long nanoTime                                 = 0                     ; // 创建时间(纳秒)
    protected long expires                                  = -1                    ; // 过期时间(毫秒) 从创建时刻计时expires毫秒后过期
    protected Boolean isNew                                 = false                 ; // 强制新建(否则根据主键值判断insert | update)
    protected boolean isFromCache                           = false                 ; // 是否来自缓存
    protected Map<String, String> keymap                    = new HashMap<>()       ; // keymap
    protected boolean isUpperKey                            = false                 ; // 是否已执行大写key转换(影响到驼峰执行)
    protected Map<String, String> converts                  = new HashMap<>()       ; // key是否已转换<key, src><当前key, 原key>
    public boolean skip                                     = false                 ; // 遍历计算时标记
    protected KeyAdapter keyAdapter                         = null                  ; // key格式转换
    protected KEY_CASE keyCase 				                = DEFAULT_KEY_CASE      ; // 列名格式

    public DataRow() {
        parseKeyCase(null);
        String pk = keyAdapter.key(DEFAULT_PRIMARY_KEY);
        primaryKeys.clear();
        if (null != pk) {
            primaryKeys.add(pk);
        }
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
    }

    protected void parseKeyCase(KEY_CASE keyCase) {
        if(null == keyCase) {
            keyCase = this.keyCase;
        }else{
            this.keyCase = keyCase;
        }

        if (null != keyCase) {
            keyAdapter = KeyAdapter.parse(keyCase);
            if(keyCase == KEY_CASE.CONFIG) {
                if (ConfigTable.IS_UPPER_KEY) {
                    keyAdapter = UpperKeyAdapter.getInstance();
                } else if (ConfigTable.IS_LOWER_KEY) {
                    keyAdapter = LowerKeyAdapter.getInstance();
                }
            }
        } else {
            keyAdapter = SrcKeyAdapter.getInstance();
        }

    }

    public DataRow(KEY_CASE keyCase) {
        this();
        parseKeyCase(keyCase);
    }

    public DataRow(String table) {
        this();
        this.setTable(table);
    }

    public DataRow(Map<String, Object> map) {
        this();
        parseMap(null, map);
    }
    public DataRow(KEY_CASE keyCase, Map<String, Object> map) {
        this(keyCase);
        parseMap(null, map);
    }
    public DataRow(LinkedHashMap columns, Map<String, Object> map) {
        this();
        parseMap(columns, map);
    }
    public DataRow parseMap(LinkedHashMap columns, Map<String, Object> map) {
        setMetadata(columns);
        Set<Map.Entry<String, Object>> set = map.entrySet();
        for (Map.Entry<String, Object> entity : set) {
            Object value = entity.getValue();
            if(null != value) {
                if(value instanceof Map) {
                    value = new DataRow((Map)value);
                }
                String type = getMetadataTypeName(entity.getKey());
                if(null != type && type.toUpperCase().contains("JSON")) {
                    String str = value.toString().trim();
                    if(str.startsWith("{")) {
                        value = DataRow.parseJson(str);
                    }else if(str.startsWith("[")) {
                        value = DataSet.parseJson(str);
                    }
                }
                /*if(value instanceof byte[]) {
                    value = new String((byte[]) value);
                }*/
            }
            put(keyAdapter.key(entity.getKey()), value);
        }
        return this;
    }
    public DataRow serCreateTime(Long time) {
        this.createTime = time;
        return this;
    }
    public DataRow serCreateTime(Date time) {
        this.createTime = time.getTime();
        return this;
    }

    public long getNanoTime() {
        return nanoTime;
    }

    public void setNanoTime(long nanoTime) {
        this.nanoTime = nanoTime;
    }

    /**
     * 数组解析成DataRow
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param list 数组
     * @param fields 下标对应的属性(字段/key)名称, 如果不输入则以下标作为DataRow的key, 如果属性数量超出list长度, 取null值存入DataRow
     * @return DataRow
     */
    public static DataRow parseList(KEY_CASE keyCase, DataRow row, Collection<?> list, String... fields) {
        if(null == row) {
            row = new DataRow(keyCase);
        }
        if (null != list) {

            if (null == fields || fields.length == 0) {
                int i = 0;
                for (Object obj : list) {
                    row.put("" + i++, obj);
                }
            } else {
                Object[] items = list.toArray();
                int len = fields.length;
                for (int i = 0; i < len; i++) {
                    String field = fields[i];
                    Object value = null;
                    if (i < items.length - 1) {
                        value = items[i];
                    }
                    row.put(field, value);
                }
            }
        }
        return row;
    }

    public static DataRow parseList(DataRow row, Collection<?> list, String... fields) {
        return parseList(KEY_CASE.CONFIG, row, list, fields);
    }
    public static DataRow parseList(Collection<?> list, String... fields) {
        return parseList(KEY_CASE.CONFIG, null, list, fields);
    }
    public static DataRow parseList(KEY_CASE keyCase, Collection<?> list, String... fields) {
        return parseList(keyCase, null, list, fields);
    }

    public DataRow setKeyCase(KEY_CASE keyCase) {
        parseKeyCase(keyCase);
        return this;
    }

    /**
     * 解析实体类对象
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param obj obj
     * @param keys 列名:obj属性名 "ID:memberId"
     * @return DataRow
     */
    @SuppressWarnings("rawtypes")
    public static DataRow parse(DataRow row, Object obj, String... keys) {
        if (EntityAdapterProxy.hasAdapter(obj.getClass())) {
            row = EntityAdapterProxy.row(row, obj, keys);
            if (null != row) {
                return row;
            }
        }
        return parse(row, KEY_CASE.CONFIG, obj, keys);
    }
    public static DataRow parse(Object obj, String... keys) {
        return parse((DataRow)null, obj, keys);
    }
    public static DataRow parse(KEY_CASE keyCase, String txt, String... keys) {
        return parse((DataRow)null, keyCase, txt, keys);
    }

    public static DataRow build(DataRow row, Object obj, String... keys) {
        return parse(row, obj, keys);
    }

    public static DataRow build(Object obj, String... keys) {
        return parse((DataRow)null, obj, keys);
    }

    public static DataRow parse(DataRow row, KEY_CASE keyCase, Object obj, String... keys) {
        if(null != obj && obj instanceof String) {
            return parseJson(row, keyCase, (String)obj);
        }
        if(null == row) {
            row = new DataRow(keyCase);
        }
        Map<String, String> map = new HashMap<>();
        if (null != keys) {
            for (String key : keys) {
                String tmp[] = key.split(":");
                if (null != tmp && tmp.length > 1) {
                    map.put(putKeyCase(tmp[1].trim()), putKeyCase(tmp[0].trim()));
                }
            }
        }
        if (null != obj) {
            if (obj instanceof JsonNode) {
                row = parseJson(keyCase, (JsonNode) obj);
            } else if (obj instanceof DataRow) {
                row = (DataRow) obj;
            } else if (obj instanceof Map) {
                Map mp = (Map) obj;
                List<String> ks = BeanUtil.getMapKeys(mp);
                for (String k : ks) {
                    Object value = mp.get(k);
                    if (value instanceof Map) {
                        value = parse(value);
                    }
                    row.put(k, value);
                }
            } else {
                List<Field> fields = ClassUtil.getFields(obj.getClass(), false, false);
                for (Field field : fields) {
                    String fieldName = field.getName();
                    String col = map.get(fieldName);
                    if (null == col) {
                        col = fieldName;
                    }
                    row.put(keyCase, col, BeanUtil.getFieldValue(obj, field));
                }
            }
        }
        return row;
    }

    public static DataRow parse(KEY_CASE keyCase, Object obj, String... keys) {
        return parse(null, keyCase, obj, keys);
    }
    public static DataRow build(DataRow row, KEY_CASE keyCase, Object obj, String... keys) {
        return parse(row, keyCase, obj, keys);
    }
    public static DataRow build(KEY_CASE keyCase, Object obj, String... keys) {
        return parse(null, keyCase, obj, keys);
    }

    /**
     * 解析json结构字符
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param keyCase key大小写
     * @param json json
     * @return DataRow
     */
    public static DataRow parseJson(DataRow row, KEY_CASE keyCase, String json) {
        if (null != json) {
            try {
                return parseJson(row, keyCase, BeanUtil.JSON_MAPPER.readTree(json));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static DataRow parseJson(KEY_CASE keyCase, String json) {
        return parseJson(null, keyCase, json);
    }

    public static DataRow parseJson(DataRow row, String json) {
        return parseJson(row, KEY_CASE.CONFIG, json);
    }
    public static DataRow parseJson(String json) {
        return parseJson(null, KEY_CASE.CONFIG, json);
    }

    /**
     * 解析JSONObject
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param keyCase keyCase
     * @param json json
     * @return DataRow
     */
    public static DataRow parseJson(DataRow row, KEY_CASE keyCase, JsonNode json) {
        return (DataRow) parseJsonObject(row, keyCase, json);
    }
    public static DataRow parseJson(KEY_CASE keyCase, JsonNode json) {
        return (DataRow) parseJsonObject(null, keyCase, json);
    }

    public static DataRow parseJson(DataRow row, JsonNode json) {
        return parseJson(row, KEY_CASE.CONFIG, json);
    }
    public static DataRow parseJson(JsonNode json) {
        return parseJson(null, KEY_CASE.CONFIG, json);
    }

    public static Object parseJsonObject(Object obj, KEY_CASE keyCase, JsonNode json) {
        if (null == json) {
            return obj;
        }
        if (json.isValueNode()) {
            return BeanUtil.value(json);
        }
        if (json.isArray()) {
            Collection<Object> list = null;
            if(null != obj && obj instanceof Collection) {
                list = (Collection)obj;
            }else{
                list = new ArrayList<>();
            }
            Iterator<JsonNode> items = json.iterator();
            boolean isDataRow = true;
            while (items.hasNext()) {
                JsonNode item = items.next();
                Object row = parseJsonObject(obj, keyCase, item);
                if(row instanceof DataRow) {
                }else{
                    isDataRow = false;
                }
                list.add(row);
            }
            if(isDataRow) {
                return DataSet.parse(list);
            }
            return list;
        } else if (json.isObject()) {
            DataRow row = null;
            if(obj instanceof DataRow) {
                row = (DataRow)obj;
            }else{
                row = new DataRow(keyCase);
            }
            Iterator<Map.Entry<String, JsonNode>> fields = json.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                JsonNode value = field.getValue();
                String key = field.getKey();
                if (null != value) {
                    if (value.isValueNode()) {
                        row.put(key, BeanUtil.value(value));
                    } else {
                        row.put(key, parseJsonObject(keyCase, value));
                    }
                } else {
                    row.put(key, null);
                }

            }
            return row;
        }

        return null;
    }

    public static Object parseJsonObject(KEY_CASE keyCase, JsonNode json) {
        return parseJsonObject(null, keyCase, json);
    }
    /**
     * 解析xml结构字符
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param keyCase KEY_CASE
     * @param xml xml
     * @return DataRow
     */
    public static DataRow parseXml(DataRow row, KEY_CASE keyCase, String xml) {
        if (null != xml) {
            try {
                Document doc = DocumentHelper.parseText(xml);
                return parseXml(row, keyCase, doc.getRootElement());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static DataRow parseXml(DataRow row, String xml) {
        return parseXml(row, KEY_CASE.CONFIG, xml);
    }

    public static DataRow parseXml(String xml) {
        return parseXml(null, KEY_CASE.CONFIG, xml);
    }

    /**
     * 解析xml
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param keyCase KEY_CASE
     * @param element element
     * @return DataRow
     */
    public static DataRow parseXml(DataRow row, KEY_CASE keyCase, Element element) {

        if(null == row) {
            row = new DataRow(keyCase);
        }
        if (null == element) {
            return row;
        }
        Iterator<Element> childs = element.elementIterator();
        String key = element.getName();
        String namespace = element.getNamespacePrefix();
        if (BasicUtil.isNotEmpty(namespace)) {
            key = namespace + ":" + key;
        }
        if (element.isTextOnly() || !childs.hasNext()) {
            row.put(key, element.getTextTrim());
        } else {
            while (childs.hasNext()) {
                Element child = childs.next();
                String childKey = child.getName();
                String childNamespace = child.getNamespacePrefix();
                if (BasicUtil.isNotEmpty(childNamespace)) {
                    childKey = childNamespace + ":" + childKey;
                }
                if (child.isTextOnly() || !child.elementIterator().hasNext()) {
                    row.put(childKey, child.getTextTrim());
                    continue;
                }
                DataRow childRow = parseXml(row, keyCase, child);
                Object childStore = row.get(childKey);
                if (null == childStore) {
                    row.put(childKey, childRow);
                } else {
                    if (childStore instanceof DataRow) {
                        DataSet childSet = new DataSet();
                        childSet.add((DataRow) childStore);
                        childSet.add(childRow);
                        row.put(childKey, childSet);
                    } else if (childStore instanceof DataSet) {
                        ((DataSet) childStore).add(childRow);
                    }
                }
            }
        }
        Iterator<Attribute> attrs = element.attributeIterator();
        while (attrs.hasNext()) {
            Attribute attr = attrs.next();
            row.attr(attr.getName(), attr.getValue());
        }
        return row;
    }
    public static DataRow parseXml(KEY_CASE keyCase, Element element) {
        return parseXml(null, keyCase, element);
    }

    /**
     * 解析 key1, value1, key2, value2, key3:value3组合
     * @param row 在此基础上执行, 如果不提供则新创建
     * @param kvs kvs
     * @return DataRow
     */
    public static DataRow parseArray(DataRow row, String... kvs) {
        if(null == row) {
            row = new DataRow();
        }
        int len = kvs.length;
        int i = 0;
        while (i < len) {
            String p1 = kvs[i];
            if (BasicUtil.isEmpty(p1)) {
                i++;
                continue;
            } else if (p1.contains(":")) {
                String ks[] = BeanUtil.parseKeyValue(p1);
                row.put(ks[0], ks[1]);
                i++;
                continue;
            } else {
                if (i + 1 < len) {
                    String p2 = kvs[i + 1];
                    if (BasicUtil.isEmpty(p2) || !p2.contains(":")) {
                        row.put(p1, p2);
                        i += 2;
                        continue;
                    } else {
                        String ks[] = BeanUtil.parseKeyValue(p2);
                        row.put(ks[0], ks[1]);
                        i += 2;
                        continue;
                    }
                }

            }
            i++;
        }
        return row;
    }
    public static DataRow parseArray(String... kvs) {
        return parseArray((DataRow)null, kvs);
    }
    public DataRow setContainer(String key, DataSet container) {
        containers.put(key, container);
        return this;
    }
    public DataSet getContainer(String key) {
        return containers.get(key);
    }
    public DataRow setParent(String key, DataRow parent) {
        parents.put(key, parent);
        return this;
    }
    public DataRow getParent(String key) {
        return parents.get(key);
    }
    public DataSet getAllParent(String key) {
        DataSet set = new DataSet();
        DataRow parent = this.getParent(key);
        while (null != parent) {
            set.add(parent);
            parent = parent.getParent(key);
        }
        return set;
    }

    public Boolean getOverride() {
        return override;
    }

    public void setOverride(Boolean override) {
        this.override = override;
    }

    public DataRow setMetadata(LinkedHashMap<String, Column> metadatas) {
        this.metadatas = metadatas;
        return this;
    }

    public DataRow setMetadata(String name, Column column) {
        if(null == metadatas) {
            metadatas = new LinkedHashMap<>();
        }
        metadatas.put(name.toUpperCase(), column);
        return this;
    }
    public DataRow setMetadata(Column column) {
        if(null != column) {
            return setMetadata(column.getName(), column);
        }
        return this;
    }

    public LinkedHashMap<String, Column> getMetadatas() {
        return getMetadatas(false);
    }

    /**
     *
     * @param create 如果没有metadata是否通过keys创建
     * @return LinkedHashMap
     */
    public LinkedHashMap<String, Column> getMetadatas(boolean create) {
        LinkedHashMap<String, Column> result = metadatas;
        if(null == result || result.isEmpty()) {
            if(null != container) {
                result = container.getMetadatas();
            }
        }
        if(null == result || result.isEmpty()) {
            if(create) {
                result = new LinkedHashMap<>();
                for(String key:keySet()) {
                    result.put(key.toUpperCase(), new Column(key));
                }
            }
        }
        return result;
    }
    public Column getMetadata(String column) {
        LinkedHashMap<String, Column> metadatas = getMetadatas();
        if(null == metadatas) {
            return null;
        }
        Column metadata = metadatas.get(column.toUpperCase());
        if(null == metadata && null != container) {
            metadata = container.getMetadata(column);
        }
        return metadata;
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
    /**
     * 创建时间
     * @return long
     */
    public long getCreateTime() {
        return createTime;
    }
    public DataRow setCreateTime(long createTime) {
        this.createTime = createTime;
        return this;
    }

    /**
     * 过期时间
     * @return long
     */
    public long getExpires() {
        return expires;
    }

    /**
     * 设置过期时间
     * @param millisecond millisecond
     * @return DataRow
     */
    public DataRow setExpires(long millisecond) {
        this.expires = millisecond;
        return this;
    }

    public DataRow setExpires(int millisecond) {
        this.expires = millisecond;
        return this;
    }

    /**
     * 合并数据
     * @param row  row
     * @param over key相同时是否覆盖原数据
     * @return DataRow
     */
    public DataRow merge(DataRow row, boolean over) {
        List<String> keys = row.keys();
        for (String key : keys) {
            if (over || null == this.get(key)) {
                this.put(key, row.get(KEY_CASE.SRC, key));
            }
        }
        return this;
    }

    public Object ognl(String formula, Object values) throws Exception {
        if (null == values) {
            values = this;
        }
        formula = BeanUtil.parseRuntimeValue(values, formula);
        OgnlContext context = new OgnlContext(null, null, new DefaultOgnlMemberAccess(true));
        Object value = Ognl.getValue(formula, context, values);
        return value;
    }

    public Object ognl(String formula) throws Exception {
        return ognl(formula, this);
    }

    public DataRow ognl(String key, String formula, Object values) throws Exception {
        put(key, ognl(formula, values));
        return this;
    }

    public DataRow ognl(String key, String formula) throws Exception {
        return ognl(key, formula, this);
    }

    public DataRow merge(DataRow row) {
        return merge(row, false);
    }

    /**
     * 是否是新数据<br/>
     * 强制isNew=true时返回true<br/>
     * 全部主键都有值时返回false只要有一个主键值为空返回true
     * @return Boolean
     */
    public Boolean isNew() {
        if(null != isNew && isNew) {
            return true;
        }
        /*String pk = getPrimaryKey();
        String pv = getString(pk);
        return (null == pv || (null == isNew) || isNew || BasicUtil.isEmpty(pv));*/

        boolean fullPv = true; //主键值是否都有
        List<String> ks = getPrimaryKeys();
        for(String k:ks) {
            Object v = get(k);
            if(BasicUtil.isEmpty(v)) {
                fullPv = false;
                break;
            }
        }
        return !fullPv;
    }

    /**
     * 是否来自缓存
     * @return boolean
     */
    public boolean isFromCache() {
        return isFromCache;
    }

    /**
     * 设置是否来自缓存
     * @param bol bol
     * @return DataRow
     */
    public DataRow setIsFromCache(boolean bol) {
        this.isFromCache = bol;
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
    public DataRow putOrigin(String key, Object value) {
        if(null == origin) {
            origin = new LinkedHashMap<>();
        }
        origin.put(key, value);
        return this;
    }
    public String getCd() {
        return getString("cd");
    }

    public String getId() {
        return getString("id");
    }

    public String getCode() {
        return getString("code");
    }

    public String getNm() {
        return getString("nm");
    }

    public String getName() {
        return getString("name");
    }

    public String getTitle() {
        return getString("title");
    }

    /**
     * 默认子集
     * @return DataSet
     */
    public DataSet getItems() {
        Object items = get(KEY_ITEMS);
        if (items instanceof DataSet) {
            return (DataSet) items;
        }
        return null;
    }

    public DataRow putItems(Object obj) {
        put(KEY_ITEMS, obj);
        return this;
    }

    /**
     * key转换成小写
     * @param recursion 是否递归
     * @param keys keys
     * @return DataRow
     */
    @Override
    public DataRow toLowerKey(boolean recursion, String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                Object value = get(key);
                if(recursion) {
                    if (value instanceof AnyData) {
                        value = ((AnyData) value).toLowerKey(recursion, keys);
                    } else if (value instanceof Collection) {
                        Collection list = (Collection) value;
                        for(Object item:list) {
                            if(item instanceof AnyData) {
                                ((AnyData)item).toLowerKey(recursion, keys);
                            }
                        }
                    }
                }
                remove(keyAdapter.key(key));
                key = key.toLowerCase();
                put(KEY_CASE.SRC, key, value);
            }
        } else {
            for (String key : keys()) {
                Object value = get(key);
                if(recursion) {
                    if (value instanceof AnyData) {
                        value = ((AnyData) value).toLowerKey(recursion, keys);
                    } else if (value instanceof Collection) {
                        Collection list = (Collection) value;
                        for(Object item:list) {
                            if(item instanceof AnyData) {
                                ((AnyData)item).toLowerKey(recursion, keys);
                            }
                        }
                    }
                }
                remove(keyAdapter.key(key));
                key = key.toLowerCase();
                put(KEY_CASE.SRC, key, value);
            }
        }
        parseKeyCase(KEY_CASE.LOWER);
        return this;
    }
    /**
     * key转换成大写
     * @param recursion 是否递归
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return DataRow
     */
    @Override
    public DataRow toUpperKey(boolean recursion, String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                Object value = get(key);
                remove(keyAdapter.key(key));
                key = key.toUpperCase();
                if(recursion) {
                    if (value instanceof AnyData) {
                        value = ((AnyData) value).toUpperKey(recursion, keys);
                    } else if (value instanceof Collection) {
                        Collection list = (Collection) value;
                        for(Object item:list) {
                            if(item instanceof AnyData) {
                                ((AnyData)item).toUpperKey(recursion, keys);
                            }
                        }
                    }
                }
                put(KEY_CASE.SRC, key, value);
            }
        } else {
            for (String key : keys()) {
                Object value = get(key);
                remove(keyAdapter.key(key));
                key = key.toUpperCase();
                if(recursion) {
                    if (value instanceof AnyData) {
                        value = ((AnyData) value).toUpperKey(recursion, keys);
                    } else if (value instanceof Collection) {
                        Collection list = (Collection) value;
                        for(Object item:list) {
                            if(item instanceof AnyData) {
                                ((AnyData)item).toUpperKey(recursion, keys);
                            }
                        }
                    }
                }
                put(KEY_CASE.SRC, key, value);
            }
        }
        parseKeyCase(KEY_CASE.UPPER);
        return this;
    }

    /**
     * value转换成小写
     * @param keys keys
     * @return DataRow
     */
    public DataRow toLowerValue(String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                Object value = get(key);
                if(value instanceof String) {
                    put(KEY_CASE.SRC, key, ((String) value).toLowerCase());
                }
            }
        } else {
            for (String key : keys()) {
                Object value = get(key);
                if(value instanceof String) {
                    put(KEY_CASE.SRC, key, ((String) value).toLowerCase());
                }
            }
        }
        return this;
    }

    /**
     * value转换成大写
     * @param keys keys
     * @return DataRow
     */
    public DataRow toUpperValue(String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                Object value = get(key);
                if(value instanceof String) {
                    put(KEY_CASE.SRC, key, ((String) value).toUpperCase());
                }
            }
        } else {
            for (String key : keys()) {
                Object value = get(key);
                if(value instanceof String) {
                    put(KEY_CASE.SRC, key, ((String) value).toUpperCase());
                }
            }
        }
        return this;
    }

    /**
     * 指定列是否为空
     * @param key key
     * @return boolean
     */
    public boolean isNull(String key) {
        Object obj = get(key);
        return obj == null;
    }

    public boolean isNotNull(String key) {
        return !isNull(key);
    }

    public boolean isEmpty(boolean recursion, String key) {
        Object obj = get(key);
        return BasicUtil.isEmpty(recursion, obj);
    }

    public boolean isEmpty(String key) {
        Object obj = get(key);
        return BasicUtil.isEmpty(obj);
    }

    public boolean isNotEmpty(String key) {
        return !isEmpty(key);
    }

    /**
     * 添加主键
     * @param applyContainer 是否应用到上级容器 默认false
     * @param pks pks
     * @return DataRow
     */
    public DataRow addPrimaryKey(boolean applyContainer, String... pks) {
        if (null != pks) {
            List<String> list = new ArrayList<>();
            for (String pk : pks) {
                list.add(pk);
            }
            return addPrimaryKey(applyContainer, list);
        }
        return this;
    }

    public DataRow addPrimaryKey(String... pks) {
        return addPrimaryKey(false, pks);
    }

    public DataRow addPrimaryKey(boolean applyContainer, Collection<String> pks) {
        if (BasicUtil.isEmpty(pks)) {
            return this;
        }

        /*没有处于容器中时, 设置自身主键*/
        if (null == primaryKeys) {
            primaryKeys = new ArrayList<>();
        }
        for (String item : pks) {
            if (BasicUtil.isEmpty(item)) {
                continue;
            }
            item = keyAdapter.key(item);
            if (!primaryKeys.contains(item)) {
                primaryKeys.add(item);
            }
        }
        /*设置容器主键*/
        if (hasContainer() && applyContainer) {
            getContainer().setPrimaryKey(false, primaryKeys);
        }
        return this;
    }

    public DataRow setPrimaryKey(boolean applyContainer, String... pks) {
        if (null != pks) {
            List<String> list = new ArrayList<>();
            for (String pk : pks) {
                list.add(pk);
            }
            return setPrimaryKey(applyContainer, list);
        }
        return this;
    }

    public DataRow setPrimaryKey(String... pks) {
        return setPrimaryKey(false, pks);
    }

    /**
     * 设置主键
     * @param applyContainer 是否应用到上级容器
     * @param pks keys
     * @return DataRow
     */
    public DataRow setPrimaryKey(boolean applyContainer, Collection<String> pks) {
        if (BasicUtil.isEmpty(pks)) {
            return this;
        }
        /*设置容器主键*/
        if (hasContainer() && applyContainer) {
            getContainer().setPrimaryKey(pks);
        }

        if (null == this.primaryKeys) {
            this.primaryKeys = new ArrayList<>();
        } else {
            this.primaryKeys.clear();
        }
        return addPrimaryKey(applyContainer, pks);
    }

    public DataRow setPrimaryKey(Collection<String> pks) {
        return setPrimaryKey(false, pks);
    }

    public Column getPrimaryColumn() {
        LinkedHashMap<String, Column> columns = getPrimaryColumns();
        if(!columns.isEmpty()) {
            return columns.values().iterator().next();
        }
        String pk = getPrimaryKey();
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
                if(column.isPrimaryKey() == 1) {
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
     * 读取主键
     * 主键为空时且容器有主键时, 读取容器主键, 否则返回默认主键
     * @return List
     */
    public List<String> getPrimaryKeys() {
        /*有主键直接返回*/
        if (hasSelfPrimaryKeys()) {
            return primaryKeys;
        }

        /*处于容器中并且容器有主键, 返回容器主键*/
        if (hasContainer() && getContainer().hasPrimaryKeys()) {
            return getContainer().getPrimaryKeys();
        }

       /* *//*本身与容器都没有主键 返回默认主键*//*
        List<String> defaultPrimary = new ArrayList<>();
        String configKey = ConfigTable.DEFAULT_PRIMARY_KEY;
        if (null != configKey && !configKey.trim().equals("")) {
            defaultPrimary.add(configKey);
        }
         return defaultPrimary;*/
        return null;

    }

    public String getPrimaryKey() {
        List<String> keys = getPrimaryKeys();
        if (null != keys && !keys.isEmpty()) {
            return keys.get(0);
        }
        return null;
    }

    /**
     * 主键值
     * @return List
     */
    public List<Object> getPrimaryValues() {
        List<Object> values = new ArrayList<Object>();
        List<String> keys = getPrimaryKeys();
        if (null != keys) {
            for (String key : keys) {
                values.add(get(key));
            }
        }
        return values;
    }

    public Object getPrimaryValue() {
        String key = getPrimaryKey();
        if (null != key) {
            return get(key);
        }
        return null;
    }
    public DataRow setPrimaryValue(Object value) {
        String key = getPrimaryKey();
        if (null != key) {
            put(key, value);
        }
        return this;
    }
    /**
     * 是否有主键
     * @return boolean
     */
    public boolean hasPrimaryKeys() {
        if (hasSelfPrimaryKeys()) {
            return true;
        }
        if (null != getContainer()) {
            return getContainer().hasPrimaryKeys();
        }
        if (keys().contains(ConfigTable.DEFAULT_PRIMARY_KEY)) {
            return true;
        }
        return false;
    }

    /**
     * 自身是否有主键
     * @return boolean
     */
    public boolean hasSelfPrimaryKeys() {
        if (null != primaryKeys && !primaryKeys.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public String getCategory() {
        return category;
    }

    public DataRow setCategory(String category) {
        this.category = category;
        return this;
    }

    public DataRow getRelations() {
        return relations;
    }

    public DataRow setRelations(DataRow relations) {
        this.relations = relations;
        return this;
    }
    public DataRow addRelation(DataRow relation) {
        if(null == relations) {
            relations = new DataRow();
        }
        this.relations.put(relation.getCategory(), relation);
        return this;
    }
    public DataRow addRelation(String relation) {
        if(null == relations) {
            relations = new DataRow();
        }
        DataRow row = new DataRow();
        row.setCategory(relation);
        this.relations.put(relation, row);
        return this;
    }
    public DataRow getRelation(String key) {
        if(null == relations) {
            relations = new DataRow();
        }
        return (DataRow)relations.get(key);
    }

    public String getDataLink() {
        if (BasicUtil.isEmpty(datalink) && null != getContainer()) {
            return getContainer().getDatalink();
        }
        return datalink;
    }

    /**
     * 设置数据源
     * 当前对象处于容器中时, 设置容器数据源
     * @param dest  dest
     * @return DataRow
     */
    public DataRow setDest(String dest) {
        if (null == dest) {
            return this;
        }
        if (dest.contains(".") && !dest.contains(":")) {
            String[] tmps = dest.split("\\.");
            Catalog catalog = null;
            Schema schema = null;
            Table table = null;
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
            Table table = new Table(dest);
            setTable(table);
        }
        return this;
    }

    /**
     * 子类
     * @return Object
     */
    public Object getChildren() {
        return get(KEY_CHILDREN);
    }

    public DataRow setChildren(Object children) {
        put(KEY_CHILDREN, children);
        return this;
    }

    /**
     * 所有上级数据(递归)
     * @return List
     */
    @SuppressWarnings("unchecked")
  /*  public List<Object> getAllParent(String key) {
        if (null != get(KEY_ALL_PARENT)) {
            return (List<Object>) get(KEY_ALL_PARENT);
        }
        List<Object> parents = new ArrayList<Object>();
        Object parent = getParent(key);
        if (null != parent) {
            parents.add(parent);
            if (parent instanceof DataRow) {
                DataRow tmp = (DataRow) parent;
                parents.addAll(tmp.getAllParent(key));
            }
        }
        put(KEY_ALL_PARENT, parents);
        return parents;
    }

    public List<Object> getAllParent() {
        return getAllParent(KEY_PARENT);
    }*/
    public DataSet getAllChild(String key) {
        Object obj = get(KEY_ALL_CHILDREN);
        if (null != obj) {
            return (DataSet) obj;
        }
        DataSet set = new DataSet();
        DataSet childs = getSet(key);
        for(DataRow child:childs) {
            set.add(child);
            set.addAll(child.getAllChild(key));
        }
        put(KEY_ALL_CHILDREN, set);
        return set;
    }
    /**
     * 转换成对象
     * @param <T>  T
     * @param clazz  clazz
     * @param configs 属性对应关系  name:USER_NAME
     * @return T
     */
    public <T> T entity(Class<T> clazz, String... configs) {
        T entity = null;
        if (null == clazz) {
            return entity;
        }
        if (EntityAdapterProxy.hasAdapter(clazz)) {
            entity = EntityAdapterProxy.entity(clazz, this, metadatas);
            if (null != entity) {
                return entity;
            }
        }
        try {
            entity = (T) clazz.newInstance();
            /*读取类属性*/
            List<Field> fields = ClassUtil.getFields(clazz, false, false);
            for (Field field : fields) {
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                Object value = get(field.getName());
                /*属性赋值*/
                BeanUtil.setFieldValue(entity, field, value);
            }//end 自身属性

            if (null != configs) {
                for (String config : configs) {
                    String field = config;
                    String column = config;
                    if (config.contains(":")) {
                        String[] tmps = config.split(":");
                        if (tmps.length >= 2) {
                            field = tmps[0];
                            column = tmps[1];
                        }
                    }
                    Object value = get(column);
                    BeanUtil.setFieldValue(entity, field, value);
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    /**
     * 是否有指定的key
     * @param key key
     * @return boolean
     */
    public boolean has(String key) {
        return get(key) != null;
    }

    public boolean hasValue(String key) {
        return get(key) != null;
    }

    public boolean hasKey(String key) {
        return keySet().contains(keyAdapter.key(key));
    }

    public boolean containsKey(String key) {
        return keySet().contains(keyAdapter.key(key));
    }

    public List<String> keys() {
        List<String> keys = new ArrayList<>();
        for(String key:keySet()) {
            keys.add(key);
        }
        return keys;
    }
    @Override
    public void putAll(Map<? extends String, ? extends Object> map) {
        for(String key:map.keySet()) {
            put(key, map.get(key));
        }
    }
    protected void mapPut(String key, Object value) {
        super.put(key, value);
    }
    protected Object mapGet(String key) {
        return super.get(key);
    }
    public DataRow put(KEY_CASE keyCase, String key, Object value) {
        return put(true, keyCase, key, value);
    }
    public DataRow put(boolean checkUpdate, String key, Object value) {
        return put(checkUpdate, null, key, value);
    }

    public DataRow put(boolean checkUpdate, KEY_CASE keyCase, String key, Object value) {
        KeyAdapter keyAdapter = this.keyAdapter;
        if(null != keyCase) {
            keyAdapter = KeyAdapter.parse(keyCase);
        }
        if (null != key) {
            if(keyAdapter.getKeyCase() != KEY_CASE.SRC) {
                key = keyAdapter.key(key);
            }
            boolean ignore = false;
            if(checkUpdate) {
                if (key.startsWith("+")) {
                    key = key.substring(1);
                    addUpdateColumns(key);
                }else if (key.startsWith("-")) {
                    key = key.substring(1);
                    addIgnoreColumns(key);
                    ignore = true;
                }

                Object oldValue = get(keyCase, key);
                if (null == oldValue || !oldValue.equals(value)) {
                    mapPut(key, value);
                }
                if(null == value) {
                    if(isInsertNullColumn() || isUpdateNullColumn()) {
                        addUpdateColumns(key);
                    }
                }else if(BasicUtil.isEmpty(true, value)) {
                    if(isInsertEmptyColumn() || isUpdateEmptyColumn()) {
                        addUpdateColumns(key);
                    }
                }else {
                    if (!ignore && !BasicUtil.equals(oldValue, value)) {
                        addUpdateColumns(key);
                    }
                }
            }else{
                mapPut(key, value);
            }
            if (ignoreCase) {
                String ignoreKey = key.replace("_","").replace("-","").toUpperCase();
                keymap.put(ignoreKey, key);
            }
        }
        return this;
    }

    /**
     *
     * @param keyCase keyCase
     * @param key key
     * @param value value
     * @param checkUpdate 检测是否需要更新
     * @param pk        是否是主键 pk		是否是主键
     * @param override    是否覆盖之前的主键(追加到primaryKeys) 默认覆盖(单一主键)
     * @return DataRow
     */
    public DataRow put(boolean checkUpdate, KEY_CASE keyCase, String key, Object value, boolean pk, boolean override) {
        if (pk) {
            if (override) {
                primaryKeys.clear();
            }
            addPrimaryKey(key);
        }
        put(checkUpdate, keyCase, key, value);
        return this;
    }
    public DataRow put(KEY_CASE keyCase, String key, Object value, boolean pk, boolean override) {
       return put(true, keyCase, key, value, pk, override);
    }

    public DataRow put(String key, Object value, boolean pk, boolean override) {
        return put(null, key, value, pk, override);
    }

    public DataRow put(KEY_CASE keyCase, String key, Object value, boolean pk) {
        put(keyCase, key, value, pk, true);
        return this;
    }

    public DataRow put(String key, Object value, boolean pk) {
        put(null, key, value, pk, true);
        return this;
    }
    public KEY_CASE keyCase() {
        return this.keyCase;
    }
    public DataRow put(String key) {
        DataRow row = new DataRow(keyCase());
        put(key, row);
        return row;
    }
    public DataSet puts(String key) {
        DataSet set = new DataSet();
        put(key, set);
        return set;
    }
    /**
     * 这是重写的父类put不要改返回值类型
     * @param key key
     * @param value value
     * @return Object
     */
    @Override
    public Object put(String key, Object value) {
        put(null, key, value, false, true);
        return this;
    }
    public DataRow set(String key, Object value) {
        put(null, key, value, false, true);
        return this;
    }
    public DataRow putWithoutNull(String key, Object value) {
        if(null != value) {
            this.put(key, value);
        }
        return this;
    }
    public DataRow putWithoutEmpty(String key, Object value) {
        if(BasicUtil.isNotEmpty(value)) {
            this.put(key, value);
        }
        return this;
    }

    /**
     * 原来的值为空时执行
     * @param key key
     * @param value value
     * @return this
     */
    public DataRow putIfEmpty(String key, Object value) {
        if(isEmpty(key)) {
            this.put(key, value);
        }
        return this;
    }
    /**
     * 原来的值为null时执行
     * @param key key
     * @param value value
     * @return this
     */
    public DataRow putIfNull(String key, Object value) {
        if(isNull(key)) {
            this.put(key, value);
        }
        return this;
    }

    public DataRow attr(String key, Object value) {
        if(null == attributes) {
            attributes = new DataRow();
        }
        attributes.put(key, value);
        return this;
    }

    public DataRow setAttribute(String key, Object value) {
        if(null == attributes) {
            attributes = new DataRow();
        }
        attributes.put(key, value);
        return this;
    }

    public Object attr(String key) {
        if(null == attributes) {
            attributes = new DataRow();
        }
        return attributes.get(key);
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
    public DataRow setAttributes(DataRow attributes) {
        this.attributes = attributes;
        return this;
    }
    public DataRow tag(String key, Object value) {
        if(null == tags) {
            tags = new LinkedHashMap<>();
        }
        tags.put(key, value);
        return this;
    }

    public DataRow addTag(String key, Object value) {
        if(null == tags) {
            tags = new LinkedHashMap<>();
        }
        tags.put(key, value);
        return this;
    }

    public Object tag(String key) {
        if(null == tags) {
            tags = new LinkedHashMap<>();
        }
        return tags.get(key);
    }

    public Object getTag(String key) {
        if(null == tags) {
            tags = new LinkedHashMap<>();
        }
        return tags.get(key);
    }

    public LinkedHashMap<String, Object> getTags() {
        if(null == tags) {
            tags = new LinkedHashMap<>();
        }
        return tags;
    }

    public DataRow getRow(String key) {
        if (null == key) {
            if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
                return new DataRow();
            }
            return null;
        }
        Object obj = get(key);
        if (null != obj) {
            if(obj instanceof DataRow) {
                return (DataRow) obj;
            }else if(obj instanceof String) {
                try {
                    return DataRow.parseJson(obj.toString());
                }catch (Exception e) {
                    log.warn("{}>{}转换成DataRow失败", key, obj);
                }
            }else if(obj instanceof Map) {
                return new DataRow((Map)obj);
            }
        }
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return null;
    }

    public Point getPoint(String key) {
        if (null == key) {
            if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
                return new Point();
            }
            return null;
        }
        Object obj = get(key);
        if (null != obj && obj instanceof Point) {
            return (Point) obj;
        }
        if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new Point();
        }
        return null;
    }
    public DataRow getRow(String ... keys) {
        if (null == keys || keys.length == 0) {
            if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
                return new DataRow();
            }
            return null;
        }
        DataRow result = this;
        for(String key:keys) {
            if(null != result) {
                result = result.getRow(key);
            }else{
                if(ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
                    return new DataRow();
                }
                return null;
            }
        }
        if(null == result && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            return new DataRow();
        }
        return result;
    }
    public DataSet getSet(String key) {
        DataSet set = null;
        if (null != key) {
            Object obj = get(key);
            if (null != obj) {
                if (obj instanceof DataSet) {
                    set = (DataSet) obj;
                } else if (obj instanceof List) {
                    List<?> list = (List<?>) obj;
                    set = new DataSet();
                    for (Object item : list) {
                        set.add(DataRow.parse(item));
                    }
                } else if (obj instanceof String) {
                    set = DataSet.parseJson((String) obj);
                }
            }
        }
        if(null == set && ConfigTable.IS_RETURN_EMPTY_INSTANCE_REPLACE_NULL) {
            set = new DataSet();
        }
        return set;
    }

    public List<?> getList(String key) {
        if (null == key) {
            return null;
        }
        Object obj = get(key);
        if (null != obj) {
            if(obj instanceof List) {
                return (List<?>) obj;
            }else if(obj instanceof DataSet) {
                return ((DataSet)obj).getRows();
            }
        }
        return null;
    }

    public String getStringNvl(String key, String... defs) {
        String result = getString(key);
        if (null == result) {
            if (null == defs || defs.length == 0) {
                result = "";
            } else {
                result = BasicUtil.nvl(defs).toString();
            }
        }
        return result;
    }
    public String getStringEvl(String key, String... defs) {
        String result = getString(key);
        if (BasicUtil.isEmpty(result)) {
            if (null == defs || defs.length == 0) {
                result = "";
            } else {
                result = BasicUtil.evl(defs).toString();
            }
        }
        return result;
    }

    /**
     * 返回第1个非空值
     * @param keys keys
     * @return String
     */
    public String getStringWithoutEmpty(String ... keys) {
        if(null == keys) {
            return null;
        }
        String result = null;
        for(String key:keys) {
            if (null == key) {
                continue;
            }
            if (key.contains("${") && key.contains("}")) {
                result = BeanUtil.parseFinalValue(this, key);
            } else {
                if(!contains(key)) {
                    continue;
                }
                Object value = get(key);
                if (null != value) {
                    if(value instanceof byte[]) {
                        result = new String((byte[])value);
                    }else {
                        result = value.toString();
                    }
                }
            }
            if(BasicUtil.isNotEmpty(result)) {
                break;
            }
        }
        return result;
    }
    /**
     * 返回第1个非NULL值
     * @param keys keys
     * @return String
     */
    public String getStringWithoutNull(String ... keys) {
        if(null == keys) {
            return null;
        }
        String result = null;
        for(String key:keys) {
            if (null == key) {
                continue;
            }
            if (key.contains("${") && key.contains("}")) {
                result = BeanUtil.parseFinalValue(this, key);
            } else {
                if(!contains(key)) {
                    continue;
                }
                Object value = get(key);
                if (null != value) {
                    if(value instanceof byte[]) {
                        result = new String((byte[])value);
                    }else {
                        result = value.toString();
                    }
                }
            }
            if(null != result) {
                break;
            }
        }
        return result;
    }
    /**
     * 返回第1个存在的key对应的值, 有可能返回null或""
     * @param keys keys
     * @return String
     */
    public String getString(String ... keys) {
        if(null == keys) {
            return null;
        }
        String result = null;
        for(String key:keys) {
            if (null == key) {
                continue;
            }
            if (key.contains("${") && key.contains("}")) {
                result = BeanUtil.parseFinalValue(this, key);
            } else {
                if(!contains(key)) {
                    continue;
                }
                Object value = get(key);
                if (null != value) {
                    if(value instanceof byte[]) {
                        result = new String((byte[])value);
                    }else {
                        result = value.toString();
                    }
                }
            }
            break;
        }
        return result;
    }
    public String key(int index) {
        List<String> keys = keys();
        if(index < keys.size()) {
            return keys.get(index);
        }
        return null;
    }
    public String getString(int index) {
        return getString(key(index));
    }
    //为方便新人查询实现两次
    public String getString(String key) {
        if(null == key) {
            return null;
        }
        String result = null;
        if (key.contains("${") && key.contains("}")) {
            result = BeanUtil.parseFinalValue(this, key);
        } else {
            if(contains(key)) {
                Object value = get(key);
                if (null != value) {
                    if (value instanceof byte[]) {
                        result = new String((byte[]) value);
                    } else {
                        result = value.toString();
                    }
                }
            }
        }
        if(null == result && ConfigTable.IS_RETURN_EMPTY_STRING_REPLACE_NULL) {
            result = "";
        }
        return result;
    }

    /**
     * boolean类型true 解析成 1
     * @param keys key
     * @return int
     * @throws NumberFormatException 无效的数字格式
     */
    public Integer getInt(String ... keys) throws NumberFormatException {
        Object val = get(keys);
        if (val instanceof Boolean) {
            boolean bol = (Boolean) val;
            if (bol) {
                return 1;
            } else {
                return 0;
            }
        } else {
            Double dbl = getDouble(keys);
            if(null != dbl) {
                return dbl.intValue();
            }else{
                return null;
            }
        }
    }
    public Integer getInt(int index) throws NumberFormatException {
        return getInt(key(index));
    }
    public Integer getInt(String key) throws NumberFormatException {
        Object val = get(key);
        if (val instanceof Boolean) {
            boolean bol = (Boolean) val;
            if (bol) {
                return 1;
            } else {
                return 0;
            }
        } else {
            Double dbl = getDouble(key);
            if(null != dbl) {
                return dbl.intValue();
            }else{
                return null;
            }
        }
    }
    public Integer getInt(int index, Integer def) {
        return getInt(key(index), def);
    }
    public Integer getInt(String key, Integer def) {
        try {
            Integer result = getInt(key);
            if(null == result) {
                result = def;
            }
            return result;
        } catch (Exception e) {
            return def;
        }
    }
    public Integer getInt(Integer def, String ... keys) {
        Integer result = null;
        for(String key:keys) {
            result = getInt(key, null);
            if(null != result) {
                return result;
            }
        }
        return def;
    }

    public Double getDouble(String ... keys) throws NumberFormatException {
        Object value = get(keys);
        if(null != value) {
            return Double.parseDouble(value.toString());
        }
        return null;
    }
    public Double getDouble(int index) throws NumberFormatException {
        return getDouble(key(index));
    }
    public Double getDouble(String key) throws NumberFormatException {
        Object value = get(key);
        if(null != value) {
            return Double.parseDouble(value.toString());
        }
        return null;
    }

    public Double getDouble(int index, Double def) {
        return getDouble(key(index), def);
    }
    public Double getDouble(String key, Double def) {
        try {
            Double dbl = getDouble(key);
            if(null == dbl) {
                return def;
            }
            return dbl;
        } catch (Exception e) {
            return def;
        }
    }
    public Double getDouble(int index, Integer def) {
        return getDouble(key(index), def);
    }
    public Double getDouble(String key, Integer def) {
        if(null == def) {
            return getDouble(key, (Double)null);
        }
        return getDouble(key, def.doubleValue());
    }

    public Long getLong(String ... keys) throws NumberFormatException {
        Object value = get(keys);
        return BasicUtil.parseLong(value);
    }

    public Long getLong(int index) throws NumberFormatException {
        return getLong(key(index));
    }
    public Long getLong(String key) throws NumberFormatException {
        Object value = get(key);
        return BasicUtil.parseLong(value);
    }

    public Long getLong(int index, Long def) {
        return getLong(key(index), def);
    }
    public Long getLong(String key, Long def) {
        try {
            Long result = getLong(key);
            if(null == result) {
                result = def;
            }
            return result;
        } catch (Exception e) {
            return def;
        }
    }
    public Long getLong(int index, Integer def) {
        return getLong(key(index), def);
    }
    public Long getLong(String key, Integer def) {
        if(null == def) {
            return getLong(key, (Long)null);
        }
        return getLong(key, def.longValue());
    }

    public Float getFloat(String ... keys) throws NumberFormatException {
        Object value = get(keys);
        return Float.parseFloat(value.toString());
    }
    public Float getFloat(int index) throws NumberFormatException {
        return getFloat(key(index));
    }
    public Float getFloat(String key) throws NumberFormatException {
        Object value = get(key);
        return Float.parseFloat(value.toString());
    }

    public Float getFloat(int index, Float def) {
        return getFloat(key(index), def);
    }
    public Float getFloat(String key, Float def) {
        try {
            Float result = getFloat(key);
            if(null == result) {
                result = def;
            }
            return result;
        } catch (Exception e) {
            return def;
        }
    }
    public Float getFloat(int index, Integer def) {
        return getFloat(key(index), def);
    }
    public Float getFloat(String key, Integer def) {
        if(null == def) {
            return getFloat(key, (Float)null);
        }
        return getFloat(key, def.floatValue());
    }

    public Boolean getBoolean(int index, Boolean def) {
        return getBoolean(key(index), def);
    }
    public Boolean getBoolean(String key, Boolean def) {
        return BasicUtil.parseBoolean(getString(key), def);
    }

    public Boolean getBoolean(String ... keys)  {
        return BasicUtil.parseBoolean(getString(keys));
    }

    public Boolean getBoolean(int index) {
        return getBoolean(key(index));
    }
    public Boolean getBoolean(String key) {
        return BasicUtil.parseBoolean(getString(key));
    }

    public BigDecimal getDecimal(String ... keys) throws NumberFormatException {
        return new BigDecimal(getString(keys));
    }

    public BigDecimal getDecimal(int index) throws NumberFormatException {
        return getDecimal(key(index));
    }
    public BigDecimal getDecimal(String key) throws NumberFormatException {
        return new BigDecimal(getString(key));
    }

    public BigDecimal getDecimal(int index, Double def) {
        return getDecimal(key(index), def);
    }
    public BigDecimal getDecimal(String key, Double def) {
        return getDecimal(key, new BigDecimal(def));
    }

    public BigDecimal getDecimal(int index, Integer def) {
        return getDecimal(key(index), def);
    }
    public BigDecimal getDecimal(String key, Integer def) {
        return getDecimal(key, new BigDecimal(def));
    }

    public BigDecimal getDecimal(int index, BigDecimal def) {
        return getDecimal(key(index), def);
    }
    public BigDecimal getDecimal(String key, BigDecimal def) {
        try {
            BigDecimal result = getDecimal(key);
            if (null == result) {
                return def;
            }
            return result;
        } catch (Exception e) {
            return def;
        }
    }

    public String getDecimal(String key, String format) {
        BigDecimal result = getDecimal(key);
        return NumberUtil.format(result, format);
    }

    public String getDecimal(int index, String format) {
        return getDecimal(key(index), format);
    }

    public String getDecimal(String key, double def, String format) {
        return getDecimal(key, new BigDecimal(def), format);
    }

    public String getDecimal(int index, double def, String format) {
        return getDecimal(key(index), def, format);
    }

    public String getDecimal(int index, BigDecimal def, String format) {
        return getDecimal(key(index), def, format);
    }
    public String getDecimal(String key, BigDecimal def, String format) {
        BigDecimal result = null;
        try {
            result = getDecimal(key);
            if (null == result) {
                result = def;
            }
        } catch (Exception e) {
            result = def;
        }
        return NumberUtil.format(result, format);
    }

    public Date getDate(int index, Date def) {
        return getDate(key(index), def);
    }
    public Date getDate(String key, Date def) {
        Date result = def;
        Object date = get(key);
        if (null == date) {
            return result;
        }

        if (date instanceof Date) {
            result = (Date) date;
        }else {
            result = DateUtil.parse(date, def);
        }

        if(null == result) {
            result = def;
        }
        return result;
    }

    public Date getDate(int index, String def) throws Exception {
        return getDate(key(index), def);
    }
    public Date getDate(String key, String def) throws Exception {
        try {
            return getDate(key);
        } catch (Exception e) {
            return DateUtil.parse(def);
        }
    }

    public Date getDate(String ... keys) throws Exception {
        return DateUtil.parse(get(keys));
    }
    public Date getDate(int index) throws Exception {
        return getDate(key(index));
    }
    public Date getDate(String key) throws Exception {
        return DateUtil.parse(get(key));
    }
    public byte[] getBytes(int index) {
        return getBytes(key(index));
    }

    public byte[] getBytes(String key) {
        return (byte[]) get(key);
    }

    /**
     * 超长部分忽略
     * @param length 最长显示长度
     * @param columns 检测列
     * @return this
     */
    public DataRow ellipsis(int length, String ... columns) {
        for(String column:columns) {
            String value = getString(column);
            if(null != value) {
                value = BasicUtil.ellipsis(length, value);
                put(column, value);
            }
        }
        return this;
    }
    /**
     * {id:1, code:a, value:100}<br/>
     * toSet("k","v")转换成<br/>
     * [{k:id, v:1}, {k:code, v:a}, [k:value, v:100]]
     * @param key 原map中的key存放位置
     * @param value 原map中的value存放位置
     * @return DataSet
     */
    public DataSet toSet(String key, String value) {
        DataSet set = new DataSet();
        for(String k:keySet()) {
            DataRow row = new DataRow();
            row.put(key, k);
            row.put(value, get(k));
            set.add(row);
        }
        return set;
    }
    /**
     * 转换成json格式
     * @return String
     */
    public String toJSON() {
        return BeanUtil.map2json(this);
    }
    public String json() {
        return toJSON();
    }

    public String toJson() {
        return toJSON();
    }

    public String getJson() {
        return BeanUtil.map2json(this);
    }
    public String toJSON(JsonInclude.Include include) {
        return BeanUtil.map2json(this, include);
    }

    public String toJson(JsonInclude.Include include) {
        return toJSON(include);
    }

    public String getJson(JsonInclude.Include include) {
        return BeanUtil.map2json(this, include);
    }

    public DataRow removeEmpty(String... keys) {
        return removeEmpty(false, keys);
    }
    public DataRow removeEmpty(boolean recursion, String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                if (this.isEmpty(recursion, key)) {
                    this.remove(key);
                }
            }
        } else {
            List<String> cols = keys();
            for (String key : cols) {
                if (this.isEmpty(recursion, key)) {
                    this.remove(key);
                }
            }
        }
        return this;
    }

    public DataRow removeNull(String... keys) {
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                if (null == this.get(key)) {
                    this.remove(key);
                }
            }
        }
        List<String> cols = keys();
        for (String key : cols) {
            if (null == this.get(key)) {
                this.remove(key);
            }
        }
        return this;
    }
    public boolean equals(DataRow row, String ... columns) {
        if(null == row || null == columns || columns.length == 0) {
            return false;
        }
        for(String column:columns) {
            String v1 = getString(column);
            String v2 = row.getString(column);
            if(!BasicUtil.equals(v1, v2)) {
                return false;
            }
        }
        return true;
    }
    /**
     * 轮换成xml格式
     * @return String
     */
    public String toXML() {
        return BeanUtil.map2xml(this);
    }

    public String toXML(boolean border, boolean order) {
        return BeanUtil.map2xml(this, border, order);
    }

    /**
     * 是否处于容器内
     * @return boolean
     */
    public boolean hasContainer() {
        if (null != getContainer()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 包含当前对象的容器
     * @return DataSet
     */
    public DataSet getContainer() {
        return container;
    }

    public DataRow setContainer(DataSet container) {
        this.container = container;
        return this;
    }

    public Catalog getCatalog() {
        if (null != catalog) {
            return catalog;
        } else {
            DataSet container = getContainer();
            if (null != container) {
                return container.getCatalog();
            } else {
                return null;
            }
        }
    }

    public String getCatalogName() {
        if (null != catalog) {
            return catalog.getName();
        } else {
            DataSet container = getContainer();
            if (null != container) {
                return container.getCatalogName();
            } else {
                return null;
            }
        }
    }

    public DataRow setCatalog(String catalog) {
        if(BasicUtil.isNotEmpty(catalog)) {
            this.catalog = new Catalog(catalog);
        }else{
            this.catalog = null;
        }
        return this;
    }

    public DataRow setCatalog(Catalog catalog) {
        this.catalog = catalog;
        return this;
    }

    public Schema getSchema() {
        if (null != schema) {
            return schema;
        } else {
            DataSet container = getContainer();
            if (null != container) {
                return container.getSchema();
            } else {
                return null;
            }
        }
    }

    public String getSchemaName() {
        if (null != schema) {
            return schema.getName();
        } else {
            DataSet container = getContainer();
            if (null != container) {
                return container.getSchemaName();
            } else {
                return null;
            }
        }
    }

    public DataRow setSchema(String schema) {
        if(BasicUtil.isNotEmpty(schema)) {
            this.schema = new Schema(schema);
        }else{
            this.schema = null;
        }
        return this;
    }

    public DataRow setSchema(Schema schema) {
        this.schema = schema;
        return this;
    }

    public Table getTable() {
        return getTable(true);
    }

    public Table getTable(boolean checkContainer) {
        if (null != tables && !tables.isEmpty()) {
            return tables.values().iterator().next();
        } else if(checkContainer) {
            DataSet container = getContainer();
            if (null != container) {
                return container.getTable(false);
            } else {
                return null;
            }
        }else{
            return null;
        }
    }
    public String getTableName() {
        Table table = getTable();
        if(null != table) {
            return table.getName();
        }
        return null;
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
        if(null == dest && null != container) {
            dest = container.getDest();
        }
        return dest;
    }
    public DataRow setTable(Table table) {
        tables = new LinkedHashMap<>();
        addTable(table);
        return this;
    }
    public LinkedHashMap<String, Table> getTables() {
        return getTables(true);
    }
    public LinkedHashMap<String, Table> getTables(boolean checkContainer) {
        if (null != tables && !tables.isEmpty()) {
            return tables;
        } else if(checkContainer) {
            DataSet container = getContainer();
            if (null != container) {
                return container.getTables(false);
            } else {
                return tables;
            }
        }
        return tables;
    }
    public DataRow setTables(List<Table> tables) {
        this.tables = new LinkedHashMap<>();
        for(Table table:tables) {
            addTable(table);
        }
        return this;
    }

    public DataRow setTables(LinkedHashMap<String, Table> tables) {
        this.tables = tables;
        return this;
    }
    public DataRow addTable(Table table) {
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

    public DataRow setTable(String table) {
        setDest(table);
        return this;
    }

    /**
     * 验证是否过期
     * 根据当前时间与创建时间对比
     * 过期返回 true
     * @param millisecond    过期时间(毫秒) millisecond
     * @return boolean
     */
    public boolean isExpire(int millisecond) {
        if (System.currentTimeMillis() - createTime > millisecond) {
            return true;
        }
        return false;
    }

    /**
     * 是否过期
     * @param millisecond millisecond
     * @return boolean
     */
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

    /**
     * 复制数据
     * @return Object
     */
    public DataRow clone() {
        DataRow clone = null;
        try{
            clone = (DataRow) super.clone();
        }catch (Exception e) {
            clone = new DataRow();
        }
        clone.container = this.container;
        clone.primaryKeys = this.primaryKeys;
        clone.datasource = this.datasource;
        clone.schema = this.schema;
        clone.tables = this.tables;
        clone.createTime = this.createTime;
        clone.nanoTime = this.nanoTime;
        clone.isNew = this.isNew;
        return clone;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public DataRow setIsNew(Boolean isNew) {
        this.isNew = isNew;
        return this;
    }

    public List<String> getUpdateColumns() {
        //不要过滤主键，在上层方法根据情况过滤
        return updateColumns;
    }
    public LinkedHashMap<String, Column> getUpdateColumns(boolean metadata) {
        LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
        List<String> cols = getUpdateColumns();
        if(null != cols) {
            for (String column : cols) {
                Column col = null;
                if (null != metadatas) {
                    col = metadatas.get(column.toUpperCase());
                }
                if (null == col) {
                    col = new Column(column);
                }
                columns.put(column.toUpperCase(), col);
            }
        }
        return columns;
    }
    public LinkedHashMap<String, Column> getColumns() {
        LinkedHashMap<String, Column> columns = new LinkedHashMap<>();
        for(String key:keySet()) {
            Column column = null;
            if(null != metadatas) {
                column = metadatas.get(key.toUpperCase());
            }
            if(null == column) {
                column = new Column(key);
            }
            columns.put(key.toUpperCase(), column);
        }
        return columns;
    }

    public List<String> getIgnoreUpdateColumns() {
        return ignoreUpdateColumns;
    }

    /**
     * 删除指定的key
     * 不和remove命名 避免调用remoate("ID","CODE")时与HashMap.remove(Object key, Object value) 冲突
     * @param keys keys
     * @return DataRow
     */
    public DataRow removes(String... keys) {
        if (null != keys) {
            for (String key : keys) {
                if (null != key) {
                    key = keyAdapter.key(key);
                    super.remove(key);
                    updateColumns.remove(key);
                }
            }
        }
        return this;
    }

    public DataRow removes(List<String> keys) {
        if (null != keys) {
            for (String key : keys) {
                if (null != key) {
                    key = keyAdapter.key(key);
                    super.remove(key);
                    updateColumns.remove(key);
                }
            }
        }
        return this;
    }

    public Object remove(Object key) {
        if (null == key) {
            return null;
        }
        updateColumns.remove( keyAdapter.key(key.toString()));
        return super.remove( keyAdapter.key(key.toString()));
    }

    /**
     * 清空需要更新的列
     * @return DataRow
     */
    public DataRow clearUpdateColumns() {
        updateColumns.clear();
        return this;
    }

    public DataRow clearIgnoreUpdateColumns() {
        ignoreUpdateColumns.clear();
        return this;
    }

    public DataRow removeUpdateColumns(String... cols) {
        if (null != cols) {
            for (String col : cols) {
                String key =  keyAdapter.key(col);
                updateColumns.remove(key);
                ignoreUpdateColumns.add(key);
            }
        }
        return this;
    }

    /**
     * 添加需要更新的列
     * @param cols cols
     * @return DataRow
     */
    public DataRow addUpdateColumns(String... cols) {
        if (null != cols) {
            for (String col : cols) {
                String key =  keyAdapter.key(col);
                if (!updateColumns.contains(key)) {
                    updateColumns.add(key);
                    ignoreUpdateColumns.remove(key);
                }
            }
        }
        return this;
    }
    public DataRow addIgnoreColumns(String... cols) {
        if (null != cols) {
            for (String col : cols) {
                String key =  keyAdapter.key(col);
                if (!ignoreUpdateColumns.contains(key)) {
                    ignoreUpdateColumns.add(key);
                    updateColumns.remove(key);
                }
            }
        }
        return this;
    }

    public DataRow addAllUpdateColumns() {
        updateColumns.clear();
        updateColumns.addAll(keys());
        ignoreUpdateColumns.clear();
        return this;
    }

    /**
     * 将数据从data中复制到this
     * @param regex 是否开启正则匹配
     * @param data data
     * @param fixs fixs
     * @param keys this与data中的key不同时 "this.key:data.key"(CD:ORDER_CD)
     * @return DataRow
     */
    public DataRow copy(boolean regex, DataRow data, String[] fixs, String... keys) {
        return copy(data, BeanUtil.array2list(fixs, keys));
    }

    public DataRow copy(boolean regex, DataRow data, String... keys) {
        if (null == data) {
            return this;
        }
        if (null == keys || keys.length == 0) {
            return copy(data, data.keys());
        } else {
            return copy(data, BeanUtil.array2list(keys));
        }

    }
    public DataRow copy(boolean regex, DataRow data, List<String> fixs, String... keys) {
        if (null == data || data.isEmpty()) {
            return this;
        }
        List<String> list = BeanUtil.merge(fixs, keys);
        for (String key : list) {
            String ks[] = BeanUtil.parseKeyValue(key);
            if (null != ks && ks.length > 1) {
                put(ks[0], data.get(ks[1]));
            } else {
                put(key, data.get(key));
            }
        }
        return this;
    }

    public DataRow copy(DataRow data, String[] fixs, String... keys) {
        return copy(false, data, fixs, keys);
    }

    public DataRow copy(DataRow data, String... keys) {
        return copy(false, data, keys);
    }
    public DataRow copy(DataRow data, List<String> fixs, String... keys) {
        return copy(false, data, fixs, keys);
    }
    /**
    /**
     * 抽取指定列, 生成新的DataRow, 新的DataRow只包括指定列的值, 不包含其他附加信息(如来源表)
     * @param keys keys
     * @param regex 是否开启正则匹配
     * @return DataRow
     */
    public DataRow extract(boolean regex, String... keys) {
        DataRow result = new DataRow();
        result.copy(regex, this, keys);
        return result;
    }

    public DataRow extract(boolean regex, List<String> keys) {
        DataRow result = new DataRow();
        result.copy(regex, this, keys);
        return result;
    }

    public DataRow extract(String... keys) {
        return extract(false, keys);
    }

    public DataRow extract(List<String> keys) {
       return extract(false, keys);
    }

    /**
     * 复制String类型数据
     * @param data data
     * @param keys keys
     * @return DataRow
     */
    public DataRow copyString(DataRow data, String... keys) {
        if (null == data || null == keys) {
            return this;
        }
        for (String key : keys) {
            String ks[] = BeanUtil.parseKeyValue(key);
            Object obj = data.get(ks[1]);
            if (BasicUtil.isNotEmpty(obj)) {
                this.put(ks[0], obj.toString());
            } else {
                this.put(ks[0], null);
            }
        }
        return this;
    }

    /**
     * 所有数字列
     * @return List
     */
    public List<String> numberKeys() {
        List<String> result = new ArrayList<>();
        List<String> keys = keys();
        for (String key : keys) {
            if (get(key) instanceof Number) {
                result.add(key);
            }
        }
        return result;
    }
    /**
     * 检测必选项
     * @param keys keys
     * @return boolean
     */
    public boolean checkRequired(String... keys) {
        List<String> ks = new ArrayList<>();
        if (null != keys && keys.length > 0) {
            for (String key : keys) {
                ks.add(key);
            }
        }
        return checkRequired(ks);
    }

    public boolean checkRequired(List<String> keys) {
        if (null != keys) {
            for (String key : keys) {
                if (isEmpty(key)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 驼峰转下划线
     * @return DataRow
     */
    public DataRow camel_() {
        List<String> keys = keys();
        for (String key : keys) {
            Object value = get(key);
            String camel_ = BeanUtil.camel_(key);
            updateColumns.remove(key);
            super.remove(key);
            put(KEY_CASE.SRC, camel_, value);
        }
        this.setKeyCase(KEY_CASE.SRC);
        return this;
    }
    public DataRow camel() {
        return camel(false);
    }
    /**
     * 下划线转小驼峰
     * @param lower 是否先转换成小写 遇到全大写但没有下划线的情况 false:不处理 true:全部转成小写
     * @return DataSet
     */
    public DataRow camel(boolean lower) {
        if(lower) {
            toLowerKey();
        }
        List<String> keys = keys();
        for (String key : keys) {
            Object value = get(key);
            String camel = BeanUtil.camel(key);
            updateColumns.remove(key);
            super.remove(key);
            put(KEY_CASE.SRC, camel, value);
        }
        this.setKeyCase(KEY_CASE.camel);
        return this;
    }

    /**
     * 下划线转大驼峰
     * @return DataSet
     */
    public DataRow Camel() {
        List<String> keys = keys();
        for (String key : keys) {
            Object value = get(key);
            String Camel = BeanUtil.Camel(key);
            updateColumns.remove(key);
            super.remove(key);
            put(KEY_CASE.SRC, Camel, value);
        }
        this.setKeyCase(KEY_CASE.Camel);
        return this;
    }

    /**
     * key大小写转换
     * @param keyCase keyCase
     * @param key key
     * @return String
     */
    protected static String getKeyCase(KEY_CASE keyCase, String key) {
        if (null == key || keyCase == KEY_CASE.SRC) {
            return key;
        }
        if (keyCase == KEY_CASE.LOWER) {
            key = key.toLowerCase();
        } else if (keyCase == KEY_CASE.UPPER) {
            key = key.toUpperCase();
        } else if (keyCase == KEY_CASE.Camel) {
            key = BeanUtil.Camel(key);
        } else if (keyCase == KEY_CASE.camel) {
            key = BeanUtil.camel(key);
        }
        // else if(keyCase.getCode().contains("_")) {
//			// 驼峰转下划线
//			key = BeanUtil.camel_(key);
//			if(keyCase == KEY_CASE.CAMEL_CONFIG) {
//					if(ConfigTable.IS_UPPER_KEY) {
//						key = key.toUpperCase();
//					}
//					if(ConfigTable.IS_LOWER_KEY) {
//						key = key.toLowerCase();
//					}
//			}else if(keyCase == KEY_CASE.CAMEL_LOWER) {
//				key = key.toLowerCase();
//			}else if(keyCase == KEY_CASE.CAMEL_UPPER) {
//				key = key.toUpperCase();
//			}
//		}
        return key;
    }

    public static String getKeyCase(String key) {
        return getKeyCase(KEY_CASE.CONFIG, key);
    }


    /**
     * key大小写转换
     * @param keyCase keyCase
     * @param key key
     * @return String
     */
    protected static String putKeyCase(KEY_CASE keyCase, String key) {
        if (null == key || keyCase == KEY_CASE.SRC) {
            return key;
        }
        if (keyCase == KEY_CASE.UPPER) {
            key = key.toUpperCase();
        } else if (keyCase == KEY_CASE.LOWER) {
            key = key.toLowerCase();
        } else if (keyCase == KEY_CASE.Camel) {
            key = BeanUtil.Camel(key);
        } else if (keyCase == KEY_CASE.camel) {
            key = BeanUtil.camel(key);
        } else if (keyCase.getCode().contains("_")) {
            // 驼峰转下划线
            key = BeanUtil.camel_(key);
            if (keyCase == KEY_CASE.CAMEL_CONFIG) {
                if (ConfigTable.IS_UPPER_KEY) {
                    key = key.toUpperCase();
                }
                if (ConfigTable.IS_LOWER_KEY) {
                    key = key.toLowerCase();
                }
            } else if (keyCase == KEY_CASE.CAMEL_LOWER) {
                key = key.toLowerCase();
            } else if (keyCase == KEY_CASE.CAMEL_UPPER) {
                key = key.toUpperCase();
            }
        }
        return key;
    }

    public static String putKeyCase(String key) {
        return putKeyCase(KEY_CASE.CONFIG, key);
    }


    /**
     * 是否更新null列
     * @return boolean
     */
    public boolean isUpdateNullColumn() {
        return updateNullColumn;
    }

    /**
     * 设置是否更新null列
     * @param updateNullColumn updateNullColumn
     * @return DataRow
     */
    public DataRow setUpdateNullColumn(boolean updateNullColumn) {
        this.updateNullColumn = updateNullColumn;
        return this;
    }

    /**
     * 是否更新空列
     * @return boolean
     */
    public boolean isUpdateEmptyColumn() {
        return updateEmptyColumn;
    }

    /**
     * 设置是否更新空列
     * @param updateEmptyColumn updateEmptyColumn
     * @return DataRow
     */
    public DataRow setUpdateEmptyColumn(boolean updateEmptyColumn) {
        this.updateEmptyColumn = updateEmptyColumn;
        return this;
    }

    public boolean isInsertNullColumn() {
        return insertNullColumn;
    }

    public void setInsertNullColumn(boolean insertNullColumn) {
        this.insertNullColumn = insertNullColumn;
    }

    public boolean isInsertEmptyColumn() {
        return insertEmptyColumn;
    }
    public boolean isReplaceEmptyNull() {
        return replaceEmptyNull;
    }
    public DataRow setReplaceEmptyNull(boolean val) {
        replaceEmptyNull = val;
        return this;
    }

    public void setInsertEmptyColumn(boolean insertEmptyColumn) {
        this.insertEmptyColumn = insertEmptyColumn;
    }

    /**
     * 替换key
     * @param key key
     * @param target target
     * @param remove 是否删除原来的key
     * @return DataRow
     */
    public DataRow changeKey(String key, String target, boolean remove) {
        if(null == key || null == target) {
            return this;
        }
        if(keyAdapter.key(key).equals(keyAdapter.key(target))) {
            return this;
        }
        put(target, get(key));
        if (remove && !target.equalsIgnoreCase(key)) {
            remove(keyAdapter.key(key));
        }
        return this;
    }

    public DataRow changeKey(String key, String target) {
        return changeKey(key, target, true);
    }

    /**
     * 替换所有空值
     * @param value value
     * @return DataRow
     */
    public DataRow replaceEmpty(String value) {
        List<String> keys = keys();
        for (String key : keys) {
            if (isEmpty(key)) {
                put(KEY_CASE.SRC, key, value);
            }
        }
        return this;
    }

    /**
     * 所有String类型的值执行trim
     * @return this
     */
    public DataRow trim() {
        List<String> keys = keys();
        for (String key : keys) {
            Object value = get(key);
            if (value instanceof String) {
                put(KEY_CASE.SRC, key, ((String)value).trim());
            }
        }
        return this;
    }
    public DataRow trim(String ... keys) {
        for (String key : keys) {
            Object value = get(key);
            if (value instanceof String) {
                put(KEY_CASE.SRC, key, ((String)value).trim());
            }
        }
        return this;
    }

    /**
     * 多个空白压缩成一个空格
     * @return DataRow
     */
    public DataRow compress() {
        for(String key:keySet()) {
            Object value = get(key);
            if (value instanceof String) {
                put(KEY_CASE.SRC, key, BasicUtil.compress((String)value));
            }
        }
        return this;
    }
    public DataRow compress(String ... keys) {
        for(String key:keys) {
            Object value = get(key);
            if (value instanceof String) {
                put(KEY_CASE.SRC, key, BasicUtil.compress((String)value));
            }
        }
        return this;
    }

    /**
     * 全角转半角
     * @return this
     */
    public DataRow sbc2dbc() {
        for(String key:keySet()) {
            Object value = get(key);
            if (value instanceof String) {
                put(KEY_CASE.SRC, key, CharUtil.sbc2dbc((String)value));
            }
        }
        return this;
    }
    public DataRow sbc2dbc(String ... keys) {
        for(String key:keys) {
            Object value = get(key);
            if (value instanceof String) {
                put(KEY_CASE.SRC, key, CharUtil.sbc2dbc((String)value));
            }
        }
        return this;
    }
    /**
     * 替换所有空值
     * @param keys keys
     * @param replace replace
     * @return DataRow
     */
    public DataRow replaceEmpty(String replace, String ... keys) {
        List<String> ks = null;
        if(null == keys || keys.length ==0) {
            ks = keys();
        }else{
            ks = BeanUtil.array2list(keys);
        }
        for(String key:ks) {
            if (isEmpty(key)) {
                put(key, replace);
            }
        }
        return this;
    }

    /**
     * 替换所有NULL值
     * @param keys keys
     * @param replace replace
     * @return DataRow
     */
    public DataRow replaceNull(String replace, String ... keys) {
        List<String> ks = null;
        if(null == keys || keys.length ==0) {
            ks = keys();
        }else{
            ks = BeanUtil.array2list(keys);
        }
        for(String key:ks) {
            if (null == get(key)) {
                put(key, replace);
            }
        }
        return this;
    }

    public DataRow replaces(String oldChar, String replace, String ... keys) {
        List<String> ks = null;
        if(null == keys || keys.length ==0) {
            ks = keys();
        }else{
            ks = BeanUtil.array2list(keys);
        }
        if (null == replace) {
            replace = "";
        }
        for (String key : ks) {
            Object value = get(key);
            if (value != null && value instanceof String) {
                put(key, ((String) value).replace(oldChar, replace));
            }
        }
        return this;
    }

    public DataRow replaces(boolean regex, String oldChar, String replace, String ... keys) {
        if(regex) {
            return replaceRegex(oldChar, replace, keys);
        }else{
            return replaces(oldChar, replace, keys);
        }
    }
    public DataRow replaceRegex(String regex, String replace, String ... keys) {
        List<String> ks = null;
        if(null == keys || keys.length ==0) {
            ks = keys();
        }else{
            ks = BeanUtil.array2list(keys);
        }
        if (null == replace) {
            replace = "";
        }
        for (String key : ks) {
            Object value = get(key);
            if (value != null && value instanceof String) {
                put(key, ((String) value).replaceAll(regex, replace));
            }
        }
        return this;
    }
    /**
     * 拼接value
     * @param keys keys
     * @return String
     */
    public String join(String... keys) {
        String result = "";
        if (null != keys) {
            for (String key : keys) {
                String val = getString(key);
                if (BasicUtil.isNotEmpty(val)) {
                    if (result.isEmpty()) {
                        result = val;
                    } else {
                        result += "," + val;
                    }
                }
            }
        }
        return result;
    }
    public DataRow convertDate(String ... keys) {
        Date def = null;
        return convertDate(def, keys);
    }
    public DataRow convertDate(Date def, String ... keys) {
        if(null == keys || keys.length ==0) {
            keys = BeanUtil.list2array(keys());
        }
        for(String key:keys) {
            Object v = get(key);
            remove(keyAdapter.key(key));
            Date result = DateUtil.parse(v, def);
            put(key, result);
        }
        return this;
    }
    public DataRow convertInt(String ... keys) {
        Integer def = null;
        return convertInt(def, keys);
    }
    public DataRow convertInt(Integer def, String ... keys) {
        if(null == keys || keys.length ==0) {
            keys = BeanUtil.list2array(keys());
        }
        for(String key:keys) {
            Object v = get(key);
            remove(keyAdapter.key(key));
            Integer result = BasicUtil.parseInt(v, def);
            put(key, result);
        }
        return this;
    }
    public DataRow convertLong(String ... keys) {
        Long def = null;
        return convertLong(def, keys);
    }
    public DataRow convertLong(Long def, String ... keys) {
        if(null == keys || keys.length ==0) {
            keys = BeanUtil.list2array(keys());
        }
        for(String key:keys) {
            Object v = get(key);
            remove(keyAdapter.key(key));
            Long result = BasicUtil.parseLong(v, def);
            put(key, result);
        }
        return this;
    }
    public DataRow convertDouble(String ... keys) {
        Double def = null;
        return convertDouble(def, keys);
    }
    public DataRow convertDouble(Double def, String ... keys) {
        if(null == keys || keys.length ==0) {
            keys = BeanUtil.list2array(keys());
        }
        for(String key:keys) {
            Object v = get(key);
            remove(keyAdapter.key(key));
            Double result = BasicUtil.parseDouble(v, def);
            put(key, result);
        }
        return this;
    }
    /**
     * 指定key转换成number
     * @param keys keys
     * @return DataRow
     */
    public DataRow convertNumber(String... keys) {
        if(null == keys || keys.length ==0) {
            keys = BeanUtil.list2array(keys());
        }
        for (String key : keys) {
            Object v = get(key);
            Object result = null;
            if (null != v) {
                if(v instanceof Integer
                        || v instanceof Long
                        || v instanceof Short
                        || v instanceof Double
                        || v instanceof Float
                        || v instanceof Byte
                        || v instanceof BigDecimal
                ) {
                    continue;
                }else if(v instanceof Date) {
                    Date date = (Date)v;
                    result = date.getTime();
                }else if(v instanceof java.sql.Timestamp) {
                    java.sql.Timestamp timestamp = (java.sql.Timestamp)v;
                    result = timestamp.getTime();
                }else if(v instanceof java.sql.Date) {
                    Date date = (java.sql.Date)v;
                    result = date.getTime();
                }else if(v instanceof LocalDateTime) {
                    result = DateUtil.parse((LocalDateTime)v).getTime();
                }else if(v instanceof LocalDate) {
                    result = DateUtil.parse((LocalDate)v).getTime();
                }
                if(null == result) {
                    result = new BigDecimal(v.toString());
                }
                remove(keyAdapter.key(key));
                put(key, result);
            }
        }
        return this;
    }

    public DataRow convertDecimal(BigDecimal def, String... keys) {
        if(null == keys || keys.length ==0) {
            keys = BeanUtil.list2array(keys());
        }
        for (String key : keys) {
            Object v = get(key);
            BigDecimal result = null;
            if (null != v) {
                result = BasicUtil.parseDecimal(v, def);
            }
            if(null == result) {
                result = def;
            }

            remove(keyAdapter.key(key));
            put(key, result);
        }
        return this;
    }

    public DataRow convertString(String... keys) {
        List<String> list = null;
        if (null == keys || keys.length == 0) {
            list = keys();
        } else {
            list = BeanUtil.array2list(keys);
        }
        for (String key : list) {
            String v = getString(key);
            if (null != v) {
                put(key, v.toString());
            }
        }
        return this;
    }

    /**
     * 是否包含key(不要求完全匹配, 根据KEY_CASE有可能不区分大小写)
     * @param key key
     * @return boolean
     */
    public boolean contains(String key) {
        boolean result = false;
        if(null != key) {
            key = keyAdapter.key(key);
            if (ignoreCase) {
                String ignoreKey = key.replace("_","").replace("-","").toUpperCase();
                String tmp = keymap.get(ignoreKey);
                if(null != tmp) {
                    key = tmp;
                }
            }
            result = super.containsKey(key);
        }

        return result;
    }

    /**
     * 返回第一个存在的key对应的value, key不要求完全匹配根据KEY_CASE有可能不区分大小写<br/>
     * 如果需要取第一个不为null的值调用nvl(String ... keys)<br/>
     * 第一个不为空的值调用evl(String ... keys)
     * @param keys keys
     * @return Object
     */
    public Object get(String ... keys) {
        Object result = null;
        if(null == keys) {
            return result;
        }
        for(String key:keys) {
            if (null != key) {
                if(keyAdapter.getKeyCase() != KEY_CASE.SRC) {
                    key = keyAdapter.key(key);
                    if (ignoreCase) {
                        String ignoreKey = key.replace("_","").replace("-","").toUpperCase();
                        String tmp = keymap.get(ignoreKey);
                        if (null != tmp) {
                            key = tmp;
                        }
                    }
                }
                if(super.containsKey(key)) {
                    return super.get(key);
                }
            }
        }
        return result;
    }

    public Object get(String key) {
        if(keyAdapter.getKeyCase() == KEY_CASE.SRC && !ignoreCase) {
            return super.get(key);
        }
        Object result = null;
        if (null != key) {
            key = keyAdapter.key(key);
            if (ignoreCase) {
                String ignoreKey = key.replace("_","").replace("-","").toUpperCase();
                String tmp = keymap.get(ignoreKey);
                if(null != tmp) {
                    key = tmp;
                }
            }
            result = super.get(key);

        }
        return result;
    }

    public Object get(KEY_CASE keyCase, String key) {
        KeyAdapter keyAdapter = this.keyAdapter;
        if(null != keyCase) {
            keyAdapter = KeyAdapter.parse(keyCase);
        }
        Object result = null;
        if (null != key) {
            result = super.get(keyAdapter.key(key));
        }
        return result;
    }

    /**
     * 按keys顺序递归取值, 如果其中一层是数组 取第0个，不支持多维数组<br/>
     * strict=false时, 如果遇到基础类型值(包含String)则直接返回当前值，忽略之后的key<br/>
     * strict=true时，必须提取到最后一层，如果失败则返回null
     * 如提取用户的部门的领导的年率, 中间遇到部门只是个String类型, 则直接返回部门String<br/>
     * @param keys keys
     * @param strict 是否严格按key顺序提取到最后一层
     * @return Object
     */
    public Object recursion(boolean strict, String... keys) {
        if (null == keys || keys.length == 0) {
            return null;
        }
        Object result = this;
        int size = keys.length;
        for (int i=0; i<size; i++) {
            String key = keys[i];
            if (null != result) {
                //如果是数组 取第0个，不支持多维数组
                if(result instanceof Collection) {
                    Collection list = (Collection) result;
                    if(!list.isEmpty()) {
                        result = list.iterator().next();
                    }
                }
                if (ClassUtil.isWrapClass(result) && !(result instanceof String)) {
                    result = BeanUtil.getFieldValue(result, key);
                } else {
                    if(!strict) {
                        //不严格要求提取到最后一层
                        return result;
                    }else{
                        //严格要求提取到最后一层
                        if(i == size -1) {
                            return result;
                        }else{
                            //没有到最后一层就遇到基础类型不能继续下一级提取了
                            return null;
                        }
                    }
                }
            }
        }
        return result;
    }

    public Object recursion(String... keys) {
        return recursion(true, keys);
    }

    public Object nvl(String... keys) {
        return BeanUtil.nvl(this, keys);
    }

    public Object evl(String... keys) {
        return BeanUtil.evl(this, keys);
    }

    /**
     * 在key列基础上 +value, 如果原来没有key列则默认0并put到target
     * 如果target与key一致则覆盖原值
     * @param target 计算结果key
     * @param key key
     * @param value value
     * @return DataRow
     */
    public DataRow add(String target, String key, int value) {
        put(target, getInt(key, 0) + value);
        return this;
    }

    public DataRow add(String target, String key, double value) {
        put(target, getDouble(key, 0D) + value);
        return this;
    }

    public DataRow add(String target, String key, short value) {
        put(target, getInt(key, 0) + value);
        return this;
    }

    public DataRow add(String target, String key, float value) {
        put(target, getFloat(key, 0F) + value);
        return this;
    }

    public DataRow add(String target, String key, BigDecimal value) {
        put(target, getDecimal(key, 0).add(value));
        return this;
    }

    public DataRow add(String key, int value) {
        return add(key, key, value);
    }

    public DataRow add(String key, double value) {
        return add(key, key, value);
    }

    public DataRow add(String key, short value) {
        return add(key, key, value);
    }

    public DataRow add(String key, float value) {
        return add(key, key, value);
    }

    public DataRow add(String key, BigDecimal value) {
        return add(key, key, value);
    }

    public DataRow subtract(String target, String key, int value) {
        put(target, getInt(key, 0) - value);
        return this;
    }

    public DataRow subtract(String target, String key, double value) {
        put(target, getDouble(key, 0D) - value);
        return this;
    }

    public DataRow subtract(String target, String key, short value) {
        put(target, getInt(key, 0) - value);
        return this;
    }

    public DataRow subtract(String target, String key, float value) {
        put(target, getFloat(key, 0F) - value);
        return this;
    }

    public DataRow subtract(String target, String key, BigDecimal value) {
        put(target, getDecimal(key, 0).subtract(value));
        return this;
    }

    public DataRow subtract(String key, int value) {
        return subtract(key, key, value);
    }

    public DataRow subtract(String key, double value) {
        return subtract(key, key, value);
    }

    public DataRow subtract(String key, short value) {
        return subtract(key, key, value);
    }

    public DataRow subtract(String key, float value) {
        return subtract(key, key, value);
    }

    public DataRow subtract(String key, BigDecimal value) {
        return subtract(key, key, value);
    }

    public DataRow multiply(String target, String key, int value) {
        put(target, getInt(key, 0) * value);
        return this;
    }

    public DataRow multiply(String target, String key, double value) {
        put(target, getDouble(key, 0D) * value);
        return this;
    }

    public DataRow multiply(String target, String key, short value) {
        put(target, getInt(key, 0) * value);
        return this;
    }

    public DataRow multiply(String target, String key, float value) {
        put(target, getFloat(key, 0F) * value);
        return this;
    }

    public DataRow multiply(String target, String key, BigDecimal value) {
        put(target, getDecimal(key, 0).multiply(value));
        return this;
    }

    public DataRow multiply(String key, int value) {
        return multiply(key, key, value);
    }

    public DataRow multiply(String key, double value) {
        return multiply(key, key, value);
    }

    public DataRow multiply(String key, short value) {
        return multiply(key, key, value);
    }

    public DataRow multiply(String key, float value) {
        return multiply(key, key, value);
    }

    public DataRow multiply(String key, BigDecimal value) {
        return multiply(key, key, value);
    }

    public DataRow divide(String target, String key, int value) {
        put(target, getInt(key, 0) / value);
        return this;
    }

    public DataRow divide(String target, String key, double value) {
        put(target, getDouble(key, 0D) / value);
        return this;
    }

    public DataRow divide(String target, String key, short value) {
        put(target, getInt(key, 0) / value);
        return this;
    }

    public DataRow divide(String target, String key, float value) {
        put(target, getFloat(key, 0F) / value);
        return this;
    }

    /**
     * 除法，涉及到小数位与舍入问题可以提供scale, mode参数
     * @param target 结果保存位置, 如果与key一致则覆盖原值
     * @param key 属性
     * @param scale 小数位
     * @param round 舍入模式 参考BigDecimal静态常量
     *       ROUND_UP        = 0 舍入远离零的舍入模式 在丢弃非零部分之前始终增加数字（始终对非零舍弃部分前面的数字加 1） 如:2.36 转成 2.4<br/>
     *       ROUND_DOWN      = 1 接近零的舍入模式 在丢弃某部分之前始终不增加数字(从不对舍弃部分前面的数字加1, 即截短). 如:2.36 转成 2.3<br/>
     *       ROUND_CEILING   = 2 接近正无穷大的舍入模式 如果 BigDecimal 为正, 则舍入行为与 ROUND_UP 相同 如果为负, 则舍入行为与 ROUND_DOWN 相同 相当于是 ROUND_UP 和 ROUND_DOWN 的合集<br/>
     *       ROUND_FLOOR     = 3 接近负无穷大的舍入模式 如果 BigDecimal 为正, 则舍入行为与 ROUND_DOWN 相同 如果为负, 则舍入行为与 ROUND_UP 相同 与ROUND_CEILING 正好相反<br/>
     *       ROUND_HALF_UP   = 4 四舍五入<br/>
     *       ROUND_HALF_DOWN = 5 五舍六入<br/>
     *       ROUND_HALF_EVEN = 6 四舍六入 五留双(银行家舍入法) <br/>
     *         如果舍弃部分左边的数字为奇数, 则舍入行为与 ROUND_HALF_UP 相同（四舍五入）<br/>
     *         如果为偶数, 则舍入行为与 ROUND_HALF_DOWN 相同（五舍六入）<br/>
     *         如:1.15 转成 1.2, 因为5前面的1是奇数;1.25 转成 1.2, 因为5前面的2是偶数<br/>
     *      ROUND_UNNECESSARY=7 断言所请求的操作具有准确的结果，因此不需要舍入。如果在产生不精确结果的操作上指定了该舍入模式，则会抛出ArithmeticException异常
     * @return DataRow
     */
    public DataRow divide(String target, String key, BigDecimal value, int scale, int round) {
        put(target, getDecimal(key, 0).divide(value, scale, round));
        return this;
    }
    public DataRow divide(String target, String key, BigDecimal value, int mode) {
        put(target, getDecimal(key, 0).divide(value, mode));
        return this;
    }

    public DataRow divide(String key, int value) {
        return divide(key, key, value);
    }

    public DataRow divide(String key, double value) {
        return divide(key, key, value);
    }

    public DataRow divide(String key, short value) {
        return divide(key, key, value);
    }

    public DataRow divide(String key, float value) {
        return divide(key, key, value);
    }

    public DataRow divide(String key, BigDecimal value, int mode) {
        return divide(key, key, value, mode);
    }

    /**
     * 舍入
     * @param target 结果保存位置,如果与key一致则覆盖原值
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
     * @return DataRow
     */
    public DataRow round(String target, String key, int scale, int mode) {
        BigDecimal value = getDecimal(key, 0);
        value.setScale(scale, mode);
        put(target, value);
        return this;
    }

    public DataRow round(String key, int scale, int mode) {
        return round(key, key, scale, mode);
    }

    public String toString() {
        String result = this.getClass().getSimpleName();
        Object pv = getPrimaryValue();
        if(null != pv) {
            result += "(" + pv + ")";
        }
        result += ":" + toJSON();
        return result;
    }

    protected DataRow numberFormat(String src, String tar, String format, String def) {
        if (null == tar || null == src || isEmpty(src) || null == format) {
            return this;
        }
        try {
            put(tar, NumberUtil.format(getString(src), format));
        }catch (Exception e) {
            put(tar, def);
        }
        return this;
    }

    protected DataRow dateFormat(String src, String tar, String format, Date def) {
        if (null == tar || null == src || isEmpty(src) || null == format) {
            return this;
        }
        put(tar, DateUtil.format(getDate(src, def), format));
        return this;
    }
    protected DataRow dateFormat(String src, String tar, String format, String def) {
        if (null == tar || null == src || isEmpty(src) || null == format) {
            return this;
        }
        try {
            put(tar, DateUtil.format(getDate(src), format));
        }catch (Exception e) {
            put(tar, def);
        }
        return this;
    }

    /**
     * 日期解析,推荐调用parse.date()
     * @param src 源列
     * @param tar 结果列
     * @param format 如果是String 按format格式解析
     * @param def 默认值
     * @return DataRow
     */
    protected DataRow dateParse(String src, String tar, String format, Date def)  {
        if (null == tar || null == src || isEmpty(src) || null == format) {
            return this;
        }
        Object value = get(src);
        Date date = null;
        if(value instanceof String && null != format) {
            date = DateUtil.parse((String)value, format);
        }else {
            date = DateUtil.parse(value);
        }
        put(tar, date);
        return this;
    }

    /**
     * 数字解析,解析成BigDecimal 推荐调用parse.number()
     * @param src 源列
     * @param tar 结果列
     * @param def 默认值 如果默认值转换数字失败会抛出异常
     * @return DataRow
     */
    protected DataRow numberParse(String src, String tar, String def) {
        if (null == tar || null == src || isEmpty(src) ) {
            return this;
        }
        Object value = get(src);
        if(null == value) {
            value = def;
        }
        BigDecimal result = null;
        if(null != value) {
            try {
                if (value instanceof String) {
                    String str = ((String) value).replace(",","");
                    result = new BigDecimal(str);
                } else {
                    result = BasicUtil.parseDecimal(value, null);
                }
            }catch (Exception e) {
                if(null != def) {
                    result = new BigDecimal(def);
                }
            }
        }
        put(tar, result);
        return this;
    }
    public class Format implements Serializable{
        /**
         * 根据列名日期格式化,如果失败 默认 ""
         * @param format 日期格式
         * @param cols 参考格式化的列(属性)如果不指定则不执行(避免传参失败)<br/>
         *             支持date(format, "SRC:TAR:DEF")表示把SRC列的值格式华后存入TAR列,SRC列保持不变,如果格式化失败使用默认值DEF<br/>
         *             如果需要根据数据烦劳确定参与格式化的列参考date(format,Class)<br/>
         *             如果需要格式化所有的日期类型的列(类型中出现date关键字)参考date(greedy, format)
         * @return DataRow
         */
        public DataRow date(String format, String ... cols) {
            if (null == cols || BasicUtil.isEmpty(format)) {
                return DataRow.this;
            }
            for (String col : cols) {
                String src = col;
                String tar = col;
                String def = "";
                if(col.contains(":")) {
                    String[] tmps = col.split(":");
                    if(tmps.length>=2) {
                        src = tmps[0];
                        tar = tmps[1];
                    }
                    if(tmps.length > 2) {
                        def = tmps[2];
                    }
                }
                dateFormat(tar, src, format, def);
            }
            return DataRow.this;
        }
        /**
         * 根据数据类型日期格式化,如果失败 默认 ""<br/>
         * 如set.format.date("yyyy-MM-dd", Date.class);
         * @param format 日期格式
         * @param classes 数据类型(包括java和sql类型;不区分大小写),不指定则不执行(避免传参失败)<br/>
         *             如果需要根据列名确定参与格式化的列参考date(format, cols)<br/>
         *             如果需要格式化所有的日期类型的列(类型中出现date关键字)参考date(greedy, format)
         * @return DataSet
         */
        public DataRow date(String format, Class ... classes) {
            List<String> keys = keys();
            for(String key:keys) {
                Object value = get(key);
                if(null != value) {
                    Class vc = value.getClass();
                    boolean exe = false;
                    for(Class c:classes) {
                        if(vc.equals(c)) {
                            exe = true;
                            break;
                        }
                    }
                    if(exe) {
                        dateFormat(key, key, format, "");
                    }
                }
            }
            return DataRow.this;
        }

        /**
         * 格式化所有日期类型列(类型或列名中出现date关键字)
         * @param greedy false:只检查JAVA和SQL数据类型, true:在以上基础上检测列名
         * @param format 日期格式
         * @param def 默认值
         * @return DataRow
         */
        public DataRow date(boolean greedy, String format, String def) {
            List<String> checked = new ArrayList<>();
            List<String> keys = keys();
            for(String key:keys) {
                Object value = get(key);
                if(null != value) {
                    String vc = value.getClass().getSimpleName();
                    boolean exe = false;
                    if(vc.toUpperCase().contains("DATE")) {
                        exe = true;
                    }
                    if(!exe) {
                        Column column = getMetadata(key);
                        if(null != column) {
                            if(column.getTypeName().toUpperCase().contains("DATE")) {
                                exe = true;
                            }
                        }
                    }
                    if(exe) {
                        checked.add(key);
                        dateFormat(key, key, format, "");
                    }
                }
            }
            if(greedy) {
                for(String key:keys) {
                    if(checked.contains(key)) {
                        continue;
                    }
                    if(key.toUpperCase().contains("DATE")) {
                        dateFormat(key, key, format, def);
                    }
                }
            }
            return DataRow.this;
        }
        /**
         * 格式化所有日期类型列(类型或列名中出现date关键字)
         * @param greedy false:只检查JAVA和SQL数据类型, true:在以上基础上检测列名
         * @param format 日期格式
         * @param def 默认值
         * @return DataRow
         */
        public DataRow date(boolean greedy, String format, Date def) {
            List<String> checked = new ArrayList<>();
            List<String> keys = keys();
            for(String key:keys) {
                Object value = get(key);
                if(null != value) {
                    String vc = value.getClass().getSimpleName();
                    boolean exe = false;
                    if(vc.toUpperCase().contains("DATE")) {
                        exe = true;
                    }
                    if(!exe) {
                        Column column = getMetadata(key);
                        if(null != column) {
                            if(column.getTypeName().toUpperCase().contains("DATE")) {
                                exe = true;
                            }
                        }
                    }
                    if(exe) {
                        checked.add(key);
                        dateFormat(key, key, format, def);
                    }
                }
            }
            if(greedy) {
                for(String key:keys) {
                    if(checked.contains(key)) {
                        continue;
                    }
                    Column column = metadatas.get(key);
                    if(key.toUpperCase().contains("DATE")) {
                        dateFormat(key, key, format, def);
                    }
                }
            }
            return DataRow.this;
        }
        /**
         * 格式化所有日期类型列(类型或列名中出现date关键字),如果失败 默认 ""
         * @param greedy false:只检查JAVA和SQL数据类型, true:在以上基础上检测列名
         * @param format 日期格式
         * @return DataRow
         */
        public DataRow date(boolean greedy, String format) {
            return date(greedy, format, "");
        }

        /**
         * 根据列名数字格式化,如果失败 默认 ""
         * @param format 数字格式
         * @param cols 参考格式化的列(属性)如果不指定则不执行(避免传参失败)<br/>
         *             支持number(format, "SRC:TAR:DEF")表示把SRC列的值格式华后存入TAR列,SRC列保持不变,如果格式化失败使用默认值DEF<br/>
         *             如果需要根据数据烦劳确定参与格式化的列参考number(format,Class)<br/>
         *             如果需要格式化所有的数字类型的列参考number(greedy, format)
         * @return DataRow
         */
        public DataRow number(String format, String ... cols) {
            if (null == cols || BasicUtil.isEmpty(format)) {
                return DataRow.this;
            }
            for (String col : cols) {
                String src = col;
                String tar = col;
                String def = "";
                if(col.contains(":")) {
                    String[] tmps = col.split(":");
                    if(tmps.length>=2) {
                        src = tmps[0];
                        tar = tmps[1];
                    }
                    if(tmps.length > 2) {
                        def = tmps[2];
                    }
                }
                numberFormat(tar, src, format, def);
            }
            return DataRow.this;
        }
        /**
         * 根据数据类型数字格式化,如果失败 默认 ""<br/>
         * 如set.format.number("##.00", Date.class);
         * @param format 数字格式
         * @param classes 数据类型(包括java和sql类型;不区分大小写),不指定则不执行(避免传参失败)<br/>
         *             如果需要根据列名确定参与格式化的列参考number(format, cols)<br/>
         *             如果需要格式化所有的数字类型的列参考number(greedy, format)
         * @return DataSet
         */
        public DataRow number(String format, Class ... classes) {
            List<String> keys = keys();
            for(String key:keys) {
                Object value = get(key);
                if(null != value) {
                    Class vc = value.getClass();
                    boolean exe = false;
                    for(Class c:classes) {
                        if(vc.equals(c)) {
                            exe = true;
                            break;
                        }
                    }
                    if(exe) {
                        numberFormat(key, key, format, "");
                    }
                }
            }
            return DataRow.this;
        }

        /**
         * 格式化所有数字类型列
         * @param greedy 传入true时执行
         * @param format 数字格式
         * @param def 默认值
         * @return DataRow
         */
        public DataRow number(boolean greedy, String format, String def) {
            if(!greedy) {
                return DataRow.this;
            }
            List<String> checked = new ArrayList<>();
            List<String> keys = keys();
            for(String key:keys) {
                Object value = get(key);
                if(null != value) {
                    String vc = value.getClass().getSimpleName().toUpperCase();
                    boolean exe = false;
                    if(vc.startsWith("INT") || vc.contains("SHORT") || vc.contains("LONG") || vc.contains("FLOAT") || vc.contains("DOUBLE") || vc.contains("DECIMAL") || vc.contains("NUMERIC") || vc.contains("NUMBER")) {
                        exe = true;
                    }
                    if(!exe) {
                        Column column = getMetadata(key);
                        if(null != column) {
                            vc = column.getTypeName().toUpperCase();
                            if(vc.startsWith("INT") || vc.contains("SHORT") || vc.contains("LONG") || vc.contains("FLOAT") || vc.contains("DOUBLE") || vc.contains("DECIMAL") || vc.contains("NUMERIC") || vc.contains("NUMBER")) {
                                exe = true;
                            }
                        }
                    }
                    if(exe) {
                        numberFormat(key, key, format, def);
                    }
                }
            }
            return DataRow.this;
        }
        /**
         * @param greedy 传入true时执行
         * @param format 数字格式
         * @return DataRow
         */
        public DataRow number(boolean greedy, String format) {
            return number(greedy, format, "");
        }
    }
    public class Parse implements Serializable{
        /**
         * 根据列名日期格式化,如果失败 默认 ""
         * @param cols 参考格式化的列(属性)如果不指定则不执行(避免传参失败)<br/>
         *             支持date(format, "SRC:TAR:2020-01-01 10:10:10")表示把SRC列的值格式华后存入TAR列,SRC列保持不变,如果格式化失败使用默认值2020-01-01 10:10:10<br/>
         *             如果需要根据数据烦劳确定参与格式化的列参考date(format,Class)<br/>
         *             如果需要格式化所有的日期类型的列(类型中出现date关键字)参考date(greedy, format)
         * @return DataRow
         */
        public DataRow date(String ... cols) {
            for(String col:cols) {
                String src = col;
                String tar = col;
                Date def = null;
                if(col.contains(":")) {
                    String[] tmps = col.split(":");
                    if(tmps.length >= 2) {
                        src = tmps[0];
                        tar = tmps[1];
                    }
                    if(tmps.length > 2) {
                        def = DateUtil.parse(col.replace(src+":"+tar+":","").trim());
                    }
                    dateParse(src, tar, null, def);
                }
            }
            return DataRow.this;
        }
        public DataRow number(String ... cols) {
            for(String col:cols) {
                String src = col;
                String tar = col;
                String def = null;
                if(col.contains(":")) {
                    String[] tmps = col.split(":");
                    if(tmps.length >= 2) {
                        src = tmps[0];
                        tar = tmps[1];
                    }
                    if(tmps.length > 2) {
                        def =col.replace(src+":"+tar+":","").trim();
                    }
                    numberParse(src, tar, def);
                }
            }
            return DataRow.this;
        }
    }
    public Format format = new Format();
    public Parse parse = new Parse();

}