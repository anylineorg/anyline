package org.anyline.office.docx.util;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.FileUtil;
import org.anyline.util.regular.Regular;
import org.anyline.util.regular.RegularUtil;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StyleParser {
    public static Map<String,Map<String,String>> load(String html){
        String style = html;
        if(html.contains("<style")){
            style = RegularUtil.cut(html,"<style",">","</style>");
        }
        Map<String,Map<String,String>> styles = new HashMap<String,Map<String,String>>();
        if(null != style){
            while(style.contains("}")){
                String item = RegularUtil.cut(style,"{begin}","}");
                if(null == item){
                    break;
                }
                style = style.substring(item.length()+1);
                item = item.trim();
                String key = RegularUtil.cut(item,"{begin}","{");
                String value = RegularUtil.cut(item,"{","{end}");
                if(null == key || null ==value){
                    continue;
                }
                key = key.trim();
                value = value.trim();
                String[] keys = key.split(",");
                for(String k:keys){
                    Map<String,String> tmps = parse(value);
                    BeanUtil.merge(tmps, styles.get(k));
                    styles.put(k, tmps);
                }
            }
        }
        return styles;
    }

    public static Map<String,String> parse(String txt){
        Map<String,String> styles = new HashMap<String,String>();

        if(null != txt) {
            txt = BasicUtil.compressionSpace(txt);//多个空格压缩成一个
            String[] items = txt.split(";");
            for (String item : items) {
                if (item.contains(":")) {
                    String[] kv = item.split(":");
                    if (kv.length == 2) {
                        String k = kv[0].trim();
                        String v = kv[1].trim();
                        styles.put(k, v);
                        if("border".equalsIgnoreCase(k)){
                            v = v.replace("solid","single");
                            String[] vs = v.split(" ");
                            //border:5px solid red;
                            if("none".equalsIgnoreCase(v)){
                                styles.remove("border-top-width");
                                styles.remove("border-top-style");
                                styles.remove("border-top-color");
                                styles.remove("border-right-width");
                                styles.remove("border-right-style");
                                styles.remove("border-right-color");
                                styles.remove("border-bottom-width");
                                styles.remove("border-bottom-style");
                                styles.remove("border-bottom-color");
                                styles.remove("border-left-width");
                                styles.remove("border-left-style");
                                styles.remove("border-left-color");
                                styles.remove("border-insideV-width");
                                styles.remove("border-insideV-style");
                                styles.remove("border-insideV-color");
                                styles.remove("border-insideH-width");
                                styles.remove("border-insideH-style");
                                styles.remove("border-insideH-color");
                            }
                            else if(vs.length ==3){
                                styles.put("border-top-width", vs[0]);
                                styles.put("border-top-style", vs[1]);
                                styles.put("border-top-color", vs[2]);
                                styles.put("border-right-width", vs[0]);
                                styles.put("border-right-style", vs[1]);
                                styles.put("border-right-color", vs[2]);
                                styles.put("border-bottom-width", vs[0]);
                                styles.put("border-bottom-style", vs[1]);
                                styles.put("border-bottom-color", vs[2]);
                                styles.put("border-left-width", vs[0]);
                                styles.put("border-left-style", vs[1]);
                                styles.put("border-left-color", vs[2]);
                                styles.put("border-insideV-width", vs[0]);
                                styles.put("border-insideV-style", vs[1]);
                                styles.put("border-insideV-color", vs[2]);
                                styles.put("border-insideH-width", vs[0]);
                                styles.put("border-insideH-style", vs[1]);
                                styles.put("border-insideH-color", vs[2]);
                            }
                        }else if("border-width".equalsIgnoreCase(k)){
                            String[] vs = v.split(" ");
                            if(vs.length ==2){
                                styles.put("border-top-width", vs[0]);
                                styles.put("border-bottom-width", vs[0]);
                                styles.put("border-insideH-width", vs[0]);
                                styles.put("border-left-width", vs[1]);
                                styles.put("border-right-width", vs[1]);
                                styles.put("border-insideV-width", vs[1]);
                            }
                        }else if("border-style".equalsIgnoreCase(k)){
                            v = v.replace("solid","single");
                            String[] vs = v.split(" ");
                            if(vs.length ==2){
                                styles.put("border-top-style", vs[0]);
                                styles.put("border-bottom-style", vs[0]);
                                styles.put("border-insideH-style", vs[0]);
                                styles.put("border-left-style", vs[1]);
                                styles.put("border-right-style", vs[1]);
                                styles.put("border-insideV-style", vs[1]);
                            }
                        }else if("border-color".equalsIgnoreCase(k)){
                            String[] vs = v.split(" ");
                            if(vs.length ==2){
                                styles.put("border-top-color", vs[0]);
                                styles.put("border-bottom-color", vs[0]);
                                styles.put("border-insideH-color", vs[0]);
                                styles.put("border-left-color", vs[1]);
                                styles.put("border-right-color", vs[1]);
                                styles.put("border-insideV-color", vs[0]);
                            }
                        }else if("font".equalsIgnoreCase(k)){
                            if("bold".equalsIgnoreCase(v)){
                                styles.put("font-weight","700");
                            }
                        }else if("font-weight".equalsIgnoreCase(k)){
                            if("bold".equalsIgnoreCase(v)){
                                styles.put("font-weight","700");
                            }
                        }else if("font-style".equalsIgnoreCase(k)){
                            if("italic".equalsIgnoreCase(v)){
                                styles.put("italic","true");
                            }
                        }else if("text-decoration".equalsIgnoreCase(k)){
                            if("underline".equalsIgnoreCase(v)){
                                styles.put("underline","true");
                            }
                        }
                    }
                }
            }
        }
        return styles;
    }

    public static Map<String,String> parse(Map<String,String> src, String txt){
        if(null == src){
            src = new HashMap<String,String>();
        }
        BeanUtil.merge(src,StyleParser.parse(txt));
        return src;
    }
}
