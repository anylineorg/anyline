package org.anyline.entity.authorize;

import java.util.ArrayList;
import java.util.List;

public class PrivilegeGroup extends Privilege {
    private List<Privilege> privileges = new ArrayList<>();
    public List<Privilege> privileges() {
        return privileges;
    }
    public PrivilegeGroup add(Privilege privilege) {
        privileges.add(privilege);
        return this;
    }
    public PrivilegeGroup remove(Privilege privilege) {
        privileges.remove(privilege);
        return this;
    }
}
