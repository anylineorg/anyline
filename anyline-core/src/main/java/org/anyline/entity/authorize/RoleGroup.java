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
