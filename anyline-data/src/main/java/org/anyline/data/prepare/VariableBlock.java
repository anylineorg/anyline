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
