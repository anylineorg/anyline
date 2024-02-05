package org.anyline.data.handler;

import java.util.Map;

public interface MapHandler extends StreamHandler{

    /**
     * 在while(ResultSet.next())遍历中调用
     * @param map 返回通过ResultSet中的一行
     * @return boolean 返回false表示中断遍历,read方法不再再次被调用
     */
    boolean read(Map<String,Object> map);
}
