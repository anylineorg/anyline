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


package org.anyline.data.jdbc.h2;


import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.metadata.type.init.StandardTypeMetadata;
//https://www.h2database.com/html/datatypes.html
public enum H2TypeMetadataAlias implements TypeMetadataAlias {
    ARRAY                   (StandardTypeMetadata.ARRAY               ),
    BFILE                   (StandardTypeMetadata.ILLEGAL               ),
    BINARY_DOUBLE           (StandardTypeMetadata.ILLEGAL               ),
    BINARY_FLOAT            (StandardTypeMetadata.ILLEGAL               ),
    BIGINT                  (StandardTypeMetadata.BIGINT                ),
    BIGSERIAL               (StandardTypeMetadata.BIGINT                ),
    BINARY                  (StandardTypeMetadata.BINARY                , 2, 1, 1),
    BIT                     (StandardTypeMetadata.BOOLEAN               ),
    BLOB                    (StandardTypeMetadata.BLOB                  ),
    BOOL                    (StandardTypeMetadata.BOOLEAN               ),
    BOOLEAN                 (StandardTypeMetadata.BOOLEAN               ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BYTEA                   (StandardTypeMetadata.ILLEGAL               ),
    CHAR                    (StandardTypeMetadata.CHAR                  , 2, 1, 1),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.CLOB                 ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.TIMESTAMP             ),
    DATETIME2               (StandardTypeMetadata.TIMESTAMP             ),
    DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP             ),
    DECFLOAT                (StandardTypeMetadata.DECFLOAT              ),
    DECIMAL                 (StandardTypeMetadata.DECIMAL               ),
    DOUBLE                  (StandardTypeMetadata.DOUBLE                ),
    DOUBLE_PRECISION        (StandardTypeMetadata.DOUBLE_PRECISION      ),
    ENUM                    (StandardTypeMetadata.ENUM                  ),
    FLOAT                   (StandardTypeMetadata.DOUBLE                ),
    FLOAT4                  (StandardTypeMetadata.DOUBLE                ),
    FLOAT8                  (StandardTypeMetadata.DOUBLE                ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.GEOMETRY              ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.BLOB                  ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.INTERVAL               ),
    INT                     (StandardTypeMetadata.INTEGER               ),
    INT2                    (StandardTypeMetadata.INTEGER               ),
    INT4                    (StandardTypeMetadata.INTEGER               ),
    INT8                    (StandardTypeMetadata.INTEGER               ),
    INTEGER                 (StandardTypeMetadata.INTEGER               ),
    JAVA_OBJECT             (StandardTypeMetadata.JAVA_OBJECT           ),
    JSON                    (StandardTypeMetadata.JSON                  ),
    JSONB                   (StandardTypeMetadata.BLOB                  ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.LONG_TEXT                  ),
    LONGBLOB                (StandardTypeMetadata.BLOB                  ),
    LONGTEXT                (StandardTypeMetadata.CLOB                 ),
    LSEG                    (StandardTypeMetadata.GEOMETRY           ),
    MACADDR                 (StandardTypeMetadata.GEOMETRY           ),
    MONEY                   (StandardTypeMetadata.DECIMAL               ),
    NUMBER                  (StandardTypeMetadata.DECIMAL               ),
    NCHAR                   (StandardTypeMetadata.NVARCHAR              ),
    NCLOB                   (StandardTypeMetadata.CLOB                 ),
    NTEXT                   (StandardTypeMetadata.CLOB                 ),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR              ),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR              ),
    PATH                    (StandardTypeMetadata.GEOMETRY           ),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB                  ),
    MEDIUMINT               (StandardTypeMetadata.INTEGER               ),
    MEDIUMTEXT              (StandardTypeMetadata.CLOB                 ),
    MULTILINESTRING         (StandardTypeMetadata.GEOMETRY           ),
    MULTIPOINT              (StandardTypeMetadata.GEOMETRY           ),
    MULTIPOLYGON            (StandardTypeMetadata.GEOMETRY           ),
    NUMERIC                 (StandardTypeMetadata.NUMERIC               ),
    POINT                   (StandardTypeMetadata.ST_POINT              ),
    POLYGON                 (StandardTypeMetadata.GEOMETRY           ),
    REAL                    (StandardTypeMetadata.REAL                  ),
    RAW                     (StandardTypeMetadata.ILLEGAL               ),
    ROWID                   (StandardTypeMetadata.ILLEGAL               ),
    ROW                     (StandardTypeMetadata.ROW                   ),
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
    ST_GEOMETRY             (StandardTypeMetadata.GEOMETRY           ),
    ST_POINT                (StandardTypeMetadata.ST_POINT              ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.CLOB                 ),
    TIME                    (StandardTypeMetadata.TIME                  ),
    TIMEZ                   (StandardTypeMetadata.TIME_WITH_ZONE             ),
    TIME_WITH_ZONE          (StandardTypeMetadata.TIME_WITH_ZONE             ),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP_WITH_ZONE             ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP_WITH_ZONE             ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.UUID               ),
    UROWID                  (StandardTypeMetadata.VARCHAR               ),
    VARBIT                  (StandardTypeMetadata.BLOB                  ),
    TINYBLOB                (StandardTypeMetadata.BLOB                  ),
    TINYINT                 (StandardTypeMetadata.TINYINT               ),
    TINYTEXT                (StandardTypeMetadata.CLOB                 ),
    VARBINARY               (StandardTypeMetadata.VARBINARY             ,0, 1, 1),
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

    H2TypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    H2TypeMetadataAlias(TypeMetadata standard, String lengthRefer, String precisionRefer, String scaleRefer, int ignoreLength, int ignorePrecision, int ignoreScale){
        this.standard = standard;
        this.lengthRefer = lengthRefer;
        this.precisionRefer = precisionRefer;
        this.scaleRefer = scaleRefer;
        this.ignoreLength = ignoreLength;
        this.ignorePrecision = ignorePrecision;
        this.ignoreScale = ignoreScale;
    }

    H2TypeMetadataAlias(TypeMetadata standard, int ignoreLength, int ignorePrecision, int ignoreScale){
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
