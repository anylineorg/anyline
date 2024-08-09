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

public interface MetadataDiffer {
    /**
     * ALTER DDL 使用于哪个表 默认作用于dest
     * @param direct 表
     * @return MetadataDiffer
     */
    MetadataDiffer setDirect(DIRECT direct);
    MetadataDiffer setDirect(Table direct);
    Table getDirect();
    enum DIRECT{
        //a.compare(b)
        ORIGIN,  //源表 a
        DEST     //目标表 b
    };
}
