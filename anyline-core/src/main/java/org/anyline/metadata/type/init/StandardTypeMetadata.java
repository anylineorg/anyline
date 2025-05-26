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

package org.anyline.metadata.type.init;

import com.fasterxml.jackson.databind.JsonNode;
import org.anyline.adapter.KeyAdapter;
import org.anyline.proxy.ConvertProxy;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.geometry.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.Base64Util;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.*;

import static org.anyline.metadata.type.DatabaseType.*;

public enum StandardTypeMetadata implements TypeMetadata {

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
     * vector
     *
     ******************************************************************************************************************/

    /* *****************************************************************************************************************
     *
     *                                              String
     *
     * ****************************************************************************************************************/
    CHAR(CATEGORY.CHAR, "CHAR", null, String.class, 0, 1, 1, MySQL, PostgreSQL, Informix, HANA, Derby, Doris) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            if(null != value) {
                String str = value.toString();
                if(BasicUtil.checkEl(str)) {
                    //${A.USER_ID}
                    value = str.substring(2, str.length() - 1);
                    return value;
                }
            }
            if(value instanceof String) {
            }else if(value instanceof Date) {
                value = DateUtil.format((Date)value);
            }else{
                value = value.toString();
            }
            if(!placeholder) {
                value = "'" + value + "'";
            }
            return value;
        }
    }
   , NCHAR(CATEGORY.CHAR, "NCHAR", null, String.class, 0, 1, 1, ORACLE, MSSQL, Informix, GBase8S, SinoDB) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , CLOB(CATEGORY.TEXT, "CLOB", null, String.class,1, 1, 1, ORACLE, Informix, GBase8S, SinoDB, Derby, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , NCLOB(CATEGORY.TEXT, "NCLOB", null, String.class,1, 1, 1, ORACLE, HANA) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , NVARCHAR(CATEGORY.CHAR, "NVARCHAR", null, String.class, 0, 1, 1, MSSQL, Informix, GBase8S, SinoDB, HANA, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , LVARCHAR(CATEGORY.CHAR, "LVARCHAR", null, String.class, 0, 1, 1, Informix, GBase8S, SinoDB, SinoDB) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , NVARCHAR2(CATEGORY.CHAR, "NVARCHAR2", NVARCHAR, String.class, 0, 1, 1, ORACLE) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , LONGTEXT(CATEGORY.TEXT, "LONGTEXT", null, String.class,1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , MEDIUMTEXT(CATEGORY.TEXT, "MEDIUMTEXT", null, String.class,1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , TEXT(CATEGORY.TEXT, "TEXT", null, String.class,1, 1, 1, MySQL, PostgreSQL, SQLite, Informix, GBase8S, SinoDB, IoTDB, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , MATCH_ONLY_TEXT(CATEGORY.TEXT, "match_only_text", null, String.class,1, 1, 1, ElasticSearch) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }

   , NTEXT(CATEGORY.TEXT, "NTEXT", null, String.class,1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , TINYTEXT(CATEGORY.TEXT, "TINYTEXT", null, String.class,1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , ACLITEM(CATEGORY.TEXT, "ACLITEM", null, String.class, 1, 1, 1, KingBase)
    /**
     * Informix(长度不超过 255 )
     */
   , VARCHAR(CATEGORY.CHAR, "VARCHAR", null, String.class, 0, 1, 1, MySQL, PostgreSQL, ORACLE, Informix, GBase8S, SinoDB, HANA, Derby, KingBase, Doris) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , VARCHAR2(CATEGORY.CHAR, "VARCHAR2", VARCHAR, String.class, 0, 1, 1, ORACLE, KingBase, DM, oscar) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , SYSNAME(CATEGORY.TEXT, "SYSNAME", null, String.class,1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
   , UUID(CATEGORY.TEXT, "UUID", null, String.class,1, 1, 1, PostgreSQL, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            if(null != value) {
                value = java.util.UUID.fromString(value.toString());
            }
            if(null == value) {
                value = def;
            }
            return value;
        }
    }
   , UNIQUEIDENTIFIER(CATEGORY.TEXT, "UNIQUEIDENTIFIER", null, String.class,1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }

    /**
     * mysql(byte[])
     */
   , BINARY(CATEGORY.BYTES, "BINARY", null, byte[].class, 0, 1, 1, MySQL, MSSQL, HANA, ElasticSearch)
   , VARBINARY(CATEGORY.BYTES, "VARBINARY", null, byte[].class, 0, 1, 1, MySQL, MSSQL, HANA)

    , STRING(CATEGORY.TEXT, "String", null, String.class, 1, 1, 1, Doris, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , FixedString(CATEGORY.TEXT, "FixedString", null, String.class, 0, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , FIXED_STRING(CATEGORY.TEXT, "FIXED_STRING", null, String.class, 0, 1, 1, Nebula) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }
    , HLL(CATEGORY.TEXT, "HLL", null, String.class, 0, 1, 1, Doris) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return CHAR.write(value, def, placeholder);}
    }

    /* *****************************************************************************************************************
     *
     *                                              String-format
     *
     * ****************************************************************************************************************/
   , JSON(CATEGORY.TEXT, "JSON", null, String.class,1, 1, 1, MySQL, PostgreSQL, KingBase, Doris) {
        @Override
        public Object convert(Object value, Class target, Object def) {
            if(null == value) {
                return def;
            }
            Class transfer = transfer();
            Class compatible = compatible();
            try{
                if(null == target) {
                    JsonNode node = BeanUtil.JSON_MAPPER.readTree(value.toString());
                    if (node.isArray()) {
                        value = DataSet.parseJson(KeyAdapter.KEY_CASE.SRC, node);
                    } else {
                        value = DataRow.parseJson(KeyAdapter.KEY_CASE.SRC, node);
                    }
                }else{
                    value = super.convert(value, target, def);
                }
            }catch (Exception e) {
                //不能转成DataSet的List
                value = super.convert(value, target, def);
            }
            return value;
        }
        public Object read(Object value, Object def, Class clazz) {
            if(null == value) {
                return value;
            }
            if(value.getClass() == clazz) {
                return value;
            }
            String str = value.toString().trim();
            try{
                JsonNode node = BeanUtil.JSON_MAPPER.readTree(str);
                if(null == clazz) {
                    if (node.isArray()) {
                        //value = DataSet.parseJson(node);
                        Collection<Object> list = new ArrayList<>();
                        Iterator<JsonNode> items = node.iterator();
                        boolean isDataRow = true;
                        while (items.hasNext()) {
                            JsonNode item = items.next();
                            Object row = DataRow.parseJsonObject(KeyAdapter.KEY_CASE.SRC, item);
                            if(row instanceof DataRow) {
                            }else{
                                isDataRow = false;
                            }
                            list.add(row);
                        }
                        if(isDataRow) {
                            value = DataSet.parse(KeyAdapter.KEY_CASE.SRC, list);
                        }else{
                            value = list;
                        }
                    } else {
                        value = DataRow.parseJson(KeyAdapter.KEY_CASE.SRC, node);
                    }
                }
            }catch (Exception e) {
                e.printStackTrace();
            }
            return value;}

    }
   , XML(CATEGORY.TEXT, "XML", null, String.class,1, 1, 1, MSSQL, KingBase) {}
    /* *****************************************************************************************************************
     *
     *                                              number-int/long
     *
     * ****************************************************************************************************************/
    , BIT_INT(CATEGORY.INT, "BIT", null, Integer.class,1, 1, 1, MSSQL)
    , BIT_BOOLEAN(CATEGORY.BOOLEAN, "BIT", null, Boolean.class,1, 1, 1, MySQL)
    , BITMAP(CATEGORY.BYTES, "BITMAP", null, Byte.class,1, 1, 1, Doris)
    , VARBIT(CATEGORY.BOOLEAN, "VARBIT", null, Byte[].class,1, 1, 1, PostgreSQL)
    , SHORT(CATEGORY.INT, "SHORT", null, Short.class,1, 1, 1)

    , BYTE(CATEGORY.INT, "BYTE", null, Short.class,1, 1, 1)
    // 2字节
    , INT2(CATEGORY.INT, "INT2", null, Short.class,1, 1, 1)
    , INT4(CATEGORY.INT, "INT4", null, Integer.class,1, 1, 1)
    // 8位(注意有些库也表示8字节=64位)
    , INT8(CATEGORY.INT, "INT8", null, Short.class,1, 1, 1, MySQL, MSSQL, Informix, GBase8S, SinoDB, Derby, Doris)
    , INT16(CATEGORY.INT, "INT16", null, Short.class, 1, 1, 1, IoTDB, ClickHouse)
    //int32 经常是对应 INT 如果个别数据库(如ClickHouse)中是INT32 需要在 code_datatype_compat 中修改 SQL_META_CODE
    , INT(CATEGORY.INT, "INT", null, Integer.class, 1, 1, 1, IoTDB, ClickHouse)
    , INT32(CATEGORY.INT, "INT", null, Integer.class, 1, 1, 1, IoTDB, ClickHouse)
    , INT64(CATEGORY.INT, "INT64", null, Long.class, 1, 1, 1, IoTDB, ClickHouse)
    , INT128(CATEGORY.INT, "INT128", null, Long.class, 1, 1, 1, ClickHouse)
    , INT256(CATEGORY.INT, "INT256", null, Long.class, 1, 1, 1, ClickHouse)
    , LONG_TEXT(CATEGORY.TEXT, "LONG", null, String.class,1, 1, 1, ORACLE, ElasticSearch) {}
    , BIGINT(CATEGORY.INT, "BIGINT", null, Long.class,1, 1, 1, MySQL, Informix, GBase8S, SinoDB, HANA, Derby, KingBase, Doris)
    , LONG(CATEGORY.INT, "LONG", null, Long.class, 1, 1, 1, ElasticSearch)
    , UNSIGNED_LONG(CATEGORY.INT, "UNSIGNED_LONG", null, Long.class, 1, 1, 1, ElasticSearch)
    , OID(CATEGORY.INT, "OID", null, Long.class, 1, 1, 1, KingBase)
    , LARGEINT(CATEGORY.INT, "LARGEINT", null, Long.class,1, 1, 1, Doris)
    , MEDIUMINT(CATEGORY.INT, "MEDIUMINT", null, Integer.class,1, 1, 1, MySQL)
    , INTEGER(CATEGORY.INT, "INTEGER", INT32, Integer.class,1, 1, 1, MySQL, SQLite, HANA, ElasticSearch, Derby, KingBase)
    , SMALLINT(CATEGORY.INT, "SMALLINT", INT16, Integer.class,1, 1, 1, MySQL, Informix, GBase8S, SinoDB, HANA, Derby, KingBase, Doris)
    , TINYINT(CATEGORY.INT, "TINYINT", INT8, Integer.class,1, 1, 1, MySQL, HANA, KingBase, Doris)
    , SERIAL(CATEGORY.INT, "SERIAL", INT32,  Integer.class, 1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , SERIAL2(CATEGORY.INT, "SERIAL2", INT16, Integer.class,1, 1, 1, PostgreSQL)
    , SERIAL4(CATEGORY.INT, "SERIAL4", INT32, Integer.class,1, 1, 1, PostgreSQL)
    , SERIAL8(CATEGORY.INT, "SERIAL8", INT64, Long.class,1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , SMALLSERIAL(CATEGORY.INT, "SMALLSERIAL", SMALLINT, Integer.class,1, 1, 1, PostgreSQL)
    , BIGSERIAL(CATEGORY.INT, "BIGSERIAL", BIGINT, Long.class,1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , BOOLEAN(CATEGORY.BOOLEAN, "BOOLEAN", null, Boolean.class,1, 1, 1, Informix, GBase8S, SinoDB, HANA, ElasticSearch, KingBase)
    , BOOL(CATEGORY.BOOLEAN, "BOOL", null, Boolean.class,1, 1, 1, PostgreSQL, Doris) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return BOOLEAN.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              number-double/float
     *
     * ****************************************************************************************************************/
    , DECIMAL(CATEGORY.FLOAT, "DECIMAL", null, BigDecimal.class, 1, 0, 0, MySQL, PostgreSQL, ORACLE, Informix, GBase8S, SinoDB, HANA, Derby, Doris) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            BigDecimal result = BasicUtil.parseDecimal(value, null);
            if(null != def && null == result) {
                result = BasicUtil.parseDecimal(def, null);
            }
            return result;
        }
    }
    , DEC(CATEGORY.FLOAT, "DEC", null, BigDecimal.class, 1, 0, 0, DM) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
           return DECIMAL.write(value, def, array, placeholder);
        }
    }
   , SMALLDECIMAL(CATEGORY.FLOAT, "SMALLDECIMAL", null, BigDecimal.class, 1, 0, 0, HANA) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DECIMAL.write(value, def, placeholder);
        }
    }
   , DOUBLE(CATEGORY.FLOAT, "DOUBLE", null, Double.class, 1, 0, 0, MySQL, Informix, GBase8S, SinoDB, HANA, IoTDB, ElasticSearch, Derby, Doris) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            Double result = BasicUtil.parseDouble(value, null);
            if(null != def && null == result) {
                result = BasicUtil.parseDouble(def, null);
            }
            return result;
        }
    }
    , DOUBLE_PRECISION(CATEGORY.FLOAT, "DOUBLE PRECISION", null, Double.class, 1, 1, 1, H2) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }

    /**
     * mysql(p, s)
     * pg:
     * informix(p)
     * oracle(p)
     * mysql, oracle(BigDecimal)
     */
   , FLOAT(CATEGORY.FLOAT, "FLOAT", null, Float.class, 1, 2, 3, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            Float result = BasicUtil.parseFloat(value, null);
            if(null != def && null == result) {
                result = BasicUtil.parseFloat(def, null);
            }
            return result;
        }
    }
   , SMALLFLOAT(CATEGORY.FLOAT, "SMALLFLOAT", null, Float.class, 1, 0, 1, Informix, GBase8S, SinoDB) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    , FLOAT4(CATEGORY.FLOAT, "FLOAT4", null, Float.class, 1, 2, 1, PostgreSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    , SCALED_FLOAT(CATEGORY.FLOAT, "SCALED_FLOAT", null, Float.class, 1, 2, 1, ElasticSearch) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    , HALF_FLOAT(CATEGORY.FLOAT, "HALF_FLOAT", null, Float.class, 1, 2, 1, ElasticSearch) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    , FLOAT8(CATEGORY.FLOAT, "FLOAT8", null, Double.class, 1, 2, 1, PostgreSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DOUBLE.write(value, def, placeholder);}
    }
    , DECFLOAT(CATEGORY.FLOAT, "DECFLOAT", null, Double.class, 1, 2, 1, H2) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DOUBLE.write(value, def, placeholder);}
    }
   , BINARY_DOUBLE(CATEGORY.FLOAT, "BINARY_DOUBLE", null, Double.class, 1, 0, 0, ORACLE) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DOUBLE.write(value, def, placeholder);}
    }
   , BINARY_FLOAT(CATEGORY.FLOAT, "BINARY_FLOAT", null, Float.class, 1, 0, 0, ORACLE) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DOUBLE.write(value, def, placeholder);}
    }
   , MONEY(CATEGORY.FLOAT, "MONEY", null, BigDecimal.class, 1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DECIMAL.write(value, def, placeholder);}
    }
   , SMALLMONEY(CATEGORY.FLOAT, "SMALLMONEY", null, BigDecimal.class, 1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DECIMAL.write(value, def, placeholder);}
    }
   , NUMERIC(CATEGORY.FLOAT, "NUMERIC", null, BigDecimal.class, 1, 0, 0, MySQL, SQLite, Informix, GBase8S, SinoDB, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DECIMAL.write(value, def, placeholder);}
    }
   , NUMBER(CATEGORY.FLOAT, "NUMBER", null, BigDecimal.class, 1, 2, 2, ORACLE) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DECIMAL.write(value, def, placeholder);}
    }
   , REAL(CATEGORY.FLOAT, "REAL", DOUBLE, Double.class, 1, 0, 0, MySQL, SQLite, Informix, GBase8S, SinoDB, HANA, Derby, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }

    ,UINT8(CATEGORY.INT, "UInt8", INTEGER, Short.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return INTEGER.write(value, def, placeholder);}
    }
    ,UINT16(CATEGORY.INT, "UInt16", INTEGER, Short.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return INTEGER.write(value, def, placeholder);}
    }
    ,UINT32(CATEGORY.INT, "UInt32", INTEGER, Integer.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return INTEGER.write(value, def, placeholder);}
    }
    ,UINT64(CATEGORY.INT, "UInt64", INTEGER, Long.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return INTEGER.write(value, def, placeholder);}
    }
    ,UINT128(CATEGORY.INT, "UInt128", INTEGER, Long.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return INTEGER.write(value, def, placeholder);}
    }
    ,UINT256(CATEGORY.INT, "UInt256", INTEGER, Long.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return INTEGER.write(value, def, placeholder);}
    }
    ,FLOAT32(CATEGORY.FLOAT, "Float32", FLOAT, Long.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    ,FLOAT64(CATEGORY.FLOAT, "Float64", DOUBLE, Long.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    ,DECIMAL32(CATEGORY.FLOAT, "Decimal32", DECIMAL, BigDecimal.class, 1, 0, 2, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    ,DECIMAL64(CATEGORY.FLOAT, "Decimal64", DECIMAL, BigDecimal.class, 1, 0, 2, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    ,DECIMAL128(CATEGORY.FLOAT, "Decimal128", DECIMAL, BigDecimal.class, 1, 0, 2, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    ,DECIMAL256(CATEGORY.FLOAT, "Decimal256", DECIMAL, BigDecimal.class, 1, 0, 2, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return FLOAT.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              date
     *                               write 需要根据数据库类型 由内置函数转换
     *
     * ****************************************************************************************************************/
    , DATE(CATEGORY.DATE, "DATE", null, java.sql.Date.class, 1, 1, 1, MySQL, PostgreSQL, Informix, GBase8S, SinoDB, HANA, Derby, Doris, ElasticSearch) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            Date date = DateUtil.parse(value);
            if(null == date && null != def) {
                date = DateUtil.parse(def);
            }
            if (null != date) {
                if(placeholder) {
                    value = new java.sql.Date(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date, "yyyy-MM-dd") +"'";
                }
            }
            return value;
        }
    }
    , DATEV2(CATEGORY.DATE, "DATEV2", null, java.sql.Date.class, 1, 1, 1, SelectDB) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATE.write(value, def, placeholder);}
    }
    , DATE_NANOS(CATEGORY.DATE, "date_nanos", null, java.sql.Date.class, 1, 1, 1, ElasticSearch) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            Date date = DateUtil.parse(value);
            if(null == date && null != def) {
                date = DateUtil.parse(def);
            }
            if (null != date) {
                if(placeholder) {
                    value = new java.sql.Date(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date, "yyyy-MM-dd") +"'";
                }
            }
            return value;
        }
    }
   , DATETIME(CATEGORY.DATETIME, "DATETIME", "DATETIME({S})", null, LocalDateTime.class, 1, 1, 2, MySQL, Informix, GBase8S, SinoDB, Doris) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            Date date = DateUtil.parse(value);
            if(null == date && null != def) {
                date = DateUtil.parse(def);
            }
            if(null != date) {
                if(placeholder) {
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
    , DATETIMEV2(CATEGORY.DATETIME, "DATETIMEV2", null, java.sql.Timestamp.class, 1, 1, 1, SelectDB) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATETIME.write(value, def, placeholder);}
    }
    , DATETIME2(CATEGORY.DATETIME, "DATETIME2", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATETIME.write(value, def, placeholder);}
    }
    , DATETIME_WITH_TIME_ZONE(CATEGORY.DATETIME, "DATETIME WITH TIME ZONE", "DATETIME({S}) WITH TIME ZONE", null, java.sql.Timestamp.class, 1, 1, 2, DM) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATETIME.write(value, def, placeholder);}
    }

    /**
     * MSSQL<br/>
     * 2020-01-01 15:10:10.0000011
     */
   , DATETIMEOFFSET(CATEGORY.DATETIME, "DATETIMEOFFSET", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATETIME.write(value, def, placeholder);}
    }
   , SMALLDATETIME(CATEGORY.DATETIME, "SMALLDATETIME", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATETIME.write(value, def, placeholder);}
    }
   , SQL_DATETIMEOFFSET(CATEGORY.DATETIME, "SQL_DATETIMEOFFSET", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATETIME.write(value, def, placeholder);}
    }
   , SECONDDATE(CATEGORY.DATE, "SECONDDATE", null, java.util.Date.class, 1, 1, 1, HANA) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return DATETIME.write(value, def, placeholder);}
    }
   , TIME(CATEGORY.TIME, "TIME", null, java.sql.Time.class, 1, 1, 1, MySQL, PostgreSQL, HANA, Derby) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            Date date = DateUtil.parse(value);
            if(null == date && null != def) {
                date = DateUtil.parse(def);
            }
            if(null != date) {
                if(placeholder) {
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
    , TIME_WITH_TIME_ZONE(CATEGORY.TIME, "TIME WITH TIME ZONE","TIME({S}) WITH TIME ZONE", null, java.sql.Time.class, 1, 1, 2, DM, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return TIME.write(value, def, placeholder);
        }
    }
    , TIME_WITH_ZONE(CATEGORY.TIME, "TIME WITH TIME ZONE","TIME({S}) WITH TIME ZONE", null, java.sql.Time.class, 1, 1, 2, DM, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return TIME.write(value, def, placeholder);
        }
    }
    , TIMEZ(CATEGORY.TIME, "TIMEZ", null, java.sql.Time.class, 1, 1, 1, PostgreSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return TIME.write(value, def, placeholder);
        }
    }
    , TIMETZ(CATEGORY.TIME, "TIMEZ", null, java.sql.Time.class, 1, 1, 1, Redshift) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return TIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMP(CATEGORY.TIMESTAMP, "TIMESTAMP", "TIMESTAMP({S})", null, java.sql.Timestamp.class, 1, 1, 2, MySQL, PostgreSQL, ORACLE, HANA, Derby) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATETIME.write(value, def, placeholder);
        }
    }
    , TIMESTAMP_WITH_TIME_ZONE(CATEGORY.TIMESTAMP,  "TIMESTAMP WITH TIME ZONE", "TIMESTAMP({S}) WITH TIME ZONE", null, java.sql.Timestamp.class, 1, 1, 2, PostgreSQL, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATETIME.write(value, def, placeholder);
        }
    }
    , TIMESTAMP_WITH_ZONE(CATEGORY.TIMESTAMP, "TIMESTAMP WITH TIME ZONE", "TIMESTAMP({S}) WITH TIME ZONE", null, java.sql.Timestamp.class, 1, 1, 2, PostgreSQL, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATETIME.write(value, def, placeholder);
        }
    }
    , TIMESTAMP_WITH_LOCAL_ZONE(CATEGORY.TIMESTAMP, "TIMESTAMP WITH LOCAL TIME ZONE", "TIMESTAMP({S}) WITH LOCAL TIME ZONE", null, java.sql.Timestamp.class, 1, 1, 2, PostgreSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATETIME.write(value, def, placeholder);
        }
    }
    , TIMESTAMP_WITH_LOCAL_TIME_ZONE(CATEGORY.TIMESTAMP, "TIMESTAMP WITH LOCAL TIME ZONE","TIMESTAMP({S}) WITH LOCAL TIME ZONE", null, java.sql.Timestamp.class, 1, 1, 2, PostgreSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATETIME.write(value, def, placeholder);
        }
    }
    , TIMESTAMPTZ(CATEGORY.TIMESTAMP, "TIMESTAMPTZ", null, java.sql.Timestamp.class, 1, 1, 1, PostgreSQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATETIME.write(value, def, placeholder);
        }
    }
    , TIMESTAMP_NTZ(CATEGORY.TIMESTAMP, "TIMESTAMP_NTZ", null, java.sql.Timestamp.class, 1, 1, 1, MaxCompute) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return TIMESTAMP.write(value, def, placeholder);
        }
    }
   , YEAR(CATEGORY.DATE, "YEAR", null, java.sql.Date.class, 1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATE.write(value, def, placeholder);
        }
    }
    , DATE32(CATEGORY.DATE, "DATE32", null, java.sql.Date.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATE.write(value, def, placeholder);
        }
    }
    , DATETIME64(CATEGORY.DATE, "DATETIME64", null, java.sql.Timestamp.class, 1, 1, 1, ClickHouse) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return DATETIME.write(value, def, placeholder);
        }
    }
    , INTERVAL_DAY(CATEGORY.INTERVAL, "INTERVAL DAY", null, null, 1, 2, 2, ORACLE, KingBase)
    , INTERVAL_DAY_HOUR(CATEGORY.INTERVAL, "INTERVAL DAY TO HOUR", "INTERVAL DAY({P}) TO HOUR({S})", null, null, 1, 2, 2, ORACLE, KingBase)
    , INTERVAL_DAY_MINUTE(CATEGORY.INTERVAL, "INTERVAL DAY TO MINUTE","INTERVAL DAY({P}) TO MINUTE({S})", null, null, 1, 2, 2, ORACLE, KingBase)
    , INTERVAL_DAY_SECOND(CATEGORY.INTERVAL, "INTERVAL DAY TO SECOND", "INTERVAL DAY({P}) TO SECOND({S})", null, null, 1, 2, 2, ORACLE, KingBase)
    , INTERVAL_HOUR(CATEGORY.INTERVAL, "INTERVAL HOUR", "INTERVAL HOUR({P})", null, null, 1, 2, 1, ORACLE, KingBase)
    , INTERVAL_HOUR_MINUTE(CATEGORY.INTERVAL, "INTERVAL HOUR TO MINUTE", "INTERVAL HOUR({P}) TO MINUTE({S})", null, null, 1, 2, 2, ORACLE, KingBase)
    , INTERVAL_HOUR_SECOND(CATEGORY.INTERVAL, "INTERVAL HOUR TO SECOND", "INTERVAL HOUR({P}) TO SECOND({S})", null, null, 1, 2, 2, ORACLE, KingBase)
    , INTERVAL_MINUTE(CATEGORY.INTERVAL, "INTERVAL MINUTE", "INTERVAL MINUTE({P})", null, null, 1, 2, 1, ORACLE, KingBase)
    , INTERVAL_MINUTE_SECOND(CATEGORY.INTERVAL, "INTERVAL MINUTE TO SECOND", "INTERVAL MINUTE({P}) TO SECOND({S})", null, null, 1, 2, 2, ORACLE, KingBase)
    , INTERVAL_MONTH(CATEGORY.INTERVAL, "INTERVAL MONTH", "INTERVAL MONTH({P})", null, null, 1, 2, 1, ORACLE, KingBase)
    , INTERVAL_SECOND(CATEGORY.INTERVAL, "INTERVAL SECOND", "INTERVAL SECOND({P})", null, null, 1, 2, 1, ORACLE, KingBase)
    , INTERVAL_YEAR(CATEGORY.INTERVAL, "INTERVAL YEAR", "INTERVAL YEAR({P})", null, null, 1, 2, 1, ORACLE, KingBase)
    , INTERVAL_YEAR_MONTH(CATEGORY.INTERVAL, "INTERVAL YEAR TO MONTH", "INTERVAL YEAR({P}) TO MONTH({S})",null, null, 1, 2, 2, ORACLE, KingBase)
    , DURATION(CATEGORY.INTERVAL, "DURATION", null, null, 1, 1, 1, Nebula)

    /* *****************************************************************************************************************
     *
     *                                              byte[]
     *
     * ****************************************************************************************************************/
   , BLOB(CATEGORY.BLOB, "BLOB", null, byte[].class, 1, 1, 1, MySQL, ORACLE, SQLite, Informix, GBase8S, SinoDB, HANA, Derby, KingBase) {
        public Object read(Object value, Object def, Class clazz) {
            if(clazz == byte[].class) {

            }else if(clazz == String.class) {
                value = new String((byte[])value);
            }
            return value;
        }
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            if(value instanceof byte[]) {

            }else {
                if(value instanceof String) {
                    String str = (String)value;
                    if(Base64Util.verify(str)) {
                        try {
                            value = Base64Util.decode(str);
                        }catch (Exception e) {
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
    , VARBYTE(CATEGORY.BYTES, "VARBYTE", null, byte[].class, 1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return BLOB.write(value, def, placeholder);}
    }
    , BYTES(CATEGORY.BYTES, "BYTES", null, byte[].class, 1, 1, 1, InfluxDB) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return BLOB.write(value, def, placeholder);}
    }
   , LONGBLOB(CATEGORY.BYTES, "LONGBLOB", null, byte[].class, 1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return BLOB.write(value, def, placeholder);}
    }
   , MEDIUMBLOB(CATEGORY.BYTES, "MEDIUMBLOB", null, byte[].class, 1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return BLOB.write(value, def, placeholder);}
    }
   , TINYBLOB(CATEGORY.BYTES, "TINYBLOB", null, byte[].class, 1, 1, 1, MySQL) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return BLOB.write(value, def, placeholder);}
    }
   , MULTILINESTRING(CATEGORY.BYTES, "MULTILINESTRING", null, byte[].class, 1, 1, 1, MySQL)
   , BYTEA(CATEGORY.BYTES, "BYTEA", null, byte[].class, 1, 1, 1, PostgreSQL)
   , JSONB(CATEGORY.BYTES, "JSONB", null, byte[].class, 1, 1, 1, PostgreSQL, KingBase) {
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {return BLOB.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]-file
     *
     * ****************************************************************************************************************/
   , IMAGE(CATEGORY.BYTES, "IMAGE", null, byte[].class, 1, 1, 1, MSSQL, DM)
   , BFILE(CATEGORY.BYTES, "BFILE", null, byte[].class, 1, 1, 1, ORACLE, KingBase, DM)
    /* *****************************************************************************************************************
     *
     *                                              byte[]-geometry
     *
     * ****************************************************************************************************************/
   , POINT(CATEGORY.GEOMETRY, "POINT", null, Point.class, byte[].class, 1, 1, 1, MySQL, PostgreSQL, KingBase) {
        public Object read(Object value, Object def, Class clazz) {
            if(null == value) {
                return value;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            if(null == value) {
                value = def;
            }
            if(value instanceof byte[]) {
                return value;
            }
            return value;
        }
    }
   , ST_POINT(CATEGORY.GEOMETRY, "ST_POINT", null, Point.class, byte[].class, 1, 1, 1, MySQL, PostgreSQL) {
        public Object read(Object value, Object def, Class clazz) {
            return POINT.read(value, def, clazz);
        }
        public Object write(Object value, Object def, boolean array, Boolean placeholder) {
            return POINT.write(value, def, placeholder);
        }
    }
    , GEOGRAPHY_POINT(CATEGORY.GEOMETRY, "GEOGRAPHY_POINT", null, Point.class, byte[].class, 1, 1, 1, VoltDB)
    , MULTIPOLYGON(CATEGORY.GEOMETRY, "MULTIPOLYGON", null, MultiPolygon.class, byte[].class, 1, 1, 1, MySQL)
    , RING(CATEGORY.GEOMETRY, "RING", null, Ring.class, byte[].class, 1, 1, 1, ClickHouse)
    , MULTIPOINT(CATEGORY.GEOMETRY, "MULTIPOINT", null, MultiPoint.class, byte[].class, 1, 1, 1, MySQL)
    , POLYGON(CATEGORY.GEOMETRY, "POLYGON", null, Polygon.class, byte[].class, 1, 1, 1, MySQL, PostgreSQL, KingBase)
    , GEOMETRY(CATEGORY.GEOMETRY, "GEOMETRY", null, byte[].class, 1, 1, 1, MySQL)
    , ST_GEOMETRY(CATEGORY.GEOMETRY, "ST_GEOMETRY", null, byte[].class, 1, 1, 1, HANA)
    , GEOMETRYCOLLECTION(CATEGORY.GEOMETRY, "GEOMETRYCOLLECTION", null, byte[].class, 1, 1, 1, MySQL)
    , HIERARCHYID(CATEGORY.GEOMETRY, "HIERARCHYID", null, byte[].class, 1, 1, 1, MSSQL)
    , LINE(CATEGORY.GEOMETRY, "LINE", null, LineString.class, byte[].class, 1, 1, 1, PostgreSQL, KingBase)
    , LINESTRING(CATEGORY.GEOMETRY, "LINESTRING", null, LineString.class, byte[].class, 1, 1, 1, MySQL)
    , PATH(CATEGORY.GEOMETRY, "PATH", null, LineString.class, 1, 1, 1, PostgreSQL, KingBase)
    , LSEG(CATEGORY.GEOMETRY, "LSEG", null, byte[].class, 1, 1, 1, PostgreSQL, KingBase)
    , GEOGRAPHY(CATEGORY.GEOMETRY, "GEOGRAPHY", null, byte[].class, 1, 1, 1, MSSQL, PostgreSQL)
    , BOX(CATEGORY.GEOMETRY, "BOX", null, byte[].class, 1, 1, 1, PostgreSQL, KingBase)
    , CIDR(CATEGORY.GEOMETRY, "CIDR", null, byte[].class, 1, 1, 1, PostgreSQL, KingBase)
    , CIRCLE(CATEGORY.GEOMETRY, "CIRCLE", null, byte[].class, 1, 1, 1, PostgreSQL)
    , INET(CATEGORY.GEOMETRY, "INET", null, byte[].class, 1, 1, 1, PostgreSQL, KingBase)

    /* *****************************************************************************************************************
     *
     *                                              vector
     *
     * ****************************************************************************************************************/


    , BINARY_VECTOR(CATEGORY.VECTOR, "BINARY_VECTOR", null, ArrayList.class, 1, 0, 1, Milvus)
    , FLOAT_VECTOR(CATEGORY.VECTOR, "FLOAT_VECTOR", null, ArrayList.class, 1, 0, 1, Milvus)
    , FLOAT16_VECTOR(CATEGORY.VECTOR, "FLOAT16_VECTOR", null, ArrayList.class, 1, 0, 1, Milvus)
    , BFLOAT16_VECTOR(CATEGORY.VECTOR, "BFLOAT16_VECTOR", null, ArrayList.class, 1, 0, 1, Milvus)
    , SPARSE_FLOAT_VECTOR(CATEGORY.VECTOR, "SPARSE_FLOAT_VECTOR", null, ArrayList.class, 1, 0, 1, Milvus)
    , GTSVECTOR(CATEGORY.VECTOR, "GTSVECTOR", null, ArrayList.class, 1, 1, 1, KingBase)
    , TSVECTOR(CATEGORY.VECTOR, "TSVECTOR", null, ArrayList.class, 1, 1, 1, PostgreSQL, KingBase)
    , DENSE_VECTOR(CATEGORY.VECTOR, "dense_vector", null, ArrayList.class, 1, 1, 1, ElasticSearch)
    , VECTOR(CATEGORY.VECTOR, "vector", null, ArrayList.class, 1, 0, 1)
    , SPARSE_VECTOR(CATEGORY.VECTOR, "sparse_vector", null, ArrayList.class, 1, 1, 1, ElasticSearch)
    /* *****************************************************************************************************************
     *
     *                                              待实现
     *
     * ****************************************************************************************************************/

    , BINARY_INTEGER(CATEGORY.NONE, "BINARY_INTEGER", null, null, 1, 1, 1, KingBase)
    , BIT_VARYING(CATEGORY.NONE, "BIT VARYING", null, null, 1, 1, 1, KingBase)
    , BPCHAR(CATEGORY.NONE, "BPCHAR", null, null, 1, 1, 1, KingBase)
    , BPCHARBYTE(CATEGORY.NONE, "BPCHARBYTE", null, null, 1, 1, 1, KingBase)
    , CHARACTER(CATEGORY.NONE, "CHARACTER", null, null, 1, 1, 1, KingBase)
    , CID(CATEGORY.NONE, "CID", null, null, 1, 1, 1, KingBase)
    , DATERANGE(CATEGORY.NONE, "DATERANGE", null, null, 1, 1, 1, KingBase)
    , DSINTERVAL(CATEGORY.NONE, "DSINTERVAL", null, null, 1, 1, 1, KingBase)
    , INT4RANGE(CATEGORY.NONE, "INT4RANGE", null, null, 1, 1, 1, KingBase)
    , INT8RANGE(CATEGORY.NONE, "INT8RANGE", null, null, 1, 1, 1, KingBase)
    , JSONPATH(CATEGORY.NONE, "JSONPATH", null, null, 1, 1, 1, KingBase)
    , MACADDR8(CATEGORY.NONE, "MACADDR8", null, null, 1, 1, 1, KingBase)
    , NATURALN(CATEGORY.NONE, "NATURALN", null, null, 1, 1, 1, KingBase)
    , NUMRANGE(CATEGORY.NONE, "NUMRANGE", null, null, 1, 1, 1, KingBase)
    , ORA_DATE(CATEGORY.NONE, "ORA_DATE", null, null, 1, 1, 1, KingBase)
    , POSITIVE(CATEGORY.NONE, "POSITIVE", null, null, 1, 1, 1, KingBase)
    , POSITIVEN(CATEGORY.NONE, "POSITIVEN", null, null, 1, 1, 1, KingBase)
    , REFCURSOR(CATEGORY.NONE, "REFCURSOR", null, null, 1, 1, 1, KingBase)
    , REGCLASS(CATEGORY.NONE, "REGCLASS", null, null, 1, 1, 1, KingBase)
    , REGCONFIG(CATEGORY.NONE, "REGCONFIG", null, null, 1, 1, 1, KingBase)
    , REGDICTIONARY(CATEGORY.NONE, "REGDICTIONARY", null, null, 1, 1, 1, KingBase)
    , REGNAMESPACE(CATEGORY.NONE, "REGNAMESPACE", null, null, 1, 1, 1, KingBase)
    , REGOPER(CATEGORY.NONE, "REGOPER", null, null, 1, 1, 1, KingBase)
    , REGOPERATOR(CATEGORY.NONE, "REGOPERATOR", null, null, 1, 1, 1, KingBase)
    , REGPROC(CATEGORY.NONE, "REGPROC", null, null, 1, 1, 1, KingBase)
    , REGPROCEDURE(CATEGORY.NONE, "REGPROCEDURE", null, null, 1, 1, 1, KingBase)
    , REGTYPE(CATEGORY.NONE, "REGTYPE", null, null, 1, 1, 1, KingBase)
    , REGROLE(CATEGORY.NONE, "REGROLE", null, null, 1, 1, 1, KingBase)
    , SIGNTYPE(CATEGORY.NONE, "SIGNTYPE", null, null, 1, 1, 1, KingBase)
    , SIMPLE_DOUBLE(CATEGORY.NONE, "SIMPLE_DOUBLE", null, null, 1, 1, 1, KingBase)
    , SIMPLE_INTEGER(CATEGORY.NONE, "SIMPLE_INTEGER", null, null, 1, 1, 1, KingBase)
    , SIMPLE_FLOAT(CATEGORY.NONE, "SIMPLE_FLOAT", null, null, 1, 1, 1, KingBase)
    , TID(CATEGORY.NONE, "TID", null, null, 1, 1, 1, KingBase)
    , TIME_WITHOUT_TIME_ZONE(CATEGORY.NONE, "TIME WITHOUT TIME ZONE", null, null, 1, 1, 1, KingBase)
    , TIME_TZ_UNCONSTRAINED(CATEGORY.NONE, "TIME TZ UNCONSTRAINED", null, null, 1, 1, 1, KingBase)
    , TIME_UNCONSTRAINED(CATEGORY.NONE, "TIME_UNCONSTRAINED", null, null, 1, 1, 1, KingBase)
    , TIMESTAMP_WITHOUT_TIME_ZONE(CATEGORY.NONE, "TIMESTAMP WITHOUT TIME ZONE", null, null, 1, 1, 1, KingBase)
    , TSRANGE(CATEGORY.NONE, "TSRANGE", null, null, 1, 1, 1, KingBase)
    , TSTZRANGE(CATEGORY.NONE, "TSTZRANGE", null, null, 1, 1, 1, KingBase)
    , VARCHARBYTE(CATEGORY.NONE, "VARCHARBYTE", null, null, 1, 1, 1, KingBase)
    , XID(CATEGORY.NONE, "XID", null, null, 1, 1, 1, KingBase)
    , YMINTERVAL(CATEGORY.NONE, "YMINTERVAL", null, null, 1, 1, 1, ElasticSearch)
    , RANGE(CATEGORY.NONE, "RANGE", null, null, 1, 1, 1, KingBase)
    , CURSOR(CATEGORY.NONE, "CURSOR", null, null, 1, 1, 1, XuGu)
    , ENUM(CATEGORY.NONE, "ENUM", null, String.class, 1, 1, 1, MySQL)
    , INTERVAL(CATEGORY.NONE, "INTERVAL", null, null, 1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , GUID(CATEGORY.NONE, "GUID", null, null, 1, 1, 1, XuGu)
    , MACADDR(CATEGORY.NONE, "MACADDR", null, null, 1, 1, 1, PostgreSQL, KingBase)
    , MULTISET(CATEGORY.NONE, "MULTISET", null, String.class, 1, 1, 1, SinoDB)
    , JAVA_OBJECT(CATEGORY.NONE, "JAVA_OBJECT", null, String.class, 1, 1, 1, H2)
    , RAW(CATEGORY.NONE, "RAW", null, byte[].class, 1, 1, 1, ORACLE)
    , ROW(CATEGORY.NONE, "ROW", null, null, 1, 1, 1, H2)
    , ROWID(CATEGORY.NONE, "ROWID", null, null, 1, 1, 1, ORACLE)
    , SYS_REFCURSOR(CATEGORY.NONE, "SYS_REFCURSOR", null, null, 1, 1, 1, XuGu)
    , SET(CATEGORY.NONE, "SET", null, String.class, 1, 1, 1, MySQL, SinoDB)
    , LIST(CATEGORY.NONE, "LIST", null, String.class, 1, 1, 1, SinoDB)
    , TSQUERY(CATEGORY.NONE, "TSQUERY", null, null, 1, 1, 1, PostgreSQL, KingBase)
    , PG_SNAPSHOT(CATEGORY.NONE, "PG_SNAPSHOT", null, null, 1, 1, 1, PostgreSQL)
    /**
     * pg
     * 弃用 换成pg_snapshot
     */
   , TXID_SNAPSHOT(CATEGORY.NONE, "TXID_SNAPSHOT", null, null, 1, 1, 1, PostgreSQL, KingBase)
   , UROWID(CATEGORY.NONE, "UROWID", null, null, 1, 1, 1, ORACLE)
   , SQL_VARIANT(CATEGORY.NONE, "SQL_VARIANT", null, null, 1, 1, 1, MSSQL)

   , KEYWORD(CATEGORY.NONE, "KEYWORD", null, null, 1, 1, 1, ElasticSearch)

    , OBJECT(CATEGORY.NONE, "OBJECT", null, null, 1, 1, 1, ElasticSearch)

    , ARRAY(CATEGORY.NONE, "ARRAY", null, null, 2, 2, 2, Doris)

    , MAP(CATEGORY.NONE, "MAP", null, null, 1, 1, 1, Doris)

    , STRUCT(CATEGORY.NONE, "STRUCT", null, null, 1, 1, 1, Doris)
    , STRUCTS(CATEGORY.NONE, "STRUCTS", null, null, 1, 1, 1, Hive)

    , AGG_STATE(CATEGORY.NONE, "AGG_STATE", null, null, 1, 1, 1, Doris)
    , LowCardinality(CATEGORY.NONE, "LowCardinality", null, null, 1, 1, 1, ClickHouse)
    , SimpleAggregateFunction(CATEGORY.NONE, "SimpleAggregateFunction", null, null, 1, 1, 1, ClickHouse)
    , TUPLE(CATEGORY.NONE, "TUPLE", null, null, 1, 1, 1, ClickHouse)
    , IP(CATEGORY.NONE, "ip", null, null, 1, 1, 1, ElasticSearch)
    , IPV4(CATEGORY.NONE, "IPV4", null, null, 1, 1, 1, ClickHouse)
    , IPV6(CATEGORY.NONE, "IPV6", null, null, 1, 1, 1, ClickHouse)
    , ALIAS(CATEGORY.NONE, "alias", null, null, 1, 1, 1, ElasticSearch)
    , COMPLETION(CATEGORY.NONE, "completion", null, null, 1, 1, 1, ElasticSearch)
    , FLATTENED(CATEGORY.NONE, "flattened", null, null, 1, 1, 1, ElasticSearch)
    , JOIN(CATEGORY.NONE, "join", null, null, 1, 1, 1, ElasticSearch)
    , GEO_POINT(CATEGORY.NONE, "geo_point", null, null, 1, 1, 1, ElasticSearch)
    , GEO_SHAPE(CATEGORY.NONE, "geo_shape", null, null, 1, 1, 1, ElasticSearch)
    , HISTOGRAM(CATEGORY.NONE, "histogram", null, null, 1, 1, 1, ElasticSearch)
    , PERCOLATOR(CATEGORY.NONE, "percolator", null, null, 1, 1, 1, ElasticSearch)
    , NESTED(CATEGORY.NONE, "nested", null, null, 1, 1, 1, ElasticSearch)
    , RANK_FEATURES(CATEGORY.NONE, "rank_features", null, null, 1, 1, 1, ElasticSearch)
    , RANK_FEATURE(CATEGORY.NONE, "rank_feature", null, null, 1, 1, 1, ElasticSearch)
    , SHAPE(CATEGORY.NONE, "shape", null, null, 1, 1, 1, ElasticSearch)
    , TOKEN_COUNT(CATEGORY.NONE, "token_count", null, null, 1, 1, 1, ElasticSearch)
    , VERSION(CATEGORY.NONE, "version", null, null, 1, 1, 1, ElasticSearch)
    , AGGREGATE_METRIC_DOUBLE(CATEGORY.NONE, "aggregate_metric_double", null, null, 1, 1, 1, ElasticSearch)
    , SUPER(CATEGORY.NONE, "aggregate_metric_double", null, null, 1, 1, 1, Redshift)

    ;

    private final List<DatabaseType> dbs = new ArrayList<>();
    private final CATEGORY category;
    private final TypeMetadata origin;
    /**
     * 主要用来跟输入值对比 定位数据类型
     */
    private final String name;
    /**
     * 主要用来生成SQL 默认与name一致,一些结构复杂的情况需要如:
     * formula = INTERVAL DAY({P}) TO SECOND({S})用来生成SQL
     * name = INTERVAL DAY TO SECOND 用来定位数据类型
     *
     */
    private String formula;
    private Class transfer                  ; //中间转换类型 转换成其他格式前先转换成transfer类型
    private Class compatible          ; //从数据库中读写数据的类型
    private final int ignoreLength;
    private final int ignorePrecision;
    private final int ignoreScale;
    private int maxLength = -1;
    private int maxPrecision = -1;
    private int maxScale = -1;
    private boolean array;
    private Refer refer;
    StandardTypeMetadata(CATEGORY category, String name, String formula, TypeMetadata origin, Class transfer, Class compatible, int ignoreLength, int ignorePrecision, int ignoreScale, DatabaseType ... dbs) {
        this.category = category;
        if(null != origin) {
            this.origin = origin;
        }else{
            this.origin = this;
        }
        if(null != dbs) {
            for(DatabaseType db:dbs) {
                this.dbs.add(db);
            }
        }
        this.name = name;
        this.transfer = transfer;
        this.compatible = compatible;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
        this.formula = formula;
    }
    StandardTypeMetadata(CATEGORY category, String name, TypeMetadata origin, Class transfer, Class compatible, int ignoreLength, int ignorePrecision, int ignoreScale, DatabaseType ... dbs) {
        this(category, name, null, origin, transfer, compatible, ignoreLength, ignorePrecision, ignoreScale, dbs);
    }
    StandardTypeMetadata(CATEGORY category, String name, TypeMetadata origin, Class compatible, int ignoreLength, int ignorePrecision, int ignoreScale, DatabaseType ... dbs) {
        this(category, name, null, origin, null, compatible, ignoreLength, ignorePrecision, ignoreScale, dbs);
    }
    StandardTypeMetadata(CATEGORY category, String name, String formula, TypeMetadata origin, Class compatible, int ignoreLength, int ignorePrecision, int ignoreScale, DatabaseType ... dbs) {
        this(category, name, formula, origin,null,  compatible, ignoreLength, ignorePrecision, ignoreScale, dbs);
    }
    public TypeMetadata getOrigin() {
        return origin;
    }

    @Override
    public CATEGORY getCategory() {
        return category;
    }

    @Override
    public CATEGORY_GROUP getCategoryGroup() {
        if(null != category) {
            return category.group();
        }
        return CATEGORY_GROUP.NONE;
    }
    public void compatible(Class compatible){
        this.compatible = compatible;
    }
    @Override
    public Object convert(Object value, Object def) {
        return convert(value, null, def);
    }

    @Override
    public Object convert(Object value, Class target, boolean array) {
        Object def = null;
        return convert(value, target, array, def);
    }

    @Override
    public Object convert(Object value, Class target, boolean array, Object def) {
        if(null == target) {
            target = compatible;
        }
        if(null != value) {
            if(value.getClass() == target) {
                return value;
            }
            if(null != transfer) {
                value = ConvertProxy.convert(value, transfer, array, def);
            }
            value = ConvertProxy.convert(value, target, array, def);
        }
        return value;
    }
    @Override
    public Object convert(Object value, Object obj, Field field) {
        return convert(value, field.getType());
    }

    @Override
    public Object read(Object value, Object def, Class clazz, boolean array) {
        if(null == clazz) {
            clazz = transfer;
        }
        if(null == clazz) {
            clazz = compatible;
        }
        value = ConvertProxy.convert(value, clazz, array, def, false);
        return value;
    }

    /**
     *
     * 以String类型拼接SQL需要引号或类型转换函数
     * @param value  value
     * @param def def
     * @param placeholder 是否需要占位符
     * @return Object
     */
    @Override
    public Object write(Object value, Object def, boolean array, Boolean placeholder) {
        if(null != value) {
            if(value.getClass() != compatible) {
                if (null != transfer) {
                    value = ConvertProxy.convert(value, transfer, array, def);
                }
                value = ConvertProxy.convert(value, compatible, array, def);
            }
            if(null != value && compatible == String.class && !placeholder) {
                value = "'" + value + "'";
            }
        }
        return value;
    }

    @Override
    public String getName() {
        return name;
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
    public int maxLength() {
        return maxLength;
    }

    @Override
    public int maxPrecision() {
        return maxPrecision;
    }

    @Override
    public int maxScale() {
        return maxScale;
    }

    @Override
    public boolean support() {
        return true;
    }

    @Override
    public String formula() {
        return formula;
    }

    @Override
    public Class compatible() {
        return compatible;
    }

    @Override
    public Class transfer() {
        return transfer;
    }

    /**
     * 支持的数据库类型
     * @return DatabaseTypes
     */
    @Override
    public List<DatabaseType> databaseTypes() {
        return dbs;
    }

    @Override
    public boolean isArray() {
        return array;
    }

    @Override
    public void setArray(boolean array) {
        this.array = array;
    }

    @Override
    public Refer refer() {
        if(null == refer) {
            refer = new Refer();
            refer.ignoreLength(ignoreLength).ignorePrecision(ignorePrecision).ignoreScale(ignoreScale);
        }
        return refer;
    }
}
