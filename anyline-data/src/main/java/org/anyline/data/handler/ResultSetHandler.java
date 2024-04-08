package org.anyline.data.handler;

import java.sql.ResultSet;

public interface ResultSetHandler extends StreamHandler{
    /**
     * 在while(ResultSet.next())遍历中调用，注意不要在方法中再调用next()方法
     * @param result 结果集
     * @return boolean 返回false表示中断遍历,read方法不再再次被调用
     */
    boolean read(ResultSet result);
}
