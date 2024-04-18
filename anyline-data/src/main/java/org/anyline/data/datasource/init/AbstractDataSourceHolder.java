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
 * 
 *           
 */ 
 
 
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

package org.anyline.data.datasource.init;

import org.anyline.data.datasource.DataSourceHolder;
import org.anyline.data.datasource.DataSourceKeyMap;
import org.anyline.util.BeanUtil;
import org.anyline.util.ConfigTable;

import java.util.Map;

public abstract class AbstractDataSourceHolder implements DataSourceHolder {
    public <T> T value(String prefix, String key, Class<T> clazz, T def){
        return ConfigTable.environment().value(prefix, key, DataSourceKeyMap.maps, clazz, def);
    }
    public Object value(String prefix, String key){
        return ConfigTable.environment().value(prefix, key, DataSourceKeyMap.maps, Object.class, null);
    }

    public <T> T value(Map map, String keys, Class<T> clazz, T def){
        return BeanUtil.value(map, keys, DataSourceKeyMap.maps, clazz, def);
    }
    public static Object value(Map map, String keys){
        return BeanUtil.value(map, keys, DataSourceKeyMap.maps, Object.class, null);
    }

}
