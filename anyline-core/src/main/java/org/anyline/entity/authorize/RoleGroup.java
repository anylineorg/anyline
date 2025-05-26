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

package org.anyline.entity.authorize;

import java.util.ArrayList;
import java.util.List;

public class RoleGroup extends Role {
    private List<Role> roles = new ArrayList<>();
    public List<Role> roles() {
        return roles;
    }
    public RoleGroup add(Role role) {
        roles.add(role);
        return this;
    }
    public RoleGroup remove(Role role) {
        roles.remove(role);
        return this;
    }
}
