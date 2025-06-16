/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.exception;

import org.anyline.metadata.type.DatabaseType;

import java.util.List;

public class AnylineException extends RuntimeException {
    private Exception cause;
    private int status;
    private String code;
    private String title;
    private String content;
    protected DatabaseType database;
    protected Object datasource;

    protected String cmd;
    protected List<Object> values;

    public AnylineException() {
        super();
    }
    public AnylineException(String title, Exception cause) {
        super(cause);
        this.title = title;
        this.cause = cause;
    }
    public AnylineException(int status, String code, String title, String content) {
        super(title);
        this.status = status;
        this.code = code;
        this.title = title;
        this.content = content;
    }
    public AnylineException(String code, String title, String content) {
        super(title);
        this.code = code;
        this.title = title;
        this.content = content;
    }
    public AnylineException(int status, String code, String title) {
        super(title);
        this.status = status;
        this.code = code;
        this.title = title;
    }
    public AnylineException(String code, String title) {
        super(title);
        this.code = code;
        this.title = title;
    }
    public AnylineException(int status, String code) {
        super(code);
        this.status = status;
        this.code = code;
    }
    public AnylineException(String code) {
        super(code);
        this.code = code;
    }
    public AnylineException(int status) {
        this.status = status;
    }
    public Exception getCause() {
        return this.cause;
    }

    public void setCause(final Exception cause) {
        this.cause = cause;
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public DatabaseType getDatabase() {
        return database;
    }

    public void setDatabase(DatabaseType database) {
        this.database = database;
    }

    public Object getDatasource() {
        return datasource;
    }

    public void setDatasource(Object datasource) {
        this.datasource = datasource;
    }


    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public List<Object> getValues() {
        return values;
    }

    public void setValues(List<Object> values) {
        this.values = values;
    }
}
