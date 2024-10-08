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

package org.anyline.entity;

public class Having {
    private Aggregation aggregation;
    private String column;
    private Compare compare;
    private Object value;
    private String text;
    public Having(){}
    public Having(String text){
        this.text = text;
    }
    public String text() {
        return text;
    }
    public Having text(String text) {
        this.text = text;
        return this;
    }
    public Aggregation aggregation() {
        return aggregation;
    }

    public Having aggregation(Aggregation aggregation) {
        this.aggregation = aggregation;
        return this;
    }

    public String column() {
        return column;
    }

    public Having column(String column) {
        this.column = column;
        return this;
    }

    public Compare compare() {
        return compare;
    }

    public Having compare(Compare compare) {
        this.compare = compare;
        return this;
    }

    public Object value() {
        return value;
    }

    public Having value(Object value) {
        this.value = value;
        return this;
    }
}
