package org.anyline.entity.authorize;

import java.util.ArrayList;
import java.util.List;

public class UserGroup extends User {
    private List<User> users = new ArrayList<>();
    public List<User> users() {
        return users;
    }
    public UserGroup add(User user) {
        users.add(user);
        return this;
    }
    public UserGroup remove(User user) {
        users.remove(user);
        return this;
    }
}
