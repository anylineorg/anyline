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

package org.anyline.metadata;

import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.JavaType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.TypeMetadataHolder;
import org.anyline.util.BasicUtil;

import java.io.Serializable;
import java.util.*;

public class Column extends TableAffiliation<Column> implements Serializable {

    public static LinkedHashMap<TypeMetadata.CATEGORY, TypeMetadata.Refer> typeCategoryConfigs = new LinkedHashMap<>();

    public enum TYPE implements Type{
        NORMAL(1),
        TAG(2);
        public final int value;
        TYPE(int value) {
            this.value = value;
        }
        public int value() {
            return value;
        }
    }
    public enum Aggregation {
        MIN			            ("MIN"  			    , "最小"),
        MAX			            ("MAX"  			    , "最大"),
        SUM			            ("SUM"  			    , "求和"),
        REPLACE			        ("REPLACE"  			, "替换"),                    // 对于维度列相同的行，指标列会按照导入的先后顺序，后导入的替换先导入的。
        REPLACE_IF_NOT_NULL     ("REPLACE_IF_NOT_NULL", "非空值替换"),               // 与REPLACE的区别在于对于null值，不做替换。这里要注意的是字段默认值要给NULL，而不能是空字符串，如果是空字符串，会给你替换成空字符串。
        HLL_UNION			    ("HLL_UNION"  		, "HLL 类型的列的聚合方式"),     // 通过 HyperLogLog 算法聚合
        BITMAP_UNION            ("BITMAP_UNION"  		, "BIMTAP 类型的列的聚合方式，");// 进行位图的并集聚合
        final String code;
        final String name;
        Aggregation(String code, String name) {
            this.code = code;
            this.name = name;
        }
        public String getName() {
            return name;
        }
        public String getCode() {
            return code;
        }
    }
    public static <T extends Column>  void sort(Map<String, T> columns) {
        sort(columns, false);
    }
    public static <T extends Column>  void sort(Map<String, T> columns, boolean nullFirst) {
        List<T> list = new ArrayList<>();
        list.addAll(columns.values());
        sort(list, nullFirst);
        columns.clear();
        for(T column:list) {
            columns.put(column.getName().toUpperCase(), column);
        }
    }
    public static <T extends Column>  void sort(List<T> columns) {
        sort(columns, false);
    }

    /**
     * 列排序
     * @param columns 列集合
     * @param nullFirst 未设置过位置(setPosition)的列是否排在最前面
     * @param <T> Column
     */
    public static <T extends Column>  void sort(List<T> columns, boolean nullFirst) {
        Collections.sort(columns, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                Integer p1 = o1.getPosition();
                Integer p2 = o2.getPosition();
                if(p1 == p2) {
                    return 0;
                }
                if(nullFirst) {
                    if (null == p1) {
                        return -1;
                    }
                    if (null == p2) {
                        return 1;
                    }
                }else{
                    if (null == p1) {
                        return 1;
                    }
                    if (null == p2) {
                        return -1;
                    }
                }
                return p1 > p2 ? 1:-1;
            }
        });
    }
    public static class Property{
        String type;
        public Property(String type) {
            this.type = type;
        }
        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
    protected String keyword = "COLUMN"           ;
    protected String originName                   ; // 原名,只有查询时才会区分,添加列时用name即可 SELECT ID AS USER_ID FROM USER; originName=ID, name=USER_ID
    protected String typeName                     ; // 类型名称 varchar完整类型调用getFullType > varchar(10)
    protected String qualifier                    ; // 数据类型限定符 DATETIME YEAR TO MINUTE(6) 中的YEAR TO MINUTE部分 表达式中以{Q}表示
    protected String originType                   ; // 原始类型(未解析,交给具体的adapter解析)
    protected TypeMetadata typeMetadata           ;
    protected String fullType                     ; // 完整类型名称
    protected String finalType                    ; // 如果设置了finalType 生成SQL时 name finalType 其他属性
    protected int ignoreLength                = -1; // 是否忽略长度
    protected int ignorePrecision             = -1; // 是否忽略有效位数
    protected int ignoreScale                 = -1; // 是否忽略小数位
    protected int maxLength                   = -1;
    protected int maxPrecision                = -1;
    protected int maxScale                    = -1;
    //数字类型:precision,scale 日期:length 时间戳:scale 其他:length
    protected Integer precisionLength             ; // 精确长度 根据数据类型返回precision或length
    protected Integer length                      ; // 长度(注意varchar,date,timestamp,number的区别)
    protected String lengthUnit = ""              ; // 长度单位如byte char
    protected Integer octetLength                 ;
    protected Integer precision                   ; // 有效位数 整个字段的长度(包含小数部分)  123.45：precision = 5, scale = 2 对于SQL Server 中 varchar(max)设置成 -1 null:表示未设置
    protected Integer scale                       ; // 小数部分的长度
    protected Integer dimension                   ; // 维度(向量)

    protected String className                    ; // 对应的Java数据类型 java.lang.Long
    protected Integer displaySize                 ; // display size
    protected Integer type                        ; // 类型
    protected String childTypeName                ;
    protected TypeMetadata childTypeMetadata      ; //
    protected JavaType javaType                   ;
    protected String jdbcType                     ; // 有可能与typeName不一致 可能多个typeName对应一个jdbcType 如point>
    protected String dateScale                    ; // 日期类型 精度
    protected Boolean nullable              = null; // 是否可以为NULL -1:未配置 1:是(NULL)  0:否(NOT NULL)
    protected Boolean caseSensitive         = null; // 是否区分大小写
    protected Boolean currency              = null; // 是否是货币
    protected Boolean signed                = null; // 是否可以带正负号
    protected Boolean autoIncrement         = null; // 是否自增
    protected Integer incrementSeed         = null; // 自增起始值
    protected Integer incrementStep         = null; // 自增增量
    protected Boolean primary               = null; // 是否主键
    protected String primaryType                  ; // 主键类型 如BTREE
    protected Boolean unique                = null; // 是否唯一
    protected Boolean generated             = null; // 是否generated
    protected Object defaultValue                 ; // 默认值
    protected String defaultConstraint            ; // 默认约束名
    protected String charset                      ; // 编码
    protected String collate                      ; // 排序编码
    protected Aggregation aggregation             ; //聚合类型
    protected Boolean withTimeZone          = null;
    protected Boolean withLocalTimeZone     = null;
    protected Column reference                    ; // 外键依赖列
    protected Integer srid                        ; // SRID
    protected boolean array                       ; // 是否数组
    protected boolean isKey                       ; // doris中用到

    protected Integer position                    ; // 在表或索引中的位置, 如果需要在第一列 设置成0
    protected String order                        ; // 在索引中的排序方式ASC | DESC

    protected String after                        ; // 修改列时 在表中的位置
    protected String before                       ; // 修改列时 在表中的位置
    protected String onUpdate = null              ; // 是否在更新行时 更新这一列数据
    protected Object value                        ;
    protected boolean defaultCurrentDateTime = false;
    protected Boolean index                       ; // 是否需要创建索引(ES里用的其他数据库应该通过new Index()创建索引)
    protected Boolean store                       ; // 是否需要存储
    protected String analyzer                     ; // 分词器
    protected String searchAnalyzer               ; // 查询分词器
    protected Integer ignoreAbove                 ; // 可创建索引的最大词长度
    protected String coerce;
    protected String copyTo;
    protected String docValues;
    protected String dynamic;
    protected String eagerGlobalOrdinals;
    protected String enabled;
    protected String format;
    protected String ignoreMalformed;
    protected String indexOptions;
    protected String indexPhrases;
    protected String indexPrefixes;
    protected String meta;
    protected String fields;
    protected String normalizer;
    protected String norms;
    protected String nullValue;
    protected String positionIncrementGap;
    protected LinkedHashMap<String, Property> properties = new LinkedHashMap<>();
    protected String similarity                 ; // 相似度算法 如l2_norm dot_product cosine max_inner_product
    protected String subObjects;
    protected String termVector;
    protected int parseLvl                      = 0;// 类型解析级别0:未解析 1:column解析 2:adapter解析
    protected ColumnFamily family;                 ; // 列族

    public Column() {
    }
    public Column(Table table, String name, String type) {
        setTable(table);
        setName(name);
        setType(type);
    }
    public Column(String name) {
        setName(name);
    }
    public Column(Schema schema, String table, String name) {
        this(null, schema, table, name);
    }
    public Column(Catalog catalog, Schema schema, String table, String name) {
        setCatalog(catalog);
        setSchema(schema);
        setName(name);
        setTable(table);
    }
    public Column(String name, String type, int precision, int scale) {
        this.name = name;
        setType(type);
        this.precision = precision;
        this.scale = scale;
    }

    public Column(String name, String type, int precision) {
        this.name = name;
        setType(type);
        this.precision = precision;
    }
    public Column(Table table, String name, String type, int precision, int scale) {
        setTable(table);
        this.name = name;
        setType(type);
        this.precision = precision;
        this.scale = scale;
    }

    public Column(Table table, String name, String type, int precision) {
        setTable(table);
        this.name = name;
        setType(type);
        this.precision = precision;
    }

    public Column(String name, String type) {
        this.name = name;
        setType(type);
    }

    public Boolean getIndex() {
        return index;
    }
    public boolean isIndex() {
        return null != index && index;
    }

    public Column setIndex(Boolean index) {
        this.index = index;
        return this;
    }

    public Boolean getStore() {
        return store;
    }
    public boolean isStore() {
        return null != store && store;
    }

    public Column setStore(Boolean store) {
        this.store = store;
        return this;
    }

    public String getAnalyzer() {
        return analyzer;
    }

    public Column setAnalyzer(String analyzer) {
        this.analyzer = analyzer;
        return this;
    }
    public String getQualifier() {
        return qualifier;
    }

    public Column setQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public String getSearchAnalyzer() {
        return searchAnalyzer;
    }

    public Column setSearchAnalyzer(String searchAnalyzer) {
        this.searchAnalyzer = searchAnalyzer;
        return this;
    }

    public Integer getIgnoreAbove() {
        return ignoreAbove;
    }

    public Column setIgnoreAbove(Integer ignoreAbove) {
        this.ignoreAbove = ignoreAbove;
        return this;
    }

    public String getCoerce() {
        return coerce;
    }

    public Column setCoerce(String coerce) {
        this.coerce = coerce;
        return this;
    }

    public String getCopyTo() {
        return copyTo;
    }

    public Column setCopyTo(String copyTo) {
        this.copyTo = copyTo;
        return this;
    }

    public String getDocValues() {
        return docValues;
    }

    public Column setDocValues(String docValues) {
        this.docValues = docValues;
        return this;
    }

    public String getDynamic() {
        return dynamic;
    }

    public Column setDynamic(String dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    public String getEagerGlobalOrdinals() {
        return eagerGlobalOrdinals;
    }

    public Column setEagerGlobalOrdinals(String eagerGlobalOrdinals) {
        this.eagerGlobalOrdinals = eagerGlobalOrdinals;
        return this;
    }

    public String getEnabled() {
        return enabled;
    }

    public Column setEnabled(String enabled) {
        this.enabled = enabled;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    public Column setDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getIgnoreMalformed() {
        return ignoreMalformed;
    }

    public Column setIgnoreMalformed(String ignoreMalformed) {
        this.ignoreMalformed = ignoreMalformed;
        return this;
    }

    public String getIndexOptions() {
        return indexOptions;
    }

    public Column setIndexOptions(String indexOptions) {
        this.indexOptions = indexOptions;
        return this;
    }

    public String getIndexPhrases() {
        return indexPhrases;
    }

    public Column setIndexPhrases(String indexPhrases) {
        this.indexPhrases = indexPhrases;
        return this;
    }

    public String getIndexPrefixes() {
        return indexPrefixes;
    }

    public Column setIndexPrefixes(String indexPrefixes) {
        this.indexPrefixes = indexPrefixes;
        return this;
    }

    public String getMeta() {
        return meta;
    }

    public Column setMeta(String meta) {
        this.meta = meta;
        return this;
    }

    public String getFields() {
        return fields;
    }

    public Column setFields(String fields) {
        this.fields = fields;
        return this;
    }

    public String getNormalizer() {
        return normalizer;
    }

    public Column setNormalizer(String normalizer) {
        this.normalizer = normalizer;
        return this;
    }

    public String getNorms() {
        return norms;
    }

    public Column setNorms(String norms) {
        this.norms = norms;
        return this;
    }

    public String getNullValue() {
        return nullValue;
    }

    public Column setNullValue(String nullValue) {
        this.nullValue = nullValue;
        return this;
    }

    public String getPositionIncrementGap() {
        return positionIncrementGap;
    }

    public Column setPositionIncrementGap(String positionIncrementGap) {
        this.positionIncrementGap = positionIncrementGap;
        return this;
    }

    public String getSimilarity() {
        return similarity;
    }

    public Column setSimilarity(String similarity) {
        this.similarity = similarity;
        return this;
    }

    public String getSubObjects() {
        return subObjects;
    }

    public Column setSubObjects(String subObjects) {
        this.subObjects = subObjects;
        return this;
    }

    public String getTermVector() {
        return termVector;
    }

    public Column setTermVector(String termVector) {
        this.termVector = termVector;
        return this;
    }
    public Column addProperty(String name, String type) {
        properties.put(name, new Property(type));
        return this;
    }
    public LinkedHashMap<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(LinkedHashMap<String, Property> properties) {
        this.properties = properties;
    }

    public Column drop() {
        this.action = ACTION.DDL.COLUMN_DROP;
        return super.drop();
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }
    public boolean isArray() {
        return array;
    }

    public Column setArray(boolean array) {
        this.array = array;
        return this;
    }

    public String getDateScale() {
        if(getmap && null != update) {
            return update.getDateScale();
        }
        return dateScale;
    }

    public Boolean getWithTimeZone() {
        return withTimeZone;
    }
    public boolean isWithTimeZone() {
        return null != withLocalTimeZone && withLocalTimeZone;
    }

    public void setWithTimeZone(int withTimeZone) {
        if(withTimeZone == 0){
            this.withTimeZone = false;
        }else if(withTimeZone == 1){
            this.withTimeZone = true;
        }
    }
    public void setWithTimeZone(Boolean withTimeZone) {
        this.withTimeZone = withTimeZone;
    }

    public Boolean getWithLocalTimeZone() {
        return withLocalTimeZone;
    }

    public boolean setWithLocalTimeZone() {
        return null != withLocalTimeZone && withLocalTimeZone;
    }

    public void setWithLocalTimeZone(int withLocalTimeZone) {
        if(withLocalTimeZone == 0){
            this.withLocalTimeZone = false;
        }else if(withLocalTimeZone == 1){
            this.withLocalTimeZone = true;
        }
    }

    public Column setDateScale(String dateScale) {
        if(setmap && null != update) {
            update.setDateScale(dateScale);
            return this;
        }
        this.dateScale = dateScale;
        return this;
    }

    public boolean isKey() {
        return isKey;
    }

    public void setKey(boolean key) {
        isKey = key;
    }

    public String getClassName() {
        if(getmap && null != update) {
            return update.getClassName();
        }
        return className;
    }

    public Column setClassName(String className) {
        if(setmap && null != update) {
            update.setClassName(className);
            return this;
        }
        this.className = className;
        return this;
    }

    public String getChildTypeName() {
        if(getmap && null != childTypeName) {
            return update.getChildTypeName();
        }
        return childTypeName;
    }

    public Column setChildTypeName(String childTypeName) {
        if(setmap && null != update) {
            update.setChildTypeName(childTypeName);
            return this;
        }
        this.childTypeName = childTypeName;
        return this;
    }

    public TypeMetadata getChildTypeMetadata() {
        if(getmap && null != update) {
            return update.childTypeMetadata;
        }
        if(array && null != childTypeMetadata) {
            childTypeMetadata.setArray(array);
        }
        return childTypeMetadata;
    }

    public Column setChildTypeMetadata(TypeMetadata childTypeMetadata) {
        if(setmap && null != update) {
            update.setChildTypeMetadata(childTypeMetadata);
            return this;
        }
        this.childTypeMetadata = childTypeMetadata;
        return this;
    }

    public Integer getDisplaySize() {
        if(getmap && null != update) {
            return update.getDisplaySize();
        }
        return displaySize;
    }

    public Column setDisplaySize(Integer displaySize) {
        if(setmap && null != update) {
            update.setDisplaySize(displaySize);
            return this;
        }
        this.displaySize = displaySize;
        return this;
    }

    public String getOriginType() {
        if(null == originType) {
            return typeName;
        }
        return originType;
    }

    public void setOriginType(String originType) {
        this.originType = originType;
    }

    public Integer getType() {
        if(getmap && null != update) {
            return update.getType();
        }
        return type;
    }

    /**
     * 设置数据类型 根据 jdbc定义的类型ID
     * @param type type
     * @return Column
     */
    public Column setType(Integer type) {
        if(setmap && null != update) {
            update.setType(type);
            return this;
        }
        if(this.type != type) {
            this.className = null;
        }
        this.type = type;
        return this;
    }

    /**
     * 设置数据类型 根据数据库定义的数据类型 实际调用了setTypeName(String)
     * @param type  数据类型 如 int  varchar(10) decimal(18, 6)
     * @return Column
     */
    public Column setType(String type) {
        if(setmap && null != update) {
            update.setType(type);
            return this;
        }
        return setTypeName(type);
    }

    public String getTypeName() {
        if(getmap && null != update) {
            return update.getTypeName();
        }
        if(null == typeName) {
            if(null != typeMetadata && typeMetadata != TypeMetadata.ILLEGAL && typeMetadata != TypeMetadata.NONE) {
                typeName = typeMetadata.getName();
            }
        }
        return typeName;
    }

    public String getJdbcType() {
        if(getmap && null != update) {
            return update.jdbcType;
        }
        return jdbcType;
    }

    public Column setJdbcType(String jdbcType) {
        if(setmap && null != update) {
            update.setJdbcType(jdbcType);
            return this;
        }
        this.jdbcType = jdbcType;
        return this;
    }

    public Column setTypeName(String typeName) {
        return setTypeName(typeName, true);
    }

    /**
     * 设置数据类型 根据数据库定义的数据类型
     * @param typeName 数据类型 如 int  varchar(10) decimal(18, 6)
     * @return Column
     */
    public Column setTypeName(String typeName, boolean parse) {
        if(setmap && null != update) {
            update.setTypeName(typeName, parse);
            return this;
        }
        if(null == this.typeName || !this.typeName.equalsIgnoreCase(typeName)) {
            //修改数据类型的重置解析状态
            parseLvl = 0;
        }
        this.typeName = typeName;
        if(parse) {
            setOriginType(typeName);
            parseType(1, databaseType);
        }
        //fullType = null;
        return this;
    }

    /**
     *
     * @param lvl 解析阶段
     * @param database 新数据库类型
     * @return Column
     */
    public Column parseType(int lvl, DatabaseType database) {
        if(lvl <= parseLvl) {
            return this;
        }
        TypeMetadata.parse(database, this, TypeMetadataHolder.gets(database), null);
        return this;
    }

    public int getParseLvl() {
        return parseLvl;
    }

    public void setParseLvl(int parseLvl) {
        this.parseLvl = parseLvl;
    }

    public Column setFullType(String fullType) {
        this.fullType = fullType;
        return this;
    }
    public String getFullType() {
        return getFullType(databaseType);
    }

    public String getFullType(DatabaseType database) {
        return getFullType(database, null);
    }
    public String getFullType(DatabaseType database, TypeMetadata.Refer refer) {
        if(getmap && null != update) {
            return update.getFullType(database);
        }
        if(null != fullType && this.databaseType == database) {
            return fullType;
        }
        int ignoreLength = -1;
        int ignorePrecision = -1;
        int ignoreScale = -1;
        int maxLength = -1;
        int maxPrecision = -1;
        int maxScale = -1;
        String result = null;
        String type = null;
        String formula = null;
        if(null != refer) {
            ignoreLength = refer.ignoreLength();
            ignorePrecision = refer.ignorePrecision();
            ignoreScale = refer.ignoreScale();
            maxLength = refer.maxLength();
            maxPrecision = refer.maxPrecision();
            maxScale = refer.maxScale();
            formula = refer.getFormula();
        }else{
            ignoreLength = ignoreLength(database);
            ignorePrecision = ignorePrecision(database);
            ignoreScale = ignoreScale(database);
            maxLength = maxLength(database);
            maxPrecision = maxPrecision(database);
            maxScale = maxScale(database);
            formula = formula(database);
        }
        if(null != typeMetadata && typeMetadata != TypeMetadata.NONE && typeMetadata != TypeMetadata.ILLEGAL && database == this.databaseType) {
            type = typeMetadata.getName();
        }else{
            type = getTypeName();
        }
        boolean appendLength = false;
        boolean appendPrecision = false;
        boolean appendScale = false;

        if(ignoreLength != 1) {
            if(null == length) {
                //null表示没有设置过,有可能用的precision,复制precision值
                // -1也表示设置过了不要再用length覆盖
                if(null != precision && precision != -1) {
                    length = precision;
                }
            }
            if(null != length) {
                if(length > 0 || length == -2) { //-2:max
                    appendLength = true;
                }
            }
        }
        if(ignorePrecision != 1) {
            if(null == precision) {
                //null表示没有设置过,有可能用的length,复制length
                // -1也表示设置过了不要再用length覆盖
                if(null != length && length != -1) {
                    precision = length;
                }
            }
            if(null != precision) {
                if(precision > 0) {
                    if(ignorePrecision == 3) {
                        if(null != scale && scale > 0) {
                            appendPrecision = true;
                        }else{
                            appendPrecision = false;
                        }
                    }else{
                        appendPrecision = true;
                    }
                }
            }
        }
        if(ignoreScale != 1) {
            if(null != scale) {
                if(scale > 0) {
                    if(ignoreScale == 3) {
                        if(null != precision && precision > 0) {
                            appendScale = true;
                        }else{
                            appendScale = false;
                        }
                    }else{
                        appendScale = true;
                    }
                }
            }
        }

        if(maxLength != -1){
            if(null != length){
                if(length > maxLength) {
                    length = maxLength;
                }
            }
        }

        if(maxPrecision != -1){
            if(null != precision){
                if(precision > maxPrecision) {
                    precision = maxPrecision;
                }
            }
        }

        if(maxScale != -1){
            if(null != scale){
                if(scale > maxScale) {
                    scale = maxScale;
                }
            }
        }

        if(BasicUtil.isNotEmpty(formula)) {
            result = formula;
            result = result.replace("{L}", length+"");
            result = result.replace("{P}", precision+"");
            result = result.replace("{S}", scale+"");
            result = result.replace("{U}", lengthUnit);
            result = result.replace("(0)", "");
            result = result.replace("(null)","");
        }else if(null != type) {
            StringBuilder builder = new StringBuilder();
            if(type.contains("{")) {
                result = type;
                result = result.replace("{L}", length + "");
                result = result.replace("{P}", precision + "");
                result = result.replace("{S}", scale + "");
                result = result.replace("{U}", lengthUnit);
                result = result.replace("(0)", "");
                result = result.replace("(null)", "");
            }else {
                builder.append(type);
                if (appendLength || appendPrecision || appendScale) {
                    builder.append("(");
                }
                if (appendLength) {
                    if (length == -2) {
                        builder.append("max");
                    } else {
                        builder.append(length);
                        if(BasicUtil.isNotEmpty(lengthUnit)){
                            builder.append(" ").append(lengthUnit);
                        }
                    }
                } else {
                    if (appendPrecision) {
                        builder.append(precision);
                    }
                    if (appendScale) {//可能单独出现
                        if (appendPrecision) {
                            builder.append(", ");
                        }
                        builder.append(scale);
                    }
                }
                if (appendLength || appendPrecision || appendScale) {
                    builder.append(")");
                }

                String child = getChildTypeName();
                Integer srid = getSrid();
                if (null != child) {
                    builder.append("(");
                    builder.append(child);
                    if (null != srid) {
                        builder.append(", ").append(srid);
                    }
                    builder.append(")");
                }
                if (isArray()) {
                    builder.append("[]");
                }
                result = builder.toString();
            }
        }

        return result;
    }

    /**
     * 精确长度 根据数据类型返回precision或length
     * @return Integer
     */
    public Integer getPrecisionLength() {
        if(null != precisionLength && precisionLength != -1) {
            return precisionLength;
        }
        if(null != precision && precision != -1) {
            precisionLength = precision;
        }else{
            precisionLength = length;
        }
        return precisionLength;
    }
    public Integer getLength() {
        if(getmap && null != update) {
            return update.getLength();
        }
        if(null != length && length != -1) {
            return length;
        }
        return precision;
    }
    public Column resetLength(Integer length){
        if(null != originType) {
            originType = originType.replace("(" + this.length, "(" + length);
        }
        setLength(length);
        setParseLvl(0);
        return this;
    }
    public Column setLength(Integer length) {
        if(setmap && null != update) {
            update.setLength(length);
            return this;
        }
        if(ignoreLength == 1) {
            this.precision = length;
        }else {
            this.length = length;
        }
        //fullType = null;
        return this;
    }

    public Integer getOctetLength() {
        if(getmap && null != update) {
            return update.getOctetLength();
        }
        return octetLength;
    }
    public Column setOctetLength(Integer length) {
        if(setmap && null != update) {
            update.setOctetLength(length);
            return this;
        }
        this.octetLength = length;
        return this;
    }

    public Integer getPrecision() {
        if(getmap && null != update) {
            return update.getPrecision();
        }
        if(null != precision && precision != -1) {
            return precision;
        }
        return length;
    }

    public Column setPrecision(Integer precision) {
        if(setmap && null != update) {
            update.setPrecision(precision);
            return this;
        }
        if(ignorePrecision == 1) {
            this.length = precision;
        }else {
            this.precision = precision;
        }
        //fullType = null;
        return this;
    }
    public Column setPrecision(Integer precision, Integer scale) {
        if(setmap && null != update) {
            update.setPrecision(precision, scale);
            return this;
        }
        this.precision = precision;
        this.scale = scale;
        //fullType = null;
        return this;
    }

    public Object getValue() {
        if(getmap && null != update) {
            return update.value;
        }
        return value;
    }

    public Column setValue(Object value) {
        if(setmap && null != update) {
            update.setValue(value);
            return this;
        }
        this.value = value;
        return this;
    }

    public Boolean getCaseSensitive() {
        if(getmap && null != update) {
            return update.getCaseSensitive();
        }
        return caseSensitive;
    }

    public boolean isCaseSensitive() {
        if(getmap && null != update) {
            return update.isCaseSensitive();
        }
        return null != caseSensitive && caseSensitive;
    }

    public Column setCaseSensitive(int caseSensitive) {
        if(setmap && null != update) {
            update.setCaseSensitive(caseSensitive);
            return this;
        }
        if (caseSensitive == 1) {
            this.caseSensitive = true;
        }else if (caseSensitive == 0) {
            this.caseSensitive = false;
        }
        return this;
    }
    public Column caseSensitive(int caseSensitive) {
        return setCaseSensitive(caseSensitive);
    }
    public Column caseSensitive(Boolean caseSensitive) {
        if(setmap && null != update) {
            update.caseSensitive(caseSensitive);
            return this;
        }
        this.caseSensitive = caseSensitive;
        return this;
    }

    public Boolean getCurrency() {
        if(getmap && null != update) {
            return update.currency;
        }
        return currency;
    }
    public boolean isCurrency() {
        if(getmap && null != update) {
            return update.isCurrency();
        }
        return null != currency && currency;
    }

    public Column setCurrency(int currency) {
        if(setmap && null != update) {
            update.setCurrency(currency);
            return this;
        }
        if (currency == 1) {
            this.currency = true;
        }else if (currency == 0) {
            this.currency = false;
        }

        return this;
    }
    public Column currency(int currency) {
        return setCurrency(currency);
    }
    public Column setCurrency(Boolean currency) {
        return currency(currency);
    }
    public Column currency(Boolean currency) {
        if(setmap && null != update) {
            update.currency(currency);
            return this;
        }
        this.currency = currency;
        return this;
    }

    public Boolean getSigned() {
        if(getmap && null != update) {
            return update.signed;
        }
        return signed;
    }
    public boolean isSigned() {
        if(getmap && null != update) {
            return update.isSigned();
        }
        return null != signed && signed;
    }

    public Column setSigned(int signed) {
        if(setmap && null != update) {
            update.setSigned(signed);
            return this;
        }
        if (signed == 1) {
            this.signed = true;
        }else if (signed == 0) {
            this.signed = false;
        }
        return this;
    }
    public Column signed(int signed) {
        return setSigned(signed);
    }
    public Column setSigned(Boolean signed) {
        if(setmap && null != update) {
            update.setSigned(signed);
            return this;
        }
        this.signed = signed;
        return this;
    }

    public Aggregation getAggregation() {
        if(getmap && null != update) {
            return update.aggregation;
        }
        return aggregation;
    }

    public Column setAggregation(Aggregation aggregation) {
        if(setmap && null != update) {
            update.setAggregation(aggregation);
            return this;
        }
        this.aggregation = aggregation;
        return this;
    }

    public Integer getScale() {
        if(getmap && null != update) {
            return update.getScale();
        }
        return scale;
    }

    public Column setScale(Integer scale) {
        if(setmap && null != update) {
            update.setScale(scale);
            return this;
        }
        this.scale = scale;
        //fullType = null;
        return this;
    }

    public Boolean getNullable() {
        if(getmap && null != update) {
            return update.nullable;
        }
        return nullable;
    }
    public boolean isNullable() {
        if(getmap && null != update) {
            return update.isNullable();
        }
        return null != nullable && nullable;
    }

    public Column setNullable(int nullable) {
        if(setmap && null != update) {
            update.setNullable(nullable);
            return this;
        }
        if (nullable == 1) {
            this.nullable = true;
        }else if (nullable == 0) {
            this.nullable = false;
        }
        return this;
    }
    public Column nullable(int nullable) {
        return setNullable(nullable);
    }
    public Column setNullable(Boolean nullable) {
        return nullable(nullable);
    }
    public Column nullable(Boolean nullable) {
        if(setmap && null != update) {
            update.nullable(nullable);
            return this;
        }
        this.nullable = nullable;
        return this;
    }

    public Boolean getAutoIncrement() {
        if(getmap && null != update) {
            return update.autoIncrement;
        }
        return autoIncrement;
    }
    public Boolean isAutoIncrement() {
        if(getmap && null != update) {
            return update.isAutoIncrement();
        }
        return null != autoIncrement && autoIncrement;
    }

    public Column setAutoIncrement(int autoIncrement) {
        if(setmap && null != update) {
            update.setAutoIncrement(autoIncrement);
            return this;
        }
        if(autoIncrement == 1) {
            nullable(false);
            this.autoIncrement = true;
        }else if (autoIncrement == 0) {
            this.autoIncrement = false;
        }
        //fullType = null;
        return this;
    }

    public Column autoIncrement(int autoIncrement) {
        return setAutoIncrement(autoIncrement);
    }
    //不要实现 setAutoIncrement有些工具会因为参数与属性类型不一致抛出异常
    public Column setAutoIncrement(Boolean autoIncrement) {
        return autoIncrement(autoIncrement);
    }
    public Column autoIncrement(Boolean autoIncrement) {
        if(setmap && null != update) {
            update.autoIncrement(autoIncrement);
            return this;
        }
        if(null != autoIncrement) {
            if(autoIncrement) {
                nullable(false);
            }
        }
        this.autoIncrement = autoIncrement;
        return this;
    }

    /**
     * 递增列
     * @param seed 起始值
     * @param step 增量
     * @return  Column
     */
    public Column setAutoIncrement(int seed, int step) {
        if(setmap && null != update) {
            update.setAutoIncrement(seed, step);
            return this;
        }
        setAutoIncrement(1);
        this.incrementSeed= seed;
        this.incrementStep = step;
        return this;
    }

    public Boolean getUnique() {
        if(getmap && null != update) {
            return update.unique;
        }
        return unique;
    }

    public boolean isUnique() {
        if(getmap && null != update) {
            return update.isUnique();
        }
        return null != unique && unique;
    }
    public Column setUnique(int unique) {
        if(setmap && null != update) {
            update.setUnique(unique);
            return this;
        }
        if (unique == 1) {
            this.unique = true;
        }else if (unique == 0) {
            this.unique = false;
        }
        return this;
    }

    public Column unique(int unique) {
        return setUnique(unique);
    }
    public Column setUnique(Boolean unique) {
        return unique(unique);
    }
    public Column unique(Boolean unique) {
        if(setmap && null != update) {
            update.unique(unique);
            return this;
        }
        this.unique = unique;
        return this;
    }

    public Boolean getPrimaryKey() {
        if(getmap && null != update) {
            return update.primary;
        }
        return primary;
    }
    public boolean isPrimaryKey() {
        if(getmap && null != update) {
            return update.primary;
        }
        return null != primary && primary;
    }

    public Column setPrimary(int primary) {
        if(setmap && null != update) {
            update.setPrimary(primary);
            return this;
        }
        if (primary == 1) {
            this.primary = true;
        }else if (primary == 0) {
            this.primary = false;
        }
        return this;
    }
    public Column primary(int primary) {
        return setPrimary(primary);
    }
    public Column setPrimary(Boolean primary) {
        return primary(primary);
    }
    public Column setPrimaryKey(Boolean primary) {
        return primary(primary);
    }
    public Column primary(Boolean primary) {
        if(setmap && null != update) {
            update.primary(primary);
            return this;
        }
        this.primary = primary;
        return this;
    }

    public String getPrimaryType() {
        return primaryType;
    }

    public void setPrimaryType(String primaryType) {
        this.primaryType = primaryType;
    }

    public Column setPrimary(int primary, String type) {
        setPrimary(primary);
        this.primaryType = type;
        return this;
    }
    public Column primary(int primary, String type) {
        primary(primary);
        this.primaryType = type;
        return this;
    }
    public Column setPrimary(Boolean primary, String type) {
        setPrimary(primary);
        this.primaryType = type;
        return this;
    }
    public Column setPrimaryKey(Boolean primary, String type) {
        setPrimaryKey(primary);
        this.primaryType = type;
        return this;
    }
    public Column primary(Boolean primary, String type) {
        primary(primary);
        this.primaryType = type;
        return this;
    }

    public Boolean getGenerated() {
        if(getmap && null != update) {
            return update.generated;
        }
        return generated;
    }

    public boolean isGenerated() {
        if (getmap && null != update) {
            return update.isGenerated();
        }
        return null != generated && generated;
    }

    public Column setGenerated(int generated) {
        if(setmap && null != update) {
            update.setGenerated(generated);
            return this;
        }
        if (generated == 1) {
            this.generated = true;
        }else if (generated == 0) {
            this.generated = false;
        }
        return this;
    }
    public Column generated(int generated) {
       return setGenerated(generated);
    }
    public Column setGenerated(Boolean generated) {
        return generated(generated);
    }
    public Column generated(Boolean generated) {
        if(setmap && null != update) {
            update.generated(generated);
            return this;
        }
        this.generated = generated;
        return this;
    }

    public Object getDefaultValue() {
        if(getmap && null != update) {
            return update.defaultValue;
        }
        return defaultValue;
    }

    public Column setDefaultValue(Object defaultValue) {
        if(setmap && null != update) {
            update.setDefaultValue(defaultValue);
            return this;
        }
        this.defaultValue = defaultValue;
        return this;
    }
    public Column setDefaultCurrentDateTime(boolean currentDateTime) {
        if(setmap && null != update) {
            update.setDefaultCurrentDateTime(currentDateTime);
            return this;
        }
        this.defaultCurrentDateTime = currentDateTime;
        return this;
    }
    public Column setDefaultCurrentDateTime() {
        return setDefaultCurrentDateTime(true);
    }
    public boolean isDefaultCurrentDateTime() {
        return this.defaultCurrentDateTime;
    }

    public String getDefaultConstraint() {
        if(getmap && null != update) {
            return update.defaultConstraint;
        }
        return defaultConstraint;
    }

    public Column setDefaultConstraint(String defaultConstraint) {
        if(setmap && null != update) {
            update.setDefaultConstraint(defaultConstraint);
            return this;
        }
        this.defaultConstraint = defaultConstraint;
        return this;
    }

    public Integer getPosition() {
        if(getmap && null != update) {
            return update.position;
        }
        return position;
    }

    public String getOrder() {
        if(getmap && null != update) {
            return update.order;
        }
        return order;
    }

    public Column setOrder(String order) {
        if(setmap && null != update) {
            update.setOrder(order);
            return this;
        }
        this.order = order;
        return this;
    }

    public Column setPosition(Integer position) {
        if(setmap && null != update) {
            update.setPosition(position);
            return this;
        }
        this.position = position;
        return this;
    }

    public String getAfter() {
        if(getmap && null != update) {
            return update.after;
        }
        return after;
    }

    public Integer getIncrementSeed() {
        if(getmap && null != update) {
            return update.incrementSeed;
        }
        return incrementSeed;
    }

    public Column setIncrementSeed(Integer incrementSeed) {
        if(setmap && null != update) {
            update.setIncrementSeed(incrementSeed);
            return this;
        }
        this.incrementSeed = incrementSeed;
        return this;
    }

    public Integer getIncrementStep() {
        if(getmap && null != update) {
            return update.incrementStep;
        }
        return incrementStep;
    }

    public Column setIncrementStep(Integer incrementStep) {
        if(setmap && null != update) {
            update.setIncrementStep(incrementStep);
            return this;
        }
        this.incrementStep = incrementStep;
        return this;
    }

    public String getOnUpdate() {
        if(getmap && null != update) {
            return update.onUpdate;
        }
        return onUpdate;
    }

    public Column setOnUpdate(String onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }
    public Column onUpdate(String onUpdate) {
        return setOnUpdate(onUpdate);
    }


    public Column setAfter(String after) {
        if(setmap && null != update) {
            update.setAfter(after);
            return this;
        }
        this.after = after;
        return this;
    }

    public String getOriginName() {
        if(getmap && null != update) {
            return update.originName;
        }
        return originName;
    }

    public Column setOriginName(String originName) {
        if(setmap && null != update) {
            update.setOriginName(originName);
            return this;
        }
        this.originName = originName;
        return this;
    }

    public String getBefore() {
        if(getmap && null != update) {
            return update.before;
        }
        return before;
    }

    public String getCharset() {
        if(getmap && null != update) {
            return update.charset;
        }
        return charset;
    }

    public Column setCharset(String charset) {
        if(setmap && null != update) {
            update.setCharset(charset);
            return this;
        }
        this.charset = charset;
        return this;
    }

    public String getLengthUnit() {
        if(getmap && null != update) {
            return update.lengthUnit;
        }
        return lengthUnit;
    }

    public Column setLengthUnit(String lengthUnit) {
        if(setmap && null != update) {
            update.setLengthUnit(lengthUnit);
            return this;
        }
        this.lengthUnit = lengthUnit;
        return this;
    }

    public String getCollate() {
        if(getmap && null != update) {
            return update.collate;
        }
        return collate;
    }

    public Column setCollate(String collate) {
        if(setmap && null != update) {
            update.setCollate(collate);
            return this;
        }
        this.collate = collate;
        return this;
    }
    public Column setBefore(String before) {
        if(setmap && null != update) {
            update.setBefore(before);
            return this;
        }
        this.before = before;
        return this;
    }
    public boolean equals(Column column) {
        return equals(column, true);
    }
    
    public boolean equals(Column column, boolean ignoreCase) {
        if(null == column) {
            return false;
        }
        if (!BasicUtil.equals(name, column.getName(), ignoreCase)) {
            return false;
        }
        TypeMetadata columnTypeMetadata = column.getTypeMetadata();
        TypeMetadata origin = null;
        TypeMetadata columnOrigin = null;
        if(null != typeMetadata) {
            origin = typeMetadata.getOrigin();
        }
        if(null != columnTypeMetadata) {
            columnOrigin = columnTypeMetadata.getOrigin();
        }

        if(!BasicUtil.equals(typeMetadata, columnTypeMetadata, ignoreCase)
                && !BasicUtil.equals(typeMetadata, columnOrigin, ignoreCase)
                && !BasicUtil.equals(origin, columnTypeMetadata, ignoreCase)
                && !BasicUtil.equals(origin, columnOrigin, ignoreCase)
        ) {
            return false;
        }
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata || 0 == typeMetadata.ignoreLength()) {
            if (!BasicUtil.equals(getLength(), column.getLength())) {
                return false;
            }
        }
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata || 0 == typeMetadata.ignorePrecision()) {
            if (!BasicUtil.equals(getPrecision(), column.getPrecision())) {
                return false;
            }
        }
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata || 0 == typeMetadata.ignoreScale()) {
            if (!BasicUtil.equals(getScale(), column.getScale())) {
                return false;
            }
        }
        if(!BasicUtil.equals(getDefaultValue(), column.getDefaultValue())) {
            return false;
        }
        if(!BasicUtil.equals(BasicUtil.evl(getComment()), BasicUtil.evl(column.getComment()))) {
            return false;
        }
        if(!BasicUtil.equals(getNullable(), column.getNullable())) {
            return false;
        }
        Boolean isAutoIncrement = getAutoIncrement();
        if(isAutoIncrement == null){
            isAutoIncrement = false;
        }
        Boolean colAutoIncrement = column.getAutoIncrement();
        if(colAutoIncrement == null){
            colAutoIncrement = false;
        }
        if(isAutoIncrement != colAutoIncrement) {
            return false;
        }
        if(!BasicUtil.equals(getCharset(), column.getCharset(), ignoreCase)) {
            return false;
        }
        if(!BasicUtil.equals(getPrimaryKey(), column.getPrimaryKey())) {
            return false;
        }
        if(null != table && table.isSort()) {
            if (!BasicUtil.equals(getPosition(), column.getPosition())) {
                return false;
            }
        }
        return true;
    }
    
    public TypeMetadata getTypeMetadata() {
        if(getmap && null != update) {
            return update.typeMetadata;
        }
        if(array && null != typeMetadata) {
            typeMetadata.setArray(array);
        }
        return typeMetadata;
    }

    public TypeMetadata.CATEGORY getTypeCategory() {
        if(null != typeMetadata) {
            return typeMetadata.getCategory();
        }
        return TypeMetadata.CATEGORY.NONE;
    }

    public Column setTypeMetadata(TypeMetadata typeMetadata) {
        if(setmap && null != update) {
            update.setTypeMetadata(typeMetadata);
            return this;
        }
        this.typeMetadata = typeMetadata;
        return this;
    }

    public Column setNewName(String newName, boolean setmap, boolean getmap) {
        if(null == update) {
            update(setmap, getmap);
        }
        update.setName(newName);
        Table table = getTable();
        if(null != table) {
            //修改主键列名
            LinkedHashMap<String, Column> pks = table.getPrimaryKeyColumns();
            if(null != pks && pks.containsKey(this.getName().toUpperCase())) {
                pks.remove(this.getName().toUpperCase());
                pks.put(newName.toUpperCase(), update);
            }
        }
        return update;
    }
    
    public JavaType getJavaType() {
        if(getmap && null != update) {
            return update.javaType;
        }
        return javaType;
    }

    public Column setJavaType(JavaType javaType) {
        if(setmap && null != update) {
            update.setJavaType(javaType);
            return this;
        }
        this.javaType = javaType;
        return this;
    }

    public Integer getSrid() {
        if(getmap && null != update) {
            return update.srid;
        }
        return srid;
    }

    public Column setSrid(Integer srid) {
        if(setmap && null != update) {
            update.setSrid(srid);
            return this;
        }
        this.srid = srid;
        return this;
    }

    public Column getReference() {
        if(getmap && null != update) {
            return update.reference;
        }
        return reference;
    }

    public Column setReference(Column reference) {
        if(setmap && null != update) {
            update.setReference(reference);
            return this;
        }
        this.reference = reference;
        return this;
    }

    public ColumnFamily getFamily() {
        return family;
    }

    public Column setFamily(ColumnFamily family) {
        this.family = family;
        return this;
    }

    public void ignoreLength(int ignoreLength) {
        this.ignoreLength = ignoreLength;
    }

    public void maxLength(int maxLength) {
        this.maxLength = maxLength;
    }
    public void ignorePrecision(int ignorePrecision) {
        this.ignorePrecision = ignorePrecision;
    }
    public void maxPrecision(int maxPrecision) {
        this.maxPrecision = maxPrecision;
    }

    public void ignoreScale(int ignoreScale) {
        this.ignoreScale = ignoreScale;
    }
    public void maxScale(int maxScale) {
        this.maxScale = maxScale;
    }
    public int ignoreScale(DatabaseType database) {
        if(null != typeMetadata) {
            return MetadataReferHolder.ignoreScale(database, typeMetadata);
        }else{
            return ignoreScale();
        }
    }
    public String formula(DatabaseType database) {
        if(null != typeMetadata) {
            return MetadataReferHolder.formula(database, typeMetadata);
        }else{
            return null;
        }
    }

    /**
     * 是否需要指定精度 主要用来识别能取出精度，但DDL不需要精度的类型
     * 精确判断通过adapter
     * @return boolean
     */
    public int ignoreLength() {
        if(-1 != ignoreLength) {
            return ignoreLength;
        }
        if(null != typeMetadata) {
            return typeMetadata.ignoreLength();
        }
        return ignoreLength;
    }
    public int ignoreLength(DatabaseType database) {
        if(null != typeMetadata) {
            return MetadataReferHolder.ignoreLength(database, typeMetadata);
        }else{
            return ignoreLength();
        }
    }

    public int maxLength() {
        if(-1 != maxLength) {
            return maxLength;
        }
        if(null != typeMetadata) {
            return typeMetadata.maxLength();
        }
        return maxLength;
    }
    public int maxLength(DatabaseType database) {
        if(null != typeMetadata) {
            return MetadataReferHolder.maxLength(database, typeMetadata);
        }else{
            return maxLength();
        }
    }

    /**
     * 是否需要指定精度 主要用来识别能取出精度，但DDL不需要精度的类型
     * 精确判断通过adapter
     * @return boolean
     */
    public int ignorePrecision() {
        if(-1 != ignorePrecision) {
            return ignorePrecision;
        }
        if(null != typeMetadata) {
            return typeMetadata.ignorePrecision();
        }
        return ignorePrecision;
    }

    public int ignorePrecision(DatabaseType database) {
        if(null != typeMetadata) {
            return MetadataReferHolder.ignorePrecision(database, typeMetadata);
        }else{
            return ignorePrecision();
        }
    }

    public int maxPrecision() {
        if(-1 != maxPrecision) {
            return maxPrecision;
        }
        if(null != typeMetadata) {
            return typeMetadata.maxPrecision();
        }
        return maxPrecision;
    }

    public int maxPrecision(DatabaseType database) {
        if(null != typeMetadata) {
            return MetadataReferHolder.maxPrecision(database, typeMetadata);
        }else{
            return maxPrecision();
        }
    }

    public String getFinalType() {
        return finalType;
    }

    public Column setFinalType(String finalType) {
        this.finalType = finalType;
        return this;
    }

    /**
     * 是否需要指定精度 主要用来识别能取出精度，但DDL不需要精度的类型
     * 精确判断通过adapter
     * @return boolean
     */
    public int ignoreScale() {
        if(-1 != ignoreScale) {
            return ignoreScale;
        }
        if(null != typeMetadata) {
            return typeMetadata.ignoreScale();
        }
        return ignoreScale;
    }
    public int maxScale() {
        if(-1 != maxScale) {
            return maxScale;
        }
        if(null != typeMetadata) {
            return typeMetadata.maxScale();
        }
        return maxScale;
    }
    public int maxScale(DatabaseType database) {
        if(null != typeMetadata) {
            return MetadataReferHolder.maxScale(database, typeMetadata);
        }else{
            return maxScale();
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(name).append(" ");
        builder.append(getFullType());
        if(BasicUtil.isNotEmpty(defaultValue)) {
            builder.append(" default ").append(defaultValue);
        }
        return builder.toString();
    }
    public String keyword() {
        return this.keyword;
    }

/* ********************************* field refer ********************************** */
    public static final String FIELD_TYPE_CATEGORY_CONFIG          = "TYPE_CATEGORY_CONFIG";
    public static final String FIELD_KEYWORD                       = "KEYWORD";
    public static final String FIELD_ORIGIN_NAME                   = "ORIGIN_NAME";
    public static final String FIELD_ORIGIN_TYPE                   = "ORIGIN_TYPE";
    public static final String FIELD_TYPE_METADATA                 = "TYPE_METADATA";
    public static final String FIELD_FULL_TYPE                     = "FULL_TYPE";
    public static final String FIELD_FINAL_TYPE                    = "FINAL_TYPE";
    public static final String FIELD_IGNORE_LENGTH                 = "IGNORE_LENGTH";
    public static final String FIELD_IGNORE_PRECISION              = "IGNORE_PRECISION";
    public static final String FIELD_IGNORE_SCALE                  = "IGNORE_SCALE";
    public static final String FIELD_PRECISION_LENGTH              = "PRECISION_LENGTH";
    public static final String FIELD_LENGTH                        = "LENGTH";
    public static final String FIELD_OCTET_LENGTH                  = "OCTET_LENGTH";
    public static final String FIELD_PRECISION                     = "PRECISION";
    public static final String FIELD_SCALE                         = "SCALE";
    public static final String FIELD_DIMS                          = "DIMS";
    public static final String FIELD_CLASS_NAME                    = "CLASS_NAME";
    public static final String FIELD_DISPLAY_SIZE                  = "DISPLAY_SIZE";
    public static final String FIELD_TYPE                          = "TYPE";
    public static final String FIELD_CHILD_TYPE_NAME               = "CHILD_TYPE_NAME";
    public static final String FIELD_CHILD_TYPE_METADATA           = "CHILD_TYPE_METADATA";
    public static final String FIELD_JAVA_TYPE                     = "JAVA_TYPE";
    public static final String FIELD_JDBC_TYPE                     = "JDBC_TYPE";
    public static final String FIELD_DATE_SCALE                    = "DATE_SCALE";
    public static final String FIELD_NULLABLE                      = "NULLABLE";
    public static final String FIELD_CASE_SENSITIVE                = "CASE_SENSITIVE";
    public static final String FIELD_CURRENCY                      = "CURRENCY";
    public static final String FIELD_SIGNED                        = "SIGNED";
    public static final String FIELD_AUTO_INCREMENT                = "AUTO_INCREMENT";
    public static final String FIELD_AUTO_INCREMENT_CHECK          = "AUTO_INCREMENT_CHECK";
    public static final String FIELD_AUTO_INCREMENT_CHECK_VALUE    = "AUTO_INCREMENT_CHECK_VALUE";
    public static final String FIELD_INCREMENT_SEED                = "INCREMENT_SEED";
    public static final String FIELD_INCREMENT_STEP                = "INCREMENT_STEP";
    public static final String FIELD_PRIMARY                       = "PRIMARY";
    public static final String FIELD_PRIMARY_CHECK                 = "PRIMARY_CHECK";
    public static final String FIELD_PRIMARY_CHECK_VALUE           = "PRIMARY_CHECK_VALUE";
    public static final String FIELD_PRIMARY_TYPE                  = "PRIMARY_TYPE";
    public static final String FIELD_UNIQUE                        = "UNIQUE";
    public static final String FIELD_GENERATED                     = "GENERATED";
    public static final String FIELD_DEFAULT_VALUE                 = "DEFAULT_VALUE";
    public static final String FIELD_DEFAULT_CONSTRAINT            = "DEFAULT_CONSTRAINT";
    public static final String FIELD_CHARSET                       = "CHARSET";
    public static final String FIELD_COLLATE                       = "COLLATE";
    public static final String FIELD_AGGREGATION                   = "AGGREGATION";
    public static final String FIELD_WITH_TIME_ZONE                = "WITH_TIME_ZONE";
    public static final String FIELD_WITH_LOCAL_TIME_ZONE          = "WITH_LOCAL_TIME_ZONE";
    public static final String FIELD_SRID                          = "SRID";
    public static final String FIELD_ARRAY                         = "ARRAY";
    public static final String FIELD_ARRAY_CHECK                   = "ARRAY_CHECK";
    public static final String FIELD_ARRAY_CHECK_VALUE             = "ARRAY_CHECK_VALUE";
    public static final String FIELD_IS_KEY                        = "IS_KEY";
    public static final String FIELD_IS_KEY_CHECK                  = "IS_KEY_CHECK";
    public static final String FIELD_IS_KEY_CHECK_VALUE            = "IS_KEY_CHECK_VALUE";
    public static final String FIELD_POSITION                      = "POSITION";
    public static final String FIELD_ORDER                         = "ORDER";
    public static final String FIELD_AFTER                         = "AFTER";
    public static final String FIELD_BEFORE                        = "BEFORE";
    public static final String FIELD_ON_UPDATE                     = "ON_UPDATE";
    public static final String FIELD_ON_UPDATE_CHECK               = "ON_UPDATE_CHECK";
    public static final String FIELD_ON_UPDATE_CHECK_VALUE         = "ON_UPDATE_CHECK_VALUE";
    public static final String FIELD_VALUE                         = "VALUE";
    public static final String FIELD_DEFAULT_CURRENT_DATE_TIME     = "DEFAULT_CURRENT_DATE_TIME";
    public static final String FIELD_DEFAULT_CURRENT_DATE_TIME_CHECK = "DEFAULT_CURRENT_DATE_TIME_CHECK";
    public static final String FIELD_DEFAULT_CURRENT_DATE_TIME_CHECK_VALUE = "DEFAULT_CURRENT_DATE_TIME_CHECK_VALUE";
    public static final String FIELD_INDEX                         = "INDEX";
    public static final String FIELD_INDEX_CHECK                   = "INDEX_CHECK";
    public static final String FIELD_INDEX_CHECK_VALUE             = "INDEX_CHECK_VALUE";
    public static final String FIELD_STORE                         = "STORE";
    public static final String FIELD_STORE_CHECK                   = "STORE_CHECK";
    public static final String FIELD_STORE_CHECK_VALUE             = "STORE_CHECK_VALUE";
    public static final String FIELD_ANALYZER                      = "ANALYZER";
    public static final String FIELD_SEARCH_ANALYZER               = "SEARCH_ANALYZER";
    public static final String FIELD_IGNORE_ABOVE                  = "IGNORE_ABOVE";
    public static final String FIELD_COERCE                        = "COERCE";
    public static final String FIELD_COPY_TO                       = "COPY_TO";
    public static final String FIELD_DOC_VALUES                    = "DOC_VALUES";
    public static final String FIELD_DYNAMIC                       = "DYNAMIC";
    public static final String FIELD_EAGER_GLOBAL_ORDINALS         = "EAGER_GLOBAL_ORDINALS";
    public static final String FIELD_ENABLED                       = "ENABLED";
    public static final String FIELD_FORMAT                        = "FORMAT";
    public static final String FIELD_IGNORE_MALFORMED              = "IGNORE_MALFORMED";
    public static final String FIELD_INDEX_OPTIONS                 = "INDEX_OPTIONS";
    public static final String FIELD_INDEX_PHRASES                 = "INDEX_PHRASES";
    public static final String FIELD_INDEX_PREFIXES                = "INDEX_PREFIXES";
    public static final String FIELD_META                          = "META";
    public static final String FIELD_FIELDS                        = "FIELDS";
    public static final String FIELD_NORMALIZER                    = "NORMALIZER";
    public static final String FIELD_NORMS                         = "NORMS";
    public static final String FIELD_NULL_VALUE                    = "NULL_VALUE";
    public static final String FIELD_POSITION_INCREMENT_GAP        = "POSITION_INCREMENT_GAP";
    public static final String FIELD_PROPERTY                     = "PROPERTY";
    public static final String FIELD_SIMILARITY                    = "SIMILARITY";
    public static final String FIELD_SUB_OBJECTS                   = "SUB_OBJECTS";
    public static final String FIELD_TERM_VECTOR                   = "TERM_VECTOR";
    public static final String FIELD_PARSE_LVL                     = "PARSE_LVL";
    public static final String FIELD_FIELD_FAMILY                  = "FAMILY ";
}