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

package org.anyline.data.prepare.init;

import org.anyline.data.prepare.Variable;
import org.anyline.data.prepare.VariableBlock;
import org.anyline.entity.Compare;
import org.anyline.util.BasicUtil;

import java.util.ArrayList;
import java.util.List;

public class DefaultVariableBlock implements VariableBlock {
    protected List<Variable> variables = new ArrayList<>();
    protected String box;
    protected String body;
    public DefaultVariableBlock() {

    }
    public DefaultVariableBlock(String box, String body) {
        this.box = box;
        this.body = body;
    }
    @Override
    public String box() {
        return box;
    }

    @Override
    public VariableBlock box(String box) {
        this.box = box;
        return this;
    }
    @Override
    public String body() {
        return body;
    }

    @Override
    public VariableBlock body(String body) {
        this.body = body;
        return this;
    }

    @Override
    public List<Variable> variables() {
        return variables;
    }

    @Override
    public VariableBlock variables(List<Variable> variables) {
        this.variables = variables;
        return this;
    }

    @Override
    public VariableBlock add(Variable... variables) {
        for(Variable variable:variables) {
            this.variables.add(variable);
        }
        return this;
    }
    public boolean active() {
        for(Variable var:variables) {
            Compare.EMPTY_VALUE_SWITCH swt = var.getSwt();
            List<Object> values = var.getValues();
            if(BasicUtil.isEmpty(values)) {
                return false;
            }
            for(Object value:values) {
                if(BasicUtil.isEmpty(value)) {
                    return false; //任何一个空值
                }
            }
        }
        return true;
    }
    public VariableBlock clone() {
        DefaultVariableBlock clone = new DefaultVariableBlock();
        clone.box = box;
        clone.body = body;
        clone.variables = new ArrayList<>();
        //变量不要clone 必须与condition共享
        return clone;
    }
}
