package org.anyline.data.jdbc.mssql;

import org.anyline.entity.mdtadata.DataType;
import org.anyline.util.DateUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class DataTypeAdapter extends org.anyline.data.jdbc.adapter.DataTypeAdapter {
    private static Map<String, DataType> types = new Hashtable<>();
    
    public  DataType type(String type){
        if(null != type){
            DataType dt = types.get(type.toUpperCase());
            if(null == dt){
                dt = super.type(type);
            }
            return dt;
        }
        return null;
    }
    public DataTypeAdapter(){
        // 类型定位时通过key,先通过子类定位,失败后通过父类
        // key:开发中有可能书写的类型(特别是在多数据库环境下,创建表时不为mysql写一个脚本,pg写一个脚本,而一个脚本两处执行)
        // value:实际执行时的类型
        // 如Long在mysql中用BigInt 在pg用中int8,需要在各自的types中分别put("long")put("bigint")put("int8"),MySQL中可以不put("bigint")因为父类中已经put了key与value一致的情况
        // 执行时通过value
        // 把每种可能的key put进types并对应准确的类型
        // 父类中有的value不需要重复实现,需要重新实现的一般是与内置函数有关的类型(如to_date)

        // 以下按字母顺序 方便查找
        // 后面备注表示key在哪个数据库中使用过
        // 了类配置中:如果在当前数据库中没的再现的,应该把value换成当前数据库支持的正常的类型
        types.put("BFILE"                   , SQL_BFILE                 ); //     ,  ,oracle,
        types.put("BINARY_DOUBLE"           , SQL_BINARY_DOUBLE         ); //     ,  ,oracle,
        types.put("BINARY_FLOAT"            , SQL_BINARY_FLOAT          ); //     ,  ,oracle,
        types.put("BIGINT"                  , SQL_BIGINT                ); //mysql,  ,      ,mssql,
        types.put("BIGSERIAL"               , SQL_SERIAL8               ); //     ,pg,
        types.put("BINARY"                  , SQL_BINARY                ); //mysql,  ,      ,mssql,
        types.put("BIT"                     , SQL_BIT                   ); //mysql,pg,      ,mssql,
        types.put("BLOB"                    , SQL_BYTEA                  ); //mysql,  ,oracle
        types.put("BOOL"                    , SQL_BOOL                  ); //     ,pg
        types.put("BOX"                     , SQL_BOX                   ); //     ,pg
        types.put("BYTEA"                   , SQL_BYTEA                 ); //     ,pg
        types.put("CHAR"                    , SQL_CHAR                  ); //mysql,pg,      ,mssql,
        types.put("CIDR"                    , SQL_CIDR                  ); //      pg
        types.put("CIRCLE"                  , SQL_CIRCLE                ); //      pg
        types.put("CLOB"                    , SQL_CLOB                  ); //     ,  ,oracle
        types.put("DATE"                    , SQL_DATE                  ); //mysql,pg,oracle,mssql
        types.put("DATETIME"                , SQL_DATETIME              ); //mysql,  ,      ,mssql
        types.put("DATETIME2"               , SQL_DATETIME2             ); //mysql,  ,      ,mssql
        types.put("DATETIMEOFFSET"          , SQL_DATETIMEOFFSET        ); //mysql,  ,      ,mssql
        types.put("DECIMAL"                 , SQL_DECIMAL               ); //mysql,  ,oracle,mssql
        types.put("DOUBLE"                  , SQL_DOUBLE                ); //mysql,
        types.put("ENUM"                    , SQL_ENUM                  ); //mysql,
        types.put("FLOAT"                   , SQL_FLOAT4                ); //mysql,  ,oracle,mssql
        types.put("FLOAT4"                  , SQL_FLOAT4                ); //     ,pg
        types.put("FLOAT8"                  , SQL_FLOAT8                ); //     ,pg
        types.put("GEOGRAPHY"               , SQL_GEOGRAPHY             ); //     ,  ,      ,mssql
        types.put("GEOMETRY"                , SQL_GEOMETRY              ); //mysql
        types.put("GEOMETRYCOLLECTIO"       , SQL_GEOMETRYCOLLECTIO     ); //mysql
        types.put("HIERARCHYID"             , SQL_HIERARCHYID           ); //     ,  ,      ,mssql
        types.put("IMAGE"                   , SQL_IMAGE                 ); //     ,  ,      ,mssql
        types.put("INET"                    , SQL_INET                  ); //     ,pg
        types.put("INTERVAL"                , SQL_INTERVAL              ); //     ,pg
        types.put("INT"                     , SQL_INT                   ); //mysql,  ,      ,mssql,
        types.put("INT2"                    , SQL_INT                   ); //     ,pg
        types.put("INT4"                    , SQL_INT                   ); //
        types.put("INT8"                    , SQL_BIGINT                ); //
        types.put("INTEGER"                 , SQL_INT                   ); //mysql
        types.put("JSON"                    , SQL_JSON                  ); //mysql,pg
        types.put("JSONB"                   , SQL_JSONB                 ); //     ,pg
        types.put("LINE"                    , SQL_LINE                  ); //mysql,pg
        types.put("LONG"                    , SQL_BIGINT                ); //     ,pg
        types.put("LONGBLOB"                , SQL_BYTEA                 ); //mysql
        types.put("LONGTEXT"                , SQL_LONGTEXT              ); //mysql
        types.put("LSEG"                    , SQL_LSEG                  ); //     ,pg
        types.put("MACADDR"                 , SQL_MACADDR               ); //     ,pg
        types.put("MONEY"                   , SQL_MONEY                 ); //     ,pg,      ,mssql
        types.put("NUMBER"                  , SQL_NUMBER                ); //     ,  ,oracle
        types.put("NCHAR"                   , SQL_NCHAR                 ); //     ,  ,oracle,mssql
        types.put("NCLOB"                   , SQL_NCLOB                 ); //     ,  ,oracle
        types.put("NTEXT"                   , SQL_NTEXT                 ); //     ,  ,      ,mssql
        types.put("NVARCHAR"                , SQL_NVARCHAR              ); //     ,  ,      ,mssql
        types.put("NVARCHAR2"               , SQL_NVARCHAR2             ); //     ,  ,oracle
        types.put("PATH"                    , SQL_PATH                  ); //     ,pg
        types.put("MEDIUMBLOB"              , SQL_MEDIUMBLOB            ); //mysql,
        types.put("MEDIUMINT"               , SQL_MEDIUMINT             ); //mysql,
        types.put("MEDIUMTEXT"              , SQL_MEDIUMTEXT            ); //mysql,
        types.put("MULTILINESTRING"         , SQL_MULTILINESTRING       ); //mysql,
        types.put("MULTIPOINT"              , SQL_MULTIPOINT            ); //mysql,
        types.put("MULTIPOLYGON"            , SQL_MULTIPOLYGON          ); //mysql,
        types.put("NUMERIC"                 , SQL_NUMERIC               ); //mysql,  ,       ,mssql
        types.put("POINT"                   , SQL_POINT                 ); //mysql,pg
        types.put("POLYGON"                 , SQL_POLYGON               ); //mysql,pg
        types.put("REAL"                    , SQL_REAL                  ); //mysql,  ,      ,mssql
        types.put("RAW"                     , SQL_RAW                   ); //     ,  ,oracle
        types.put("ROWID"                   , SQL_ROWID                 ); //     ,  ,oracle
        types.put("SERIAL"                  , SQL_SERIAL                ); //     ,pg,
        types.put("SERIAL2"                 , SQL_SERIAL2               ); //     ,pg,
        types.put("SERIAL4"                 , SQL_SERIAL4               ); //     ,pg,
        types.put("SERIAL8"                 , SQL_SERIAL8               ); //     ,pg,
        types.put("SET"                     , SQL_SET                   ); //mysql,
        types.put("SMALLDATETIME"           , SQL_SMALLDATETIME         ); //     ,  ,      ,mssql
        types.put("SMALLMONEY"              , SQL_SMALLMONEY            ); //     ,  ,      ,mssql
        types.put("SMALLINT"                , SQL_SMALLINT              ); //mysql,
        types.put("SMALSERIAL"              , SQL_SERIAL2               ); //     ,pg,
        types.put("SQL_VARIANT"             , DataType.NOT_SUPPORT      ); //     ,  ,      ,mssql
        types.put("SYSNAME"                 , SQL_SYSNAME               ); //     ,  ,      ,mssql
        types.put("TEXT"                    , SQL_TEXT                  ); //mysql,pg,      ,mssql
        types.put("TIME"                    , SQL_TIME                  ); //mysql,pg,      ,mssql
        types.put("TIMEZ"                   , SQL_TIMEZ                 ); //     ,pg
        types.put("TIMESTAMP"               , SQL_TIMESTAMP             ); //mysql,pg,oracle,mssql
        types.put("TIMESTAMP_LOCAL_ZONE"    , SQL_TIMESTAMP_LOCAL_ZONE  ); //     ,pg
        types.put("TIMESTAMP_ZONE"          , SQL_TIMESTAMP_ZONE        ); //     ,pg
        types.put("TSQUERY"                 , SQL_TSQUERY               ); //     ,pg
        types.put("TSVECTOR"                , SQL_TSVECTOR              ); //     ,pg
        types.put("TXID_SNAPSHOT"           , SQL_TXID_SNAPSHOT         ); //     ,pg
        types.put("UNIQUEIDENTIFIER"        , SQL_UNIQUEIDENTIFIER      ); //     ,  ，     ,mssql
        types.put("UUID"                    , SQL_UUID                  ); //     ,pg
        types.put("UROWID"                  , SQL_UROWID                ); //     ,  ,oracle
        types.put("VARBIT"                  , SQL_VARBIT                ); //     ,pg
        types.put("TINYBLOB"                , SQL_TINYBLOB              ); //mysql,
        types.put("TINYINT"                 , SQL_TINYINT               ); //mysql,  ,      ,mssql
        types.put("TINYTEXT"                , SQL_TINYTEXT              ); //mysql,
        types.put("VARBINARY"               , SQL_VARBINARY             ); //mysql,  ,      ,mssql
        types.put("VARCHAR"                 , SQL_VARCHAR               ); //mysql,pg,oracle,mssql
        types.put("VARCHAR2"                , SQL_VARCHAR               ); //     ,  ,oracle,
        types.put("XML"                     , SQL_XML                   ); //     ,pg，      ,mssql
        types.put("YEAR"                    , SQL_YEAR                  ); //mysql,


        types.put("JAVA.MATH.DECIMAL"              , SQL_DECIMAL        );
        types.put("JAVA.LANG.DOUBLE"               , SQL_DOUBLE         );
        types.put("JAVA.LANG.BOOLEAN"              , SQL_BOOL           );
        types.put("JAVA.LANG.INTEGER"              , SQL_INT            );
        types.put("JAVA.LANG.LONG"                 , SQL_LONG           );
        types.put("JAVA.LANG.FLOAT"                , SQL_FLOAT          );
        types.put("JAVA.LANG.STRING"               , SQL_VARCHAR        );
        types.put("JAVA.UTIL.DATE"                 , SQL_DATETIME       );
        types.put("JAVA.SQL.DATE"                  , SQL_DATE           );
        types.put("JAVA.SQL.TIMESTAMP"             , SQL_TIMESTAMP      );
        types.put("JAVA.SQL.TIME"                  , SQL_TIME           );
        types.put("JAVA.TIME.LOCALDATE"            , SQL_DATE           );
        types.put("JAVA.TIME.LOCALTIME"            , SQL_TIME           );
        types.put("JAVA.TIME.LOCALDATETIME"        , SQL_DATETIME       );

    } 

        /* *********************************************************************************************************************************
         *
         *                                              date
         *
         * *********************************************************************************************************************************/
        //TODO         write 需要根据数据库类型 由内置函数转换
        protected DataType SQL_DATE              = new DataType() {public String getName(){return "DATE";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                Date date = DateUtil.parse(value);
                if (null != date) {
                    if(placeholder){
                        value = new java.sql.Date(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd");
                    }
                }else{
                    value = null;
                }
                return value;
            }
        };  //mysql,pg,oracle
        protected DataType SQL_TIMESTAMP          = new DataType() {public String getName(){return "TIMESTAMP";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new Timestamp(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date) + "'";
                    }
                }else{
                    value = null;
                }
                return value;
            }
        };  //mysql

        protected DataType SQL_TIME              = new DataType() {public String getName(){return "TIME";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return DataTypeAdapter.super.SQL_TIME.write(value, def, placeholder);}
        };  //mysql,pg
        protected DataType SQL_TIMEZ             = new DataType() {public String getName(){return "TIMEZ";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return SQL_TIME.write(value, def, placeholder);}
        };  //     ,pg
        protected DataType SQL_TIMESTAMP_ZONE    = new DataType() {public String getName(){return "TIMESTAMP";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return SQL_TIME.write(value, def, placeholder);}
        };  //     ,pg
        protected DataType SQL_TIMESTAMP_LOCAL_ZONE= new DataType() {public String getName(){return "TIMESTAMP";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return SQL_TIME.write(value, def, placeholder);}
        };  //     ,pg

        protected DataType SQL_YEAR              = new DataType() {public String getName(){return "YEAR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return SQL_DATE.write(value, def, placeholder);}
        };

    
}
