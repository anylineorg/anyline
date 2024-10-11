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

package org.anyline.entity;

import java.util.ArrayList;
import java.util.List;

public class DefaultHavingStore implements HavingStore{

    private List<Having> list = new ArrayList<>();
    @Override
    public List<Having> gets() {
        return list;
    }

    @Override
    public void add(Having having) {
        list.add(having);
    }

    @Override
    public void add(String having) {
        list.add(new Having(having));
    }

    @Override
    public String getRunText() {
        StringBuilder builder = new StringBuilder();
        if(!isEmpty()){
            builder.append("HAVING ");
        }
        boolean first = true;
        for(Having having:list){
            if(!first){
                builder.append(" AND ");
            }
            first = false;
            builder.append(having.text());
        }
        return builder.toString();
    }

    @Override
    public void clear() {
        list.clear();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
    public DataRow map(boolean empty) {
        DataRow row = new OriginRow();
        return row;
    }
    public List<DataRow> list(boolean empty){
        List<DataRow> list = new ArrayList<>();
        for(Having having:this.list){
            list.add(having.map(empty));
        }
        return list;
    }

    @Override
    public HavingStore clone() {
        HavingStore store = new DefaultHavingStore();
        for(Having having:list){
            store.add(having);
        }
        return store;
    }
}
