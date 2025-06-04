package org.anyline.runtime;

import org.anyline.annotation.AnylineComponent;
import org.anyline.listener.LoadListener;
import org.anyline.util.ConfigTable;

import java.util.Map;

@AnylineComponent("anyline.environment.listener.actuator")
public class ActuatorLoadListener implements LoadListener {
    @Override
    public void start() {
        Map<String, ExpressionActuator> actuators = ConfigTable.environment().getBeans(ExpressionActuator.class);
        for(ExpressionActuator item:actuators.values()) {
            ActuatorHolder.reg(item);
        }
    }
}
