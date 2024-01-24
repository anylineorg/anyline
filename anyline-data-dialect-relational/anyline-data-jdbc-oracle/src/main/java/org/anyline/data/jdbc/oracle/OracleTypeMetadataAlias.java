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


package org.anyline.data.jdbc.oracle;

import org.anyline.data.metadata.TypeMetadataAlias;
import org.anyline.metadata.type.init.StandardTypeMetadata;
import org.anyline.metadata.type.TypeMetadata;

public enum OracleTypeMetadataAlias implements TypeMetadataAlias {

    BFILE                   (StandardTypeMetadata.BFILE),
    BINARY_DOUBLE           (StandardTypeMetadata.BINARY_DOUBLE),
    BINARY_FLOAT            (StandardTypeMetadata.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    BIGINT                  (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BIGSERIAL               (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BINARY                  (StandardTypeMetadata.BLOB,"DATA_LENGTH"),
    BIT                     (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BLOB                    (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    BOOL                    (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BOX                     (StandardTypeMetadata.ILLEGAL),
    BYTEA                   (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    CHAR                    (StandardTypeMetadata.CHAR, "DATA_LENGTH"),
    CIDR                    (StandardTypeMetadata.ILLEGAL),
    CIRCLE                  (StandardTypeMetadata.ILLEGAL),
    CLOB                    (StandardTypeMetadata.CLOB, "DATA_LENGTH"),
    DATE                    (StandardTypeMetadata.DATE, "DATA_LENGTH"),
    DATETIME                (StandardTypeMetadata.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    DATETIME2               (StandardTypeMetadata.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    DATETIMEOFFSET          (StandardTypeMetadata.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    DECIMAL                 (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    DOUBLE                  (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    ENUM                    (StandardTypeMetadata.ILLEGAL),
    FLOAT                   (StandardTypeMetadata.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    FLOAT4                  (StandardTypeMetadata.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    FLOAT8                  (StandardTypeMetadata.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    GEOGRAPHY               (StandardTypeMetadata.ILLEGAL),
    GEOMETRY                (StandardTypeMetadata.ILLEGAL),
    GEOMETRYCOLLECTION      (StandardTypeMetadata.ILLEGAL),
    HIERARCHYID             (StandardTypeMetadata.ILLEGAL),
    IMAGE                   (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    INET                    (StandardTypeMetadata.ILLEGAL),
    INTERVAL                (StandardTypeMetadata.ILLEGAL),
    INT                     (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INT2                    (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INT4                    (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INT8                    (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INTEGER                 (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    JSON                    (StandardTypeMetadata.CLOB, "DATA_LENGTH"),
    JSONB                   (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    LINE                    (StandardTypeMetadata.ILLEGAL),
    LONG                    (StandardTypeMetadata.LONG_TEXT, "DATA_LENGTH"),
    LONGBLOB                (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    LONGTEXT                (StandardTypeMetadata.CLOB, "DATA_LENGTH"),
    LSEG                    (StandardTypeMetadata.ILLEGAL),
    MACADDR                 (StandardTypeMetadata.ILLEGAL),
    MONEY                   (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    NUMBER                  (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    NCHAR                   (StandardTypeMetadata.NCHAR, "DATA_LENGTH"),
    NCLOB                   (StandardTypeMetadata.NCLOB, "DATA_LENGTH"),
    NTEXT                   (StandardTypeMetadata.NCLOB, "DATA_LENGTH"),
    NVARCHAR                (StandardTypeMetadata.NVARCHAR2, "DATA_LENGTH"),
    NVARCHAR2               (StandardTypeMetadata.NVARCHAR2, "DATA_LENGTH"),
    PATH                    (StandardTypeMetadata.ILLEGAL),
    MEDIUMBLOB              (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    MEDIUMINT               (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    MEDIUMTEXT              (StandardTypeMetadata.CLOB, "DATA_LENGTH"),
    MULTILINESTRING         (StandardTypeMetadata.ILLEGAL),
    MULTIPOINT              (StandardTypeMetadata.ILLEGAL),
    MULTIPOLYGON            (StandardTypeMetadata.ILLEGAL),
    NUMERIC                 (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    POINT                   (StandardTypeMetadata.ILLEGAL),
    POLYGON                 (StandardTypeMetadata.ILLEGAL),
    REAL                    (StandardTypeMetadata.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    RAW                     (StandardTypeMetadata.RAW, "DATA_LENGTH"),
    ROWID                   (StandardTypeMetadata.ROWID, "DATA_LENGTH"),
    SERIAL                  (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SERIAL2                 (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SERIAL4                 (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SERIAL8                 (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SET                     (StandardTypeMetadata.ILLEGAL),
    SMALLDATETIME           (StandardTypeMetadata.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    SMALLMONEY              (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SMALLINT                (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SMALLSERIAL             (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SQL_VARIANT             (StandardTypeMetadata.ILLEGAL),
    SYSNAME                 (StandardTypeMetadata.ILLEGAL),
    TEXT                    (StandardTypeMetadata.CLOB, "DATA_LENGTH"),
    TIME                    (StandardTypeMetadata.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMEZ                   (StandardTypeMetadata.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMESTAMP               (StandardTypeMetadata.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardTypeMetadata.TIMESTAMP_WITH_LOCAL_ZONE, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMESTAMP_WITH_ZONE          (StandardTypeMetadata.TIMESTAMP_WITH_ZONE, "DATA_LENGTH", null,  "DATA_SCALE"),
    TSQUERY                 (StandardTypeMetadata.ILLEGAL),
    TSVECTOR                (StandardTypeMetadata.ILLEGAL),
    TXID_SNAPSHOT           (StandardTypeMetadata.ILLEGAL),
    UNIQUEIDENTIFIER        (StandardTypeMetadata.ILLEGAL),
    UUID                    (StandardTypeMetadata.ILLEGAL),
    UROWID                  (StandardTypeMetadata.UROWID, "DATA_LENGTH"),
    VARBIT                  (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    TINYBLOB                (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    TINYINT                 (StandardTypeMetadata.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    TINYTEXT                (StandardTypeMetadata.CLOB, "DATA_LENGTH"),
    VARBINARY               (StandardTypeMetadata.BLOB, "DATA_LENGTH"),
    VARCHAR                 (StandardTypeMetadata.VARCHAR2, "DATA_LENGTH"),
    VARCHAR2                (StandardTypeMetadata.VARCHAR2, "DATA_LENGTH"),
    XML                     (StandardTypeMetadata.ILLEGAL),
    YEAR                    (StandardTypeMetadata.DATE, "DATA_LENGTH");
    /**
     * 标准类型
     */
    private final TypeMetadata standard;
    /**
     * 读取元数据时 长度对应的列<br/>
     * 正常情况下只有一列<br/>
     * 如果需要取多列以,分隔
     */
    private String length;
    /**
     * 读取元数据时 有效位数对应的列<br/>
     * 正常情况下只有一列<br/>
     * 如果需要取多列以,分隔
     */
    private String precision;
    /**
     * 读取元数据时 小数位对应的列<br/>
     * 正常情况下只有一列<br/>
     * 如果需要取多列以,分隔
     */
    private String scale;
    private OracleTypeMetadataAlias(TypeMetadata standard){
        this.standard = standard;
    }

    private OracleTypeMetadataAlias(TypeMetadata standard, String length, String precision, String scale){
        this.standard = standard;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
    }
    private OracleTypeMetadataAlias(TypeMetadata standard, String precision, String scale){
        this.standard = standard;
        this.precision = precision;
        this.scale = scale;
    }
    private OracleTypeMetadataAlias(TypeMetadata standard, String length){
        this.standard = standard;
        this.length = length;
    }

    @Override
    public TypeMetadata standard() {
        return standard;
    }

    @Override
    public String length() {
        return length;
    }
    @Override
    public String precision() {
        return precision;
    }

    @Override
    public String scale() {
        return scale;
    }

    @Override
    public TypeMetadata.Config config() {
        TypeMetadata.Config config = new TypeMetadata.Config();
        config.setLengthColumn(this.length);
        config.setPrecisionColumn(this.precision);
        config.setScaleColumn(this.scale);
        return config;
    }

}
