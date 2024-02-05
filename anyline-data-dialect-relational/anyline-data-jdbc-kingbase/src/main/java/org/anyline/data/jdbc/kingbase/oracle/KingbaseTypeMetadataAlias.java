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


package org.anyline.data.jdbc.kingbase.oracle;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum KingbaseTypeMetadataAlias implements TypeMetadataAlias {
    ACLITEM                 (StandardTypeMetadata.ACLITEM               ),
    BINARY_INTEGER          (StandardTypeMetadata.BINARY_INTEGER               ),
    BFILE                   (StandardTypeMetadata.BFILE                 ),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY_DOUBLE         ),
    BINARY_FLOAT            (StandardTypeMetadata.FLOAT_ORACLE          ),
    BIGINT                  (StandardTypeMetadata.NUMBER                ),
    BIGSERIAL               (StandardTypeMetadata.NUMBER                ),
    BINARY                  (StandardTypeMetadata.BLOB                  ),
    BIT                     (StandardTypeMetadata.BIT                   , 1, 2, 1),
    BIT_VARYING             (StandardTypeMetadata.BIT_VARYING                  ),
    BLOB                    (StandardTypeMetadata.BLOB                  ),
    BOOL                    (StandardTypeMetadata.NUMBER                ),
    BOX                     (StandardTypeMetadata.ILLEGAL               ),
    BPCHAR                     (StandardTypeMetadata.BPCHAR               ),
    BPCHARBYTE                     (StandardTypeMetadata.BPCHARBYTE               ),
    BYTEA                   (StandardTypeMetadata.BYTEA                  ),
    CHAR                    (StandardTypeMetadata.CHAR                  ),
    CHARACTER                    (StandardTypeMetadata.CHARACTER                  ),
    CID                    (StandardTypeMetadata.CID               ),
    CIDR                    (StandardTypeMetadata.ILLEGAL               ),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL               ),
    CLOB                    (StandardTypeMetadata.CLOB                  ),
    DATE                    (StandardTypeMetadata.DATE                  ),
    DATETIME                (StandardTypeMetadata.DATETIME             ),
    DATERANGE                (StandardTypeMetadata.DATERANGE             ),
    DATETIME2               (StandardTypeMetadata.DATETIME             ),
    DATETIMEOFFSET          (StandardTypeMetadata.DATETIME             ),
    DECIMAL                 (StandardTypeMetadata.NUMBER                ),
    DOUBLE                  (StandardTypeMetadata.NUMBER                ),
    DOUBLE_PRECISION                  (StandardTypeMetadata.DOUBLE_PRECISION                ),
    DSINTERVAL                  (StandardTypeMetadata.DSINTERVAL                ),
    ENUM                    (StandardTypeMetadata.ILLEGAL               ),
    FLOAT                   (StandardTypeMetadata.FLOAT_ORACLE          ),
    FLOAT4                  (StandardTypeMetadata.FLOAT_ORACLE          ),
    FLOAT8                  (StandardTypeMetadata.FLOAT_ORACLE          ),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL               ),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL               ),
    GTSVECTOR      (StandardTypeMetadata.GTSVECTOR               ),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL               ),
    IMAGE                   (StandardTypeMetadata.BLOB                  ),
    INET                    (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL                (StandardTypeMetadata.ILLEGAL               ),
    INTERVAL_DAY                (StandardTypeMetadata.INTERVAL_DAY               ),
    INTERVAL_DAY_HOUR                (StandardTypeMetadata.INTERVAL_DAY_HOUR               ),
    INTERVAL_DAY_MINUTE                (StandardTypeMetadata.INTERVAL_DAY_MINUTE               ),
    INTERVAL_DAY_SECOND                (StandardTypeMetadata.INTERVAL_DAY_SECOND               ),
    INTERVAL_HOUR                (StandardTypeMetadata.INTERVAL_HOUR               ),
    INTERVAL_HOUR_MINUTE                (StandardTypeMetadata.INTERVAL_HOUR_MINUTE               ),
    INTERVAL_HOUR_SECOND                (StandardTypeMetadata.INTERVAL_HOUR_SECOND               ),
    INTERVAL_MINUTE                (StandardTypeMetadata.INTERVAL_MINUTE               ),
    INTERVAL_MINUTE_SECOND                (StandardTypeMetadata.INTERVAL_MINUTE_SECOND               ),
    INTERVAL_MONTH                (StandardTypeMetadata.INTERVAL_MONTH               ),
    INTERVAL_SECOND                (StandardTypeMetadata.INTERVAL_SECOND               ),
    INTERVAL_YEAR                (StandardTypeMetadata.INTERVAL_YEAR               ),
    INTERVAL_YEAR_MONTH                (StandardTypeMetadata.INTERVAL_YEAR_MONTH               ),
    INT                     (StandardTypeMetadata.NUMBER                ),
    INT2                    (StandardTypeMetadata.NUMBER                ),
    INT4                    (StandardTypeMetadata.NUMBER                ),
    INT4RANGE                    (StandardTypeMetadata.INT4RANGE                ),
    INT8RANGE                    (StandardTypeMetadata.INT8RANGE                ),
    INT8                    (StandardTypeMetadata.NUMBER                ),
    INTEGER                 (StandardTypeMetadata.NUMBER                ),
    JSON                    (StandardTypeMetadata.CLOB                  ),
    JSONPATH                    (StandardTypeMetadata.JSONPATH                  ),
    JSONB                   (StandardTypeMetadata.BLOB                  ),
    LINE                    (StandardTypeMetadata.ILLEGAL               ),
    LONG                    (StandardTypeMetadata.LONG_TEXT                  ),
    LONGBLOB                (StandardTypeMetadata.BLOB                  ),
    LONGTEXT                (StandardTypeMetadata.CLOB                  ),
    LSEG                    (StandardTypeMetadata.ILLEGAL               ),
    MACADDR                 (StandardTypeMetadata.MACADDR8               ),
    MACADDR8                 (StandardTypeMetadata.MACADDR8               ),
    MONEY                   (StandardTypeMetadata.NUMBER                ),
    NUMBER                  (StandardTypeMetadata.NUMBER                ),
    NCHAR                   (StandardTypeMetadata.NCHAR                 ),
    NCLOB                   (StandardTypeMetadata.NCLOB                 ),
    NTEXT                   (StandardTypeMetadata.NCLOB                 ),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR2             ),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR2             ),
    PATH                    (StandardTypeMetadata.ILLEGAL               ),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB                  ),
    MYSQL_DATE              (StandardTypeMetadata.KINGBASE_MYSQL_DATE                  ),
    MYSQL_TIME              (StandardTypeMetadata.KINGBASE_MYSQL_TIME                  ),
    MEDIUMINT               (StandardTypeMetadata.NUMBER                ),
    MEDIUMTEXT              (StandardTypeMetadata.CLOB                  ),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL               ),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL               ),
    NATURALN                 (StandardTypeMetadata.NATURALN                ),
    NUMRANGE                 (StandardTypeMetadata.NUMRANGE                ),
    NUMERIC                 (StandardTypeMetadata.NUMBER                ),
    POINT                   (StandardTypeMetadata.ILLEGAL               ),
    OID                   (StandardTypeMetadata.OID               ),
    ORA_DATE                   (StandardTypeMetadata.ORA_DATE               ),
    POLYGON                 (StandardTypeMetadata.ILLEGAL               ),
    POSITIVE                 (StandardTypeMetadata.POSITIVE               ),
    POSITIVEN                 (StandardTypeMetadata.POSITIVEN               ),
    REAL                    (StandardTypeMetadata.REAL          ),
    RAW                     (StandardTypeMetadata.RAW                   ),
    REFCURSOR                     (StandardTypeMetadata.REFCURSOR                   ),
    REGCLASS                     (StandardTypeMetadata.REGCLASS                   ),
    REGCONFIG                     (StandardTypeMetadata.REGCONFIG                   ),
    REGDICTIONARY                     (StandardTypeMetadata.REGDICTIONARY                   ),
    REGNAMESPACE                     (StandardTypeMetadata.REGNAMESPACE                   ),
    REGOPER                     (StandardTypeMetadata.REGOPER                   ),
    REGOPERATOR                     (StandardTypeMetadata.REGOPERATOR                   ),
    REGPROC                     (StandardTypeMetadata.REGPROC                   ),
    REGPROCEDURE                     (StandardTypeMetadata.REGPROCEDURE                   ),
    REGTYPE                     (StandardTypeMetadata.REGTYPE                   ),
    REGROLE                     (StandardTypeMetadata.REGROLE                   ),
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
    SIGNTYPE                     (StandardTypeMetadata.SIGNTYPE                   ),
    SIMPLE_DOUBLE                     (StandardTypeMetadata.SIMPLE_DOUBLE                   ),
    SIMPLE_INTEGER                     (StandardTypeMetadata.SIMPLE_INTEGER                   ),
    SIMPLE_FLOAT                     (StandardTypeMetadata.SIMPLE_FLOAT                   ),
    STRING                     (StandardTypeMetadata.STRING                   ),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL               ),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL               ),
    TEXT                    (StandardTypeMetadata.CLOB                  ),
    TID                    (StandardTypeMetadata.TID             ),
    TIME                    (StandardTypeMetadata.TIMESTAMP             ),
    TIMEZ                   (StandardTypeMetadata.TIMESTAMP             ),
    TIME_WITHOUT_TIME_ZONE      (StandardTypeMetadata.TIME_WITHOUT_TIME_ZONE),
    TIME_WITH_TIME_ZONE      (StandardTypeMetadata.TIME_WITH_TIME_ZONE),
    TIME_TZ_UNCONSTRAINED      (StandardTypeMetadata.TIME_TZ_UNCONSTRAINED),
    TIME_UNCONSTRAINED      (StandardTypeMetadata.TIME_UNCONSTRAINED),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITHOUT_TIME_ZONE    (StandardTypeMetadata.TIMESTAMP_WITHOUT_TIME_ZONE             ),
    TIMESTAMP_WITH_TIME_ZONE    (StandardTypeMetadata.TIMESTAMP_WITH_TIME_ZONE             ),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP             ),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP             ),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL               ),
    TSRANGE                (StandardTypeMetadata.TSRANGE               ),
    TSTZRANGE                (StandardTypeMetadata.TSTZRANGE               ),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL               ),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL               ),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL               ),
    UUID                    (StandardTypeMetadata.ILLEGAL               ),
    UROWID                  (StandardTypeMetadata.UROWID                ),
    VARBIT                  (StandardTypeMetadata.BLOB                  ),
    VARCHARBYTE                  (StandardTypeMetadata.VARCHARBYTE                  ),
    TINYBLOB                (StandardTypeMetadata.BLOB                  ),
    TINYINT                 (StandardTypeMetadata.NUMBER                ),
    TINYTEXT                (StandardTypeMetadata.CLOB                  ),
    VARBINARY               (StandardTypeMetadata.BLOB                  ),
    VARCHAR                 (StandardTypeMetadata.VARCHAR               ),
    VARCHAR2                (StandardTypeMetadata.VARCHAR2               ),
    XML                     (StandardTypeMetadata.ILLEGAL               ),
    XID                     (StandardTypeMetadata.XID               ),
    YMINTERVAL              (StandardTypeMetadata.YMINTERVAL               ),
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
