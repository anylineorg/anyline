package org.anyline.util;

public class LogUtil {
    /**
     * @param color  颜色代号：背景颜色代号(41-46)；前景色代号(31-36)
     * 30  白色
     * 31  红色
     * 32  绿色
     * 33  黄色
     * 34  蓝色
     * 35  紫色
     * 36  浅蓝
     * 37  灰色
     * @param type    样式代号：0无；1加粗；3斜体；4下划线
     * @param content 日志内容
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
