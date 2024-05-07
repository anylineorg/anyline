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

import org.anyline.metadata.View;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 表或列之间的对比结果
 */
public class ViewsDiffer implements MetadataDiffer {
    private List<View> adds = new ArrayList<>();
    private List<View> drops = new ArrayList<>();
    private List<View> updates = new ArrayList<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static ViewsDiffer compare(LinkedHashMap<String, View> origins, LinkedHashMap<String, View> dests){
        ViewsDiffer differ = new ViewsDiffer();
        List<View> adds = new ArrayList<>();
        List<View> drops = new ArrayList<>();
        List<View> updates = new ArrayList<>();

        if(null != origins){
            origins = new LinkedHashMap<>();
        }
        if(null == dests){
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()){
            View origin = origins.get(key);
            View dest = dests.get(key);
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

    public List<View> getAdds() {
        return adds;
    }

    public void setAdds(List<View> adds) {
        this.adds = adds;
    }

    public List<View> getDrops() {
        return drops;
    }

    public void setDrops(List<View> drops) {
        this.drops = drops;
    }

    public List<View> getUpdates() {
        return updates;
    }

    public void setUpdates(List<View> updates) {
        this.updates = updates;
    }
}
