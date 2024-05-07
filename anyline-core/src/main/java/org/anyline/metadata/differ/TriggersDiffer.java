/*
 * Copyright 2006-2023 www.anyline.org
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
 */



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
