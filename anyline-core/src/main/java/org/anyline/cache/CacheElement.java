package org.anyline.cache;

public class CacheElement {
    private long createTime;    //创建时间
    private int expires;        //过期时间(毫秒)
    private Object value;       //value
    public CacheElement(){
        this.createTime = System.currentTimeMillis();
    }
    public long getCreateTime(){
        return this.createTime;
    }
    public void setCreateTime(long createTime){
        this.createTime = createTime;
    }
    public Object getValue(){
        return value;
    }
    public void setValue(Object value){
        this.value = value;
    }
    public int getExpires(){
        return this.expires;
    }
    public void setExpires(int expires){
        this.expires = expires;
    }

}
