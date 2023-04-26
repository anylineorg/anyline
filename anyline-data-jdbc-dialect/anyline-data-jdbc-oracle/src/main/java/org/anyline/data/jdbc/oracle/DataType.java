package org.anyline.data.jdbc.oracle;

public enum DataType implements org.anyline.entity.metadata.DataType{
    ILLEGAL                 (org.anyline.data.metadata.DataType.ILLEGAL         ),
    BFILE                   (org.anyline.data.metadata.DataType.BFILE           ),
    BINARY_DOUBLE           (org.anyline.data.metadata.DataType.BINARY_DOUBLE   ),
    BINARY_FLOAT            (org.anyline.data.metadata.DataType.BINARY_FLOAT    ),
    BLOB                    (org.anyline.data.metadata.DataType.BLOB            ),
    CHAR                    (org.anyline.data.metadata.DataType.CHAR            ),
    CLOB                    (org.anyline.data.metadata.DataType.CLOB            ),
    DATE                    (org.anyline.data.metadata.DataType.DATE            ),
    FLOAT                   (org.anyline.data.metadata.DataType.FLOAT           ),
    NUMBER                  (org.anyline.data.metadata.DataType.NUMBER          ),
    NCHAR                   (org.anyline.data.metadata.DataType.NCHAR           ),
    NCLOB                   (org.anyline.data.metadata.DataType.NCLOB           ),
    NVARCHAR2               (org.anyline.data.metadata.DataType.NVARCHAR2       ),
    RAW                     (org.anyline.data.metadata.DataType.RAW             ),
    ROWID                   (org.anyline.data.metadata.DataType.ROWID           ),
    TIMESTAMP               (org.anyline.data.metadata.DataType.TIMESTAMP       ),
    UROWID                  (org.anyline.data.metadata.DataType.UROWID          ),
    VARCHAR                 (org.anyline.data.metadata.DataType.VARCHAR         ),
    VARCHAR2                (org.anyline.data.metadata.DataType.VARCHAR2        );
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
