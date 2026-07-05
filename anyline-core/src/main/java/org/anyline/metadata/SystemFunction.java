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


package org.anyline.metadata;

import org.anyline.metadata.type.DatabaseType;

import java.util.List;


public interface SystemFunction {

        interface META {
                enum Category {
                        /** NULL值判断、替换、合并等跨数据库差异最大的痛点 */
                        NULL_HANDLE("NULL处理"),

                        /** TO_CHAR/TO_NUMBER/CAST/CONVERT等各DB命名和语法不同 */
                        TYPE_CAST("类型转换"),

                        /** 截取、查找、连接、填充、比较等日常最高频操作 */
                        STRING("字符串处理"),

                        /** 日期加减、格式化、提取、时区等跨DB差异最大的领域 */
                        DATE_TIME("日期时间"),

                        /** IF/CASE/NVL/GREATEST/LEAST等条件与选择逻辑 */
                        CONDITIONAL("条件逻辑"),

                        /** 序列值获取、自增ID等各DB实现完全不同 */
                        SEQUENCE("序列自增"),

                        /** UUID生成与转换，各DB名完全不同 */
                        UUID("UUID生成"),

                        /** AES/SHA/MD5等加密与哈希，各DB实现库不同 */
                        CRYPTO("加密哈希"),

                        /** 正则匹配/替换/提取，语法差异大 */
                        REGEX("正则表达式"),

                        /** 统计聚合与窗口，核心分析能力 */
                        AGGREGATE("聚合与窗口"),

                        /** ST_*空间，各DB扩展命名差异极大 */
                        SPATIAL("空间几何"),

                        /** JSON解析/提取/构造，近年新增差异最大 */
                        JSON("JSON处理"),

                        /** XML解析/查询/构造，各DB实现不同 */
                        XML("XML处理"),

                        /** 基础/高级数学，度数三角和双曲跨DB差异大 */
                        MATH("数学运算"),

                        /** 命名锁/咨询锁，各DB锁机制完全不同 */
                        LOCK("锁与并发"),

                        /** IS_TRUE/IS_NULL等布尔判断 */
                        BOOLEAN("布尔判断"),

                        /** 按位与或异或，聚合与标量语义不同 */
                        BIT_OP("位运算"),

                        /** 数组构造/查询/变换，PG独有类型 */
                        ARRAY("数组操作"),

                        /** IP地址解析/计算/格式化 */
                        NETWORK("网络IP"),

                        /** 国产数据库标签安全策略，达梦/金仓/神通/瀚高间需兼容 */
                        LABEL_SECURITY("标签安全"),

                        /** 版本/连接/配置/存储等系统管理 */
                        SYSTEM("系统管理"),

                        /** 流复制/组复制/复制槽等，PG和MySQL各有独有实现 */
                        REPLICATION("复制管理"),

                        /** 对象权限检查，PG信息模式独有 */
                        PRIVILEGE("权限检查"),

                        /** 全文检索引擎，PG独有tsvector/tsquery */
                        FULLTEXT("全文搜索"),

                        /** 预测/聚类/特征/统计检验，Oracle独有 */
                        DATA_MINING("数据挖掘"),

                        /** 数据压缩与解压 */
                        COMPRESS("压缩处理"),

                        /** 排序规则/字符编码/Unicode处理 */
                        ENCODING("编码处理"),

                        /** 范围类型操作，PG独有 */
                        RANGE("范围类型"),

                        /** 系统目录查询/DDL重建/元数据解析 */
                        CATALOG("系统目录"),

                        /** 未归类的杂项 */
                        OTHER("其他");

                        private final String title;

                        Category(String title) {
                                this.title = title;
                        }

                        public String title() {
                                return title;
                        }
                }
                /**
                 * 标准名称
                 * @return String
                 */
                String name();
                /**
                 * 功能说明
                 * @return String
                 */
                String title();

                /**
                 * 类别
                 * @return Category
                 */
                Category category();
                /**
                 * 数据库标记
                 * @return DatabaseType
                 */
                DatabaseType database();
                /**
                 * 定义可用的参数列表
                 * @return List
                 */
                List<String> params();

        }
        /**
         * 数据库类型
         * @return DatabaseType
         */
        DatabaseType database();

        /**
         * 实参,注意参数有可能是个
         * @return list
         */
        List<String> params();
        void params(List<String> params);

        /**
         * 当前数据库是否支持此
         * @return true=支持(默认); false=不支持
         */
        default boolean support() {
                return true;
        }
        void support(boolean support);

        /**
         * 获取本数据库中的参数顺序（语义名称列表）
         * <p>
         * 例如：SUBSTRING(str, pos, len) 返回 ["string", "position", "length"]
         * <p>
         * 各数据库实现可覆盖此方法以指定本数据库的参数顺序
         * <p>
         * 默认实现会从 formulaDefine() 中自动提取参数顺序
         * @return 参数名称列表，如果返回null则表示参数顺序与标准顺序一致无需重排
         */
        default List<String> orders(){
                // 从 formulaDefine() 中自动提取参数顺序
                String formula = formula();
                if (formula != null && formula.contains("${")) {
                        return extractPlaceholders(formula);
                }
                return null;
        };

        /**
         * 根据目标数据库的参数顺序重新排序参数
         * <p>
         * 将源顺序的参数列表转换为目标数据库需要的顺序
         * @param sourceOrder 源数据库的参数顺序定义（语义名称列表）
         * @param params 要重排的参数列表（按源数据库顺序排列）
         * @return 重排后的参数列表（按本数据库顺序排列）
         */
        default List<String> reorderParams(List<String> sourceOrder, List<String> params){
                if (null == sourceOrder || null == params || params.isEmpty()) {
                        return params;
                }
                List<String> targetOrder = this.orders();
                if (null == targetOrder || targetOrder.isEmpty()) {
                        return params;
                }
                // 如果顺序相同，直接返回
                if (sourceOrder.equals(targetOrder)) {
                        return params;
                }
                // 构建参数索引映射
                java.util.Map<String, String> paramMap = new java.util.LinkedHashMap<>();
                for (int i = 0; i < sourceOrder.size() && i < params.size(); i++) {
                        paramMap.put(sourceOrder.get(i), params.get(i));
                }
                // 按目标顺序重新排列参数
                List<String> result = new java.util.ArrayList<>();
                for (String key : targetOrder) {
                        String param = paramMap.get(key);
                        if (param != null) {
                                result.add(param);
                        }
                }
                // 添加未映射的额外参数
                for (int i = sourceOrder.size(); i < params.size(); i++) {
                        result.add(params.get(i));
                }
                return result;
        };

        /**
         * 转换后格式 包含参数
         * <p>
         * 如果 formulaDefine 包含 ${paramName} 占位符，则替换为实际参数值
         * 例如：POSITION(${substring} IN ${string}) 替换为 POSITION('B' IN 'ABC')
         * 占位符名称按出现顺序自动提取，无需单独定义 paramOrder
         * <p>
         * 只有在 META.params() 中定义的参数名称才能使用
         * <p>
         * 如果 formulaDefine 返回 null，则使用默认格式 define(param1, param2, ...)
         * @return 转换后格式
         */
        default String parse() {
                List<String> params = params();

                // 如果 formulaDefine 包含 ${} 占位符，进行参数替换
                String formulaDef = formula();
                if (formulaDef != null && formulaDef.contains("${")) {
                        String result = formulaDef;
                        if (null != params) {
                                // 从 formulaDefine 中提取所有占位符名称（按出现顺序）
                                java.util.List<String> placeholders = extractPlaceholders(formulaDef);
                                // 验证参数名称是否在 META.params() 中定义
                                META meta = meta();
                                for (String placeholder : placeholders) {
                                        if (!meta.params().contains(placeholder)) {
                                                // 参数名称未在 META 中定义，使用默认格式
                                                return parse(params);
                                        }
                                }
                                for (int i = 0; i < placeholders.size() && i < params.size(); i++) {
                                        String placeholder = "${" + placeholders.get(i) + "}";
                                        result = result.replace(placeholder, params.get(i));
                                }
                        }
                        return result;
                }

                // 默认格式：define(param1, param2, ...)
                return parse(params);
        }

        /**
         * 默认格式：define(param1, param2, ...)
         * @param params 参数列表
         * @return 格式化字符串
         */
        default String parse(List<String> params) {
                StringBuilder formula = new StringBuilder();
                formula.append(title()).append("(");
                if(null != params) {
                        boolean first = true;
                        for(String param : params) {
                                if(!first) {
                                        formula.append(",");
                                }
                                first = false;
                                formula.append(param);
                        }
                }
                formula.append(")");
                return formula.toString();
        }

        /**
         * 从 formula 中提取所有 ${paramName} 占位符名称（按出现顺序）
         * @param formula 定义字符串
         * @return 占位符名称列表
         */
        static java.util.List<String> extractPlaceholders(String formula) {
                java.util.List<String> placeholders = new java.util.ArrayList<>();
                if (formula == null) {
                        return placeholders;
                }
                // 匹配 ${paramName} 格式的占位符
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\\$\\{(\\w+)\\}");
                java.util.regex.Matcher matcher = pattern.matcher(formula);
                while (matcher.find()) {
                        placeholders.add(matcher.group(1));
                }
                return placeholders;
        }

        META meta();

        /**
         * 名 不包含参数
         * @return String
         */
        String title();

        /**
         * 带参数占位符的完整格式
         * <p>
         * 例如：POSITION(${substring} IN ${string})
         * 如果返回 null，则使用默认格式 title(param1, param2, ...)
         * @return 带占位符的格式字符串
         */
        default String formula() {
            return null;
        }
}