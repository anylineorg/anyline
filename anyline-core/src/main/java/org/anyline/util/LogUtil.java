package org.anyline.util;

import java.math.BigDecimal;

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
}
