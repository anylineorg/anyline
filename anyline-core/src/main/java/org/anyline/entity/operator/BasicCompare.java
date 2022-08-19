package org.anyline.entity.operator;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicCompare implements Compare{
    protected Object target;
    protected List<Object> targets = new ArrayList<>();


    @Override
    public Compare setTarget(Object target) {
        this.target = target;
        return this;
    }
    @Override
    public Compare setString(String target) {
        this.target = target;
        return this;
    }
    @Override
    public Compare addTarget(Object value) {
        targets.add(value);
        return this;
    }

    @Override
    public Compare setTargets(List<Object> targets) {
        this.targets = targets;
        return this;
    }
    @Override
    public Object getTarget(){
        return target;
    }
    @Override
    public String getString(){
        return target.toString();
    }
}
