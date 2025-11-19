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

package org.anyline.metadata.parser;

import org.anyline.metadata.SystemFunction;

import java.util.ArrayList;
import java.util.List;

public class FunctionCall {
    private String name;
    private String text;
    private int start;
    private int end;
    private SystemFunction function;
    private List<FunctionCall> calls;
    private List<Parameter> parameters;

    public FunctionCall(String functionName, String fullText, int startIndex, int endIndex) {
        this.name = functionName;
        this.text = fullText;
        this.start = startIndex;
        this.end = endIndex;
        this.calls = new ArrayList<>();
        this.parameters = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public List<FunctionCall> getCalls() {
        return calls;
    }

    public void setCalls(List<FunctionCall> calls) {
        this.calls = calls;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return String.format("%s[%d-%d]%s",
                name, start, end, text);
    }
}
