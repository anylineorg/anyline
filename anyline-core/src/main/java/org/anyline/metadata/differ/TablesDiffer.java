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

import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

/**
 * 表或列之间的对比结果
 */
public class TablesDiffer implements MetadataDiffer {
    private LinkedHashMap<String, Table> adds = new LinkedHashMap<>();
    private LinkedHashMap<String, Table> drops = new LinkedHashMap<>();
    private LinkedHashMap<String, Table> updates = new LinkedHashMap<>();
    private LinkedHashMap<String, TableDiffer> differs =new LinkedHashMap<>();

    public boolean isEmpty(){
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests){
        return compare(origins, dests, true);
    }

    /**
     * 比较差异
     * @param origins origins
     * @param dests dests
     * @param ignoreSchema 是否忽略 catalog schema
     * @return TablesDiffer
     */
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests, boolean ignoreSchema){
        TablesDiffer differ = new TablesDiffer();
        LinkedHashMap<String, Table> adds = new LinkedHashMap<>();
        LinkedHashMap<String, Table> drops = new LinkedHashMap<>();
        LinkedHashMap<String, Table> updates = new LinkedHashMap<>();
        LinkedHashMap<String, TableDiffer> differs =new LinkedHashMap<>();
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
                drops.put(key, origin);
            }else {
                //更新部分
                TableDiffer dif = origin.compare(dest);
                if(!dif.isEmpty()){
                    origin.setUpdate(dest, false, false);
                    updates.put(key, origin);
                    differs.put(key, dif);
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
        differ.setDiffers(differs);
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

    public LinkedHashMap<String, TableDiffer> getDiffers() {
        return differs;
    }

    public void setDiffers(LinkedHashMap<String, TableDiffer> differs) {
        this.differs = differs;
    }
}
