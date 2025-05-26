/*
 * Copyright 2006-2025 www.anyline.org
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

import org.anyline.metadata.Procedure;

import java.util.LinkedHashMap;

/**
 * 表或列之间的对比结果
 */
public class ProceduresDiffer extends AbstractDiffer {
    private LinkedHashMap<String, Procedure> adds = new LinkedHashMap<>();
    private LinkedHashMap<String, Procedure> drops = new LinkedHashMap<>();
    private LinkedHashMap<String, Procedure> updates = new LinkedHashMap<>();

    public boolean isEmpty() {
        return adds.isEmpty() && drops.isEmpty() && updates.isEmpty();
    }
    public static ProceduresDiffer compare(LinkedHashMap<String, Procedure> origins, LinkedHashMap<String, Procedure> dests) {
        ProceduresDiffer differ = new ProceduresDiffer();
        LinkedHashMap<String, Procedure> adds = new LinkedHashMap<>();
        LinkedHashMap<String, Procedure> drops = new LinkedHashMap<>();
        LinkedHashMap<String, Procedure> updates = new LinkedHashMap<>();

        if(null != origins) {
            origins = new LinkedHashMap<>();
        }
        if(null == dests) {
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()) {
            Procedure origin = origins.get(key);
            Procedure dest = dests.get(key);
            if(null == dest) {
                //新表不存在
                drops.put(key, origins.get(origin));
            }else {
                if(!origin.equals(dest)) {
                    origin.setUpdate(dest, false, false);
                    updates.put(key, origin);
                }
            }
        }
        for(String key:dests.keySet()) {
            if(!origins.containsKey(key)) {
                adds.put(key, dests.get(key));
            }
        }
        differ.setAdds(adds);
        differ.setDrops(drops);
        differ.setUpdates(updates);
        return differ;
    }

    public LinkedHashMap<String, Procedure> getAdds() {
        return adds;
    }

    public void setAdds(LinkedHashMap<String, Procedure> adds) {
        this.adds = adds;
    }

    public LinkedHashMap<String, Procedure> getDrops() {
        return drops;
    }

    public void setDrops(LinkedHashMap<String, Procedure> drops) {
        this.drops = drops;
    }

    public LinkedHashMap<String, Procedure> getUpdates() {
        return updates;
    }

    public void setUpdates(LinkedHashMap<String, Procedure> updates) {
        this.updates = updates;
    }
}
