/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.data.influxdb.entity;

import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.client.write.PointSettings;
import org.anyline.data.influxdb.metadata.InfluxMeasurement;
import org.anyline.entity.OriginRow;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.Instant;
import java.util.Map;

public class InfluxRow extends OriginRow {
    protected String bucket;
    protected String org;
    protected Point point;
    public InfluxRow(Point point) {
        this.point = point;
    }
    public InfluxRow(String measurement) {
        point = new Point(measurement);
        primaryKeys.clear();
        primaryKeys.add("time");
        parseKeyCase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
        setTable(measurement);
    }
    public InfluxRow(InfluxMeasurement measurement) {
        point = new Point(measurement.getName());
        primaryKeys.clear();
        primaryKeys.add("time");
        parseKeyCase(keyCase);
        createTime = System.currentTimeMillis();
        nanoTime = System.currentTimeMillis();
        setTable(measurement);
    }
    public Point point() {
        return this.point;
    }
    public InfluxRow bucket(String bucket) {
        this.bucket = bucket;
        return this;
    }
    public String bucket() {
        return this.bucket;
    }
    public InfluxRow org(String org) {
        this.org = org;
        return this;
    }
    public String org() {
        return this.org;
    }
    @Nonnull
    public static InfluxRow measurement(@Nonnull String measurement) {
        return new InfluxRow(measurement);
    }

    @Nonnull
    public InfluxRow addTag(@Nonnull String key, @Nullable String value) {
        point.addTag(key, value);
        return this;
    }

    @Nonnull
    public InfluxRow addTags(@Nonnull Map<String, String> tags) {
        point.addTags(tags);
        return this;
    }

    @Nonnull
    public InfluxRow addField(@Nonnull String field, boolean value) {
        point.addField(field, value);
        return this;
    }

    public InfluxRow addField(@Nonnull String field, long value) {
        point.addField(field, value);
        return this;
    }

    @Nonnull
    public InfluxRow addField(@Nonnull String field, double value) {
        point.addField(field, value);
        return this;
    }

    @Nonnull
    public InfluxRow addField(@Nonnull String field, @Nullable Number value) {
        point.addField(field, value);
        return this;
    }

    @Nonnull
    public InfluxRow addField(@Nonnull String field, @Nullable String value) {
        point.addField(field, value);
        return this;
    }

    @Nonnull
    public InfluxRow addFields(@Nonnull Map<String, Object> fields) {
        point.addFields(fields);
        return this;
    }

    @Nonnull
    public InfluxRow time(@Nullable Instant time, @Nonnull WritePrecision precision) {
        point.time(time, precision);
        return this;
    }

    @Nonnull
    public InfluxRow time(@Nullable Number time, @Nonnull WritePrecision precision) {
        point.time(time, precision);
        return this;
    }

    @Nonnull
    public InfluxRow time(@Nullable Long time, @Nonnull WritePrecision precision) {
        point.time(time, precision);
        return this;
    }

    @Nonnull
    public WritePrecision getPrecision() {
        return point.getPrecision();
    }

    public boolean hasFields() {
        return point.hasFields();
    }

    @Nonnull
    public String toLineProtocol() {
        return point.toLineProtocol();
    }

    @Nonnull
    public String toLineProtocol(@Nullable PointSettings settings) {
        return point.toLineProtocol(settings);
    }
}
