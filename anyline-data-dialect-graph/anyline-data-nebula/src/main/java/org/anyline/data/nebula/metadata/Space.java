package org.anyline.data.nebula.metadata;

import org.anyline.metadata.Catalog;

public class Space extends Catalog {
    private String vidType;

    public String getVidType() {
        return vidType;
    }

    public void setVidType(String vidType) {
        this.vidType = vidType;
    }
}
