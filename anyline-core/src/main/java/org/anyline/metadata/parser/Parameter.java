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

package org.anyline.metadata.parser;

public class Parameter {

    enum DATE_FORMAT_PATTERN {
        HH("小时(01–12)"),
        HH12("小时(01–12)"),
        HH24("时间段(00–23)"),
        MI("一分钟(00–59)"),
        SS("第二次(00–59)"),
        MS("毫秒(000–999)"),
        US("微秒(000000–999999)"),
        FF1("第二节十局(0–9)"),
        FF2("百分之一秒(00–99)"),
        FF3("毫秒(000–999)"),
        FF4("十分之一毫秒(0000–9999)"),
        FF5("百分之一毫秒(00000–99999)"),
        FF6("微秒(000000–999999)"),
        SSSS("午夜过后的秒钟(0–86399)"),
        SSSSS("午夜过后的秒钟(0–86399)"),
        AM("子午指示器(无周期)"),
        am("子午指示器(无周期)"),
        PM("子午指示器(无周期)"),
        pm("子午指示器(无周期)"),
        A_M_("子午指示器(无周期)"),
        a_m_("子午指示器(无周期)"),
        P_M_("子午指示器(无周期)"),
        p_m_("子午指示器(带句号)"),
        Y_YYY("年份(4位及以上)带逗号"),
        YYYY("年份(4位及以上)"),
        YYY("年份后三位"),
        YY("年份的最后两位数字"),
        Y("年份的最后一位数字"),
        IYYY("ISO 8601 周编号年份(4位及以上)"),
        IYY("ISO 8601周编号年份的后三位数字"),
        IY("ISO 8601周编号年份的后两位数字"),
        I("ISO 8601周编号年份的最后一位数字"),
        BC("纪元指示器(无周期)"),
        bc("纪元指示器(无周期)"),
        AD("纪元指示器(无周期)"),
        ad("纪元指示器(无周期)"),
        B_C_("纪元指示器(无周期)"),
        b_c_("纪元指示器(无周期)"),
        A_D_("纪元指示器(无周期)"),
        a_d_("时代指示器(含周期)"),
        MONTH("完整的大写月份名称(空白填充至9字)"),
        Month("完整大写月份名称(空格填充至9个字符)"),
        month("完整的小写月份名称(空格填充至9字)"),
        MON("缩写为大写月份名(英文为3字，局部长度有所不同)"),
        Mon("缩写为大写月份名(英文为3个字符，本地化长度有所不同)"),
        mon("缩写小写月份名(英文为3字，局部长度有所不同)"),
        MM("月份号(01–12)"),
        DAY("完整的大写字母日名(空格填充至9字元)"),
        Day("全大写的日期名称(空格填充至9字)"),
        day("完整的小写日期名称(空格填充至9字)"),
        DY("大写日名缩写(英文为3字，本地长度有所不同)"),
        Dy("缩写为大写的日名(英文为3字，本地化长度有所不同)"),
        dy("简写小写的日名(英文为3字，本地化长度有所不同)"),
        DDD("年份(001–366)"),
        IDDD("ISO 8601 周编号年(001–371;一年的第1天为第一个ISO周的星期一)"),
        DD("月份的星期几(01–31)"),
        D("星期几，周日()至周六(17)"),
        ID("ISO 8601 星期一()至周日(17)"),
        W("每月的一周(1–5)(第一周从每月的第一天开始)"),
        WW("年级周数(1–53)(第一周从新年的第一天开始)"),
        IW("ISO 8601周编号年(01–53;第一周为第一个星期四)"),
        CC("世纪(两位数字)(二十一世纪始于2001-01-01)"),
        J("儒略历日期(整数日，自公元前4714年11月24日当地午夜起)"),
        Q("季度"),
        RM("月份(大写罗马数字)(I–XII;I=一月)"),
        rm("月份，罗马数字小写(i–xii;i=一月)"),
        TZ("大写时区缩写"),
        tz("小写时区缩写"),
        TZH("时区时间"),
        TZM("时区分钟"),
        OF("UTC的时区偏移(HH或HHMM:)");
        private final String title;
        DATE_FORMAT_PATTERN(String title){
            this.title = title;
        }
        public String title(){
            return title;
        }
    }
    private String content;
    private int startIndex;
    private int endIndex;
    private boolean isFunction;
    private FunctionCall call;

    public Parameter(String content, int startIndex, int endIndex, boolean isFunction) {
        this.content = content;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.isFunction = isFunction;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }

    public void setCall(boolean call) {
        isFunction = call;
    }

    // Getter和Setter方法
    public String getContent() { return content; }
    public int getStartIndex() { return startIndex; }
    public int getEndIndex() { return endIndex; }
    public boolean getCall() { return isFunction; }
    public FunctionCall getFunction() { return call; }
    public void setFunction(FunctionCall function) {
        this.call = function;
    }

    @Override
    public String toString() {
        return String.format("参数[%d-%d]: %s %s",
                startIndex, endIndex, content, isFunction ? "(函数)" : "(其他)");
    }
}