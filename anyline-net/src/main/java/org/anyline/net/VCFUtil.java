/*
 * Copyright 2006-2022 www.anyline.org
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
 *
 *
 */
package org.anyline.net;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class VCFUtil {
    /**
     * 生成通讯录格式
     * @param mobile 手机号
     * @param name 姓名
     * @return cvf格式内容
     * BEGIN:VCARD
     * VERSION:2.1
     * N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:;encode(姓名);;;
     * TEL;CELL:手机号
     * END:VCARD
     */
    public static String format(String mobile, String name){
        StringBuilder builder = new StringBuilder();
        builder.append("BEGIN:VCARD\n");
        builder.append("ERSION:2.1\n");
        builder.append("N;CHARSET=UTF-8;ENCODING=QUOTED-PRINTABLE:;").append(encode(name)).append(";;;\n");
        builder.append("TEL;CELL:").append(mobile).append("\n");
        builder.append("END:VCARD");
        return builder.toString();
    }
    public static String format(String mobile){
        return format(mobile, mobile);
    }

    /**
     * 批量生成
     * @param mobiles 手机号或手机号,姓名
     * @return String
     */
    public static String format(List<String> mobiles){
       StringBuilder builder = new StringBuilder();
       for(String mobile:mobiles){
            if(mobile.contains(",")){
                String[] items = mobile.split(",");
                if(items.length==1){
                    builder.append(format(items[0])).append("\n");
                }else if(items.length>1){
                    builder.append(format(items[0], items[1])).append("\n");
                }
            }else{
                builder.append(format(mobile,mobile)).append("\n");
            }
       }
       return builder.toString();
    }
    /**
     * 编码 UTF8 quoted-printable
     * @param str 需要编码的字符串
     * @return 编码后的字符串
     */
    public static String encode(String str) {
        if (str != null) {
            try {
                char[] encode = str.toCharArray();
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < encode.length; i++) {
                    if ((encode[i] >= '!') && (encode[i] <= '~')
                            && (encode[i] != '=') && (encode[i] != '\n')) {
                        sb.append(encode[i]);
                    } else if (encode[i] == '=') {
                        sb.append("=3D");
                    } else if (encode[i] == '\n') {
                        sb.append("\n");
                    } else {
                        StringBuffer sbother = new StringBuffer();
                        sbother.append(encode[i]);
                        String ss = sbother.toString();
                        byte[] buf = null;
                        buf = ss.getBytes("utf-8");
                        // UTF-8: buf.length == 3
                        // GBK: buf.length == 2
                        if (buf.length == 3) {
                            for (int j = 0; j < buf.length; j++) {
                                String s16 = String.valueOf(Integer
                                        .toHexString(buf[j]));
                                char c16_6;
                                char c16_7;
                                if (s16.charAt(6) >= 97 && s16.charAt(6) <= 122) {
                                    c16_6 = (char) (s16.charAt(6) - 32);
                                } else {
                                    c16_6 = s16.charAt(6);
                                }
                                if (s16.charAt(7) >= 97 && s16.charAt(7) <= 122) {
                                    c16_7 = (char) (s16.charAt(7) - 32);
                                } else {
                                    c16_7 = s16.charAt(7);
                                }
                                sb.append("=" + c16_6 + c16_7);
                            }
                        }
                    }
                }
                str = sb.toString();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return str;
    }

    /**
     * 解码 UTF8 quoted-printable
     * @param str 需要解码的字符串
     * @return 解码后的字符串
     */
    public static String decode(String str){
        if (str != null) {
            try {
                StringBuffer sb = new StringBuffer(str);
                for (int i = 0; i < sb.length(); i++) {
                    if (sb.charAt(i) == '\n' && sb.charAt(i - 1) == '=') {
                        sb.deleteCharAt(i - 1);
                    }
                }
                str = sb.toString();
                byte[] bytes = str.getBytes("US-ASCII");
                for (int i = 0; i < bytes.length; i++) {
                    byte b = bytes[i];
                    if (b != 95) {
                        bytes[i] = b;
                    } else {
                        bytes[i] = 32;
                    }
                }
                if (bytes != null) {
                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    for (int i = 0; i < bytes.length; i++) {
                        int b = bytes[i];
                        if (b == '=') {
                            try {
                                int u = Character.digit((char) bytes[++i], 16);
                                int l = Character.digit((char) bytes[++i], 16);
                                if (u == -1 || l == -1) {
                                    continue;
                                }
                                buffer.write((char) ((u << 4) + l));
                            } catch (ArrayIndexOutOfBoundsException e) {
                                e.printStackTrace();
                            }
                        } else {
                            buffer.write(b);
                        }
                    }
                    str = new String(buffer.toByteArray(), "UTF-8");
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return str;
    }
}
