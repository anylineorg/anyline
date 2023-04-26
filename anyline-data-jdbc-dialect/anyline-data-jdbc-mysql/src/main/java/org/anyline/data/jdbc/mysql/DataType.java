package org.anyline.data.jdbc.mysql;

public enum DataType implements org.anyline.entity.metadata.DataType{
    ILLEGAL               (org.anyline.data.metadata.DataType.ILLEGAL           ),
    BIGINT                (org.anyline.data.metadata.DataType.BIGINT            )  ,
    BINARY                (org.anyline.data.metadata.DataType.BINARY            )  ,
    BIT                   (org.anyline.data.metadata.DataType.BIT               )  ,
    BLOB                  (org.anyline.data.metadata.DataType.BLOB              )  ,
    CHAR                  (org.anyline.data.metadata.DataType.CHAR              )  ,
    DATE                  (org.anyline.data.metadata.DataType.DATE              )  ,
    DATETIME              (org.anyline.data.metadata.DataType.DATETIME          )  ,
    DATETIME2             (org.anyline.data.metadata.DataType.DATETIME2         )  ,
    DATETIMEOFFSET        (org.anyline.data.metadata.DataType.DATETIMEOFFSET    )  ,
    DECIMAL               (org.anyline.data.metadata.DataType.DECIMAL           )  ,
    DOUBLE                (org.anyline.data.metadata.DataType.DOUBLE            )  ,
    ENUM                  (org.anyline.data.metadata.DataType.ENUM              )  ,
    FLOAT                 (org.anyline.data.metadata.DataType.FLOAT             )  ,
    GEOMETRY              (org.anyline.data.metadata.DataType.GEOMETRY          )  ,
    GEOMETRYCOLLECTION    (org.anyline.data.metadata.DataType.GEOMETRYCOLLECTION)  ,
    INT                   (org.anyline.data.metadata.DataType.INT               )  ,
    INTEGER               (org.anyline.data.metadata.DataType.INTEGER           )  ,
    JSON                  (org.anyline.data.metadata.DataType.JSON              )  ,
    LINE                  (org.anyline.data.metadata.DataType.LINE              )  ,
    LONGBLOB              (org.anyline.data.metadata.DataType.LONGBLOB          )  ,
    LONGTEXT              (org.anyline.data.metadata.DataType.LONGTEXT          )  ,
    MEDIUMBLOB            (org.anyline.data.metadata.DataType.MEDIUMBLOB        )  ,
    MEDIUMINT             (org.anyline.data.metadata.DataType.MEDIUMINT         )  ,
    MEDIUMTEXT            (org.anyline.data.metadata.DataType.MEDIUMTEXT        )  ,
    MULTILINESTRING       (org.anyline.data.metadata.DataType.MULTILINESTRING   )  ,
    MULTIPOINT            (org.anyline.data.metadata.DataType.MULTIPOINT        )  ,
    MULTIPOLYGON          (org.anyline.data.metadata.DataType.MULTIPOLYGON      )  ,
    NUMERIC               (org.anyline.data.metadata.DataType.NUMERIC           )  ,
    POINT                 (org.anyline.data.metadata.DataType.POINT             )  ,
    POLYGON               (org.anyline.data.metadata.DataType.POLYGON           )  ,
    REAL                  (org.anyline.data.metadata.DataType.REAL              )  ,
    SET                   (org.anyline.data.metadata.DataType.SET               )  ,
    SMALLINT              (org.anyline.data.metadata.DataType.SMALLINT          )  ,
    TEXT                  (org.anyline.data.metadata.DataType.TEXT              )  ,
    TIME                  (org.anyline.data.metadata.DataType.TIME              )  ,
    TIMESTAMP             (org.anyline.data.metadata.DataType.TIMESTAMP         )  ,
    TINYBLOB              (org.anyline.data.metadata.DataType.TINYBLOB          )  ,
    TINYINT               (org.anyline.data.metadata.DataType.TINYINT           )  ,
    TINYTEXT              (org.anyline.data.metadata.DataType.TINYTEXT          )  ,
    VARBINARY             (org.anyline.data.metadata.DataType.VARBINARY         )  ,
    VARCHAR               (org.anyline.data.metadata.DataType.VARCHAR           )  ,
    YEAR                  (org.anyline.data.metadata.DataType.YEAR              )  ;

    private final  org.anyline.entity.metadata.DataType standard;

    private DataType(org.anyline.entity.metadata.DataType standard){
        this.standard = standard;
    }
    @Override
    public String type() {
        return standard.type();
    }

    @Override
    public Object read(Object value, Class clazz) {
        return standard.read(value, clazz);
    }

    @Override
    public Object write(Object value, Object def, boolean placeholder) {
        return standard.write(value, def, placeholder);
    }

    @Override
    public boolean ignorePrecision() {
        return standard.ignorePrecision();
    }

    @Override
    public boolean ignoreScale() {
        return standard.ignoreScale();
    }

    @Override
    public boolean support() {
        return true;
    }

}
