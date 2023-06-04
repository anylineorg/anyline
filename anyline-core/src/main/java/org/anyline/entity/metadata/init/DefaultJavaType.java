package org.anyline.entity.metadata.init;

import org.anyline.entity.DataRow;
import org.anyline.entity.metadata.Convert;
import org.anyline.entity.metadata.ConvertException;
import org.anyline.entity.metadata.DataType;
import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.LinkedHashMap;

public enum DefaultJavaType implements DataType {



    /* *****************************************************************************************************************
     *
     * 													JAVA DATA TYPE
     *
     * =================================================================================================================
     * String
     * number-int/long
     * number-double/float
     * date
     * byte[]
     * byte[]-geometry
     *
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     *
     *                                              String
     *
     * ****************************************************************************************************************/

    JAVA_STRING("VARCHAR", String.class, false, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(value instanceof String){
                String str = (String)value;
                if(str.startsWith("${") && str.endsWith("}")){
                    value = str.substring(2, str.length()-1);
                }
            }else if(value instanceof Date){
                value = DateUtil.format((Date)value);
            }else{
                value = value.toString();
            }
            if(null != value) {
                if (!placeholder) {
                    String str = value.toString();
                    if (str.startsWith("'") && str.endsWith("'")){
                    }else{
                        value = "'" + str + "'";
                    }
                }
            }
            return value;
        }
    }
    ,JAVA_BOOLEAN("BOOLEAN", Boolean.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Boolean result =  BasicUtil.parseBoolean(value, null);
            if(null != def && null == result){
                result =  BasicUtil.parseBoolean(def, null);
            }
            return result;
        }
    }
    ,JAVA_INTEGER("INT", Integer.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Integer result = BasicUtil.parseInt(value, null);
            if(null == value){
                result = BasicUtil.parseInt(def, null);
            }
            return result;
        }
    }
    ,JAVA_LONG("LONG", Long.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Long result = BasicUtil.parseLong(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseLong(def, null);
            }
            return result;
        }
    }
    ,JAVA_FLOAT("FLOAT", Float.class, false, false){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Float result = BasicUtil.parseFloat(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseFloat(def, null);
            }
            return result;
        }
    }
    ,JAVA_DOUBLE("DOUBLE", Double.class, false, false){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Double result = BasicUtil.parseDouble(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseDouble(def, null);
            }
            return result;
        }
    }
    ,JAVA_DECIMAL("DECIMAL", BigDecimal.class, false, false){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            BigDecimal result =  BasicUtil.parseDecimal(value, null);
            if(null != def && null == result){
                result =  BasicUtil.parseDecimal(def, null);
            }
            return result;
        }
    }


    ,JAVA_DATE("DATETIME", Date.class, true, true){
        public Object read(Object value, Object def, Class clazz){
            Convert convert = convert(clazz);
            if(null != convert){
                try {
                    value = convert.exe(value, def);
                }catch (ConvertException e){
                    //TODO 根据异常信息 决定下一行
                    e.printStackTrace();
                }
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Timestamp(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    }
    ,JAVA_SQL_TIMESTAMP("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            Date date = DateUtil.parse(value);
            if(null != date) {
                if(placeholder){
                    value = new java.sql.Timestamp(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date) + "'";
                }
            }
            return value;
        }
    }
    ,JAVA_SQL_TIME("TIME", Time.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            Date date = DateUtil.parse(value);
            if(null != date) {
                if(placeholder){
                    value = new java.sql.Timestamp(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date, "HH:mm:ss") + "'";
                }
            }
            return value;
        }
    }
    ,JAVA_SQL_DATE("DATE", java.sql.Date.class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null != value && value.getClass() != clazz){
                Date date = DateUtil.parse(value);
                return JAVA_DATE.read(date, def, clazz);
            }

            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Timestamp(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    }
    ,JAVA_LOCAL_DATE("DATE", LocalDate.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Date(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
                    }
                }
            }
            return value;
        }
    }
    ,JAVA_LOCAL_TIME("TIME", LocalTime.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new Time(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    }
    ,JAVA_LOCAL_DATE_TIME("DATETIME", LocalDateTime.class, true, true){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Timestamp(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    }

    ,JAVA_BYTES("", Byte[].class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    ,BYTES("", byte[].class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    ,JAVA_BYTE("", Byte.class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    ,BYTE("", byte.class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    ,ANYLINE_DATAROW("", DataRow.class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }

    ,ANYLINE_DATASET("", DataRow.class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }


    ;

    private LinkedHashMap<Class,Convert> converts = new LinkedHashMap<>();
    private final  String name;
    private final  Class clazz;
    private final  Boolean ignorePrecision;
    private final  Boolean ignoreScale;
    private DefaultJavaType(String name, Class clazz, Boolean ignorePrecision, Boolean ignoreScale){
        this.name = name;
        this.clazz = clazz;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    @Override
    public Object read(Object value, Object def, Class clazz) {
        return value;
    }

    @Override
    public Object write(Object value, Object def, boolean placeholder) {
        return value;
    }

    @Override
    public DataType convert(Convert convert) {
        converts.put(convert.getTarget(), convert);
        return this;
    }

    @Override
    public Convert convert(Class clazz) {
        return null;
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
    public Class supportClass() {
        return clazz;
    }


}
