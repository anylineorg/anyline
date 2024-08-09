package org.anyline.data.hbase.adapter;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.TypeMetadata;

public enum HBaseTypeMetadataAlias implements TypeMetadataAlias {
    ;

    @Override
    public TypeMetadata standard() {
        return null;
    }

    @Override
    public TypeMetadata.Refer refer() {
        return null;
    }
}
