package org.anyline.data.jdbc.tdengine.metadata;

import org.anyline.metadata.PartitionTable;

import java.util.LinkedHashMap;

public class TDenginePartitionTable extends PartitionTable {
    protected LinkedHashMap<String, Object> usingTags = new LinkedHashMap<>();
    public LinkedHashMap<String, Object> getUsingTags(){
        return usingTags;
    }
    public TDenginePartitionTable setUsingTags(LinkedHashMap<String, Object> tags){
        this.usingTags = tags;
        return this;
    }
    public TDenginePartitionTable addUsingTag(String name, Object value){
        usingTags.put(name, value);
        return this;
    }
    public Object getUsingTag(String name){
        return usingTags.get(name);
    }

}
