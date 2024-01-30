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


package org.anyline.data.jdbc.kingbase.postgres;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum KingbaseTypeMetadataAlias implements TypeMetadataAlias {

    BFILE                   (StandardTypeMetadata.BFILE                 ),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY_DOUBLE         ),
    BINARY_FLOAT            (StandardTypeMetadata.FLOAT4                ),
    BIGINT                  (StandardTypeMetadata.NUMBER                ),
    BIGSERIAL               (StandardTypeMetadata.NUMBER                ),
    BINARY                  (StandardTypeMetadata.BLOB                  ),
    BIT                     (StandardTypeMetadata.NUMBER                ),
    BLOB                    (StandardTypeMetadata.BLOB                  ),
    BOOL                    (StandardTypeMetadata.NUMBER                ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTEA                   (StandardTypeMetadata.BLOB                  ),
    CHAR                    (StandardTypeMetadata.CHAR                  ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.CLOB                  ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.TIMESTAMP             ),
    DATETIME2               (StandardTypeMetadata.TIMESTAMP             ),
    DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP             ),
    DECIMAL                 (StandardTypeMetadata.NUMBER                ),
    DOUBLE                  (StandardTypeMetadata.NUMBER                ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.FLOAT_ORACLE          ),
    FLOAT4                  (StandardTypeMetadata.FLOAT_ORACLE          ),
    FLOAT8                  (StandardTypeMetadata.FLOAT_ORACLE          ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.BLOB                  ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
    INT                     (StandardTypeMetadata.NUMBER                ),
    INT2                    (StandardTypeMetadata.NUMBER                ),
    INT4                    (StandardTypeMetadata.NUMBER                ),
    INT8                    (StandardTypeMetadata.NUMBER                ),
    INTEGER                 (StandardTypeMetadata.NUMBER                ),
    JSON                    (StandardTypeMetadata.CLOB                  ),
    JSONB                   (StandardTypeMetadata.BLOB                  ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.LONG_TEXT                  ),
    LONGBLOB                (StandardTypeMetadata.BLOB                  ),
    LONGTEXT                (StandardTypeMetadata.CLOB                  ),
    LSEG                    (StandardTypeMetadata.ILLEGAL               ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
    MONEY                   (StandardTypeMetadata.NUMBER                ),
    NUMBER                  (StandardTypeMetadata.NUMBER                ),
    NCHAR                   (StandardTypeMetadata.NCHAR                 ),
    NCLOB                   (StandardTypeMetadata.NCLOB                 ),
    NTEXT                   (StandardTypeMetadata.NCLOB                 ),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR2             ),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR2             ),
    PATH                    (StandardTypeMetadata.ILLEGAL               ),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB                  ),
    MEDIUMINT               (StandardTypeMetadata.NUMBER                ),
    MEDIUMTEXT              (StandardTypeMetadata.CLOB                  ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL               ),
    NUMERIC                 (StandardTypeMetadata.NUMBER                ),
    POINT                   (StandardTypeMetadata.ILLEGAL               ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
    REAL                    (StandardTypeMetadata.FLOAT_ORACLE          ),
    RAW                     (StandardTypeMetadata.RAW                   ),
    ROWID                   (StandardTypeMetadata.ROWID                 ),
    SERIAL                  (StandardTypeMetadata.NUMBER                ),
    SERIAL2                 (StandardTypeMetadata.NUMBER                ),
    SERIAL4                 (StandardTypeMetadata.NUMBER                ),
    SERIAL8                 (StandardTypeMetadata.NUMBER                ),
    SET                     (StandardTypeMetadata.ILLEGAL               ),
    SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP             ),
    SMALLMONEY              (StandardTypeMetadata.NUMBER                ),
    SMALLINT                (StandardTypeMetadata.NUMBER                ),
    SMALLSERIAL             (StandardTypeMetadata.NUMBER                ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.CLOB                  ),
    TIME                    (StandardTypeMetadata.TIMESTAMP             ),
    TIMEZ                   (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP             ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.ILLEGAL               ),
    UROWID                  (StandardTypeMetadata.UROWID                ),
    VARBIT                  (StandardTypeMetadata.BLOB                  ),
    TINYBLOB                (StandardTypeMetadata.BLOB                  ),
    TINYINT                 (StandardTypeMetadata.NUMBER                ),
    TINYTEXT                (StandardTypeMetadata.CLOB                  ),
    VARBINARY               (StandardTypeMetadata.BLOB                  ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
    XML                     (StandardTypeMetadata.ILLEGAL               ),
    YEAR                    (StandardTypeMetadata.DATE                  );

    private final TypeMetadata standard;
    private int ignoreLength = -1;
    private int ignorePrecision = -1;
    private int ignoreScale = -1;
    private String lengthRefer;
    private String precisionRefer;
    private String scaleRefer;
    private TypeMetadata.Config config;

    KingbaseTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    KingbaseTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.lengthRefer = lengthRefer;
        this.precisionRefer = precisionRefer;
        this.scaleRefer = scaleRefer;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    KingbaseTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
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
