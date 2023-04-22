package org.anyline.data.jdbc.mysql;

import org.anyline.entity.DataRow;
import org.anyline.util.BasicUtil;

import java.math.BigDecimal;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public enum DataType {

    BIGINT	            {public String getName(){return "bigint";}				public Class getJavaClass(){return Long.class;}				public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BIGINT;	           }
        public Object read(Object value, Class clazz){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    BINARY	            {public String getName(){return "binary";}				public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BINARY;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    BIT	                {public String getName(){return "bit";}					public Class getJavaClass(){return Boolean.class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BIT;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    BLOB	            {public String getName(){return "blob";}				public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.BLOB;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    CHAR	            {public String getName(){return "char";}				public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.CHAR;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    DATE	            {public String getName(){return "date";} 				public Class getJavaClass(){return java.sql.Date.class;}	public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DATE;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    DATETIME	        {public String getName(){return "datetime";}			public Class getJavaClass(){return LocalDateTime.class;}	public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DATETIME;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    DECIMAL	            {public String getName(){return "decimal";}				public Class getJavaClass(){return BigDecimal.class;}		public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DECIMAL;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    DOUBLE	            {public String getName(){return "double";}				public Class getJavaClass(){return Double.class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.DOUBLE;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    ENUM	            {public String getName(){return "enum";}				public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.ENUM;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    FLOAT	            {public String getName(){return "float";}				public Class getJavaClass(){return Float.class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.FLOAT;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    GEOMETRY	        {public String getName(){return "geometry";}			public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.GEOMETRY;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    GEOMETRYCOLLECTION	{public String getName(){return "geometrycollection";}	public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.GEOMETRYCOLLECTION;}
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    INT	                {public String getName(){return "int";}					public Class getJavaClass(){return Integer.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.INT;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    INTEGER	            {public String getName(){return "int";}					public Class getJavaClass(){return Integer.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.INTEGER;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    JSON	            {public String getName(){return "json";}				public Class getJavaClass(){return DataRow.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.JSON;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    LINESTRING	        {public String getName(){return "linestring";}			public Class getJavaClass(){return byte[].class;} 			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.LINE;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    LONGBLOB	        {public String getName(){return "longblob";}            public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.LONGBLOB;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    LONGTEXT	        {public String getName(){return "longtext";}            public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.LONGTEXT;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    MEDIUMBLOB	        {public String getName(){return "mediumblob";}          public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MEDIUMBLOB;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    MEDIUMINT	        {public String getName(){return "mediumint";}           public Class getJavaClass(){return Integer.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MEDIUMINT;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    MEDIUMTEXT	        {public String getName(){return "mediumtext";}          public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MEDIUMTEXT;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    MULTILINESTRING	    {public String getName(){return "multilinestring";}     public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MULTILINESTRING;    }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    MULTIPOINT	        {public String getName(){return "multipoint";}          public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MULTIPOINT;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    MULTIPOLYGON	    {public String getName(){return "multipolygon";}        public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.MULTIPOLYGON;	   }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    NUMERIC	            {public String getName(){return "decimal";}             public Class getJavaClass(){return BigDecimal.class;}		public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.NUMERIC;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    POINT	            {public String getName(){return "point";}               public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.POINT;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    POLYGON	            {public String getName(){return "polygon";}             public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.POLYGON;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    REAL	            {public String getName(){return "double";}              public Class getJavaClass(){return Double.class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.REAL;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    SET	                {public String getName(){return "set";}                 public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.SET;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    SMALLINT	        {public String getName(){return "smallint";}            public Class getJavaClass(){return Integer.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.SMALLINT;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    TEXT	            {public String getName(){return "text";}                public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TEXT;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    TIME	            {public String getName(){return "time";}                public Class getJavaClass(){return Time.class;}				public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TIME;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    TIMESTAMP	        {public String getName(){return "timestamp";}           public Class getJavaClass(){return Timestamp.class;}		public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TIMESTAMP;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    TINYBLOB	        {public String getName(){return "tinyblob";}            public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TINYBLOB;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    TINYINT	            {public String getName(){return "tinyint";}             public Class getJavaClass(){return Integer.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TINYINT;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    TINYTEXT	        {public String getName(){return "tinytext";}            public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.TINYTEXT;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    VARBINARY	        {public String getName(){return "varbinary";}           public Class getJavaClass(){return byte[].class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.VARBINARY;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    VARCHAR	            {public String getName(){return "varchar";}             public Class getJavaClass(){return String.class;}			public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.VARCHAR;	       }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    },
    YEAR	            {public String getName(){return "year";}             	public Class getJavaClass(){return java.sql.Date.class;}	public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}	public org.anyline.data.entity.DataType getStandard(){return org.anyline.data.entity.DataType.YEAR;	           }
        public Object read(Object value, Class clazz){
            return value;
        }
        public Object write(Object value, boolean placeholder){
            if(null != value){
                value = BasicUtil.parseLong(value, null);
            }
            return value;
        }

    };

    /**
     * 从数据库中读取数据,常用的基本类型可以自动转换,不常用的如json/point/polygon/blob等转换成anyline对应的类型
     * @param value value
     * @param clazz 目标数据类型(给entity赋值时可以根据class, DataRow赋值时可以指定class，否则按检测metadata类型转换 转换不不了的原样返回)
     * @return Object
     */
    public abstract Object read(Object value, Class clazz);
    /**
     * 通过占位符写入数据库前转换成数据库可接受的Java数据类型<br/>
     * @param placeholder 是否占位符
     * @param value value
     * @return Object
     */
    public abstract Object write(Object value, boolean placeholder);


    public abstract org.anyline.data.entity.DataType getStandard();
    public abstract Class getJavaClass();
    public abstract String getName();

    /**
     * 定义列时 数据类型格式
     * @return boolean
     */
    public abstract boolean isIgnorePrecision();
    public abstract boolean isIgnoreScale();
}
