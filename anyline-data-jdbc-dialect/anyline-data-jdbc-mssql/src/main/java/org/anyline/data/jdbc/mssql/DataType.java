package org.anyline.data.jdbc.mssql;

public enum DataType implements org.anyline.entity.mdtadata.DataType{
    ILLEGAL          (org.anyline.data.metadata.DataType.ILLEGAL         ),
    BIGINT           (org.anyline.data.metadata.DataType.BIGINT          ),
    BINARY           (org.anyline.data.metadata.DataType.BINARY          ),
    BIT              (org.anyline.data.metadata.DataType.BIT             ),
    CHAR             (org.anyline.data.metadata.DataType.CHAR            ),
    DATE             (org.anyline.data.metadata.DataType.DATE            ),
    DATETIME         (org.anyline.data.metadata.DataType.DATETIME        ),
    DATETIME2        (org.anyline.data.metadata.DataType.DATETIME2       ),
    DATETIMEOFFSET   (org.anyline.data.metadata.DataType.DATETIMEOFFSET  ),
    DECIMAL          (org.anyline.data.metadata.DataType.DECIMAL         ),
    FLOAT            (org.anyline.data.metadata.DataType.FLOAT           ),
    GEOGRAPHY        (org.anyline.data.metadata.DataType.GEOGRAPHY       ),
    HIERARCHYID      (org.anyline.data.metadata.DataType.HIERARCHYID     ),
    IMAGE            (org.anyline.data.metadata.DataType.IMAGE           ),
    INT              (org.anyline.data.metadata.DataType.INT             ),
    MONEY            (org.anyline.data.metadata.DataType.MONEY           ),
    NCHAR            (org.anyline.data.metadata.DataType.NCHAR           ),
    NTEXT            (org.anyline.data.metadata.DataType.NTEXT           ),
    NVARCHAR         (org.anyline.data.metadata.DataType.NVARCHAR        ),
    NUMERIC          (org.anyline.data.metadata.DataType.NUMERIC         ),
    REAL             (org.anyline.data.metadata.DataType.REAL            ),
    SMALLDATETIME    (org.anyline.data.metadata.DataType.SMALLDATETIME   ),
    SMALLMONEY       (org.anyline.data.metadata.DataType.SMALLMONEY      ),
    SQL_VARIANT      (org.anyline.data.metadata.DataType.SQL_VARIANT     ),
    SYSNAME          (org.anyline.data.metadata.DataType.SYSNAME         ),
    TEXT             (org.anyline.data.metadata.DataType.TEXT            ),
    TIME             (org.anyline.data.metadata.DataType.TIME            ),
    TIMESTAMP        (org.anyline.data.metadata.DataType.TIMESTAMP       ),
    UNIQUEIDENTIFIER (org.anyline.data.metadata.DataType.UNIQUEIDENTIFIER),
    TINYINT          (org.anyline.data.metadata.DataType.TINYINT         ),
    VARBINARY        (org.anyline.data.metadata.DataType.VARBINARY       ),
    VARCHAR          (org.anyline.data.metadata.DataType.VARCHAR         ),
    XML              (org.anyline.data.metadata.DataType.XML             );

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
