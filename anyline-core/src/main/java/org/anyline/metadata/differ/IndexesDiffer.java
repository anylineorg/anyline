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

import org.anyline.metadata.Index;
import org.anyline.metadata.Table;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class IndexesDiffer extends AbstractDiffer implements Serializable {
    private static final long serialVersionUID = 1L;
    private LinkedHashMap<String, Index> adds = new LinkedHashMap<>();
    private LinkedHashMap<String, Index> drops = new LinkedHashMap<>();
    private LinkedHashMap<String, Index> alters = new LinkedHashMap<>();

    public static IndexesDiffer compare(LinkedHashMap<String, Index> origins, LinkedHashMap<String, Index> dests, Table direct) {
        IndexesDiffer differ = new IndexesDiffer();
        LinkedHashMap<String, Index> adds = new LinkedHashMap<>();
        LinkedHashMap<String, Index> drops = new LinkedHashMap<>();
        LinkedHashMap<String, Index> updates = new LinkedHashMap<>();

        if(null == origins) {
            origins = new LinkedHashMap<>();
        }
        if(null == dests) {
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()) {
            Index origin = origins.get(key);
            if(origin.isPrimary()) {
                continue;
            }
            Index dest = dests.get(key);
            if(null != dest && dest.isPrimary()) {
                continue;
            }
            if(null == dest) {
                drops.put(key, origin);
            }else {
                if(!origin.equals(dest)) {
                    if(!origin.isPrimary()) {
                        origin.setUpdate(dest, false, false);
                        updates.put(key, origin);
                    }
                }
            }
        }
        for(String key:dests.keySet()) {
            if(!origins.containsKey(key)) {
                Index index = dests.get(key);
                if(index.isPrimary()) {
                    continue;
                }
                adds.put(key, index);
            }
        }
        differ.setDirect(direct);
        differ.setAdds(adds);
        differ.setDrops(drops);
        differ.setAlters(updates);
        return differ;
    }

    public boolean isEmpty() {
        return adds.isEmpty() && drops.isEmpty() && alters.isEmpty();
    }

    public LinkedHashMap<String, Index> getAdds() {
        return adds;
    }

    public void setAdds(LinkedHashMap<String, Index> adds) {
        this.adds = adds;
    }

    public LinkedHashMap<String, Index> getDrops() {
        return drops;
    }

    public void setDrops(LinkedHashMap<String, Index> drops) {
        this.drops = drops;
    }

    public LinkedHashMap<String, Index> getAlters() {
        return alters;
    }

    public void setAlters(LinkedHashMap<String, Index> alters) {
        this.alters = alters;
    }
}
