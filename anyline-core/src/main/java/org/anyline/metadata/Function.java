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



package org.anyline.metadata;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Function extends Metadata<Function> implements Serializable {
    protected String keyword = "FUNCTION"           ;
    protected List<Parameter> parameters = new ArrayList<>();
    public Function(){}
    public Function(Catalog catalog, Schema schema, String name){
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }
    public List<Parameter> getParameters() {
        if(getmap && null != update) {
            return update.parameters;
        }
        return parameters;
    }

    public Function setParameters(List<Parameter> parameters) {
        if(setmap && null != update) {
            update.parameters = parameters;
            return this;
        }
        this.parameters = parameters;
        return this;
    }

    public String getKeyword() {
        return this.keyword;
    }

    public Function clone() {
        Function copy = super.clone();
        List<Parameter> pms = new ArrayList<>();
        for(Parameter parameter:parameters) {
            pms.add(parameter.clone());
        }
        copy.parameters = pms;

        return copy;
    }

/* ********************************* field refer ********************************** */
    public static final String FIELD_KEYWORD                       = "KEYWORD";
    public static final String FIELD_PARAMETER                     = "PARAMETER";
}