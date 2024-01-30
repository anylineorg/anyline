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


package org.anyline.data.jdbc.mssql;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum MSSQLTypeMetadataAlias implements TypeMetadataAlias {
    BFILE                   (TypeMetadata.ILLEGAL                       ), 
    BINARY_DOUBLE           (StandardTypeMetadata.NUMERIC               ),
    BINARY_FLOAT            (StandardTypeMetadata.FLOAT_MSSQL              ),
    BIGINT                  (StandardTypeMetadata.BIGINT                ),
    BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
    BINARY                  (StandardTypeMetadata.BINARY                ),
    BIT                     (StandardTypeMetadata.BIT                   ),
    BLOB                    (StandardTypeMetadata.VARBINARY             ),
    BOOL                    (StandardTypeMetadata.BIT                   ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTEA                   (StandardTypeMetadata.VARBINARY             ),
    CHAR                    (StandardTypeMetadata.CHAR                  ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.TEXT                  ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.DATETIME              ),
    DATETIME2               (StandardTypeMetadata.DATETIME2             ),
    DATETIMEOFFSET          (StandardTypeMetadata.DATETIMEOFFSET        ),
    DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
    DOUBLE                  (StandardTypeMetadata.DECIMAL               ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.FLOAT_MSSQL                 ),
    FLOAT4                  (StandardTypeMetadata.FLOAT_MSSQL                 ),
    FLOAT8                  (StandardTypeMetadata.FLOAT_MSSQL                 ),
    GEOGRAPHY               (StandardTypeMetadata.GEOGRAPHY             ),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.HIERARCHYID           ),
    IMAGE                   (StandardTypeMetadata.IMAGE                 ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
    INT                     (StandardTypeMetadata.INT                   ),
    INT2                    (StandardTypeMetadata.INT                   ),
    INT4                    (StandardTypeMetadata.INT                   ),
    INT8                    (StandardTypeMetadata.BIGINT                ),
    INTEGER                 (StandardTypeMetadata.INT                   ),
    JSON                    (StandardTypeMetadata.ILLEGAL               ),
    JSONB                   (StandardTypeMetadata.ILLEGAL               ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.BIGINT                ),
    LONGBLOB                (StandardTypeMetadata.VARBINARY             ),
    LONGTEXT                (StandardTypeMetadata.TEXT                  ),
    LSEG                    (StandardTypeMetadata.ILLEGAL               ),
    MACADDR                 (StandardTypeMetadata.ILLEGAL               ),
    MONEY                   (StandardTypeMetadata.MONEY                 ),
    NUMBER                  (StandardTypeMetadata.NUMERIC               ),
    NCHAR                   (StandardTypeMetadata.NCHAR                 ),
    NCLOB                   (StandardTypeMetadata.VARBINARY             ),
    NTEXT                   (StandardTypeMetadata.NTEXT                 ),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR              ),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR              ),
    PATH                    (StandardTypeMetadata.ILLEGAL               ),
    MEDIUMBLOB              (StandardTypeMetadata.VARBINARY             ),
    MEDIUMINT               (StandardTypeMetadata.INT                   ),
    MEDIUMTEXT              (StandardTypeMetadata.TEXT                  ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL               ),
    NUMERIC                 (StandardTypeMetadata.NUMERIC               ),
    POINT                   (StandardTypeMetadata.ILLEGAL               ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
    REAL                    (StandardTypeMetadata.REAL                  ),
    RAW                     (StandardTypeMetadata.ILLEGAL               ),
    ROWID                   (StandardTypeMetadata.ILLEGAL               ),
    SERIAL                  (StandardTypeMetadata.INT                   ),
    SERIAL2                 (StandardTypeMetadata.TINYINT               ),
    SERIAL4                 (StandardTypeMetadata.INT                   ),
    SERIAL8                 (StandardTypeMetadata.BIGINT                ),
    SET                     (StandardTypeMetadata.ILLEGAL               ),
    SMALLDATETIME           (StandardTypeMetadata.SMALLDATETIME         ),
    SMALLMONEY              (StandardTypeMetadata.SMALLMONEY            ),
    SMALLINT                (StandardTypeMetadata.INT                   ),
    SMALLSERIAL             (StandardTypeMetadata.INT                   ),
    SQL_VARIANT             (StandardTypeMetadata.SQL_VARIANT           ),
    SYSNAME                 (StandardTypeMetadata.SYSNAME               ),
    TEXT                    (StandardTypeMetadata.TEXT                  ),
    TIME                    (StandardTypeMetadata.TIME                  ),
    TIMEZ                   (StandardTypeMetadata.TIME                  ),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP             ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.UNIQUEIDENTIFIER      ),
    UUID                    (StandardTypeMetadata.ILLEGAL               ),
    UROWID                  (StandardTypeMetadata.ILLEGAL               ),
    VARBIT                  (StandardTypeMetadata.VARBINARY             ),
    TINYBLOB                (StandardTypeMetadata.VARBINARY             ),
    TINYINT                 (StandardTypeMetadata.TINYINT               ),
    TINYTEXT                (StandardTypeMetadata.TEXT                  ),
    VARBINARY               (StandardTypeMetadata.VARBINARY             ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR               ),
    XML                     (StandardTypeMetadata.XML                   ),
    YEAR                    (StandardTypeMetadata.DATE                  );


    private final TypeMetadata standard;
    private int ignoreLength = -1;
    private int ignorePrecision = -1;
    private int ignoreScale = -1;
    private String lengthRefer;
    private String precisionRefer;
    private String scaleRefer;
    private TypeMetadata.Config config;

    MSSQLTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    MSSQLTypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.lengthRefer = lengthRefer;
        this.precisionRefer = precisionRefer;
        this.scaleRefer = scaleRefer;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    MSSQLTypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
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
