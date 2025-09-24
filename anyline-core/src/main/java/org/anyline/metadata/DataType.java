package org.anyline.metadata;

import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.JavaType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.TypeMetadataHolder;
import org.anyline.util.BasicUtil;

public class DataType {
    protected DatabaseType database               ; // 数据库类型
    protected String name                         ; // 类型名称 varchar完整类型调用getFullType > varchar(10)
    protected String originName                   ; // 原名,只有查询时才会区分,添加列时用name即可 SELECT ID AS USER_ID FROM USER; originName=ID, name=USER_ID
    protected String qualifier                    ; // 数据类型限定符 DATETIME YEAR TO MINUTE(6) 中的YEAR TO MINUTE部分 表达式中以{Q}表示
    protected String originType                   ; // 原始类型(未解析,交给具体的adapter解析)
    protected TypeMetadata metadata               ;
    protected String fullType                     ; // 完整类型名称
    protected String finalType                    ; // 如果设置了finalType 生成SQL时 name finalType 其他属性
    protected Integer type                        ; // 类型
    protected DataType child                      ;
    protected JavaType javaType                   ;
    protected String jdbcType                     ; // 有可能与typeName不一致 可能多个typeName对应一个jdbcType 如point>

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



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public DataType getChild() {
        return child;
    }

    public void setChild(DataType child) {
        this.child = child;
    }

    public String getQualifier() {
        return qualifier;
    }

    public DataType setQualifier(String qualifier) {
        this.qualifier = qualifier;
        return this;
    }

    public Integer getDimension() {
        return dimension;
    }

    public DataType setDimension(Integer dimension) {
        this.dimension = dimension;
        return this;
    }

    public String getDateScale() {
        return dateScale;
    }

    public DataType setDateScale(String dateScale) {
        this.dateScale = dateScale;
        return this;
    }

    public Integer getDisplaySize() {
        return displaySize;
    }

    public DataType setDisplaySize(Integer displaySize) {
        this.displaySize = displaySize;
        return this;
    }


    /**
     * 设置数据类型 根据数据库定义的数据类型 实际调用了setTypeName(String)
     * @param type  数据类型 如 int  varchar(10) decimal(18, 6)
     * @return DataType
     */
    public DataType setType(String type) {
        this.metadata = null;
        this.ignorePrecision = -1;
        this.ignoreLength = -1;
        this.ignoreScale = -1;
        this.array = false;
        return setTypeName(type);
    }


    public String getTypeName() {
        if(null == typeName) {
            if(null != metadata && metadata != TypeMetadata.ILLEGAL && metadata != TypeMetadata.NONE) {
                typeName = metadata.getName();
            }
        }
        return typeName;
    }

    public String getJdbcType() {
        return jdbcType;
    }

    public DataType setJdbcType(String jdbcType) {
        this.jdbcType = jdbcType;
        return this;
    }

    public DataType setTypeName(String typeName) {
        return setTypeName(typeName, true);
    }

    /**
     * 设置数据类型 根据数据库定义的数据类型
     * @param typeName 数据类型 如 int  varchar(10) decimal(18, 6)
     * @return DataType
     */
    public DataType setTypeName(String typeName, boolean parse) {
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
     * @return DataType
     */
    public DataType parseType(int lvl, DatabaseType database) {
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

    public DataType setFullType(String fullType) {
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
        if(null != metadata && metadata != TypeMetadata.NONE && metadata != TypeMetadata.ILLEGAL && database == this.databaseType) {
            type = metadata.getName();
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

    public DataType setArray(boolean array) {
        this.array = array;
        return this;
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
    public DataType resetLength(Integer length){
        if(null != originType) {
            originType = originType.replace("(" + this.length, "(" + length);
        }
        setLength(length);
        setParseLvl(0);
        return this;
    }
    public DataType setLength(Integer length) {
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
    public DataType setOctetLength(Integer length) {
        this.octetLength = length;
        return this;
    }

    public Integer getPrecision() {
        if(null != precision && precision != -1) {
            return precision;
        }
        return length;
    }

    public DataType setPrecision(Integer precision) {
        if(ignorePrecision == 1) {
            this.length = precision;
        }else {
            this.precision = precision;
        }
        //fullType = null;
        return this;
    }
    public DataType setPrecision(Integer precision, Integer scale) {
        this.precision = precision;
        this.scale = scale;
        //fullType = null;
        return this;
    }

    public Integer getScale() {
        return scale;
    }

    public DataType setScale(Integer scale) {
        this.scale = scale;
        //fullType = null;
        return this;
    }

    public String getOriginName() {
        return originName;
    }

    public DataType setOriginName(String originName) {
        this.originName = originName;
        return this;
    }

    public String getLengthUnit() {
        return lengthUnit;
    }

    public DataType setLengthUnit(String lengthUnit) {
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

    public DataType setTypeMetadata(TypeMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public DataType setJavaType(JavaType javaType) {
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
}
