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

package org.anyline.data.adapter;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.type.DatabaseType;

import java.util.ArrayList;
import java.util.List;

public class SystemFunctionConverterProxy {
    private static final Log log = LogProxy.get(SystemFunctionConverterProxy.class);
    private static List<SystemFunctionConverter> parsers = new ArrayList();

    public static void reg(SystemFunctionConverter parser) {
        parsers.add(parser);
    }
    public static String convert(DatabaseType origin, DatabaseType target, String cmd) {
        for (SystemFunctionConverter parser : parsers) {
            cmd = parser.convert(origin, target, cmd);
        }
        return cmd;
    }
    public static void convert(DatabaseType origin, DatabaseType target, Metadata metadata) {
        for (SystemFunctionConverter parser : parsers) {
             parser.convert(origin, target, metadata);
        }
    }
}
