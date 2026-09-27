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
import org.anyline.data.adapter.function.SystemFunctionConverter;
import org.anyline.data.adapter.function.SystemFunctionFactory;
import org.anyline.log.Log;
import org.anyline.log.LogProxy;
import org.anyline.metadata.*;
import org.anyline.data.metadata.function.DateTimeFunction;
import org.anyline.data.metadata.function.TypeCastFunction;
import org.anyline.metadata.type.DatabaseType;
import org.anyline.data.jdbc.function.FunctionCall;
import org.anyline.data.jdbc.function.Parameter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 跨数据库函数转换器
 * <p>
 * 将源数据库 SQL 中的系统函数转换为目标数据库的等价形式，
 * 包括函数名转换、参数顺序调整、日期格式转换。
 * <p>
 * 查找策略（四步）：
 * <ol>
 *   <li>META.supportedBy(target) → 不支持则抛异常</li>
 *   <li>META lookup → 在目标库查找同 META 的函数映射</li>
 *   <li>NAME lookup → META 未命中则按函数名查找</li>
 *   <li>兜底 → 仍未找到则原样输出（未知函数透传）</li>
 * </ol>
 */

public class DefaultFunctionConverter implements SystemFunctionConverter {
    private static final Log log = LogProxy.get(DefaultFunctionConverter.class);


    /**
     * 转换 SQL 文本中的函数调用（入口方法）
     * <p>
     * 从后往前解析替换，避免字符串位置偏移导致替换错误
     * @param origin 源数据库类型
     * @param target 目标数据库类型
     * @param cmd    原始 SQL 文本
     * @return 转换后的 SQL 文本
     */
    @Override
    public String convert(DatabaseType origin, DatabaseType target, String cmd) {
        List<FunctionCall> calls = SystemFunctionParser.parse(cmd);
        int size = calls.size();
        for (int i = size-1; i >= 0; i--) {
            FunctionCall call = calls.get(i);
            String convert = convert(origin, target, call);
            cmd = replace(cmd, call.getStart(), call.getEnd(), convert);
        }
        return cmd;
    }
    /**
     * 转换表元数据中列默认值里包含的函数调用
     * @param origin 源数据库类型
     * @param target 目标数据库类型
     * @param metadata 表/列等元数据对象
     */
    @Override
    public void convert(DatabaseType origin, DatabaseType target, Metadata metadata) {
        if(null == metadata){
            return;
        }
        if(origin == null || target == null || origin == target){
            return;
        }
        if(metadata instanceof Table){
            Table table = (Table) metadata;
            LinkedHashMap<String, Column> columns = table.getColumns();
            for(Column column : columns.values()){
                Object def = column.getDefaultValue();
                if(def instanceof String){
                   String str = (String)def;
                   str = convert(origin, target, str);
                   column.setDefaultValue(str);
                }
            }
        }
    }

    /**
     * 把origin的begin至end部分替换成replace
     * @param origin 原文
     * @param begin 开始位置
     * @param end 结束位置
     * @param replace 替换成 replace
     * @return String
     */
    public String replace(String origin, int begin, int end, String replace) {
        if(begin == 0 && end == origin.length()){
            return replace;
        }
        return origin.substring(0, begin) + replace + origin.substring(end);
    }

    /**
     * 转换单个函数调用（核心方法）
     * <p>
     * 五步查找策略：
     * <ol>
     *   <li>META.supportedBy(target) → 不支持则抛出 UnsupportedOperationException</li>
     *   <li>META lookup → 在目标库查找同 META 的函数映射</li>
     *   <li>NAME lookup → META 未命中则按函数名查找目标库</li>
     *   <li>NAME 命中且 supported=false → 抛异常（目标库明确标记不支持）</li>
     *   <li>兜底 → 仍未找到则原样输出</li>
     * </ol>
     * 找到目标函数后执行：递归转换嵌套参数、日期格式转换、参数顺序重排、模板替换
     *
     * @param origin 源数据库类型
     * @param target 目标数据库类型
     * @param call   解析后的函数调用对象
     * @return 转换后的函数调用文本
     */
    public String convert(DatabaseType origin, DatabaseType target, FunctionCall call) {
        String cmd = call.getText();
        String name = call.getName();
        //原函数
        SystemFunction function = SystemFunctionFactory.function(origin, name);
        SystemFunction fun = function;
        if(null != function) {
            SystemFunction.META meta = function.meta();

            // 1. 跨库 META：通过 META 查找目标数据库对应的函数
            fun = SystemFunctionFactory.function(target, meta);
            if(null == fun) {
                // 2. META 没找到，通过名称（SQL中写的函数名）查找
                fun = SystemFunctionFactory.function(target, name);
            }
            if(null != fun && !fun.support()) {
                // 3. 函数标记 unsurported → 目标数据库明确不支持
                throw new UnsupportedOperationException(
                    "Function '" + name + "' (META: " + meta.name()
                    + ") is not supported in target database " + target);
            }
            if(null == fun) {
                // 4. 还是没找到 → 原样输出不做转换
                fun = function;
            }
        }
        
        List<Parameter> parameters = call.getParameters();
        List<String> params = new ArrayList<>();
        
        // 获取目标函数的 formula 模板（带 ${} 占位符的调用格式）
        String formulaTemplate = fun != null ? fun.formula() : null;
        
        // 处理所有参数：递归转换嵌套函数 + 日期格式化转换
        for (Parameter parameter : parameters) {
            String text = parameter.getText();
            if (parameter.isFunction()) {
                // 递归转换嵌套函数
                String converted = text != null ? convert(origin, target, text) : null;
                params.add(converted != null ? converted : "");
            } else {
                // 日期或时间格式化处理：基于源函数判断是否需要转换
                // 扩展触发条件：DATE_FORMAT, TIME_FORMAT, TO_CHAR_DATE（Oracle的日期TO_CHAR）,
                // TO_DATE, STR_TO_DATE, TO_TIMESTAMP 等涉及日期格式的函数
                boolean isDateFormatFunction = function != null && (
                    function.meta() == DateTimeFunction.DATE_FORMAT
                    || function.meta() == DateTimeFunction.TIME_FORMAT
                    || function.meta() == TypeCastFunction.TO_CHAR
                    || function.meta() == TypeCastFunction.TO_DATE
                    || function.meta() == DateTimeFunction.STR_TO_DATE
                    || function.meta() == TypeCastFunction.TO_TIMESTAMP
                );
                if (isDateFormatFunction) {
                    // PG(YYYY-MM-DD HH24:MI:SS) > MYSQL(%Y-%m-%d %H:%i:%s)
                    text = format(origin, target, text);
                }
                params.add(text != null ? text : "");
            }
        }
        
        // 参数顺序转换处理
        List<String> sourceOrder = function != null ? function.orders() : null;
        List<String> targetOrder = fun != null ? fun.orders() : null;
        
        // 如果源函数未找到，记录警告日志
        if (function == null && log.isWarnEnabled()) {
            log.warn("SystemFunctionConverter: source function not found for '" + name + "' in " + origin);
        }
        
        // 如果源和目标都有参数顺序定义，且顺序不同，则进行参数重排
        if (sourceOrder != null && targetOrder != null && !sourceOrder.equals(targetOrder)) {
            // 参数重排前记录日志，用于排查参数名不对齐问题
            if (log.isDebugEnabled()) {
                log.debug("SystemFunctionConverter: reordering params for '" + name 
                    + "' from " + origin + ":" + sourceOrder + " to " + target + ":" + targetOrder);
            }
            // 验证参数名是否能对齐
            java.util.Set<String> sourceNames = new java.util.LinkedHashSet<>(sourceOrder);
            java.util.Set<String> targetNames = new java.util.LinkedHashSet<>(targetOrder);
            if (!sourceNames.equals(targetNames) && log.isWarnEnabled()) {
                java.util.Set<String> onlySource = new java.util.LinkedHashSet<>(sourceNames);
                onlySource.removeAll(targetNames);
                java.util.Set<String> onlyTarget = new java.util.LinkedHashSet<>(targetNames);
                onlyTarget.removeAll(sourceNames);
                log.warn("SystemFunctionConverter: parameter name mismatch for '" + name 
                    + "' (META: " + function.meta() + ")"
                    + " - source only: " + onlySource + ", target only: " + onlyTarget
                    + ". Some parameters may be lost during conversion.");
            }
            params = fun.reorderParams(sourceOrder, params);
        }
        
        // 如果目标函数有参数顺序定义但源函数没有，记录警告日志
        if (function == null && targetOrder != null && !targetOrder.isEmpty() && log.isWarnEnabled()) {
            log.warn("SystemFunctionConverter: cannot reorder params for '" + name + "' because source function not found");
        }

        // 直接用计算好的params替换formula模板中的占位符（formulaTemplate已在前面定义）
        if (formulaTemplate != null && formulaTemplate.contains("${")) {
            // 提取占位符（按出现顺序）
            List<String> placeholders = SystemFunction.extractPlaceholders(formulaTemplate);
            
            // 用计算好的params替换占位符
            int replaceCount = Math.min(placeholders.size(), params.size());
            for (int i = 0; i < replaceCount; i++) {
                String placeholder = "${" + placeholders.get(i) + "}";
                formulaTemplate = formulaTemplate.replace(placeholder, params.get(i));
            }
            
            // 检查是否所有占位符都被替换（检测参数丢失）
            if (log.isWarnEnabled() && params.size() < placeholders.size()) {
                List<String> missingPlaceholders = new ArrayList<>();
                for (int i = params.size(); i < placeholders.size(); i++) {
                    missingPlaceholders.add(placeholders.get(i));
                }
                log.warn("SystemFunctionConverter: some placeholders were not replaced for '" + name 
                    + "' - missing params for: " + missingPlaceholders 
                    + ". Template may contain literal ${...} in output.");
            }
            
            // 如果有额外的参数（可变参数），追加到末尾
            if (params.size() > placeholders.size()) {
                // 找到最后一个占位符的位置，在其后添加额外参数
                int lastPlaceholderEnd = formulaTemplate.lastIndexOf(")");
                if (lastPlaceholderEnd > 0) {
                    StringBuilder extraParams = new StringBuilder();
                    for (int i = placeholders.size(); i < params.size(); i++) {
                        if (extraParams.length() > 0) {
                            extraParams.append(", ");
                        }
                        extraParams.append(params.get(i));
                    }
                    // 在最后一个占位符后、括号前插入额外参数
                    String before = formulaTemplate.substring(0, lastPlaceholderEnd);
                    String after = formulaTemplate.substring(lastPlaceholderEnd);
                    // 如果已有参数，添加逗号分隔
                    if (!before.trim().endsWith("(")) {
                        before += ", ";
                    }
                    formulaTemplate = before + extraParams + after;
                }
            }
            
            cmd = formulaTemplate;
        } else {
            // 没有占位符，使用默认格式 define(param1, param2, ...)
            if (fun != null) {
                cmd = fun.title() + "(" + String.join(",", params) + ")";
            } else {
                cmd = call.getText();
            }
        }
        return cmd;
    }
    
    /**
     * 日期格式字符串转换（跨数据库格式模式互转）
     * <p>
     * 使用两阶段替换策略避免交叉污染：
     * 1. 先将所有源模式替换为唯一的临时标记
     * 2. 再将临时标记替换为目标模式
     * <p>
     * 例如 SQL Server 'yyyy-MM-dd' → MySQL:
     *   阶段1: 'yyyy-MM-dd' → '\0FMT0\0-\0FMT1\0-\0FMT2\0'
     *   阶段2: '\0FMT0\0-\0FMT1\0-\0FMT2\0' → '%Y-%m-%d'
     * <p>
     * 这避免了 String.replace() 全局替换导致的交叉污染问题
     * （如 SQL Server 的 d 模式替换会错误地影响 dd 模式的替换结果）
     */
    public String format(DatabaseType origin, DatabaseType target, String format){
        if(null == format){
            return format;
        }
        Map<String, DateFormatPattern> origins = DateFormatPatternFactory.names(origin);
        Map<DateFormatPattern.META, DateFormatPattern> targets = DateFormatPatternFactory.metas(target);
        
        if (origins == null || origins.isEmpty() || targets == null || targets.isEmpty()) {
            return format;
        }
        
        // 按模式长度降序排序，确保长模式优先匹配
        // 例如：yyyy(4字符) 必须在 yy(2字符) 之前处理
        List<DateFormatPattern> sortedPatterns = origins.values().stream()
                .sorted(Comparator.comparingInt(p -> -p.define().length()))
                .collect(Collectors.toList());
        
        // === 阶段1: 用临时唯一标记替换所有源模式 ===
        // 使用不可见字符作为标记分隔符，避免与格式字符串中的正常文本冲突
        Map<String, String> markerToTarget = new LinkedHashMap<>();
        int index = 0;
        
        for(DateFormatPattern pattern: sortedPatterns){
            String define = pattern.define();
            DateFormatPattern tar = targets.get(pattern.meta());
            if(null != tar){
                // 生成唯一标记：\0 + 类型前缀 + 序号 + \0
                // \0 (null char) 不会出现在正常的日期格式字符串中
                String marker = "\0FMT" + index + "\0";
                format = format.replace(define, marker);
                markerToTarget.put(marker, tar.define());
                index++;
            }
        }
        
        // === 阶段2: 将临时标记替换为目标模式 ===
        for(Map.Entry<String, String> entry : markerToTarget.entrySet()){
            format = format.replace(entry.getKey(), entry.getValue());
        }
        
        return format;
    }
}