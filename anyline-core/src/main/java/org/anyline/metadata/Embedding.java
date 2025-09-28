package org.anyline.metadata;

import java.io.Serializable;

public class Embedding implements Serializable {
    private static final long serialVersionUID = 1L;
    private String model;
    private String provider;

    public Embedding() {
    }
    public Embedding(String model, String provider) {
        this.model = model;
        this.provider = provider;
    }
    public Embedding(String model) {
        this.model = model;
    }
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }
}