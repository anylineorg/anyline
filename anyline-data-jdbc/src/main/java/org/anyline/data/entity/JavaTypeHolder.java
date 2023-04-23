package org.anyline.data.entity;

import org.anyline.entity.mdtadata.JavaType;
import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class JavaTypeHolder {
    private static Map<String, JavaType> types = new Hashtable<>();
    public static Map<String, JavaType> types(){
        return types;
    }
    public static JavaType type(String clazz){
        if(null != clazz) {
            return types.get(clazz.toUpperCase());
        }
        return null;
    }
    static {

        /* *****************************************************************************************************************
         *
         * String
         * number-int/long
         * number-double/float
         * date
         * byte[]
         * byte[]-geometry
         *
         ******************************************************************************************************************/

        /* *********************************************************************************************************************************
         *
         *                                              String
         *
         * *********************************************************************************************************************************/
        JavaType STRING              = new JavaType() {public Class getJavaClass(){return String.class;}
            public Object read(Object value, Class clazz){return value;}
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
        };
        JavaType BOOLEAN             = new JavaType() {public Class getJavaClass(){return Boolean.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                value =  BasicUtil.parseBoolean(value, null);
                if(null == value){
                    value =  BasicUtil.parseBoolean(value, null);
                }
                return value;
            }
        };
        JavaType INTEGER             = new JavaType() {public Class getJavaClass(){return Integer.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                value = BasicUtil.parseInt(value, null);
                if(null == value){
                    value = BasicUtil.parseInt(def, null);
                }
                return value;
            }
        };
        JavaType LONG             = new JavaType() {public Class getJavaClass(){return Long.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                value = BasicUtil.parseLong(value, null);
                if(null == value){
                    value = BasicUtil.parseLong(def, null);
                }
                return value;
            }
        };
        JavaType FLOAT             = new JavaType() {public Class getJavaClass(){return Float.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                value = BasicUtil.parseFloat(value, null);
                if(null == value){
                    value = BasicUtil.parseFloat(def, null);
                }
                return value;
            }
        };
        JavaType DOUBLE             = new JavaType() {public Class getJavaClass(){return Long.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                value = BasicUtil.parseDouble(value, null);
                if(null == value){
                    value = BasicUtil.parseDouble(def, null);
                }
                return value;
            }
        };
        JavaType DECIMAL             = new JavaType() {public Class getJavaClass(){return BigDecimal.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                value =  BasicUtil.parseDecimal(value, null);
                if(null == value){
                    value =  BasicUtil.parseDecimal(def, null);
                }
                return value;
            }
        };
        JavaType SQL_TIMESTAMP             = new JavaType() {public Class getJavaClass(){return java.sql.Timestamp.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new Timestamp(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date) + "'";
                    }
                }
                return value;
            }
        };
        JavaType SQL_TIME             = new JavaType() {public Class getJavaClass(){return java.sql.Time.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new Time(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "HH:mm:ss") + "'";
                    }
                }
                return value;
            }
        };

        JavaType DATE             = new JavaType() {public Class getJavaClass(){return Date.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(value instanceof Time){
                }else {
                    Date date = DateUtil.parse(value);
                    if(null != date) {
                        if(placeholder){
                            value = new Time(date.getTime());
                        }else{
                            value = "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
                        }
                    }
                }
                return value;
            }
        };
        JavaType SQL_DATE             = new JavaType() {public Class getJavaClass(){return Date.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(value instanceof Time){
                }else {
                    Date date = DateUtil.parse(value);
                    if(null != date) {
                        if(placeholder){
                            value = new Time(date.getTime());
                        }else{
                            value = "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
                        }
                    }
                }
                return value;
            }
        };
        JavaType LOCAL_DATE             = new JavaType() {public Class getJavaClass(){return LocalDate.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(value instanceof Time){
                }else {
                    Date date = DateUtil.parse(value);
                    if(null != date) {
                        if(placeholder){
                            value = new Time(date.getTime());
                        }else{
                            value = "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
                        }
                    }
                }
                return value;
            }
        };
        JavaType LOCAL_TIME             = new JavaType() {public Class getJavaClass(){return LocalTime.class;}
            public Object read(Object value, Class clazz){return value;}
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
        };
        JavaType LOCAL_DATE_TIME             = new JavaType() {public Class getJavaClass(){return LocalDateTime.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(value instanceof Time){
                }else {
                    Date date = DateUtil.parse(value);
                    if(null != date) {
                        if(placeholder){
                            value = new Time(date.getTime());
                        }else{
                            value = "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
                        }
                    }
                }
                return value;
            }
        };

        //以下按字母顺序 方便查找
        types.put("JAVA.MATH.DECIMAL"              , DECIMAL          );
        types.put("JAVA.LANG.DOUBLE"               , DOUBLE           );
        types.put("JAVA.LANG.BOOLEAN"              , BOOLEAN          );
        types.put("JAVA.LANG.INTEGER"              , INTEGER          );
        types.put("JAVA.LANG.LONG"                 , LONG             );
        types.put("JAVA.LANG.FLOAT"                , FLOAT            );
        types.put("JAVA.LANG.STRING"               , STRING           );
        types.put("JAVA.TIME.DATE"                 , DATE             );
        types.put("JAVA.SQL.DATE"                  , SQL_DATE         );
        types.put("JAVA.SQL.TIMESTAMP"             , SQL_TIMESTAMP    );
        types.put("JAVA.SQL.TIME"                  , SQL_TIME         );
        types.put("JAVA.TIME.LOCALDATE"            , LOCAL_DATE       );
        types.put("JAVA.TIME.LOCALTIME"            , LOCAL_TIME       );
        types.put("JAVA.TIME.LOCALDATETIME"        , LOCAL_DATE_TIME  );

    }
}
