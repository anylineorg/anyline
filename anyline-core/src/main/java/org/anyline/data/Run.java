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

package org.anyline.data;

import org.anyline.entity.Compare;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.*;
import org.anyline.metadata.type.TypeMetadata;

import java.util.LinkedHashMap;
import java.util.List;

public interface Run {
    void init();
    void slice(boolean slice);
    boolean slice();

    /**
     * 过滤条件是否为空
     * @return boolean
     */
    boolean isEmptyCondition();

    /**
     * 获取行数
     * @return 未设置行数的返回-1
     */
    long getRows();

    Table getTable();
    Run setText(String text);
    String getText();
    Catalog getCatalog();
    Schema getSchema();
    String getTableName();
    String getCatalogName();
    String getSchemaName();
    String getDest();
    List<String> getInsertColumns();
    LinkedHashMap<String, Column> getInsertColumns(boolean metadata);
    List<String> getUpdateColumns();
    LinkedHashMap<String, Column> getUpdateColumns(boolean metadata);
    String getBaseQuery(Boolean placeholder) ;
    default String getBaseQuery() {
        return getBaseQuery(true);
    }
    String getFinalQuery(Boolean placeholder);
    default String getFinalQuery() {
        return getFinalQuery(true);
    }

    String getTotalQuery(Boolean placeholder) ;
    default String getTotalQuery() {
        return getTotalQuery(true);
    }
    String getFinalExists(Boolean placeholder);
    default String getFinalExists() {
        return getFinalExists(true);
    }
    String getFinalInsert(Boolean placeholder);
    default String getFinalInsert() {
        return getFinalInsert(true);
    }
    String getFinalDelete(Boolean placeholder);
    default String getFinalDelete() {
        return getFinalDelete(true);
    }
    String getFinalUpdate(Boolean placeholder);
    default String getFinalUpdate() {
        return getFinalUpdate(true);
    }
    String getFinalExecute(Boolean placeholder);
    default String getFinalExecute() {
        return getFinalExecute(true);
    }

    /**
     * SQL是否支持换行
     * @return boolean
     */
    default boolean supportBr() {
        return true;
    }
    void supportBr(boolean support);

    List<Object> getValues() ;
    PageNavi getPageNavi() ;
    void setPageNavi(PageNavi pageNavi) ;
    String getQueryColumn();

    Compare.EMPTY_VALUE_SWITCH getStrict();

    void setSwt(Compare.EMPTY_VALUE_SWITCH swt);
    boolean isValid();
    boolean checkValid();
    void setValid(boolean valid);
    StringBuilder getBuilder();
    void setBuilder(StringBuilder builder);
    //1-DataRow 2-Entity
    int getOriginType();
    void setOriginType(int from);
    boolean isSetValue(String condition, String variable);
    boolean isSetValue(String variable);

    List<String> getQueryColumns();

    List<String> getExcludeColumns();

    Object getValue();
    void setValueType(TypeMetadata type);
    TypeMetadata getValueType();
    String log(ACTION.DML action, Boolean placeholder);

}
