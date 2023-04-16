package org.anyline.entity.data;

import java.io.Serializable;

public interface Column extends Serializable {

    public static enum STANDARD_DATA_TYPE{
        BFILE	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
        BINARY_DOUBLE	    {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
        BINARY_FLOAT        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
        BIGINT	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        BINARY	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
        BIT	                {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql,pg
        BLOB	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,  ,oracle
        BOOL	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        BOX	                {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        BYTEA	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        CHAR	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
        CIDR	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //      pg
        CIRCLE	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //      pg
        CLOB                {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //        ,oracle
        DATE	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg,oracle
        DATETIME	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        DECIMAL	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql,  ,oracle
        DOUBLE	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql
        ENUM	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        FLOAT	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql,  ,oracle
        INET	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        INTERVAL	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        GEOMETRY	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        GEOMETRYCOLLECTION	{public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        INT	                {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        INTEGER	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        JSON	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
        JSONB	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        LINE                {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
        LSEG	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        MACADDR	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        MONEY	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        NUMBER	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //     ,  ,oracle
        NCHAR	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
        NCLOB	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
        NVARCHAR2           {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
        PATH	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        LONGBLOB	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        LONGTEXT	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        MEDIUMBLOB	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        MEDIUMINT	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        MEDIUMTEXT	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        MULTILINESTRING	    {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        MULTIPOINT	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        MULTIPOLYGON	    {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        NUMERIC	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return false;}}, //mysql
        POINT	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
        POLYGON	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
        REAL	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
        RAW 	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
        ROWID 	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
        SET	                {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
        SMALLINT	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        TEXT	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
        TIME	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg
        TIMEZ	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        TIMESTAMP	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql,pg,oracle
        TIMESTAMPZ	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        TSQUERY             {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        TSVECTOR            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        TXID_SNAPSHOT       {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        UUID                {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        UROWID              {public boolean isIgnorePrecision(){return false;}    public boolean isIgnoreScale(){return true;}}, //     ,  ,oracle
        VARBIT              {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        TINYBLOB	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        TINYINT	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        TINYTEXT	        {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //mysql
        VARBINARY	        {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql
        VARCHAR	            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //mysql,pg,oracle
        VARCHAR2            {public boolean isIgnorePrecision(){return false;}   public boolean isIgnoreScale(){return true;}},  //     ,  ,oracle
        XML 	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}},  //     ,pg
        YEAR	            {public boolean isIgnorePrecision(){return true;}    public boolean isIgnoreScale(){return true;}};  //mysql

        public abstract boolean isIgnorePrecision();
        public abstract boolean isIgnoreScale();
    }

    public String getCatalog();

    public String getClassName();

    public Integer getDisplaySize();

    public String getComment();

    public String getName();
    public String getOriginalName();

    public Integer getType();

    public Table getTable();
    public String getTypeName();

    public Integer getPrecision();

    public String getSchema();
    public String getTableName();

    public int isCaseSensitive();

    public int isCurrency();

    public int isSigned();
    public Integer getScale();

    public int isNullable();

    public int isAutoIncrement();
    public int isPrimaryKey();

    public int isGenerated();

    public Object getDefaultValue();

    public Integer getPosition();

    public String getOrder();

    public String getBefore() ;
    public String getAfter() ;

    public Integer getIncrementSeed();
    public Integer getIncrementStep();
    public int isOnUpdate();

    public String getCharset();

    public String getCollate();
    public void delete();
    public boolean isDelete();
    //完整的数据类型 varchar(32)
    public String getFullType();
    public boolean equals(Column column);
}
