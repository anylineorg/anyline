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


package org.anyline.data.metadata.function;

import org.anyline.metadata.SystemFunction.META;
import org.anyline.metadata.type.DatabaseType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
* MATH 类函数定义
* 包含所有数据库的 MATH 相关函数
*/
public enum MathFunction implements META {
		ABS("绝对值", "expression"),
		ACOS("反余弦", "expression"),
		ACOSD("反余弦度", "expression"),
		ACOSH("反双曲余弦", "expression"),
		ASIN("反正弦", "expression"),
		ASIND("反正弦度", "expression"),
		ASINH("逆双曲正弦", "expression"),
		ATAN("反正切", "expression"),
		ATAN2("双参数反正切", "y,x"),
		ATAN2D("反正切度", "y,x"),
		ATAND("反正切度", "expression"),
		ATANH("反双曲正切", "expression"),
		ATN2("双参数反正切", "y"),
		CBRT("立方根", "expression"),
		CEIL("向上取整", "expression"),
		CEILING("向上取整", "expression"),
		COS("余弦", "expression"),
		COSD("参数元素余弦,以度为单位", "expression"),
		COSH("双曲余弦", "expression"),
		COT("余切", "expression"),
		COTD("余切，以度为单位参数", "expression"),
		DCBRT("十进制立方根", DatabaseType.PostgreSQL),
		DEGREES("弧度转角度", "expression"),
		DEXP("十进制指数", DatabaseType.PostgreSQL),
		DIV("整数除法", "dividend,divisor"),
		DLOG1("十进制自然对数", DatabaseType.PostgreSQL),
		DLOG10("十进制以10为底对数", DatabaseType.PostgreSQL),
		DPOW("十进制幂", DatabaseType.PostgreSQL),
		DROUND("十进制四舍五入", "expression", DatabaseType.PostgreSQL),
		DSQRT("十进制平方根", DatabaseType.PostgreSQL),
		DTRUNC("十进制截断", "expression", DatabaseType.PostgreSQL),
		ERF("误差", "expression"),
		ERFC("互补误差", "expression"),
		EXP("指数", "expression"),
		FACTORIAL("阶乘", "expression"),
		FLOOR("向下取整", "expression"),
		GAMMA("伽马", "expression"),
		GCD("最大公约数", "expression1"),
		IS_FINITE("否是有穷日期时间", "expression"),
		LCM("最小公倍数", "expression1"),
		LGAMMA("伽马绝对值自然对数", "expression"),
		LN("自然对数", "expression"),
		LOG("对数", "expression"),
		LOG10("以10为底对数", "expression"),
		LOG2("以2为底对数", "expression"),
		MIN_SCALE("精确表示所提供值所需最小刻度", "expression"),
		MOD("取模", "dividend,divisor"),
		NUMERIC_ABS("绝对值", DatabaseType.PostgreSQL),
		NUMERIC_ACCUM("累加器", DatabaseType.PostgreSQL),
		NUMERIC_ACCUM_INV("累加器", DatabaseType.PostgreSQL),
		NUMERIC_ADD("加法", DatabaseType.PostgreSQL),
		NUMERIC_AVG("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_AVG_ACCUM("累加器", DatabaseType.PostgreSQL),
		NUMERIC_AVG_COMBINE("合并", DatabaseType.PostgreSQL),
		NUMERIC_CMP("比较", DatabaseType.PostgreSQL),
		NUMERIC_COMBINE("合并", DatabaseType.PostgreSQL),
		NUMERIC_DIV("除法", DatabaseType.PostgreSQL),
		NUMERIC_DIV_TRUNC("除法", DatabaseType.PostgreSQL),
		NUMERIC_EQ("相等判断", DatabaseType.PostgreSQL),
		NUMERIC_EXP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_GE("大于等于判断", DatabaseType.PostgreSQL),
		NUMERIC_GT("大于判断", DatabaseType.PostgreSQL),
		NUMERIC_IN("输入转换", DatabaseType.PostgreSQL),
		NUMERIC_INC("输入转换", DatabaseType.PostgreSQL),
		NUMERIC_LARGER("取较大值", DatabaseType.PostgreSQL),
		NUMERIC_LE("小于等于判断", DatabaseType.PostgreSQL),
		NUMERIC_LN("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_LOG("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_LT("小于判断", DatabaseType.PostgreSQL),
		NUMERIC_MOD("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_MUL("乘法", DatabaseType.PostgreSQL),
		NUMERIC_NE("不等判断", DatabaseType.PostgreSQL),
		NUMERIC_OUT("输出转换", DatabaseType.PostgreSQL),
		NUMERIC_POLY_AVG("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_POLY_COMBINE("合并", DatabaseType.PostgreSQL),
		NUMERIC_POLY_STDDEV_POP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_POLY_STDDEV_SAMP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_POLY_SUM("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_POLY_VAR_POP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_POLY_VAR_SAMP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_POWER("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_RECV("二进制接收", DatabaseType.PostgreSQL),
		NUMERIC_SEND("二进制发送", DatabaseType.PostgreSQL),
		NUMERIC_SMALLER("取较小值", DatabaseType.PostgreSQL),
		NUMERIC_SORTSUPPORT("排序支持", DatabaseType.PostgreSQL),
		NUMERIC_SQRT("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_STDDEV_POP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_STDDEV_SAMP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_SUB("减法", DatabaseType.PostgreSQL),
		NUMERIC_SUM("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_SUPPORT("索引支持", DatabaseType.PostgreSQL),
		NUMERIC_UMINUS("一元负值", DatabaseType.PostgreSQL),
		NUMERIC_UPLUS("一元正值", DatabaseType.PostgreSQL),
		NUMERIC_VAR_POP("numeric操作符", DatabaseType.PostgreSQL),
		NUMERIC_VAR_SAMP("numeric操作符", DatabaseType.PostgreSQL),
		PI("圆周率π"),
		POW("幂", "base,exponent"),
		POWER("幂运算", "base,exponent"),
		RADIANS("角度转弧度", "expression"),
		RAND("随机数"),
		RANDOM("随机浮点值"),
		RANDOM_BYTES("一个随机字节向量", "length"),
		RANDOM_INT("随机整数值"),
		RANDOM_NORMAL("正态分布随机值"),
		REMAINDER("余数", "dividend,divisor"),
		ROUND("四舍五入", "expression,precision"),
		ROUND_FUNC("ROUND(GBase)"),
		SCALARGEJOINSEL("标量>=连接率", DatabaseType.PostgreSQL),
		SCALARGESEL("标量>=选择率", DatabaseType.PostgreSQL),
		SCALARGTJOINSEL("标量>连接选择率", DatabaseType.PostgreSQL),
		SCALARGTSEL("标量>选择率", DatabaseType.PostgreSQL),
		SCALARLEJOINSEL("标量<=连接率", DatabaseType.PostgreSQL),
		SCALARLESEL("标量<=选择率", DatabaseType.PostgreSQL),
		SCALARLTJOINSEL("标量<连接选择率", DatabaseType.PostgreSQL),
		SCALARLTSEL("标量<选择率", DatabaseType.PostgreSQL),
		SCALE("小数比例(小数部分中小数位数)"),
		SET_SEED("后续和调用种子", DatabaseType.PostgreSQL),
		SETSEED("设置随机种子", "seed", DatabaseType.PostgreSQL),
		SIGN("符号", "expression"),
		SIN("正弦", "expression"),
		SIND("正弦,以度为单位参数"),
		SINH("双曲正弦", "expression"),
		SQRT("平方根", "expression"),
		SQUARE("平方"),
		TAN("正切", "expression"),
		TAND("切线以度为单位参数"),
		TANH("双曲正切", "expression"),
		TRIM_SCALE("删除尾随零", "expression"),
		TRUNC("截断到指定小数位数", "expression,precision"),
		TRUNC_FUNC("TRUNCATE(GBase)"),
		TRUNCATE("截断数字", "expression,precision"),
		UMINUS("取反", "expression"),
		WIDTH_BUCKET("直方图中桶数或编号", "expression,min,max,buckets")		;

    private final String title;
    private DatabaseType database;
    private final List<String> params = new ArrayList<>();

    MathFunction(String title) {
        this(title, null, null);
    }
    MathFunction(String title, String params) {
        this(title, params, null);
    }
    MathFunction(String title, DatabaseType database) {
        this(title, null, database);
    }
    MathFunction(String title, String params, DatabaseType database) {
        this.title = title;
        this.database = database;
        if (null != params) {
            this.params.addAll(Arrays.asList(params.split(",")));
        }
    }

    public String title() { return title; }
    public Category category() { return Category.MATH; }
    public DatabaseType database() { return database; }
    public List<String> params() { return params; }

    
    
}