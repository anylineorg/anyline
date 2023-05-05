package org.anyline.entity.metadata;


import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.entity.data.DatabaseType;

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
        public String concat(Object value) {
            return null;
        }

        @Override
        public DatabaseType[] dbs() {
            return new DatabaseType[0];
        }

        @Override
        public Object convert(Object value, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target) {
            return null;
        }

        @Override
        public Object convert(Object value, Class target, Object def) {
            return null;
        }

        @Override
        public Object convert(Object value, Object obj, Field field) {
            return null;
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
     * 以String类型拼接SQL需要引号或类型转换函数
     * @return Class
     */
    public default String concat(Object value){
        if(null != value) {
            value = ConvertAdapter.convert(value, compatible());
        }
        if(null != value){
            return value.toString();
        }
        return null;
    }

    /**
     * 支持的数据库
     * @return DatabaseType
     */
    public abstract DatabaseType[] dbs();

    public abstract Object convert(Object value, Object def);
    public abstract Object convert(Object value, Class target);
    public abstract Object convert(Object value, Class target, Object def);
    public abstract Object convert(Object value, Object obj, Field field);
}
