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


package org.anyline.data.jdbc.function.init;


import org.anyline.metadata.DateFormatPattern;


public enum MSSQLGenusDateFormatPattern implements DateFormatPattern {
    YYYY(META.YYYY,"yyyy"),
    YY(META.YY,"yy"),
    MM(META.MM,"MM"),
    DD(META.DD,"dd"),
    HH24(META.HH24,"HH"),
    HH12(META.HH12,"hh"),
    MI(META.MI,"mm"),
    SS(META.SS,"ss"),
    MS(META.MS,"fff"),
    AM(META.AM,"tt"),
    MONTH(META.MONTH,"MMMM"),
    MON(META.MON,"MMM"),
    WEEK_NAME(META.WEEK_NAME,"dddd"),
    WEEK_NM(META.WEEK_NM,"ddd"),
    DAY_OF_MONTH(META.DAY_OF_MONTH,"dd"),
    DAY_OF_YEAR(META.DAY_OF_YEAR,"ddd"),
    DAY_OF_WEEK(META.DAY_OF_WEEK,"d"),
    WEEK_OF_YEAR(META.WEEK_OF_YEAR,"ww"),
    WW(META.WW,"ww"),
    IW(META.IW,"isoww"),
    YEAR(META.YEAR,"yyyy"),
    TIME(META.TIME,"HH:mm:ss"),
    TIME_AP(META.TIME_AP,"hh:mm:ss tt"),
    DS(META.DS,"yyyy-MM-dd"),
    DL(META.DL,"MMMM dd, yyyy"),
    TS(META.TS,"HH:mm:ss"),
    DDD(META.DDD,"dy"),
    Q(META.Q,"q"),
    Mon(META.Mon,"MMM"),
    mon(META.mon,"MMM"),
    dy(META.dy,"ddd"),
    CC(META.CC,"yy"),
    Y(META.Y,"y"),
    SSSS(META.SSSS,"sssss"),
    W(META.W,"w"),
    ;
    private final String define;
    private final META meta;

    public META meta(){
        return meta;
    }
    public String define(){
        return define;
    }
    MSSQLGenusDateFormatPattern(META meta, String define) {
        this.meta = meta;
        this.define = define;
    }
}