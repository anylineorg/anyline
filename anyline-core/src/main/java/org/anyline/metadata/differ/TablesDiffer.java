package org.anyline.metadata.differ;

import org.anyline.metadata.Table;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 表或列之间的对比结果
 */
public class TablesDiffer implements MetadataDiffer {
    private List<Table> adds = new ArrayList<>();
    private List<Table> drops = new ArrayList<>();
    private List<Table> updates = new ArrayList<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests){
        TablesDiffer differ = new TablesDiffer();
        List<Table> adds = new ArrayList<>();
        List<Table> drops = new ArrayList<>();
        List<Table> updates = new ArrayList<>();

        if(null != origins){
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
                drops.add(origins.get(origin));
            }else {
                if(!origin.equals(dest)){
                    origin.setUpdate(dest, false, false);
                    updates.add(origin);
                }
            }
        }
        for(String key:dests.keySet()){
            if(!origins.containsKey(key)){
                adds.add(dests.get(key));
            }
        }
        differ.setAdds(adds);
        differ.setDrops(drops);
        differ.setUpdates(updates);
        return differ;
    }

    public List<Table> getAdds() {
        return adds;
    }

    public void setAdds(List<Table> adds) {
        this.adds = adds;
    }

    public List<Table> getDrops() {
        return drops;
    }

    public void setDrops(List<Table> drops) {
        this.drops = drops;
    }

    public List<Table> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Table> updates) {
        this.updates = updates;
    }
}
