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


import org.anyline.data.adapter.DateFormatPatternFactory;
import org.anyline.data.adapter.function.SystemFunctionFactory;
import org.anyline.metadata.DateFormatPattern;
import org.anyline.metadata.SystemFunction;
import org.anyline.metadata.type.DatabaseOrigin;
import org.anyline.metadata.type.DatabaseType;

/**
 * 系统函数注册初始化器
 * <p>
 * 在启动时将四个数据库（MySQL、Oracle、PostgreSQL、MSSQL）的
 * 系统函数定义和日期格式模式注册到对应的工厂类中，
 * 供 DefaultFunctionConverter 跨数据库转换时查询使用。
 */


public class SystemFunctionHolder {
    private static final String mysql_illegals = "";
    private static final String oracle_illegals = "";
    private static final String postgres_illegals = "";
    private static final String mssql_illegals = "";
    private static final String impala_illegals = "";
    /**
     * 执行注册：遍历所有 GenusFunction 和 GenusDateFormatPattern 枚举并注册
     */
    public SystemFunctionHolder(){
        //函数
        for (SystemFunction function:MySQLGenusFunction.values()) {
            SystemFunctionFactory.reg(DatabaseOrigin.MySQL, function);
        }
        for (SystemFunction function:OracleGenusFunction.values()) {
            SystemFunctionFactory.reg(DatabaseOrigin.Oracle, function);
        }
        for (SystemFunction function:PostgresGenusFunction.values()) {
            SystemFunctionFactory.reg(DatabaseOrigin.PostgreSQL, function);
        }
        for (SystemFunction function: MSSQLGenusFunction.values()) {
            SystemFunctionFactory.reg(DatabaseOrigin.MSSQL, function);
        }
        for (SystemFunction function: ImpalaGenusFunction.values()) {
            SystemFunctionFactory.reg(DatabaseType.Impala, function);
        }
        for (SystemFunction function: ImpalaGenusFunction.values()) {
            SystemFunctionFactory.reg(DatabaseType.Cloudera, function);
        }
        //日期格式化参数
        for(DateFormatPattern pattern:MySQLGenusDateFormatPattern.values()){
            DateFormatPatternFactory.reg(DatabaseOrigin.MySQL, pattern, false);
        }
        for(DateFormatPattern pattern:OracleGenusDateFormatPattern.values()){
            DateFormatPatternFactory.reg(DatabaseOrigin.Oracle, pattern, false);
        }
        for(DateFormatPattern pattern:PostgresGenusDateFormatPattern.values()){
            DateFormatPatternFactory.reg(DatabaseOrigin.PostgreSQL, pattern, false);
        }
        for(DateFormatPattern pattern: MSSQLGenusDateFormatPattern.values()){
            DateFormatPatternFactory.reg(DatabaseOrigin.MSSQL, pattern, false);
        }
        for(DateFormatPattern pattern: ImpalaGenusDateFormatPattern.values()){
            DateFormatPatternFactory.reg(DatabaseOrigin.Impala, pattern, false);
        }
        //明确不支持部分
        SystemFunctionFactory.illegal(DatabaseOrigin.MySQL, mysql_illegals);
        SystemFunctionFactory.illegal(DatabaseOrigin.Oracle, oracle_illegals);
        SystemFunctionFactory.illegal(DatabaseOrigin.PostgreSQL, postgres_illegals);
        SystemFunctionFactory.illegal(DatabaseOrigin.MSSQL, mssql_illegals);
        SystemFunctionFactory.illegal(DatabaseOrigin.Impala, impala_illegals);

    }
}