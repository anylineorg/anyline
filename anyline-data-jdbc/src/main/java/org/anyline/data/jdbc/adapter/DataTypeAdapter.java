package org.anyline.data.jdbc.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.entity.Point;
import org.anyline.entity.mdtadata.ColumnType;
import org.anyline.entity.mdtadata.DataType;
import org.anyline.entity.mdtadata.JavaType;
import org.anyline.util.Base64Util;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class DataTypeAdapter {
    private static Map<String, DataType> types = new Hashtable<>();

    public DataType type(String type){
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
        // 如Long在mysql中用BigInt 在pg用中int8,需要在各自的types中分别put("long")put("bigint")put("int8"),MySQL中可以不put("bigint")因为父类中已经put了key与value一致的情况
        // 执行时通过value
        // 子类中把每种可能的key put进types并对应准确的类型
        // 父类中有的value不需要重复实现,需要重新实现的一般是与内置函数有关的类型(如to_date)

        // 以下按字母顺序 方便查找
        // 后面备注表示key在哪个数据库中使用过
        // 了类配置中:如果在当前数据库中没的再现的,应该把value换成当前数据库支持的正常的类型
        types.put("BFILE"                   , SQL_BFILE                 ); //        ,oracle,
        types.put("BINARY_DOUBLE"           , SQL_BINARY_DOUBLE         ); //        ,oracle,
        types.put("BINARY_FLOAT"            , SQL_BINARY_FLOAT          ); //        ,oracle,
        types.put("BIGINT"                  , SQL_BIGINT                ); //mysql,  ,      ,mssql
        types.put("BIGSERIAL"               , SQL_SERIAL8               ); //     ,pg,
        types.put("BINARY"                  , SQL_BINARY                ); //mysql,  ,      ,mssql
        types.put("BIT"                     , SQL_BIT                   ); //mysql,pg,
        types.put("BLOB"                    , SQL_BLOB                  ); //mysql,  ,oracle
        types.put("BOOL"                    , SQL_BOOL                  ); //     ,pg,
        types.put("BOX"                     , SQL_BOX                   ); //     ,pg,
        types.put("BYTEA"                   , SQL_BYTEA                 ); //     ,pg,
        types.put("CHAR"                    , SQL_CHAR                  ); //mysql,pg,      ,mssql
        types.put("CIDR"                    , SQL_CIDR                  ); //      pg,
        types.put("CIRCLE"                  , SQL_CIRCLE                ); //      pg,
        types.put("CLOB"                    , SQL_CLOB                  ); //        ,oracle,
        types.put("DATE"                    , SQL_DATE                  ); //mysql,pg,oracle,
        types.put("DATETIME"                , SQL_DATETIME              ); //mysql,
        types.put("DATETIME2"               , SQL_DATETIME2             ); //     ,   ,     ,mssql,
        types.put("DATETIMEOFFSET"          , SQL_DATETIMEOFFSET        ); //mysql,  ,      ,mssql
        types.put("DECIMAL"                 , SQL_DECIMAL               ); //mysql,  ,oracle,mssql
        types.put("DOUBLE"                  , SQL_DOUBLE                ); //mysql,
        types.put("ENUM"                    , SQL_ENUM                  ); //mysql,
        types.put("FLOAT"                   , SQL_FLOAT                 ); //mysql,  ,oracle,mssql,
        types.put("FLOAT4"                  , SQL_FLOAT4                ); //     ,pg,
        types.put("FLOAT8"                  , SQL_FLOAT8                ); //     ,pg,
        types.put("GEOGRAPHY"               , SQL_GEOGRAPHY             ); //     ,  ,      ,mssql
        types.put("GEOMETRY"                , SQL_GEOMETRY              ); //mysql,  ,      ,mssql
        types.put("GEOMETRYCOLLECTIO"       , SQL_GEOMETRYCOLLECTIO     ); //mysql,
        types.put("HIERARCHYID"             , SQL_HIERARCHYID           ); //     ,  ,      ,mssql
        types.put("IMAGE"                   , SQL_IMAGE                 ); //     ,  ,      ,mssql
        types.put("INET"                    , SQL_INET                  ); //     ,pg,
        types.put("INTERVAL"                , SQL_INTERVAL              ); //     ,pg,
        types.put("INT"                     , SQL_INT                   ); //mysql,  ,      ,mssql,
        types.put("INT2"                    , SQL_INT2                  ); //     ,pg,
        types.put("INT4"                    , SQL_INT4                  ); //
        types.put("INT8"                    , SQL_INT8                  ); //
        types.put("INTEGER"                 , SQL_INTEGER               ); //mysql,
        types.put("JSON"                    , SQL_JSON                  ); //mysql,pg,
        types.put("JSONB"                   , SQL_JSONB                 ); //     ,pg,
        types.put("LINE"                    , SQL_LINE                  ); //mysql,pg,
        types.put("LSEG"                    , SQL_LSEG                  ); //     ,pg,
        types.put("MACADDR"                 , SQL_MACADDR               ); //     ,pg,
        types.put("MONEY"                   , SQL_MONEY                 ); //     ,pg,      ,mssql
        types.put("NUMBER"                  , SQL_NUMBER                ); //     ,  ,oracle,
        types.put("NCHAR"                   , SQL_NCHAR                 ); //     ,  ,oracle,
        types.put("NCLOB"                   , SQL_NCLOB                 ); //     ,  ,oracle,
        types.put("NVARCHAR2"               , SQL_NVARCHAR2             ); //     ,  ,oracle,
        types.put("PATH"                    , SQL_PATH                  ); //     ,pg,
        types.put("LONG"                    , SQL_LONG                  ); //     ,  ,oracle,
        types.put("LONGBLOB"                , SQL_LONGBLOB              ); //mysql,
        types.put("LONGTEXT"                , SQL_LONGTEXT              ); //mysql,
        types.put("MEDIUMBLOB"              , SQL_MEDIUMBLOB            ); //mysql,
        types.put("MEDIUMINT"               , SQL_MEDIUMINT             ); //mysql,
        types.put("MEDIUMTEXT"              , SQL_MEDIUMTEXT            ); //mysql,
        types.put("MULTILINESTRING"         , SQL_MULTILINESTRING       ); //mysql,
        types.put("MULTIPOINT"              , SQL_MULTIPOINT            ); //mysql,
        types.put("MULTIPOLYGON"            , SQL_MULTIPOLYGON          ); //mysql,
        types.put("NUMERIC"                 , SQL_NUMERIC               ); //mysql,
        types.put("POINT"                   , SQL_POINT                 ); //mysql,pg,
        types.put("POLYGON"                 , SQL_POLYGON               ); //mysql,pg,
        types.put("REAL"                    , SQL_REAL                  ); //mysql,
        types.put("RAW"                     , SQL_RAW                   ); //     ,  ,oracle,
        types.put("ROWID"                   , SQL_ROWID                 ); //     ,  ,oracle,
        types.put("SERIAL"                  , SQL_SERIAL                ); //     ,pg,
        types.put("SERIAL2"                 , SQL_SERIAL2               ); //     ,pg,
        types.put("SERIAL4"                 , SQL_SERIAL4               ); //     ,pg,
        types.put("SERIAL8"                 , SQL_SERIAL8               ); //     ,pg,
        types.put("SET"                     , SQL_SET                   ); //mysql,
        types.put("SMALLINT"                , SQL_SMALLINT              ); //mysql,
        types.put("SMALSERIAL"              , SQL_SERIAL2               ); //     ,pg,
        types.put("TEXT"                    , SQL_TEXT                  ); //mysql,pg,
        types.put("TIME"                    , SQL_TIME                  ); //mysql,pg,
        types.put("TIMEZ"                   , SQL_TIMEZ                 ); //     ,pg,
        types.put("TIMESTAMP"               , SQL_TIMESTAMP             ); //mysql,pg,oracle,
        types.put("TIMESTAMP_LOCAL_ZONE"    , SQL_TIMESTAMP_LOCAL_ZONE  ); //     ,pg,
        types.put("TIMESTAMP_ZONE"          , SQL_TIMESTAMP_ZONE        ); //     ,pg,
        types.put("TSQUERY"                 , SQL_TSQUERY               ); //     ,pg,
        types.put("TSVECTOR"                , SQL_TSVECTOR              ); //     ,pg,
        types.put("TXID_SNAPSHOT"           , SQL_TXID_SNAPSHOT         ); //     ,pg,
        types.put("UUID"                    , SQL_UUID                  ); //     ,pg,
        types.put("UROWID"                  , SQL_UROWID                ); //     ,  ,oracle,
        types.put("VARBIT"                  , SQL_VARBIT                ); //     ,pg,
        types.put("TINYBLOB"                , SQL_TINYBLOB              ); //mysql,
        types.put("TINYINT"                 , SQL_TINYINT               ); //mysql,
        types.put("TINYTEXT"                , SQL_TINYTEXT              ); //mysql,
        types.put("VARBINARY"               , SQL_VARBINARY             ); //mysql,
        types.put("VARCHAR"                 , SQL_VARCHAR               ); //mysql,pg,oracle,
        types.put("VARCHAR2"                , SQL_VARCHAR2              ); //     ,  ,oracle,
        types.put("XML"                     , SQL_XML                   ); //     ,pg,
        types.put("YEAR"                    , SQL_YEAR                  ); //mysql,


        types.put("JAVA.MATH.DECIMAL"              , SQL_DECIMAL        );
        types.put("JAVA.LANG.DOUBLE"               , SQL_DOUBLE         );
        types.put("JAVA.LANG.BOOLEAN"              , SQL_BOOL           );
        types.put("JAVA.LANG.INTEGER"              , SQL_INTEGER        );
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


    /* *****************************************************************************************************************
     *
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

    /* *********************************************************************************************************************************
     *
     *                                              String
     *
     * *********************************************************************************************************************************/
    protected DataType SQL_CHAR              = new ColumnType() {public String getName(){return "CHAR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
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
    };  //mysql,pg

    protected DataType SQL_NCHAR             = new ColumnType() {public String getName(){return "NCHAR";}               public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //     ,  ,oracle
    protected DataType SQL_CLOB              = new ColumnType() {public String getName(){return "CLOB";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //        ,oracle

    protected DataType SQL_NCLOB             = new ColumnType() {public String getName(){return "NCLOB";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //     ,  ,oracle
    protected DataType SQL_NVARCHAR2         = new ColumnType() {public String getName(){return "NVARCHAR2";}           public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //     ,  ,oracle
    protected DataType SQL_LONGTEXT          = new ColumnType() {public String getName(){return "LONGTEXT";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //mysql
    protected DataType SQL_MEDIUMTEXT        = new ColumnType() {public String getName(){return "MEDIUMTEXT";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //mysql
    protected DataType SQL_MULTILINESTRING   = new ColumnType() {public String getName(){return "MULTILINESTRING";}     public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //mysql

    protected DataType SQL_TEXT              = new ColumnType() {public String getName(){return "TEXT";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //mysql,pg

    protected DataType SQL_TINYTEXT          = new ColumnType() {public String getName(){return "TINYTEXT";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_CHAR.write(value, def, placeholder);}
    };  //mysql
    protected DataType SQL_VARCHAR           = new ColumnType() {public String getName(){return "VARCHAR";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_CHAR.write(value, def, placeholder);
        }
    };  //mysql,pg,oracle
    protected DataType SQL_VARCHAR2          = new ColumnType() {public String getName(){return "VARCHAR2";}            public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_CHAR.write(value, def, placeholder);
        }
    };  //     ,  ,oracle
    protected DataType SQL_UUID              = new ColumnType() {public String getName(){return "UUID";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
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
    };  //     ,pg
    /* *********************************************************************************************************************************
     *
     *                                              String-format
     *
     * *********************************************************************************************************************************/

    protected DataType SQL_JSON              = new ColumnType() {public String getName(){return "JSON";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){
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
                        value = DataSet.parseJson(node);
                    } else {
                        value = DataRow.parseJson(node);
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
            return value;
        }
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql,pg
    protected DataType SQL_JSONB             = new ColumnType() {public String getName(){return "JSONB";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_XML               = new ColumnType() {public String getName(){return "XML";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    /* *********************************************************************************************************************************
     *
     *                                              number-int/long
     *
     * *********************************************************************************************************************************/

    protected DataType SQL_SHORT               = new ColumnType() {public String getName(){return "SHORT";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Short result = BasicUtil.parseShort(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseShort(def, null);
            }
            return result;
        }
    };
    protected DataType SQL_INT               = new ColumnType() {public String getName(){return "INT";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Integer result = BasicUtil.parseInt(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseInt(def, null);
            }
            return result;
        }
    };  //mysql
    protected DataType SQL_LONG               = new ColumnType() {public String getName(){return "LONG";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Long result = BasicUtil.parseLong(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseLong(def, null);
            }
            return result;
        }
    };  //oracle
    protected DataType SQL_SERIAL               = new ColumnType() {public String getName(){return "SERIAL";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_LONG.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_SERIAL2               = new ColumnType() {public String getName(){return "SERIAL2";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_LONG.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_SERIAL4               = new ColumnType() {public String getName(){return "SERIAL4";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_LONG.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_SERIAL8               = new ColumnType() {public String getName(){return "SERIAL8";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_LONG.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_BIGERIAL               = new ColumnType() {public String getName(){return "BIGSERIAL";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_LONG.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_INT2               = new ColumnType() {public String getName(){return "INT2";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_SHORT.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_INT4               = new ColumnType() {public String getName(){return "INT4";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_INT.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_INT8               = new ColumnType() {public String getName(){return "INT8";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_LONG.write(value, def, placeholder);
        }
    };  //
    protected DataType SQL_BIT               = new ColumnType() {public String getName(){return "BIT";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if("0".equals(value.toString()) || "false".equalsIgnoreCase(value.toString())){
                value = 0;
            }else{
                value = 1;
            }
            return value;
        }
    };  //mysql,pg
    protected DataType SQL_BIGINT            = new ColumnType() {public String getName(){return "BIGINT";}              public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_LONG.write(value, def, placeholder);
        }
    };  //mysql

    protected DataType SQL_MEDIUMINT         = new ColumnType() {public String getName(){return "MEDIUMINT";}           public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_INT.write(value, def, placeholder);}
    };  //mysql

    protected DataType SQL_INTEGER           = new ColumnType() {public String getName(){return "INTEGER";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_INT.write(value, def, placeholder);}
    };  //mysql
    protected DataType SQL_SMALLINT          = new ColumnType() {public String getName(){return "SMALLINT";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_INT.write(value, def, placeholder);}
    };  //mysql
    protected DataType SQL_TINYINT           = new ColumnType() {public String getName(){return "TINYINT";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_INT.write(value, def, placeholder);}
    };  //mysql
    /* *********************************************************************************************************************************
     *
     *                                              number-double/float
     *
     * *********************************************************************************************************************************/

    protected DataType SQL_DECIMAL           = new ColumnType() {public String getName(){return "DECIMAL";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
        public Object read(Object value, Class clazz){return value;}
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
    }; //mysql,  ,oracle

    protected DataType SQL_DOUBLE            = new ColumnType() {public String getName(){return "DOUBLE";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
        public Object read(Object value, Class clazz){return value;}
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
    }; //mysql

    protected DataType SQL_FLOAT             = new ColumnType() {public String getName(){return "FLOAT";}               public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
        public Object read(Object value, Class clazz){return value;}
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
    }; //mysql,  ,oracle

    protected DataType SQL_FLOAT4             = new ColumnType() {public String getName(){return "FLOAT4";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_FLOAT.write(value, def, placeholder);
        }
    }; //    ,pg  ,

    protected DataType SQL_FLOAT8             = new ColumnType() {public String getName(){return "FLOAT8";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_DOUBLE.write(value, def, placeholder);
        }
    }; //    ,pg  ,

    protected DataType SQL_BINARY_DOUBLE     = new ColumnType() {public String getName(){return "BINARY_DOUBLE";}       public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_DOUBLE.write(value, def, placeholder);}
    };  //        ,oracle
    protected DataType SQL_BINARY_FLOAT      = new ColumnType() {public String getName(){return "BINARY_FLOAT";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_FLOAT.write(value, def, placeholder);}
    };  //        ,oracle
    protected DataType SQL_MONEY             = new ColumnType() {public String getName(){return "MONEY";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_DECIMAL.write(value, def, placeholder);
        }
    };  //     ,pg
    protected DataType SQL_NUMERIC            = new ColumnType() {public String getName(){return "NUMERIC";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_DECIMAL.write(value, def, placeholder);
        }
    };  //mysql
    protected DataType SQL_NUMBER            = new ColumnType() {public String getName(){return "NUMBER";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_DECIMAL.write(value, def, placeholder);
        }
    }; //     ,  ,oracle

    protected DataType SQL_REAL              = new ColumnType() {public String getName(){return "REAL";}                public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_FLOAT.write(value, def, placeholder);}
    };  //mysql
    /* *********************************************************************************************************************************
     *
     *                                              date
     *
     * *********************************************************************************************************************************/
    //TODO         write 需要根据数据库类型 由内置函数转换
    protected DataType SQL_DATE              = new ColumnType() {public String getName(){return "DATE";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
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
    };  //mysql,pg,oracle
    protected DataType SQL_DATETIME          = new ColumnType() {public String getName(){return "DATETIME";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
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

    protected DataType SQL_DATETIME2          = new ColumnType() {public String getName(){return "DATETIME2";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_DATETIME.write(value, def, placeholder);
        }
    };  //mssql
    protected DataType SQL_DATETIMEOFFSET     = new ColumnType() {public String getName(){return "DATETIMEOFFSET";}     public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_DATETIME.write(value, def, placeholder);
        }
    };  //mssql

    protected DataType SQL_TIME              = new ColumnType() {public String getName(){return "TIME";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
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
    };  //mysql,pg
    protected DataType SQL_TIMEZ             = new ColumnType() {public String getName(){return "TIMEZ";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_TIMESTAMP         = new ColumnType() {public String getName(){return "TIMESTAMP";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            return SQL_DATETIME.write(value, def, placeholder);
        }
    };;  //mysql,pg,oracle
    protected DataType SQL_TIMESTAMP_ZONE    = new ColumnType() {public String getName(){return "TIMESTAMP";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_TIMESTAMP_LOCAL_ZONE= new ColumnType() {public String getName(){return "TIMESTAMP";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg

    protected DataType SQL_YEAR              = new ColumnType() {public String getName(){return "YEAR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_DATE.write(value, def, placeholder);}
    };  //mysql
    /* *********************************************************************************************************************************
     *
     *                                              byte[]
     *
     * *********************************************************************************************************************************/

    protected DataType SQL_BLOB              = new ColumnType() {public String getName(){return "BLOB";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
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
    };  //mysql,  ,oracle

    protected DataType SQL_LONGBLOB          = new ColumnType() {public String getName(){return "LONGBLOB";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_BLOB.write(value, def, placeholder);}
    };  //mysql

    protected DataType SQL_MEDIUMBLOB        = new ColumnType() {public String getName(){return "MEDIUMBLOB";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_BLOB.write(value, def, placeholder);}
    };  //mysql
    protected DataType SQL_TINYBLOB          = new ColumnType() {public String getName(){return "TINYBLOB";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return SQL_BLOB.write(value, def, placeholder);}
    };  //mysql
    /* *********************************************************************************************************************************
     *
     *                                              byte[]-file
     *
     * *********************************************************************************************************************************/
    protected DataType SQL_IMAGE = new ColumnType() {public String getName(){return "IMAGE";}  public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mssql
    /* *********************************************************************************************************************************
     *
     *                                              byte[]-geometry
     *
     * *********************************************************************************************************************************/

    protected DataType SQL_POINT             = new ColumnType() {public String getName(){return "POINT";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){
            if(null == value){
                return value;
            }
            Point point = BasicUtil.parsePoint(value);
            if(null == clazz){
                value = point;
            }else if(null != point){
                if (clazz == Point.class) {
                    value = point;
                } else if (clazz == double[].class) {
                    value = BeanUtil.Double2double(point.getArray(), 0);
                } else if (clazz == Double[].class) {
                    value = point.getArray();
                } else if (clazz == byte[].class) {
                    value = point.bytes();
                }
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
            if(value instanceof Point){
                value = ((Point)value).bytes();
            }else if(value instanceof double[]){
                double[] ds = (double[]) value;
                if(ds.length == 2){
                    if (ds.length >= 2) {
                        value = new Point(ds[0], ds[1]).bytes();
                    }
                }
            }else if(value instanceof Double[]){
                Double[] ds = (Double[]) value;
                if(ds.length == 2 && null != ds[0] && null != ds[1]){
                    value = new Point(ds[0], ds[1]).bytes();
                }
            }
            return value;
        }
    };  //mysql,pg

    protected DataType SQL_MULTIPOLYGON      = new ColumnType() {public String getName(){return "MULTIPOLYGON";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql
    protected DataType SQL_MULTIPOINT        = new ColumnType() {public String getName(){return "MULTIPOINT";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql
    protected DataType SQL_POLYGON           = new ColumnType() {public String getName(){return "POLYGON";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql,pg

    protected DataType SQL_GEOMETRY          = new ColumnType() {public String getName(){return "GEOMETRY";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql
    protected DataType SQL_GEOMETRYCOLLECTIO = new ColumnType() {public String getName(){return "GEOMETRYCOLLECTION";}  public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql
    protected DataType SQL_HIERARCHYID = new ColumnType() {public String getName(){return "HIERARCHYID";}  public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mssql

    protected DataType SQL_LINE              = new ColumnType() {public String getName(){return "LINE";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql,pg
    protected DataType SQL_LSEG              = new ColumnType() {public String getName(){return "LSEG";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg

    protected DataType SQL_GEOGRAPHY              = new ColumnType() {public String getName(){return "GEOGRAPHY";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,mssql
























    protected DataType SQL_BFILE             = new ColumnType() {public String getName(){return "BFILE";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //        ,oracle


    protected DataType SQL_BINARY            = new ColumnType() {public String getName(){return "BINARY";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql
    protected DataType SQL_BOOL              = new ColumnType() {public String getName(){return "BOOL";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_BOX               = new ColumnType() {public String getName(){return "BOX";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_BYTEA             = new ColumnType() {public String getName(){return "BYTEA";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg

    protected DataType SQL_CIDR              = new ColumnType() {public String getName(){return "CIDR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //      pg
    protected DataType SQL_CIRCLE            = new ColumnType() {public String getName(){return "CIRCLE";}              public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //      pg
    protected DataType SQL_ENUM              = new ColumnType() {public String getName(){return "ENUM";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql
    protected DataType SQL_INET              = new ColumnType() {public String getName(){return "INET";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_INTERVAL          = new ColumnType() {public String getName(){return "INTERVAL";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_MACADDR           = new ColumnType() {public String getName(){return "MACADDR";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_PATH              = new ColumnType() {public String getName(){return "PATH";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg

    protected DataType SQL_RAW               = new ColumnType() {public String getName(){return "RAW";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,  ,oracle
    protected DataType SQL_ROWID             = new ColumnType() {public String getName(){return "ROWID";}               public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,  ,oracle
    protected DataType SQL_SET               = new ColumnType() {public String getName(){return "SET";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql
    protected DataType SQL_TSQUERY           = new ColumnType() {public String getName(){return "TSQUERY";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_TSVECTOR          = new ColumnType() {public String getName(){return "TSVECTOR";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_TXID_SNAPSHOT     = new ColumnType() {public String getName(){return "TXID_SNAPSHOT";}       public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_UROWID            = new ColumnType() {public String getName(){return "UROWID";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,  ,oracle

    protected DataType SQL_VARBIT            = new ColumnType() {public String getName(){return "VARBIT";}              public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //     ,pg
    protected DataType SQL_VARBINARY         = new ColumnType() {public String getName(){return "VARBINARY";}           public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){return value;}
    };  //mysql

























    /* *****************************************************************************************************************
     *
     * String
     * number-int/long
     * number-double/float
     * date
     * byte[]
     * byte[]-geometry
     *
     ******************************************************************************************************************/

    /* *********************************************************************************************************************************
     *
     *                                              String
     *
     * *********************************************************************************************************************************/
    protected DataType JAVA_STRING              = new JavaType() {public Class getJavaClass(){return String.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return false;}public boolean isIgnoreScale() {return true;}

        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            if(value instanceof String){
                String str = (String)value;
                if(str.startsWith("${") && str.endsWith("}")){
                    value = str.substring(2, str.length()-1);
                }
            }else if(value instanceof Date){
                value = DateUtil.format((Date)value);
            }else{
                value = value.toString();
            }
            if(null != value) {
                if (!placeholder) {
                    String str = value.toString();
                    if (str.startsWith("'") && str.endsWith("'")){
                    }else{
                        value = "'" + str + "'";
                    }
                }
            }
            return value;
        }


    };
    protected DataType JAVA_BOOLEAN             = new JavaType() {public Class getJavaClass(){return Boolean.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Boolean result =  BasicUtil.parseBoolean(value, null);
            if(null != def && null == result){
                result =  BasicUtil.parseBoolean(def, null);
            }
            return result;
        }
    };
    protected DataType JAVA_INTEGER             = new JavaType() {public Class getJavaClass(){return Integer.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Integer result = BasicUtil.parseInt(value, null);
            if(null == value){
                result = BasicUtil.parseInt(def, null);
            }
            return result;
        }
    };
    protected DataType JAVA_LONG             = new JavaType() {public Class getJavaClass(){return Long.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            Long result = BasicUtil.parseLong(value, null);
            if(null != def && null == result){
                result = BasicUtil.parseLong(def, null);
            }
            return result;
        }
    };
    protected DataType JAVA_FLOAT             = new JavaType() {public Class getJavaClass(){return Float.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return false;}public boolean isIgnoreScale() {return false;}
        public Object read(Object value, Class clazz){return value;}
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
    };
    protected DataType JAVA_DOUBLE             = new JavaType() {public Class getJavaClass(){return Long.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return false;}public boolean isIgnoreScale() {return false;}
        public Object read(Object value, Class clazz){return value;}
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
    };
    protected DataType JAVA_DECIMAL             = new JavaType() {public Class getJavaClass(){return BigDecimal.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return false;}public boolean isIgnoreScale() {return false;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(null == value){
                value = def;
            }
            BigDecimal result =  BasicUtil.parseDecimal(value, null);
            if(null != def && null == result){
                result =  BasicUtil.parseDecimal(def, null);
            }
            return result;
        }
    };
    protected DataType JAVA_SQL_TIMESTAMP             = new JavaType() {public Class getJavaClass(){return java.sql.Timestamp.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            Date date = DateUtil.parse(value);
            if(null != date) {
                if(placeholder){
                    value = new java.sql.Timestamp(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date) + "'";
                }
            }
            return value;
        }
    };
    protected DataType JAVA_SQL_TIME             = new JavaType() {public Class getJavaClass(){return java.sql.Time.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            Date date = DateUtil.parse(value);
            if(null != date) {
                if(placeholder){
                    value = new java.sql.Time(date.getTime());
                }else{
                    value = "'" + DateUtil.format(date, "HH:mm:ss") + "'";
                }
            }
            return value;
        }
    };

    protected DataType JAVA_DATE             = new JavaType() {public Class getJavaClass(){return java.util.Date.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Timestamp(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    };
    protected DataType JAVA_SQL_DATE             = new JavaType() {public Class getJavaClass(){return java.sql.Date.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Date(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
                    }
                }
            }
            return value;
        }
    };
    protected DataType JAVA_LOCAL_DATE             = new JavaType() {public Class getJavaClass(){return LocalDate.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Date(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd") + "'";
                    }
                }
            }
            return value;
        }
    };
    protected DataType JAVA_LOCAL_TIME             = new JavaType() {public Class getJavaClass(){return LocalTime.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Time(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    };
    protected DataType JAVA_LOCAL_DATE_TIME             = new JavaType() {public Class getJavaClass(){return LocalDateTime.class;}public String getName() {return null;}public boolean isIgnorePrecision() {return true;}public boolean isIgnoreScale() {return true;}
        public Object read(Object value, Class clazz){return value;}
        public Object write(Object value, Object def, boolean placeholder){
            if(value instanceof Time){
            }else {
                Date date = DateUtil.parse(value);
                if(null != date) {
                    if(placeholder){
                        value = new java.sql.Timestamp(date.getTime());
                    }else{
                        value = "'" + DateUtil.format(date, "yyyy-MM-dd HH:mm:ss") + "'";
                    }
                }
            }
            return value;
        }
    };





}
