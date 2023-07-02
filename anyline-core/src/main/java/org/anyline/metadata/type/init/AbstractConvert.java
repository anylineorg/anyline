package org.anyline.metadata.type.init;

import org.anyline.metadata.type.Convert;

public abstract class AbstractConvert implements Convert {

    public AbstractConvert(Class origin, Class target){
        this.origin = origin;
        this.target = target;
    }
    private final Class origin;
    private final Class target;

    public Class getOrigin() {
        return origin;
    }

    public Class getTarget() {
        return target;
    }
}
