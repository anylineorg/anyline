package org.anyline.data.jdbc.oracle;

import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.entity.metadata.ConvertException;
import org.anyline.entity.metadata.init.AbstractConvert;
import org.anyline.util.DateUtil;

import java.util.Date;

public class OracleConvert {
    public static void reg(){

        ConvertAdapter.reg(new AbstractConvert(String.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(String.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(String.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(String.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.util.Date.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = (Date)value;
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.util.Date.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = (Date)value;
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.util.Date.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = (Date)value;
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.util.Date.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = (Date)value;
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Date.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Date.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Date.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Date.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Time.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Time.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Time.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Time.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Timestamp.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Timestamp.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Timestamp.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.sql.Timestamp.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDate.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDate.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDate.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDate.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalTime.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalTime.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalTime.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalTime.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDateTime.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDateTime.class, oracle.sql.TIMESTAMP.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMP.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDateTime.class, oracle.sql.TIMESTAMPTZ.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.TIMESTAMPTZ.of(DateUtil.offsetDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(java.time.LocalDateTime.class, oracle.sql.DATE.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(value);
                    return oracle.sql.DATE.of(DateUtil.localDateTime(date));
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.format(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return date;
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.sqlDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.sqlTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.sqlTimestamp(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.localDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.localTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.localDateTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return DateUtil.format(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return date;
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return DateUtil.sqlDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return DateUtil.sqlTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return DateUtil.sqlTimestamp(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return DateUtil.localDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return DateUtil.localTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMP.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMP)value).timestampValue());
                    return DateUtil.localDateTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return DateUtil.format(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return date;
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return DateUtil.sqlDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return DateUtil.sqlTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return DateUtil.sqlTimestamp(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return DateUtil.localDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return DateUtil.localTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.TIMESTAMPTZ.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.TIMESTAMPTZ)value).timestampValue());
                    return DateUtil.localDateTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, String.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.format(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.util.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return date;
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.sql.Date.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.sqlDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.sql.Time.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.sqlTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.sql.Timestamp.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.sqlTimestamp(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.time.LocalDate.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.localDate(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.time.LocalTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.localTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
        ConvertAdapter.reg(new AbstractConvert(oracle.sql.DATE.class, java.time.LocalDateTime.class) {
            @Override
            public Object exe(Object value, Object def) throws ConvertException {
                try {
                    Date date = DateUtil.parse(((oracle.sql.DATE)value).timestampValue());
                    return DateUtil.localDateTime(date);
                } catch (Exception e) {
                    return value;
                }
            }
        });
    }
}
