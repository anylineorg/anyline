package org.anyline.util;

import org.anyline.util.encrypt.MD5Util;

public class MixUtil {

    public static String mix(String seed, String value){
        return mix(seed, 6, 8, value);
    }
    public static String mix(String value){
        return mix("al", 6, 8, value);
    }
    public static String mix(String seed, int begin, int end, String value){
        String result = MD5Util.crypto(seed + MD5Util.crypto(value).substring(begin) + value).substring(begin, end);
        return result;
    }

    public static boolean verify(String verify, String seed, String value){
        return verify(verify, seed, 6, 8, value);
    }
    public static boolean verify(String verify, String value){
        return verify(verify,"al", 6, 8, value);
    }
    public static boolean verify(String verify, String seed, int begin, int end, String value){
        String result = MD5Util.crypto(seed + MD5Util.crypto(value).substring(begin) + value).substring(begin, end);
        return result.equals(verify);
    }
}
