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

import org.anyline.metadata.adapter.MetadataRefer;

import java.io.Serializable;

public class Database extends Metadata<Database> implements Serializable {
    protected String keyword = "DATABASE"           ;
    protected String charset                      ; // 编码
    protected String collate                      ; // 排序编码
    protected String filePath                     ; // 文件位置
    protected String logPath                      ; // 日志位置
    protected String user                         ; // 所属用户
    public Database() {}
    public Database(String name) {
        setName(name);
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

    public String toString() {
        return name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
    public String getKeyword() {
        return this.keyword;
    }


    public void setUser(String user) {
        this.user = user;
    }

}
