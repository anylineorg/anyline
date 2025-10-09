package org.anyline.metadata;

import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.JavaType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.TypeMetadataHolder;
import org.anyline.util.BasicUtil;

public class DataTypeDefine {
    protected DatabaseType database               ; // 数据库类型
    protected String name                         ; // 类型名称 varchar完整类型调用getFullType > varchar(10)
    protected TypeMetadata metadata               ;
    protected String originName                   ; // 原名,只有查询时才会区分,添加列时用name即可 SELECT ID AS USER_ID FROM USER; originName=ID, name=USER_ID
    protected String qualifier                    ; // 数据类型限定符 DATETIME YEAR TO MINUTE(6) 中的YEAR TO MINUTE部分 表达式中以{Q}表示
    protected String originType                   ; // 原始类型(未解析,交给具体的adapter解析)
    protected String fullType                     ; // 完整类型名称
    protected String finalType                    ; // 如果设置了finalType 生成SQL时 name finalType 其他属性
    protected Integer type                        ; // 类型
    protected DataTypeDefine child                      ;
    protected JavaType javaType                   ;
    protected String jdbcType                     ; // 有可能与typeName不一致 可能多个typeName对应一个jdbcType 如point
    protected String className                    ; // 对应的Java数据类型 java.lang.Long

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
    protected Integer displaySize                 ; // display size
    protected String dateScale                    ; // 日期类型 精度
    protected Integer srid                        ; // SRID
    protected boolean array                       ; // 是否数组

    protected int parseLvl                      = 0;// 类型解析级别0:未解析 1:column解析 2:adapter解析
    public DataTypeDefine() {
    }
    public DataTypeDefine(String name) {
        setName(name, true);
    }
    public DataTypeDefine(String name, int precision, int scale) {
        setName(name, true);
        setPrecision(precision);
        setScale(scale);
    }
    public DataTypeDefine(String name, int precision) {
        setName(name, true);
        setPrecision(precision);
    }
    public DatabaseType database() {
        return database;
    }
    public void setDatabase(DatabaseType database) {
       this.database = database;
    }

    public String getOriginType() {
        return originType;
    }

    public void setOriginType(String originType) {
        this.originType = originType;
    }

    public TypeMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(TypeMetadata metadata) {
        this.metadata = metadata;
    }

    public String getFinalType() {
        return finalType;
    }

    public void setFinalType(String finalType) {
        this.finalType = finalType;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public int getParseLvl() {
        return parseLvl;
    }

    public void setParseLvl(int parseLvl) {
        this.parseLvl = parseLvl;
    }
    public DataTypeDefine getChild() {
        return child;
    }
    public String getChildName() {
        if(null != child){
            return child.getName();
        }
        return null;
    }

    public void setChild(DataTypeDefine child) {
        this.child = child;
    }
    public void setChild(String child) {
        this.child = new DataTypeDefine(child);
    }

    public String getQualifier() {
        return qualifier;
    }

    public DataTypeDefine setQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    public DataTypeDefine setDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public String getDateScale() {
        return dateScale;
    }

    public DataTypeDefine setDateScale(String dateScale) {
        this.dateScale = dateScale;
        return this;
    }

    public Integer getDisplaySize() {
        return displaySize;
    }

    public DataTypeDefine setDisplaySize(Integer displaySize) {
        this.displaySize = displaySize;
        return this;
    }

    public String getName() {
        if(null == name) {
            if(null != metadata && metadata != TypeMetadata.ILLEGAL && metadata != TypeMetadata.NONE) {
                name = metadata.getName();
            }
        }
        return name;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public DataTypeDefine setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }
    
    /**
     * 设置数据类型 根据数据库定义的数据类型
     * @param name 数据类型 如 int  varchar(10) decimal(18, 6)
     * @return DataType
     */
    public DataTypeDefine setName(String name, boolean parse) {
        if(null == this.name || !this.name.equalsIgnoreCase(name)) {
            //修改数据类型的重置解析状态
            parseLvl = 0;
        }

        this.ignorePrecision = -1;
        this.ignoreLength = -1;
        this.ignoreScale = -1;
        this.array = false;
        this.name = name;
        if(parse) {
            this.metadata = null;
            setOriginType(name);
            parse(1, database);
        }
        //fullType = null;
        return this;
    }

    public String getClassName() {
        return className;
    }

    public DataTypeDefine setClassName(String className) {
        this.className = className;
        return this;
    }
    
    /**
     *
     * @param lvl 解析阶段
     * @param database 新数据库类型
     * @return DataType
     */
    public DataTypeDefine parse(int lvl, DatabaseType database) {
        if(lvl <= parseLvl) {
            return this;
        }
        this.metadata = TypeMetadata.parse(database, this, TypeMetadataHolder.gets(database), null);
        return this;
    }
 

    public DataTypeDefine setFullType(String fullType) {
        this.fullType = fullType;
        return this;
    }
    public String getFullType() {
        return getFullType(database);
    }

    public String getFullType(DatabaseType database) {
        return getFullType(database, null);
    }
    public String getFullType(DatabaseType database, TypeMetadata.Refer refer) {
        if(null != fullType && this.database == database) {
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
        if(null != metadata && metadata != TypeMetadata.NONE && metadata != TypeMetadata.ILLEGAL && database == this.database) {
            type = metadata.getName();
        }else{
            type = getName();
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

                String childName = null;
                if(null != child){
                    childName = child.getName();
                }
                Integer srid = getSrid();
                if (null != childName) {
                    builder.append("(");
                    builder.append(childName);
                    if (null != srid) {
                        builder.append(", ").append(srid);
                    }
                    builder.append(")");
                }
                result = builder.toString();
            }
        }
        if(BasicUtil.isNotEmpty(result)){
            if (isArray()) {
                result += "[]";
            }
        }
        return result;
    }
    public boolean isArray() {
        return array;
    }

    public DataTypeDefine setArray(boolean array) {
        this.array = array;
        return this;
    }

    public Integer getSrid() {
        return srid;
    }

    public void setSrid(Integer srid) {
        this.srid = srid;
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
        if(null != length && length != -1) {
            return length;
        }
        return precision;
    }
    public DataTypeDefine resetLength(Integer length){
        if(null != originType) {
            originType = originType.replace("(" + this.length, "(" + length);
        }
        setLength(length);
        setParseLvl(0);
        return this;
    }
    public DataTypeDefine setLength(Integer length) {
        if(ignoreLength == 1) {
            this.precision = length;
        }else {
            this.length = length;
        }
        //fullType = null;
        return this;
    }

    public Integer getOctetLength() {
        return octetLength;
    }
    public DataTypeDefine setOctetLength(Integer length) {
        this.octetLength = length;
        return this;
    }

    public Integer getPrecision() {
        if(null != precision && precision != -1) {
            return precision;
        }
        return length;
    }

    public DataTypeDefine setPrecision(Integer precision) {
        if(ignorePrecision == 1) {
            this.length = precision;
        }else {
            this.precision = precision;
        }
        //fullType = null;
        return this;
    }
    public DataTypeDefine setPrecision(Integer precision, Integer scale) {
        this.precision = precision;
        this.scale = scale;
        //fullType = null;
        return this;
    }

    public Integer getScale() {
        return scale;
    }

    public DataTypeDefine setScale(Integer scale) {
        this.scale = scale;
        //fullType = null;
        return this;
    }

    public String getOriginName() {
        return originName;
    }

    public DataTypeDefine setOriginName(String originName) {
        this.originName = originName;
        return this;
    }

    public String getLengthUnit() {
        return lengthUnit;
    }

    public DataTypeDefine setLengthUnit(String lengthUnit) {
        this.lengthUnit = lengthUnit;
        return this;
    }

    public TypeMetadata getTypeMetadata() {
        if(array && null != metadata) {
            metadata.setArray(array);
        }
        return metadata;
    }

    public TypeMetadata.CATEGORY getTypeCategory() {
        if(null != metadata) {
            return metadata.getCategory();
        }
        return TypeMetadata.CATEGORY.NONE;
    }

    public DataTypeDefine setTypeMetadata(TypeMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public DataTypeDefine setJavaType(JavaType javaType) {
        this.javaType = javaType;
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
        if(null != metadata) {
            return MetadataReferHolder.ignoreScale(database, metadata);
        }else{
            return ignoreScale();
        }
    }
    public String formula(DatabaseType database) {
        if(null != metadata) {
            return MetadataReferHolder.formula(database, metadata);
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
        if(null != metadata) {
            return metadata.ignoreLength();
        }
        return ignoreLength;
    }
    public int ignoreLength(DatabaseType database) {
        if(null != metadata) {
            return MetadataReferHolder.ignoreLength(database, metadata);
        }else{
            return ignoreLength();
        }
    }

    public int maxLength() {
        if(-1 != maxLength) {
            return maxLength;
        }
        if(null != metadata) {
            return metadata.maxLength();
        }
        return maxLength;
    }
    public int maxLength(DatabaseType database) {
        if(null != metadata) {
            return MetadataReferHolder.maxLength(database, metadata);
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
        if(null != metadata) {
            return metadata.ignorePrecision();
        }
        return ignorePrecision;
    }

    public int ignorePrecision(DatabaseType database) {
        if(null != metadata) {
            return MetadataReferHolder.ignorePrecision(database, metadata);
        }else{
            return ignorePrecision();
        }
    }

    public int maxPrecision() {
        if(-1 != maxPrecision) {
            return maxPrecision;
        }
        if(null != metadata) {
            return metadata.maxPrecision();
        }
        return maxPrecision;
    }

    public int maxPrecision(DatabaseType database) {
        if(null != metadata) {
            return MetadataReferHolder.maxPrecision(database, metadata);
        }else{
            return maxPrecision();
        }
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
        if(null != metadata) {
            return metadata.ignoreScale();
        }
        return ignoreScale;
    }
    public int maxScale() {
        if(-1 != maxScale) {
            return maxScale;
        }
        if(null != metadata) {
            return metadata.maxScale();
        }
        return maxScale;
    }
    public int maxScale(DatabaseType database) {
        if(null != metadata) {
            return MetadataReferHolder.maxScale(database, metadata);
        }else{
            return maxScale();
        }
    }

    /* ********************************* field refer ********************************** */
    public static final String FIELD_TYPE_CATEGORY_CONFIG          = "TYPE_CATEGORY_CONFIG";
    public static final String FIELD_KEYWORD                       = "KEYWORD";
    public static final String FIELD_NAME                          = "TYPE";
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
    public static final String FIELD_CHILD_TYPE_NAME               = "CHILD_TYPE_NAME";
    public static final String FIELD_CHILD_TYPE_METADATA           = "CHILD_TYPE_METADATA";
    public static final String FIELD_JAVA_TYPE                     = "JAVA_TYPE";
    public static final String FIELD_JDBC_TYPE                     = "JDBC_TYPE";
    public static final String FIELD_DATE_SCALE                    = "DATE_SCALE";
}
