package org.anyline.data.metadata;

import com.fasterxml.jackson.databind.JsonNode;
import org.anyline.adapter.KeyAdapter;
import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.geometry.*;
import org.anyline.metadata.type.ColumnType;
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

import static org.anyline.metadata.type.DatabaseType.*;


public enum StandardColumnType implements ColumnType {

/*
* -- "public"."chk_column" definition

-- Drop table

-- DROP TABLE "public"."chk_column";
,kingbase
CREATE TABLE "public"."chk_column" (
	"c2" aclitem NULL,
	"c3" bfile NULL,
	"c4" bigint NULL,
	"c5" bigint AUTO_INCREMENT,
	"c6" binary_integer NULL,
	"c7" bit(1) NULL,
	"c8" bit varying NULL,
	"c9" blob NULL,
	"c10" boolean NULL,
	"c11" boolean NULL,
	"c12" box NULL,
	"c13" bpchar NULL,
	"c14" bpcharbyte NULL,
	"c15" character(1 char) NULL,
	"c16" character(1 char) NULL,
	"c17" character(1 char) NULL,
	"c19" cidr NULL,
	"c20" clob NULL,
	"c21" varchar NULL,
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
	"c31" numeric NULL,
	"c32" double precision NULL,
	"c33" double precision NULL,
	"c34" dsinterval NULL,
	"c35" real NULL,
	"c36" double precision NULL,
	"c37" gtsvector NULL,
	"c38" inet NULL,
	"c39" smallint NULL,
	"c40" integer NULL,
	"c41" int4range NULL,
	"c42" bigint NULL,
	"c43" int8range NULL,
	"c44" integer NULL,
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
	"c60" interval second(2,6) NULL,
	"c61" interval year(2) NULL,
	"c62" interval year(2) to month NULL,
	"c63" json NULL,
	"c64" jsonb NULL,
	"c65" jsonpath NULL,
	"c66" line NULL,
	"c67" lseg NULL,
	"c68" macaddr NULL,
	"c69" macaddr8 NULL,
	"c70" money NULL,
	"c71" mysql_date NULL,
	"c72" mysql_time NULL,
	"c73" name NULL COLLATE "c",
	"c74" character(1 char) NULL,
	"c75" varchar NULL,
	"c76" varchar NULL,
	"c77" "natural" NULL,
	"c78" naturaln NULL,
	"c79" character(1 char) NULL,
	"c80" varchar NULL,
	"c81" nclob NULL,
	"c82" numeric NULL,
	"c83" numeric NULL,
	"c84" numrange NULL,
	"c85" varchar NULL,
	"c86" varchar NULL,
	"c87" oid NULL,
	"c88" ora_date NULL,
	"c89" path NULL,
	"c90" point NULL,
	"c91" polygon NULL,
	"c92" positive NULL,
	"c93" positiven NULL,
	"c94" real NULL,
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
	"c106" integer AUTO_INCREMENT,
	"c107" smallint AUTO_INCREMENT,
	"c108" integer AUTO_INCREMENT,
	"c109" bigint AUTO_INCREMENT,
	"c110" signtype NULL,
	"c111" simple_double NULL,
	"c112" simple_integer NULL,
	"c113" simple_float NULL,
	"c114" smallint NULL,
	"c115" smallint AUTO_INCREMENT,
	"c116" string NULL,
	"c117" text NULL,
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
	"c132" tinyint NULL,
	"c133" integer NULL,
	"c134" tsquery NULL,
	"c135" tsrange NULL,
	"c136" tstzrange NULL,
	"c137" tsvector NULL,
	"c138" txid_snapshot NULL,
	"c139" uuid NULL,
	"c140" bit varying NULL,
	"c141" varchar NULL,
	"c142" varcharbyte NULL,
	"c143" xid NULL,
	"c144" xml NULL,
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
    /**
     * mysql,pg
     */
    CHAR("CHAR", new DatabaseType[]{MYSQL, PostgreSQL, Informix, HANA}, String.class, false, true){
        
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
    ,NCHAR("NCHAR", new DatabaseType[]{ORACLE, MSSQL, Informix},String.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,CLOB("CLOB", new DatabaseType[]{ORACLE, Informix}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,NCLOB("NCLOB", new DatabaseType[]{ORACLE, HANA}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,NVARCHAR("NVARCHAR", new DatabaseType[]{MSSQL, Informix, HANA}, String.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,NVARCHAR2("NVARCHAR2", new DatabaseType[]{ORACLE}, String.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,LONGTEXT("LONGTEXT", new DatabaseType[]{MYSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,MEDIUMTEXT("MEDIUMTEXT", new DatabaseType[]{MYSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql,pg,sqlite
     */
    ,TEXT("TEXT", new DatabaseType[]{MYSQL, PostgreSQL, SQLite, Informix}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,NTEXT("NTEXT", new DatabaseType[]{MSSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,TINYTEXT("TINYTEXT", new DatabaseType[]{MYSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql,pg,oracle,Informix(长度不超过 255 )
     */
    ,VARCHAR("VARCHAR", new DatabaseType[]{MYSQL, PostgreSQL, ORACLE, Informix, HANA}, String.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    ,LVARCHAR("LVARCHAR", new DatabaseType[]{Informix}, String.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,VARCHAR2("VARCHAR2", new DatabaseType[]{ORACLE}, String.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SYSNAME("SYSNAME", new DatabaseType[]{MSSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,UUID("UUID", new DatabaseType[]{PostgreSQL}, String.class, true, true){
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
    ,UNIQUEIDENTIFIER("UNIQUEIDENTIFIER", new DatabaseType[]{MSSQL}, String.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
    }
    /**
     * mysql(byte[]),mssql
     */
    ,BINARY("BINARY", new DatabaseType[]{MYSQL, MSSQL, HANA}, byte[].class, false, true)
    /**
     * mysql(byte[]),mssql
     */
    ,VARBINARY("VARBINARY", new DatabaseType[]{MYSQL, MSSQL, HANA}, byte[].class, false, true)


    /* *****************************************************************************************************************
     *
     *                                              String-format
     *
     * ****************************************************************************************************************/

    /**
     * mysql,pg
     */
    ,JSON("JSON", new DatabaseType[]{MYSQL, PostgreSQL}, String.class, true, true){

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

    /**
     * mssql
     */
    ,XML("XML", new DatabaseType[]{MSSQL}, String.class, true, true){}
    /* *****************************************************************************************************************
     *
     *                                              number-int/long
     *
     * ****************************************************************************************************************/
    /**
     * mysql(Boolean),pg(Boolean),mssql
     */
    ,BIT("BIT", new DatabaseType[]{MYSQL, MSSQL}, Boolean.class, true, true)
    /**
     * pg
     */
    ,VARBIT("VARBIT", new DatabaseType[]{PostgreSQL}, Byte[].class, true, true)
    ,SHORT("SHORT", new DatabaseType[]{}, Short.class, true, true)

    /**
     * mysql,mssql,kingbase
     */
    ,INT("INT", new DatabaseType[]{MYSQL, MSSQL, Informix}, Integer.class, true, true)
    /**
     * Informix
     */
    ,INFORMIX_INTEGER("INTEGER", new DatabaseType[]{Informix}, Integer.class, true, true)
    /**
     * oracle
     */
    ,LONG("LONG", new DatabaseType[]{ORACLE}, String.class, true, true){}
    /**
     * pg, informix
     */
    ,SERIAL("SERIAL", new DatabaseType[]{PostgreSQL, Informix}, Integer.class, true, true)
    /**
     * pg
     */
    ,SERIAL2("SERIAL2", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
    /**
     * pg
     */
    ,SERIAL4("SERIAL4", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
    /**
     * pg
     */
    ,SERIAL8("SERIAL8", new DatabaseType[]{PostgreSQL, Informix}, Long.class, true, true)
    /**
     * pg
     */
    ,SMALLSERIAL("SERIAL2", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
    /**
     * pg
     */
    ,BIGSERIAL("SERIAL8", new DatabaseType[]{PostgreSQL, Informix}, Long.class, true, true)
    /**
     * pg
     */
    ,INT2("INT2", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
    /**
     * pg
     */
    ,INT4("INT4", new DatabaseType[]{PostgreSQL}, Integer.class, true, true)
    /**
     * pg
     */
    ,INT8("INT8", new DatabaseType[]{PostgreSQL, Informix}, Long.class, true, true)
    /**
     * mysql
     */
    ,BIGINT("BIGINT", new DatabaseType[]{MYSQL, Informix, HANA}, Long.class, true, true)
    /**
     * mysql
     */
    ,MEDIUMINT("MEDIUMINT", new DatabaseType[]{MYSQL}, Integer.class, true, true)
    /**
     * mysql,sqlite
     */
    ,INTEGER("INTEGER", new DatabaseType[]{MYSQL, SQLite, HANA}, Integer.class, true, true)
    /**
     * mysql
     */
    ,SMALLINT("SMALLINT", new DatabaseType[]{MYSQL, Informix, HANA}, Integer.class, true, true)
    /**
     * mysql
     */
    ,TINYINT("TINYINT", new DatabaseType[]{MYSQL, HANA}, Integer.class, true, true){
    }
    /**
     * pg
     */
    ,BOOLEAN("BOOLEAN", new DatabaseType[]{PostgreSQL, Informix, HANA}, Boolean.class, true, true)
    /**
     * pg
     */
    ,BOOL("BOOLEAN", new DatabaseType[]{PostgreSQL}, Boolean.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return BOOLEAN.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              number-double/float
     *
     * ****************************************************************************************************************/

    /**
     * mysql,pg,oracle
     */
    ,DECIMAL("DECIMAL", new DatabaseType[]{MYSQL, PostgreSQL, ORACLE, Informix, HANA}, BigDecimal.class, false, false){
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
    ,SMALLDECIMAL("SMALLDECIMAL", new DatabaseType[]{HANA}, BigDecimal.class, false, false){
        public Object write(Object value, Object def, boolean placeholder){
            return DECIMAL.write(value, def, placeholder);
        }
    }
    //
    /**
     * mysql
     */
    ,DOUBLE("DOUBLE", new DatabaseType[]{MYSQL, Informix, HANA}, Double.class, false, false){
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
     * mysql(p,s)
     * pg:
     * informix(p)
     * oracle(p)
     * mysql,  ,oracle(BigDecimal)
     */
    ,FLOAT_MYSQL("FLOAT", new DatabaseType[]{MYSQL}, Float.class, false, false){
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
    ,FLOAT_INFORMIX("FLOAT", new DatabaseType[]{Informix}, Float.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return FLOAT_MYSQL.write(value, def, placeholder);}
    }
    ,FLOAT_ORACLE("FLOAT", new DatabaseType[]{ORACLE}, Float.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return FLOAT_MYSQL.write(value, def, placeholder);}
    }
    ,SMALLFLOAT("SMALLFLOAT", new DatabaseType[]{Informix}, Float.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return FLOAT_MYSQL.write(value, def, placeholder);}
    }
    /**
     * ms
     */
    ,FLOAT_MSSQL("FLOAT", new DatabaseType[]{MSSQL}, Float.class, false, true){
        public Object write(Object value, Object def, boolean placeholder){return FLOAT_MYSQL.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,FLOAT4("FLOAT4", new DatabaseType[]{PostgreSQL}, Float.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return FLOAT_MYSQL.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,FLOAT8("FLOAT8", new DatabaseType[]{PostgreSQL}, Double.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,BINARY_DOUBLE("BINARY_DOUBLE", new DatabaseType[]{ORACLE}, Double.class, false, false){
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,BINARY_FLOAT("BINARY_FLOAT", new DatabaseType[]{ORACLE}, Float.class, false, false){
        public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
    }
    /**
     * pg
     */
    ,MONEY("MONEY", new DatabaseType[]{PostgreSQL, Informix}, BigDecimal.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SMALLMONEY("SMALLMONEY", new DatabaseType[]{MSSQL}, BigDecimal.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * mysql,sqlite
     */
    ,NUMERIC("NUMERIC", new DatabaseType[]{MYSQL, SQLite, Informix}, BigDecimal.class, false, false){
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * oracle
     */
    ,NUMBER("NUMBER", new DatabaseType[]{ORACLE}, BigDecimal.class, false, false){
        public Object write(Object value, Object def, boolean placeholder){return DECIMAL.write(value, def, placeholder);}
    }
    /**
     * mysql(Double),sqlite
     */
    ,REAL("REAL", new DatabaseType[]{MYSQL, SQLite, Informix, HANA}, Double.class, false, false){
        public Object write(Object value, Object def, boolean placeholder){return FLOAT_MYSQL.write(value, def, placeholder);}
    }
    /* *****************************************************************************************************************
     *
     *                                              date
     *                               write 需要根据数据库类型 由内置函数转换
     *
     * ****************************************************************************************************************/
    /**
     * mysql,pg
     */
    ,DATE("DATE", new DatabaseType[]{MYSQL, PostgreSQL, Informix, HANA}, java.sql.Date.class, false, false){
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
     * mysql(LocalDateTime)
     */
    ,DATETIME("DATETIME", new DatabaseType[]{MYSQL, Informix}, LocalDateTime.class, true, true){
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
    ,DATETIME2("DATETIME2", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mssql<br/>
     * 2020-01-01 15:10:10.0000011
     */
    ,DATETIMEOFFSET("DATETIMEOFFSET", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SMALLDATETIME("SMALLDATETIME", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SQL_DATETIMEOFFSET("SQL_DATETIMEOFFSET", new DatabaseType[]{MSSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mssql
     */
    ,SECONDDATE("SECONDDATE", new DatabaseType[]{HANA}, java.util.Date.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return DATETIME.write(value, def, placeholder);}
    }
    /**
     * mysql,pg
     */
    ,TIME("TIME", new DatabaseType[]{MYSQL, PostgreSQL, HANA}, java.sql.Time.class, true, true){
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
    ,TIMEZ("TIMEZ", new DatabaseType[]{PostgreSQL}, java.sql.Time.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){
            return TIME.write(value, def, placeholder);
        }
    }
    /**
     * mysql,pg,oracle
     */
    ,TIMESTAMP("TIMESTAMP", new DatabaseType[]{MYSQL, PostgreSQL, ORACLE, HANA}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
    /**
     * pg
     */
    ,TIMESTAMP_ZONE("TIMESTAMP", new DatabaseType[]{PostgreSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
    /**
     * pg
     */
    ,TIMESTAMP_LOCAL_ZONE("TIMESTAMP", new DatabaseType[]{PostgreSQL}, java.sql.Timestamp.class, true, true){
        public Object write(Object value, Object def, boolean placeholder){
            return DATETIME.write(value, def, placeholder);
        }
    }
    /**
     * mysql
     */
    ,YEAR("YEAR", new DatabaseType[]{MYSQL}, java.sql.Date.class, true, true){
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
     * mysql(byte[]),  ,oracle,sqlite
     */
    ,BLOB("BLOB", new DatabaseType[]{MYSQL, ORACLE, SQLite, Informix, HANA}, byte[].class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(clazz == byte[].class){

            }else if(clazz == String.class){
                value = new String((byte[])value);
            }
            return value;
        }
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
    ,LONGBLOB("LONGBLOB", new DatabaseType[]{MYSQL}, byte[].class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,MEDIUMBLOB("MEDIUMBLOB", new DatabaseType[]{MYSQL}, byte[].class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,TINYBLOB("TINYBLOB", new DatabaseType[]{MYSQL}, byte[].class, true, true){
        public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
    }
    /**
     * mysql
     */
    ,MULTILINESTRING("MULTILINESTRING", new DatabaseType[]{MYSQL}, byte[].class, true, true)
    /**
     * pg
     */
    ,BYTEA("BYTEA", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)
    ,BYTE("BYTE", new DatabaseType[]{Informix}, byte[].class, true, true)
    /**
     * pg
     */
    ,JSONB("JSONB", new DatabaseType[]{PostgreSQL}, byte[].class, true, true){
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
    ,IMAGE("IMAGE", new DatabaseType[]{MSSQL}, byte[].class, true, true)
    /**
     * oracle
     */
    ,BFILE("BFILE", new DatabaseType[]{ORACLE}, byte[].class, true, true)
    /* *****************************************************************************************************************
     *
     *                                              byte[]-geometry
     *
     * ****************************************************************************************************************/
    /**
     * mysql,pg
     */
    ,POINT("POINT", new DatabaseType[]{MYSQL, PostgreSQL}, Point.class, byte[].class, true, true){
        public Object read(Object value, Object def, Class clazz){
            if(null == value){
                return value;
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
            return value;
        }
    }
    ,ST_POINT("ST_POINT", new DatabaseType[]{MYSQL, PostgreSQL}, Point.class, byte[].class, true, true){
        public Object read(Object value, Object def, Class clazz){
            return POINT.read(value, def, clazz);
        }
        public Object write(Object value, Object def, boolean placeholder){
            return POINT.write(value, def, placeholder);
        }
    }
    /**
     * mysql
     */
    ,MULTIPOLYGON("MULTIPOLYGON", new DatabaseType[]{MYSQL}, MultiPolygon.class, byte[].class, true, true)
    /**
     * mysql
     */
    ,MULTIPOINT("MULTIPOINT", new DatabaseType[]{MYSQL}, MultiPoint.class, byte[].class, true, true)
    /**
     * mysql,pg
     */
    ,POLYGON("POLYGON", new DatabaseType[]{MYSQL, PostgreSQL}, Polygon.class, byte[].class, true, true)
    /**
     * mysql
     */
    ,GEOMETRY("GEOMETRY", new DatabaseType[]{MYSQL}, byte[].class, true, true)
    ,ST_GEOMETRY("ST_GEOMETRY", new DatabaseType[]{HANA}, byte[].class, true, true)
    /**
     * mysql
     */
    ,GEOMETRYCOLLECTION("GEOMETRYCOLLECTION", new DatabaseType[]{MYSQL}, byte[].class, true, true)
    /**
     * mssql
     */
    ,HIERARCHYID("HIERARCHYID", new DatabaseType[]{MSSQL}, byte[].class, true, true)
    /**
     * pg
     */
    ,LINE("LINE", new DatabaseType[]{PostgreSQL}, LineString.class, byte[].class, true, true)
    /**
     * mysql
     */
    ,LINESTRING("LINESTRING", new DatabaseType[]{MYSQL}, LineString.class, byte[].class, true, true)
    /**
     * pg
     */
    ,PATH("PATH",  new DatabaseType[]{PostgreSQL},LineString.class, true, true)
    /**
     * pg
     */
    ,LSEG("LSEG", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)
    /**
     * mssql
     */
    ,GEOGRAPHY("GEOGRAPHY", new DatabaseType[]{MSSQL}, byte[].class, true, true)
    /**
     * pg
     */
    ,BOX("BOX", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)
    /**
     * pg
     */
    ,CIDR("CIDR", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)
    /**
     * pg
     */
    ,CIRCLE("CIRCLE", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)
    /**
     * pg
     */
    ,INET("INET", new DatabaseType[]{PostgreSQL}, byte[].class, true, true)


    /* *****************************************************************************************************************
     *
     *                                              待实现
     *
     * ****************************************************************************************************************/

    /**
     * mysql
     */
    ,ENUM("ENUM", new DatabaseType[]{MYSQL}, String.class, true, true)
    /**
     * pg
     */
    ,INTERVAL("INTERVAL", new DatabaseType[]{PostgreSQL, Informix}, null, true, true)
    /**
     * oracle
     */
    ,RAW("RAW",  new DatabaseType[]{ORACLE},byte[].class, true, true)
    /**
     * oracle
     */
    ,ROWID("ROWID",  new DatabaseType[]{ORACLE},null, true, true)
    /**
     * mysql
     */
    ,SET("SET",  new DatabaseType[]{MYSQL},String.class, true, true)
    /**
     * pg
     */
    ,TSQUERY("TSQUERY",  new DatabaseType[]{PostgreSQL},null, true, true)
    /**
     * pg
     */
    ,TSVECTOR("TSVECTOR",  new DatabaseType[]{PostgreSQL},null, true, true)
    /**
     * pg
     */
    ,MACADDR("MACADDR", new DatabaseType[]{PostgreSQL}, null, true, true)
    /**
     * pg
     */
    ,PG_SNAPSHOT("PG_SNAPSHOT", new DatabaseType[]{PostgreSQL}, null, true, true)
    /**
     * pg
     * 弃用 换成pg_snapshot
     */
    ,TXID_SNAPSHOT("TXID_SNAPSHOT", new DatabaseType[]{PostgreSQL}, null, true, true)
    /**
     * oracle
     */
    ,UROWID("UROWID",  new DatabaseType[]{ORACLE},null, true, true)
    /**
     * mssql
     */
    ,SQL_VARIANT("SQL_VARIANT",  new DatabaseType[]{MSSQL},null, true, true)

    ;
    private final DatabaseType[] dbs;
    private final String name;
    private Class transfer                  ; //中间转换类型 转换成其他格式前先转换成transfer类型
    private final Class compatible          ; //从数据库中读写数据的类型
    private final Boolean ignorePrecision;
    private final Boolean ignoreScale;
    StandardColumnType(String name, DatabaseType[] dbs, Class transfer, Class compatible, Boolean ignorePrecision, Boolean ignoreScale){
        this.name = name;
        this.dbs = dbs;
        this.transfer = transfer;
        this.compatible = compatible;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    StandardColumnType(String name, DatabaseType[] dbs,  Class compatible, Boolean ignorePrecision, Boolean ignoreScale){
        this.name = name;
        this.dbs = dbs;
        this.compatible = compatible;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }
    @Override
    public Object convert(Object value, Object def){
        return convert(value, null, def);
    }

    @Override
    public Object convert(Object value, Class target){
        Object def = null;
        return convert(value, target, def);
    }

    @Override
    public Object convert(Object value, Class target, Object def) {
        if(null == target){
            target = compatible;
        }
        if(null != value){
            if(value.getClass() == target){
                return value;
            }
            if(null != transfer) {
                value = ConvertAdapter.convert(value, transfer, def);
            }
            value = ConvertAdapter.convert(value, target, def);
        }
        return value;
    }

    @Override
    public Object convert(Object value, Object obj, Field field) {
        return convert(value, field.getType());
    }

    @Override
    public Object read(Object value, Object def, Class clazz) {
        if(null == clazz){
            clazz = transfer;
        }
        if(null == clazz){
            clazz = compatible;
        }
        value = ConvertAdapter.convert(value, clazz, def);
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
    public Object write(Object value, Object def, boolean placeholder) {
        if(null != value){
            if(value.getClass() != compatible) {
                if (null != transfer) {
                    value = ConvertAdapter.convert(value, transfer, def);
                }
                value = ConvertAdapter.convert(value, compatible, def);
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

    @Override
    public DatabaseType[] dbs() {
        return dbs;
    }

}
