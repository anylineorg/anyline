package org.anyline.data.jdbc.adapter;

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

    UNSUPPORT("UNSUPPORT", String.class, true, true){
        @Override
        public Object read(Object value, Class clazz) {
            return null;
        }

        @Override
        public Object write(Object value, Object def, boolean placeholder) {
            return null;
        }
    },
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
    }//mysql,pg
    ,NCHAR("NCHAR", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//     ,  ,oracle
    ,CLOB("CLOB", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//        ,oracle
    ,NCLOB("NCLOB", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//        ,oracle
    ,NVARCHAR("NVARCHAR", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//        ,mssql
    ,NVARCHAR2("NVARCHAR2", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//,oracle
    ,LONGTEXT("LONGTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mysql
    ,MEDIUMTEXT("MEDIUMTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mysql
    ,TEXT("TEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mysql,pg
    ,NTEXT("NTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mssql
    ,TINYTEXT("TINYTEXT", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mysql
    ,VARCHAR("VARCHAR", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mysql,pg,oracle
    ,VARCHAR2("VARCHAR2", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//oracle
    ,SYSNAME("SYSNAME", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mssql
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
    }//pg
    ,UNIQUEIDENTIFIER("UNIQUEIDENTIFIER", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }//mssql
    ,BINARY("BINARY", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,VARBINARY("VARBINARY", String.class, false, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql


    /* *****************************************************************************************************************
     *
     *                                              String-format
     *
     * ****************************************************************************************************************/

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
    }//mysql,pg

    ,XML("XML", String.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mssql
    /* *****************************************************************************************************************
     *
     *                                              number-int/long
     *
     * ****************************************************************************************************************/

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
    }//mysql,mssql
    //pg中作为数组存在,不同与mysql,mssql
    ,BITS("BIT", Byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return value;
        }
    }//pg
    ,VARBIT("VARBIT", Byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BITS.write(value, def, placeholder);}
    }//pg
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
    }//mysql
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
    }//oracle
    ,SERIAL("SERIAL", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }//pg
    ,SERIAL2("SERIAL2", Short.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }//pg
    ,SERIAL4("SERIAL4", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SERIAL.write(value, def, placeholder);}
    }//pg
    ,SERIAL8("SERIAL8", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }//pg
    ,SAMLLSERIAL("SERIAL2", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }//pg
    ,BIGSERIAL("SERIAL8", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }//pg
    ,INT2("INT2", Short.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }
    ,INT4("INT4", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }
    ,INT8("INT8", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }
    ,BIGINT("BIGINT", Long.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return LONG.write(value, def, placeholder);}
    }//mysql
    ,MEDIUMINT("MEDIUMINT", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }//mysql
    ,INTEGER("MEDIUMINT", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }//mysql
    ,SMALLINT("SMALLINT", Integer.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
    }//mysql
    ,TINYINT("TINYINT", Short.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }//mysql
    /* *****************************************************************************************************************
     *
     *                                              number-double/float
     *
     * ****************************************************************************************************************/

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
    }//mysql,  ,oracle
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
    }//mysql
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
    }//mysql,  ,oracle
    ,FLOAT4("FLOAT4", Float.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }//pg
    ,FLOAT8("FLOAT4", Double.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }//pg
    ,BINARY_DOUBLE("BINARY_DOUBLE", Double.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }//oracle
    ,BINARY_FLOAT("BINARY_FLOAT", Float.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }//oracle
    ,MONEY("MONEY", BigDecimal.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }//pg
    ,SMALLMONEY("SMALLMONEY", BigDecimal.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }//mssql
    ,NUMERIC("NUMERIC", BigDecimal.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }//mysql
    ,NUMBER("NUMBER", BigDecimal.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }//oracle
    ,REAL("REAL", Float.class, false, false){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }//mysql
    /* *****************************************************************************************************************
     *
     *                                              date
     *                               write 需要根据数据库类型 由内置函数转换
     *
     * ****************************************************************************************************************/

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
    }//mysql,pg,oracle
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
    }//mysql
    ,DATETIME2("DATETIME2", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }//mssql
    ,SMALLDATETIME("SMALLDATETIME", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }//mssql
    ,SQL_DATETIMEOFFSET("SQL_DATETIMEOFFSET", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }//mssql
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
    }//mysql,pg
    ,TIMEZ("TIMEZ", java.sql.Time.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return TIME.write(value, def, placeholder);
        }
    }//pg
    ,TIMESTAMP("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }//mysql,pg,oracle
    ,TIMESTAMP_ZONE("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }//pg
    ,IMESTAMP_LOCAL_ZONE("TIMESTAMP", java.sql.Timestamp.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }//pg
    ,YEAR("YEAR", java.sql.Date.class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return DATE.write(value, def, placeholder);
        }
    }//mysql
    /* *****************************************************************************************************************
     *
     *                                              byte[]
     *
     * ****************************************************************************************************************/
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
    }////mysql,  ,oracle
    ,LONGBLOB("LONGBLOB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }//mysql
    ,MEDIUMBLOB("MEDIUMBLOB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }//mysql
    ,TINYBLOB("TINYBLOB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }//mysql
    ,MULTILINESTRING("MULTILINESTRING", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,BYTEA("BYTEA", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,JSONB("JSONB", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }//pg
    /* *****************************************************************************************************************
     *
     *                                              byte[]-file
     *
     * ****************************************************************************************************************/

    ,IMAGE("IMAGE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mssql
    ,BFILE("BFILE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//oracle
    /* *****************************************************************************************************************
     *
     *                                              byte[]-geometry
     *
     * ****************************************************************************************************************/

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
    }//mysql,pg


    ,MULTIPOLYGON("MULTIPOLYGON", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,MULTIPOINT("MULTIPOINT", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,POLYGON("POLYGON", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql,pg
    ,GEOMETRY("GEOMETRY", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,GEOMETRYCOLLECTION("GEOMETRYCOLLECTION", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,HIERARCHYID("HIERARCHYID", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mssql
    ,LINE("LINE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql,pg
    ,LSEG("LSEG", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,GEOGRAPHY("GEOGRAPHY", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mssql
    ,BOX("BOX", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,CIDR("CIDR", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,CIRCLE("CIRCLE", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,INET("INET", byte[].class, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg


    /* *****************************************************************************************************************
     *
     *                                              待实现
     *
     * ****************************************************************************************************************/

    ,ENUM("INET", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,INTERVAL("INTERVAL", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,PATH("PATH", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,RAW("RAW", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//oracle
    ,ROWID("ROWID", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//oracle
    ,SET("SET", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mysql
    ,TSQUERY("TSQUERY", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,TSVECTOR("TSVECTOR", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg

    ,PG_SNAPSHOT("PG_SNAPSHOT", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    //弃用 换成pg_snapshot
    ,TXID_SNAPSHOT("TXID_SNAPSHOT", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//pg
    ,UROWID("UROWID", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//oracle
    ,SQL_VARIANT("SQL_VARIANT", null, true, true){
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    }//mssql

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
    }//pg
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
    public String getName() {
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
    public boolean isIgnorePrecision() {
        return ignorePrecision;
    }

    @Override
    public boolean isIgnoreScale() {
        return ignoreScale;
    }
}
