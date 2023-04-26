package org.anyline.data.jdbc.sqlite;

public enum DataType implements org.anyline.entity.mdtadata.DataType{
    ILLEGAL          (org.anyline.data.metadata.DataType.ILLEGAL         ),
    INTEGER           (org.anyline.data.metadata.DataType.BIGINT         ),
    REAL           (org.anyline.data.metadata.DataType.BINARY            ),
    TEXT              (org.anyline.data.metadata.DataType.BIT            ),
    BLOB             (org.anyline.data.metadata.DataType.CHAR            );

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
