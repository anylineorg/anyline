package org.anyline.adapter;

public interface DataReader {
    /**
     * 从数据库中读取数据(非基础类型时需要)
     * @param value value
     * @return Object
     */
    public Object read(Object value);
}
