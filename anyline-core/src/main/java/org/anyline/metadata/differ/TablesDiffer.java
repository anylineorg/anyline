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
public class TablesDiffer extends AbstractDiffer {
    private LinkedHashMap<String, Table> adds = new LinkedHashMap<>();              // 添加的表
    private LinkedHashMap<String, Table> drops = new LinkedHashMap<>();             // 删除的表
    private LinkedHashMap<String, Table> alters = new LinkedHashMap<>();            // 修改表(只记录修改的表名)
    private LinkedHashMap<String, TableDiffer> differs =new LinkedHashMap<>();      // 具体修改内容(记录详细的修改明细，生成DDL需要根据这里)

    public boolean isEmpty() {
        return adds.isEmpty() && drops.isEmpty() && alters.isEmpty();
    }
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests, DIRECT direct) {
        return compare(origins, dests, direct, true);
    }
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests, boolean ignoreSchema) {
        return compare(origins, dests, DIRECT.ORIGIN, ignoreSchema);
    }
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests) {
        return compare(origins, dests, DIRECT.ORIGIN, true);
    }

    /**
     * 比较差异
     * @param origins origins
     * @param dests dests
     * @param ignoreSchema 是否忽略 catalog schema
     * @return TablesDiffer
     */
    public static TablesDiffer compare(LinkedHashMap<String, Table> origins, LinkedHashMap<String, Table> dests, DIRECT direct, boolean ignoreSchema) {
        TablesDiffer differ = new TablesDiffer();
        LinkedHashMap<String, Table> adds = new LinkedHashMap<>();
        LinkedHashMap<String, Table> drops = new LinkedHashMap<>();
        LinkedHashMap<String, Table> alters = new LinkedHashMap<>();
        LinkedHashMap<String, TableDiffer> differs =new LinkedHashMap<>();
        if(null == origins) {
            origins = new LinkedHashMap<>();
        }
        if(null == dests) {
            dests = new LinkedHashMap<>();
        }
        for(String key:origins.keySet()) {
            Table origin = origins.get(key);
            Table dest = dests.get(key);
            if(null == dest) {
                //新表不存在
                drops.put(key, origin);
            }else {
                //更新部分
                TableDiffer dif = origin.compare(dest, direct);
                if(!dif.isEmpty()) {
                    origin.setUpdate(dest, false, false);
                    alters.put(key, origin);
                    differs.put(key, dif);
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
        differ.setDiffers(differs);
        return differ;
    }

    @Override
    public MetadataDiffer setDirect(DIRECT direct) {
        if(null != differs){
            for(TableDiffer differ:differs.values()){
                differ.setDirect(direct);
            }
        }
        return this;
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

    public LinkedHashMap<String, Table> getAlters() {
        return alters;
    }

    public void setAlters(LinkedHashMap<String, Table> alters) {
        this.alters = alters;
    }

    public LinkedHashMap<String, TableDiffer> getDiffers() {
        return differs;
    }

    public void setDiffers(LinkedHashMap<String, TableDiffer> differs) {
        this.differs = differs;
    }
}
