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



package org.anyline.data.adapter.init;

import org.anyline.data.param.ConfigStore;
import org.anyline.data.run.*;
import org.anyline.data.runtime.DataRuntime;
import org.anyline.entity.*;
import org.anyline.entity.generator.PrimaryGenerator;
import org.anyline.metadata.*;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.metadata.type.TypeMetadata;

import java.util.List;
import java.util.Map;

/**
 * 所有的非jdbc adapter复制这个源码，在这个基础上修改实现
 */

public abstract class TemplateAdapter extends AbstractDriverAdapter {

    /* *****************************************************************************************************************
     *
     *                                                     common
     *------------------------------------------------------------------------------------------------------------------
     * boolean isBooleanColumn(DataRuntime runtime, Column column)
     * boolean isNumberColumn(DataRuntime runtime, Column column)
     * boolean isCharColumn(DataRuntime runtime, Column column)
     * String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value)
     * String type(String type)
     * String type2class(String type)
     *
     * protected String string(List<String> keys, String key, ResultSet set, String def) throws Exception
     * protected String string(List<String> keys, String key, ResultSet set) throws Exception
     * protected Integer integer(List<String> keys, String key, ResultSet set, Integer def) throws Exception
     * protected Boolean bool(List<String> keys, String key, ResultSet set, Boolean def) throws Exception
     * protected Boolean bool(List<String> keys, String key, ResultSet set, int def) throws Exception
     * protected Object value(List<String> keys, String key, ResultSet set, Object def) throws Exception
     * protected Object value(List<String> keys, String key, ResultSet set) throws Exception
     ******************************************************************************************************************/

    /**
     * 转换成相应数据库类型<br/>
     * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型<br/>
     * 同时解析长度、有效位数、精度<br/>
     * 如有些数据库中用bigint有些数据库中有long
     * @param meta 列
     * @return 具体数据库中对应的数据类型
     */
    @Override
    public TypeMetadata typeMetadata(DataRuntime runtime, Column meta) {
        return super.typeMetadata(runtime, meta);
    }

    /**
     * 转换成相应数据库类型<br/>
     * 把编码时输入的数据类型如(long)转换成具体数据库中对应的数据类型，如有些数据库中用bigint有些数据库中有long
     * @param type 编码时输入的类型
     * @return 具体数据库中对应的数据类型
     */
    @Override
    public TypeMetadata typeMetadata(DataRuntime runtime, String type) {
        return super.typeMetadata(runtime, type);
    }

    /**
     * 检测针对表的主键生成器
     * @param type 数据库类型
     * @param table 表
     * @return PrimaryGenerator
     */
    @Override
    protected PrimaryGenerator checkPrimaryGenerator(DatabaseType type, String table) {
        return super.checkPrimaryGenerator(type, table);
    }

    /**
     * 数据类型拼写兼容
     * @param name name
     * @return spell
     */
    @Override
    public TypeMetadata spell(String name) {
        return super.spell(name);
    }

    /**
     * 合成完整名称
     * @param meta 合成完整名称
     * @return String
     */
    @Override
    public String name(Metadata meta) {
        return super.name(meta);
    }

    /**
     * 构造完整表名
     * @param builder builder
     * @param meta Metadata
     * @return StringBuilder
     */
    @Override
    public StringBuilder name(DataRuntime runtime, StringBuilder builder, Metadata meta) {
        return super.name(runtime, builder, meta);
    }

    /**
     * 拼接完整列名
     * @param builder builder
     * @param meta Column
     * @return StringBuilder
     */
    @Override
    public StringBuilder name(DataRuntime runtime, StringBuilder builder, Column meta) {
        return super.name(runtime, builder, meta);
    }

    /**
     * 拼接界定符
     * @param builder StringBuilder
     * @param src 原文
     * @return StringBuilder
     */
    @Override
    public StringBuilder delimiter(StringBuilder builder, String src) {
        return super.delimiter(builder, src);
    }

    /**
     * 拼接界定符
     * @param builder StringBuilder
     * @param list 原文
     * @return StringBuilder
     */
    @Override
    public StringBuilder delimiter(StringBuilder builder, List<String> list) {
        return super.delimiter(builder, list);
    }

    /**
     * 是否是boolean列
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column 列
     * @return boolean
     */
    @Override
    public boolean isBooleanColumn(DataRuntime runtime, Column column) {
        return super.isBooleanColumn(runtime, column);
    }

    /**
     * 是否同数字
     * @param column 列
     * @return boolean
     */
    @Override
    public boolean isNumberColumn(DataRuntime runtime, Column column) {
        return super.isNumberColumn(runtime, column);
    }

    /**
     * 是否是字符类型
     * 决定值是否需要加单引号
     * number boolean 返回false
     * 其他返回true
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param column 列
     * @return boolean
     */
    @Override
    public boolean isCharColumn(DataRuntime runtime, Column column) {
        return super.isCharColumn(runtime, column);
    }

    /**
     * 内置函数
     * @param column 列属性
     * @param value SQL_BUILD_IN_VALUE
     * @return String
     */
    @Override
    public String value(DataRuntime runtime, Column column, SQL_BUILD_IN_VALUE value) {
        return super.value(runtime, column, value);
    }

    /**
     * 写入数据库前类型转换<br/>
     *
     * @param metadata Column 用来定位数据类型
     * @param placeholder 是否占位符
     * @param value value
     * @return Object
     */
    @Override
    public Object write(DataRuntime runtime, Column metadata, Object value, boolean placeholder) {
        return super.write(runtime, metadata, value, placeholder);
    }

    /**
     * 从数据库中读取数据<br/>
     * 先由子类根据metadata.typeName(CHAR,INT)定位到具体的数据库类型ColumnType<br/>
     * 如果定位成功由CoumnType根据class转换(class可不提供)<br/>
     * 如果没有定位到ColumnType再根据className(String,BigDecimal)定位到JavaType<br/>
     * 如果定位失败或转换失败(返回null)再由父类转换<br/>
     * 如果没有提供metadata和class则根据value.class<br/>
     * 常用类型jdbc可以自动转换直接返回就可以(一般子类DataType返回null父类原样返回)<br/>
     * 不常用的如json/point/polygon/blob等转换成anyline对应的类型<br/>
     *
     * @param metadata Column 用来定位数据类型
     * @param value value
     * @param clazz 目标数据类型(给entity赋值时应该指定属性class, DataRow赋值时可以通过JDBChandler指定class)
     * @return Object
     */
    @Override
    public Object read(DataRuntime runtime, Column metadata, Object value, Class clazz) {
        return super.read(runtime, metadata, value, clazz);
    }

    /**
     * 在不检测数据库结构时才生效,否则会被convert代替
     * 生成value格式 主要确定是否需要单引号  或  类型转换
     * 有些数据库不提供默认的 隐式转换 需要显示的把String转换成相应的数据类型
     * 如 TO_DATE('')
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param builder builder
     * @param obj Object
     * @param key 列名
     */
    @Override
    public void value(DataRuntime runtime, StringBuilder builder, Object obj, String key) {
        super.value(runtime, builder, obj, key);
    }

    /**
     * 参数值 数据类型转换
     * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param table 表
     * @param value  值
     * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
     */
    @Override
    public boolean convert(DataRuntime runtime, Catalog catalog, Schema schema, String table, RunValue value) {
        return super.convert(runtime, catalog, schema, table, value);
    }

    /**
     * 设置参数值,主要根据数据类型格执行式化，如对象,list,map等插入json列
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)
     * @param compare 比较方式 默认 equal 多个值默认 in
     * @param column 列
     * @param value value
     */
    @Override
    public void addRunValue(DataRuntime runtime, Run run, Compare compare, Column column, Object value) {
        super.addRunValue(runtime, run, compare, column, value);
    }

    /**
     * 参数值 数据类型转换
     * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param configs ConfigStore
     * @param run  值
     * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
     */
    @Override
    public boolean convert(DataRuntime runtime, ConfigStore configs, Run run) {
        return super.convert(runtime, configs, run);
    }

    /**
     * 参数值 数据类型转换
     * 子类先解析(有些同名的类型以子类为准)、失败后再调用默认转换
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param table Table
     * @param run  run
     * @return boolean 返回false表示转换失败 如果有多个 adapter 则交给adapter继续转换
     */
    @Override
    public boolean convert(DataRuntime runtime, Table table, Run run) {
        return super.convert(runtime, table, run);
    }

    /**
     * 数据类型转换
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param columns 列
     * @param value 值
     * @return boolean 返回false表示转换失败 如果有多个adapter 则交给adapter继续转换
     */
    @Override
    public boolean convert(DataRuntime runtime, Map<String,Column> columns, RunValue value) {
        return super.convert(runtime, columns, value);
    }

    /**
     * 根据数据库列属性 类型转换(一般是在更新数据库时调用)
     * 子类先解析(有些同名的类型以子类为准)、失败后再到这里解析
     * @param metadata 列
     * @param run 最终待执行的命令和参数(如JDBC环境中的SQL)Value
     * @return boolean 是否完成类型转换,决定下一步是否继续
     */
    @Override
    public boolean convert(DataRuntime runtime, Column metadata, RunValue run) {
        return super.convert(runtime, metadata, run);
    }

    /**
     * 数据类型转换,没有提供column的根据value类型
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param metadata 列
     * @param value 值
     * @return Object
     */
    @Override
    public Object convert(DataRuntime runtime, Column metadata, Object value) {
        return super.convert(runtime, metadata, value);
    }

    /**
     * 数据类型转换,没有提供column的根据value类型
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param meta 数据类型
     * @param value 值
     * @return Object
     */
    @Override
    public Object convert(DataRuntime runtime, TypeMetadata meta, Object value) {
        return super.convert(runtime, meta, value);
    }

    /**
     * 对象名称格式化(大小写转换)，在查询系统表时需要
     * @param runtime 运行环境主要包含驱动适配器 数据源或客户端
     * @param name name
     * @return String
     */
    @Override
    public String objectName(DataRuntime runtime, String name) {
        return super.objectName(runtime, name);
    }

    /**
     *
     * 根据 catalog, schema, name检测tables集合中是否存在
     * @param list metas
     * @param catalog 对于MySQL, 则对应相应的数据库, 对于Oracle来说, 则是对应相应的数据库实例, 可以不填, 也可以直接使用Connection的实例对象中的getCatalog()方法返回的值填充；
     * @param schema 可以理解为数据库的登录名, 而对于Oracle也可以理解成对该数据库操作的所有者的登录名。对于Oracle要特别注意, 其登陆名必须是大写, 不然的话是无法获取到相应的数据, 而MySQL则不做强制要求。
     * @param name name
     * @return 如果存在则返回Table 不存在则返回null
     * @param <T> Table
     */
    @Override
    public <T extends Metadata> T search(List<T> list, Catalog catalog, Schema schema, String name) {
        return super.search(list, catalog, schema, name);
    }

}