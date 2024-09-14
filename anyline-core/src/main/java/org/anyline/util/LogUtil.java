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

package org.anyline.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.metadata.Column;
import org.anyline.metadata.type.TypeMetadata;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LogUtil {
    private static final String SINGLE_CHAR = "abcdefghijklmnopqrstuvwxyz0123456789:,.?'\"_-=+!@#$%^&*{}[]()\\/ ";
    /**
     * @param color  前景色代号(31-36) 背景颜色(41-46) <br>
     * 30:黑色 <br>
     * 31:红色 <br>
     * 32:绿色 <br>
     * 33:黄色 <br>
     * 34:蓝色 <br>
     * 35:紫色 <br>
     * 36:浅蓝 <br>
     * 37:灰色 <br>
     *
     * 40:黑色 <br>
     * 41:红色 <br>
     * 42:绿色 <br>
     * 43:黄色 <br>
     * 44:蓝色 <br>
     * 45:紫色 <br>
     * 46:浅蓝 <br>
     * 47:灰色
     * @param type    样式代号:0无;1加粗;3斜体;4下划线
     * @param content 日志内容
     * @return String
     */
    public static String format(String content, int color, int type) {
        boolean hasType = type != 1 && type != 3 && type != 4;
        if (hasType) {
            return String.format("\033[%dm%s\033[0m", color, content);
        } else {
            return String.format("\033[%d;%dm%s\033[0m", color, type, content);
        }
    }
    public static String format(String content, int color) {
        return format(content, color, 0);
    }
    public static String format(int content, int color) {
        return format(content+"", color, 0);
    }
    public static String format(double content, int color) {
        return format(content+"", color, 0);
    }
    public static String format(long content, int color) {
        return format(content+"", color, 0);
    }
    public static String format(BigDecimal content, int color) {
        return format(content+"", color, 0);
    }
    public static String format(boolean content, int color) {
        return format(content+"", color, 0);
    }

    /**
     * 参数日志格式化
     * @param params params
     * @return String
     */
    public static String param(List params) {
        StringBuilder builder = new StringBuilder();
        if(null != params && !params.isEmpty()) {
            builder.append("\n");
            int idx = 0;
            for(Object param:params) {
                builder.append("param").append(idx++).append("=").append(param);
                if(null != param) {
                    builder.append("(").append(ClassUtil.type(param)).append(")");
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }
    public static String param(List keys, List values) {
        StringBuilder builder = new StringBuilder();
        if (null != keys && null != values && !keys.isEmpty()) {
            //有key并且与value一一对应
            builder.append("\n");
            if(keys.size() == values.size()) {
                int size = keys.size();
                for (int i = 0; i < size; i++) {
                    Object key = keys.get(i);
                    Object value = values.get(i);
                    builder.append(keys.get(i)).append("=");
                    builder.append(value);
                    if (null != value) {
                        builder.append("(").append(ClassUtil.type(value)).append(")");
                    }
                    builder.append("\n");
                }
            }else{
                return param(values);
            }
        }else if(null != values) {
            int idx = 0;
            builder.append("\n");
            for(Object value:values) {
                builder.append("param").append(idx++).append("=").append(value);
                if(null != value) {
                    builder.append("(").append(ClassUtil.type(value)).append(")");
                }
                builder.append("\n");
            }
        }
        return builder.toString();
    }


    public static String table(DataSet set) {
        return table(set, set.keys(), true, ConfigTable.LOG_QUERY_RESULT_ROWS, ConfigTable.LOG_QUERY_RESULT_TABLE_MAX_WIDTH);
    }
    /**
     * 表格日志格式化
     * @param set 数据源
     * @param keys 输出的列
     * @param color 标题颜色
     * @param rows 显示多少行
     * @param width 表格宽度(中文占2个宽度)(根据分辨率)
     * @return table
     */
    public static String table(DataSet set, List<String> keys, boolean color, int rows, int width) {
        StringBuilder result = new StringBuilder();
        int limit_width = width; //每行限制宽度
        int col_max_width = ConfigTable.LOG_QUERY_RESULT_CUT_WIDTH; //截断后最宽可保留多少 如果不设置会因为整行内容太宽 以及limit_width限制 按比例截断造成 内容太短
        int col_max_length = 90; //多行合计
        boolean ellipsis = true;//超出是否显示省略号
        boolean br = true; //超出是否换行

        Map<String, Integer> origin_widths = new HashMap<>(); //每列最大宽度
        Map<String, Integer> view_widths = new HashMap<>();   //显示宽度
        Map<String, Integer> zip_cols = new HashMap<>();      //整行超宽后 需要压缩的列 String格式的列
        Map<String, Integer> cols_aligns = new HashMap<>();   //对齐方式 -1左对齐(默认) 1右对齐 0居中
        for(String key:keys) {
            Column column = set.getMetadata(key);
            if(null != column) {
                TypeMetadata tm = column.getTypeMetadata();
                if(null != tm) {
                    TypeMetadata.CATEGORY_GROUP tc = tm.getCategoryGroup();
                    if(tc == TypeMetadata.CATEGORY_GROUP.DATETIME || tc == TypeMetadata.CATEGORY_GROUP.BOOLEAN) {
                        cols_aligns.put(key.toUpperCase(), 0);
                    }else if(tc == TypeMetadata.CATEGORY_GROUP.NUMBER) {
                        cols_aligns.put(key.toUpperCase(), 1);
                    }
                }
            }
        }
        boolean has_pk = !set.getPrimaryKeys().isEmpty();

        int idx = 0;
        for(DataRow row:set) {
            if(idx ++ >= rows && rows != -1) {
                break;
            }
            for(String key:keys) {
                Integer origin_width = origin_widths.get(key.toUpperCase());
                if(null == origin_width) {
                    origin_width = 0;
                }
                String value = " " + row.getString(key) + " ";
                int key_width = len(key) + 2; //两侧有空格
                int value_width = 0;
                if(null != value) {
                    value_width = len(value);
                    origin_width = NumberUtil.max(origin_width, value_width, key_width);
                    if(value_width > key_width) {
                        //value > key 的列,超宽时优先压缩
                        if(isZip(row, key) && value_width > key_width) {
                            zip_cols.put(key.toUpperCase(), value_width);
                        }
                    }
                }
                origin_widths.put(key.toUpperCase(), origin_width);
            }
        }
        double max_width = 0d; //每行总宽度
        for(Integer col_width:origin_widths.values()) {
            max_width += col_width;
        }
        if(limit_width > max_width) {
            limit_width = (int)max_width;
        }
        //按比例计算 每列 显示宽度
        double rate = limit_width/max_width;
        int exceed = 0; //根据列且宽度 超出的宽度 按比例缩短最宽n列(但不能小于列名，否则换行显示)
        for(String key:keys) {
            int origin_width = origin_widths.get(key.toUpperCase());  //原宽度
            int rate_width = (int)(origin_width*rate); //按比例计算后宽度
            int view_width = origin_width;//最终显示宽度
            int key_width = len(key)+2;
            if(zip_cols.containsKey(key.toUpperCase())) {
                if(rate_width < col_max_width) {
                    rate_width = col_max_width;
                }
                view_width = rate_width;
            }
            if(view_width < key_width) {
                view_width = key_width;
            }
            if(view_width > rate_width) {
                exceed += view_width - rate_width;
            }
            view_widths.put(key.toUpperCase(), view_width);
        }
        if(exceed > 0) {
            double zip_cols_width = 0d; //需要压缩的宽列一共宽度
            for(String key:zip_cols.keySet()) {
                zip_cols_width += zip_cols.get(key);
            }
            //按比例压缩
            for(String key:zip_cols.keySet()) {
                int view_width = view_widths.get(key);
                int key_width = len(key) + 2;
                view_width = view_width - (int)((view_width/zip_cols_width)*exceed);
                if(view_width < key_width) {
                    //比列名短
                    view_width = key_width;
                }
                view_widths.put(key, view_width);
            }

        }
        //压缩后还是太宽的 换行显示
        List<List<String>> tables = new ArrayList<>();
        List<String> table = new ArrayList<>();
        int total = 0;
        for(String key:keys) {
            int view_width = view_widths.get(key.toUpperCase());
            total += view_width;
            if(total > limit_width) {
                tables.add(table);
                //下一个表
                table = new ArrayList<>();
                table.add(key);
                total = view_width;
            }else{
                table.add(key);
            }
        }
        if(!table.isEmpty()) {
            tables.add(table);
        }
        int tab_index = 0;
        result.append("[tables:").append(tables.size()).append("][rows:").append(set.size()).append("][cols:").append(keys.size()).append("]");
        if(BasicUtil.isNotEmpty(ConfigTable.LOG_QUERY_RESULT_ALT)) {
            result.append(ConfigTable.LOG_QUERY_RESULT_ALT);
        }
        result.append("\n");
        for(List<String> cols:tables) {
            StringBuilder builder = new StringBuilder();
            //分隔线
            StringBuilder row_split = new StringBuilder();
            if(!has_pk) {
                row_split.append("---");
            }
            int col_size = cols.size();
            for(int i=0; i<col_size; i++) {
                Integer view_width = view_widths.get(cols.get(i).toUpperCase());
                String left = "";
                String right = "+";
                if(i == 0) {
                    left = "+";
                }
                row_split.append(left);
                fill(row_split, view_width, "-");
                row_split.append(right);
            }
            builder.append(row_split);
            builder.append("\n");
            //生成表头
            StringBuilder title = new StringBuilder();
            if(!has_pk) {
                title.append((tab_index+1)+"/"+tables.size());
                tab_index ++;
            }
            for(int i=0; i<col_size; i++) {
                String key = cols.get(i);
                int view_width = view_widths.get(key.toUpperCase());
                String left = "";
                String right = "|";
                if(i == 0) {
                    left = "|";
                }
                title.append(left);
                String content = cell(" " + key + " ", view_width, cols_aligns.get(key.toUpperCase()));
                title.append(content);
                title.append(right);
            }
            if(color) {
                builder.append(format(title.toString(), 34));
            }else{
                builder.append(title);
            }
            builder.append("\n");
            builder.append(row_split);
            idx = 0;
            for(DataRow row:set) {
                if(idx >= rows && rows != -1) {
                    break;
                }
                builder.append("\n");
                if(!has_pk) {
                    builder.append(cell(idx+"", 3, 1));
                }
                idx ++;
                for(int i=0; i<col_size; i++) {
                    String key = cols.get(i);
                    String value = " " + row.getString(key) + " ";
                    int view_width = view_widths.get(key.toUpperCase());
                    String left = "";
                    String right = "|";
                    if(i == 0) {
                        left = "|";
                    }
                    builder.append(left);
                    Integer align = cols_aligns.get(key.toUpperCase());
                    String content = cell(value, view_width, align);
                    if(null != align && align == 1) {
                        content = format(content, 36);
                    }
                    builder.append(content);
                    builder.append(right);
                }
                builder.append("\n");
                builder.append(row_split);
            }
            builder.append("\n");
            builder.append(title);
            builder.append("\n");
            builder.append(row_split);
            builder.append("\n");
            builder.append("\n");
            result.append(builder);
        }
        return result.toString();
    }

    private static boolean isZip(DataRow row, String key) {
        Column column = row.getMetadata(key);
        boolean zip = true;
        if(null != column) {
            TypeMetadata.CATEGORY_GROUP type = column.getTypeMetadata().getCategoryGroup();
            //NUMBER, BOOLEAN, BYTES, DATETIME 这几类不压缩
            if(type == TypeMetadata.CATEGORY_GROUP.NUMBER || type == TypeMetadata.CATEGORY_GROUP.BOOLEAN || type == TypeMetadata.CATEGORY_GROUP.BYTES || type == TypeMetadata.CATEGORY_GROUP.DATETIME) {
                zip = false;
            }
        }
        return zip;
    }

    public static void fill(StringBuilder builder, int size, String ch) {
        for(int i=0; i<size; i++) {
            builder.append(ch);
        }
    }
    public static int len(String src) {
        String chrs[] = src.split("");
        int cnt = 0;
        for(String chr:chrs) {
            if(SINGLE_CHAR.contains(chr.toLowerCase())) {
                cnt += 1;
            }else{
                cnt += 2;
            }
        }
        return cnt;
    }


    /**
     *
     * @param src 原文
     * @param size 补齐宽度
     * @param align 对齐方式 -1左对齐(默认) 1右对齐 0居中
     * @return String
     */
    public static String cell(String src, int size, Integer align) {
        if(null == align) {
            align = -1;
        }
        StringBuilder result = new StringBuilder();
        String chrs[] = src.split("");
        int cnt = 0;
        for(String chr:chrs) {
            if(cnt >= size) {
                break;
            }
            if(SINGLE_CHAR.contains(chr.toLowerCase())) {
                cnt += 1;
            }else{
                if(cnt + 2 > size) {
                    break;
                }
                cnt += 2;
            }
            result.append(chr);
        }
        int dif = size - cnt;
        if(align == 1) {
            for (int i = 0; i < dif; i++) {
                result.insert(0," ");
            }
        }else if(align == 0) {
            int left = (int)Math.ceil(dif/2);
            int right = dif - left;
            for (int i = 0; i < left; i++) {
                result.insert(0," ");
            }
            for (int i = 0; i < right; i++) {
                result.append(" ");
            }
        }else{
            for (int i = 0; i < dif; i++) {
                result.append(" ");
            }
        }
        return result.toString();
    }

}
