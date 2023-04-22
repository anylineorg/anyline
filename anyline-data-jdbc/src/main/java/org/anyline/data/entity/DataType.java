package org.anyline.data.entity;

public enum DataType {

    BFILE	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
    BINARY_DOUBLE	    {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
    BINARY_FLOAT        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
    BIGINT	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    BINARY	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
    BIT	                {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql,pg
    BLOB	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,  ,oracle
    BOOL	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    BOX	                {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    BYTEA	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    CHAR	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
    CIDR	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //      pg
    CIRCLE	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //      pg
    CLOB                {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
    DATE	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg,oracle
    DATETIME	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    DECIMAL	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql,  ,oracle
    DOUBLE	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql
    ENUM	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    FLOAT	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql,  ,oracle
    INET	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    INTERVAL	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    GEOMETRY	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    GEOMETRYCOLLECTION	{public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    INT	                {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    INTEGER	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    JSON	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
    JSONB	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    LINE                {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
    LSEG	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    MACADDR	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    MONEY	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    NUMBER	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //     ,  ,oracle
    NCHAR	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
    NCLOB	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
    NVARCHAR2           {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
    PATH	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    LONGBLOB	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    LONGTEXT	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    MEDIUMBLOB	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    MEDIUMINT	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    MEDIUMTEXT	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    MULTILINESTRING	    {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    MULTIPOINT	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    MULTIPOLYGON	    {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    NUMERIC	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql
    POINT	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
    POLYGON	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
    REAL	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
    RAW 	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
    ROWID 	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
    SET	                {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
    SMALLINT	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    TEXT	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
    TIME	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
    TIMEZ	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    TIMESTAMP	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg,oracle
    TIMESTAMPZ	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    TSQUERY             {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    TSVECTOR            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    TXID_SNAPSHOT       {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    UUID                {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    UROWID              {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
    VARBIT              {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    TINYBLOB	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    TINYINT	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    TINYTEXT	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
    VARBINARY	        {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
    VARCHAR	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql,pg,oracle
    VARCHAR2            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
    XML 	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
    YEAR	            {public  Object read(Object value, Class clazz){return value;}    public Object write(Object value, boolean placeholder){return value;}    public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}};  //mysql



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


    
    //public abstract Class getJavaClass();
   // public abstract String getName();

    /**
     * 定义列时 数据类型格式
     * @return boolean
     */
    public abstract boolean isIgnorePrecision();
    public abstract boolean isIgnoreScale();
}
