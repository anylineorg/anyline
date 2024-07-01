/*
 * Copyright 2006-2023 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package org.anyline.metadata;

public interface ACTION {
    enum DML implements ACTION{
        SELECT,
        COUNT,
        INSERT,
        UPDATE,
        DELETE,
        EXISTS,
        EXECUTE,
        PROCEDURE
    }
    enum DDL implements ACTION{
        IGNORE    (CMD.IGNORE, "忽略"),
        TABLE_CREATE    (CMD.CREATE, "表创建"),
        TABLE_ALTER     (CMD.ALTER, "表结构修改"),
        TABLE_DROP      (CMD.DROP, "表删除"),
        TABLE_RENAME    (CMD.RENAME, "表重命名"),
        TABLE_COMMENT   (CMD.ALTER, "表修改备注"),

        VIEW_CREATE   (CMD.CREATE, "视图创建"),
        VIEW_ALTER    (CMD.ALTER, "视图修改"),
        VIEW_DROP     (CMD.DROP, "视图删除"),
        VIEW_RENAME   (CMD.RENAME, "视图重命名"),

        MASTER_TABLE_CREATE   (CMD.CREATE, "超表创建"),
        MASTER_TABLE_ALTER    (CMD.ALTER, "超表修改"),
        MASTER_TABLE_DROP     (CMD.DROP, "超表删除"),
        MASTER_TABLE_RENAME   (CMD.RENAME, "超表重命名"),

        PARTITION_TABLE_CREATE   (CMD.CREATE, "子表(分区表)创建"),
        PARTITION_TABLE_ALTER    (CMD.ALTER, "子表(分区表)修改"),
        PARTITION_TABLE_DROP     (CMD.DROP, "子表(分区表)删除"),
        PARTITION_TABLE_RENAME   (CMD.RENAME, "子表(分区表)重命名"),

        COLUMN_ADD   (CMD.CREATE, "列创建"),
        COLUMN_ALTER    (CMD.ALTER, "列结构修改"),
        COLUMN_DROP     (CMD.DROP, "列删除"),
        COLUMN_RENAME   (CMD.RENAME, "列重命名"),

        TAG_ADD   (CMD.CREATE, "标签(子表列)创建"),
        TAG_ALTER    (CMD.ALTER, "标签(子表列)结构修改"),
        TAG_DROP     (CMD.DROP, "标签(子表列)删除"),
        TAG_RENAME   (CMD.RENAME, "标签(子表列)重命名"),

        PRIMARY_ADD   (CMD.CREATE, "主键创建"),
        PRIMARY_ALTER    (CMD.ALTER, "主键修改"),
        PRIMARY_DROP     (CMD.DROP, "主键删除"),
        PRIMARY_RENAME   (CMD.RENAME, "主键重命名"),

        FOREIGN_ADD   (CMD.CREATE, "外键创建"),
        FOREIGN_ALTER    (CMD.ALTER, "外键修改"),
        FOREIGN_DROP     (CMD.DROP, "外键删除"),
        FOREIGN_RENAME   (CMD.RENAME, "外键重命名"),

        INDEX_ADD   (CMD.CREATE, "索引创建"),
        INDEX_ALTER    (CMD.ALTER, "索引修改"),
        INDEX_DROP     (CMD.DROP, "索引删除"),
        INDEX_RENAME   (CMD.RENAME, "索引重命名"),

        CONSTRAINT_ADD   (CMD.CREATE, "约束添加"),
        CONSTRAINT_ALTER    (CMD.ALTER, "约束修改"),
        CONSTRAINT_DROP     (CMD.DROP, "约束删除"),
        CONSTRAINT_RENAME   (CMD.RENAME, "约束重命名"),

        PROCEDURE_CREATE   (CMD.CREATE, "存储过程创建"),
        PROCEDURE_ALTER    (CMD.ALTER, "存储过程修改"),
        PROCEDURE_DROP     (CMD.DROP, "存储过程删除"),
        PROCEDURE_RENAME   (CMD.RENAME, "存储过程重命名"),

        FUNCTION_CREATE   (CMD.CREATE, "函数创建"),
        FUNCTION_ALTER    (CMD.ALTER, "函数修改"),
        FUNCTION_DROP     (CMD.DROP, "函数删除"),
        FUNCTION_RENAME   (CMD.RENAME, "函数重命名"),

        SEQUENCE_CREATE   (CMD.CREATE, "序列创建"),
        SEQUENCE_ALTER    (CMD.ALTER, "序列修改"),
        SEQUENCE_DROP     (CMD.DROP, "序列删除"),
        SEQUENCE_RENAME   (CMD.RENAME, "序列重命名"),

        TRIGGER_ADD   (CMD.CREATE, "触发器创建"),
        TRIGGER_ALTER    (CMD.ALTER, "触发器修改"),
        TRIGGER_DROP     (CMD.DROP, "触发器删除"),
        TRIGGER_RENAME   (CMD.RENAME, "触发器重命名"),
        ;
        private final String title;
        private final CMD cmd;
        DDL(CMD cmd, String title) {
            this.cmd = cmd;
            this.title = title;
        }
        public CMD getCmd() {
            return cmd;
        }
        public String getTitle() {
            return this.title;
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
        SKIP("跳过后续prepare/before/after"), //跞当前命令，执行下一个，before1返回SKIP则跳过exe1 after1,同时跳过其他拦截器 //before2, before3
        BREAK("中断主流程执行") //SKIP + 当前命令组内其他命令中断执行
        ;
        private final String title;
        SWITCH(String title) {
            this.title = title;
        }
        public String getTitle() {
            return this.title;
        }
    }

}
