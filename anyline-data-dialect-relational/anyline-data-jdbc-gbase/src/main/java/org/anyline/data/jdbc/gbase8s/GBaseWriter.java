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

package org.anyline.data.jdbc.gbase8s;

import org.anyline.adapter.DataWriter;
import org.anyline.metadata.type.TypeMetadata;
import org.anyline.proxy.ConvertProxy;
import org.anyline.util.DateUtil;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public enum GBaseWriter {
    DateWriter(new Object[]{java.sql.Date.class, Timestamp.class, Date.class, LocalDate.class, LocalDateTime.class}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder, TypeMetadata type) {
            if(!placeholder && null != value) {
                Date date = (Date) ConvertProxy.convert(value, Date.class, false);
                value = " to_date ( '"+DateUtil.format(date)+"', 'YYYY-MM-DD HH24:MI:SS')";
            }
            return value;
        }
    }),
    ;
    public Object[] supports(){
        return supports;
    }
    public DataWriter writer(){
        return writer;
    }
    private final Object[] supports;
    private final DataWriter writer;
    GBaseWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
