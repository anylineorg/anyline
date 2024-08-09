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

import java.math.BigDecimal;
import java.util.List;

public class LogUtil {
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

}
