package org.anyline.entity.metadata.init;

import org.anyline.entity.Point;
import org.anyline.entity.metadata.Convert;
import org.anyline.util.DateUtil;

import java.util.Date;

public enum DefaultConvert implements Convert {
    /* *****************************************************************************************************************
    *                                               日期
    * ==================================================================================================================
    * java.util.Date
     * java.sql.Date
     * java.sql.Time
     * java.time.LocalDate
     * java.time.LocalTime
     * java.time.LocalDateTime
    *
    * *****************************************************************************************************************/
    javaUtilDate_javaSqlDate(java.util.Date.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) {
            Date date = (Date)value;
            return DateUtil.sqlDate(date);
        }
    },
    javaUtilDate_javaTimeLocalDate(java.util.Date.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) {
            Date date = (Date)value;
            return DateUtil.localDate(date);
        }
    },

    /* *****************************************************************************************************************
     *                                               array
     * ==================================================================================================================
     * java.entity.Double
     *
     * *****************************************************************************************************************/
    Doubles_Point(Double[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) {
            Point point = new Point((Double[])value);
            return point;
        }
    },
    double_Point(double[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) {
            Point point = new Point((double[])value);
            return point;
        }
    },
    bytes_String(byte[].class, String.class){
        @Override
        public Object exe(Object value, Object def) {
            return new String((byte[]) value);
        }
    },
    bytes_Point(byte[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) {
            return new Point((byte[]) value);
        }
    },

    /* *****************************************************************************************************************
     *                                               Point
     * ==================================================================================================================
     * java.entity.Point
     *
     * *****************************************************************************************************************/
    Point_bytes(Point.class, byte[].class){
        @Override
        public Object exe(Object value, Object def) {
            Point point = (Point) value;
            return point.bytes();
        }
    },
    Point_Doubles(Point.class, Double[].class){
        @Override
        public Object exe(Object value, Object def) {
            Point point = (Point) value;
            return point.getDoubles();
        }
    },
    Point_doubles(Point.class, double[].class){
        @Override
        public Object exe(Object value, Object def) {
            Point point = (Point) value;
            try{
                value = point.doubles();
            }catch (Exception e){
                e.printStackTrace();
            }
            return value;
        }
    }
    ;
    private DefaultConvert(Class origin, Class target){
        this.origin = origin;
        this.target = target;
    }
    private final Class origin;
    private final Class target;

    public Class getOrigin() {
        return origin;
    }

    public Class getTarget() {
        return target;
    }
}