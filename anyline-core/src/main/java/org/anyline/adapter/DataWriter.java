package org.anyline.adapter;

public interface DataWriter {
    /**
     * 写入数据库前类型转换(非基础类型时需要)
     * @param value value
     * @param placeholder 是否启动占位符
     * @return Object
     */
    public Object write(Object value, boolean placeholder);
}
