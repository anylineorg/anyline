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

package org.anyline.data.influxdb.entity;

import org.anyline.entity.DataSet;

public class InfluxSet extends DataSet {
    private String measurement;
    private String time;
    private String time_format;
    private String time_precision;
    private String time_zone;
    private String group_by;
    private String order_by;
    private String limit;
    private String offset;
    private String fill;
    private String fill_value;
    private String fill_time;
    private String fill_time_value;
    private String fill_time_format;
    private String fill_time_precision;
    private String fill_time_zone;

    protected String bucket;
    protected String org;

    public InfluxSet bucket(String bucket) {
        this.bucket = bucket;
        return this;
    }
    public String bucket() {
        return this.bucket;
    }
    public InfluxSet org(String org) {
        this.org = org;
        return this;
    }
    public String org() {
        return this.org;
    }
}
