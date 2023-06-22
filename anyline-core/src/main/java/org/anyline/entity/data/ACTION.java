package org.anyline.entity.data;

public interface ACTION {
    enum DML{
        SELECT,
        INSERT,
        UPDATE,
        DELETE,
        EXISTS,
        EXECUTE,
        PROCEDURE
    }
    enum DDL{
        TABLE_CREATE    ("表创建"),
        TABLE_ALTER     ("表结构修改"),
        TABLE_DROP      ("表删除"),
        TABLE_RENAME    ("表重命名"),
        TABLE_COMMENT   ("表修改备注"),

        VIEW_CREATE   ("视图创建"),
        VIEW_ALTER    ("视图修改"),
        VIEW_DROP     ("视图删除"),
        VIEW_RENAME   ("视图重命名"),

        MASTER_TABLE_CREATE   ("超表创建"),
        MASTER_TABLE_ALTER    ("超表修改"),
        MASTER_TABLE_DROP     ("超表删除"),
        MASTER_TABLE_RENAME   ("超表重命名"),

        PARTITION_TABLE_CREATE   ("子表(分区表)创建"),
        PARTITION_TABLE_ALTER    ("子表(分区表)修改"),
        PARTITION_TABLE_DROP     ("子表(分区表)删除"),
        PARTITION_TABLE_RENAME   ("子表(分区表)重命名"),

        COLUMN_ADD   ("列创建"),
        COLUMN_ALTER    ("列结构修改"),
        COLUMN_DROP     ("列删除"),
        COLUMN_RENAME   ("列重命名"),

        TAG_ADD   ("标签(子表列)创建"),
        TAG_ALTER    ("标签(子表列)结构修改"),
        TAG_DROP     ("标签(子表列)删除"),
        TAG_RENAME   ("标签(子表列)重命名"),

        PRIMARY_ADD   ("主键创建"),
        PRIMARY_ALTER    ("主键修改"),
        PRIMARY_DROP     ("主键删除"),
        PRIMARY_RENAME   ("主键重命名"),

        FOREIGN_ADD   ("外键创建"),
        FOREIGN_ALTER    ("外键修改"),
        FOREIGN_DROP     ("外键删除"),
        FOREIGN_RENAME   ("外键重命名"),

        INDEX_ADD   ("索引创建"),
        INDEX_ALTER    ("索引修改"),
        INDEX_DROP     ("索引删除"),
        INDEX_RENAME   ("索引重命名"),

        CONSTRAINT_ADD   ("约束添加"),
        CONSTRAINT_ALTER    ("约束修改"),
        CONSTRAINT_DROP     ("约束删除"),
        CONSTRAINT_RENAME   ("约束重命名"),

        PROCEDURE_CREATE   ("存储过程创建"),
        PROCEDURE_ALTER    ("存储过程修改"),
        PROCEDURE_DROP     ("存储过程删除"),
        PROCEDURE_RENAME   ("存储过程重命名"),

        FUNCTION_CREATE   ("函数创建"),
        FUNCTION_ALTER    ("函数修改"),
        FUNCTION_DROP     ("函数删除"),
        FUNCTION_RENAME   ("函数重命名"),

        TRIGGER_ADD   ("触发器创建"),
        TRIGGER_ALTER    ("触发器修改"),
        TRIGGER_DROP     ("触发器删除"),
        TRIGGER_RENAME   ("触发器重命名"),
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
     * 
     * */
    enum SWITCH {
        CONTINUE("正常执行"),
        SKIP("跳过后续prepare/before/after"), //before1返回SKIP则跳过before2,before3,但不影响after2,after3,
        BREAK("中断主流程执行")
        ;
        private final String title;
        SWITCH(String title){
            this.title = title;
        }
    }
}
