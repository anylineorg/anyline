/*
 * Copyright 2006-2022 www.anyline.org
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
 *
 *
 */
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
