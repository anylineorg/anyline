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


package org.anyline.data.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.geometry.*;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.util.Base64Util;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Time;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import org.anyline.metadata.type.TypeMetadata.CATEGORY;

import static org.anyline.metadata.type.DatabaseType.*;


public enum StandardColumnType implements TypeMetadata {

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
    CHAR(CATEGORY.STRING, "CHAR", new DatabaseType[]{MySQL, PostgreSQL, Informix, HANA, Derby}, String.class, false, true){
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
   , NCHAR(CATEGORY.STRING, "NCHAR", new DatabaseType[]{ORACLE, MSSQL, Informix}, String.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , CLOB(CATEGORY.STRING, "CLOB", new DatabaseType[]{ORACLE, Informix, Derby, KingBase}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , NCLOB(CATEGORY.STRING, "NCLOB", new DatabaseType[]{ORACLE, HANA}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , NVARCHAR(CATEGORY.STRING, "NVARCHAR", new DatabaseType[]{MSSQL, Informix, HANA, KingBase}, String.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , NVARCHAR2(CATEGORY.STRING, "NVARCHAR2", new DatabaseType[]{ORACLE}, String.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , LONGTEXT(CATEGORY.STRING, "LONGTEXT", new DatabaseType[]{MySQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , MEDIUMTEXT(CATEGORY.STRING, "MEDIUMTEXT", new DatabaseType[]{MySQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , TEXT(CATEGORY.STRING, "TEXT", new DatabaseType[]{MySQL, PostgreSQL, SQLite, Informix, IoTDB, KingBase}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , NTEXT(CATEGORY.STRING, "NTEXT", new DatabaseType[]{MSSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , TINYTEXT(CATEGORY.STRING, "TINYTEXT", new DatabaseType[]{MySQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * Informix(长度不超过 255 )
     */
   , VARCHAR(CATEGORY.STRING, "VARCHAR", new DatabaseType[]{MySQL, PostgreSQL, ORACLE, Informix, HANA, Derby, KingBase}, String.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , LVARCHAR(CATEGORY.STRING, "LVARCHAR", new DatabaseType[]{Informix}, String.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , VARCHAR2(CATEGORY.STRING, "VARCHAR2", new DatabaseType[]{ORACLE}, String.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , SYSNAME(CATEGORY.STRING, "SYSNAME", new DatabaseType[]{MSSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
   , UUID(CATEGORY.STRING, "UUID", new DatabaseType[]{PostgreSQL, KingBase}, String.class, true, true){
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
   , UNIQUEIDENTIFIER(CATEGORY.STRING, "UNIQUEIDENTIFIER", new DatabaseType[]{MSSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql(byte[])
     */
   , BINARY(CATEGORY.BYTES, "BINARY", new DatabaseType[]{MySQL, MSSQL, HANA, ElasticSearch}, byte[].class, false, true)
   , VARBINARY(CATEGORY.BYTES, "VARBINARY", new DatabaseType[]{MySQL, MSSQL, HANA}, byte[].class, false, true)


    /* *****************************************************************************************************************
     *
     *                                              String-format
     *
     * ****************************************************************************************************************/
   , JSON(CATEGORY.STRING, "JSON", new DatabaseType[]{MySQL, PostgreSQL, KingBase}, String.class, true, true){
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
   , XML(CATEGORY.STRING, "XML", new DatabaseType[]{MSSQL, KingBase}, String.class, true, true){}
    /* *****************************************************************************************************************
     *
     *                                              number-int/long
     *
     * ****************************************************************************************************************/
    , BIT(CATEGORY.BOOLEAN, "BIT", new DatabaseType[]{MySQL, MSSQL}, Boolean.class, true, true)
    , BITMAP(CATEGORY.BYTES, "BITMAP", new DatabaseType[]{Doris}, Byte.class, true, true)
    , VARBIT(CATEGORY.BOOLEAN, "VARBIT", new DatabaseType[]{PostgreSQL}, Byte[].class, true, true)
    , SHORT(CATEGORY.INT, "SHORT", new DatabaseType[]{}, Short.class, true, true)

    , BYTE(CATEGORY.INT, "BYTE", new DatabaseType[]{}, Short.class, true, true)
   , INT(CATEGORY.INT, "INT", new DatabaseType[]{MySQL, MSSQL, Informix, Derby}, Integer.class, true, true)
   , INT32(CATEGORY.INT, "INT32", new DatabaseType[]{IoTDB}, Integer.class, true, true)
   , INT64(CATEGORY.INT, "INT64", new DatabaseType[]{IoTDB}, Integer.class, true, true)
   , INFORMIX_INTEGER(CATEGORY.INT, "INTEGER", new DatabaseType[]{Informix}, Integer.class, true, true)
   , LONG(CATEGORY.INT, "LONG", new DatabaseType[]{ORACLE, ElasticSearch}, String.class, true, true){}
   , SERIAL(CATEGORY.INT, "SERIAL", new DatabaseType[]{PostgreSQL, Informix}, Integer.class, true, true)
   , SERIAL2(CATEGORY.INT, "SERIAL2", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
   , SERIAL4(CATEGORY.INT, "SERIAL4", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
   , SERIAL8(CATEGORY.INT, "SERIAL8", new DatabaseType[]{PostgreSQL, Informix}, Long.class, true, true)
   , SMALLSERIAL(CATEGORY.INT, "SERIAL2", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
   , BIGSERIAL(CATEGORY.INT, "SERIAL8", new DatabaseType[]{PostgreSQL, Informix}, Long.class, true, true)
   , INT2(CATEGORY.INT, "INT2", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
   , INT4(CATEGORY.INT, "INT4", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
   , INT8(CATEGORY.INT, "INT8", new DatabaseType[]{PostgreSQL, Informix}, Long.class, true, true)
   , BIGINT(CATEGORY.INT, "BIGINT", new DatabaseType[]{MySQL, Informix, HANA, Derby, KingBase}, Long.class, true, true)
   , LARGEINT(CATEGORY.INT, "LARGEINT", new DatabaseType[]{Doris}, Long.class, true, true)
   , MEDIUMINT(CATEGORY.INT, "MEDIUMINT", new DatabaseType[]{MySQL}, Integer.class, true, true)
   , INTEGER(CATEGORY.INT, "INTEGER", new DatabaseType[]{MySQL, SQLite, HANA, ElasticSearch, Derby, KingBase}, Integer.class, true, true)
   , SMALLINT(CATEGORY.INT, "SMALLINT", new DatabaseType[]{MySQL, Informix, HANA, Derby, KingBase}, Integer.class, true, true)
   , TINYINT(CATEGORY.INT, "TINYINT", new DatabaseType[]{MySQL, HANA, KingBase}, Integer.class, true, true)
   , BOOLEAN(CATEGORY.BOOLEAN, "BOOLEAN", new DatabaseType[]{PostgreSQL, Informix, HANA, ElasticSearch, KingBase}, Boolean.class, true, true)
   , BOOL(CATEGORY.BOOLEAN, "BOOLEAN", new DatabaseType[]{PostgreSQL}, Boolean.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BOOLEAN.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              number-double/float
     *
     * ****************************************************************************************************************/
   , DECIMAL(CATEGORY.FLOAT, "DECIMAL", new DatabaseType[]{MySQL, PostgreSQL, ORACLE, Informix, HANA, Derby}, BigDecimal.class, false, false){
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
   , SMALLDECIMAL(CATEGORY.FLOAT, "SMALLDECIMAL", new DatabaseType[]{HANA}, BigDecimal.class, false, false){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DECIMAL.write(value, def, placeholder);
        }
    }
   , DOUBLE(CATEGORY.FLOAT, "DOUBLE", new DatabaseType[]{MySQL, Informix, HANA, IoTDB, ElasticSearch, Derby}, Double.class, false, false){
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
    /**
     * mysql(p, s)
     * pg:
     * informix(p)
     * oracle(p)
     * mysql, oracle(BigDecimal)
     */
   , FLOAT_MySQL(CATEGORY.FLOAT, "FLOAT", new DatabaseType[]{MySQL}, Float.class, false, false){
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
   , FLOAT_INFORMIX(CATEGORY.FLOAT, "FLOAT", new DatabaseType[]{Informix}, Float.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT_ORACLE(CATEGORY.FLOAT, "FLOAT", new DatabaseType[]{ORACLE}, Float.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , SMALLFLOAT(CATEGORY.FLOAT, "SMALLFLOAT", new DatabaseType[]{Informix}, Float.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT_MSSQL(CATEGORY.FLOAT, "FLOAT", new DatabaseType[]{MSSQL}, Float.class, false, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT4(CATEGORY.FLOAT, "FLOAT4", new DatabaseType[]{PostgreSQL}, Float.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT(CATEGORY.FLOAT, "FLOAT", new DatabaseType[]{IoTDB, ElasticSearch, Derby}, Float.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
   , FLOAT8(CATEGORY.FLOAT, "FLOAT8", new DatabaseType[]{PostgreSQL}, Double.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
   , BINARY_DOUBLE(CATEGORY.FLOAT, "BINARY_DOUBLE", new DatabaseType[]{ORACLE}, Double.class, false, false){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
   , BINARY_FLOAT(CATEGORY.FLOAT, "BINARY_FLOAT", new DatabaseType[]{ORACLE}, Float.class, false, false){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
   , MONEY(CATEGORY.FLOAT, "MONEY", new DatabaseType[]{PostgreSQL, Informix, KingBase}, BigDecimal.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , SMALLMONEY(CATEGORY.FLOAT, "SMALLMONEY", new DatabaseType[]{MSSQL}, BigDecimal.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , NUMERIC(CATEGORY.FLOAT, "NUMERIC", new DatabaseType[]{MySQL, SQLite, Informix, KingBase}, BigDecimal.class, false, false){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , NUMBER(CATEGORY.FLOAT, "NUMBER", new DatabaseType[]{ORACLE}, BigDecimal.class, false, false){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
   , REAL(CATEGORY.FLOAT, "REAL", new DatabaseType[]{MySQL, SQLite, Informix, HANA, Derby, KingBase}, Double.class, false, false){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return FLOAT_MySQL.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              date
     *                               write 需要根据数据库类型 由内置函数转换
     *
     * ****************************************************************************************************************/
   , DATE(CATEGORY.DATE, "DATE", new DatabaseType[]{MySQL, PostgreSQL, Informix, HANA, Derby}, java.sql.Date.class, true, true){
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
   , DATETIME(CATEGORY.DATE, "DATETIME", new DatabaseType[]{MySQL, Informix}, LocalDateTime.class, true, true){
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
   , DATETIME2(CATEGORY.DATE, "DATETIME2", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * MSSQL<br/>
     * 2020-01-01 15:10:10.0000011
     */
   , DATETIMEOFFSET(CATEGORY.DATE, "DATETIMEOFFSET", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , SMALLDATETIME(CATEGORY.DATE, "SMALLDATETIME", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , SQL_DATETIMEOFFSET(CATEGORY.DATE, "SQL_DATETIMEOFFSET", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , SECONDDATE(CATEGORY.DATE, "SECONDDATE", new DatabaseType[]{HANA}, java.util.Date.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
   , TIME(CATEGORY.DATE, "TIME", new DatabaseType[]{MySQL, PostgreSQL, HANA, Derby}, java.sql.Time.class, true, true){
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
    , TIME_WITH_ZONE(CATEGORY.DATE, "TIME WITH TIME ZONE", new DatabaseType[]{DM}, java.sql.Time.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return TIME.write(value, def, placeholder);
        }
    }
   , TIMEZ(CATEGORY.DATE, "TIMEZ", new DatabaseType[]{PostgreSQL}, java.sql.Time.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return TIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMP(CATEGORY.DATE, "TIMESTAMP", new DatabaseType[]{MySQL, PostgreSQL, ORACLE, HANA, Derby}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMP_WITH_ZONE(CATEGORY.DATE, "TIMESTAMP WITH TIME ZONE", new DatabaseType[]{PostgreSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMP_WITH_LOCAL_ZONE(CATEGORY.DATE, "TIMESTAMP WITH LOCAL TIME ZONE", new DatabaseType[]{PostgreSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , TIMESTAMPTZ(CATEGORY.DATE, "TIMESTAMPTZ", new DatabaseType[]{PostgreSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
   , YEAR(CATEGORY.DATE, "YEAR", new DatabaseType[]{MySQL}, java.sql.Date.class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return DATE.write(value, def, placeholder);
        }
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]
     *
     * ****************************************************************************************************************/
    /**
     * mysql(byte[]), oracle, sqlite
     */
   , BLOB(CATEGORY.BYTES, "BLOB", new DatabaseType[]{MySQL, ORACLE, SQLite, Informix, HANA, Derby, KingBase}, byte[].class, true, true){
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
   , LONGBLOB(CATEGORY.BYTES, "LONGBLOB", new DatabaseType[]{MySQL}, byte[].class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
   , MEDIUMBLOB(CATEGORY.BYTES, "MEDIUMBLOB", new DatabaseType[]{MySQL}, byte[].class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
   , TINYBLOB(CATEGORY.BYTES, "TINYBLOB", new DatabaseType[]{MySQL}, byte[].class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
   , MULTILINESTRING(CATEGORY.BYTES, "MULTILINESTRING", new DatabaseType[]{MySQL}, byte[].class, true, true)
   , BYTEA(CATEGORY.BYTES, "BYTEA", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)
   , JSONB(CATEGORY.BYTES, "JSONB", new DatabaseType[]{PostgreSQL, KingBase}, byte[].class, true, true){
        public Object write(Object value, Object def, boolean array, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              byte[]-file
     *
     * ****************************************************************************************************************/
   , IMAGE(CATEGORY.BYTES, "IMAGE", new DatabaseType[]{MSSQL}, byte[].class, true, true)
   , BFILE(CATEGORY.BYTES, "BFILE", new DatabaseType[]{ORACLE, KingBase}, byte[].class, true, true)
    /* *****************************************************************************************************************
     *
     *                                              byte[]-geometry
     *
     * ****************************************************************************************************************/
   , POINT(CATEGORY.GEOMETRY, "POINT", new DatabaseType[]{MySQL, PostgreSQL, KingBase}, Point.class, byte[].class, true, true){
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
   , ST_POINT(CATEGORY.GEOMETRY, "ST_POINT", new DatabaseType[]{MySQL, PostgreSQL}, Point.class, byte[].class, true, true){
        public Object read(Object value, Object def, Class clazz){
            return POINT.read(value, def, clazz);
        }
        public Object write(Object value, Object def, boolean array, boolean placeholder){
            return POINT.write(value, def, placeholder);
        }
    }
   , GEOGRAPHY_POINT(CATEGORY.GEOMETRY, "GEOGRAPHY_POINT", new DatabaseType[]{VoltDB}, Point.class, byte[].class, true, true)
   , MULTIPOLYGON(CATEGORY.GEOMETRY, "MULTIPOLYGON", new DatabaseType[]{MySQL}, MultiPolygon.class, byte[].class, true, true)
   , MULTIPOINT(CATEGORY.GEOMETRY, "MULTIPOINT", new DatabaseType[]{MySQL}, MultiPoint.class, byte[].class, true, true)
   , POLYGON(CATEGORY.GEOMETRY, "POLYGON", new DatabaseType[]{MySQL, PostgreSQL, KingBase}, Polygon.class, byte[].class, true, true)
   , GEOMETRY(CATEGORY.GEOMETRY, "GEOMETRY", new DatabaseType[]{MySQL}, byte[].class, true, true)
   , ST_GEOMETRY(CATEGORY.GEOMETRY, "ST_GEOMETRY", new DatabaseType[]{HANA}, byte[].class, true, true)
   , GEOMETRYCOLLECTION(CATEGORY.GEOMETRY, "GEOMETRYCOLLECTION", new DatabaseType[]{MySQL}, byte[].class, true, true)
   , HIERARCHYID(CATEGORY.GEOMETRY, "HIERARCHYID", new DatabaseType[]{MSSQL}, byte[].class, true, true)
   , LINE(CATEGORY.GEOMETRY, "LINE", new DatabaseType[]{PostgreSQL, KingBase}, LineString.class, byte[].class, true, true)
   , LINESTRING(CATEGORY.GEOMETRY, "LINESTRING", new DatabaseType[]{MySQL}, LineString.class, byte[].class, true, true)
   , PATH(CATEGORY.GEOMETRY, "PATH", new DatabaseType[]{PostgreSQL, KingBase}, LineString.class, true, true)
   , LSEG(CATEGORY.GEOMETRY, "LSEG", new DatabaseType[]{PostgreSQL, KingBase}, byte[].class, true, true)
   , GEOGRAPHY(CATEGORY.GEOMETRY, "GEOGRAPHY", new DatabaseType[]{MSSQL, PostgreSQL}, byte[].class, true, true)
   , BOX(CATEGORY.GEOMETRY, "BOX", new DatabaseType[]{PostgreSQL, KingBase}, byte[].class, true, true)
   , CIDR(CATEGORY.GEOMETRY, "CIDR", new DatabaseType[]{PostgreSQL, KingBase}, byte[].class, true, true)
   , CIRCLE(CATEGORY.GEOMETRY, "CIRCLE", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)
   , INET(CATEGORY.GEOMETRY, "INET", new DatabaseType[]{PostgreSQL, KingBase}, byte[].class, true, true)


    /* *****************************************************************************************************************
     *
     *                                              待实现
     *
     * ****************************************************************************************************************/
   , ENUM(CATEGORY.NONE, "ENUM", new DatabaseType[]{MySQL}, String.class, true, true)
   , INTERVAL(CATEGORY.NONE, "INTERVAL", new DatabaseType[]{PostgreSQL, Informix}, null, true, true)
   , RAW(CATEGORY.NONE, "RAW", new DatabaseType[]{ORACLE}, byte[].class, true, true)
   , ROWID(CATEGORY.NONE, "ROWID", new DatabaseType[]{ORACLE}, null, true, true)
   , SET(CATEGORY.NONE, "SET", new DatabaseType[]{MySQL}, String.class, true, true)
   , TSQUERY(CATEGORY.NONE, "TSQUERY", new DatabaseType[]{PostgreSQL, KingBase}, null, true, true)
   , TSVECTOR(CATEGORY.NONE, "TSVECTOR", new DatabaseType[]{PostgreSQL, KingBase}, null, true, true)
   , MACADDR(CATEGORY.NONE, "MACADDR", new DatabaseType[]{PostgreSQL, KingBase}, null, true, true)
   , PG_SNAPSHOT(CATEGORY.NONE, "PG_SNAPSHOT", new DatabaseType[]{PostgreSQL}, null, true, true)
    /**
     * pg
     * 弃用 换成pg_snapshot
     */
   , TXID_SNAPSHOT(CATEGORY.NONE, "TXID_SNAPSHOT", new DatabaseType[]{PostgreSQL, KingBase}, null, true, true)
   , UROWID(CATEGORY.NONE, "UROWID", new DatabaseType[]{ORACLE}, null, true, true)
   , SQL_VARIANT(CATEGORY.NONE, "SQL_VARIANT", new DatabaseType[]{MSSQL}, null, true, true)

   , KEYWORD(CATEGORY.NONE, "KEYWORD", new DatabaseType[]{ElasticSearch}, null, true, true)

   , OBJECT(CATEGORY.NONE, "OBJECT", new DatabaseType[]{ElasticSearch}, null, true, true)

    ;
    private final DatabaseType[] dbs;
    private final CATEGORY category;
    private final String name;
    private Class transfer                  ; //中间转换类型 转换成其他格式前先转换成transfer类型
    private final Class compatible          ; //从数据库中读写数据的类型
    private final Boolean ignorePrecision;
    private final Boolean ignoreScale;
    private boolean array;
    StandardColumnType(CATEGORY category, String name, DatabaseType[] dbs, Class transfer, Class compatible, Boolean ignorePrecision, Boolean ignoreScale){
        this.category = category;
        this.name = name;
        this.dbs = dbs;
        this.transfer = transfer;
        this.compatible = compatible;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    StandardColumnType(CATEGORY category, String name, DatabaseType[] dbs, Class compatible, Boolean ignorePrecision, Boolean ignoreScale){
        this.category = category;
        this.name = name;
        this.dbs = dbs;
        this.compatible = compatible;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    @Override
    public CATEGORY getCategory(){
        return category;
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
    public DatabaseType[] dbs() {
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
}
