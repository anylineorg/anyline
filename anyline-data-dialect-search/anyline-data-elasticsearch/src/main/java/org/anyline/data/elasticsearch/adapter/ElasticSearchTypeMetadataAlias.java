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


package org.anyline.data.elasticsearch.adapter;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum ElasticSearchTypeMetadataAlias implements TypeMetadataAlias {

    BFILE                   (StandardTypeMetadata.BINARY                 ),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY         ),
    BINARY_FLOAT            (StandardTypeMetadata.FLOAT          ),
    BIGINT                  (StandardTypeMetadata.LONG_TEXT                ),
    BIGSERIAL               (StandardTypeMetadata.LONG_TEXT                ),
    BINARY                  (StandardTypeMetadata.BINARY                  ),
    BIT                     (StandardTypeMetadata.BYTE                ),
    BLOB                    (StandardTypeMetadata.BINARY                  ),
    BOOL                    (StandardTypeMetadata.BOOLEAN                ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTEA                   (StandardTypeMetadata.BINARY                  ),
    CHAR                    (StandardTypeMetadata.TEXT                  ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.TEXT                  ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.DATE             ),
    DATETIME2               (StandardTypeMetadata.DATE             ),
    DATETIMEOFFSET          (StandardTypeMetadata.DATE             ),
    DECIMAL                 (StandardTypeMetadata.DOUBLE                ),
    DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.FLOAT          ),
    FLOAT4                  (StandardTypeMetadata.FLOAT          ),
    FLOAT8                  (StandardTypeMetadata.FLOAT          ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.BINARY                  ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
    INT                     (StandardTypeMetadata.INTEGER                   ),
    INT2                    (StandardTypeMetadata.INTEGER                   ),
    INT4                    (StandardTypeMetadata.INTEGER                   ),
    INT8                    (StandardTypeMetadata.LONG_TEXT                ),
    INTEGER                 (StandardTypeMetadata.INTEGER                   ),
    JSON                    (StandardTypeMetadata.OBJECT                  ),
    JSONB                   (StandardTypeMetadata.BINARY                  ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.LONG_TEXT                  ),
    LONGBLOB                (StandardTypeMetadata.BINARY                  ),
    LONGTEXT                (StandardTypeMetadata.TEXT                  ),
    LSEG                    (StandardTypeMetadata.ILLEGAL               ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
    MONEY                   (StandardTypeMetadata.DOUBLE                ),
    NUMBER                  (StandardTypeMetadata.DOUBLE                ),
    NCHAR                   (StandardTypeMetadata.TEXT                 ),
    NCLOB                   (StandardTypeMetadata.TEXT                 ),
    NTEXT                   (StandardTypeMetadata.TEXT                 ),
    NVARCHAR                (StandardTypeMetadata.TEXT             ),
    NVARCHAR2               (StandardTypeMetadata.TEXT             ),
    PATH                    (StandardTypeMetadata.ILLEGAL               ),
    MEDIUMBLOB              (StandardTypeMetadata.BINARY                  ),
    MEDIUMINT               (StandardTypeMetadata.DOUBLE                ),
    MEDIUMTEXT              (StandardTypeMetadata.TEXT                  ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL               ),
    NUMERIC                 (StandardTypeMetadata.DOUBLE                ),
    POINT                   (StandardTypeMetadata.ILLEGAL               ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
    REAL                    (StandardTypeMetadata.FLOAT          ),
    RAW                     (StandardTypeMetadata.ILLEGAL                   ),
    ROWID                   (StandardTypeMetadata.ILLEGAL                 ),
    SERIAL                  (StandardTypeMetadata.INTEGER),
    SERIAL2                 (StandardTypeMetadata.INTEGER                   ),
    SERIAL4                 (StandardTypeMetadata.INTEGER                   ),
    SERIAL8                 (StandardTypeMetadata.LONG_TEXT                ),
    SET                     (StandardTypeMetadata.DATE               ),
    SMALLDATETIME           (StandardTypeMetadata.DATE             ),
    SMALLMONEY              (StandardTypeMetadata.DOUBLE                ),
    SMALLINT                (StandardTypeMetadata.DOUBLE                ),
    SMALLSERIAL             (StandardTypeMetadata.DOUBLE                ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.TEXT                  ),
    TIME                    (StandardTypeMetadata.DATE             ),
    TIMEZ                   (StandardTypeMetadata.DATE             ),
    TIMESTAMP               (StandardTypeMetadata.DATE             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.DATE             ),
    TIMESTAMP_WITH_TIME_ZONE          (StandardTypeMetadata.DATE             ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.ILLEGAL               ),
    UROWID                  (StandardTypeMetadata.ILLEGAL                ),
    VARBIT                  (StandardTypeMetadata.BINARY                  ),
    TINYBLOB                (StandardTypeMetadata.BINARY                  ),
    TINYINT                 (StandardTypeMetadata.DOUBLE                ),
    TINYTEXT                (StandardTypeMetadata.TEXT                  ),
    VARBINARY               (StandardTypeMetadata.BINARY                  ),
    VARCHAR                 (StandardTypeMetadata.TEXT               ),
    VARCHAR2                (StandardTypeMetadata.TEXT               ),
    XML                     (StandardTypeMetadata.ILLEGAL               ),
    YEAR                    (StandardTypeMetadata.DATE                  ),
    OBJECT                  (StandardTypeMetadata.OBJECT                  ),
    KEYWORD                 (StandardTypeMetadata.KEYWORD                  );

    private final TypeMetadata standard;
    private int ignoreLength = -1;
    private int ignorePrecision = -1;
    private int ignoreScale = -1;
    private String lengthRefer;
    private String precisionRefer;
    private String scaleRefer;
    private TypeMetadata.Config config;

    ElasticSearchTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    ElasticSearchTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.lengthRefer = lengthRefer;
        this.precisionRefer = precisionRefer;
        this.scaleRefer = scaleRefer;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    ElasticSearchTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

    @Override
    public TypeMetadata.Config config() {
        if(null == config){
            config = new TypeMetadata.Config();
            config.setLengthRefer(lengthRefer).setPrecisionRefer(precisionRefer).setScaleRefer(scaleRefer);
            config.setIgnoreLength(ignoreLength).setIgnorePrecision(ignorePrecision).setIgnoreScale(ignoreScale);
        }
        return config;
    }
}
