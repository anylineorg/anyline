package org.anyline.data.entity;

import org.anyline.entity.Point;
import org.anyline.entity.mdtadata.ColumnType;
import org.anyline.entity.mdtadata.JavaType;
import org.anyline.util.Base64Util;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.DateUtil;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

public class ColumnTypeHolder {
    private static Map<String, ColumnType> types = new Hashtable<>();
    public static Map<String, ColumnType> types(){
        return types;
    }
    public static ColumnType type(String type){
        if(null != type){
            return types.get(type);
        }else{
            return null;
        }
    }
    static {

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
        ColumnType CHAR              = new ColumnType() {public String getName(){return"CHAR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
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

        ColumnType NCHAR             = new ColumnType() {public String getName(){return"NCHAR";}               public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //     ,  ,oracle
        ColumnType CLOB              = new ColumnType() {public String getName(){return"CLOB";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //        ,oracle

        ColumnType NCLOB             = new ColumnType() {public String getName(){return"NCLOB";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //     ,  ,oracle
        ColumnType NVARCHAR2         = new ColumnType() {public String getName(){return"NVARCHAR2";}           public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //     ,  ,oracle
        ColumnType LONGTEXT          = new ColumnType() {public String getName(){return"LONGTEXT";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //mysql
        ColumnType MEDIUMTEXT        = new ColumnType() {public String getName(){return"MEDIUMTEXT";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //mysql
        ColumnType MULTILINESTRING   = new ColumnType() {public String getName(){return"MULTILINESTRING";}     public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //mysql

        ColumnType TEXT              = new ColumnType() {public String getName(){return"TEXT";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //mysql,pg

        ColumnType TINYTEXT          = new ColumnType() {public String getName(){return"TINYTEXT";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return CHAR.write(value, def, placeholder);}
        };  //mysql
        ColumnType VARCHAR           = new ColumnType() {public String getName(){return"VARCHAR";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                return CHAR.write(value, def, placeholder);
            }
        };  //mysql,pg,oracle
        ColumnType VARCHAR2          = new ColumnType() {public String getName(){return"VARCHAR2";}            public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                return CHAR.write(value, def, placeholder);
            }
        };  //     ,  ,oracle
        ColumnType UUID              = new ColumnType() {public String getName(){return"UUID";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                if(null != value){
                    value = java.util.UUID.fromString(value.toString());
                }
                return value;
            }
        };  //     ,pg
        /* *********************************************************************************************************************************
         *
         *                                              String-format
         *
         * *********************************************************************************************************************************/

        ColumnType JSON              = new ColumnType() {public String getName(){return"JSON";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql,pg
        ColumnType JSONB             = new ColumnType() {public String getName(){return"JSONB";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType XML               = new ColumnType() {public String getName(){return"XML";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        /* *********************************************************************************************************************************
         *
         *                                              number-int/long
         *
         * *********************************************************************************************************************************/

        ColumnType INT               = new ColumnType() {public String getName(){return"INT";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                return BasicUtil.parseInt(value, null);
            }
        };  //mysql
        ColumnType LONG               = new ColumnType() {public String getName(){return"LONG";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                return BasicUtil.parseLong(value, null);
            }
        };  //
        ColumnType INT4               = new ColumnType() {public String getName(){return"INT4";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                return INT.write(value, def, placeholder);
            }
        };  //
        ColumnType INT8               = new ColumnType() {public String getName(){return"INT8";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                return LONG.write(value, def, placeholder);
            }
        };  //
        ColumnType BIT               = new ColumnType() {public String getName(){return"BIT";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
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
        ColumnType BIGINT            = new ColumnType() {public String getName(){return"BIGINT";}              public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                return BasicUtil.parseLong(value, null);
            }
        };  //mysql

        ColumnType MEDIUMINT         = new ColumnType() {public String getName(){return"MEDIUMINT";}           public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
        };  //mysql

        ColumnType INTEGER           = new ColumnType() {public String getName(){return"INTEGER";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
        };  //mysql
        ColumnType SMALLINT          = new ColumnType() {public String getName(){return"SMALLINT";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
        };  //mysql
        ColumnType TINYINT           = new ColumnType() {public String getName(){return"TINYINT";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return INT.write(value, def, placeholder);}
        };  //mysql
        /* *********************************************************************************************************************************
         *
         *                                              number-double/float
         *
         * *********************************************************************************************************************************/

        ColumnType DECIMAL           = new ColumnType() {public String getName(){return"DECIMAL";}             public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                return BasicUtil.parseDecimal(value, null);
            }
        }; //mysql,  ,oracle

        ColumnType DOUBLE            = new ColumnType() {public String getName(){return"DOUBLE";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                return BasicUtil.parseDouble(value, null);
            }
        }; //mysql

        ColumnType FLOAT             = new ColumnType() {public String getName(){return"FLOAT";}               public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                if(null == value){
                    value = def;
                }
                return BasicUtil.parseFloat(value, null);
            }
        }; //mysql,  ,oracle

        ColumnType BINARY_DOUBLE     = new ColumnType() {public String getName(){return"BINARY_DOUBLE";}       public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return DOUBLE.write(value, def, placeholder);}
        };  //        ,oracle
        ColumnType BINARY_FLOAT      = new ColumnType() {public String getName(){return"BINARY_FLOAT";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return FLOAT.write(value, def, placeholder);}
        };  //        ,oracle
        ColumnType MONEY             = new ColumnType() {public String getName(){return"MONEY";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                return DECIMAL.write(value, def, placeholder);
            }
        };  //     ,pg
        ColumnType NUMERIC            = new ColumnType() {public String getName(){return"NUMERIC";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                return DECIMAL.write(value, def, placeholder);
            }
        };  //mysql
        ColumnType NUMBER            = new ColumnType() {public String getName(){return"NUMBER";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){
                return DECIMAL.write(value, def, placeholder);
            }
        }; //     ,  ,oracle

        ColumnType REAL              = new ColumnType() {public String getName(){return"REAL";}                public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return FLOAT.write(value, def, placeholder);}
        };  //mysql
        /* *********************************************************************************************************************************
         *
         *                                              date
         *
         * *********************************************************************************************************************************/
        //TODO         write 需要根据数据库类型 由内置函数转换
        ColumnType DATE              = new ColumnType() {public String getName(){return"DATE";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
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
        ColumnType DATETIME          = new ColumnType() {public String getName(){return"DATETIME";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
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

        ColumnType TIME              = new ColumnType() {public String getName(){return"TIME";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql,pg
        ColumnType TIMEZ             = new ColumnType() {public String getName(){return"TIMEZ";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType TIMESTAMP         =  DATETIME;  //mysql,pg,oracle
        ColumnType TIMESTAMP_ZONE    = new ColumnType() {public String getName(){return"TIMESTAMP";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType TIMESTAMP_LOCAL_ZONE= new ColumnType() {public String getName(){return"TIMESTAMP";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg

        ColumnType YEAR              = new ColumnType() {public String getName(){return"YEAR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return DATE.write(value, def, placeholder);}
        };  //mysql
        /* *********************************************************************************************************************************
         *
         *                                              byte[]
         *
         * *********************************************************************************************************************************/

        ColumnType BLOB              = new ColumnType() {public String getName(){return"BLOB";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
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

        ColumnType LONGBLOB          = new ColumnType() {public String getName(){return"LONGBLOB";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
        };  //mysql

        ColumnType MEDIUMBLOB        = new ColumnType() {public String getName(){return"MEDIUMBLOB";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
        };  //mysql
        ColumnType TINYBLOB          = new ColumnType() {public String getName(){return"TINYBLOB";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return BLOB.write(value, def, placeholder);}
        };  //mysql
        /* *********************************************************************************************************************************
         *
         *                                              byte[]-file
         *
         * *********************************************************************************************************************************/
        /* *********************************************************************************************************************************
         *
         *                                              byte[]-geometry
         *
         * *********************************************************************************************************************************/

        ColumnType POINT             = new ColumnType() {public String getName(){return"POINT";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){
                if(null == value){
                    return value;
                }
                Point point = BasicUtil.parsePoint(value);
                String classNmae = clazz.getSimpleName();
                if(classNmae.equals("point")){
                    value = point;
                }else if(classNmae.equals("double[]")){
                    value = BeanUtil.Double2double(point.getArray(), 0);
                }else if(classNmae.equals("Double[]")){
                    value = point.getArray();
                }else if(classNmae.equals("byte[]")){
                    value = point.bytes();
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

        ColumnType MULTIPOLYGON      = new ColumnType() {public String getName(){return"MULTIPOLYGON";}        public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql
        ColumnType MULTIPOINT        = new ColumnType() {public String getName(){return"MULTIPOINT";}          public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql
        ColumnType POLYGON           = new ColumnType() {public String getName(){return"POLYGON";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql,pg

        ColumnType GEOMETRY          = new ColumnType() {public String getName(){return"GEOMETRY";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql
        ColumnType GEOMETRYCOLLECTIO = new ColumnType() {public String getName(){return"GEOMETRYCOLLECTION";}  public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql

        ColumnType LINE              = new ColumnType() {public String getName(){return"LINE";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql,pg
        ColumnType LSEG              = new ColumnType() {public String getName(){return"LSEG";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
























        ColumnType BFILE             = new ColumnType() {public String getName(){return"BFILE";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //        ,oracle
         

        ColumnType BINARY            = new ColumnType() {public String getName(){return"BINARY";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql
        ColumnType BOOL              = new ColumnType() {public String getName(){return"BOOL";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType BOX               = new ColumnType() {public String getName(){return"BOX";}                 public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType BYTEA             = new ColumnType() {public String getName(){return"BYTEA";}               public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg

        ColumnType CIDR              = new ColumnType() {public String getName(){return"CIDR";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //      pg
        ColumnType CIRCLE            = new ColumnType() {public String getName(){return"CIRCLE";}              public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //      pg
        ColumnType ENUM              = new ColumnType() {public String getName(){return"ENUM";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql
        ColumnType INET              = new ColumnType() {public String getName(){return"INET";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType INTERVAL          = new ColumnType() {public String getName(){return"INTERVAL";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType MACADDR           = new ColumnType() {public String getName(){return"MACADDR";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType PATH              = new ColumnType() {public String getName(){return"PATH";}                public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg

        ColumnType RAW               = new ColumnType() {public String getName(){return"RAW";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,  ,oracle
        ColumnType ROWID             = new ColumnType() {public String getName(){return"ROWID";}               public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,  ,oracle
        ColumnType SET               = new ColumnType() {public String getName(){return"SET";}                 public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql
        ColumnType TSQUERY           = new ColumnType() {public String getName(){return"TSQUERY";}             public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType TSVECTOR          = new ColumnType() {public String getName(){return"TSVECTOR";}            public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType TXID_SNAPSHOT     = new ColumnType() {public String getName(){return"TXID_SNAPSHOT";}       public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType UROWID            = new ColumnType() {public String getName(){return"UROWID";}              public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,  ,oracle

        ColumnType VARBIT            = new ColumnType() {public String getName(){return"VARBIT";}              public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //     ,pg
        ColumnType VARBINARY         = new ColumnType() {public String getName(){return"VARBINARY";}           public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}
            public Object read(Object value, Class clazz){return value;}
            public Object write(Object value, Object def, boolean placeholder){return value;}
        };  //mysql 

        //以下按字母顺序 方便查找
        types.put("BFILE"                   , BFILE                 ); //        ,oracle
        types.put("BINARY_DOUBLE"           , BINARY_DOUBLE         ); //        ,oracle
        types.put("BINARY_FLOAT"            , BINARY_FLOAT          ); //        ,oracle
        types.put("BIGINT"                  , BIGINT                ); //mysql
        types.put("BINARY"                  , BINARY                ); //mysql
        types.put("BIT"                     , BIT                   ); //mysql,pg
        types.put("BLOB"                    , BLOB                  ); //mysql,  ,oracle
        types.put("BOOL"                    , BOOL                  ); //     ,pg
        types.put("BOX"                     , BOX                   ); //     ,pg
        types.put("BYTEA"                   , BYTEA                 ); //     ,pg
        types.put("CHAR"                    , CHAR                  ); //mysql,pg
        types.put("CIDR"                    , CIDR                  ); //      pg
        types.put("CIRCLE"                  , CIRCLE                ); //      pg
        types.put("CLOB"                    , CLOB                  ); //        ,oracle
        types.put("DATE"                    , DATE                  ); //mysql,pg,oracle
        types.put("DATETIME"                , DATETIME              ); //mysql
        types.put("DECIMAL"                 , DECIMAL               ); //mysql,  ,oracle
        types.put("DOUBLE"                  , DOUBLE                ); //mysql
        types.put("ENUM"                    , ENUM                  ); //mysql
        types.put("FLOAT"                   , FLOAT                 ); //mysql,  ,oracle
        types.put("GEOMETRY"                , GEOMETRY              ); //mysql
        types.put("GEOMETRYCOLLECTIO"       , GEOMETRYCOLLECTIO     ); //mysql
        types.put("INET"                    , INET                  ); //     ,pg
        types.put("INTERVAL"                , INTERVAL              ); //     ,pg
        types.put("INT"                     , INT                   ); //mysql
        types.put("INT4"                    , INT4                  ); //
        types.put("INT8"                    , INT8                  ); //
        types.put("INTEGER"                 , INTEGER               ); //mysql
        types.put("JSON"                    , JSON                  ); //mysql,pg
        types.put("JSONB"                   , JSONB                 ); //     ,pg
        types.put("LINE"                    , LINE                  ); //mysql,pg
        types.put("LSEG"                    , LSEG                  ); //     ,pg
        types.put("MACADDR"                 , MACADDR               ); //     ,pg
        types.put("MONEY"                   , MONEY                 ); //     ,pg
        types.put("NUMBER"                  , NUMBER                ); //     ,  ,oracle
        types.put("NCHAR"                   , NCHAR                 ); //     ,  ,oracle
        types.put("NCLOB"                   , NCLOB                 ); //     ,  ,oracle
        types.put("NVARCHAR2"               , NVARCHAR2             ); //     ,  ,oracle
        types.put("PATH"                    , PATH                  ); //     ,pg
        types.put("LONGBLOB"                , LONGBLOB              ); //mysql
        types.put("LONGTEXT"                , LONGTEXT              ); //mysql
        types.put("MEDIUMBLOB"              , MEDIUMBLOB            ); //mysql
        types.put("MEDIUMINT"               , MEDIUMINT             ); //mysql
        types.put("MEDIUMTEXT"              , MEDIUMTEXT            ); //mysql
        types.put("MULTILINESTRING"         , MULTILINESTRING       ); //mysql
        types.put("MULTIPOINT"              , MULTIPOINT            ); //mysql
        types.put("MULTIPOLYGON"            , MULTIPOLYGON          ); //mysql
        types.put("NUMERIC"                 , NUMERIC               ); //mysql
        types.put("POINT"                   , POINT                 ); //mysql,pg
        types.put("POLYGON"                 , POLYGON               ); //mysql,pg
        types.put("REAL"                    , REAL                  ); //mysql
        types.put("RAW"                     , RAW                   ); //     ,  ,oracle
        types.put("ROWID"                   , ROWID                 ); //     ,  ,oracle
        types.put("SET"                     , SET                   ); //mysql
        types.put("SMALLINT"                , SMALLINT              ); //mysql
        types.put("TEXT"                    , TEXT                  ); //mysql,pg
        types.put("TIME"                    , TIME                  ); //mysql,pg
        types.put("TIMEZ"                   , TIMEZ                 ); //     ,pg
        types.put("TIMESTAMP"               , TIMESTAMP             ); //mysql,pg,oracle
        types.put("TIMESTAMP_LOCAL_ZONE"    , TIMESTAMP_LOCAL_ZONE  ); //     ,pg
        types.put("TIMESTAMP_ZONE"          , TIMESTAMP_ZONE        ); //     ,pg
        types.put("TSQUERY"                 , TSQUERY               ); //     ,pg
        types.put("TSVECTOR"                , TSVECTOR              ); //     ,pg
        types.put("TXID_SNAPSHOT"           , TXID_SNAPSHOT         ); //     ,pg
        types.put("UUID"                    , UUID                  ); //     ,pg
        types.put("UROWID"                  , UROWID                ); //     ,  ,oracle
        types.put("VARBIT"                  , VARBIT                ); //     ,pg
        types.put("TINYBLOB"                , TINYBLOB              ); //mysql
        types.put("TINYINT"                 , TINYINT               ); //mysql
        types.put("TINYTEXT"                , TINYTEXT              ); //mysql
        types.put("VARBINARY"               , VARBINARY             ); //mysql
        types.put("VARCHAR"                 , VARCHAR               ); //mysql,pg,oracle
        types.put("VARCHAR2"                , VARCHAR2              ); //     ,  ,oracle
        types.put("XML"                     , XML                   ); //     ,pg
        types.put("YEAR"                    , YEAR                  ); //mysql
    }
}
