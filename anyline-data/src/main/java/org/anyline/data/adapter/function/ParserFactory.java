/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.adapter.function;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.type.DatabaseOrigin;
import org.anyline.metadata.type.DatabaseType;

import java.util.HashMap;
import java.util.Map;

public class ParserFactory {
    private static final Log log = LogProxy.get(ParserFactory.class);

    protected static Map<Object, Parser> parsers = new HashMap<>();

    public static void reg(Object type, Parser parser) {
        parsers.put(type, parser);
    }

    public static Parser parser(DatabaseType type) {
        Parser parser = parsers.get(type);
        if (null == parser) {
            DatabaseOrigin origin = type.origin();
            if (null != origin) {
                parser = parsers.get(origin);
            }
        }
        return parser;
    }

    public static Map<Object, Parser> parsers() {
        return parsers;
    }

    public static boolean hasParser(DatabaseType type) {
        return parser(type) != null;
    }
}