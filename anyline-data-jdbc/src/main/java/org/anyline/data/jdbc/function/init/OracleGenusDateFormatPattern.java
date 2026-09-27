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


public enum OracleGenusDateFormatPattern implements DateFormatPattern {
    //注册先定义长的、常用的
    AD(META.AD,"AD"),
    A_D_(META.A_D_,"A.D."),
    AM(META.AM,"AM"),
    A_M_(META.A_M_,"A.M."),
    BC(META.BC,"BC"),
    B_C_(META.B_C_,"B.C."),
    CC(META.CC,"CC"),
    SCC(META.CC,"SCC"),
    DDD(META.DDD,"DDD"),
    DD(META.DD,"DD"),
    DAY(META.DAY,"DAY"),
    DL(META.DL,"DL"),
    DS(META.DS,"DS"),
    DY(META.DY,"DY"),
    EE(META.EE,"EE"),
    FX(META.FX,"FX"),
    FM(META.FM,"FM"),
    HH24(META.HH24,"HH24"),
    HH12(META.HH12,"HH12"),
    HH(META.HH12,"HH"),
    IW(META.IW,"IW"),
    IYY(META.IYY,"IYY"),
    IY(META.IY,"IY"),
    MI(META.MI,"MI"),
    MM(META.MM,"MM"),
    MONTH(META.MONTH,"MONTH"),
    MON(META.MON,"MON"),
    PM(META.PM,"PM"),
    P_M_(META.P_M_,"P.M."),
    Q(META.Q,"Q"),
    RM(META.RM,"RM"),
    RR(META.YY,"RR"),
    RRRR(META.YYYY,"RRRR"),
    SSSSS(META.SSSSS,"SSSSS"),
    SS(META.SS,"SS"),
    TS(META.TS,"TS"),
    TZD(META.TZD,"TZD"),
    TZH(META.TZH,"TZH"),
    TZM(META.TZM,"TZM"),
    TZR(META.TZR,"TZR"),
    WW(META.WW,"WW"),
    SYEAR(META.SYEAR,"SYEAR"),
    YEAR(META.YEAR,"YEAR"),
    Y_YYY(META.Y_YYY,"Y,YYY"),
    IYYY(META.IYYY,"IYYY"),
    SYYYY(META.SYYYY,"SYYYY"),
    YYYY(META.YYYY,"YYYY"),
    YYY(META.YYY,"YYY"),
    YY(META.YY,"YY"),
    Y(META.Y,"Y"),
    X(META.X,"X"),
    D(META.DY_OF_WEEK,"D"),
    E(META.E,"E"),
    I(META.I,"I"),
    J(META.J,"J"),
    W(META.W,"W")
    ;
    private final String define;
    private final META meta;

    public META meta(){
        return meta;
    }
    public String define(){
        return define;
    }
    OracleGenusDateFormatPattern(META meta, String define) {
        this.meta = meta;
        this.define = define;
    }
}