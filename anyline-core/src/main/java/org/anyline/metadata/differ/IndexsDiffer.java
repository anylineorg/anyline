package org.anyline.metadata.differ;

import org.anyline.metadata.Index;
import org.anyline.metadata.Index;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class IndexsDiffer implements MetadataDiffer {
    private List<Index> adds = new ArrayList<>();
    private List<Index> drops = new ArrayList<>();
    private List<Index> updates = new ArrayList<>();

    public static IndexsDiffer compare(LinkedHashMap<String, Index> origins, LinkedHashMap<String, Index> dests){
        IndexsDiffer differ = new IndexsDiffer();
        List<Index> adds = new ArrayList<>();
        List<Index> drops = new ArrayList<>();
        List<Index> updates = new ArrayList<>();

        if(null != origins){
            origins = new LinkedHashMap<>();
        }
        if(null == dests){
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()){
            Index origin = origins.get(key);
            Index dest = dests.get(key);
            if(null == dest){
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

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }

    public List<Index> getAdds() {
        return adds;
    }

    public void setAdds(List<Index> adds) {
        this.adds = adds;
    }

    public List<Index> getDrops() {
        return drops;
    }

    public void setDrops(List<Index> drops) {
        this.drops = drops;
    }

    public List<Index> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Index> updates) {
        this.updates = updates;
    }
}
