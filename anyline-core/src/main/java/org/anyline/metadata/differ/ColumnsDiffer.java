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

import org.anyline.metadata.Column;
import org.anyline.metadata.Table;

import java.util.LinkedHashMap;

public class ColumnsDiffer extends AbstractDiffer{
    private LinkedHashMap<String, Column> adds = new LinkedHashMap<>();
    private LinkedHashMap<String, Column> drops = new LinkedHashMap<>();
    private LinkedHashMap<String, Column> alters = new LinkedHashMap<>();

    public static ColumnsDiffer compare(LinkedHashMap<String, Column> origins, LinkedHashMap<String, Column> dests, Table direct) {
        ColumnsDiffer differ = new ColumnsDiffer();
        LinkedHashMap<String, Column> adds = new LinkedHashMap<>();
        LinkedHashMap<String, Column> drops = new LinkedHashMap<>();
        LinkedHashMap<String, Column> alters = new LinkedHashMap<>();

        if(null == origins) {
            origins = new LinkedHashMap<>();
        }
        if(null == dests) {
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()) {
            Column origin = origins.get(key);
            Column dest = dests.get(key);
            if(null == dest) {
                //新表不存在这一列
                drops.put(key, origin);
            }else {
                //不比较 catalog schema
                if(!origin.equals(dest)) {
                    origin.setUpdate(dest, false, false);
                    alters.put(key, origin);
                }
            }
        }
        for(String key:dests.keySet()) {
            if(!origins.containsKey(key)) {
                adds.put(key, dests.get(key));
            }
        }
        differ.setDirect(direct);
        differ.setAdds(adds);
        differ.setDrops(drops);
        differ.setAlters(alters);
        return differ;
    }

    public boolean isEmpty() {
        return adds.isEmpty() && drops.isEmpty() && alters.isEmpty();
    }

    public LinkedHashMap<String, Column> getAdds() {
        return adds;
    }

    public void setAdds(LinkedHashMap<String, Column> adds) {
        this.adds = adds;
    }

    public LinkedHashMap<String, Column> getDrops() {
        return drops;
    }

    public void setDrops(LinkedHashMap<String, Column> drops) {
        this.drops = drops;
    }

    public LinkedHashMap<String, Column> getAlters() {
        return alters;
    }

    public void setAlters(LinkedHashMap<String, Column> alters) {
        this.alters = alters;
    }
}
