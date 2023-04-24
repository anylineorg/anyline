package org.anyline.data.jdbc.oracle;

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

        //需要兼容的类型如:INT INT2 INT4 INT8 BIGINT INTEGER LONG DECIMAL NUMBER MONEY DATE DATETIME TIMESTAMP TIME

        //以下按字母顺序 方便查找
        types.put("BFILE"                   , SQL_BFILE                 ); //        ,oracle
        types.put("BINARY_DOUBLE"           , SQL_BINARY_DOUBLE         ); //        ,oracle
        types.put("BINARY_FLOAT"            , SQL_BINARY_FLOAT          ); //        ,oracle
        types.put("BIGINT"                  , SQL_BIGINT                ); //mysql
        types.put("BINARY"                  , SQL_BINARY                ); //mysql
        types.put("BIT"                     , SQL_BIT                   ); //mysql,pg
        types.put("BLOB"                    , SQL_BLOB                  ); //mysql,  ,oracle
        types.put("BOOL"                    , SQL_BOOL                  ); //     ,pg
        types.put("BOX"                     , SQL_BOX                   ); //     ,pg
        types.put("BYTEA"                   , SQL_BYTEA                 ); //     ,pg
        types.put("CHAR"                    , SQL_CHAR                  ); //mysql,pg
        types.put("CIDR"                    , SQL_CIDR                  ); //      pg
        types.put("CIRCLE"                  , SQL_CIRCLE                ); //      pg
        types.put("CLOB"                    , SQL_CLOB                  ); //        ,oracle
        types.put("DATE"                    , SQL_DATE                  ); //mysql,pg,oracle
        types.put("DATETIME"                , SQL_TIMESTAMP             ); //mysql
        types.put("DECIMAL"                 , SQL_DECIMAL               ); //mysql,  ,oracle
        types.put("DOUBLE"                  , SQL_DOUBLE                ); //mysql
        types.put("ENUM"                    , SQL_ENUM                  ); //mysql
        types.put("FLOAT"                   , SQL_FLOAT                 ); //mysql,  ,oracle
        types.put("GEOMETRY"                , SQL_GEOMETRY              ); //mysql
        types.put("GEOMETRYCOLLECTIO"       , SQL_GEOMETRYCOLLECTIO     ); //mysql
        types.put("INET"                    , SQL_INET                  ); //     ,pg
        types.put("INTERVAL"                , SQL_INTERVAL              ); //     ,pg
        types.put("INT"                     , SQL_INT                   ); //mysql
        types.put("INT2"                    , SQL_INT2                  ); //     ,pg
        types.put("INT4"                    , SQL_INT4                  ); //
        types.put("INT8"                    , SQL_INT8                  ); //
        types.put("INTEGER"                 , SQL_INTEGER               ); //mysql
        types.put("JSON"                    , SQL_JSON                  ); //mysql,pg
        types.put("JSONB"                   , SQL_JSONB                 ); //     ,pg
        types.put("LINE"                    , SQL_LINE                  ); //mysql,pg
        types.put("LSEG"                    , SQL_LSEG                  ); //     ,pg
        types.put("MACADDR"                 , SQL_MACADDR               ); //     ,pg
        types.put("MONEY"                   , SQL_NUMBER                ); //     ,pg
        types.put("NUMBER"                  , SQL_NUMBER                ); //     ,  ,oracle
        types.put("NCHAR"                   , SQL_NCHAR                 ); //     ,  ,oracle
        types.put("NCLOB"                   , SQL_NCLOB                 ); //     ,  ,oracle
        types.put("NVARCHAR2"               , SQL_NVARCHAR2             ); //     ,  ,oracle
        types.put("PATH"                    , SQL_PATH                  ); //     ,pg
        types.put("LONG"                    , SQL_LONG                  ); //
        types.put("LONGBLOB"                , SQL_LONGBLOB              ); //mysql
        types.put("LONGTEXT"                , SQL_LONGTEXT              ); //mysql
        types.put("MEDIUMBLOB"              , SQL_MEDIUMBLOB            ); //mysql
        types.put("MEDIUMINT"               , SQL_MEDIUMINT             ); //mysql
        types.put("MEDIUMTEXT"              , SQL_MEDIUMTEXT            ); //mysql
        types.put("MULTILINESTRING"         , SQL_MULTILINESTRING       ); //mysql
        types.put("MULTIPOINT"              , SQL_MULTIPOINT            ); //mysql
        types.put("MULTIPOLYGON"            , SQL_MULTIPOLYGON          ); //mysql
        types.put("NUMERIC"                 , SQL_NUMERIC               ); //mysql
        types.put("POINT"                   , SQL_POINT                 ); //mysql,pg
        types.put("POLYGON"                 , SQL_POLYGON               ); //mysql,pg
        types.put("REAL"                    , SQL_REAL                  ); //mysql
        types.put("RAW"                     , SQL_RAW                   ); //     ,  ,oracle
        types.put("ROWID"                   , SQL_ROWID                 ); //     ,  ,oracle
        types.put("SERIAL"                  , SQL_SERIAL                ); //     ,pg,
        types.put("SET"                     , SQL_SET                   ); //mysql
        types.put("SMALLINT"                , SQL_INT                   ); //mysql
        types.put("TEXT"                    , SQL_TEXT                  ); //mysql,pg
        types.put("TIME"                    , SQL_TIMESTAMP             ); //mysql,pg
        types.put("TIMEZ"                   , SQL_TIMESTAMP             ); //     ,pg
        types.put("TIMESTAMP"               , SQL_TIMESTAMP             ); //mysql,pg,oracle
        types.put("TIMESTAMP_LOCAL_ZONE"    , SQL_TIMESTAMP_LOCAL_ZONE  ); //     ,pg
        types.put("TIMESTAMP_ZONE"          , SQL_TIMESTAMP_ZONE        ); //     ,pg
        types.put("TSQUERY"                 , SQL_TSQUERY               ); //     ,pg
        types.put("TSVECTOR"                , SQL_TSVECTOR              ); //     ,pg
        types.put("TXID_SNAPSHOT"           , SQL_TXID_SNAPSHOT         ); //     ,pg
        types.put("UUID"                    , SQL_UUID                  ); //     ,pg
        types.put("UROWID"                  , SQL_UROWID                ); //     ,  ,oracle
        types.put("VARBIT"                  , SQL_VARBIT                ); //     ,pg
        types.put("TINYBLOB"                , SQL_TINYBLOB              ); //mysql
        types.put("TINYINT"                 , SQL_TINYINT               ); //mysql
        types.put("TINYTEXT"                , SQL_TINYTEXT              ); //mysql
        types.put("VARBINARY"               , SQL_VARBINARY             ); //mysql
        types.put("VARCHAR"                 , SQL_VARCHAR               ); //mysql,pg,oracle
        types.put("VARCHAR2"                , SQL_VARCHAR2              ); //     ,  ,oracle
        types.put("XML"                     , SQL_XML                   ); //     ,pg
        types.put("YEAR"                    , SQL_YEAR                  ); //mysql


        types.put("JAVA.MATH.DECIMAL"              , SQL_NUMBER         );
        types.put("JAVA.LANG.DOUBLE"               , SQL_NUMBER         );
        types.put("JAVA.LANG.BOOLEAN"              , SQL_BOOL           );
        types.put("JAVA.LANG.INTEGER"              , SQL_INT            );
        types.put("JAVA.LANG.LONG"                 , SQL_LONG           );
        types.put("JAVA.LANG.FLOAT"                , SQL_FLOAT          );
        types.put("JAVA.LANG.STRING"               , SQL_VARCHAR        );
        types.put("JAVA.UTIL.DATE"                 , SQL_TIMESTAMP      );
        types.put("JAVA.SQL.DATE"                  , SQL_DATE           );
        types.put("JAVA.SQL.TIMESTAMP"             , SQL_TIMESTAMP      );
        types.put("JAVA.SQL.TIME"                  , SQL_DATE           );
        types.put("JAVA.TIME.LOCALDATE"            , SQL_DATE           );
        types.put("JAVA.TIME.LOCALTIME"            , SQL_DATE           );
        types.put("JAVA.TIME.LOCALDATETIME"        , SQL_TIMESTAMP      );

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
                }
                return value;
            }
        };  //mysql

        protected DataType SQL_TIME              = new DataType() {public String getName(){return "TIME";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql,pg
        protected DataType SQL_TIMEZ             = new DataType() {public String getName(){return "TIMEZ";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        protected DataType SQL_TIMESTAMP_ZONE    = new DataType() {public String getName(){return "TIMESTAMP";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        protected DataType SQL_TIMESTAMP_LOCAL_ZONE= new DataType() {public String getName(){return "TIMESTAMP";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg

        protected DataType SQL_YEAR              = new DataType() {public String getName(){return "YEAR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return SQL_DATE.write(value, def, placeholder);}
        };


}
