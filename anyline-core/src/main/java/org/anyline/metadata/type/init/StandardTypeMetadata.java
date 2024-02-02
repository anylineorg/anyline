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

import com.fasterxml.jackson.databind.JsonNode;
import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.init.ConvertAdapter;
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

/*

-- DROP TABLE "public"."chk_column";
, kingbase
CREATE TABLE "public"."chk_column" (
	"c2" aclitem NULL,
	"c6" binary_integer NULL,
	"c7" bit(1) NULL,
	"c8" bit varying NULL,
	"c13" bpchar NULL,
	"c14" bpcharbyte NULL,
	"c15" character(1 char) NULL,
	"c16" character(1 char) NULL,
	"c17" character(1 char) NULL,
	"c20" clob NULL,
	"c22" bytea NULL,
	"c23" date NULL,
	"c24" daterange NULL,
	"c25" datetime NULL,
	"c18" cid NULL,
	"c26" dbms_id NULL,
	"c27" dbms_id_30 NULL,
	"c28" dbms_quoted_id_30 NULL,
	"c29" dbms_quoted_id_30 NULL,
	"c30" "dbms_sql.desc_rec" NULL,
	"c32" double precision NULL,
	"c33" double precision NULL,
	"c34" dsinterval NULL,
	"c35" real NULL,
	"c36" double precision NULL,
	"c37" gtsvector NULL,
	"c41" int4range NULL,
	"c43" int8range NULL,
	"c45" pg_catalog.interval NULL,
	"c46" interval day(2) NULL,
	"c47" interval day(2) NULL,
	"c48" interval day(2) to hour NULL,
	"c49" interval day(2) to minute NULL,
	"c50" interval day(2) to second(6) NULL,
	"c51" interval hour(2) NULL,
	"c52" interval hour(2) NULL,
	"c53" interval hour(2) to minute NULL,
	"c54" interval hour(2) to second(6) NULL,
	"c55" interval minute(2) NULL,
	"c56" interval minute(2) NULL,
	"c57" interval minute(2) to second(6) NULL,
	"c58" interval month(2) NULL,
	"c59" interval month(2) NULL,
	"c60" interval second(2, 6) NULL,
	"c61" interval year(2) NULL,
	"c62" interval year(2) to month NULL,
	"c65" jsonpath NULL,
	"c69" macaddr8 NULL,
	"c71" mysql_date NULL,
	"c72" mysql_time NULL,
	"c73" name NULL COLLATE "c",
	"c74" character(1 char) NULL,
	"c77" "natural" NULL,
	"c78" naturaln NULL,
	"c79" character(1 char) NULL,
	"c84" numrange NULL,
	"c87" oid NULL,
	"c88" ora_date NULL,
	"c92" positive NULL,
	"c93" positiven NULL,
	"c95" refcursor NULL,
	"c96" regclass NULL,
	"c97" regconfig NULL,
	"c98" regdictionary NULL,
	"c99" regnamespace NULL,
	"c100" regoper NULL,
	"c101" regoperator NULL,
	"c102" regproc NULL,
	"c103" regprocedure NULL,
	"c104" regtype NULL,
	"c105" regrole NULL,
	"c110" signtype NULL,
	"c111" simple_double NULL,
	"c112" simple_integer NULL,
	"c113" simple_float NULL,
	"c116" string NULL,
	"c118" tid NULL,
	"c119" time without time zone NULL,
	"c120" time with time zone NULL,
	"c121" time without time zone NULL,
	"c122" time_tz_unconstrained NULL,
	"c123" time_unconstrained NULL,
	"c124" timestamp without time zone NULL,
	"c125" timestamp with time zone NULL,
	"c126" time without time zone NULL,
	"c127" timestamp_ltz_unconstrained NULL,
	"c128" timestamp_tz_unconstrained NULL,
	"c129" timestamp_unconstrained NULL,
	"c130" timestamp with time zone NULL,
	"c131" time with time zone NULL,
	"c135" tsrange NULL,
	"c136" tstzrange NULL,
	"c140" bit varying NULL,
	"c142" varcharbyte NULL,
	"c143" xid NULL,
	"c145" yminterval NULL
);

-- Column comments

COMMENT ON COLUMN "public"."chk_column"."c1" IS '12';*/

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
    CHAR(CATEGORY.CHAR, "CHAR", null, String.class, 0, 1, 1, MySQL, PostgreSQL, Informix, HANA, Derby, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
   , NCHAR(CATEGORY.CHAR, "NCHAR", null, String.class, 0, 1, 1, ORACLE, MSSQL, Informix, GBase8S, SinoDB){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , CLOB(CATEGORY.TEXT, "CLOB", null, String.class,1, 1, 1, ORACLE, Informix, GBase8S, SinoDB, Derby, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , NCLOB(CATEGORY.TEXT, "NCLOB", null, String.class,1, 1, 1, ORACLE, HANA){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    , NVARCHAR(CATEGORY.CHAR, "NVARCHAR", null, String.class, 0, 1, 1, MSSQL, Informix, GBase8S, SinoDB, HANA, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    , LVARCHAR(CATEGORY.CHAR, "LVARCHAR", null, String.class, 0, 1, 1, Informix, GBase8S, SinoDB, SinoDB){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , NVARCHAR2(CATEGORY.CHAR, "NVARCHAR2", NVARCHAR, String.class, 0, 1, 1, ORACLE){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , LONGTEXT(CATEGORY.TEXT, "LONGTEXT", null, String.class,1, 1, 1, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , MEDIUMTEXT(CATEGORY.TEXT, "MEDIUMTEXT", null, String.class,1, 1, 1, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , TEXT(CATEGORY.TEXT, "TEXT", null, String.class,1, 1, 1, MySQL, PostgreSQL, SQLite, Informix, GBase8S, SinoDB, IoTDB, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , NTEXT(CATEGORY.TEXT, "NTEXT", null, String.class,1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , TINYTEXT(CATEGORY.TEXT, "TINYTEXT", null, String.class,1, 1, 1, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * Informix(长度不超过 255 )
     */
   , VARCHAR(CATEGORY.CHAR, "VARCHAR", null, String.class, 0, 1, 1, MySQL, PostgreSQL, ORACLE, Informix, GBase8S, SinoDB, HANA, Derby, KingBase, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , VARCHAR2(CATEGORY.CHAR, "VARCHAR2", VARCHAR, String.class, 0, 1, 1, ORACLE, KingBase, DM, oscar){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , SYSNAME(CATEGORY.TEXT, "SYSNAME", null, String.class,1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , UUID(CATEGORY.TEXT, "UUID", null, String.class,1, 1, 1, PostgreSQL, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
   , UNIQUEIDENTIFIER(CATEGORY.TEXT, "UNIQUEIDENTIFIER", null, String.class,1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql(byte[])
     */
   , BINARY(CATEGORY.BYTES, "BINARY", null, byte[].class, 0, 1, 1, MySQL, MSSQL, HANA, ElasticSearch)
   , VARBINARY(CATEGORY.BYTES, "VARBINARY", null, byte[].class, 0, 1, 1, MySQL, MSSQL, HANA)

    , STRING(CATEGORY.TEXT, "STRING", null, String.class, 1, 1, 1, Doris, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    , FIXED_STRING(CATEGORY.TEXT, "FixedString", null, String.class, 0, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    , HLL(CATEGORY.TEXT, "HLL", null, String.class, 0, 1, 1, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }

    /* *****************************************************************************************************************
     *
     *                                              String-format
     *
     * ****************************************************************************************************************/
   , JSON(CATEGORY.TEXT, "JSON", null, String.class,1, 1, 1, MySQL, PostgreSQL, KingBase, Doris){
        @Override
        public Object convert(Object value, Class target, Object def) {
            if(null == value){
                return def;
            }
            Class transfer = transfer();
            Class compatible = compatible();
            try{
                if(null == target) {
                    JsonNode node = BeanUtil.JSON_MAPPER.readTree(value.toString());
                    if (node.isArray()) {
                        value = DataSet.parseJson(node);
                    } else {
                        value = DataRow.parseJson(node);
                    }
                }else{
                    value = super.convert(value, target, def);
                }
            }catch (Exception e){
                //不能转成DataSet的List
                value = super.convert(value, target, def);
            }
            return value;
        }
        public Object read(Object value, Object def, Class clazz){
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
                        //value = DataSet.parseJson(node);
                        Collection<Object> list = new ArrayList<>();
                        Iterator<JsonNode> items = node.iterator();
                        boolean isDataRow = true;
                        while (items.hasNext()) {
                            JsonNode item = items.next();
                            Object row = DataRow.parseJsonObject(KeyAdapter.KEY_CASE.CONFIG, item);
                            if(row instanceof DataRow){
                            }else{
                                isDataRow = false;
                            }
                            list.add(row);
                        }
                        if(isDataRow){
                            value = DataSet.parse(list);
                        }else{
                            value = list;
                        }
                    } else {
                        value = DataRow.parseJson(node);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return value;}

    }
   , XML(CATEGORY.TEXT, "XML", null, String.class,1, 1, 1, MSSQL, KingBase){}
    /* *****************************************************************************************************************
     *
     *                                              number-int/long
     *
     * ****************************************************************************************************************/
    , BIT(CATEGORY.BOOLEAN, "BIT", null, Boolean.class,1, 1, 1, MySQL, MSSQL)
    , BITMAP(CATEGORY.BYTES, "BITMAP", null, Byte.class,1, 1, 1, Doris)
    , VARBIT(CATEGORY.BOOLEAN, "VARBIT", null, Byte[].class,1, 1, 1, PostgreSQL)
    , SHORT(CATEGORY.INT, "SHORT", null, Short.class,1, 1, 1)

    , BYTE(CATEGORY.INT, "BYTE", null, Short.class,1, 1, 1)
    , INT(CATEGORY.INT, "INT", null, Integer.class,1, 1, 1, MySQL, MSSQL, Informix, GBase8S, SinoDB, Derby, Doris)
    , INT32(CATEGORY.INT, "INT32", null, Integer.class, 1, 1, 1, IoTDB, ClickHouse)
    , INT64(CATEGORY.INT, "INT64", null, Integer.class, 1, 1, 1, IoTDB, ClickHouse)
    , INT128(CATEGORY.INT, "INT128", null, Integer.class, 1, 1, 1, ClickHouse)
    , INT256(CATEGORY.INT, "INT256", null, Integer.class, 1, 1, 1, ClickHouse)
    , INFORMIX_INTEGER(CATEGORY.INT, "INTEGER", null, Integer.class,1, 1, 1, Informix, GBase8S, SinoDB)
    , LONG_TEXT(CATEGORY.TEXT, "LONG", null, String.class,1, 1, 1, ORACLE, ElasticSearch){}
    , INT2(CATEGORY.INT, "INT2", null, Integer.class,1, 1, 1, PostgreSQL)
    , INT4(CATEGORY.INT, "INT4", null, Integer.class,1, 1, 1, PostgreSQL)
    , INT8(CATEGORY.INT, "INT8", null, Long.class,1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , BIGINT(CATEGORY.INT, "BIGINT", null, Long.class,1, 1, 1, MySQL, Informix, GBase8S, SinoDB, HANA, Derby, KingBase, Doris)
    , LARGEINT(CATEGORY.INT, "LARGEINT", null, Long.class,1, 1, 1, Doris)
    , MEDIUMINT(CATEGORY.INT, "MEDIUMINT", null, Integer.class,1, 1, 1, MySQL)
    , INTEGER(CATEGORY.INT, "INTEGER", null, Integer.class,1, 1, 1, MySQL, SQLite, HANA, ElasticSearch, Derby, KingBase)
    , SMALLINT(CATEGORY.INT, "SMALLINT", null, Integer.class,1, 1, 1, MySQL, Informix, GBase8S, SinoDB, HANA, Derby, KingBase, Doris)
    , TINYINT(CATEGORY.INT, "TINYINT", null, Integer.class,1, 1, 1, MySQL, HANA, KingBase, Doris)
    , SERIAL(CATEGORY.INT, "SERIAL", INT,  Integer.class, 1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , SERIAL2(CATEGORY.INT, "SERIAL2", SMALLINT, Integer.class,1, 1, 1, PostgreSQL)
    , SERIAL4(CATEGORY.INT, "SERIAL4", INT, Integer.class,1, 1, 1, PostgreSQL)
    , SERIAL8(CATEGORY.INT, "SERIAL8", BIGINT, Long.class,1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , SMALLSERIAL(CATEGORY.INT, "SERIAL2", SMALLINT, Integer.class,1, 1, 1, PostgreSQL)
    , BIGSERIAL(CATEGORY.INT, "SERIAL8", BIGINT, Long.class,1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , BOOLEAN(CATEGORY.BOOLEAN, "BOOLEAN", null, Boolean.class,1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB, HANA, ElasticSearch, KingBase)
    , BOOL(CATEGORY.BOOLEAN, "BOOLEAN", null, Boolean.class,1, 1, 1, PostgreSQL, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BOOLEAN.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              number-double/float
     *
     * ****************************************************************************************************************/
   , DECIMAL(CATEGORY.FLOAT, "DECIMAL", null, BigDecimal.class, 1, 0, 0, MySQL, PostgreSQL, ORACLE, Informix, GBase8S, SinoDB, HANA, Derby, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
   , SMALLDECIMAL(CATEGORY.FLOAT, "SMALLDECIMAL", null, BigDecimal.class, 1, 0, 0, HANA){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DECIMAL.write(value, def, placeholder);
        }
    }
   , DOUBLE(CATEGORY.FLOAT, "DOUBLE", null, Double.class, 1, 0, 0, MySQL, Informix, GBase8S, SinoDB, HANA, IoTDB, ElasticSearch, Derby, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
    , DOUBLE_PRECISION(CATEGORY.FLOAT, "DOUBLE PRECISION", null, Double.class, 1, 1, 1, H2){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
    /**
     * mysql(p, s)
     * pg:
     * informix(p)
     * oracle(p)
     * mysql, oracle(BigDecimal)
     */
   , FLOAT_MySQL(CATEGORY.FLOAT, "FLOAT", null, Float.class, 1, 2, 2, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
   , FLOAT_INFORMIX(CATEGORY.FLOAT, "FLOAT", null, Float.class, 1, 1, 1, Informix, GBase8S, SinoDB){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT_ORACLE(CATEGORY.FLOAT, "FLOAT", null, Float.class, 1, 0, 1, ORACLE){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , SMALLFLOAT(CATEGORY.FLOAT, "SMALLFLOAT", null, Float.class, 1, 0, 1, Informix, GBase8S, SinoDB){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT_MSSQL(CATEGORY.FLOAT, "FLOAT", null, Float.class, 1, 0, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT4(CATEGORY.FLOAT, "FLOAT4", null, Float.class, 1, 2, 1, PostgreSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT(CATEGORY.FLOAT, "FLOAT", null, Float.class, 1, 2, 1, IoTDB, ElasticSearch, Derby, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
    , FLOAT8(CATEGORY.FLOAT, "FLOAT8", null, Double.class, 1, 2, 1, PostgreSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
    , DECFLOAT(CATEGORY.FLOAT, "DECFLOAT", null, Double.class, 1, 2, 1, H2){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
   , BINARY_DOUBLE(CATEGORY.FLOAT, "BINARY_DOUBLE", null, Double.class, 1, 0, 0, ORACLE){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
   , BINARY_FLOAT(CATEGORY.FLOAT, "BINARY_FLOAT", null, Float.class, 1, 0, 0, ORACLE){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
   , MONEY(CATEGORY.FLOAT, "MONEY", null, BigDecimal.class, 1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , SMALLMONEY(CATEGORY.FLOAT, "SMALLMONEY", null, BigDecimal.class, 1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , NUMERIC(CATEGORY.FLOAT, "NUMERIC", null, BigDecimal.class, 1, 0, 0, MySQL, SQLite, Informix, GBase8S, SinoDB, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , NUMBER(CATEGORY.FLOAT, "NUMBER", null, BigDecimal.class, 1, 2, 2, ORACLE){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , REAL(CATEGORY.FLOAT, "REAL", DOUBLE, Double.class, 1, 0, 0, MySQL, SQLite, Informix, GBase8S, SinoDB, HANA, Derby, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_INT8(CATEGORY.INT, "INT8", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return SHORT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_INT16(CATEGORY.INT, "INT16", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return TINYINT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_INT32(CATEGORY.INT, "INT32", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return SMALLINT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_INT64(CATEGORY.INT, "INT64", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BIGINT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_INT128(CATEGORY.INT, "INT128", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_INT256(CATEGORY.INT, "INT256", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }

    ,CLICKHOUSE_UINT8(CATEGORY.INT, "UINT8", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_UINT16(CATEGORY.INT, "UINT16", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_UINT32(CATEGORY.INT, "UINT32", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_UINT64(CATEGORY.INT, "UINT64", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_UINT128(CATEGORY.INT, "UINT128", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_UINT256(CATEGORY.INT, "UINT256", INTEGER, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return INTEGER.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_FLOAT32(CATEGORY.FLOAT, "FLOAT32", FLOAT, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_FLOAT64(CATEGORY.FLOAT, "FLOAT64", DOUBLE, Double.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_DECIMAL32(CATEGORY.FLOAT, "DECIMAL32", DECIMAL, Double.class, 1, 0, 2, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_DECIMAL64(CATEGORY.FLOAT, "DECIMAL64", DECIMAL, Double.class, 1, 0, 2, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_DECIMAL128(CATEGORY.FLOAT, "DECIMAL128", DECIMAL, Double.class, 1, 0, 2, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    ,CLICKHOUSE_DECIMAL256(CATEGORY.FLOAT, "DECIMAL256", DECIMAL, Double.class, 1, 0, 2, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              date
     *                               write 需要根据数据库类型 由内置函数转换
     *
     * ****************************************************************************************************************/
   , DATE(CATEGORY.DATE, "DATE", null, java.sql.Date.class, 1, 1, 1, MySQL, PostgreSQL, Informix, GBase8S, SinoDB, HANA, Derby, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
   , DATETIME(CATEGORY.DATETIME, "DATETIME", null, LocalDateTime.class, 1, 1, 1, MySQL, Informix, GBase8S, SinoDB, Doris){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
   , DATETIME2(CATEGORY.DATETIME, "DATETIME2", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * MSSQL<br/>
     * 2020-01-01 15:10:10.0000011
     */
   , DATETIMEOFFSET(CATEGORY.DATETIME, "DATETIMEOFFSET", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , SMALLDATETIME(CATEGORY.DATETIME, "SMALLDATETIME", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , SQL_DATETIMEOFFSET(CATEGORY.DATETIME, "SQL_DATETIMEOFFSET", null, java.sql.Timestamp.class, 1, 1, 1, MSSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , SECONDDATE(CATEGORY.DATE, "SECONDDATE", null, java.util.Date.class, 1, 1, 1, HANA){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , TIME(CATEGORY.TIME, "TIME", null, java.sql.Time.class, 1, 1, 1, MySQL, PostgreSQL, HANA, Derby){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
    , TIME_WITH_ZONE(CATEGORY.TIME, "TIME WITH TIME ZONE", null, java.sql.Time.class, 1, 1, 1, DM){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return TIME.write(value, def, placeholder);
        }
    }
   , TIMEZ(CATEGORY.TIME, "TIMEZ", null, java.sql.Time.class, 1, 1, 1, PostgreSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return TIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMP(CATEGORY.TIMESTAMP, "TIMESTAMP", null, java.sql.Timestamp.class, 1, 1, 1, MySQL, PostgreSQL, ORACLE, HANA, Derby){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMP_WITH_ZONE(CATEGORY.TIMESTAMP, "TIMESTAMP WITH TIME ZONE", null, java.sql.Timestamp.class, 1, 1, 1, PostgreSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMP_WITH_LOCAL_ZONE(CATEGORY.TIMESTAMP, "TIMESTAMP WITH LOCAL TIME ZONE", null, java.sql.Timestamp.class, 1, 1, 1, PostgreSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMPTZ(CATEGORY.TIMESTAMP, "TIMESTAMPTZ", null, java.sql.Timestamp.class, 1, 1, 1, PostgreSQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , YEAR(CATEGORY.DATE, "YEAR", null, java.sql.Date.class, 1, 1, 1, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATE.write(value, def, placeholder);
        }
    }
    , CLICKHOUSE_DATE32(CATEGORY.DATE, "DATE32", null, java.sql.Date.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATE.write(value, def, placeholder);
        }
    }
    , CLICKHOUSE_DATETIME64(CATEGORY.DATE, "DATETIME64", null, java.sql.Timestamp.class, 1, 1, 1, ClickHouse){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]
     *
     * ****************************************************************************************************************/
   , BLOB(CATEGORY.BLOB, "BLOB", null, byte[].class, 1, 1, 1, MySQL, ORACLE, SQLite, Informix, GBase8S, SinoDB, HANA, Derby, KingBase){
        public Object read(Object value, Object def, Class clazz){
            if(clazz == byte[].class){

            }else if(clazz == String.class){
                value = new String((byte[])value);
            }
            return value;
        }
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
   , LONGBLOB(CATEGORY.BYTES, "LONGBLOB", null, byte[].class, 1, 1, 1, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
   , MEDIUMBLOB(CATEGORY.BYTES, "MEDIUMBLOB", null, byte[].class, 1, 1, 1, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
   , TINYBLOB(CATEGORY.BYTES, "TINYBLOB", null, byte[].class, 1, 1, 1, MySQL){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
   , MULTILINESTRING(CATEGORY.BYTES, "MULTILINESTRING", null, byte[].class, 1, 1, 1, MySQL)
   , BYTEA(CATEGORY.BYTES, "BYTEA", null, byte[].class, 1, 1, 1, PostgreSQL)
   , JSONB(CATEGORY.BYTES, "JSONB", null, byte[].class, 1, 1, 1, PostgreSQL, KingBase){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]-file
     *
     * ****************************************************************************************************************/
   , IMAGE(CATEGORY.BYTES, "IMAGE", null, byte[].class, 1, 1, 1, MSSQL)
   , BFILE(CATEGORY.BYTES, "BFILE", null, byte[].class, 1, 1, 1, ORACLE, KingBase)
    /* *****************************************************************************************************************
     *
     *                                              byte[]-geometry
     *
     * ****************************************************************************************************************/
   , POINT(CATEGORY.GEOMETRY, "POINT", null, Point.class, byte[].class, 1, 1, 1, MySQL, PostgreSQL, KingBase){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return value;
            }
            return value;
        }
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(value instanceof byte[]){
                return value;
            }
            return value;
        }
    }
   , ST_POINT(CATEGORY.GEOMETRY, "ST_POINT", null, Point.class, byte[].class, 1, 1, 1, MySQL, PostgreSQL){
        public Object read(Object value, Object def, Class clazz){
            return POINT.read(value, def, clazz);
        }
        public Object write(Object value, Object def, boolean array, boolean placeholder){
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
     *                                              待实现
     *
     * ****************************************************************************************************************/
    , JAVA_OBJECT(CATEGORY.NONE, "JAVA_OBJECT", null, String.class, 1, 1, 1, H2)
    , ENUM(CATEGORY.NONE, "ENUM", null, String.class, 1, 1, 1, MySQL)
    , INTERVAL(CATEGORY.NONE, "INTERVAL", null, null, 1, 1, 1, PostgreSQL, Informix, GBase8S, SinoDB)
    , RAW(CATEGORY.NONE, "RAW", null, byte[].class, 1, 1, 1, ORACLE)
    , ROW(CATEGORY.NONE, "ROW", null, null, 1, 1, 1, H2)
    , ROWID(CATEGORY.NONE, "ROWID", null, null, 1, 1, 1, ORACLE)
    , GUID(CATEGORY.NONE, "GUID", null, null, 1, 1, 1, XuGu)
    , SYS_REFCURSOR(CATEGORY.NONE, "SYS_REFCURSOR", null, null, 1, 1, 1, XuGu)
    , CURSOR(CATEGORY.NONE, "CURSOR", null, null, 1, 1, 1, XuGu)
    , SET(CATEGORY.NONE, "SET", null, String.class, 1, 1, 1, MySQL, SinoDB)
    , MULTISET(CATEGORY.NONE, "MULTISET", null, String.class, 1, 1, 1, SinoDB)
    , LIST(CATEGORY.NONE, "LIST", null, String.class, 1, 1, 1, SinoDB)
    , TSQUERY(CATEGORY.NONE, "TSQUERY", null, null, 1, 1, 1, PostgreSQL, KingBase)
    , TSVECTOR(CATEGORY.NONE, "TSVECTOR", null, null, 1, 1, 1, PostgreSQL, KingBase)
    , MACADDR(CATEGORY.NONE, "MACADDR", null, null, 1, 1, 1, PostgreSQL, KingBase)
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

    , AGG_STATE(CATEGORY.NONE, "AGG_STATE", null, null, 1, 1, 1, Doris)
    , LowCardinality(CATEGORY.NONE, "LowCardinality", null, null, 1, 1, 1, ClickHouse)
    , SimpleAggregateFunction(CATEGORY.NONE, "SimpleAggregateFunction", null, null, 1, 1, 1, ClickHouse)
    , TUPLE(CATEGORY.NONE, "TUPLE", null, null, 1, 1, 1, ClickHouse)
    , IPV4(CATEGORY.NONE, "IPV4", null, null, 1, 1, 1, ClickHouse)
    , IPV6(CATEGORY.NONE, "IPV6", null, null, 1, 1, 1, ClickHouse)

    ;

    private final List<DatabaseType> dbs = new ArrayList<>();
    private final CATEGORY category;
    private final TypeMetadata origin;
    private final String name;
    private Class transfer                  ; //中间转换类型 转换成其他格式前先转换成transfer类型
    private final Class compatible          ; //从数据库中读写数据的类型
    private final int ignoreLength;
    private final int ignorePrecision;
    private final int ignoreScale;
    private boolean array;
    private TypeMetadata.Config config;
    StandardTypeMetadata(CATEGORY category, String name, TypeMetadata origin, Class transfer, Class compatible, int ignoreLength, int ignorePrecision, int ignoreScale, DatabaseType ... dbs){
        this.category = category;
        if(null != origin) {
            this.origin = origin;
        }else{
            this.origin = this;
        }
        if(null != dbs){
            for(DatabaseType db:dbs){
                this.dbs.add(db);
            }
        }
        this.name = name;
        this.transfer = transfer;
        this.compatible = compatible;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    StandardTypeMetadata(CATEGORY category, String name, TypeMetadata origin, Class compatible, int ignoreLength, int ignorePrecision, int ignoreScale, DatabaseType ... dbs){
        this.category = category;
        if(null != origin) {
            this.origin = origin;
        }else{
            this.origin = this;
        }
        if(null != dbs){
            for(DatabaseType db:dbs){
                this.dbs.add(db);
            }
        }
        this.name = name;
        this.compatible = compatible;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    public TypeMetadata getOrigin(){
        return origin;
    }
    @Override
    public CATEGORY getCategory(){
        return category;
    }
    @Override
    public CATEGORY_GROUP getCategoryGroup(){
        if(null != category) {
            return category.group();
        }
        return CATEGORY_GROUP.NONE;
    }
    @Override
    public Object convert(Object value, Object def){
        return convert(value, null, def);
    }

    @Override
    public Object convert(Object value, Class target, boolean array){
        Object def = null;
        return convert(value, target, array, def);
    }

    @Override
    public Object convert(Object value, Class target, boolean array, Object def) {
        if(null == target){
            target = compatible;
        }
        if(null != value){
            if(value.getClass() == target){
                return value;
            }
            if(null != transfer) {
                value = ConvertAdapter.convert(value, transfer, array, def);
            }
            value = ConvertAdapter.convert(value, target, array, def);
        }
        return value;
    }

    @Override
    public Object convert(Object value, Object obj, Field field) {
        return convert(value, field.getType());
    }

    @Override
    public Object read(Object value, Object def, Class clazz, boolean array) {
        if(null == clazz){
            clazz = transfer;
        }
        if(null == clazz){
            clazz = compatible;
        }
        value = ConvertAdapter.convert(value, clazz, array, def);
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
    public Object write(Object value, Object def, boolean array, boolean placeholder) {
        if(null != value){
            if(value.getClass() != compatible) {
                if (null != transfer) {
                    value = ConvertAdapter.convert(value, transfer, array, def);
                }
                value = ConvertAdapter.convert(value, compatible, array, def);
            }
            if(null != value && compatible == String.class && !placeholder){
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
    public boolean support() {
        return true;
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
    public TypeMetadata.Config config() {
        if(null == config){
            config = new TypeMetadata.Config();
            config.setIgnoreLength(ignoreLength).setIgnorePrecision(ignorePrecision).setIgnoreScale(ignoreScale);
        }
        return config;
    }
}
