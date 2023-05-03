package org.anyline.entity.metadata.init;

import org.anyline.entity.Point;
import org.anyline.entity.metadata.Convert;
import org.anyline.entity.metadata.ConvertException;
import org.anyline.util.BasicUtil;
import org.anyline.util.DateUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.Date;

public enum DefaultConvert implements Convert {

    /* *****************************************************************************************************************
     *                                               String
     * ==================================================================================================================
     * byte
     * date
     * number
     * date
     * *****************************************************************************************************************/
    String_bytes(String.class, byte[].class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return value.toString().getBytes();
        }
    },
    String_Integer(String.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseInt(value.toString());
            }catch (Exception e){
                return value;
            }
        }
    },
    String_int(String.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseInt(value.toString()).intValue();
            }catch (Exception e){
                return value;
            }
        }
    },
    String_Long(String.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseLong(value);
            }catch (Exception e){
                return value;
            }
        }
    },
    String_Double(String.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseDouble(value, null);
            }catch (Exception e){
                return value;
            }
        }
    },
    String_double(String.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseDouble(value, null).doubleValue();
            }catch (Exception e){
                return value;
            }
        }
    },
    String_Float(String.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseFloat(value, null);
            }catch (Exception e){
                return value;
            }
        }
    },
    String_float(String.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseFloat(value, null).floatValue();
            }catch (Exception e){
                return value;
            }
        }
    },
    String_Decimal(String.class, BigDecimal.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            try {
                return BasicUtil.parseDecimal(value, null);
            }catch (Exception e){
                return value;
            }
        }
    },
    String_javaUtilDate(String.class, Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return DateUtil.parse(value);
        }
    },

    /* *****************************************************************************************************************
     *                                                  number
     * *****************************************************************************************************************/
    BigInteger_int(BigInteger.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).intValue();
        }
    },
    BigInteger_Integer(BigInteger.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).intValue();
        }
    },
    BigInteger_long(BigInteger.class, long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).longValue();
        }
    },
    BigInteger_Long(BigInteger.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).longValue();
        }
    },
    BigInteger_float(BigInteger.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).floatValue();
        }
    },
    BigInteger_Float(BigInteger.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).floatValue();
        }
    },
    BigInteger_double(BigInteger.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).doubleValue();
        }
    },
    BigInteger_Double(BigInteger.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((BigInteger)value).doubleValue();
        }
    },
    Integer_int(Integer.class, int.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Integer)value).intValue();
        }
    },
    int_Integer(int.class, Integer.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Integer((int)value);
        }
    },
    Float_float(Float.class, float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Float)value).floatValue();
        }
    },
    float_Float(float.class, Float.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Float((float)value);
        }
    },
    Double_double(Double.class, double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((Double)value).doubleValue();
        }
    },
    double_Double(double.class, Double.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return new Double((double)value);
        }
    },

    string_javaUtilDate(String.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    string_javaSQLDate(String.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    string_javaSQLTime(String.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    string_javaSQLTimestamp(String.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    string_javaTimeYear(String.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    string_javaTimeYearMonth(String.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    string_javaTimeMonth(String.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    string_javaTimeLocalDate(String.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    string_javaTimeLocalTime(String.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

        }
    },

    string_javaTimeLocalDateTime(String.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },
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

    javaSQLTimestamp_Long(java.sql.Timestamp.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            return ((java.sql.Timestamp)value).getTime();
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
     *                                                  java.time.LocalDate
     * *****************************************************************************************************************/

    javaTimeLocalDate_String(java.time.LocalDate.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date,"yyyy-MM-dd");
        }
    },
    javaTimeLocalDate_javaUtilDate(java.time.LocalDate.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaTimeLocalDate_javaSQLDate(java.time.LocalDate.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaTimeLocalDate_javaSQLTime(java.time.LocalDate.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaTimeLocalDate_javaSQLTimestamp(java.time.LocalDate.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaTimeLocalDate_javaTimeYear(java.time.LocalDate.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaTimeLocalDate_javaTimeYearMonth(java.time.LocalDate.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaTimeLocalDate_javaTimeMonth(java.time.LocalDate.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaTimeLocalDate_javaTimeLocalDate(java.time.LocalDate.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaTimeLocalDate_javaTimeLocalTime(java.time.LocalDate.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

        }
    },

    javaTimeLocalDate_javaTimeLocalDateTime(java.time.LocalDate.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                                  java.time.LocalTime
     * *****************************************************************************************************************/

    javaTimeLocalTime_String(java.time.LocalTime.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date,"HH:mm:ss");
        }
    },
    javaTimeLocalTime_javaUtilDate(java.time.LocalTime.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaTimeLocalTime_javaSQLDate(java.time.LocalTime.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaTimeLocalTime_javaSQLTime(java.time.LocalTime.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaTimeLocalTime_javaSQLTimestamp(java.time.LocalTime.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaTimeLocalTime_javaTimeYear(java.time.LocalTime.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaTimeLocalTime_javaTimeYearMonth(java.time.LocalTime.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaTimeLocalTime_javaTimeMonth(java.time.LocalTime.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaTimeLocalTime_javaTimeLocalTime(java.time.LocalTime.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

        }
    },

    javaTimeLocalTime_javaTimeLocalDateTime(java.time.LocalTime.class, java.time.LocalDateTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDateTime(date);
        }
    },

    /* *****************************************************************************************************************
     *                                                  java.time.LocalDateTime
     * *****************************************************************************************************************/

    javaTimeLocalDateTime_String(java.time.LocalDateTime.class, String.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.format(date);
        }
    },
    javaTimeLocalDateTime_Long(java.time.LocalDateTime.class, Long.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date.getTime();
        }
    },
    javaTimeLocalDateTime_javaUtilDate(java.time.LocalDateTime.class, java.util.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return date;
        }
    },
    javaTimeLocalDateTime_javaSQLDate(java.time.LocalDateTime.class, java.sql.Date.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlDate(date);
        }
    },
    javaTimeLocalDateTime_javaSQLTime(java.time.LocalDateTime.class, java.sql.Time.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTime(date);
        }
    },
    javaTimeLocalDateTime_javaSQLTimestamp(java.time.LocalDateTime.class, java.sql.Timestamp.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.sqlTimestamp(date);
        }
    },
    javaTimeLocalDateTime_javaTimeYear(java.time.LocalDateTime.class, java.time.Year.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Year year = Year.of(DateUtil.year(date));
            return year;
        }
    },
    javaTimeLocalDateTime_javaTimeYearMonth(java.time.LocalDateTime.class, java.time.YearMonth.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            YearMonth yearMonth = YearMonth.of(DateUtil.year(date), DateUtil.month(date));
            return yearMonth;
        }
    },
    javaTimeLocalDateTime_javaTimeMonth(java.time.LocalDateTime.class, java.time.Month.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            Month month = Month.of(DateUtil.month(date));
            return month;
        }
    },
    javaTimeLocalDateTime_javaTimeLocalDateTime(java.time.LocalDateTime.class, java.time.LocalDate.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localDate(date);
        }
    },
    javaTimeLocalDateTime_javaTimeLocalTime(java.time.LocalDateTime.class, java.time.LocalTime.class){
        @Override
        public Object exe(Object value, Object def) throws ConvertException {
            Date date = DateUtil.parse(value);
            return DateUtil.localTime(date);

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
    /* *****************************************************************************************************************
     *                                               Point
     * ==================================================================================================================
     * java.entity.Point
     *
     * *****************************************************************************************************************/
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