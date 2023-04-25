package org.anyline.data.jdbc.sqlite;

import org.anyline.entity.mdtadata.DataType;

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
        // 子类中把每种可能的key put进types并对应准确的类型
        // 父类中有的value不需要重复实现,需要重新实现的一般是与内置函数有关的类型(如to_date)

        // 以下按字母顺序 方便查找
        // 后面备注表示key在哪个数据库中使用过
        // 了类配置中:如果在当前数据库中没的再现的,应该把value换成当前数据库支持的正常的类型
        types.put("BFILE"                   , SQL_BLOB              ); //        ,oracle
        types.put("BINARY_DOUBLE"           , SQL_BLOB              ); //        ,oracle
        types.put("BINARY_FLOAT"            , SQL_BLOB              ); //        ,oracle
        types.put("BIGINT"                  , SQL_INTEGER           ); //mysql
        types.put("BIGSERIAL"               , SQL_REAL              ); //     ,pg,
        types.put("BINARY"                  , SQL_BLOB              ); //mysql
        types.put("BIT"                     , SQL_INTEGER           ); //mysql,pg
        types.put("BLOB"                    , SQL_BLOB              ); //mysql,  ,oracle
        types.put("BOOL"                    , SQL_INTEGER           ); //     ,pg
        types.put("BOX"                     , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("BYTEA"                   , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("CHAR"                    , SQL_TEXT              ); //mysql,pg
        types.put("CIDR"                    , DataType.NOT_SUPPORT  ); //      pg
        types.put("CIRCLE"                  , DataType.NOT_SUPPORT  ); //      pg
        types.put("CLOB"                    , SQL_TEXT              ); //        ,oracle
        types.put("DATE"                    , SQL_TEXT              ); //mysql,pg,oracle
        types.put("DATETIME"                , SQL_TEXT              ); //mysql
        types.put("DATETIME2"               , SQL_TEXT              ); //     ,  ,      ,mssql
        types.put("DATETIMEOFFSET"          , SQL_TEXT              ); //     ,  ,      ,mssql
        types.put("DECIMAL"                 , SQL_REAL              ); //mysql,  ,oracle,mssql
        types.put("DOUBLE"                  , SQL_REAL              ); //mysql
        types.put("ENUM"                    , DataType.NOT_SUPPORT  ); //mysql
        types.put("FLOAT"                   , SQL_REAL              ); //mysql,  ,oracle,mssql
        types.put("FLOAT4"                  , SQL_REAL              ); //     ,pg
        types.put("FLOAT8"                  , SQL_REAL              ); //     ,pg
        types.put("GEOGRAPHY"               , SQL_GEOGRAPHY         ); //     ,  ,      ,mssql
        types.put("GEOMETRY"                , DataType.NOT_SUPPORT  ); //mysql
        types.put("GEOMETRYCOLLECTIO"       , DataType.NOT_SUPPORT  ); //mysql
        types.put("HIERARCHYID"             , SQL_HIERARCHYID       ); //     ,  ,      ,mssql
        types.put("IMAGE"                   , SQL_IMAGE             ); //     ,  ,      ,mssql
        types.put("INET"                    , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("INTERVAL"                , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("INT"                     , SQL_INTEGER           ); //mysql,  ,      ,mssql,
        types.put("INT2"                    , SQL_INTEGER           ); //     ,pg
        types.put("INT4"                    , SQL_INTEGER           ); //
        types.put("INT8"                    , SQL_INTEGER           ); //
        types.put("INTEGER"                 , SQL_INTEGER           ); //mysql
        types.put("JSON"                    , SQL_TEXT              ); //mysql,pg
        types.put("JSONB"                   , SQL_TEXT              ); //     ,pg
        types.put("LINE"                    , SQL_INTEGER           ); //mysql,pg
        types.put("LSEG"                    , SQL_INTEGER           ); //     ,pg
        types.put("MACADDR"                 , SQL_INTEGER           ); //     ,pg
        types.put("MONEY"                   , SQL_REAL              ); //     ,pg,      ,mssql
        types.put("NUMBER"                  , SQL_REAL              ); //     ,  ,oracle
        types.put("NCHAR"                   , SQL_TEXT              ); //     ,  ,oracle,mssql
        types.put("NCLOB"                   , SQL_TEXT              ); //     ,  ,oracle
        types.put("NTEXT"                   , SQL_TEXT              ); //     ,  ,      ,mssql
        types.put("NVARCHAR"                , SQL_TEXT              ); //     ,  ,       ,mssql
        types.put("NVARCHAR2"               , SQL_TEXT              ); //     ,  ,oracle
        types.put("PATH"                    , SQL_INTEGER           ); //     ,pg
        types.put("LONG"                    , SQL_INTEGER           ); //     ,  ,oracle
        types.put("LONGBLOB"                , SQL_BLOB              ); //mysql
        types.put("LONGTEXT"                , SQL_TEXT              ); //mysql
        types.put("MEDIUMBLOB"              , SQL_BLOB              ); //mysql
        types.put("MEDIUMINT"               , SQL_INTEGER           ); //mysql
        types.put("MEDIUMTEXT"              , SQL_TEXT              ); //mysql
        types.put("MULTILINESTRING"         , DataType.NOT_SUPPORT  ); //mysql
        types.put("MULTIPOINT"              , DataType.NOT_SUPPORT  ); //mysql
        types.put("MULTIPOLYGON"            , DataType.NOT_SUPPORT  ); //mysql
        types.put("NUMERIC"                 , SQL_REAL              ); //mysql,pg ,       ,mssql
        types.put("POINT"                   , DataType.NOT_SUPPORT  ); //mysql,pg
        types.put("POLYGON"                 , DataType.NOT_SUPPORT  ); //mysql,pg
        types.put("REAL"                    , SQL_REAL              ); //mysql
        types.put("RAW"                     , DataType.NOT_SUPPORT  ); //     ,  ,oracle
        types.put("ROWID"                   , DataType.NOT_SUPPORT  ); //     ,  ,oracle
        types.put("SERIAL"                  , SQL_INTEGER           ); //     ,pg,
        types.put("SERIAL2"                 , SQL_INTEGER           ); //     ,pg,
        types.put("SERIAL4"                 , SQL_INTEGER           ); //     ,pg,
        types.put("SERIAL8"                 , SQL_INTEGER           ); //     ,pg,
        types.put("SET"                     , DataType.NOT_SUPPORT  ); //mysql
        types.put("SMALLDATETIME"           , SQL_TEXT              ); //     ,  ,      ,mssql
        types.put("SMALLMONEY"              , SQL_REAL              ); //     ,  ,      ,mssql
        types.put("SMALLINT"                , SQL_INTEGER           ); //mysql
        types.put("SMALSERIAL"              , SQL_INTEGER           ); //     ,pg,
        types.put("SQL_VARIANT"             , DataType.NOT_SUPPORT  ); //     ,  ,      ,mssql
        types.put("SYSNAME"                 , SQL_TEXT              ); //     ,  ,      ,mssql
        types.put("TEXT"                    , SQL_TEXT              ); //mysql,pg       ,mssql
        types.put("TIME"                    , SQL_TEXT              ); //mysql,pg       ,mssql
        types.put("TIMEZ"                   , SQL_TEXT              ); //     ,pg
        types.put("TIMESTAMP"               , SQL_TEXT              ); //mysql,pg,oracle,mssql
        types.put("TIMESTAMP_LOCAL_ZONE"    , SQL_TEXT              ); //     ,pg
        types.put("TIMESTAMP_ZONE"          , SQL_TEXT              ); //     ,pg
        types.put("TSQUERY"                 , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("TSVECTOR"                , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("TXID_SNAPSHOT"           , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("UNIQUEIDENTIFIER"        , SQL_TEXT              ); //     ,  ，     ,mssql
        types.put("UUID"                    , SQL_TEXT              ); //     ,pg
        types.put("UROWID"                  , DataType.NOT_SUPPORT  ); //     ,  ,oracle
        types.put("VARBIT"                  , DataType.NOT_SUPPORT  ); //     ,pg
        types.put("TINYBLOB"                , SQL_BLOB              ); //mysql
        types.put("TINYINT"                 , SQL_INTEGER           ); //mysql,  ,      ,mssql
        types.put("TINYTEXT"                , SQL_TEXT              ); //mysql
        types.put("VARBINARY"               , SQL_BLOB              ); //mysql,  ,      ,mssql
        types.put("VARCHAR"                 , SQL_TEXT              ); //mysql,pg,oracle,mssql
        types.put("VARCHAR2"                , SQL_TEXT              ); //     ,  ,oracle
        types.put("XML"                     , SQL_TEXT              ); //     ,pg，      ,mssql
        types.put("YEAR"                    , SQL_INTEGER           ); //mysql


        types.put("JAVA.MATH.DECIMAL"              , SQL_REAL       );
        types.put("JAVA.LANG.DOUBLE"               , SQL_REAL       );
        types.put("JAVA.LANG.BOOLEAN"              , SQL_INTEGER    );
        types.put("JAVA.LANG.INTEGER"              , SQL_INTEGER    );
        types.put("JAVA.LANG.LONG"                 , SQL_INTEGER    );
        types.put("JAVA.LANG.FLOAT"                , SQL_REAL       );
        types.put("JAVA.LANG.STRING"               , SQL_TEXT       );
        types.put("JAVA.UTIL.DATE"                 , SQL_TEXT       );
        types.put("JAVA.SQL.DATE"                  , SQL_TEXT       );
        types.put("JAVA.SQL.TIMESTAMP"             , SQL_TEXT       );
        types.put("JAVA.SQL.TIME"                  , SQL_TEXT       );
        types.put("JAVA.TIME.LOCALDATE"            , SQL_TEXT       );
        types.put("JAVA.TIME.LOCALTIME"            , SQL_TEXT       );
        types.put("JAVA.TIME.LOCALDATETIME"        , SQL_TEXT       );
    }

}
