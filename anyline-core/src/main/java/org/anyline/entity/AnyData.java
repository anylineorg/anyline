package org.anyline.entity;

import java.io.Serializable;

public interface AnyData<T> extends Serializable {
    /**
     * key转小写,转换后删除原来的key
     * @param recursion 是否递归执行(仅支持AnyData类型)
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    T toLowerKey(boolean recursion, String... keys);
    /**
     * key转小写,转换后删除原来的key
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    default T toLowerKey(String... keys){
        return toLowerKey(true, keys);
    }
    /**
     * key转小写,转换后删除原来的key
     * @param recursion 是否递归执行(仅支持AnyData类型)
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    T toUpperKey(boolean recursion, String... keys);

    /**
     * key转大写,转换后删除原来的key
     * @param keys 需要转换的key,如果不提供则转换全部
     * @return this
     */
    default T toUpperKey(String... keys){
        return toUpperKey(true, keys);
    }

    String toJSON();

}
