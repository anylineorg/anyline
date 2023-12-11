package org.anyline.data.handler;

import java.util.Map;

public interface MapHandler extends StreamHandler{

    /**
     *
     * @param map 一行
     * @return 返回false中断遍历
     */
    boolean read(Map<String,Object> map);
}
