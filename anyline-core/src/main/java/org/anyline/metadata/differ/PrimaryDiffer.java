package org.anyline.metadata.differ;

import org.anyline.metadata.PrimaryKey;
import org.anyline.metadata.PrimaryKey;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 表或列之间的对比结果
 */
public class PrimaryDiffer implements MetadataDiffer {
    private List<PrimaryKey> adds = new ArrayList<>();
    private List<PrimaryKey> drops = new ArrayList<>();
    private List<PrimaryKey> updates = new ArrayList<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static PrimaryDiffer compare(LinkedHashMap<String, PrimaryKey> origins, LinkedHashMap<String, PrimaryKey> dests){
        PrimaryDiffer differ = new PrimaryDiffer();
        List<PrimaryKey> adds = new ArrayList<>();
        List<PrimaryKey> drops = new ArrayList<>();
        List<PrimaryKey> updates = new ArrayList<>();

        if(null != origins){
            origins = new LinkedHashMap<>();
        }
        if(null == dests){
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()){
            PrimaryKey origin = origins.get(key);
            PrimaryKey dest = dests.get(key);
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

    public List<PrimaryKey> getAdds() {
        return adds;
    }

    public void setAdds(List<PrimaryKey> adds) {
        this.adds = adds;
    }

    public List<PrimaryKey> getDrops() {
        return drops;
    }

    public void setDrops(List<PrimaryKey> drops) {
        this.drops = drops;
    }

    public List<PrimaryKey> getUpdates() {
        return updates;
    }

    public void setUpdates(List<PrimaryKey> updates) {
        this.updates = updates;
    }
}
