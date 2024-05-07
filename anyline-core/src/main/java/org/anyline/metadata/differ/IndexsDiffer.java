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
