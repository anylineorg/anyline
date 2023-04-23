package org.anyline.data.jdbc.oracle;

import org.anyline.entity.mdtadata.JavaType;
import org.anyline.util.DateUtil;

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
        JavaType SQL_TIMESTAMP             = new JavaType() {public Class getJavaClass(){return Timestamp.class;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new Timestamp(date.getTime());
                    }else{
                        value = "TO_TIMESTAMP(''" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "','yyyy-mm-dd hh24:mi:ss')";
                    }
                }
                return value;
            }
        };
        JavaType SQL_TIME             = new JavaType() {public Class getJavaClass(){return Time.class;}
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
                            value = "TO_DATE(''" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "','yyyy-mm-dd hh24:mi:ss')";
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
                            value = "TO_DATE(''" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "','yyyy-mm-dd hh24:mi:ss')";
                        }
                    }
                }
                return value;
            }
        };

        types.put("JAVA.TIME.DATE"                 , DATE             );
        types.put("JAVA.SQL.DATE"                  , SQL_DATE         );
        types.put("JAVA.SQL.TIMESTAMP"             , SQL_TIMESTAMP    );
        types.put("JAVA.SQL.TIME"                  , SQL_TIME         );
        types.put("JAVA.TIME.LOCALDATE"            , LOCAL_DATE       );
        types.put("JAVA.TIME.LOCALTIME"            , LOCAL_TIME       );
        types.put("JAVA.TIME.LOCALDATETIME"        , LOCAL_DATE_TIME  );

    }
}
