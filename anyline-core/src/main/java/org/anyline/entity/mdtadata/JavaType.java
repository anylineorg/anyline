package org.anyline.entity.mdtadata;

public interface JavaType {
    /**
     * 从数据库中读取数据,常用的基本类型可以自动转换,不常用的如json/point/polygon/blob等转换成anyline对应的类型
     * @param value value
     * @param clazz 目标数据类型(给entity赋值时可以根据class, DataRow赋值时可以指定class，否则按检测metadata类型转换 转换不不了的原样返回)
     * @return Object
     */
    public abstract Object read(Object value, Class clazz);
    /**
     * 写入数据库前类型转换<br/>
     * 如果有占位符成数据库可接受的Java数据类型<br/>
     * 如果没有占位符 需要确定加单引号或内置函数<br/>
     * @param placeholder 是否占位符
     * @param value value
     * @param def 默认值
     * @return Object
     */
    public abstract Object write(Object value, Object def, boolean placeholder);



    public abstract Class getJavaClass();
}
