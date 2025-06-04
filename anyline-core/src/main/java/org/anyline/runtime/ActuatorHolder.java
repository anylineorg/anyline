package org.anyline.runtime;

import org.anyline.annotation.AnylineAutowired;
import org.anyline.annotation.AnylineComponent;

import java.util.LinkedHashMap;
import java.util.List;

@AnylineComponent
public class ActuatorHolder {
    private static LinkedHashMap<String, LinkedHashMap<String, ExpressionActuator>> actuators = new LinkedHashMap<>();

    @AnylineAutowired
    public void setActuators(List<ExpressionActuator> actuators) {
        for (ExpressionActuator actuator : actuators) {
            reg(actuator);
        }
    }

    public static void reg(String namespace, String tag, ExpressionActuator parser) {
        LinkedHashMap<String, ExpressionActuator> nms = actuators.computeIfAbsent(namespace, k -> new LinkedHashMap<>());
        nms.put(tag, parser);
    }
    public static void reg(ExpressionActuator actuator) {
        List<String> tags = actuator.tags();
        List<String> namespaces = actuator.namespaces();
        for(String namespace : namespaces) {
            for(String tag : tags){
                reg(namespace, tag, actuator);
            }
        }
    }
    public static ExpressionActuator get(String namespace, String tag) {
        LinkedHashMap<String, ExpressionActuator> nms = actuators.get(namespace);
        if(null != nms) {
            return nms.get(tag);
        }
        return null;
    }
}
