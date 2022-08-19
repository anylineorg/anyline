package org.anyline.entity.operator;

import java.util.List;

public interface Compare {
    public Compare setString(String target);
    public Compare setTarget(Object target);
    public Compare addTarget(Object target);
    public Compare setTargets(List<Object> targets);
    public boolean compare(Object value);
    public boolean compare(Object value, Object target);
    public Object getTarget();
    public Object getString();

}
