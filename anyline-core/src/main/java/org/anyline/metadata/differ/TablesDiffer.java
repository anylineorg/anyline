package org.anyline.metadata.differ;

import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

/**
 * 表或列之间的对比结果
 */
public class TablesDiffer implements MetadataDiffer {
    private LinkedHashMap<String, Table> adds = new LinkedHashMap<>();
    private LinkedHashMap<String, Table> drops = new LinkedHashMap<>();
    private LinkedHashMap<String, Table> updates = new LinkedHashMap<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests){
        return compare(origins, dests, true);
    }
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests, boolean ignoreSchema){
        TablesDiffer differ = new TablesDiffer();
        LinkedHashMap<String, Table> adds = new LinkedHashMap<>();
        LinkedHashMap<String, Table> drops = new LinkedHashMap<>();
        LinkedHashMap<String, Table> updates = new LinkedHashMap<>();

        if(null == origins){
            origins = new LinkedHashMap<>();
        }
        if(null == dests){
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()){
            Table origin = origins.get(key);
            Table dest = dests.get(key);
            if(null == dest){
                //新表不存在
                drops.put(key, origins.get(origin));
            }else {
                if(!origin.equals(dest, true, ignoreSchema)){
                    origin.setUpdate(dest, false, false);
                    updates.put(key, origin);
                }
            }
        }
        for(String key:dests.keySet()){
            if(!origins.containsKey(key)){
                adds.put(key, dests.get(key));
            }
        }
        differ.setAdds(adds);
        differ.setDrops(drops);
        differ.setUpdates(updates);
        return differ;
    }

    public LinkedHashMap<String, Table> getAdds() {
        return adds;
    }

    public void setAdds(LinkedHashMap<String, Table> adds) {
        this.adds = adds;
    }

    public LinkedHashMap<String, Table> getDrops() {
        return drops;
    }

    public void setDrops(LinkedHashMap<String, Table> drops) {
        this.drops = drops;
    }

    public LinkedHashMap<String, Table> getUpdates() {
        return updates;
    }

    public void setUpdates(LinkedHashMap<String, Table> updates) {
        this.updates = updates;
    }
}
