package org.anyline.data.jdbc.postgresql;

public enum DataType implements org.anyline.entity.mdtadata.DataType{
    ILLEGAL                 (org.anyline.data.metadata.DataType.ILLEGAL         ),
    BIGSERIAL               (org.anyline.data.metadata.DataType.BIGSERIAL           ) ,
    BIT                     (org.anyline.data.metadata.DataType.BIT                 ) ,
    BOOL                    (org.anyline.data.metadata.DataType.BOOL                ) ,
    BOX                     (org.anyline.data.metadata.DataType.BOX                 ) ,
    BYTEA                   (org.anyline.data.metadata.DataType.BYTEA               ) ,
    CHAR                    (org.anyline.data.metadata.DataType.CHAR                ) ,
    CIDR                    (org.anyline.data.metadata.DataType.CIDR                ) ,
    CIRCLE                  (org.anyline.data.metadata.DataType.CIRCLE              ) ,
    DATE                    (org.anyline.data.metadata.DataType.DATE                ) ,
    FLOAT4                  (org.anyline.data.metadata.DataType.FLOAT4              ) ,
    FLOAT8                  (org.anyline.data.metadata.DataType.FLOAT8              ) ,
    INET                    (org.anyline.data.metadata.DataType.INET                ) ,
    INTERVAL                (org.anyline.data.metadata.DataType.INTERVAL            ) ,
    INT2                    (org.anyline.data.metadata.DataType.INT2                ) ,
    JSON                    (org.anyline.data.metadata.DataType.JSON                ) ,
    JSONB                   (org.anyline.data.metadata.DataType.JSONB               ) ,
    LINE                    (org.anyline.data.metadata.DataType.LINE                ) ,
    LONG                    (org.anyline.data.metadata.DataType.LONG                ) ,
    LSEG                    (org.anyline.data.metadata.DataType.LSEG                ) ,
    MACADDR                 (org.anyline.data.metadata.DataType.MACADDR             ) ,
    MONEY                   (org.anyline.data.metadata.DataType.MONEY               ) ,
    PATH                    (org.anyline.data.metadata.DataType.PATH                ) ,
    POINT                   (org.anyline.data.metadata.DataType.POINT               ) ,
    POLYGON                 (org.anyline.data.metadata.DataType.POLYGON             ) ,
    SERIAL                  (org.anyline.data.metadata.DataType.SERIAL              ) ,
    SERIAL2                 (org.anyline.data.metadata.DataType.SERIAL2             ) ,
    SERIAL4                 (org.anyline.data.metadata.DataType.SERIAL4             ) ,
    SERIAL8                 (org.anyline.data.metadata.DataType.SERIAL8             ) ,
    SMALLSERIAL             (org.anyline.data.metadata.DataType.SMALLSERIAL        ) ,
    TEXT                    (org.anyline.data.metadata.DataType.TEXT                ) ,
    TIME                    (org.anyline.data.metadata.DataType.TIME                ) ,
    TIMEZ                   (org.anyline.data.metadata.DataType.TIMEZ               ) ,
    TIMESTAMP               (org.anyline.data.metadata.DataType.TIMESTAMP           ) ,
    TIMESTAMP_LOCAL_ZONE    (org.anyline.data.metadata.DataType.TIMESTAMP_LOCAL_ZONE) ,
    TIMESTAMP_ZONE          (org.anyline.data.metadata.DataType.TIMESTAMP_ZONE      ) ,
    TSQUERY                 (org.anyline.data.metadata.DataType.TSQUERY             ) ,
    TSVECTOR                (org.anyline.data.metadata.DataType.TSVECTOR            ) ,
    TXID_SNAPSHOT           (org.anyline.data.metadata.DataType.TXID_SNAPSHOT       ) ,
    UUID                    (org.anyline.data.metadata.DataType.UUID                ) ,
    VARBIT                  (org.anyline.data.metadata.DataType.VARBIT              ) ,
    VARCHAR                 (org.anyline.data.metadata.DataType.VARCHAR             ) ,
    XML                     (org.anyline.data.metadata.DataType.XML                 ) ;

    private final  org.anyline.entity.mdtadata.DataType standard;

    private DataType(org.anyline.entity.mdtadata.DataType standard){
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
