package org.anyline.metadata.differ;

import org.anyline.metadata.Procedure;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 表或列之间的对比结果
 */
public class ProceduresDiffer implements MetadataDiffer {
    private List<Procedure> adds = new ArrayList<>();
    private List<Procedure> drops = new ArrayList<>();
    private List<Procedure> updates = new ArrayList<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static ProceduresDiffer compare(LinkedHashMap<String, Procedure> origins, LinkedHashMap<String, Procedure> dests){
        ProceduresDiffer differ = new ProceduresDiffer();
        List<Procedure> adds = new ArrayList<>();
        List<Procedure> drops = new ArrayList<>();
        List<Procedure> updates = new ArrayList<>();

        if(null != origins){
            origins = new LinkedHashMap<>();
        }
        if(null == dests){
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()){
            Procedure origin = origins.get(key);
            Procedure dest = dests.get(key);
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

    public List<Procedure> getAdds() {
        return adds;
    }

    public void setAdds(List<Procedure> adds) {
        this.adds = adds;
    }

    public List<Procedure> getDrops() {
        return drops;
    }

    public void setDrops(List<Procedure> drops) {
        this.drops = drops;
    }

    public List<Procedure> getUpdates() {
        return updates;
    }

    public void setUpdates(List<Procedure> updates) {
        this.updates = updates;
    }
}
