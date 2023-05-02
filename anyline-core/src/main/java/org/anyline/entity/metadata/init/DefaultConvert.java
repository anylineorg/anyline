package org.anyline.entity.metadata.init;

import org.anyline.entity.Point;
import org.anyline.entity.metadata.Convert;
import org.anyline.entity.metadata.ConvertException;
import org.anyline.util.DateUtil;

import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;

public enum DefaultConvert implements Convert {
    /* *****************************************************************************************************************
    *                                               日期
    * ==================================================================================================================
    * java.util.Date
    * java.sql.Date
    * java.sql.Time
    * java.sql.Timestamp
    * java.time.Year
    * java.time.YearMonth
    * java.time.Month
    * java.time.LocalDate
    * java.time.LocalTime
    * java.time.LocalDateTime
    * *****************************************************************************************************************/


    /* *****************************************************************************************************************
     *                                                  java.util.Date
     * *****************************************************************************************************************/
    javaUtilDate_String(java.util.Date.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.format(date);
        }
    },
    javaUtilDate_Long(java.util.Date.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return date.getTime();
        }
    },
    javaUtilDate_javaSQLDate(java.util.Date.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.sqlDate(date);
        }
    },
    javaUtilDate_javaSQLTime(java.util.Date.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.sqlTime(date);
        }
    },
    javaUtilDate_javaSQLTimestamp(java.util.Date.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaUtilDate_javaTimeYear(java.util.Date.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaUtilDate_javaTimeYearMonth(java.util.Date.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date) value;
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaUtilDate_javaTimeMonth(java.util.Date.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaUtilDate_javaTimeLocalDate(java.util.Date.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.localDate(date);
        }
    },
    javaUtilDate_javaTimeLocalTime(java.util.Date.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.localTime(date);

        }
    },

    javaUtilDate_javaTimeLocalDateTime(java.util.Date.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = (Date)value;
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                                  java.sql.Date
     * *****************************************************************************************************************/
    javaSQLDate_String(java.sql.Date.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date);
        }
    },

    javaSQLDate_javaSQLDate(java.sql.Date.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaSQLDate_javaSQLTime(java.sql.Date.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaSQLDate_javaSQLTimestamp(java.sql.Date.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaSQLDate_javaTimeYear(java.sql.Date.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaSQLDate_javaTimeYearMonth(java.sql.Date.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaSQLDate_javaTimeMonth(java.sql.Date.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaSQLDate_javaTimeLocalDate(java.sql.Date.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaSQLDate_javaTimeLocalTime(java.sql.Date.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);
        }
    },

    javaSQLDate_javaTimeLocalDateTime(java.sql.Date.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },


    /* *****************************************************************************************************************
     *                                                  java.sql.Time
     * *****************************************************************************************************************/
    javaSQLTime_String(java.sql.Time.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date, "HH:mm:ss");
        }
    },

    javaSQLTime_javaSQLDate(java.sql.Time.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },

    javaSQLTime_javaUtilDate(java.sql.Time.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaSQLTime_javaSQLTimestamp(java.sql.Time.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaSQLTime_javaTimeYear(java.sql.Time.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaSQLTime_javaTimeYearMonth(java.sql.Time.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaSQLTime_javaTimeMonth(java.sql.Time.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaSQLTime_javaTimeLocalDate(java.sql.Time.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaSQLTime_javaTimeLocalTime(java.sql.Time.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);
        }
    },

    javaSQLTime_javaTimeLocalDateTime(java.sql.Time.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },
    /* *****************************************************************************************************************
     *                                                  java.sql.Timestamp
     * *****************************************************************************************************************/


    javaSQLTimestamp_String(java.sql.Timestamp.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date);
        }
    },
    javaSQLTimestamp_javaSQLDate(java.sql.Timestamp.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaSQLTimestamp_javaUtilDate(java.sql.Timestamp.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaSQLTimestamp_javaSQLTimestampstamp(java.sql.Timestamp.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaSQLTimestamp_javaTimeLocalDate(java.sql.Timestamp.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaSQLTimestamp_javaTimeYear(java.sql.Timestamp.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaSQLTimestamp_javaTimeYearMonth(java.sql.Timestamp.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    jjavaSQLTimestamp_javaTimeMonth(java.sql.Timestamp.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaSQLTimestamp_javaTimeLocalTime(java.sql.Timestamp.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);
        }
    },

    javaSQLTimestamp_javaTimeLocalDateTime(java.sql.Timestamp.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                               array
     * =================================================================================================================
     * java.entity.Double
     *
     * ****************************************************************************************************************/
    Doubles_Point(Double[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = new Point((Double[])value);
            return point;
        }
    },
    double_Point(double[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = new Point((double[])value);
            return point;
        }
    },
    bytes_String(byte[].class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new String((byte[]) value);
        }
    },
    bytes_Point(byte[].class, Point.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
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
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = (Point) value;
            return point.bytes();
        }
    },
    Point_Doubles(Point.class, Double[].class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Point point = (Point) value;
            return point.getDoubles();
        }
    },
    Point_doubles(Point.class, double[].class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
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