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


public enum PostgresGenusDateFormatPattern implements DateFormatPattern {
    //注册先定义长的、常用的
    HH12(META.HH12,"HH12"),
    HH24(META.HH24,"HH24"),
    HH(META.HH12,"HH"),
    MI(META.MI,"MI"),
    SS(META.SS,"SS"),
    MS(META.MS,"MS"),
    US(META.US,"US"),
    FF1(META.FF1,"FF1"),
    FF2(META.FF2,"FF2"),
    FF3(META.FF3,"FF3"),
    FF4(META.FF4,"FF4"),
    FF5(META.FF5,"FF5"),
    FF6(META.FF6,"FF6"),
    SSSSS(META.SSSSS,"SSSSS"),
    SSSS(META.SSSS,"SSSS"),
    AM(META.AM,"AM"),
    am(META.am,"am"),
    PM(META.PM,"PM"),
    pm(META.pm,"pm"),
    A_M_(META.A_M_,"A.M."),
    a_m_(META.a_m_,"a.m."),
    P_M_(META.P_M_,"P.M."),
    p_m_(META.p_m_,"p.m."),
    Y_YYY(META.Y_YYY,"Y,YYY"),
    YYYY(META.YYYY,"YYYY"),
    YYY(META.YYY,"YYY"),
    YY(META.YY,"YY"),
    Y(META.Y,"Y"),
    IYYY(META.IYYY,"IYYY"),
    IYY(META.IYY,"IYY"),
    IY(META.IY,"IY"),
    I(META.I,"I"),
    BC(META.BC,"BC"),
    bc(META.bc,"bc"),
    AD(META.AD,"AD"),
    ad(META.ad,"ad"),
    B_C_(META.B_C_,"B.C."),
    b_c_(META.b_c_,"b.c."),
    A_D_(META.A_D_,"A.D."),
    a_d_(META.a_d_,"a.d."),
    MONTH(META.MONTH,"MONTH"),
    Month(META.Month,"Month"),
    month(META.month,"month"),
    MON(META.MON,"MON"),
    Mon(META.Mon,"Mon"),
    mon(META.mon,"mon"),
    MM(META.MM,"MM"),
    DAY(META.DAY,"DAY"),
    Day(META.Day,"Day"),
    day(META.day,"day"),
    DY(META.DY,"DY"),
    Dy(META.Dy,"Dy"),
    dy(META.dy,"dy"),
    IDDD(META.IDDD,"IDDD"),
    DDD(META.DDD,"DDD"),
    DD(META.DD,"DD"),
    D(META.DY_OF_WEEK,"D"),
    ID(META.ID,"ID"),
    W(META.W,"W"),
    WW(META.WW,"WW"),
    IW(META.IW,"IW"),
    CC(META.CC,"CC"),
    J(META.J,"J"),
    Q(META.Q,"Q"),
    RM(META.RM,"RM"),
    rm(META.rm,"rm"),
    TZH(META.TZH,"TZH"),
    TZM(META.TZM,"TZM"),
    TZ(META.TZ,"TZ"),
    tz(META.tz,"tz"),
    OF(META.OF,"OF");
    private final String define;
    private final META meta;

    public META meta(){
        return meta;
    }
    public String define(){
        return define;
    }
    PostgresGenusDateFormatPattern(META meta, String define) {
        this.meta = meta;
        this.define = define;
    }
}