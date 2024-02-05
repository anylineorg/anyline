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


package org.anyline.data.jdbc.tdengine;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum TDengineTypeMetadataAlias implements TypeMetadataAlias {

    BFILE                   (StandardTypeMetadata.ILLEGAL             ),
    BINARY_DOUBLE           (StandardTypeMetadata.ILLEGAL             ),
    BINARY_FLOAT            (StandardTypeMetadata.ILLEGAL             ),
    BIGINT                  (StandardTypeMetadata.BIGINT              ),
    BIGSERIAL               (StandardTypeMetadata.BIGINT              ),
    BINARY                  (StandardTypeMetadata.BINARY              ),
    BIT                     (StandardTypeMetadata.BOOL                ),
    BLOB                    (StandardTypeMetadata.ILLEGAL             ),
    BOOL                    (StandardTypeMetadata.BOOL                ),
    BOX                     (StandardTypeMetadata.ILLEGAL             ),
    BYTEA                   (StandardTypeMetadata.ILLEGAL             ),
    CHAR                    (StandardTypeMetadata.NCHAR               ),
    CIDR                    (StandardTypeMetadata.ILLEGAL             ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL             ),
    CLOB                    (StandardTypeMetadata.ILLEGAL             ),
    DATE                    (StandardTypeMetadata.TIMESTAMP           ),
    DATETIME                (StandardTypeMetadata.TIMESTAMP           ),
    DATETIME2               (StandardTypeMetadata.TIMESTAMP           ),
    DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP           ),
    DECIMAL                 (StandardTypeMetadata.DOUBLE              ),
    DOUBLE                  (StandardTypeMetadata.DOUBLE              ),
    ENUM                    (StandardTypeMetadata.ILLEGAL             ),
    FLOAT                   (StandardTypeMetadata.FLOAT         ),
    FLOAT4                  (StandardTypeMetadata.FLOAT         ),
    FLOAT8                  (StandardTypeMetadata.DOUBLE              ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL             ), //
    GEOMETRY                (StandardTypeMetadata.ILLEGAL             ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL             ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL             ),
    IMAGE                   (StandardTypeMetadata.ILLEGAL             ),
    INET                    (StandardTypeMetadata.ILLEGAL             ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL             ),
    INT                     (StandardTypeMetadata.INT                 ),
    INT2                    (StandardTypeMetadata.INT                 ),
    INT4                    (StandardTypeMetadata.INT                 ),
    INT8                    (StandardTypeMetadata.INT                 ),
    INTEGER                 (StandardTypeMetadata.INT                 ),
    JSON                    (StandardTypeMetadata.JSON                ),
    JSONB                   (StandardTypeMetadata.BLOB                ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.INT                 ),
    LONGBLOB                (StandardTypeMetadata.ILLEGAL             ),
    LONGTEXT                (StandardTypeMetadata.NCHAR               ),
    LSEG                    (StandardTypeMetadata.ILLEGAL             ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL             ),
    MONEY                   (StandardTypeMetadata.DOUBLE              ),
    NUMBER                  (StandardTypeMetadata.DOUBLE              ),
    NCHAR                   (StandardTypeMetadata.NCHAR               ),
    NCLOB                   (StandardTypeMetadata.NCHAR               ),
    NTEXT                   (StandardTypeMetadata.NCHAR               ),
    NVARCHAR                (StandardTypeMetadata.NCHAR               ),
    NVARCHAR2               (StandardTypeMetadata.NCHAR               ),
    PATH                    (StandardTypeMetadata.ILLEGAL             ),
    MEDIUMBLOB              (StandardTypeMetadata.ILLEGAL             ),
    MEDIUMINT               (StandardTypeMetadata.ILLEGAL             ),
    MEDIUMTEXT              (StandardTypeMetadata.NCHAR               ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL             ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL             ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL             ),
    NUMERIC                 (StandardTypeMetadata.DOUBLE              ),
    POINT                   (StandardTypeMetadata.ILLEGAL               ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
    REAL                    (StandardTypeMetadata.DOUBLE              ),
    RAW                     (StandardTypeMetadata.ILLEGAL             ),
    ROWID                   (StandardTypeMetadata.ILLEGAL             ),
    SERIAL                  (StandardTypeMetadata.INT                 ),
    SERIAL2                 (StandardTypeMetadata.INT                 ),
    SERIAL4                 (StandardTypeMetadata.INT                 ),
    SERIAL8                 (StandardTypeMetadata.INT                 ),
    SET                     (StandardTypeMetadata.ILLEGAL             ),
    SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP           ),
    SMALLMONEY              (StandardTypeMetadata.DECIMAL             ),
    SMALLINT                (StandardTypeMetadata.INT                 ),
    SMALLSERIAL             (StandardTypeMetadata.INT                 ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL             ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL             ),
    TEXT                    (StandardTypeMetadata.NCHAR               ),
    TIME                    (StandardTypeMetadata.TIMESTAMP           ),
    TIMEZ                   (StandardTypeMetadata.TIMESTAMP           ),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP           ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP           ),
    TIMESTAMP_WITH_TIME_ZONE          (StandardTypeMetadata.TIMESTAMP           ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL             ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL             ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL             ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL             ),
    UUID                    (StandardTypeMetadata.ILLEGAL             ),
    UROWID                  (StandardTypeMetadata.ILLEGAL             ),
    VARBIT                  (StandardTypeMetadata.ILLEGAL             ),
    TINYBLOB                (StandardTypeMetadata.ILLEGAL             ),
    TINYINT                 (StandardTypeMetadata.INT                 ),
    TINYTEXT                (StandardTypeMetadata.NCHAR               ),
    VARBINARY               (StandardTypeMetadata.BLOB                ),
    VARCHAR                 (StandardTypeMetadata.NCHAR               ),
    VARCHAR2                (StandardTypeMetadata.NCHAR               ),
    XML                     (StandardTypeMetadata.NCHAR               ),
    YEAR                    (StandardTypeMetadata.INT                 );


    private final TypeMetadata standard;
    private int ignoreLength = -1;
    private int ignorePrecision = -1;
    private int ignoreScale = -1;
    private String lengthRefer;
    private String precisionRefer;
    private String scaleRefer;
    private TypeMetadata.Config config;

    TDengineTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    TDengineTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.lengthRefer = lengthRefer;
        this.precisionRefer = precisionRefer;
        this.scaleRefer = scaleRefer;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    TDengineTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
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
