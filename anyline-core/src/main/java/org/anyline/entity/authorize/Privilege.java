/*
 * Copyright 2006-2023 www.anyline.org
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

public class Privilege extends Metadata<User> {
    private String tables;
    private String objectType;
    private String objectName;

    public Privilege(){}
    public Privilege(User user){
        this.user = user;
    }
    public String getTables() {
        return tables;
    }

    public void setTables(String tables) {
        this.tables = tables;
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

    /* ********************************* field refer ********************************** */
    public static final String FIELD_TABLE = "TABLE";
    public static final String FIELD_OBJECT_TYPE = "OBJECT_TYPE";
    public static final String FIELD_OBJECT_NAME = "OBJECT_NAME";
}
