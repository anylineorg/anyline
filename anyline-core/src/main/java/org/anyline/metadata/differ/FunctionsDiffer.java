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

import org.anyline.metadata.Function;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 表或列之间的对比结果
 */
public class FunctionsDiffer extends AbstractDiffer {
    private List<Function> adds = new ArrayList<>();
    private List<Function> drops = new ArrayList<>();
    private List<Function> alters = new ArrayList<>();

    public boolean isEmpty() {
        return adds.isEmpty() && drops.isEmpty() && alters.isEmpty();
    }
    public static FunctionsDiffer compare(LinkedHashMap<String, Function> origins, LinkedHashMap<String, Function> dests, DIRECT direct) {
        FunctionsDiffer differ = new FunctionsDiffer();
        List<Function> adds = new ArrayList<>();
        List<Function> drops = new ArrayList<>();
        List<Function> updates = new ArrayList<>();

        if(null != origins) {
            origins = new LinkedHashMap<>();
        }
        if(null == dests) {
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()) {
            Function origin = origins.get(key);
            Function dest = dests.get(key);
            if(null == dest) {
                //新表不存在
                drops.add(origins.get(origin));
            }else {
                if(!origin.equals(dest)) {
                    origin.setUpdate(dest, false, false);
                    updates.add(origin);
                }
            }
        }
        for(String key:dests.keySet()) {
            if(!origins.containsKey(key)) {
                adds.add(dests.get(key));
            }
        }
        differ.setAdds(adds);
        differ.setDrops(drops);
        differ.setAlters(updates);
        return differ;
    }

    public List<Function> getAdds() {
        return adds;
    }

    public void setAdds(List<Function> adds) {
        this.adds = adds;
    }

    public List<Function> getDrops() {
        return drops;
    }

    public void setDrops(List<Function> drops) {
        this.drops = drops;
    }

    public List<Function> getAlters() {
        return alters;
    }

    public void setAlters(List<Function> alters) {
        this.alters = alters;
    }
}
