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

package org.anyline.metadata.type.init;

import org.anyline.metadata.type.Convert;

public abstract class AbstractConvert implements Convert {

    public AbstractConvert(Class origin, Class target) {
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
