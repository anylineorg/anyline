package org.anyline.runtime;

import org.anyline.runtime.init.*;

import java.util.LinkedHashMap;

public class ActuatorHolder {
    private static LinkedHashMap<String, ExpressionActuator> actuators = new LinkedHashMap<>();
    static {
        reg(new NumberActuator());
        reg(new RandomActuator());
        reg(new TimestampActuator());
        reg(new UUIDActuator());
        reg(new DateTimeActuator());
    }
    public static void reg(String tag, ExpressionActuator parser) {
        actuators.put(tag, parser);
    }
    public static void reg(ExpressionActuator parser) {
        actuators.put(parser.tag(), parser);
    }
    public static ExpressionActuator get(String tag) {
        return actuators.get(tag);
    }
}
