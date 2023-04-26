package org.anyline.data.jdbc.postgresql;

public enum DataType implements org.anyline.entity.mdtadata.DataType {

    CHAR(org.anyline.data.jdbc.adapter.DataType.CHAR);

    private final org.anyline.entity.mdtadata.DataType standard;

    private DataType(org.anyline.entity.mdtadata.DataType standard) {
        this.standard = standard;
    }

    @Override
    public String getName() {
        return this.name();
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
    public boolean isIgnorePrecision() {
        return standard.isIgnorePrecision();
    }

    @Override
    public boolean isIgnoreScale() {
        return standard.isIgnoreScale();
    }
}
