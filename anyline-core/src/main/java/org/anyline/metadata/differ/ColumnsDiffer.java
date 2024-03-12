package org.anyline.metadata.differ;

import org.anyline.metadata.Column;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ColumnsDiffer implements MetadataDiffer{
    private List<Column> adds = new ArrayList<>();
    private List<Column> drops = new ArrayList<>();
    private List<Column> updates = new ArrayList<>();

    public static ColumnsDiffer compare(LinkedHashMap<String, Column> origins, LinkedHashMap<String, Column> dests){
        ColumnsDiffer differ = new ColumnsDiffer();
        List<Column> adds = new ArrayList<>();
        List<Column> drops = new ArrayList<>();
        List<Column> updates = new ArrayList<>();

        if(null != origins){
            origins = new LinkedHashMap<>();
        }
        if(null == dests){
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()){
            Column origin = origins.get(key);
            Column dest = dests.get(key);
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

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }

    public List<Column> getAdds() {
        return adds;
    }

    public void setAdds(List<Column> adds) {
        this.adds = adds;
    }

    public List<Column> getDrops() {
        return drops;
    }

    public void setDrops(List<Column> drops) {
        this.drops = drops;
    }

    public List<Column> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Column> updates) {
        this.updates = updates;
    }
}
