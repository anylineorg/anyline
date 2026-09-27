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


public enum MySQLGenusDateFormatPattern implements DateFormatPattern {
    //注册先定义长的、常用的
    a(META.WEEK_NM,"%a"),
    D(META.DAY_OF_MONTH_TH,"%D"),
    d(META.DD,"%d"),
    e(META.DD,"%e"),
    f(META.FF,"%f"),
    H(META.HH24,"%H"),
    h(META.HH12,"%h"),
    I(META.HH12,"%I"),
    i(META.MI,"%i"),
    j(META.DAY_OF_YEAR,"%j"), //一年中第几天
    k(META.HH24,"%k"),
    l(META.HH12,"%l"),
    M(META.MONTH,"%M"),
    m(META.MM,"%m"),
    p(META.AM,"%p"),
    r(META.TIME_AP,"%r"),
    s(META.SS,"%s"),
    S(META.SS,"%S"),
    T(META.TIME,"%T"), //21:55:09
    U(META.WW,"%U"),
    u(META.IW,"%u"),
    V(META.WEEK_OF_YEAR,"%V"),
    v(META.WK_OF_YEAR,"%v"),
    W(META.WEEK_NAME,"%W"),
    w(META.DAY_OF_WEEK,"%w"),
    b(META.MON,"%b"),
    c(META.MM,"%c"),
    Y(META.YYYY,"%Y"),
    y(META.YY,"%y"),
    X(META.YYYY,"%X"),
    x(META.YYYY,"%x"),
    DAY_OF_MONTH(META.DAY_OF_MONTH,"%e"),
    WEEK_OF_YEAR(META.WEEK_OF_YEAR,"%V"),
    WK_OF_YEAR(META.WK_OF_YEAR,"%v"),
    YEAR(META.YEAR,"%Y"),
    CC(META.CC,"%Y"),
    ;
    private final String define;
    private final META meta;

    public META meta(){
        return meta;
    }
    public String define(){
        return define;
    }
    MySQLGenusDateFormatPattern(META meta, String define) {
        this.meta = meta;
        this.define = define;
    }
}