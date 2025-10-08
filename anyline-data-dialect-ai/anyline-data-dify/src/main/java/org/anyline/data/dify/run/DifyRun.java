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

package org.anyline.data.dify.run;

import org.anyline.data.dify.datasource.DifyClient;
import org.anyline.data.dify.entity.Document;
import org.anyline.data.run.Run;
import org.anyline.data.run.TableRun;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.PageNavi;
import org.anyline.metadata.Table;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DifyRun extends TableRun implements Run {

    protected Table table;
    protected List<Document> documents;
    protected DifyClient client;
    protected String keyword;
    protected PageNavi navi;
    protected Map<String, Object> params = new HashMap<>();

    public DifyRun(DataRuntime runtime) {
        super(runtime, (Table)null);
    }

    public DifyRun(DataRuntime runtime, String table) {
        super(runtime, table);
    }

    public DifyRun(DataRuntime runtime, Table table) {
        super(runtime, table);
    }



    @Override
    public String format(String cmd) {
        //不要删除换行命令中有要求
        return cmd;
    }

    @Override
    public Table getTable() {
        return table;
    }

    @Override
    public void setTable(Table table) {
        this.table = table;
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public Map<String, Object> getQueryParams() {
        if(null != navi){
            params.put("page", navi.getCurPage());
            params.put("limit", navi.getPageRows());
        }
        if(null != keyword){
            params.put("keyword", keyword);
        }
        return params;
    }


    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public DifyClient getClient() {
        return client;
    }

    public void setClient(DifyClient client) {
        this.client = client;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public PageNavi getNavi() {
        return navi;
    }

    public void setNavi(PageNavi navi) {
        this.navi = navi;
    }
}
