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

import org.anyline.metadata.Catalog;
import org.anyline.metadata.Metadata;
import org.anyline.metadata.Schema;

public class User extends Metadata<User> {
    protected Role role;
    private String password;
    private String host;
    public User() {

    }
    public User(String name) {
        this.name = name;
    }
    public User(Catalog catalog, Schema schema, String name) {
        this.catalog = catalog;
        this.schema = schema;
        this.name = name;
    }
    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /* ********************************* field refer ********************************** */
    public static final String FIELD_HOST = "HOST";
    public static final String FIELD_ROLE = "ROLE";

    public static final String FIELD_PASSWORD = "PASSWORD";

}
