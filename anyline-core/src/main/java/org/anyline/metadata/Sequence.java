package org.anyline.metadata;

public class Sequence {
    private String name;
    private boolean next = true;//NEXTVAL  CURRVAL
    private int start = 0;
    private int increment = 1;
    private int cache = 100;
    private int min = 0;
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
    public void setName(String name) {
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
    }

    public boolean isNext() {
        return next;
    }

    public void setNext(boolean next) {
        this.next = next;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
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

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public boolean isFetchValueBeforeInsert() {
        return fetchValueBeforeInsert;
    }

    public void setFetchValueBeforeInsert(boolean fetchValueBeforeInsert) {
        this.fetchValueBeforeInsert = fetchValueBeforeInsert;
    }
}
