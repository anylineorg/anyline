package org.anyline.entity.operator;


public class Auto extends BasicCompare{
    @Override
    public boolean compare(Object value) {
        return false;
    }

    @Override
    public boolean compare(Object value, Object target) {
        return false;
    }
}
