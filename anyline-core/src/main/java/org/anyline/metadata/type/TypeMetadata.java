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

package org.anyline.metadata.type;

import org.anyline.metadata.DataTypeDefine;
import org.anyline.metadata.refer.MetadataReferHolder;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.util.BasicUtil;
import org.anyline.util.regular.RegularUtil;

import java.lang.reflect.Field;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface TypeMetadata {
    /**
     * CATEGORY主要是对数据库中数据类型的归类主要用亚区分 length/precision/scale,如text类型不需要length而varchar类型需要
     * CATEGORY_GROUP是对CATEGORY的进一步归类，更接近Java类型
     */
    enum CATEGORY_GROUP{STRING, NUMBER, BOOLEAN, BYTES, DATETIME, COLLECTION, GEOMETRY, INTERVAL, VECTOR, OTHER, NONE}
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
        VECTOR(CATEGORY_GROUP.VECTOR, 1, 2, 3),
        OTHER(CATEGORY_GROUP.OTHER, 1, 1, 1),
        NONE(CATEGORY_GROUP.NONE, 1, 1, 1);
        private final CATEGORY_GROUP group;
        private final int ignoreLength;
        private final int ignorePrecision;
        private final int ignoreScale;
        private int maxLength = -1;
        private int maxPrecision = -1;
        private int maxScale = -1;
        private Refer refer;
        CATEGORY(CATEGORY_GROUP group, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
            this.group = group;
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
            this.maxLength = maxLength;
            this.maxPrecision = maxPrecision;
            this.maxScale = maxScale;
        }
        CATEGORY(CATEGORY_GROUP group, int ignoreLength, int ignorePrecision, int ignoreScale) {
            this.group = group;
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public CATEGORY_GROUP group() {
            return group;
        }

        public Refer refer() {
            if(null == refer) {
                refer = new Refer();
                refer.ignoreLength(ignoreLength).ignorePrecision(ignorePrecision).ignoreScale(ignoreScale)
                        .maxLength(maxLength).maxPrecision(maxPrecision).maxScale(maxScale);
            }
            return refer;
        }
    }
    enum NUMBER_LENGTH_UNIT {
        BIT(1),BYTE(8);
        private final int bits;
        NUMBER_LENGTH_UNIT(int bits){
            this.bits = bits;
        }
        public int bits(){
            return bits;
        }
    }
    default boolean equals(TypeMetadata metadata) {
        if(null == metadata) {
            return false;
        }
        if(this.getOrigin() == metadata) {
            return true;
        }
        if(this == metadata) {
            return true;
        }
        if(this == metadata.getOrigin()) {
            return true;
        }
        if(this.getOrigin() == metadata.getOrigin()) {
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
        public int maxLength() {
            return -1;
        }
        @Override
        public int maxPrecision() {
            return -1;
        }

        @Override
        public int maxScale() {
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
        public Object write(Object value, Object def, Boolean placeholder) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return null;
        }
        @Override
        public Refer refer() {
            return new Refer();
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
        public int maxLength() {
            return -1;
        }
        @Override
        public int maxPrecision() {
            return -1;
        }

        @Override
        public int maxScale() {
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
        public Refer refer() {
            return new Refer();
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
        public Object write(Object value, Object def, Boolean placeholder) {
            return value;
        }

        @Override
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return value;
        }
    };
    CATEGORY getCategory();
    CATEGORY_GROUP getCategoryGroup();
    String getName();
    default TypeMetadata getOrigin() {
        return this;
    }
    int ignoreLength();
    int ignorePrecision();
    int ignoreScale();
    int maxLength();
    int maxPrecision();
    int maxScale();
    boolean support();
    default String formula() {
        return null;
    }
    default boolean isArray() {
        return false;
    }
    void setArray(boolean array);
    Refer refer();
    /**
     * 写入数据库或查询条件时的类型
     * @return Class
     */
    Class compatible();

    /**
     * 中间转换类型
     * 如 value(double[])  &gt;  transfer(Point) &gt; byte[](compatible)
     * @return Class
     */
    Class transfer();

    /**
     * 支持的数据库
     * @return DatabaseType
     */
    List<DatabaseType> databaseTypes();

    Object convert(Object value, Object def);
    default Object convert(Object value, Class target) {
        return convert(value, target, false);
    }
    Object convert(Object value, Class target, boolean array);
    default Object convert(Object value, Class target, Object def) {
        return convert(value, target, false, def);
    }
    Object convert(Object value, Class target, boolean array, Object def);
    Object convert(Object value, Object obj, Field field);

    default Object read(Object value, Object def, Class clazz) {
        return read(value, def, clazz, false);
    }
    Object read(Object value, Object def, Class clazz, boolean array);
    default Object write(Object value, Object def, Boolean placeholder) {
        return write(value, def, false, placeholder);
    }
    Object write(Object value, Object def, boolean array, Boolean placeholder);

    class Refer {
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

        private int maxLength = -1;
        private int maxPrecision = -1;
        private int maxScale = -1;
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
        public Refer() {}
        public Refer(String meta, String formula, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
            setMeta(meta);
            setFormula(formula);
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
            this.maxLength = maxLength;
            this.maxPrecision = maxPrecision;
            this.maxScale = maxScale;
        }
        public Refer(String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale, int maxLength, int maxPrecision, int maxScale) {
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
            this.maxLength = maxLength;
            this.maxPrecision = maxPrecision;
            this.maxScale = maxScale;
        }
        public Refer(String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale) {
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public Refer(String lengthRefer, String precisionRefer, String scaleRefer) {
            setLengthRefer(lengthRefer);
            setScaleRefer(scaleRefer);
            setPrecisionRefer(precisionRefer);
        }

        public Refer(int ignoreLength, int ignorePrecision, int ignoreScale) {
            this.ignoreLength = ignoreLength;
            this.ignorePrecision = ignorePrecision;
            this.ignoreScale = ignoreScale;
        }
        public int ignoreLength() {
            return ignoreLength;
        }

        public Refer ignoreLength(int ignoreLength) {
            this.ignoreLength = ignoreLength;
            return this;
        }

        public int ignorePrecision() {
            return ignorePrecision;
        }

        public Refer ignorePrecision(int ignorePrecision) {
            this.ignorePrecision = ignorePrecision;
            return this;
        }

        public int ignoreScale() {
            return ignoreScale;
        }

        public Refer ignoreScale(int ignoreScale) {
            this.ignoreScale = ignoreScale;
            return this;
        }

        public String[] getLengthRefers() {
            return lengthRefers;
        }
        public String getLengthRefer() {
            if(null != lengthRefers && lengthRefers.length > 0) {
                return lengthRefers[0];
            }
            return null;
        }

        public Refer setLengthRefers(String[] lengthRefers) {
            this.lengthRefers = lengthRefers;
            return this;
        }

        public Refer setLengthRefer(String lengthRefer) {
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

        public String getPrecisionRefer() {
            if(null != precisionRefers && precisionRefers.length > 0) {
                return precisionRefers[0];
            }
            return null;
        }
        public Refer setPrecisionRefers(String[] precisionRefers) {
            this.precisionRefers = precisionRefers;
            return this;
        }

        public Refer setPrecisionRefer(String precisionRefer) {
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

        public String getScaleRefer() {
            if(null != scaleRefers && scaleRefers.length > 0) {
                return scaleRefers[0];
            }
            return null;
        }
        public Refer setScaleRefers(String[] scaleRefers) {
            this.scaleRefers = scaleRefers;
            return this;
        }

        public Refer setScaleRefer(String scaleRefer) {
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

        public int maxLength() {
            return maxLength;
        }

        public Refer maxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public int maxPrecision() {
            return maxPrecision;
        }

        public Refer maxPrecision(int maxPrecision) {
            this.maxPrecision = maxPrecision;
            return this;
        }

        public int maxScale() {
            return maxScale;
        }

        public Refer maxScale(int maxScale) {
            this.maxScale = maxScale;
            return this;
        }

        /**
         * 合并copy的属性(非空并且!=-1的属性)
         * @param copy 复本
         * @return Config
         */
        public Refer merge(Refer copy) {
            if(null != copy) {
                String meta = copy.getMeta();
                String formula = copy.getFormula();
                int ignoreLength = copy.ignoreLength();
                int ignorePrecision = copy.ignorePrecision;
                int ignoreScale = copy.ignoreScale();
                int maxLength = copy.maxLength;
                int maxPrecision = copy.maxPrecision;
                int maxScale = copy.maxScale;

                if(BasicUtil.isNotEmpty(meta)) {
                    this.meta = meta;
                }
                if(BasicUtil.isNotEmpty(formula)) {
                    this.formula = formula;
                }
                if(-1 != ignoreLength) {
                    this.ignoreLength = ignoreLength;
                }
                if(-1 != ignorePrecision) {
                    this.ignorePrecision = ignorePrecision;
                }
                if(-1 != ignoreScale) {
                    this.ignoreScale = ignoreScale;
                }

                if(- 1 != maxLength) {
                    this.maxLength = maxLength;
                }
                if(-1 != maxPrecision) {
                    this.maxPrecision = maxPrecision;
                }
                if(-1 != maxScale) {
                    this.maxScale = maxScale;
                }
                String[] lengthRefers = copy.getLengthRefers();;
                String[] precisionRefers = copy.getPrecisionRefers();
                String[] scaleRefers = copy.getScaleRefers();
                if(null != lengthRefers && lengthRefers.length > 0) {
                    this.lengthRefers = lengthRefers;
                }
                if(null != precisionRefers && precisionRefers.length > 0) {
                    this.precisionRefers = precisionRefers;
                }
                if(null != scaleRefers && scaleRefers.length > 0) {
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
    static TypeMetadata parse(DatabaseType database, DataTypeDefine meta, LinkedHashMap<String, TypeMetadata> alias, Map<String,String> spells) {
        if(null == meta) {
            return null;
        }
        NUMBER_LENGTH_UNIT numberLengthUnit = null;
        DatabaseType srcType = meta.database();
        if(null != srcType) {
            numberLengthUnit = srcType.numberLengthUnit();
        }
        NUMBER_LENGTH_UNIT targetNumberLengthUnit = null;
        if(null != database) {
            targetNumberLengthUnit = database.numberLengthUnit();
        }

        boolean array = meta.isArray();
        String originType = meta.getOriginType();
        if(null == originType) {
            return null;
        }
        String typeName = originType;
        String up = typeName.toUpperCase();
        TypeMetadata typeMetadata = meta.getTypeMetadata();
        if(null != typeMetadata && TypeMetadata.NONE != typeMetadata && meta.getParseLvl() >=2 && meta.database() == database) {
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
            if (typeName.contains(" ")) {
                // TYPE_NAME=int identity
                typeName = typeName.split(" ")[0];
            }
        }

        if("java.lang.Long".equals(meta.getClassName())) {
            typeName = "BIGINT";
        }
        typeMetadata = parse(alias, spells, typeName, numberLengthUnit, targetNumberLengthUnit);

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

        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata) {
            try{
                //varchar(10)
                //TIMESTAMP (6) WITH TIME ZONE
                //INTERVAL YEAR(4) TO MONTH
                //INTERVAL YEAR(4) TO MONTH(2)
                List<List<String>> fetches = RegularUtil.fetchs(up, "\\((\\d+)\\)");
                if(!fetches.isEmpty()) {
                    if(fetches.size() == 2) {
                        //INTERVAL YEAR(4) TO MONTH(2)
                        precision = BasicUtil.parseInt(fetches.get(0).get(1), null);
                        scale = BasicUtil.parseInt(fetches.get(1).get(1), null);
                        typeName = typeName.replace(fetches.get(0).get(0), "");
                        typeName = typeName.replace(fetches.get(1).get(0), "");
                        typeMetadata = parse(alias, spells, typeName, numberLengthUnit, targetNumberLengthUnit);
                    }else{
                        //varchar(10)
                        //decimal(20)
                        //TIMESTAMP (6) WITH TIME ZONE
                        //INTERVAL YEAR(4) TO MONTH
                        List<String> items = fetches.get(0);
                        typeName = typeName.replace(items.get(0), ""); // TIMESTAMP (6) WITH TIME ZONE > TIMESTAMP WITH TIME ZONE
                        Integer num = BasicUtil.parseInt(items.get(1), null);
                        typeMetadata = parse(alias, spells, typeName, numberLengthUnit, targetNumberLengthUnit);
                        if(null != typeMetadata) {
                            TypeMetadata.CATEGORY_GROUP group = typeMetadata.getCategoryGroup();
                            if(group == TypeMetadata.CATEGORY_GROUP.NUMBER) {
                                precision = num;
                            }else if(group == TypeMetadata.CATEGORY_GROUP.DATETIME) {
                                scale = num;
                            }else if(group == CATEGORY_GROUP.INTERVAL) {
                                precision = num;
                            }else{
                                length = num;
                            }
                        }
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata) {
                try{
                    //decimal(10,2)
                    List<List<String>> fetchs = RegularUtil.fetchs(up, "\\((\\d+)\\s*,\\s*(\\d)\\)");
                    if(!fetchs.isEmpty()) {
                        List<String> items = fetchs.get(0);
                        String full = items.get(0);//(6,2)
                        typeName = typeName.replace(full, "").trim(); // decimal(10,2) > decimal
                        precision = BasicUtil.parseInt(items.get(1), 0);
                        scale = BasicUtil.parseInt(items.get(2), 0);
                        typeMetadata = parse(alias, spells, typeName, numberLengthUnit, targetNumberLengthUnit);
                    }
                }catch (Exception e) {
                    e.printStackTrace();
                }
        }
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata) {
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
                        meta.setChild(lens[0]);
                        meta.setSrid(BasicUtil.parseInt(lens[1], null));
                    }
                } else {
                    //没有精度和srid
                    if (BasicUtil.isNumber(tmp)) {
                        precision = BasicUtil.parseInt(tmp, null);
                    } else {
                        meta.setChild(tmp);
                    }
                }
                typeName = typeName.substring(0, typeName.indexOf("("));
            }
        }
		/*if(!BasicUtil.equalsIgnoreCase(typeName, this.typeName)) {
			this.className = null;
		}*/
        if(null == typeMetadata || TypeMetadata.NONE == typeMetadata) {
            typeMetadata = parse(alias, spells, typeName, numberLengthUnit, targetNumberLengthUnit);
        }
        if(null != typeMetadata && TypeMetadata.NONE != typeMetadata) {
            meta.setTypeMetadata(typeMetadata);
            meta.setName(typeMetadata.getName(), false);
        }else{
            //没有对应的类型原们输出
            meta.setFullType(originType);
            meta.setTypeMetadata(TypeMetadata.NONE);
        }
        meta.setOriginType(originType);
        meta.setArray(array);
        int ignoreLength = MetadataReferHolder.ignoreLength(database, typeMetadata);
        int ignorePrecision = MetadataReferHolder.ignorePrecision(database, typeMetadata);
        int ignoreScale = MetadataReferHolder.ignoreScale(database, typeMetadata);

        if(null != precision && precision > 0) {
            //指定了长度或有效位数
            if(ignorePrecision != 1) {
                //设置有效位数
                meta.setPrecision(precision);
            }else if(ignoreLength != 1) {
                //不需要有效位数再考虑长度
                if(null == length || length <= 0) {
                    length = precision;
                }
            }
        }
        if(null != scale && scale > -1) {
            if(ignoreScale != 1) {
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

    /**
     *
     * @param alias 别名对应
     * @param spells 不同拼写对应
     * @param name 类型名
     * @param numberLengthUnit 原单位
     * @param targetNumberLengthUnit 新单位
     * @return TypeMetadata
     */
    static TypeMetadata parse(LinkedHashMap<String, TypeMetadata> alias, Map<String,String> spells, String name, NUMBER_LENGTH_UNIT numberLengthUnit, NUMBER_LENGTH_UNIT targetNumberLengthUnit) {
        if(null == name) {
            return null;
        }
        TypeMetadata type = null;
        name = name.toUpperCase();
        if(null != numberLengthUnit && null != targetNumberLengthUnit && numberLengthUnit != targetNumberLengthUnit){
            //需要转换
            //原来有单位，现在也有 并且单位不一样
            int src_bit = numberLengthUnit.bits;
            int target_bit = targetNumberLengthUnit.bits;
            if(name.contains("INT")) {
                try {
                    Matcher m = Pattern.compile("INT(\\d+)").matcher(name);
                    if(m.find()) {
                        String src = m.group();
                        int num = Integer.parseInt(m.group(1));
                        int tar = num*src_bit/target_bit;
                        String target = src.replace(String.valueOf(num), String.valueOf(tar));
                        name = name.replace(src, target);
                    }
                }catch (Exception ignore){}
            }
        }

        if(null != alias) {
            type = alias.get(name);
        }
        if(null == type) {
            try {
                type = StandardTypeMetadata.valueOf(name);
            }catch (Exception ignored) {}
        }
        if(null == type && null != spells) {//拼写兼容  下划线空格兼容
            if(null != alias) {
                type = alias.get(spells.get(name));
            }
            if(null == type) {
                try {
                    type = StandardTypeMetadata.valueOf(spells.get(name));
                }catch (Exception ignored) {}
            }
        }
        if(null == type) {
            try {
                type = StandardTypeMetadata.valueOf(name.replace(" ", "_"));
            }catch (Exception ignored) {}
        }
        return type;
    }
}
