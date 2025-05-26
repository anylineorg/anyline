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

package org.anyline.data.jdbc.voltdb;

import org.anyline.adapter.DataWriter;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.util.DateUtil;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public enum VoltDBWriter {

    TimestampWriter(new Object[]{Date.class, Timestamp.class, LocalDateTime.class, LocalDate.class, "Timestamp"}, new DataWriter() {
        @Override
        public Object write(Object value, Boolean placeholder, Boolean unicode, TypeMetadata type) {
            Date date = DateUtil.parse(value);
            if(null != date) {
                value = date.getTime() * 1000;
                //For String variables, the text must be formatted as either YYYY-MM-DD hh.mm.ss.nnnnnn or just the date portion YYYY-MM-DD.
            }
            return value;
        }
    })
    ;
    public Object[] supports() {
        return supports;
    }
    public DataWriter writer() {
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    VoltDBWriter(Object[] supports, DataWriter writer) {
        this.supports = supports;
        this.writer = writer;
    }
}
