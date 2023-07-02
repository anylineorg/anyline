package org.anyline.metadata;

import java.io.Serializable;

public class Database  implements Serializable {
    protected String name                         ; // 数据库名
    protected String charset                      ; // 编码
    protected String collate                      ; // 排序编码

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    public String getCollate() {
        return collate;
    }

    public void setCollate(String collate) {
        this.collate = collate;
    }
}
