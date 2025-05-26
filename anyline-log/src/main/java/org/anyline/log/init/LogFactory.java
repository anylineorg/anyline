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

package org.anyline.log.init;

import org.anyline.annotation.AnylineComponent;
import org.anyline.log.Log;
import org.anyline.util.ConfigTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@AnylineComponent
public class LogFactory implements org.anyline.log.LogFactory{
    @Override
    public Log get(Class<?> clazz) {
        Logger logger = LoggerFactory.getLogger(clazz);
        return new DefaultLog(logger);
    }

    @Override
    public Log get(String name) {
        Logger logger = LoggerFactory.getLogger(name);
        return new DefaultLog(logger);
    }

    @Override
    public boolean disabled() {
        return ConfigTable.IS_DISABLED_DEFAULT_LOG;
    }
}
