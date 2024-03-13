package org.anyline.metadata.differ;

import org.anyline.metadata.Trigger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 表或列之间的对比结果
 */
public class TriggersDiffer implements MetadataDiffer {
    private List<Trigger> adds = new ArrayList<>();
    private List<Trigger> drops = new ArrayList<>();
    private List<Trigger> updates = new ArrayList<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static TriggersDiffer compare(LinkedHashMap<String, Trigger> origins, LinkedHashMap<String, Trigger> dests){
        TriggersDiffer differ = new TriggersDiffer();
        List<Trigger> adds = new ArrayList<>();
        List<Trigger> drops = new ArrayList<>();
        List<Trigger> updates = new ArrayList<>();

        if(null != origins){
            origins = new LinkedHashMap<>();
        }
        if(null == dests){
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()){
            Trigger origin = origins.get(key);
            Trigger dest = dests.get(key);
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

    public List<Trigger> getAdds() {
        return adds;
    }

    public void setAdds(List<Trigger> adds) {
        this.adds = adds;
    }

    public List<Trigger> getDrops() {
        return drops;
    }

    public void setDrops(List<Trigger> drops) {
        this.drops = drops;
    }

    public List<Trigger> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Trigger> updates) {
        this.updates = updates;
    }
}
