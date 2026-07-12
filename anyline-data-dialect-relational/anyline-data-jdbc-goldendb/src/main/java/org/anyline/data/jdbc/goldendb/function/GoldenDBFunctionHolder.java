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


package org.anyline.data.jdbc.goldendb.function;

import org.anyline.data.adapter.function.SystemFunctionFactory;
import org.anyline.metadata.type.DatabaseType;

/**
 * GoldenDB 函数自动注册
 */
public class GoldenDBFunctionHolder {
    private static final String illegals = "";
    public GoldenDBFunctionHolder() {
        for (GoldenDBFunction func : GoldenDBFunction.values()) {
            SystemFunctionFactory.reg(DatabaseType.GoldenDB, func);
        }
        SystemFunctionFactory.illegal(DatabaseType.GoldenDB, illegals);
    }
}