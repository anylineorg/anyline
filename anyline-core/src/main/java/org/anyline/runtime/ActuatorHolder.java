/*
 * Copyright 2006-2025 www.anyline.org
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
