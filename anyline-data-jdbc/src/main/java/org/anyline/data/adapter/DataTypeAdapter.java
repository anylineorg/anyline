package org.anyline.data.adapter;

import org.anyline.data.metadata.DataType;

import java.util.Hashtable;
import java.util.Map;

public class DataTypeAdapter {
    private static Map<String, DataType> types = new Hashtable<>();

    public org.anyline.entity.metadata.DataType type(String type){
        if(null != type){
            return types.get(type.toUpperCase());
        }else{
            return null;
        }
    }
    public DataTypeAdapter(){

        // 类型定位时通过key,先通过子类定位,失败后通过父类
        // key:开发中有可能书写的类型(特别是在多数据库环境下,创建表时不为mysql写一个脚本,pg写一个脚本,而一个脚本两处执行)
        // value:实际执行时的类型
        // 执行时通过value
        // 子类中把每种可能的key put进types并对应准确的类型(子类自己创建一个enum 保证value只能自己支持的类型)
        // 父类enum中有的value,子类enum不需要重复实现,需要重新实现的一般是与内置函数有关的类型(如to_date)

        // 以下按字母顺序 方便查找
        // 后面备注表示key在哪个数据库中使用过
        //以下所有的key每个子类中保持完全一致,有不支持的put DataType.UNSUPPORT
        types.put("BFILE"                   , DataType.BFILE                 ); //        ,oracle,
        types.put("BINARY_DOUBLE"           , DataType.BINARY_DOUBLE         ); //        ,oracle,
        types.put("BINARY_FLOAT"            , DataType.BINARY_FLOAT          ); //        ,oracle,
        types.put("BIGINT"                  , DataType.BIGINT                ); //mysql,  ,      ,mssql
        types.put("BIGSERIAL"               , DataType.SERIAL8               ); //     ,pg,
        types.put("BINARY"                  , DataType.BINARY                ); //mysql,  ,      ,mssql
        types.put("BIT"                     , DataType.BIT                   ); //mysql,pg,
        types.put("BLOB"                    , DataType.BLOB                  ); //mysql,  ,oracle,     ,sqlite
        types.put("BOOLEAN"                 , DataType.BOOLEAN               ); //     ,pg,
        types.put("BOOL"                    , DataType.BOOL                  ); //     ,pg,
        types.put("BOX"                     , DataType.BOX                   ); //     ,pg,
        types.put("BYTEA"                   , DataType.BYTEA                 ); //     ,pg,
        types.put("CHAR"                    , DataType.CHAR                  ); //mysql,pg,oracle,mssql
        types.put("CIDR"                    , DataType.CIDR                  ); //      pg,
        types.put("CIRCLE"                  , DataType.CIRCLE                ); //      pg,
        types.put("CLOB"                    , DataType.CLOB                  ); //        ,oracle,
        types.put("DATE"                    , DataType.DATE                  ); //mysql,pg,oracle,
        types.put("DATETIME"                , DataType.DATETIME              ); //mysql,
        types.put("DATETIME2"               , DataType.DATETIME2             ); //     ,   ,     ,mssql,
        types.put("DATETIMEOFFSET"          , DataType.DATETIMEOFFSET        ); //     ,  ,      ,mssql
        types.put("DECIMAL"                 , DataType.DECIMAL               ); //mysql,pg,oracle,mssql
        types.put("DOUBLE"                  , DataType.DOUBLE                ); //mysql,
        types.put("ENUM"                    , DataType.ENUM                  ); //mysql,
        types.put("FLOAT"                   , DataType.FLOAT                 ); //mysql,  ,oracle,mssql,
        types.put("FLOAT4"                  , DataType.FLOAT4                ); //     ,pg,
        types.put("FLOAT8"                  , DataType.FLOAT8                ); //     ,pg,
        types.put("GEOGRAPHY"               , DataType.GEOGRAPHY             ); //     ,  ,      ,mssql
        types.put("GEOMETRY"                , DataType.GEOMETRY              ); //mysql,  ,      ,mssql
        types.put("GEOMETRYCOLLECTION"      , DataType.GEOMETRYCOLLECTION    ); //mysql,
        types.put("HIERARCHYID"             , DataType.HIERARCHYID           ); //     ,  ,      ,mssql
        types.put("IMAGE"                   , DataType.IMAGE                 ); //     ,  ,      ,mssql
        types.put("INET"                    , DataType.INET                  ); //     ,pg,
        types.put("INTERVAL"                , DataType.INTERVAL              ); //     ,pg,
        types.put("INT"                     , DataType.INT                   ); //mysql,  ,      ,mssql,
        types.put("INT2"                    , DataType.INT2                  ); //     ,pg,
        types.put("INT4"                    , DataType.INT4                  ); //     ,pg
        types.put("INT8"                    , DataType.INT8                  ); //     ,pg
        types.put("INTEGER"                 , DataType.INTEGER               ); //mysql,  ,      ,     ,sqlite
        types.put("JSON"                    , DataType.JSON                  ); //mysql,pg,
        types.put("JSONB"                   , DataType.JSONB                 ); //     ,pg,
        types.put("LINE"                    , DataType.LINE                  ); //mysql,pg,
        types.put("LONG"                    , DataType.LONG                  ); //     ,  ,oracle,
        types.put("LONGBLOB"                , DataType.LONGBLOB              ); //mysql,
        types.put("LONGTEXT"                , DataType.LONGTEXT              ); //mysql,
        types.put("LSEG"                    , DataType.LSEG                  ); //     ,pg,
        types.put("MACADDR"                 , DataType.MACADDR               ); //     ,pg,
        types.put("MEDIUMBLOB"              , DataType.MEDIUMBLOB            ); //mysql,
        types.put("MEDIUMINT"               , DataType.MEDIUMINT             ); //mysql,
        types.put("MEDIUMTEXT"              , DataType.MEDIUMTEXT            ); //mysql,
        types.put("MULTILINESTRING"         , DataType.MULTILINESTRING       ); //mysql,
        types.put("MULTIPOINT"              , DataType.MULTIPOINT            ); //mysql,
        types.put("MULTIPOLYGON"            , DataType.MULTIPOLYGON          ); //mysql,
        types.put("MONEY"                   , DataType.MONEY                 ); //     ,pg,      ,mssql
        types.put("NUMBER"                  , DataType.NUMBER                ); //     ,  ,oracle,
        types.put("NCHAR"                   , DataType.NCHAR                 ); //     ,  ,oracle,mssql
        types.put("NCLOB"                   , DataType.NCLOB                 ); //     ,  ,oracle,
        types.put("NTEXT"                   , DataType.NTEXT                 ); //     ,  ,      ,mssql
        types.put("NVARCHAR2"               , DataType.NVARCHAR2             ); //     ,  ,oracle,
        types.put("NVARCHAR"                , DataType.NVARCHAR              ); //     ,  ,       ,mssql
        types.put("NUMERIC"                 , DataType.NUMERIC               ); //mysql,pg,       ,mssql,sqlite
        types.put("PATH"                    , DataType.PATH                  ); //     ,pg,
        types.put("POINT"                   , DataType.POINT                 ); //mysql,pg,
        types.put("POLYGON"                 , DataType.POLYGON               ); //mysql,pg,
        types.put("REAL"                    , DataType.REAL                  ); //mysql,  ,      ,mssql,sqlite
        types.put("RAW"                     , DataType.RAW                   ); //     ,  ,oracle,
        types.put("ROWID"                   , DataType.ROWID                 ); //     ,  ,oracle,
        types.put("SERIAL"                  , DataType.SERIAL                ); //     ,pg,
        types.put("SERIAL2"                 , DataType.SERIAL2               ); //     ,pg,
        types.put("SERIAL4"                 , DataType.SERIAL4               ); //     ,pg,
        types.put("SERIAL8"                 , DataType.SERIAL8               ); //     ,pg,
        types.put("SET"                     , DataType.SET                   ); //mysql,
        types.put("SMALLDATETIME"           , DataType.SMALLDATETIME         ); //     ,  ,      ,mssql
        types.put("SMALLMONEY"              , DataType.SMALLMONEY            ); //     ,  ,      ,mssql
        types.put("SMALLINT"                , DataType.SMALLINT              ); //mysql,
        types.put("SMALLSERIAL"              , DataType.SERIAL2               ); //     ,pg,
        types.put("DataType.VARIANT"        , DataType.SQL_VARIANT           ); //     ,  ,      ,mssql
        types.put("TEXT"                    , DataType.TEXT                  ); //mysql,pg,      ,mssql,sqlite
        types.put("TIME"                    , DataType.TIME                  ); //mysql,pg,      ,mssql
        types.put("TIMEZ"                   , DataType.TIMEZ                 ); //     ,pg,
        types.put("TIMESTAMP"               , DataType.TIMESTAMP             ); //mysql,pg,oracle,mssql
        types.put("TIMESTAMP_LOCAL_ZONE"    , DataType.TIMESTAMP_ZONE        ); //     ,pg,
        types.put("TIMESTAMP_ZONE"          , DataType.TIMESTAMP_ZONE        ); //     ,pg,
        types.put("TSQUERY"                 , DataType.TSQUERY               ); //     ,pg,
        types.put("TSVECTOR"                , DataType.TSVECTOR              ); //     ,pg,
        types.put("TXID_SNAPSHOT"           , DataType.TXID_SNAPSHOT         ); //     ,pg,
        types.put("UNIQUEIDENTIFIER"        , DataType.UNIQUEIDENTIFIER      ); //     ,  ，     ,mssql
        types.put("UUID"                    , DataType.UUID                  ); //     ,pg,
        types.put("UROWID"                  , DataType.UROWID                ); //     ,  ,oracle,
        types.put("VARBIT"                  , DataType.VARBIT                ); //     ,pg,
        types.put("TINYBLOB"                , DataType.TINYBLOB              ); //mysql,
        types.put("TINYINT"                 , DataType.TINYINT               ); //mysql,  ,      ,mssql
        types.put("TINYTEXT"                , DataType.TINYTEXT              ); //mysql,
        types.put("VARBINARY"               , DataType.VARBINARY             ); //mysql,  ,      ,mssql
        types.put("VARCHAR"                 , DataType.VARCHAR               ); //mysql,pg,oracle,mssql
        types.put("VARCHAR2"                , DataType.VARCHAR2              ); //     ,  ,oracle,
        types.put("XML"                     , DataType.XML                   ); //     ,pg,      ,mssql
        types.put("YEAR"                    , DataType.YEAR                  ); //mysql,


        types.put("JAVA.MATH.DECIMAL"       , DataType.JAVA_DECIMAL         );
        types.put("JAVA.LANG.DOUBLE"        , DataType.JAVA_DOUBLE          );
        types.put("JAVA.LANG.BOOLEAN"       , DataType.JAVA_BOOL            );
        types.put("JAVA.LANG.INTEGER"       , DataType.JAVA_INTEGER         );
        types.put("JAVA.LANG.LONG"          , DataType.JAVA_LONG            );
        types.put("JAVA.LANG.FLOAT"         , DataType.JAVA_FLOAT           );
        types.put("JAVA.LANG.STRING"        , DataType.JAVA_STRING          );
        types.put("JAVA.UTIL.DATE"          , DataType.JAVA_DATE            );
        types.put("JAVA.SQL.DATE"           , DataType.JAVA_DATE            );
        types.put("JAVA.SQL.TIMESTAMP"      , DataType.JAVA_SQL_TIMESTAMP   );
        types.put("JAVA.SQL.TIME"           , DataType.JAVA_SQL_TIME        );
        types.put("JAVA.TIME.LOCALDATE"     , DataType.JAVA_LOCAL_DATE      );
        types.put("JAVA.TIME.LOCALTIME"     , DataType.JAVA_LOCAL_TIME      );
        types.put("JAVA.TIME.LOCALDATETIME" , DataType.JAVA_LOCAL_DATE_TIME );
    }




}
