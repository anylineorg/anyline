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



package org.anyline.metadata.type;

import org.anyline.metadata.Column;
import org.anyline.metadata.adapter.MetadataAdapterHolder;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public interface TypeMetadata {
    enum CATEGORY_GROUP{STRING, NUMBER, BOOLEAN, BYTES, DATETIME, COLLECTION, GEOMETRY, INTERVAL, OTHER, NONE}
    //要用来区分 length/precision
    //BLOB不需要长度 BYTES需要长度
    //TIMESTAMP在有些数据库中支持SCALE需要在单独的alias中设置如TIMESTAMP(6)
    enum CATEGORY{
        CHAR(CATEGORY_GROUP.STRING, 0, 1, 1),
        TEXT(CATEGORY_GROUP.STRING, 1, 1, 1),
        BOOLEAN(CATEGORY_GROUP.BOOLEAN, 1, 1, 1),
        BYTES(CATEGORY_GROUP.BYTES, 0, 1, 1),
        BLOB(CATEGORY_GROUP.BYTES, 1, 1, 1),
        INT(CATEGORY_GROUP.NUMBER, 0, 1, 1),
        FLOAT(CATEGORY_GROUP.NUMBER, 1, 0, 0),
        DATE(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        TIME(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        DATETIME(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        TIMESTAMP(CATEGORY_GROUP.DATETIME, 1, 1, 1),
        COLLECTION(CATEGORY_GROUP.COLLECTION, 1, 1, 1),
        GEOMETRY(CATEGORY_GROUP.GEOMETRY, 1, 1, 1),
        INTERVAL(CATEGORY_GROUP.INTERVAL, 1, 2, 3),
        OTHER(CATEGORY_GROUP.OTHER, 1, 1, 1),
        NONE(CATEGORY_GROUP.NONE, 1, 1, 1);
        private final CATEGORY_GROUP group;
        private final int ignoreLength;
        private final int ignorePrecision;

        private final int ignoreScale;
        private TypeMetadata.Config config;
        CATEGORY(CATEGORY_GROUP group, int ignoreLength, int ignorePrecision, int ignoreScale) {
            this.group = group;
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public CATEGORY_GROUP group(){
            return group;
        }

        public TypeMetadata.Config config() {
            if(null == config){
                config = new TypeMetadata.Config();
                config.setIgnoreLength(ignoreLength).setIgnorePrecision(ignorePrecision).setIgnoreScale(ignoreScale);
            }
            return config;
        }
    }
    default boolean equals(TypeMetadata metadata){
        if(null == metadata){
            return false;
        }
        if(this.getOrigin() == metadata){
            return true;
        }
        if(this == metadata){
            return true;
        }
        if(this == metadata.getOrigin()){
            return true;
        }
        if(this.getOrigin() == metadata.getOrigin()){
            return true;
        }
        return false;
    }

    TypeMetadata ILLEGAL = new TypeMetadata() {
        private final String name = "ILLEGAL";

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.NONE;
        }

        @Override
        public CATEGORY_GROUP getCategoryGroup() {
            return CATEGORY_GROUP.NONE;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int ignoreLength() {
            return -1;
        }
        @Override
        public int ignorePrecision() {
            return -1;
        }

        @Override
        public int ignoreScale() {
            return -1;
        }

        @Override
        public boolean support() {
            return false;
        }

        @Override
        public void setArray(boolean array) {

        }

        @Override
        public Class compatible() {
            return null;
        }

        @Override
        public Class transfer() {
            return null;
        }

        @Override
        public List<DatabaseType> databaseTypes() {
            return new ArrayList<>();
        }

        @Override
        public Object convert(Object value, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, boolean array) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, boolean array, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Object obj, Field field) {
            return null;
        }

        @Override
        public Object read(Object value, Object def, Class clazz) {
            return null;
        }

        @Override
        public Object read(Object value, Object def, Class clazz, boolean array) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean placeholder) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean array, boolean placeholder) {
            return null;
        }
        @Override
        public Config config() {
            return new TypeMetadata.Config();
        }
    };
    //不识别的类型 原样输出
    TypeMetadata NONE = new TypeMetadata() {
        private final String name = "NONE";

        @Override
        public CATEGORY getCategory() {
            return CATEGORY.NONE;
        }
        @Override
        public String getName() {
            return name;
        }

        @Override
        public int ignoreLength() {
            return -1;
        }
        @Override
        public int ignorePrecision() {
            return -1;
        }

        @Override
        public int ignoreScale() {
            return -1;
        }

        @Override
        public boolean support() {
            return true;
        }

        @Override
        public void setArray(boolean array) {

        }

        @Override
        public Config config() {
            return new TypeMetadata.Config();
        }

        @Override
        public Class compatible() {
            return null;
        }

        @Override
        public Class transfer() {
            return null;
        }

        @Override
        public CATEGORY_GROUP getCategoryGroup() {
            return CATEGORY_GROUP.NONE;
        }

        @Override
        public List<DatabaseType> databaseTypes() {
            return new ArrayList<>();
        }

        @Override
        public Object convert(Object value, Object def) {
            return value;
        }

        @Override
        public Object convert(Object value, Class target) {
            return value;
        }

        @Override
        public Object convert(Object value, Class target, Object def) {
            return value;
        }

        @Override
        public Object convert(Object value, Class target, boolean array) {
            return value;
        }

        @Override
        public Object convert(Object value, Class target, boolean array, Object def) {
            return value;
        }

        @Override
        public Object convert(Object value, Object obj, Field field) {
            return value;
        }

        @Override
        public Object read(Object value, Object def, Class clazz) {
            return value;
        }

        @Override
        public Object read(Object value, Object def, Class clazz, boolean array) {
            return value;
        }

        @Override
        public Object write(Object value, Object def, boolean placeholder) {
            return value;
        }

        @Override
        public Object write(Object value, Object def, boolean array, boolean placeholder) {
            return value;
        }
    };
    CATEGORY getCategory();
    CATEGORY_GROUP getCategoryGroup();
    String getName();
    default TypeMetadata getOrigin(){
        return this;
    }
    int ignoreLength();
    int ignorePrecision();
    int ignoreScale();
    boolean support();
    default String formula() {
        return null;
    }
    default boolean isArray(){
        return false;
    }
    void setArray(boolean array);
    TypeMetadata.Config config();
    /**
     * 写入数据库或查询条件时的类型
     * @return Class
     */
    Class compatible();

    /**
     * 中间转换类型
     * 如 value(double[]) > transfer(Point) > byte[](compatible)
     * @return Class
     */
    Class transfer();

    /**
     * 支持的数据库
     * @return DatabaseType
     */
    List<DatabaseType> databaseTypes();

    Object convert(Object value, Object def);
    default Object convert(Object value, Class target){
        return convert(value, target, false);
    }
    Object convert(Object value, Class target, boolean array);
    default Object convert(Object value, Class target, Object def){
        return convert(value, target, false, def);
    }
    Object convert(Object value, Class target, boolean array, Object def);
    Object convert(Object value, Object obj, Field field);

    default Object read(Object value, Object def, Class clazz){
        return read(value, def, clazz, false);
    }
    Object read(Object value, Object def, Class clazz, boolean array);
    default Object write(Object value, Object def, boolean placeholder){
        return write(value, def, false, placeholder);
    }
    Object write(Object value, Object def, boolean array, boolean placeholder);

    class Config {
        /**
         * SQL 数据类型(用来比较数据类型是否相同) INTERVAL DAY　TO HOUR
         * 不提供则根据NAME 生成
         */
        private String meta;
        /**
         * SQL生成公式如INTERVAL DAY(｛p｝) TO HOUR
         * 不提供则根据NAME 生成
         */
        private String formula;
        /**
         * 是否忽略长度，创建和比较时忽略，但元数据中可能会有对应的列也有值
         * -1:未设置可以继承上级 0:不忽略 1:忽略 2:根据情况(是否提供) 3:用来处理precision和scale相互依赖的情况,只有同时有值才生效,其中一个没值就全忽略
         */
        private int ignoreLength = -1;
        private int ignorePrecision = -1;
        private int ignoreScale = -1;
        /**
         * 读取元数据时 字符类型长度对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String[] lengthRefers;
        /**
         * 读取元数据时 数字类型长度对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String[] precisionRefers;
        /**
         * 读取元数据时 小数位对应的列<br/>
         * 正常情况下只有一列<br/>
         * 如果需要取多列以,分隔
         */
        private String[] scaleRefers;
        public Config(){}
        public Config(String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
            setMeta(meta);
            setFormula(formula);
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public Config(String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public Config(String lengthRefer, String precisionRefer, String scaleRefer){
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
        }

        public Config(int ignoreLength, int ignorePrecision, int ignoreScale){
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public int ignoreLength() {
            return ignoreLength;
        }

        public Config setIgnoreLength(int ignoreLength) {
            this.ignoreLength = ignoreLength;
            return this;
        }

        public int ignorePrecision() {
            return ignorePrecision;
        }

        public Config setIgnorePrecision(int ignorePrecision) {
            this.ignorePrecision = ignorePrecision;
            return this;
        }

        public int ignoreScale() {
            return ignoreScale;
        }

        public Config setIgnoreScale(int ignoreScale) {
            this.ignoreScale = ignoreScale;
            return this;
        }

        public String[] getLengthRefers() {
            return lengthRefers;
        }
        public String getLengthRefer(){
            if(null != lengthRefers && lengthRefers.length > 0){
                return lengthRefers[0];
            }
            return null;
        }

        public Config setLengthRefers(String[] lengthRefers) {
            this.lengthRefers = lengthRefers;
            return this;
        }

        public Config setLengthRefer(String lengthRefer) {
            if(BasicUtil.isNotEmpty(lengthRefer)) {
                this.lengthRefers = lengthRefer.split(",");
            } else {

                this.lengthRefers = null;
            }
            return this;
        }
        public String[] getPrecisionRefers() {
            return precisionRefers;
        }

        public String getPrecisionRefer(){
            if(null != precisionRefers && precisionRefers.length > 0){
                return precisionRefers[0];
            }
            return null;
        }
        public Config setPrecisionRefers(String[] precisionRefers) {
            this.precisionRefers = precisionRefers;
            return this;
        }

        public Config setPrecisionRefer(String precisionRefer) {
            if(BasicUtil.isNotEmpty(precisionRefer)) {
                this.precisionRefers = precisionRefer.split(",");
            } else {
                this.precisionRefers = null;
            }
            return this;
        }
        public String[] getScaleRefers() {
            return scaleRefers;
        }

        public String getScaleRefer(){
            if(null != scaleRefers && scaleRefers.length > 0){
                return scaleRefers[0];
            }
            return null;
        }
        public Config setScaleRefers(String[] scaleRefers) {
            this.scaleRefers = scaleRefers;
            return this;
        }

        public Config setScaleRefer(String scaleRefer) {
            if(BasicUtil.isNotEmpty(scaleRefer)) {
                this.scaleRefers = scaleRefer.split(",");
            } else {

                this.scaleRefers = null;
            }
            return this;
        }

        public String getFormula() {
            return formula;
        }

        public void setFormula(String formula) {
            this.formula = formula;
        }

        public String getMeta() {
            return meta;
        }

        public void setMeta(String meta) {
            this.meta = meta;
        }

        /**
         * 合并copy的属性(非空并且!=-1的属性)
         * @param copy 复本
         * @return Config
         */
        public Config merge(Config copy){
            if(null != copy){
                String meta = copy.getMeta();
                String formula = copy.getFormula();
                int ignoreLength = copy.ignoreLength();
                int ignorePrecision = copy.ignorePrecision;
                int ignoreScale = copy.ignoreScale();
                if(BasicUtil.isNotEmpty(meta)){
                    this.meta = meta;
                }
                if(BasicUtil.isNotEmpty(formula)){
                    this.formula = formula;
                }
                if(-1 != ignoreLength){
                    this.ignoreLength = ignoreLength;
                }
                if(-1 != ignorePrecision){
                    this.ignorePrecision = ignorePrecision;
                }
                if(-1 != ignoreScale){
                    this.ignoreScale = ignoreScale;
                }
                String[] lengthRefers = copy.getLengthRefers();;
                String[] precisionRefers = copy.getPrecisionRefers();
                String[] scaleRefers = copy.getScaleRefers();
                if(null != lengthRefers && lengthRefers.length > 0){
                    this.lengthRefers = lengthRefers;
                }
                if(null != precisionRefers && precisionRefers.length > 0){
                    this.precisionRefers = precisionRefers;
                }
                if(null != scaleRefers && scaleRefers.length > 0){
                    this.scaleRefers = scaleRefers;
                }
            }
            return this;
        }
    }

    /**
     * 解析数据类型
     * @param database 数据库类型 不确定的 可以用NONE
     * @param meta 列
     * @param alias 别名
     * @param spells 拼写兼容
     * @return TypeMetadata
     */
    static TypeMetadata parse(DatabaseType database, Column meta, LinkedHashMap<String, TypeMetadata> alias, Map<String,String> spells){
        if(null == meta){
            return null;
        }
        boolean array = false;
        String originType = meta.getOriginType();
        if(null == originType){
            return null;
        }
        String typeName = originType;
        String up = typeName.toUpperCase();
        TypeMetadata typeMetadata = meta.getTypeMetadata();
        if(null != typeMetadata && TypeMetadata.NONE != typeMetadata && meta.getParseLvl() >=2 && meta.getDatabase() == database){
            return typeMetadata;
        }
        Integer length = meta.getLength();
        Integer precision = meta.getPrecision();
        Integer scale = meta.getScale();

        //数组类型
        if (typeName.contains("[]")) {
            array = true;
            typeName = typeName.replace("[]", "");
        }
        //数组类型
        if (typeName.startsWith("_")) {
            typeName = typeName.substring(1);
            array = true;
        }
        typeName = typeName.trim().replace("'", "");

        if (typeName.toUpperCase().contains("IDENTITY")) {
            meta.autoIncrement(true);
            if (typeName.contains(" ")) {
                // TYPE_NAME=int identity
                typeName = typeName.split(" ")[0];
            }
        }
        typeMetadata = parse(alias, spells, typeName);

        /*
        decimal({p}, {S})
        varchar({l})
        INTERVAL YEAR(4) TO MONTH
        INTERVAL YEAR(4) TO MONTH(2)
        INTERVAL DAY({P}) TO HOUR({S})
        TIMESTAMP ({p}) WITH TIME ZONE
        TIMESTAMP WITH LOCAL TIME ZONE
        geometry(Polygon, 4326)
        geometry(Polygon)

        TIME WITH TIME ZONE
        TIMESTAMP WITH LOCAL TIME ZONE
        TIMESTAMP WITH TIME ZONE
        DOUBLE PRECISION
        BIT VARYING
        INTERVAL DAY
        INTERVAL DAY TO HOUR
        INTERVAL DAY TO MINUTE
        INTERVAL DAY TO SECOND
        INTERVAL HOUR
        INTERVAL HOUR TO MINUTE
        INTERVAL HOUR TO SECOND
        INTERVAL MINUTE
        INTERVAL MINUTE TO SECOND
        INTERVAL MONTH
        INTERVAL SECOND
        INTERVAL YEAR
        INTERVAL YEAR TO MONTH
        TIME TZ UNCONSTRAINED
        TIME WITHOUT TIME ZONE
        TIMESTAMP WITHOUT TIME ZONE
        */

        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata){
            try{
                //varchar(10)
                //TIMESTAMP (6) WITH TIME ZONE
                //INTERVAL YEAR(4) TO MONTH
                //INTERVAL YEAR(4) TO MONTH(2)
                List<List<String>> fetches = RegularUtil.fetchs(up, "\\((\\d+)\\)");
                if(!fetches.isEmpty()){
                    if(fetches.size() == 2){
                        //INTERVAL YEAR(4) TO MONTH(2)
                        precision = BasicUtil.parseInt(fetches.get(0).get(1), null);
                        scale = BasicUtil.parseInt(fetches.get(1).get(1), null);
                    }else{
                        //varchar(10)
                        //decimal(20)
                        //TIMESTAMP (6) WITH TIME ZONE
                        //INTERVAL YEAR(4) TO MONTH
                        List<String> items = fetches.get(0);
                        typeName = typeName.replace(items.get(0), ""); // TIMESTAMP (6) WITH TIME ZONE > TIMESTAMP WITH TIME ZONE
                        Integer num = BasicUtil.parseInt(items.get(1), null);
                        typeMetadata = parse(alias, spells, typeName);
                        if(null != typeMetadata){
                            TypeMetadata.CATEGORY_GROUP group = typeMetadata.getCategoryGroup();
                            if(group == TypeMetadata.CATEGORY_GROUP.NUMBER){
                                precision = num;
                            }else if(group == TypeMetadata.CATEGORY_GROUP.DATETIME){
                                scale = num;
                            }else if(group == CATEGORY_GROUP.INTERVAL){
                                precision = num;
                            }else{
                                length = num;
                            }
                        }
                    }

                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata){
                try{
                    //decimal(10,2)
                    List<List<String>> fetchs = RegularUtil.fetchs(up, "\\((\\d+)\\s*,\\s*(\\d)\\)");
                    if(!fetchs.isEmpty()){
                        List<String> items = fetchs.get(0);
                        String full = items.get(0);//(6,2)
                        typeName = typeName.replace(full, "").trim(); // decimal(10,2) > decimal
                        precision = BasicUtil.parseInt(items.get(1), 0);
                        scale = BasicUtil.parseInt(items.get(2), 0);
                        typeMetadata = parse(alias, spells, typeName);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
        }
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata){
            //geometry(Polygon)
            //geometry(Polygon, 4326)
            if (typeName.contains("(")) {
                String tmp = typeName.substring(typeName.indexOf("(") + 1, typeName.indexOf(")"));
                if (tmp.contains(",")) {
                    //有精度或srid
                    String[] lens = tmp.split("\\,");
                    if (BasicUtil.isNumber(lens[0])) {
                        precision = BasicUtil.parseInt(lens[0], null);
                        scale = BasicUtil.parseInt(lens[1], null);
                    } else {
                        meta.setChildTypeName(lens[0]);
                        meta.setSrid(BasicUtil.parseInt(lens[1], null));
                    }
                } else {
                    //没有精度和srid
                    if (BasicUtil.isNumber(tmp)) {
                        precision = BasicUtil.parseInt(tmp, null);
                    } else {
                        meta.setChildTypeName(tmp);
                    }
                }
                typeName = typeName.substring(0, typeName.indexOf("("));
            }
        }
		/*if(!BasicUtil.equalsIgnoreCase(typeName, this.typeName)) {
			this.className = null;
		}*/
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata) {
            typeMetadata = parse(alias, spells, typeName);
        }
        if(null != typeMetadata && TypeMetadata.NONE != typeMetadata) {
            meta.setTypeMetadata(typeMetadata);
            meta.setTypeName(typeMetadata.getName(), false);
        }else{
            //没有对应的类型原们输出
            meta.setFullType(originType);
            meta.setTypeMetadata(TypeMetadata.NONE);
        }
        meta.setOriginType(originType);
        meta.setArray(array);
        int ignoreLength = MetadataAdapterHolder.ignoreLength(database, typeMetadata);
        int ignorePrecision = MetadataAdapterHolder.ignorePrecision(database, typeMetadata);
        int ignoreScale = MetadataAdapterHolder.ignoreScale(database, typeMetadata);

        if(null != precision && precision > 0){
            //指定了长度或有效位数
            if(ignorePrecision != 1){
                //设置有效位数
                meta.setPrecision(precision);
            }else if(ignoreLength != 1){
                //不需要有效位数再考虑长度
                if(null == length || length <= 0){
                    length = precision;
                }
            }
        }
        if(null != scale && scale > -1){
            if(ignoreScale != 1){
                meta.setScale(scale);
            }
        }
        if(null != length) {
            meta.setLength(length);
        }
        if(null != database && database != DatabaseType.NONE) {
            meta.setParseLvl(2);
        }
        meta.setDatabase(database);
        return typeMetadata;
    }
    static TypeMetadata parse(LinkedHashMap<String, TypeMetadata> alias, Map<String,String> spells, String name){
        if(null == name){
            return null;
        }
        TypeMetadata type = null;
        name = name.toUpperCase();
        if(null != alias) {
            type = alias.get(name);
        }
        if(null == type){
            try {
                type = StandardTypeMetadata.valueOf(name);
            }catch (Exception ignored){}
        }
        if(null == type && null != spells){//拼写兼容  下划线空格兼容
            if(null != alias) {
                type = alias.get(spells.get(name));
            }
            if(null == type){
                try {
                    type = StandardTypeMetadata.valueOf(spells.get(name));
                }catch (Exception ignored){}
            }
        }
        if(null == type){
            try {
                type = StandardTypeMetadata.valueOf(name.replace(" ", "_"));
            }catch (Exception ignored){}
        }
        return type;
    }
}
