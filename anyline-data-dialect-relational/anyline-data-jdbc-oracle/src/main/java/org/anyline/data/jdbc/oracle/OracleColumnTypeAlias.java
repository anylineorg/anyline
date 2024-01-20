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

import org.anyline.data.metadata.ColumnTypeAlias;
import org.anyline.metadata.type.init.StandardColumnType;
import org.anyline.metadata.type.TypeMetadata;

public enum OracleColumnTypeAlias implements ColumnTypeAlias {

    BFILE                   (StandardColumnType.BFILE),
    BINARY_DOUBLE           (StandardColumnType.BINARY_DOUBLE),
    BINARY_FLOAT            (StandardColumnType.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    BIGINT                  (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BIGSERIAL               (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BINARY                  (StandardColumnType.BLOB,"DATA_LENGTH"),
    BIT                     (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BLOB                    (StandardColumnType.BLOB, "DATA_LENGTH"),
    BOOL                    (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    BOX                     (StandardColumnType.ILLEGAL),
    BYTEA                   (StandardColumnType.BLOB, "DATA_LENGTH"),
    CHAR                    (StandardColumnType.CHAR, "DATA_LENGTH"),
    CIDR                    (StandardColumnType.ILLEGAL),
    CIRCLE                  (StandardColumnType.ILLEGAL),
    CLOB                    (StandardColumnType.CLOB, "DATA_LENGTH"),
    DATE                    (StandardColumnType.DATE, "DATA_LENGTH"),
    DATETIME                (StandardColumnType.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    DATETIME2               (StandardColumnType.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    DATETIMEOFFSET          (StandardColumnType.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    DECIMAL                 (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    DOUBLE                  (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    ENUM                    (StandardColumnType.ILLEGAL),
    FLOAT                   (StandardColumnType.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    FLOAT4                  (StandardColumnType.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    FLOAT8                  (StandardColumnType.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    GEOGRAPHY               (StandardColumnType.ILLEGAL),
    GEOMETRY                (StandardColumnType.ILLEGAL),
    GEOMETRYCOLLECTION      (StandardColumnType.ILLEGAL),
    HIERARCHYID             (StandardColumnType.ILLEGAL),
    IMAGE                   (StandardColumnType.BLOB, "DATA_LENGTH"),
    INET                    (StandardColumnType.ILLEGAL),
    INTERVAL                (StandardColumnType.ILLEGAL),
    INT                     (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INT2                    (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INT4                    (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INT8                    (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    INTEGER                 (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    JSON                    (StandardColumnType.CLOB, "DATA_LENGTH"),
    JSONB                   (StandardColumnType.BLOB, "DATA_LENGTH"),
    LINE                    (StandardColumnType.ILLEGAL),
    LONG                    (StandardColumnType.LONG_TEXT, "DATA_LENGTH"),
    LONGBLOB                (StandardColumnType.BLOB, "DATA_LENGTH"),
    LONGTEXT                (StandardColumnType.CLOB, "DATA_LENGTH"),
    LSEG                    (StandardColumnType.ILLEGAL),
    MACADDR                 (StandardColumnType.ILLEGAL),
    MONEY                   (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    NUMBER                  (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    NCHAR                   (StandardColumnType.NCHAR, "DATA_LENGTH"),
    NCLOB                   (StandardColumnType.NCLOB, "DATA_LENGTH"),
    NTEXT                   (StandardColumnType.NCLOB, "DATA_LENGTH"),
    NVARCHAR                (StandardColumnType.NVARCHAR2, "DATA_LENGTH"),
    NVARCHAR2               (StandardColumnType.NVARCHAR2, "DATA_LENGTH"),
    PATH                    (StandardColumnType.ILLEGAL),
    MEDIUMBLOB              (StandardColumnType.BLOB, "DATA_LENGTH"),
    MEDIUMINT               (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    MEDIUMTEXT              (StandardColumnType.CLOB, "DATA_LENGTH"),
    MULTILINESTRING         (StandardColumnType.ILLEGAL),
    MULTIPOINT              (StandardColumnType.ILLEGAL),
    MULTIPOLYGON            (StandardColumnType.ILLEGAL),
    NUMERIC                 (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    POINT                   (StandardColumnType.ILLEGAL),
    POLYGON                 (StandardColumnType.ILLEGAL),
    REAL                    (StandardColumnType.FLOAT_ORACLE, "DATA_LENGTH", "DATA_PRECISION", null),
    RAW                     (StandardColumnType.RAW, "DATA_LENGTH"),
    ROWID                   (StandardColumnType.ROWID, "DATA_LENGTH"),
    SERIAL                  (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SERIAL2                 (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SERIAL4                 (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SERIAL8                 (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SET                     (StandardColumnType.ILLEGAL),
    SMALLDATETIME           (StandardColumnType.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    SMALLMONEY              (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SMALLINT                (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SMALLSERIAL             (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    SQL_VARIANT             (StandardColumnType.ILLEGAL),
    SYSNAME                 (StandardColumnType.ILLEGAL),
    TEXT                    (StandardColumnType.CLOB, "DATA_LENGTH"),
    TIME                    (StandardColumnType.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMEZ                   (StandardColumnType.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMESTAMP               (StandardColumnType.TIMESTAMP, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMESTAMP_WITH_LOCAL_ZONE    (StandardColumnType.TIMESTAMP_WITH_LOCAL_ZONE, "DATA_LENGTH", null,  "DATA_SCALE"),
    TIMESTAMP_WITH_ZONE          (StandardColumnType.TIMESTAMP_WITH_ZONE, "DATA_LENGTH", null,  "DATA_SCALE"),
    TSQUERY                 (StandardColumnType.ILLEGAL),
    TSVECTOR                (StandardColumnType.ILLEGAL),
    TXID_SNAPSHOT           (StandardColumnType.ILLEGAL),
    UNIQUEIDENTIFIER        (StandardColumnType.ILLEGAL),
    UUID                    (StandardColumnType.ILLEGAL),
    UROWID                  (StandardColumnType.UROWID, "DATA_LENGTH"),
    VARBIT                  (StandardColumnType.BLOB, "DATA_LENGTH"),
    TINYBLOB                (StandardColumnType.BLOB, "DATA_LENGTH"),
    TINYINT                 (StandardColumnType.NUMBER, "DATA_LENGTH", "DATA_PRECISION", "DATA_SCALE"),
    TINYTEXT                (StandardColumnType.CLOB, "DATA_LENGTH"),
    VARBINARY               (StandardColumnType.BLOB, "DATA_LENGTH"),
    VARCHAR                 (StandardColumnType.VARCHAR2, "DATA_LENGTH"),
    VARCHAR2                (StandardColumnType.VARCHAR2, "DATA_LENGTH"),
    XML                     (StandardColumnType.ILLEGAL),
    YEAR                    (StandardColumnType.DATE, "DATA_LENGTH");
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
    private OracleColumnTypeAlias(TypeMetadata standard){
        this.standard = standard;
    }

    private OracleColumnTypeAlias(TypeMetadata standard, String length, String precision, String scale){
        this.standard = standard;
        this.length = length;
        this.precision = precision;
        this.scale = scale;
    }
    private OracleColumnTypeAlias(TypeMetadata standard, String precision, String scale){
        this.standard = standard;
        this.precision = precision;
        this.scale = scale;
    }
    private OracleColumnTypeAlias(TypeMetadata standard, String length){
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
