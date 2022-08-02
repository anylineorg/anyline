package org.anyline.entity.operator;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicCompare implements Compare{
    protected Object value;
    protected List<Object> values = new ArrayList<>();


    @Override
    public Compare setValue(Object value) {
        this.value = value;
        return this;
    }
    @Override
    public Compare addValue(Object value) {
        values.add(value);
        return this;
    }

    @Override
    public Compare setValues(List<Object> values) {
        this.values = values;
        return this;
    }

}
