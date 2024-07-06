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

import org.anyline.metadata.PrimaryKey;

public class PrimaryKeyDiffer implements MetadataDiffer {
    private PrimaryKey add;
    private PrimaryKey drop;
    private PrimaryKey alter;

    public static PrimaryKeyDiffer compare(PrimaryKey origin, PrimaryKey dest) {
        PrimaryKeyDiffer differ = new PrimaryKeyDiffer();
        if(null == origin && null == dest){
            return differ;
        }

        if(null == origin){
            differ.add = dest;
        }else{
            if(null == dest){
                origin.drop();
                differ.drop = origin;
            }else{
                if(!origin.equals(dest)) {
                    origin.setUpdate(dest, false, false);
                    differ.alter = origin;
                }
            }
        }
        return differ;
    }

    public PrimaryKey getAdd() {
        return add;
    }

    public void setAdd(PrimaryKey add) {
        this.add = add;
    }

    public PrimaryKey getDrop() {
        return drop;
    }

    public void setDrop(PrimaryKey drop) {
        this.drop = drop;
    }

    public PrimaryKey getAlter() {
        return alter;
    }

    public void setAlter(PrimaryKey alter) {
        this.alter = alter;
    }
}
