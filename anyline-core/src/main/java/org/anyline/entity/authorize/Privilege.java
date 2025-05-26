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

import org.anyline.metadata.Metadata;

import java.util.ArrayList;
import java.util.List;

public class Privilege extends Metadata<Privilege> {
    protected List<String> actions = new ArrayList<>();
    protected User user; // 所属用户
    protected Role role;
    private String objectType;
    private String objectName; //当前权限相关的对象名称

    public Privilege() {}
    public Privilege(User user) {
        this.user = user;
    }

    public Privilege addActions(String ... actions) {
        for(String action:actions){
            this.actions.add(action);
        }
        return this;
    }
    public List<String> getActions(){
        return actions;
    }
    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public Role getRole() {
        return role;
    }
    public String getRoleName() {
        if(null != role) {
            return role.getName();
        }
        return null;
    }
    public String getUserName() {
        if(null != user) {
            return user.getName();
        }
        return null;
    }


    public Privilege setRole(Role role) {
        this.role = role;
        return this;
    }

    /* ********************************* field refer ********************************** */
    public static final String FIELD_TABLE = "TABLE";
    public static final String FIELD_USER = "USER";
    public static final String FIELD_ROLE = "ROLE";
    public static final String FIELD_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String FIELD_OBJECT_NAME = "OBJECT_NAME";
}
