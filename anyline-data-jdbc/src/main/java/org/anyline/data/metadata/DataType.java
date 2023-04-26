package org.anyline.data.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.Point;
import org.anyline.entity.mdtadata.ColumnType;
import org.anyline.util.Base64Util;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;

public enum DataType implements org.anyline.entity.mdtadata.DataType{


    /* *****************************************************************************************************************
     *
     * 													SQL DATA TYPE
     *
     * =================================================================================================================
     * String
     * String-format
     * number-int/long
     * number-double/float
     * date
     * byte[]
     * byte[]-file
     * byte[]-geometry
     *
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     *
     *                                              String
     *
     * ****************************************************************************************************************/
    /**
     * mysql,pg
     */
    CHAR("CHAR", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(value instanceof String){
            }else if(value instanceof Date){
                value = DateUtil.format((Date)value);
            }else{
                value = value.toString();
            }
            if(!placeholder){
                value = "'" + value + "'";
            }
            return value;
        }
    }
    /**
     * oracle,mssql
     */
    ,NCHAR("NCHAR", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,CLOB("CLOB", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,NCLOB("NCLOB", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,NVARCHAR("NVARCHAR", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,NVARCHAR2("NVARCHAR2", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,LONGTEXT("LONGTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,MEDIUMTEXT("MEDIUMTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql,pg
     */
    ,TEXT("TEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,NTEXT("NTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,TINYTEXT("TINYTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql,pg,oracle
     */
    ,VARCHAR("VARCHAR", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,VARCHAR2("VARCHAR2", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SYSNAME("SYSNAME", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,UUID("UUID", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(null != value){
                value = java.util.UUID.fromString(value.toString());
            }
            if(null == value){
                value = def;
            }
            return value;
        }
    }
    /**
     * mssql
     */
    ,UNIQUEIDENTIFIER("UNIQUEIDENTIFIER", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql,mssql
     */
    ,BINARY("BINARY", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mysql,mssql
     */
    ,VARBINARY("VARBINARY", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }


    /* *****************************************************************************************************************
     *
     *                                              String-format
     *
     * ****************************************************************************************************************/

    /**
     * mysql,pg
     */
    ,JSON("JSON", String.class, true, true){
        public Object read(Object value, Class clazz){
            if(null == value){
                return value;
            }
            if(value.getClass() == clazz){
                return value;
            }
            String str = value.toString().trim();
            try{
                JsonNode node = BeanUtil.JSON_MAPPER.readTree(str);
                if(null == clazz) {
                    if (node.isArray()) {
                        value = DataSet.parseJson(node);
                    } else {
                        value = DataRow.parseJson(node);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }

    /**
     * mssql
     */
    ,XML("XML", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /* *****************************************************************************************************************
     *
     *                                              number-int/long
     *
     * ****************************************************************************************************************/
    /**
     * mysql,mssql
     */
    ,BIT("BIT", Byte.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if("0".equals(value.toString()) || "false".equalsIgnoreCase(value.toString())){
                value = 0;
            }else{
                value = 1;
            }
            return value;
        }
    }
    /**
     * pg中作为数组存在,不同与mysql,mssql
     */
    ,BITS("BIT", Byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return value;
        }
    }
    /**
     * pg
     */
    ,VARBIT("VARBIT", Byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BITS.write(value, def, placeholder);}
    }
    ,SHORT("SHORT", Short.class, true, true){public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Short result = BasicUtil.parseShort(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseShort(def, null);
            }
            return result;
        }
    }
    /**
     * mysql,mssql
     */
    ,INT("INT", Integer.class, true, true){public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Integer result = BasicUtil.parseInt(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseInt(def, null);
            }
            return result;
        }
    }
    /**
     * oracle
     */
    ,LONG("LONG", Long.class, true, true){public Object read(Object value, Class clazz){return value;}
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
    /**
     * pg
     */
    ,SERIAL("SERIAL", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,SERIAL2("SERIAL2", Short.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,SERIAL4("SERIAL4", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SERIAL.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,SERIAL8("SERIAL8", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,SAMLLSERIAL("SERIAL2", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,BIGSERIAL("SERIAL8", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,INT2("INT2", Short.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,INT4("INT4", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,INT8("INT8", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,BIGINT("BIGINT", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,MEDIUMINT("MEDIUMINT", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,INTEGER("MEDIUMINT", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,SMALLINT("SMALLINT", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,TINYINT("TINYINT", Short.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,BOOLEAN("BOOLEAN", Boolean.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,BOOL("BOOLEAN", Boolean.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BOOLEAN.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              number-double/float
     *
     * ****************************************************************************************************************/

    /**
     * mysql,  ,oracle
     */
    ,DECIMAL("DECIMAL", BigDecimal.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            BigDecimal result = BasicUtil.parseDecimal(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseDecimal(def, null);
            }
            return result;
        }
    }
    /**
     * mysql
     */
    ,DOUBLE("DOUBLE", Double.class, false, false){
        public Object read(Object value, Class clazz){return value;}
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
    /**
     * mysql,  ,oracle
     */
    ,FLOAT("FLOAT", Float.class, false, false){
        public Object read(Object value, Class clazz){return value;}
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
    /**
     * pg
     */
    ,FLOAT4("FLOAT4", Float.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,FLOAT8("FLOAT4", Double.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,BINARY_DOUBLE("BINARY_DOUBLE", Double.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,BINARY_FLOAT("BINARY_FLOAT", Float.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,MONEY("MONEY", BigDecimal.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SMALLMONEY("SMALLMONEY", BigDecimal.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,NUMERIC("NUMERIC", BigDecimal.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,NUMBER("NUMBER", BigDecimal.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,REAL("REAL", Float.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              date
     *                               write 需要根据数据库类型 由内置函数转换
     *
     * ****************************************************************************************************************/
    /**
     * mysql,pg,oracle
     */
    ,DATE("DATE", java.sql.Date.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Date date = DateUtil.parse(value);
            if(null == date && null != def){
                date = DateUtil.parse(def);
            }
            if (null != date) {
                if(placeholder){
                    value = new java.sql.Date(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date, "yyyy-MM-dd");
                }
            }
            return value;
        }
    }
    /**
     * mysql
     */
    ,DATETIME("DATE", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Date date = DateUtil.parse(value);
            if(null == date && null != def){
                date = DateUtil.parse(def);
            }
            if(null != date) {
                if(placeholder){
                    value = new java.sql.Timestamp(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date) + "'";
                }
            }else{
                value = null;
            }
            return value;
        }
    }
    /**
     * mssql
     */
    ,DATETIME2("DATETIME2", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mssql<br/>
     * 2020-01-01 15:10:10.0000011
     */
    ,DATETIMEOFFSET("DATETIMEOFFSET", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SMALLDATETIME("SMALLDATETIME", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SQL_DATETIMEOFFSET("SQL_DATETIMEOFFSET", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mysql,pg
     */
    ,TIME("TIME", java.sql.Time.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Date date = DateUtil.parse(value);
            if(null == date && null != def){
                date = DateUtil.parse(def);
            }
            if(null != date) {
                if(placeholder){
                    value = new Time(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date, "HH:mm:ss") + "'";
                }
            }else{
                value = null;
            }
            return value;
        }
    }
    /**
     * pg
     */
    ,TIMEZ("TIMEZ", java.sql.Time.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return TIME.write(value, def, placeholder);
        }
    }
    /**
     * mysql,pg,oracle
     */
    ,TIMESTAMP("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
    /**
     * pg
     */
    ,TIMESTAMP_ZONE("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
    /**
     * pg
     */
    ,IMESTAMP_LOCAL_ZONE("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
    /**
     * mysql
     */
    ,YEAR("YEAR", java.sql.Date.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATE.write(value, def, placeholder);
        }
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]
     *
     * ****************************************************************************************************************/
    /**
     * mysql,  ,oracle
     */
    ,BLOB("BLOB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(value instanceof byte[]){

            }else {
                if(value instanceof String){
                    String str = (String)value;
                    if(Base64Util.verify(str)){
                        try {
                            value = Base64Util.decode(str);
                        }catch (Exception e){
                            value = str.getBytes();
                        }
                    }else{
                        value = str.getBytes();
                    }
                }
            }
            return value;
        }
    }
    /**
     * mysql
     */
    ,LONGBLOB("LONGBLOB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,MEDIUMBLOB("MEDIUMBLOB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,TINYBLOB("TINYBLOB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,MULTILINESTRING("MULTILINESTRING", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,BYTEA("BYTEA", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,JSONB("JSONB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]-file
     *
     * ****************************************************************************************************************/
    /**
     * mssql
     */
    ,IMAGE("IMAGE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * oracle
     */
    ,BFILE("BFILE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]-geometry
     *
     * ****************************************************************************************************************/
    /**
     * mysql,pg
     */
    ,POINT("IMAGE", byte[].class, true, true){
        public Object read(Object value, Class clazz){
            if(null == value){
                return value;
            }
            Point point = BasicUtil.parsePoint(value);
            if(null == clazz){
                value = point;
            }else if(null != point){
                if (clazz == Point.class) {
                    value = point;
                } else if (clazz == double[].class) {
                    value = BeanUtil.Double2double(point.getArray(), 0);
                } else if (clazz == Double[].class) {
                    value = point.getArray();
                } else if (clazz == byte[].class) {
                    value = point.bytes();
                }
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(value instanceof byte[]){
                return value;
            }
            if(value instanceof Point){
                value = ((Point)value).bytes();
            }else if(value instanceof double[]){
                double[] ds = (double[]) value;
                if(ds.length == 2){
                    if (ds.length >= 2) {
                        value = new Point(ds[0], ds[1]).bytes();
                    }
                }
            }else if(value instanceof Double[]){
                Double[] ds = (Double[]) value;
                if(ds.length == 2 && null != ds[0] && null != ds[1]){
                    value = new Point(ds[0], ds[1]).bytes();
                }
            }
            return value;
        }
    }
    /**
     * mysql
     */
    ,MULTIPOLYGON("MULTIPOLYGON", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mysql
     */
    ,MULTIPOINT("MULTIPOINT", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mysql,pg
     */
    ,POLYGON("POLYGON", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mysql
     */
    ,GEOMETRY("GEOMETRY", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mysql
     */
    ,GEOMETRYCOLLECTION("GEOMETRYCOLLECTION", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mssql
     */
    ,HIERARCHYID("HIERARCHYID", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mysql,pg
     */
    ,LINE("LINE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,LSEG("LSEG", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mssql
     */
    ,GEOGRAPHY("GEOGRAPHY", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,BOX("BOX", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,CIDR("CIDR", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,CIRCLE("CIRCLE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,INET("INET", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }


    /* *****************************************************************************************************************
     *
     *                                              待实现
     *
     * ****************************************************************************************************************/

    /**
     * mysql
     */
    ,ENUM("INET", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,INTERVAL("INTERVAL", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,PATH("PATH", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * oracle
     */
    ,RAW("RAW", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * oracle
     */
    ,ROWID("ROWID", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mysql
     */
    ,SET("SET", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,TSQUERY("TSQUERY", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,TSVECTOR("TSVECTOR", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,MACADDR("MACADDR", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     */
    ,PG_SNAPSHOT("PG_SNAPSHOT", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * pg
     * 弃用 换成pg_snapshot
     */
    ,TXID_SNAPSHOT("TXID_SNAPSHOT", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * oracle
     */
    ,UROWID("UROWID", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }
    /**
     * mssql
     */
    ,SQL_VARIANT("SQL_VARIANT", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }

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

    ,JAVA_STRING("VARCHAR", String.class, false, true){
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
    }
    ,JAVA_BOOLEAN("BOOLEAN", Boolean.class, true, true){
        public Object read(Object value, Class clazz){return value;}
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
    ,JAVA_BOOL("BOOL", Boolean.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return JAVA_BOOLEAN.write(value, def, placeholder);}
    }
    ,JAVA_INTEGER("INT", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
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
        public Object read(Object value, Class clazz){return value;}
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
        public Object read(Object value, Class clazz){return value;}
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
        public Object read(Object value, Class clazz){return value;}
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
    ,JAVA_DECIMAL("DOUBLE", BigDecimal.class, false, false){
        public Object read(Object value, Class clazz){return value;}
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

    ,JAVA_SQL_TIMESTAMP("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
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
    ,JAVA_SQL_TIME("TIME", java.sql.Time.class, true, true){
        public Object read(Object value, Class clazz){return value;}
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

    ,JAVA_DATE("DATETIME", java.util.Date.class, true, true){
        public Object read(Object value, Class clazz){return value;}
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
    ,JAVA_SQL_DATE("DATE", java.sql.Date.class, true, true){
        public Object read(Object value, Class clazz){return value;}
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
        public Object read(Object value, Class clazz){return value;}
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
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Time(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    }
    ,JAVA_LOCAL_DATE_TIME("DATETIME", LocalDateTime.class, true, true){
        public Object read(Object value, Class clazz){return value;}
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


    ;

    private final  String name;
    private final  Class clazz;
    private final  Boolean ignorePrecision;
    private final  Boolean ignoreScale;
    private DataType(String name, Class clazz, Boolean ignorePrecision, Boolean ignoreScale){
        this.name = name;
        this.clazz = clazz;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    @Override
    public String type() {
        return name;
    }

    @Override
    public Object read(Object value, Class clazz) {
        return value;
    }

    @Override
    public Object write(Object value, Object def, boolean placeholder) {
        return value;
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

}
