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

import org.anyline.metadata.Trigger;

import java.io.Serializable;
import java.util.LinkedHashMap;

/**
 * 表或列之间的对比结果
 */
public class TriggersDiffer extends AbstractDiffer implements Serializable {
    private static final long serialVersionUID = 1L;
    private LinkedHashMap<String, Trigger> adds = new LinkedHashMap<>();
    private LinkedHashMap<String, Trigger> drops = new LinkedHashMap<>();
    private LinkedHashMap<String, Trigger> alters = new LinkedHashMap<>();

    public boolean isEmpty() {
        return adds.isEmpty() && drops.isEmpty() && alters.isEmpty();
    }
    public static TriggersDiffer compare(LinkedHashMap<String, Trigger> origins, LinkedHashMap<String, Trigger> dests, DIRECT direct) {
        TriggersDiffer differ = new TriggersDiffer();
        LinkedHashMap<String, Trigger> adds = new LinkedHashMap<>();
        LinkedHashMap<String, Trigger> drops = new LinkedHashMap<>();
        LinkedHashMap<String, Trigger> updates = new LinkedHashMap<>();

        if(null != origins) {
            origins = new LinkedHashMap<>();
        }
        if(null == dests) {
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()) {
            Trigger origin = origins.get(key);
            Trigger dest = dests.get(key);
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
        differ.setAlters(updates);
        return differ;
    }

    public LinkedHashMap<String, Trigger> getAdds() {
        return adds;
    }

    public void setAdds(LinkedHashMap<String, Trigger> adds) {
        this.adds = adds;
    }

    public LinkedHashMap<String, Trigger> getDrops() {
        return drops;
    }

    public void setDrops(LinkedHashMap<String, Trigger> drops) {
        this.drops = drops;
    }

    public LinkedHashMap<String, Trigger> getAlters() {
        return alters;
    }

    public void setAlters(LinkedHashMap<String, Trigger> alters) {
        this.alters = alters;
    }
}
