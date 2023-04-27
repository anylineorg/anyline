package org.anyline.data.jdbc.postgresql;

import java.util.Hashtable;
import java.util.Map;

public class DataTypeAdapter extends org.anyline.data.adapter.DataTypeAdapter {
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

        types.put("BFILE"                   , DataType.ILLEGAL               ); //     ,  ,oracle,
        types.put("BINARY_DOUBLE"           , DataType.FLOAT8                ); //     ,  ,oracle,
        types.put("BINARY_FLOAT"            , DataType.FLOAT4                ); //     ,  ,oracle,
        types.put("BIGINT"                  , DataType.INT8                  ); //mysql,  ,      ,mssql,
        types.put("BIGSERIAL"               , DataType.BIGSERIAL             ); //     ,pg,
        types.put("BINARY"                  , DataType.BIT                   ); //mysql,  ,      ,mssql,
        types.put("BIT"                     , DataType.BIT                   ); //mysql,pg,      ,mssql,
        types.put("BLOB"                    , DataType.BYTEA                 ); //mysql,  ,oracle,     ,sqlite
        types.put("BOOL"                    , DataType.BIT                   ); //     ,pg
        types.put("BOX"                     , DataType.ILLEGAL               ); //     ,pg
        types.put("BYTEA"                   , DataType.BYTEA                 ); //     ,pg
        types.put("CHAR"                    , DataType.CHAR                  ); //mysql,pg,oracle,mssql,
        types.put("CIDR"                    , DataType.ILLEGAL               ); //      pg
        types.put("CIRCLE"                  , DataType.ILLEGAL               ); //      pg
        types.put("CLOB"                    , DataType.TEXT                  ); //     ,  ,oracle
        types.put("DATE"                    , DataType.DATE                  ); //mysql,pg,oracle,mssql
        types.put("DATETIME"                , DataType.TIMESTAMP             ); //mysql,  ,      ,mssql
        types.put("DATETIME2"               , DataType.TIMESTAMP             ); //mysql,  ,      ,mssql
        types.put("DATETIMEOFFSET"          , DataType.TIMESTAMP             ); //mysql,  ,      ,mssql
        types.put("DECIMAL"                 , DataType.DECIMAL               ); //mysql,pg,oracle,mssql
        types.put("DOUBLE"                  , DataType.DECIMAL               ); //mysql,
        types.put("ENUM"                    , DataType.ILLEGAL               ); //mysql,
        types.put("FLOAT"                   , DataType.FLOAT4                ); //mysql,  ,oracle,mssql
        types.put("FLOAT4"                  , DataType.FLOAT4                ); //     ,pg
        types.put("FLOAT8"                  , DataType.FLOAT8                ); //     ,pg
        types.put("GEOGRAPHY"               , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("GEOMETRY"                , DataType.ILLEGAL               ); //mysql
        types.put("GEOMETRYCOLLECTION"       , DataType.ILLEGAL               ); //mysql
        types.put("HIERARCHYID"             , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("IMAGE"                   , DataType.BYTEA                 ); //     ,  ,      ,mssql
        types.put("INET"                    , DataType.INET                  ); //     ,pg
        types.put("INTERVAL"                , DataType.INTERVAL              ); //     ,pg
        types.put("INT"                     , DataType.INT4                  ); //mysql,  ,      ,mssql,
        types.put("INT2"                    , DataType.INT2                  ); //     ,pg
        types.put("INT4"                    , DataType.INT4                  ); //
        types.put("INT8"                    , DataType.INT8                  ); //
        types.put("INTEGER"                 , DataType.INT4                  ); //mysql                 ,,sqlite
        types.put("JSON"                    , DataType.JSON                  ); //mysql,pg
        types.put("JSONB"                   , DataType.JSONB                 ); //     ,pg
        types.put("LINE"                    , DataType.LINE                  ); //mysql,pg
        types.put("LONG"                    , DataType.INT8                  ); //     ,  ,oracle
        types.put("LONGBLOB"                , DataType.BYTEA                 ); //mysql
        types.put("LONGTEXT"                , DataType.TEXT                  ); //mysql
        types.put("LSEG"                    , DataType.LSEG                  ); //     ,pg
        types.put("MACADDR"                 , DataType.MACADDR               ); //     ,pg
        types.put("MONEY"                   , DataType.MONEY                 ); //     ,pg,      ,mssql
        types.put("NUMBER"                  , DataType.DECIMAL               ); //     ,  ,oracle
        types.put("NCHAR"                   , DataType.VARCHAR               ); //     ,  ,oracle,mssql
        types.put("NCLOB"                   , DataType.BYTEA                 ); //     ,  ,oracle
        types.put("NTEXT"                   , DataType.TEXT                  ); //     ,  ,      ,mssql
        types.put("NVARCHAR"                , DataType.VARCHAR               ); //     ,  ,      ,mssql
        types.put("NVARCHAR2"               , DataType.VARCHAR               ); //     ,  ,oracle
        types.put("PATH"                    , DataType.PATH                  ); //     ,pg
        types.put("MEDIUMBLOB"              , DataType.BYTEA                 ); //mysql,
        types.put("MEDIUMINT"               , DataType.INT8                  ); //mysql,
        types.put("MEDIUMTEXT"              , DataType.TEXT                  ); //mysql,
        types.put("MULTILINESTRING"         , DataType.ILLEGAL               ); //mysql,
        types.put("MULTIPOINT"              , DataType.ILLEGAL               ); //mysql,
        types.put("MULTIPOLYGON"            , DataType.ILLEGAL               ); //mysql,
        types.put("NUMERIC"                 , DataType.DECIMAL               ); //mysql,  ,       ,mssql,sqlite
        types.put("POINT"                   , DataType.POINT                 ); //mysql,pg
        types.put("POLYGON"                 , DataType.POLYGON               ); //mysql,pg
        types.put("REAL"                    , DataType.FLOAT4                ); //mysql,  ,      ,mssql,sqlite
        types.put("RAW"                     , DataType.ILLEGAL               ); //     ,  ,oracle
        types.put("ROWID"                   , DataType.ILLEGAL               ); //     ,  ,oracle
        types.put("SERIAL"                  , DataType.SERIAL                ); //     ,pg,
        types.put("SERIAL2"                 , DataType.SERIAL2               ); //     ,pg,
        types.put("SERIAL4"                 , DataType.SERIAL4               ); //     ,pg,
        types.put("SERIAL8"                 , DataType.SERIAL8               ); //     ,pg,
        types.put("SET"                     , DataType.ILLEGAL               ); //mysql,
        types.put("SMALLDATETIME"           , DataType.TIMESTAMP             ); //     ,  ,      ,mssql
        types.put("SMALLMONEY"              , DataType.DECIMAL               ); //     ,  ,      ,mssql
        types.put("SMALLINT"                , DataType.INT2                  ); //mysql,
        types.put("SMALLSERIAL"             , DataType.SMALLSERIAL           ); //     ,pg,
        types.put("SQL_VARIANT"             , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("SYSNAME"                 , DataType.ILLEGAL               ); //     ,  ,      ,mssql
        types.put("TEXT"                    , DataType.TEXT                  ); //mysql,pg,      ,mssql,sqlite
        types.put("TIME"                    , DataType.TIME                  ); //mysql,pg,      ,mssql
        types.put("TIMEZ"                   , DataType.TIMEZ                 ); //     ,pg
        types.put("TIMESTAMP"               , DataType.TIMESTAMP             ); //mysql,pg,oracle,mssql
        types.put("TIMESTAMP_LOCAL_ZONE"    , DataType.TIMESTAMP_LOCAL_ZONE  ); //     ,pg
        types.put("TIMESTAMP_ZONE"          , DataType.TIMESTAMP_ZONE        ); //     ,pg
        types.put("TSQUERY"                 , DataType.TSQUERY               ); //     ,pg
        types.put("TSVECTOR"                , DataType.TSVECTOR              ); //     ,pg
        types.put("TXID_SNAPSHOT"           , DataType.TXID_SNAPSHOT         ); //     ,pg
        types.put("UNIQUEIDENTIFIER"        , DataType.ILLEGAL               ); //     ,  ，     ,mssql
        types.put("UUID"                    , DataType.UUID                  ); //     ,pg
        types.put("UROWID"                  , DataType.ILLEGAL               ); //     ,  ,oracle
        types.put("VARBIT"                  , DataType.VARBIT                ); //     ,pg
        types.put("TINYBLOB"                , DataType.BYTEA                 ); //mysql,
        types.put("TINYINT"                 , DataType.INT2                  ); //mysql,  ,      ,mssql
        types.put("TINYTEXT"                , DataType.TEXT                  ); //mysql,
        types.put("VARBINARY"               , DataType.VARBIT                ); //mysql,  ,      ,mssql
        types.put("VARCHAR"                 , DataType.VARCHAR               ); //mysql,pg,oracle,mssql
        types.put("VARCHAR2"                , DataType.VARCHAR               ); //     ,  ,oracle,
        types.put("XML"                     , DataType.XML                   ); //     ,pg，      ,mssql
        types.put("YEAR"                    , DataType.DATE                  ); //mysql,
    }
    
}
