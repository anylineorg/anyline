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

package org.anyline.metadata.type.init;

import org.anyline.entity.DataRow;
import org.anyline.metadata.type.Convert;
import org.anyline.metadata.type.ConvertException;
import org.anyline.metadata.type.DataType;
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

    JAVA_STRING("VARCHAR", String.class, 0, 1, 1){
        public Object read(Object value, Object def, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(value instanceof String){
                String str = (String)value;
                //if(str.startsWith("${") && str.endsWith("}")){
                if(BasicUtil.checkEl(str)){
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
   , JAVA_BOOLEAN("BOOLEAN", Boolean.class, 1, 1, 1){
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
   , JAVA_INTEGER("INT", Integer.class, 1, 1, 1){
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
   , JAVA_LONG("LONG", Long.class, 1, 1, 1){
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
   , JAVA_FLOAT("FLOAT", Float.class, 1, 0, 0){
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
   , JAVA_DOUBLE("DOUBLE", Double.class, 1, 0, 0){
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
   , JAVA_DECIMAL("DECIMAL", BigDecimal.class, 1, 0, 0){
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

   , JAVA_DATE("DATETIME", Date.class, 1, 1, 1){
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
   , JAVA_SQL_TIMESTAMP("TIMESTAMP", java.sql.Timestamp.class, 1, 1, 1){
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
   , JAVA_SQL_TIME("TIME", Time.class, 1, 1, 1){
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
   , JAVA_SQL_DATE("DATE", java.sql.Date.class, 1, 1, 1){
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
   , JAVA_LOCAL_DATE("DATE", LocalDate.class, 1, 1, 1){
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
   , JAVA_LOCAL_TIME("TIME", LocalTime.class, 1, 1, 1){
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
   , JAVA_LOCAL_DATE_TIME("DATETIME", LocalDateTime.class, 1, 1, 1){
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

   , JAVA_BYTES("", Byte[].class, 1, 1, 1){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
   , BYTES("", byte[].class, 0, 1, 1){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
   , JAVA_BYTE("", Byte.class, 1, 1, 1){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
   , BYTE("", byte.class, 1, 1, 1){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
   , ANYLINE_DATAROW("", DataRow.class, 1, 1, 1){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }

   , ANYLINE_DATASET("", DataRow.class, 1, 1, 1){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return null;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }

    ;

    private LinkedHashMap<Class, Convert> converts = new LinkedHashMap<>();
    private final  String name;
    private final  Class clazz;
    private final  int ignoreLength;
    private final  int ignorePrecision;
    private final  int ignoreScale;
    private DefaultJavaType(String name, Class clazz, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.name = name;
        this.clazz = clazz;
        this.ignoreLength = ignoreLength;
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
    public int ignoreLength() {
        return ignoreLength;
    }

    @Override
    public int ignorePrecision() {
        return ignorePrecision;
    }

    @Override
    public int ignoreScale() {
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
