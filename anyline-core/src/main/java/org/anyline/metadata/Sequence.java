package org.anyline.metadata;

import java.io.Serializable;

public class Sequence extends BaseMetadata<Sequence> implements Serializable {
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

    public Sequence(){
    }
    public Sequence(String name){
        setName(name);
    }
    public Sequence(String name, boolean next){
        this.name = name;
        this.next = next;
    }
    public String getName() {
        return name;
    }
    public String sql(){
        if(next) {
            return name + "." + "NEXTVAL";
        }else{
            return name + "." + "CURRVAL";
        }
    }
    public Sequence setName(String name) {
        if(name.contains(".")){
            String[] tmps = name.split("\\.");
            name = tmps[0];
            if(tmps[1].toUpperCase().contains("NEXT")){
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
