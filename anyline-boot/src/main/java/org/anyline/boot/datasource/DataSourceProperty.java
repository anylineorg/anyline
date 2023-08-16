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


package org.anyline.boot.datasource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.mongo.MongoProperties;

public class DataSourceProperty extends DataSourceProperties {

    /**
     * [MongoDB]数据库名
     */
    private String database;

    /**
     * [MongoDB]Authentication database name
     */
    private String authenticationDatabase;

    private final MongoProperties.Gridfs gridfs = new MongoProperties.Gridfs();




    /**
     * [MongoDB]Required replica set name for the cluster. Cannot be set with URI
     */
    private String replicaSetName;

    /**
     * [MongoDB]Fully qualified name of the FieldNamingStrategy to use
     */
    private Class<?> fieldNamingStrategy;


    /**
     * [MongoDB]Whether to enable auto-index creation
     */
    private Boolean autoIndexCreation;

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public String getAuthenticationDatabase() {
        return authenticationDatabase;
    }

    public void setAuthenticationDatabase(String authenticationDatabase) {
        this.authenticationDatabase = authenticationDatabase;
    }

    public MongoProperties.Gridfs getGridfs() {
        return gridfs;
    }

    public String getReplicaSetName() {
        return replicaSetName;
    }

    public void setReplicaSetName(String replicaSetName) {
        this.replicaSetName = replicaSetName;
    }

    public Class<?> getFieldNamingStrategy() {
        return fieldNamingStrategy;
    }

    public void setFieldNamingStrategy(Class<?> fieldNamingStrategy) {
        this.fieldNamingStrategy = fieldNamingStrategy;
    }

    public Boolean getAutoIndexCreation() {
        return autoIndexCreation;
    }

    public void setAutoIndexCreation(Boolean autoIndexCreation) {
        this.autoIndexCreation = autoIndexCreation;
    }
}
