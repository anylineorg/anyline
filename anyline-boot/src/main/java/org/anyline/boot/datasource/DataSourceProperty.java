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
