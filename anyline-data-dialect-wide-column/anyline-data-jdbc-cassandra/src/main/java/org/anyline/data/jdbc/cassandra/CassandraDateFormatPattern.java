/*
 * Copyright 2006-2026 DeepBit Co.,Ltd. All rights reserved.
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


package org.anyline.data.jdbc.cassandra;


import org.anyline.metadata.DateFormatPattern;

/**
 * Cassandra CQL date/time formatting patterns.
 * Cassandra uses Java SimpleDateFormat patterns for date formatting in CQL.
 */

public enum CassandraDateFormatPattern implements DateFormatPattern {
    YYYY(META.YYYY, "yyyy"),
    YY(META.YY, "yy"),
    MM(META.MM, "MM"),
    DD(META.DD, "dd"),
    HH24(META.HH24, "HH"),
    HH12(META.HH12, "hh"),
    MI(META.MI, "mm"),
    SS(META.SS, "ss"),
    FF(META.FF, "SSS"),
    FF3(META.FF3, "SSS"),
    FF6(META.FF6, "SSSSSS"),
    AM(META.AM, "a"),
    am(META.am, "a"),
    PM(META.PM, "a"),
    pm(META.pm, "a"),
    MONTH(META.MONTH, "MMMM"),
    Month(META.Month, "MMMM"),
    month(META.month, "MMMM"),
    MON(META.MON, "MMM"),
    Mon(META.Mon, "MMM"),
    mon(META.mon, "MMM"),
    DAY(META.DAY, "EEEE"),
    Day(META.Day, "EEEE"),
    day(META.day, "EEEE"),
    MONTH_NAME(META.MONTH, "MMMM"),
    DAY_OF_WEEK(META.DAY_OF_WEEK, "u"),
    DAY_OF_MONTH(META.DAY_OF_MONTH, "d"),
    DAY_OF_YEAR(META.DAY_OF_YEAR, "D"),
    WEEK_OF_YEAR(META.WEEK_OF_YEAR, "w"),
    ;

    private final String define;
    private final META meta;

    public META meta() {
        return meta;
    }

    public String define() {
        return define;
    }

    CassandraDateFormatPattern(META meta, String define) {
        this.meta = meta;
        this.define = define;
    }
}