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


package org.anyline.data.run;

import org.anyline.data.runtime.DataRuntime;

import java.util.ArrayList;

public class SimpleRun extends TextRun implements Run {
    public SimpleRun(DataRuntime runtime){
        this.runtime = runtime;
    }
    public SimpleRun(StringBuilder builder){
        this.builder = builder;
    }
    public SimpleRun(String sql){
        this.builder.append(sql);
    }
    public String getFinalQuery() {
        return builder.toString();
    }

    public String getFinalUpdate() {
        return builder.toString();
    }

    public SimpleRun addValue(Object value) {
        RunValue runValue = new RunValue();
        runValue.setValue(value);
        if(null == values){
            values = new ArrayList<>();
        }
        values.add(runValue);
        return this;
    }
}
