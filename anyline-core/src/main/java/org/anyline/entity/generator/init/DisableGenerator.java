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

package org.anyline.entity.generator.init;

import org.anyline.metadata.Column;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.entity.generator.PrimaryGenerator;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * 不生成主键, 可以针对某个表覆盖全局配置
 */
public class DisableGenerator implements PrimaryGenerator {
    @Override
    public boolean create(Object entity, DatabaseType type, String table, List<String> columns, String other) {
        return false;
    }
    @Override
    public boolean create(Object entity, DatabaseType type, String table, LinkedHashMap<String, Column> columns, String other) {
        return false;
    }
}
