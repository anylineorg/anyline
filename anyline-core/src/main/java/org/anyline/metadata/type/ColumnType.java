package org.anyline.metadata.type;


import java.lang.reflect.Field;

public interface ColumnType {
    public static ColumnType ILLEGAL = new ColumnType() {

        @Override
        public String getName() {
            return null;
        }

        @Override
        public boolean ignorePrecision() {
            return false;
        }

        @Override
        public boolean ignoreScale() {
            return false;
        }
        @Override
        public boolean support() {
            return false;
        }

        @Override
        public Class compatible() {
            return null;
        }

        @Override
        public Class transfer() {
            return null;
        }

        @Override
        public DatabaseType[] dbs() {
            return new DatabaseType[0];
        }

        @Override
        public Object convert(Object value, Object def) {
            return value;
        }

        @Override
        public Object convert(Object value, Class target) {
            return value;
        }

        @Override
        public Object convert(Object value, Class target, Object def) {
            return value;
        }

        @Override
        public Object convert(Object value, Object obj, Field field) {
            return value;
        }

        @Override
        public Object read(Object value, Object def, Class clazz) {
            return value;
        }

        @Override
        public Object write(Object value, Object def, boolean placeholder) {
            return value;
        }

    };
    public abstract String getName();
    public abstract boolean ignorePrecision();
    public abstract boolean ignoreScale();
    public abstract boolean support();

    /**
     * 写入数据库或查询条件时的类型
     * @return Class
     */
    public abstract Class compatible();

    /**
     * 中间转换类型
     * 如 value(double[]) > transfer(Point) > byte[](compatible)
     * @return Class
     */
    public abstract Class transfer();


    /**
     * 支持的数据库
     * @return DatabaseType
     */
    public abstract DatabaseType[] dbs();

    public abstract Object convert(Object value, Object def);
    public abstract Object convert(Object value, Class target);
    public abstract Object convert(Object value, Class target, Object def);
    public abstract Object convert(Object value, Object obj, Field field);

    public Object read(Object value, Object def, Class clazz);
    public Object write(Object value, Object def, boolean placeholder);
}
