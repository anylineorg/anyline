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

import org.anyline.adapter.DataWriter;
import org.anyline.adapter.init.ConvertAdapter;
import org.anyline.util.DateUtil;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

public enum OracleWriter {

    DateWriter(new Object[]{java.sql.Date.class, Timestamp.class, java.util.Date.class, LocalDate.class, LocalDateTime.class
    ,oracle.sql.TIMESTAMP.class, oracle.sql.TIMESTAMPTZ.class, oracle.sql.TIMESTAMPLTZ.class, oracle.sql.TIMEZONETAB.class
    ,oracle.sql.DATE.class}, new DataWriter() {
        @Override
        public Object write(Object value, boolean placeholder) {
            if(!placeholder && null != value) {
                Date date = (Date)ConvertAdapter.convert(value, Date.class, false);
                value = " to_date ( '"+DateUtil.format(date)+"' , 'YYYY-MM-DD HH24:MI:SS')";
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
    OracleWriter(Object[] supports, DataWriter writer){
        this.supports = supports;
        this.writer = writer;
    }
}
