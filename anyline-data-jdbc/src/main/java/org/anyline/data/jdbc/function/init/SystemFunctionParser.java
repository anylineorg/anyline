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



import org.anyline.data.jdbc.function.FunctionCall;
import org.anyline.data.jdbc.function.Parameter;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * SQL 函数调用解析器
 * <p>
 * 解析 SQL 文本中的函数调用，识别函数名、参数列表、嵌套关系。
 * <p>
 * 支持：
 * <ul>
 *   <li>普通逗号分隔参数：FUNC(a, b, c)</li>
 *   <li>IN 特殊语法：POSITION(substr IN str)</li>
 *   <li>嵌套函数：UPPER(LOCATE('a', col))</li>
 *   <li>字符串中括号转义：FUNC('a(b)c')</li>
 * </ul>
 */

public class SystemFunctionParser {

    /**
     * 解析 SQL 文本中的所有函数调用（入口方法）
     * @param sql SQL 语句
     * @return 解析出的函数调用列表（嵌套函数通过 getCalls() 获取）
     */
    public static List<FunctionCall> parse(String sql) {
        List<FunctionCall> results = new ArrayList<>();
        parseFunctions(sql, 0, results);
        return results;
    }

    /**
     * 递归解析 SQL 片段中的函数调用
     * <p>
     * 使用正则匹配函数名(字母开头+字母数字下划线)后跟(的模式，
     * 然后匹配括号到平衡位置，提取完整函数调用文本和参数列表
     * <p>
     * 字符串识别：单引号'、双引号"、反引号`内的括号不计入括号计数
     *
     * @param sql         待解析的 SQL 片段
     * @param startOffset 在原始 SQL 中的起始偏移量
     * @param results     解析结果收集列表
     */
    private static void parseFunctions(String sql, int startOffset, List<FunctionCall> results) {
        // 匹配函数名模式：字母开头，包含字母、数字、下划线
        Pattern functionPattern = Pattern.compile("\\b([A-Za-z_][A-Za-z0-9_]*)\\s*\\(");
        Matcher matcher = functionPattern.matcher(sql);

        while (matcher.find()) {
            int functionStart = matcher.start(1);
            String functionName = matcher.group(1);

            // 跳过 DDL 表定义区的标识符（CREATE TABLE name(...)、INSERT INTO name(...) 等）
            // 无需递归：外层 matcher 继续扫描完整 SQL 时会自然遇到括号内的函数调用
            // （如 DEFAULT SYSDATE() 的前导词是 DEFAULT 不是 TABLE，不会被拦截）
            if (isInTableDefinition(sql, functionStart)) {
                continue;
            }

            // 从函数名后的左括号开始解析参数
            int paramStart = matcher.end();
            int bracketCount = 1;
            int i = paramStart;
            boolean inString = false;
            char stringDelimiter = '\0';

            while (i < sql.length() && bracketCount > 0) {
                char c = sql.charAt(i);

                // 处理字符串和反引号界定符
                if (!inString && (c == '\'' || c == '"')) {
                    inString = true;
                    stringDelimiter = c;
                } else if (!inString && c == '`') {
                    inString = true;
                    stringDelimiter = '`';
                } else if (inString && c == stringDelimiter) {
                    // 检查是否是转义的引号（反引号不需要转义）
                    if (stringDelimiter != '`' && i > 0 && sql.charAt(i-1) == '\\') {
                        // 转义引号，继续在字符串中
                    } else {
                        inString = false;
                    }
                }

                // 只有在不在字符串中时才计数括号
                if (!inString) {
                    if (c == '(') {
                        bracketCount++;
                    } else if (c == ')') {
                        bracketCount--;
                    }
                }

                i++;

                // 如果括号不匹配，跳出循环
                if (bracketCount == 0) {
                    break;
                }
            }

            if (bracketCount == 0) {
                int functionEnd = i;
                String fullFunction = sql.substring(functionStart, functionEnd);

                FunctionCall result = new FunctionCall(
                        functionName, fullFunction,
                        functionStart + startOffset, functionEnd + startOffset
                );

                // 解析参数
                String paramsText = sql.substring(paramStart, functionEnd - 1);
                parseParameters(paramsText, paramStart + startOffset, result);

                results.add(result);

                // 在参数中递归查找嵌套函数
                for (Parameter param : result.getParameters()) {
                    if (param.isFunction()) {
                        parseFunctions(
                                param.getText(),
                                param.getStartIndex(),
                                result.getCalls()
                        );
                    }
                }
            }
        }
    }

    /**
     * 判断 identifier( 是否处于 DDL 表定义子句中（表名位置，非函数调用）。
     * <p>
     * 不是穷举"前一个词是什么"，而是识别前文是否构成了
     * SQL 表定义子句的完整句式结构：
     * <pre>
     *   {CREATE|ALTER|DROP} TABLE [IF NOT EXISTS] &lt;name&gt; (
     *   INSERT [INTO] &lt;name&gt; (
     *   MERGE [INTO] &lt;name&gt; (
     * </pre>
     * 这些是 SQL 语法中固定的表定义句式，不是随意罗列的关键字列表。
     * <p>
     * 同时兼顾 SQL 数据类型尺寸语法（VARCHAR(255)、NUMERIC(10,2) 等），
     * 避免将其误解析为函数调用。
     *
     * @param sql       完整 SQL 文本
     * @param nameStart 标识符在 sql 中的起始位置
     * @return true 表示这是表名或类型名，应跳过函数解析
     */
    private static boolean isInTableDefinition(String sql, int nameStart) {
        String word = readPrevWord(sql, nameStart);
        if (word == null) {
            return false;
        }
        switch (word) {
            // 模式: {CREATE|ALTER|DROP} TABLE name(
            case "TABLE":
                return hasClauseStart(sql, nameStart, "TABLE");

            // 模式: INSERT INTO name(  （INTO 后面还有词则可能是 value 表达式，这里只处理目标表位置）
            case "INTO":
                return hasClauseStart(sql, nameStart, "INTO");

            // 模式: ... TABLE IF NOT EXISTS name(
            case "EXISTS":
                return hasClauseStart(sql, nameStart, "EXISTS");

            // 模式: UPDATE name(  （部分数据库 INSERT INTO 的别名写法）
            // UPDATE 极少跟圆括号表名，保留以防某些方言使用
            default:
                break;
        }
        return false;
    }

    /**
     * 读取 nameStart 之前的第一个词（跳过空白，反向读取连续字母数字及下划线）
     * @return 大写的词，如果前面没有词返回 null
     */
    private static String readPrevWord(String sql, int nameStart) {
        int i = nameStart - 1;
        while (i >= 0 && Character.isWhitespace(sql.charAt(i))) {
            i--;
        }
        if (i < 0 || !isWordChar(sql.charAt(i))) {
            return null;
        }
        int wordEnd = i;
        while (i >= 0 && isWordChar(sql.charAt(i))) {
            i--;
        }
        return sql.substring(i + 1, wordEnd + 1).toUpperCase();
    }

    /**
     * 从 nameStart 往回验证表定义子句的起始关键字是否匹配。
     * 三种句式结构：
     * <pre>
     *   {CREATE|ALTER|DROP|RENAME|TRUNCATE} TABLE name(
     *   {INSERT|MERGE} INTO name(
     *   {CREATE|ALTER|DROP} TABLE IF NOT EXISTS name(
     * </pre>
     */
    private static boolean hasClauseStart(String sql, int nameStart, String preceding) {
        String upper = sql.toUpperCase();
        int pos = skipWhitespace(sql, nameStart - 1);

        switch (preceding) {
            case "TABLE":
                // {CREATE|ALTER|DROP|RENAME|TRUNCATE} TABLE name(
                return matchBefore(upper, pos, "TABLE", "CREATE", "ALTER", "DROP", "RENAME", "TRUNCATE");

            case "INTO":
                // {INSERT|MERGE} INTO name(
                return matchBefore(upper, pos, "INTO", "INSERT", "MERGE");

            case "EXISTS":
                // {CREATE|ALTER|DROP} TABLE IF NOT EXISTS name(
                pos = skipWords(sql, pos, "EXISTS", "NOT", "IF", "TABLE");
                if (pos < 0) return false;
                return matchBefore(upper, pos, null, "CREATE", "ALTER", "DROP");
        }
        return false;
    }

    /**
     * 跳过 skipWord 这个词后，检查前一个词是否在候选集中。
     * @param skipWord 需要跳过的词（null 表示不需要跳过）
     */
    private static boolean matchBefore(String upper, int pos, String skipWord, String... candidates) {
        if (skipWord != null) {
            pos = skipWord(upper, pos);
            if (pos < 0) return false;
            pos = skipWhitespace(upper, pos);
        }
        String kw = readWordAt(upper, pos);
        if (kw == null) return false;
        for (String c : candidates) {
            if (c.equals(kw)) return true;
        }
        return false;
    }

    /** 从 wordEnd 往前跳过一个词，返回该词起始位置-1（即前一词的结束位置） */
    private static int skipWord(String sql, int wordEnd) {
        int i = wordEnd;
        while (i >= 0 && isWordChar(sql.charAt(i))) i--;
        return i;
    }

    /** 依次跳过多个词，返回最后一个词之前的结束位置 */
    private static int skipWords(String sql, int pos, String... words) {
        for (int i = 0; i < words.length; i++) {
            pos = skipWord(sql, pos);
            if (pos < 0) return -1;
            pos = skipWhitespace(sql, pos);
        }
        return pos;
    }

    /** 从 pos 往前跳过空白 */
    private static int skipWhitespace(String sql, int pos) {
        while (pos >= 0 && Character.isWhitespace(sql.charAt(pos))) pos--;
        return pos;
    }

    /** 从 pos 往前读一个词（不移动位置），返回大写形式 */
    private static String readWordAt(String upper, int pos) {
        if (pos < 0) return null;
        int end = pos;
        while (end >= 0 && isWordChar(upper.charAt(end))) end--;
        // end is now at one char BEFORE the word, or -1
        return upper.substring(end + 1, pos + 1);
    }

    private static boolean isWordChar(char c) {
        return Character.isLetterOrDigit(c) || c == '_';
    }

    /**
     * 解析函数参数
     */
    private static void parseParameters(String paramsText, int paramsStartOffset, FunctionCall functionResult) {
        if (paramsText.trim().isEmpty()) {
            return;
        }

        // 检查是否包含特殊语法（如 " IN "）
        String[] inParts = splitByIN(paramsText);
        if (inParts != null) {
            // 找到 IN 关键字，按 IN 分割参数
            int currentStart = 0;
            for (String part : inParts) {
                String paramStr = part.trim();
                if (!paramStr.isEmpty()) {
                    int paramStart = currentStart;
                    int paramEnd = currentStart + paramStr.length();

                    // 检查参数是否是函数
                    boolean isFunc = isFunctionParameter(paramStr);

                    Parameter param = new Parameter(
                            paramStr,
                            paramsStartOffset + paramStart,
                            paramsStartOffset + paramEnd,
                            isFunc
                    );

                    functionResult.getParameters().add(param);
                }
                currentStart += part.length() + 4; // +4 for " IN "
            }
            return;
        }

        // 普通参数按逗号分隔
        List<Integer> commaPositions = findParameterCommas(paramsText);
        List<String> paramStrings = splitParameters(paramsText, commaPositions);

        int currentStart = 0;
        for (int i = 0; i < paramStrings.size(); i++) {
            String paramStr = paramStrings.get(i).trim();
            if (!paramStr.isEmpty()) {
                int paramStart = currentStart;
                int paramEnd = currentStart + paramStr.length();

                // 检查参数是否是函数
                boolean isFunc = isFunctionParameter(paramStr);

                Parameter param = new Parameter(
                        paramStr,
                        paramsStartOffset + paramStart,
                        paramsStartOffset + paramEnd,
                        isFunc
                );

                functionResult.getParameters().add(param);
            }
            currentStart += paramStrings.get(i).length() + 1; // +1 for comma
        }
    }

    /**
     * 解析包含 IN 关键字的参数语法（如 POSITION(substring IN string)）
     * @param text 参数文本
     * @return 分割后的两个参数，如果格式不对返回 null
     */
    private static String[] splitByIN(String text) {
        if (text == null) return null;

        int bracketCount = 0;
        boolean inString = false;
        char stringDelimiter = '\0';

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 处理字符串和反引号界定符
            if (!inString && (c == '\'' || c == '"')) {
                inString = true;
                stringDelimiter = c;
            } else if (!inString && c == '`') {
                inString = true;
                stringDelimiter = '`';
            } else if (inString && c == stringDelimiter) {
                // 检查是否是转义的引号（反引号不需要转义）
                if (stringDelimiter != '`' && i > 0 && text.charAt(i-1) == '\\') {
                    // 转义引号，继续在字符串中
                } else {
                    inString = false;
                }
            }

            // 只有在不在字符串中且括号平衡时才识别 IN
            if (!inString && bracketCount == 0) {
                // 检查是否是 " IN "（两边有空格）
                if (c == ' ' && i + 2 < text.length() &&
                    text.substring(i+1, i+3).equals("IN") &&
                    i + 3 < text.length() && text.charAt(i+3) == ' ') {
                    String before = text.substring(0, i).trim();
                    String after = text.substring(i + 4).trim();
                    if (!before.isEmpty() && !after.isEmpty()) {
                        return new String[]{before, after};
                    }
                }
            }

            if (!inString) {
                if (c == '(') {
                    bracketCount++;
                } else if (c == ')') {
                    bracketCount--;
                }
            }
        }

        return null;
    }

    /**
     * 查找参数分隔逗号的位置
     */
    private static List<Integer> findParameterCommas(String text) {
        List<Integer> commas = new ArrayList<>();
        int bracketCount = 0;
        boolean inString = false;
        char stringDelimiter = '\0';

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 处理字符串和反引号界定符
            if (!inString && (c == '\'' || c == '"')) {
                inString = true;
                stringDelimiter = c;
            } else if (!inString && c == '`') {
                inString = true;
                stringDelimiter = '`';
            } else if (inString && c == stringDelimiter) {
                // 检查是否是转义的引号（反引号不需要转义）
                if (stringDelimiter != '`' && i > 0 && text.charAt(i-1) == '\\') {
                    // 转义引号，继续在字符串中
                } else {
                    inString = false;
                }
            }

            // 只有在不在字符串中且括号平衡时才识别逗号
            if (!inString && bracketCount == 0 && c == ',') {
                commas.add(i);
            } else if (!inString) {
                if (c == '(') {
                    bracketCount++;
                } else if (c == ')') {
                    bracketCount--;
                }
            }
        }

        return commas;
    }

    /**
     * 根据逗号位置分割参数
     */
    private static List<String> splitParameters(String text, List<Integer> commaPositions) {
        List<String> params = new ArrayList<>();
        int start = 0;

        for (int commaPos : commaPositions) {
            params.add(text.substring(start, commaPos));
            start = commaPos + 1;
        }

        // 添加最后一个参数
        if (start < text.length()) {
            params.add(text.substring(start));
        }

        return params;
    }

    /**
     * 判断参数是否是函数
     */
    private static boolean isFunctionParameter(String param) {
        param = param.trim();
        if (param.isEmpty()) return false;

        // 如果以字母开头且包含左括号，可能是函数
        if (Character.isLetter(param.charAt(0)) && param.contains("(")) {
            // 检查括号是否平衡且不在字符串中
            return hasBalancedParentheses(param);
        }
        return false;
    }

    /**
     * 检查括号是否平衡（忽略字符串中的括号）
     */
    private static boolean hasBalancedParentheses(String text) {
        int bracketCount = 0;
        boolean inString = false;
        char stringDelimiter = '\0';

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            // 处理字符串和反引号界定符
            if (!inString && (c == '\'' || c == '"')) {
                inString = true;
                stringDelimiter = c;
            } else if (!inString && c == '`') {
                inString = true;
                stringDelimiter = '`';
            } else if (inString && c == stringDelimiter) {
                // 检查是否是转义的引号（反引号不需要转义）
                if (stringDelimiter != '`' && i > 0 && text.charAt(i-1) == '\\') {
                    // 转义引号，继续在字符串中
                } else {
                    inString = false;
                }
            }

            // 只有在不在字符串中时才计数括号
            if (!inString) {
                if (c == '(') {
                    bracketCount++;
                } else if (c == ')') {
                    bracketCount--;
                    if (bracketCount < 0) {
                        return false; // 右括号出现在左括号之前
                    }
                }
            }
        }

        return bracketCount == 0;
    }

    /**
     * 打印解析结果
     */
    public static void printResults(List<FunctionCall> results, int indent) {
        for (FunctionCall result : results) {
            String indentStr = "  ";
            System.out.println(indentStr + result);

            // 打印参数
            for (Parameter param : result.getParameters()) {
                System.out.println(indentStr + "  " + param);
                if (param.isFunction() && param.getCall() != null) {
                    // 这里可以进一步处理嵌套函数
                }
            }

            // 递归打印嵌套函数
            printResults(result.getCalls(), indent + 1);
        }
    }
}