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

package org.anyline.data.prepare;

import java.util.List;

public interface VariableBlock {
    String box();

    VariableBlock box(String box);
    String body();

    VariableBlock body(String body);

    List<Variable> variables();

    VariableBlock variables(List<Variable> variables);
    VariableBlock add(Variable ... variables);

    boolean active();
}
