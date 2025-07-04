/*
 * Copyright 2006-2025 www.anyline.org
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
 
import java.io.ByteArrayOutputStream; 
 
public class Base64Util {
    private static final char[] base64EncodeChars = new char[] {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q',
            'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r',
            's', 't', 'u', 'v', 'w', 'x', 'y', 'z', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '/' };
  
    private static byte[] base64DecodeChars = new byte[] {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1,
            -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60,
            61, -1, -1, -1, -1, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1,
            -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1 };

    public static String encode(String src, String charset) throws Exception {
        return encode(src.getBytes(charset));
    }
    public static String encode(String src) throws Exception {
        return encode(src, "UTF-8");
    }

    /**
     * 将字节数组编码为字符串
     *
     * @param data  data
     * @return String
     */
    public static String encode(byte[] data) {
        StringBuffer sb = new StringBuffer(); 
        int len = data.length; 
        int i = 0; 
        int b1, b2, b3;
  
        while (i < len) {
            b1 = data[i++] & 0xff; 
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]); 
                sb.append(base64EncodeChars[(b1 & 0x3) << 4]); 
                sb.append("=="); 
                break; 
            } 
            b2 = data[i++] & 0xff; 
            if (i == len) {
                sb.append(base64EncodeChars[b1 >>> 2]); 
                sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]); 
                sb.append(base64EncodeChars[(b2 & 0x0f) << 2]); 
                sb.append("="); 
                break; 
            } 
            b3 = data[i++] & 0xff; 
            sb.append(base64EncodeChars[b1 >>> 2]); 
            sb.append(base64EncodeChars[((b1 & 0x03) << 4) | ((b2 & 0xf0) >>> 4)]); 
            sb.append(base64EncodeChars[((b2 & 0x0f) << 2) | ((b3 & 0xc0) >>> 6)]); 
            sb.append(base64EncodeChars[b3 & 0x3f]); 
        } 
        return sb.toString(); 
    }
    public static byte[] decode(String code) throws Exception {
        return decode(code, "UTF-8");
    }
    public static byte[] decode(String code, String charset) throws Exception {
        byte[] data = code.getBytes(charset);
        int len = data.length; 
        ByteArrayOutputStream buf = new ByteArrayOutputStream(len); 
        int i = 0; 
        int b1, b2, b3, b4;
  
        while (i < len) {
  
            /* b1 */ 
            do {
                b1 = base64DecodeChars[data[i++]]; 
            } while (i < len && b1 == -1); 
            if (b1 == -1) {
                break; 
            } 
  
            /* b2 */ 
            do {
                b2 = base64DecodeChars[data[i++]]; 
            } while (i < len && b2 == -1); 
            if (b2 == -1) {
                break; 
            } 
            buf.write((b1 << 2) | ((b2 & 0x30) >>> 4)); 
  
            /* b3 */ 
            do {
                b3 = data[i++]; 
                if (b3 == 61) {
                    return buf.toByteArray(); 
                } 
                b3 = base64DecodeChars[b3]; 
            } while (i < len && b3 == -1); 
            if (b3 == -1) {
                break; 
            } 
            buf.write(((b2 & 0x0f) << 4) | ((b3 & 0x3c) >>> 2)); 
  
            /* b4 */ 
            do {
                b4 = data[i++]; 
                if (b4 == 61) {
                    return buf.toByteArray(); 
                } 
                b4 = base64DecodeChars[b4]; 
            } while (i < len && b4 == -1); 
            if (b4 == -1) {
                break; 
            } 
            buf.write(((b3 & 0x03) << 6) | b4); 
        } 
        return buf.toByteArray(); 
    }

    /**
     * 检测是否符合base64编码规则
     * @param str String
     * @return boolean
     */
    public static boolean verify(String str) {
        if (str == null) {
            return false;
        }
        int len = str.trim().length();
        if(len == 0) {
            return false;
        }
        if (len % 4 != 0) {
            return false;
        }
        char[] strChars = str.toCharArray();
        int idx = -1;
        for (char c:strChars) {
            idx ++;
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                    || c == '+' || c == '/') {
            }else if(c == '=') {
                if(idx < len - 2) {
                    return false;
                }
            }else {
                return false;
            }
        }
        return true;
    }

}
