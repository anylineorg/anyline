package org.anyline.util;

public class LogUtil {
    /**
     * @param color  前景色代号(31-36) 背景颜色(41-46)
     * 30:黑色
     * 31:红色
     * 32:绿色
     * 33:黄色
     * 34:蓝色
     * 35:紫色
     * 36:浅蓝
     * 37:灰色
     *
     * 40:黑色
     * 41:红色
     * 42:绿色
     * 43:黄色
     * 44:蓝色
     * 45:紫色
     * 46:浅蓝
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
}
