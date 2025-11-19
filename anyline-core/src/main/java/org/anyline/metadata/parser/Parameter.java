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

public class Parameter {

    private String content;
    private int startIndex;
    private int endIndex;
    private boolean isFunction;
    private FunctionCall call;

    public Parameter(String content, int startIndex, int endIndex, boolean isFunction) {
        this.content = content;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.isFunction = isFunction;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public void setCall(boolean call) {
        isFunction = call;
    }

    // Getter和Setter方法
    public String getContent() { return content; }
    public int getStartIndex() { return startIndex; }
    public int getEndIndex() { return endIndex; }
    public boolean getCall() { return isFunction; }
    public FunctionCall getFunction() { return call; }
    public void setFunction(FunctionCall function) {
        this.call = function;
    }

    @Override
    public String toString() {
        return String.format("参数[%d-%d]: %s %s",
                startIndex, endIndex, content, isFunction ? "(函数)" : "(其他)");
    }
}