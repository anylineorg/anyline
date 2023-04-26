package org.anyline.data.jdbc.oracle;

import java.util.Hashtable;
import java.util.Map;

public class DataTypeAdapter extends org.anyline.data.jdbc.adapter.DataTypeAdapter {
    private static Map<String, DataType> types = new Hashtable<>();
    
    public org.anyline.entity.metadata.DataType type(String type){
        if(null != type){
            org.anyline.entity.metadata.DataType dt = types.get(type.toUpperCase());
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
        // 执行时通过value
        // 子类中把每种可能的key put进types并对应准确的类型(子类自己创建一个enum 保证value只能自己支持的类型)
        // 父类enum中有的value,子类enum不需要重复实现,需要重新实现的一般是与内置函数有关的类型(如to_date)

        // 以下按字母顺序 方便查找
        // 后面备注表示key在哪个数据库中使用过
        //以下所有的key每个子类中保持完全一致,有不支持的put DataType.

        types.put("BFILE"                   , DataType.BFILE                 ); //     ,  ,oracle,
        types.put("BINARY_DOUBLE"           , DataType.BINARY_DOUBLE         ); //     ,  ,oracle,
        types.put("BINARY_FLOAT"            , DataType.FLOAT                 ); //     ,  ,oracle,
        types.put("BIGINT"                  , DataType.NUMBER                ); //mysql,  ,      ,mssql,
        types.put("BIGSERIAL"               , DataType.NUMBER                ); //     ,pg,
        types.put("BINARY"                  , DataType.BLOB                  ); //mysql,  ,      ,mssql,
        types.put("BIT"                     , DataType.NUMBER                ); //mysql,pg,      ,mssql,
        types.put("BLOB"                    , DataType.BLOB                  ); //mysql,  ,oracle,     ,sqlite
        types.put("BOOL"                    , DataType.NUMBER                ); //     ,pg
        types.put("BOX"                     , DataType.ILLEGAL               ); //     ,pg
        types.put("BYTEA"                   , DataType.BLOB                  ); //     ,pg
        types.put("CHAR"                    , DataType.CHAR                  ); //mysql,pg,oracle,mssql,
        types.put("CIDR"                    , DataType.ILLEGAL               ); //      pg
        types.put("CIRCLE"                  , DataType.ILLEGAL               ); //      pg
        types.put("CLOB"                    , DataType.CLOB                  ); //     ,  ,oracle
        types.put("DATE"                    , DataType.DATE                  ); //mysql,pg,oracle,mssql
        types.put("DATETIME"                , DataType.TIMESTAMP             ); //mysql,  ,      ,mssql
        types.put("DATETIME2"               , DataType.TIMESTAMP             ); //mysql,  ,      ,mssql
        types.put("DATETIMEOFFSET"          , DataType.TIMESTAMP             ); //mysql,  ,      ,mssql
        types.put("DECIMAL"                 , DataType.NUMBER                ); //mysql,pg,oracle,mssql
        types.put("DOUBLE"                  , DataType.NUMBER                ); //mysql,
        types.put("ENUM"                    , DataType.ILLEGAL               ); //mysql,
        types.put("FLOAT"                   , DataType.FLOAT                 ); //mysql,  ,oracle,mssql
        types.put("FLOAT4"                  , DataType.FLOAT                 ); //     ,pg
        types.put("FLOAT8"                  , DataType.FLOAT                 ); //     ,pg
        types.put("GEOGRAPHY"               , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("GEOMETRY"                , DataType.ILLEGAL               ); //mysql
        types.put("GEOMETRYCOLLECTION"       , DataType.ILLEGAL               ); //mysql
        types.put("HIERARCHYID"             , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("IMAGE"                   , DataType.BLOB                  ); //     ,  ,      ,mssql
        types.put("INET"                    , DataType.ILLEGAL               ); //     ,pg
        types.put("INTERVAL"                , DataType.ILLEGAL               ); //     ,pg
        types.put("INT"                     , DataType.NUMBER                ); //mysql,  ,      ,mssql,
        types.put("INT2"                    , DataType.NUMBER                ); //     ,pg
        types.put("INT4"                    , DataType.NUMBER                ); //     ,pg
        types.put("INT8"                    , DataType.NUMBER                ); //     ,pg
        types.put("INTEGER"                 , DataType.NUMBER                ); //mysql                 ,sqlite
        types.put("JSON"                    , DataType.CLOB                  ); //mysql,pg
        types.put("JSONB"                   , DataType.BLOB                  ); //     ,pg
        types.put("LINE"                    , DataType.ILLEGAL               ); //mysql,pg
        types.put("LONG"                    , DataType.NUMBER                ); //     ,  ,oracle
        types.put("LONGBLOB"                , DataType.BLOB                  ); //mysql
        types.put("LONGTEXT"                , DataType.CLOB                  ); //mysql
        types.put("LSEG"                    , DataType.ILLEGAL               ); //     ,pg
        types.put("MACADDR"                 , DataType.ILLEGAL               ); //     ,pg
        types.put("MONEY"                   , DataType.NUMBER                ); //     ,pg,      ,mssql
        types.put("NUMBER"                  , DataType.NUMBER                ); //     ,  ,oracle
        types.put("NCHAR"                   , DataType.NCHAR                 ); //     ,  ,oracle,mssql
        types.put("NCLOB"                   , DataType.NCLOB                 ); //     ,  ,oracle
        types.put("NTEXT"                   , DataType.NCLOB                 ); //     ,  ,      ,mssql
        types.put("NVARCHAR"                , DataType.NVARCHAR2             ); //     ,  ,      ,mssql
        types.put("NVARCHAR2"               , DataType.NVARCHAR2             ); //     ,  ,oracle
        types.put("PATH"                    , DataType.ILLEGAL               ); //     ,pg
        types.put("MEDIUMBLOB"              , DataType.BLOB                  ); //mysql,
        types.put("MEDIUMINT"               , DataType.NUMBER                ); //mysql,
        types.put("MEDIUMTEXT"              , DataType.CLOB                  ); //mysql,
        types.put("MULTILINESTRING"         , DataType.ILLEGAL               ); //mysql,
        types.put("MULTIPOINT"              , DataType.ILLEGAL               ); //mysql,
        types.put("MULTIPOLYGON"            , DataType.ILLEGAL               ); //mysql,
        types.put("NUMERIC"                 , DataType.NUMBER                ); //mysql,  ,       ,mssql,sqlite
        types.put("POINT"                   , DataType.ILLEGAL               ); //mysql,pg
        types.put("POLYGON"                 , DataType.ILLEGAL               ); //mysql,pg
        types.put("REAL"                    , DataType.FLOAT                 ); //mysql,  ,      ,mssql,sqlite
        types.put("RAW"                     , DataType.RAW                   ); //     ,  ,oracle
        types.put("ROWID"                   , DataType.ROWID                 ); //     ,  ,oracle
        types.put("SERIAL"                  , DataType.NUMBER                ); //     ,pg,
        types.put("SERIAL2"                 , DataType.NUMBER                ); //     ,pg,
        types.put("SERIAL4"                 , DataType.NUMBER                ); //     ,pg,
        types.put("SERIAL8"                 , DataType.NUMBER                ); //     ,pg,
        types.put("SET"                     , DataType.ILLEGAL               ); //mysql,
        types.put("SMALLDATETIME"           , DataType.TIMESTAMP             ); //     ,  ,      ,mssql
        types.put("SMALLMONEY"              , DataType.NUMBER                ); //     ,  ,      ,mssql
        types.put("SMALLINT"                , DataType.NUMBER                ); //mysql,
        types.put("SMALLSERIAL"             , DataType.NUMBER                ); //     ,pg,
        types.put("SQL_VARIANT"             , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("SYSNAME"                 , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("TEXT"                    , DataType.CLOB                  ); //mysql,pg,      ,mssql,sqlite
        types.put("TIME"                    , DataType.TIMESTAMP             ); //mysql,pg,      ,mssql
        types.put("TIMEZ"                   , DataType.TIMESTAMP             ); //     ,pg
        types.put("TIMESTAMP"               , DataType.TIMESTAMP             ); //mysql,pg,oracle,mssql
        types.put("TIMESTAMP_LOCAL_ZONE"    , DataType.TIMESTAMP             ); //     ,pg
        types.put("TIMESTAMP_ZONE"          , DataType.TIMESTAMP             ); //     ,pg
        types.put("TSQUERY"                 , DataType.ILLEGAL               ); //     ,pg
        types.put("TSVECTOR"                , DataType.ILLEGAL               ); //     ,pg
        types.put("TXID_SNAPSHOT"           , DataType.ILLEGAL               ); //     ,pg
        types.put("UNIQUEIDENTIFIER"        , DataType.ILLEGAL               ); //     ,  ，     ,mssql
        types.put("UUID"                    , DataType.ILLEGAL               ); //     ,pg
        types.put("UROWID"                  , DataType.UROWID                ); //     ,  ,oracle
        types.put("VARBIT"                  , DataType.BLOB                  ); //     ,pg
        types.put("TINYBLOB"                , DataType.BLOB                  ); //mysql,
        types.put("TINYINT"                 , DataType.NUMBER                ); //mysql,  ,      ,mssql
        types.put("TINYTEXT"                , DataType.CLOB                  ); //mysql,
        types.put("VARBINARY"               , DataType.BLOB                  ); //mysql,  ,      ,mssql
        types.put("VARCHAR"                 , DataType.VARCHAR               ); //mysql,pg,oracle,mssql
        types.put("VARCHAR2"                , DataType.VARCHAR               ); //     ,  ,oracle,
        types.put("XML"                     , DataType.ILLEGAL               ); //     ,pg，      ,mssql
        types.put("YEAR"                    , DataType.DATE                  ); //mysql,


    }
    
}
