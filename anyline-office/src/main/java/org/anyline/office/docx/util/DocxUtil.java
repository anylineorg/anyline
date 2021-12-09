package org.anyline.office.docx.util;

import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class DocxUtil {
    private static Logger log = LoggerFactory.getLogger(DocxUtil.class);


    /**
     * 根据关键字查找样式列表ID
     * @param docx docx文件
     * @param key 关键字
     * @return String
     */
    public static String listStyle(File docx, String key){
        try {
            String num_xml = ZipUtil.read(docx, "word/document.xml");
            Document document = DocumentHelper.parseText(num_xml);
            List<Element> ts = DomUtil.elements(document.getRootElement(),"t");
            for(Element t:ts){
                if(t.getTextTrim().contains(key)){
                    Element r = t.getParent();
                    Element p = r.getParent();
                    Element pr = p.element("pPr");
                    if(null != pr) {
                        Element numPr = pr.element("numPr");
                        if(null != numPr){
                            Element numId = numPr.element("numId");
                            if(null != numId){
                                String val = numId.attributeValue("val");
                                if(null != val){
                                    return val;
                                }
                            }
                        }
                    }
                }

            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据关键字查找样式列表ID
     * @param docx docx文件
     * @return String
     */
    public static List<String> listStyles(File docx){
        List<String> list = new ArrayList<>();
        try {
            String num_xml = ZipUtil.read(docx, "word/numbering.xml");
            Document document = DocumentHelper.parseText(num_xml);
            List<Element> nums = document.getRootElement().elements("num");
            for(Element num:nums){
                list.add(num.attributeValue("numId"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 合并文件(只合并内容document.xml)合并到第一个文件中
     * @param files files
     */
    public static void merge(File ... files){
        if(null != files && files.length>1){
            List<String> docs = new ArrayList<>();
            for(File file:files){
                docs.add(ZipUtil.read(file,"word/document.xml"));
            }
            String result = merge(docs);
            try {
                Document document = DocumentHelper.parseText(result);
                Element root = document.getRootElement().element("body");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public static String merge(List<String>  docs){
        String result = null;
        return result;
    }


    public static Element prev(Element element){
        Element prev = null;
        List<Element> elements = element.getParent().elements();
        int index = elements.indexOf(element);
        if(index>0){
            prev = elements.get(index-1);
        }
        return prev;
    }
    public static String prevName(Element element){
        Element prev = prev(element);
        if(null != prev){
            return prev.getName();
        }else{
            return "";
        }
    }
    public static Element last(Element element){
        Element last = null;
        List<Element> elements = element.getParent().elements();
        if(elements.size()>0){
            last = elements.get(elements.size()-1);
        }
        return last;
    }
    public static String lastName(Element element){
        Element last = last(element);
        if(null != last){
            return last.getName();
        }else{
            return "";
        }
    }

    private static boolean isEmpty(Element element){
        List<Element> elements = element.elements();
        for(Element item:elements){
            String name = item.getName();
            if(name.equalsIgnoreCase("t") || name.equalsIgnoreCase("tbl")){
                return false;
            }
        }
        String txt = element.getTextTrim();
        if(txt.length() > 0){
            return false;
        }
        return true;
    }

    /**
     * exclude 是否是element中唯一内容
     * @param element element
     * @param exclude 不包含
     * @return boolean
     */
    public static boolean isEmpty(Element element, Element exclude){
        List<Element> elements = element.elements();
        for(Element item:elements){
            String name = item.getName();
            if(name.equalsIgnoreCase("t") || name.equalsIgnoreCase("tbl")){
                if(item != exclude) {
                    return false;
                }
            }
        }
        String txt = element.getTextTrim();
        if(txt.length() > 0){
            return false;
        }
        return true;
    }
    private static boolean isEmpty(List<Element> elements){
        for(Element item:elements){
            String name = item.getName();
            if(name.equalsIgnoreCase("r") || name.equalsIgnoreCase("t") || name.equalsIgnoreCase("tbl")){
                return false;
            }
        }
        return true;
    }

    public static boolean hasParent(Element element, String parent){
        Element p = element.getParent();
        while(true){
            if(null == p){
                break;
            }
            if(p.getName().equalsIgnoreCase(parent)) {
                return true;
            }
            p = p.getParent();
        }
        return false;
    }

    /**
     * src插入到ref之后
     * @param src src
     * @param ref ref
     */
    public static void after(Element src, Element ref){
        if(null == ref || null == src){
            return;
        }
        //同级
        if(ref.getParent() == src.getParent()){
            List<Element> elements = ref.getParent().elements();
            int index = elements.indexOf(ref)+1;
            elements.remove(src);
            if(index > elements.size()-1){
                elements.add(src);
            }else {
                elements.add(index, src);
            }
        }else{
            //ref更下级
            after(src, ref.getParent());
        }

    }
    public static void after(List<Element> srcs, Element ref){
        if(null == ref || null == srcs){
            return;
        }
        int size = srcs.size();
        for(int i=size-1; i>=0; i--){
            Element src = srcs.get(i);
            // after(src, ref);
        }
        for(Element src:srcs){
            after(src, ref);
        }

    }
    /**
     * src插入到ref之前
     * @param src src
     * @param ref ref
     */
    public static void before(Element src, Element ref){
        if(null == ref || null == src){
            return;
        }
        List<Element> elements = ref.getParent().elements();
        int index = elements.indexOf(ref);
        while (!elements.contains(src)){
            src = src.getParent();
            if(null == src){
                return;
            }
        }
        elements.remove(src);
        elements.add(index, src);

    }
    /**
     * 当前节点在上级节点的下标
     * @param element element
     * @return index
     */
    public static int index(Element element){
        int index = -1;
        List<Element> elements = element.getParent().elements();
        index = elements.indexOf(element);
        return index;
    }

    /**
     * 拆分关键字
     * 拆分123${key}abc成多个w:t
     * @param txt txt
     * @return List
     */
    public static List<String> splitKey(String txt){
        List<String> list = new ArrayList<>();
        try {
            List<String> keys = RegularUtil.fetch(txt, "\\$\\{.*?\\}");
            int size = keys.size();
            if(size>0){
                String key = keys.get(keys.size()-1);
                int index = txt.lastIndexOf(key);
                String t1 = txt.substring(0, index);
                String t2 = txt.substring(index + key.length());
                if (t2.length() > 0) {
                    list.addAll(splitKey(t2));
                }
                list.add(key);
                if (t1.length() > 0) {
                    list.addAll(splitKey(t1));
                }
                txt = txt.substring(0, txt.length() - key.length());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void border(Element border, Map<String,String> styles){
        border(border,"top", styles);
        border(border,"right", styles);
        border(border,"bottom", styles);
        border(border,"left", styles);
        border(border,"insideH", styles);
        border(border,"insideV", styles);
        border(border,"tl2br", styles);
        border(border,"tr2bl", styles);
    }
    public static void border(Element border, String side, Map<String,String> styles){
        Element item = null;
        String width = styles.get("border-"+side+"-width");
        String style = styles.get("border-"+side+"-style");
        String color = styles.get("border-"+side+"-color");
        int dxa = DocxUtil.width(width);
        int line = ((int)(DocxUtil.dxa2pt(dxa)*8)/4*4);
        if(BasicUtil.isNotEmpty(width)){
            item = addElement(border, side);
            item.addAttribute("w:sz", line+"");
            item.addAttribute("w:val", style);
            item.addAttribute("w:color", color);
        }
    }
    public static void padding(Element margin, Map<String,String> styles){
        padding(margin,"top", styles);
        padding(margin,"start", styles);
        padding(margin,"bottom", styles);
        padding(margin,"end", styles);

    }
    public static void padding(Element margin, String side, Map<String,String> styles){
        String width = styles.get("padding-"+side);
        int dxa = DocxUtil.width(width);
        if(BasicUtil.isNotEmpty(width)){
            Element item = addElement(margin, side);
            item.addAttribute("w:w", dxa+"");
            item.addAttribute("w:type",  "dxa");
        }
    }
    public static void font(Element pr, Map<String,String> styles){
        String fontSize = styles.get("font-size");
        if(null != fontSize){
            int pt = 0;
            if(fontSizes.containsKey(fontSize)){
                pt = fontSizes.get(fontSize);
            }else{
                if(fontSize.endsWith("px")){
                    int px = BasicUtil.parseInt(fontSize.replace("px",""),0);
                    pt = (int)DocxUtil.px2pt(px);
                }else if(fontSize.endsWith("pt")){
                    pt = BasicUtil.parseInt(fontSize.replace("pt",""),0);
                }
            }
            if(pt>0){
                // <w:sz w:val="28"/>
                addElement(pr, "sz","val", pt+"");
            }
        }
        //加粗
        String fontWeight = styles.get("font-weight");
        if(null != fontWeight && fontWeight.length()>0){
            int weight = BasicUtil.parseInt(fontWeight,0);
            if(weight >=700){
                //<w:b w:val="true"/>
                addElement(pr, "b","val","true");
            }
        }
        //下划线
        String underline = styles.get("underline");
        if(null != underline){
            if(underline.equalsIgnoreCase("true") || underline.equalsIgnoreCase("single")){
                //<w:u w:val="single"/>
                addElement(pr, "u","val","single");
            }else{
                addElement(pr, "u","val",underline);
                /*dash - a dashed line
                dashDotDotHeavy - a series of thick dash, dot, dot characters
                dashDotHeavy - a series of thick dash, dot characters
                dashedHeavy - a series of thick dashes
                dashLong - a series of long dashed characters
                dashLongHeavy - a series of thick, long, dashed characters
                dotDash - a series of dash, dot characters
                dotDotDash - a series of dash, dot, dot characters
                dotted - a series of dot characters
                dottedHeavy - a series of thick dot characters
                double - two lines
                none - no underline
                single - a single line
                thick - a single think line
                wave - a single wavy line
                wavyDouble - a pair of wavy lines
                wavyHeavy - a single thick wavy line
                words - a single line beneath all non-space characters
                */
            }
        }
        //删除线
        String dstrike = styles.get("dstrike");
        if(null != dstrike){
            if(dstrike.equalsIgnoreCase("true")){
                //<w:dstrike w:val="true"/>
                addElement(pr, "dstrike","val","true");
            }
        }
        //斜体
        String italics = styles.get("italic");
        if(null != italics){
            if(italics.equalsIgnoreCase("true")){
                //<w:dstrike w:val="true"/>
                addElement(pr, "i","val","true");
            }
        }
        String fontFamily = styles.get("font-family");
        if(null != fontFamily){
            addElement(pr, "rFonts","eastAsia",fontFamily);
        }
        String fontFamilyAscii = styles.get("font-family-ascii");
        if(null != fontFamilyAscii){
            addElement(pr, "rFonts","ascii",fontFamilyAscii);
        }
        String fontFamilyEast = styles.get("font-family-east");
        if(null != fontFamilyEast){
            addElement(pr, "rFonts","eastAsia",fontFamilyEast);
        }
        fontFamilyEast = styles.get("font-family-eastAsia");
        if(null != fontFamilyEast){
            addElement(pr, "rFonts","eastAsia",fontFamilyEast);
        }
        String fontFamilyhAnsi = styles.get("font-family-height");
        if(null != fontFamilyhAnsi){
            addElement(pr, "rFonts","hAnsi",fontFamilyhAnsi);
        }
        fontFamilyhAnsi = styles.get("font-family-hAnsi");
        if(null != fontFamilyhAnsi){
            addElement(pr, "rFonts","hAnsi",fontFamilyhAnsi);
        }
        String fontFamilyComplex = styles.get("font-family-complex");
        if(null != fontFamilyComplex){
            addElement(pr, "rFonts","cs",fontFamilyComplex);
        }
        fontFamilyComplex = styles.get("font-family-cs");
        if(null != fontFamilyComplex){
            addElement(pr, "rFonts","cs",fontFamilyComplex);
        }

        String fontFamilyHint = styles.get("font-family-hint");
        if(null != fontFamilyHint){
            addElement(pr, "rFonts","hint",fontFamilyHint);
        }
        //<w:rFonts w:ascii="Adobe Gothic Std B" w:eastAsia="宋体" w:hAnsi="宋体" w:cs="宋体" w:hint="eastAsia"/>
    }

    public static void background(Element pr,Map<String,String> styles){
        String color = styles.get("background-color");
        if(null != color){
            //<w:shd w:val="clear" w:color="auto" w:fill="FFFF00"/>
            DocxUtil.addElement(pr, "shd", "color","auto");
            DocxUtil.addElement(pr, "shd", "val","clear");
            DocxUtil.addElement(pr, "shd", "fill",color.replace("#",""));
        }
    }

    /**
     * 添加element及属性
     * @param parent parent
     * @param tag element tag
     * @param key attribute key
     * @param value attribute value
     */
    public static void addElement(Element parent, String tag, String key, String value){
        Element element = DocxUtil.addElement(parent,tag);
        element.addAttribute("w:"+key, value);
    }
    public static Element addElement(Element parent, String tag){
        Element element = parent.element(tag);
        if(null == element){
            element = parent.addElement("w:"+tag);
        }
        return element;
    }

    public static Element next(Element parent, Element child){
        Element next = null;
        while(child.getParent() != parent){
            child = child.getParent();
            if(null == child){
                break;
            }
        }
        if(null != child){
            List<Element> elements = parent.elements();
            int index = elements.indexOf(child);
            if(index != -1){
                index ++;
                if(index >0 && index <elements.size()-1){
                    next = elements.get(index);
                }
            }
        }
        return next;
    }
    public static Element prev(Element parent, Element child){
        Element next = null;
        while(child.getParent() != parent){
            child = child.getParent();
            if(null == child){
                break;
            }
        }
        if(null != child){
            List<Element> elements = parent.elements();
            int index = elements.indexOf(child);
            if(index != -1){
                index --;
                if(index >0 && index <elements.size()-1){
                    next = elements.get(index);
                }
            }
        }
        return next;
    }
    /**
     * 当前节点下的文本
     * @param element element
     * @return String
     */
    public static String text(Element element){
        String text = "";
        Iterator<Node> nodes = element.nodeIterator();
        while (nodes.hasNext()) {
            Node node = nodes.next();
            int type = node.getNodeType();
            if(type == 3){
                text += node.getText().trim();
            }else{
                text += text((Element)node);
            }
        }
        return text.trim();
    }
    public static boolean isBlock(String text){
        if(null != text){
            List<String> styles = RegularUtil.cuts(text,true,"<style",">","</style>");
            for(String style:styles){
                text = text.replace(style,"");
            }
            text = text.trim();
            if(text.startsWith("<div") || text.startsWith("<ul") || text.startsWith("<ol") || text.startsWith("<table")){
                return true;
            }
        }
        return false;
    }


    public static List<Element> betweens(Element bookmark, String ... tags){
        String id = bookmark.attributeValue("id");
        Element end = null;
        List<Element> ends = bookmark.getParent().elements("bookmarkEnd");
        for(Element item:ends){
            if(id.equals(item.attributeValue("id"))){
                end = item;
                break;
            }
        }
        return DomUtil.betweens(bookmark, end, tags);
    }
    public static Element bookmark(Element parent, String name){
        Element start = DomUtil.element(parent, "bookmarkStart", "name", name);
        return start;
    }
    /**
     * 宽度计算
     * @param src width
     * @return dxa
     */
    public static int width(String src){
        int width = 0;
        if(null != src){
            src = src.trim().toLowerCase();
            if(src.endsWith("px")){
                src = src.replace("px","");
                width = px2dxa(BasicUtil.parseInt(src,0));
            }else if(src.endsWith("pt")){
                src = src.replace("pt","");
                width = pt2dxa(BasicUtil.parseInt(src,0));
            }else if(src.endsWith("%")){
                width = (int)(BasicUtil.parseDouble(src.replace("%",""),0d)/100*5000);

            }else if(src.endsWith("dxa")){
                width = BasicUtil.parseInt(src.replace("dxa",""),0);
            }else{
                width = px2dxa(BasicUtil.parseInt(src,0));
            }
        }
        return width;
    }
    public static String widthType(String width){
        if(null != width && width.trim().endsWith("%")){
            return "pct";
        }
        if(null != width && width.trim().endsWith("dxa")){
            return "dxa";
        }
        return "dxa";
    }
    public static final double PT_PER_PX = 0.75;
    public static final int IN_PER_PT = 72;
    public static final double CM_PER_PT = 28.3;
    public static final double MM_PER_PT = 2.83;
    public static final int EMU_PER_PX = 9525;
    public static final int px2dxa(int px){
        return pt2dxa(px2pt(px));
    }
    public static final int pt2dxa(double pt){
        return (int)(pt*20);
    }
    public static final double dxa2pt(double dxa){
        return  dxa/20;
    }
    public static final double dxa2px(double dxa){
        return  pt2px(dxa2pt(dxa));
    }
    public static final int px2emu(int px) {
        return px* EMU_PER_PX;
    }

    public static final double emu2px(double emu) {
        return (emu*EMU_PER_PX);
    }

    public static final double pt2px(double pt) {
        return (pt/PT_PER_PX);
    }

    public static final double in2px(double in) {
        return (in2pt(in)*PT_PER_PX);
    }

    public static final double px2in(double px) {
        return pt2in(px2pt(px));
    }

    public static final double cm2px(double cm) {
        return (cm2pt(cm)*PT_PER_PX);
    }

    public static final double px2cm(double px) {
        return pt2cm(px2pt(px));
    }

    public static final double mm2px(double mm) {
        return (mm2pt(mm)*PT_PER_PX);
    }

    public static final double px2mm(double px) {
        return pt2mm(px2pt(px));
    }

    public static final double pt2in(double pt) {
        return (pt/IN_PER_PT);
    }

    public static final double pt2mm(double mm) {
        return (mm/MM_PER_PT);
    }

    public static final double pt2cm(double in) {
        return (in/CM_PER_PT);
    }

    public static final double px2pt(double px) {
        return (px*PT_PER_PX);
    }

    public static final double in2pt(double in) {
        return (in*IN_PER_PT);
    }

    public static final double mm2pt(double mm) {
        return (mm*MM_PER_PT);
    }

    public static final double cm2pt(double cm) {
        return (cm*CM_PER_PT);
    }

    private static Map<String, Integer>fontSizes = new HashMap<String, Integer>() {
        {
            put("初号", 84);
            put("小初", 72);
            put("一号", 52);
            put("小一", 48);
            put("二号", 44);
            put("小二", 36);
            put("三号", 33);
            put("小三", 30);
            put("四号", 28);
            put("小四", 24);
            put("五号", 21);
            put("小五", 18);
            put("六号", 15);
            put("小六", 13);
            put("七号", 11);
            put("八号", 10);
        }
    };
}
