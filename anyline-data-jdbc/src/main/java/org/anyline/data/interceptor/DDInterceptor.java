package org.anyline.data.interceptor;

import java.util.List;

public interface DDInterceptor  extends JDBCInterceptor{
    enum ACTION{
        TABLE_CREATE    ("表创建"),
        TABLE_ALTER     ("表结构修改"),
        TABLE_DROP      ("表删除"),
        TABLE_RENAME    ("表重命名"),

        COLUMN_CREATE   ("列创建"),
        COLUMN_ALTER    ("列结构修改"),
        COLUMN_DROP     ("列删除"),
        COLUMN_RENAME   ("列重命名"),
        ;
        private final String title;
        ACTION(String title){
            this.title = title;
        }
    }


    /**
     * 可触发当前拦截器的事件<br/>
     * 拦截多个事件的实现actions(),拦截一个事件的实现action()
     * @return List
     */
    default List<ACTION> actions(){return null;}
    default ACTION action(){return null;}

    int before(Runtime runtime, Object entity);

}
