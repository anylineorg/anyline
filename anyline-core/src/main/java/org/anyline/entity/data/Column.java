package org.anyline.entity.data;

import org.anyline.entity.metadata.ColumnType;
import org.anyline.entity.metadata.JavaType;

import java.io.Serializable;

public interface Column extends Serializable {
    public String getCatalog();

    public String getClassName();

    public Integer getDisplaySize();

    public String getComment();

    public String getName();
    public String getOriginalName();

    public Integer getType();

    public Table getTable();
    public String getTypeName();

    public Integer getPrecision();

    public String getSchema();
    public String getTableName();

    public int isCaseSensitive();

    public int isCurrency();

    public int isSigned();
    public Integer getScale();

    public int isNullable();

    public int isAutoIncrement();
    public int isPrimaryKey();

    public int isGenerated();

    public Object getDefaultValue();

    public Integer getPosition();

    public String getOrder();

    public String getBefore() ;
    public String getAfter() ;

    public Integer getIncrementSeed();
    public Integer getIncrementStep();
    public int isOnUpdate();

    public String getCharset();

    public String getCollate();
    public void delete();
    public void drop();
    public boolean isDelete();
    public boolean isDrop();
    public void setDelete(boolean drop);
    public void setDrop(boolean drop);
    //完整的数据类型 varchar(32)
    public String getFullType();
    public boolean equals(Column column);
    public ColumnType getColumnType();
    public JavaType getJavaType();
    public void setColumnType(ColumnType type);
    public void setJavaType(JavaType type);

    /**
     * 日期类型的精度
     * YEAR：用于表示年份。
     * MONTH：用于表示年和月。
     * DAY：用于表示年、月和日。
     * HOUR：用于表示年、月、日和小时。
     * MINUTE：用于表示年、月、日、小时和分钟。
     * SECOND：用于表示年、月、日、小时、分钟和秒。
     * FRACTION：用于表示年、月、日、小时、分钟、秒和更高精度的分数部分。
     * @return String
     */
    public String getDateScale();
}
