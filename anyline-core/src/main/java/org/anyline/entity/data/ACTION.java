package org.anyline.entity.data;

public interface ACTION {
    enum DML{
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        EXISTS,
        EXECUTE
    }
    enum DDL{
        TABLE_CREATE    ("表创建"),
        TABLE_ALTER     ("表结构修改"),
        TABLE_DROP      ("表删除"),
        TABLE_RENAME    ("表重命名"),
        TABLE_COMMENT   ("表修改备注"),

        COLUMN_CREATE   ("列创建"),
        COLUMN_ALTER    ("列结构修改"),
        COLUMN_DROP     ("列删除"),
        COLUMN_RENAME   ("列重命名"),
        ;
        private final String title;
        DDL(String title){
            this.title = title;
        }
    }

    /*
     * before1
     * before2
     * before3
     * EXE
     * after1
     * after2
     * after3
     * */
    enum SWITCH {
        CONINUE("正常执行"),
        SKIP("跳过后续prepare/before/after"), //before1返回SKIP则跳过before2,before3,但不影响after2,after3,
        BREAK("中断主流程执行")
        ;
        private final String title;
        SWITCH(String title){
            this.title = title;
        }
    }
}
