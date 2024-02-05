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


package org.anyline.data.jdbc.hana;


import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum HanaTypeMetadataAlias implements TypeMetadataAlias {
    BFILE                   (StandardTypeMetadata.ILLEGAL               ),
    BINARY_DOUBLE           (StandardTypeMetadata.ILLEGAL               ),
    BINARY_FLOAT            (StandardTypeMetadata.ILLEGAL               ),
    BIGINT                  (StandardTypeMetadata.BIGINT                ),
    BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
    BINARY                  (StandardTypeMetadata.BINARY                ),
    BIT                     (StandardTypeMetadata.BOOLEAN               ),
    BLOB                    (StandardTypeMetadata.BLOB                  ),
    BOOL                    (StandardTypeMetadata.BOOLEAN               ),
    BOOLEAN                 (StandardTypeMetadata.BOOLEAN               ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTEA                   (StandardTypeMetadata.ILLEGAL               ),
    CHAR                    (StandardTypeMetadata.CHAR                  ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.NCLOB                 ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.TIMESTAMP             ),
    DATETIME2               (StandardTypeMetadata.TIMESTAMP             ),
    DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP             ),
    DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
    DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.DOUBLE                ),
    FLOAT4                  (StandardTypeMetadata.DOUBLE                ),
    FLOAT8                  (StandardTypeMetadata.DOUBLE                ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.ST_GEOMETRY           ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.BLOB                  ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
    INT                     (StandardTypeMetadata.INTEGER               ),
    INT2                    (StandardTypeMetadata.INTEGER               ),
    INT4                    (StandardTypeMetadata.INTEGER               ),
    INT8                    (StandardTypeMetadata.INTEGER               ),
    INTEGER                 (StandardTypeMetadata.INTEGER               ),
    JSON                    (StandardTypeMetadata.NCLOB                 ),
    JSONB                   (StandardTypeMetadata.BLOB                  ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.LONG_TEXT                  ),
    LONGBLOB                (StandardTypeMetadata.BLOB                  ),
    LONGTEXT                (StandardTypeMetadata.NCLOB                 ),
    LSEG                    (StandardTypeMetadata.ST_GEOMETRY           ),
    MACADDR                 (StandardTypeMetadata.ST_GEOMETRY           ),
    MONEY                   (StandardTypeMetadata.DECIMAL               ),
    NUMBER                  (StandardTypeMetadata.DECIMAL               ),
    NCHAR                   (StandardTypeMetadata.NVARCHAR              ),
    NCLOB                   (StandardTypeMetadata.NCLOB                 ),
    NTEXT                   (StandardTypeMetadata.NCLOB                 ),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR              ),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR              ),
    PATH                    (StandardTypeMetadata.ST_GEOMETRY           ),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB                  ),
    MEDIUMINT               (StandardTypeMetadata.INTEGER               ),
    MEDIUMTEXT              (StandardTypeMetadata.NCLOB                 ),
    MULTILINESTRING         (StandardTypeMetadata.ST_GEOMETRY           ),
    MULTIPOINT              (StandardTypeMetadata.ST_GEOMETRY           ),
    MULTIPOLYGON            (StandardTypeMetadata.ST_GEOMETRY           ),
    NUMERIC                 (StandardTypeMetadata.DECIMAL               ),
    POINT                   (StandardTypeMetadata.ST_POINT              ),
    POLYGON                 (StandardTypeMetadata.ST_GEOMETRY           ),
    REAL                    (StandardTypeMetadata.REAL                  ),
    RAW                     (StandardTypeMetadata.ILLEGAL               ),
    ROWID                   (StandardTypeMetadata.ILLEGAL               ),
    SECONDDATE              (StandardTypeMetadata.SECONDDATE            ),
    SERIAL                  (StandardTypeMetadata.INTEGER               ),
    SERIAL2                 (StandardTypeMetadata.INTEGER               ),
    SERIAL4                 (StandardTypeMetadata.INTEGER               ),
    SERIAL8                 (StandardTypeMetadata.BIGINT                ),
    SET                     (StandardTypeMetadata.ILLEGAL               ),
    SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP             ),
    SMALLDECIMAL            (StandardTypeMetadata.SMALLDECIMAL          ),
    SMALLMONEY              (StandardTypeMetadata.DECIMAL               ),
    SMALLINT                (StandardTypeMetadata.SMALLINT              ),
    SMALLSERIAL             (StandardTypeMetadata.INTEGER               ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
    ST_GEOMETRY             (StandardTypeMetadata.ST_GEOMETRY           ),
    ST_POINT                (StandardTypeMetadata.ST_POINT              ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.NCLOB                 ),
    TIME                    (StandardTypeMetadata.TIME                  ),
    TIMEZ                   (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_TIME_ZONE          (StandardTypeMetadata.TIMESTAMP             ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.ILLEGAL               ),
    UROWID                  (StandardTypeMetadata.VARCHAR               ),
    VARBIT                  (StandardTypeMetadata.BLOB                  ),
    TINYBLOB                (StandardTypeMetadata.BLOB                  ),
    TINYINT                 (StandardTypeMetadata.TINYINT               ),
    TINYTEXT                (StandardTypeMetadata.NCLOB                 ),
    VARBINARY               (StandardTypeMetadata.VARBINARY             ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
    XML                     (StandardTypeMetadata.NVARCHAR              ),
    YEAR                    (StandardTypeMetadata.INTEGER               );

    private final TypeMetadata standard;
    private int ignoreLength = -1;
    private int ignorePrecision = -1;
    private int ignoreScale = -1;
    private String lengthRefer;
    private String precisionRefer;
    private String scaleRefer;
    private TypeMetadata.Config config;

    HanaTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    HanaTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.lengthRefer = lengthRefer;
        this.precisionRefer = precisionRefer;
        this.scaleRefer = scaleRefer;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    HanaTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
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
