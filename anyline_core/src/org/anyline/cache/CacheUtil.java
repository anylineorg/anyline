package org.anyline.cache;

public class CacheUtil {
	/**
     * 返回具体的方法全路径名称 参数
     * @param targetName 全路径
     * @param methodName 方法名称
     * @param arguments 参数
     * @return 完整方法名称
     */
    public static String getCacheKey(String targetName, String methodName, Object ... arguments) {
        StringBuffer sb = new StringBuffer();
        sb.append(targetName).append(".").append(methodName);
        if ((arguments != null) && (arguments.length != 0)) {
            for (int i = 0; i < arguments.length; i++) {
                sb.append(".").append(arguments[i]);
            }
        }
        return sb.toString();
    }
}
