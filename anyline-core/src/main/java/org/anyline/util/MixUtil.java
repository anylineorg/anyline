package org.anyline.util;

import org.anyline.util.encrypt.MD5Util;

public class MixUtil {

    public static String mix(String seed, String origin){
        return mix(seed, 6, 8, origin);
    }
    public static String mix(String origin){
        return mix(ConfigTable.MIX_DEFAULT_SEED, 6, 8, origin);
    }
    public static String mix(String seed, int begin, int end, String origin){
        String result = MD5Util.crypto(seed + MD5Util.crypto(origin).substring(begin) + origin).substring(begin, end);
        return result;
    }

    /**
     *
     * @param verify 验证码
     * @param seed 加密种子
     * @param origin 源
     * @return boolean
     */
    public static boolean verify(String verify, String seed, String origin){
        return verify(verify, seed, 6, 8, origin);
    }
    public static boolean verify(String verify, String origin){
        return verify(verify,ConfigTable.MIX_DEFAULT_SEED, 6, 8, origin);
    }
    public static boolean verify(String verify, String seed, int begin, int end, String origin){
        String result = MD5Util.crypto(seed + MD5Util.crypto(origin).substring(begin) + origin).substring(begin, end);
        return result.equals(verify);
    }
}
