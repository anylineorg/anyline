package org.anyline.util;

import org.anyline.util.regular.RegularUtil;

import java.util.HashMap;
import java.util.Map;

public class StyleParser {
    public static Map<String, Map<String, String>> load(String html) {
        String style = html;
        if (html.contains("<style")) {
            style = RegularUtil.cut(html, "<style", ">", "</style>");
        }
        Map<String, Map<String, String>> styles = new HashMap<>();
        if (null != style) {
            while (style.contains("}")) {
                String item = RegularUtil.cut(style, "{begin}", "}");
                if (null == item) {
                    break;
                }
                style = style.substring(item.length() + 1);
                item = item.trim();
                String key = RegularUtil.cut(item, "{begin}", "{");
                String value = RegularUtil.cut(item, "{", "{end}");
                if (null == key || null == value) {
                    continue;
                }
                key = key.trim();
                value = value.trim();
                String[] keys = key.split(",");
                for (String k : keys) {
                    k = k.trim();
                    Map<String, String> tmps = parse(value);
                    tmps = merge(styles.get(k),tmps, true);
                    styles.put(k,tmps);
                }
            }
        }
        return styles;
    }

    public static Map<String, String> parse(String txt) {
        Map<String, String> styles = new HashMap<>();

        if (null != txt) {
            txt = BasicUtil.compressionSpace(txt);//多个空格压缩成一个
            String[] items = txt.split(";");
            for (String item : items) {
                if (item.contains(":")) {
                    String[] kv = item.split(":");
                    if (kv.length == 2) {
                        String k = kv[0].trim();
                        String v = kv[1].trim();
                        styles.put(k, v);
                        if ("border".equalsIgnoreCase(k)) {
                            v = v.replace("solid", "single");
                            String[] vs = v.split(" ");
                            //border:5px solid red;
                            if (vs.length == 3) {
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
                        } else if("border-top".equalsIgnoreCase(k)
                                || "border-bottom".equalsIgnoreCase(k)
                                || "border-left".equalsIgnoreCase(k)
                                || "border-right".equalsIgnoreCase(k)
                                || "border-insideV".equalsIgnoreCase(k)
                                || "border-insideH".equalsIgnoreCase(k)
                                || "border-tl2br".equalsIgnoreCase(k)
                                || "border-tr2bl".equalsIgnoreCase(k)
                        ){
                            v = v.replace("solid", "single");
                            if("none".equalsIgnoreCase(v)){
                                styles.put(k,v);
                                removeBorder(k, styles);
                            }else {
                                String[] vs = v.split(" ");
                                if (vs.length == 3) {
                                    styles.put(k + "-width", vs[0]);
                                    styles.put(k + "-style", vs[1]);
                                    styles.put(k + "-color", vs[2]);
                                }
                            }
                        }else if ("border-width".equalsIgnoreCase(k)) {
                            String[] vs = v.split(" ");
                            if (vs.length == 2) {
                                styles.put("border-top-width", vs[0]);
                                styles.put("border-bottom-width", vs[0]);
                                styles.put("border-insideH-width", vs[0]);
                                styles.put("border-left-width", vs[1]);
                                styles.put("border-right-width", vs[1]);
                                styles.put("border-insideV-width", vs[1]);
                            }
                        } else if ("border-style".equalsIgnoreCase(k)) {
                            v = v.replace("solid", "single");
                            String[] vs = v.split(" ");
                            if (vs.length == 2) {
                                styles.put("border-top-style", vs[0]);
                                styles.put("border-bottom-style", vs[0]);
                                styles.put("border-insideH-style", vs[0]);
                                styles.put("border-left-style", vs[1]);
                                styles.put("border-right-style", vs[1]);
                                styles.put("border-insideV-style", vs[1]);
                            }
                        } else if ("border-color".equalsIgnoreCase(k)) {
                            String[] vs = v.split(" ");
                            if (vs.length == 2) {
                                styles.put("border-top-color", vs[0]);
                                styles.put("border-bottom-color", vs[0]);
                                styles.put("border-insideH-color", vs[0]);
                                styles.put("border-left-color", vs[1]);
                                styles.put("border-right-color", vs[1]);
                                styles.put("border-insideV-color", vs[0]);
                            }
                        } else if ("font".equalsIgnoreCase(k)) {
                            if ("bold".equalsIgnoreCase(v)) {
                                styles.put("font-weight", "700");
                            }
                        } else if ("font-weight".equalsIgnoreCase(k)) {
                            if ("bold".equalsIgnoreCase(v)) {
                                styles.put("font-weight", "700");
                            }
                        } else if ("font-style".equalsIgnoreCase(k)) {
                            if ("italic".equalsIgnoreCase(v)) {
                                styles.put("italic", "true");
                            }
                        } else if ("text-decoration".equalsIgnoreCase(k)) {
                            if ("underline".equalsIgnoreCase(v)) {
                                styles.put("underline", "true");
                            }
                        }
                    }
                }
            }
        }
        return styles;
    }
    public static Map<String,String> inherit(Map<String, String> src, Map<String, String> parent){
        if(null == src){
            src = new HashMap<>();
        }

        if(null != parent){
            for(String k: parent.keySet()){
                if(src.containsKey(k)){
                   continue;
                }
                if(k.contains("font") || k.contains("align") || k.contains("list-style") || k.contains("speak")
                        || k.equalsIgnoreCase("line-height")
                        || k.equalsIgnoreCase("word-spacing")
                        || k.equalsIgnoreCase("border-collapse")
                        || k.equalsIgnoreCase("border-spacing")
                        || k.equalsIgnoreCase("azimuth")
                        || k.equalsIgnoreCase("color")
                        || k.equalsIgnoreCase("caption-side")
                        || k.equalsIgnoreCase("cursor")
                        || k.equalsIgnoreCase("direction")
                        || k.equalsIgnoreCase("elevation")
                        || k.equalsIgnoreCase("empty-cells")
                        || k.equalsIgnoreCase("letter-spacing")
                        || k.equalsIgnoreCase("orphans")
                        || k.equalsIgnoreCase("pitch-range")
                        || k.equalsIgnoreCase("pitch")
                        || k.equalsIgnoreCase("quotes")
                        || k.equalsIgnoreCase("richness")
                        || k.equalsIgnoreCase("speaknumeral")
                        || k.equalsIgnoreCase("speechrate")
                        || k.equalsIgnoreCase("stress")
                        || k.equalsIgnoreCase("texttransform")
                        || k.equalsIgnoreCase("text-indent")
                        || k.equalsIgnoreCase("visibility")
                        || k.equalsIgnoreCase("voice-family")
                        || k.equalsIgnoreCase("volume")
                        || k.equalsIgnoreCase("word-spacing")
                        || k.equalsIgnoreCase("whitespace")
                ) {
                    src.put(k, parent.get(k));
                }
            }
        }

        return src;
    }
    public static Map<String, String> removeBorder(String side, Map<String, String> styles){
        side = side.replace("border-","");
        styles.remove("border-"+side+"-width");
        styles.remove("border-"+side+"-style");
        styles.remove("border-"+side+"-color");
        styles.put("border-"+side,"none");
        return styles;
    }
    public static Map<String, String> removeBorder(Map<String, String> styles){
        removeBorder("top", styles);
        removeBorder("right", styles);
        removeBorder("bottom", styles);
        removeBorder("left", styles);
        removeBorder("insideV", styles);
        removeBorder("insideH", styles);
        return styles;
    }
    public static Map<String, String> merge(Map<String, String> src, Map<String, String> copy) {
        return merge(src, copy, false);
    }
    public static Map<String, String> merge(Map<String, String> src, Map<String, String> copy, boolean over) {
        if(null == src){
            src = new HashMap<>();
        }

        if(null == copy){
            copy = new HashMap<>();
        }

        if ("none".equalsIgnoreCase(copy.get("border"))) {
            removeBorder(src);
        }
        if ("none".equalsIgnoreCase(copy.get("border-left"))) {
            removeBorder("left", src);
        }
        if ("none".equalsIgnoreCase(copy.get("border-right"))) {
            removeBorder("right", src);
        }
        if ("none".equalsIgnoreCase(copy.get("border-top"))) {
            removeBorder("top", src);
        }
        if ("none".equalsIgnoreCase(copy.get("border-bottom"))) {
            removeBorder("bottom", src);
        }
        if ("none".equalsIgnoreCase(copy.get("border-insideV"))) {
            removeBorder("insideV", src);
        }
        if ("none".equalsIgnoreCase(copy.get("border-insideH"))) {
            removeBorder("insideH", src);
        }
        BeanUtil.merge(src, copy, over);
        return src;
    }

    public static Map<String,String> parse(Map<String,String> src, String txt, boolean over){
        Map<String,String> copy = StyleParser.parse(txt);
        return merge(src, copy, over);
    }
    public static Map<String,String> parse(Map<String,String> src, String txt){
        return parse(src, txt, false);
    }
}
