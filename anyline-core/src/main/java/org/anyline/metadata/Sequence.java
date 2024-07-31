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

import java.io.Serializable;

public class Sequence extends Metadata<Sequence> implements Serializable {
    protected String keyword = "SEQUENCE";
    private String name;
    private Long min = 0L;
    private Long max;
    private Long last;
    private boolean next = true;//NEXTVAL  CURRVAL
    private Long start = 1L;
    private int increment = 1;
    private int cache = 100;
    private Boolean cycle = false;
    private boolean fetchValueBeforeInsert = false; //在插入前先获取实际值

    public Sequence() {
    }
    public Sequence(Catalog catalog, Schema schema, String name) {
        this.catalog = catalog;
        this.schema = schema;
        setName(name);
    }
    public Sequence(String name) {
        setName(name);
    }
    public Sequence(String name, boolean next) {
        this.name = name;
        this.next = next;
    }

    public String sql() {
        if(next) {
            return name + "." + "NEXTVAL";
        }else{
            return name + "." + "CURRVAL";
        }
    }
    public Sequence setName(String name) {
        if(name.contains(".")) {
            String[] tmps = name.split("\\.");
            name = tmps[0];
            if(tmps[1].toUpperCase().contains("NEXT")) {
                next = true;
            }else{
                next = false;
            }
        }
        this.name = name;
        return this;
    }

    public boolean isNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public Long getStart() {
        return start;
    }

    public void setStart(Long start) {
        this.start = start;
    }

    public int getIncrement() {
        return increment;
    }

    public void setIncrement(int increment) {
        this.increment = increment;
    }

    public int getCache() {
        return cache;
    }

    public void setCache(int cache) {
        this.cache = cache;
    }

    public Long getMin() {
        return min;
    }

    public boolean isFetchValueBeforeInsert() {
        return fetchValueBeforeInsert;
    }

    public void setFetchValueBeforeInsert(boolean fetchValueBeforeInsert) {
        this.fetchValueBeforeInsert = fetchValueBeforeInsert;
    }
    public String getKeyword() {
        return this.keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setMin(Long min) {
        this.min = min;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(Long max) {
        this.max = max;
    }

    public Long getLast() {
        return last;
    }

    public void setLast(Long last) {
        this.last = last;
    }

    public Boolean isCycle() {
        return cycle;
    }

    public void setCycle(Boolean cycle) {
        this.cycle = cycle;
    }

}
