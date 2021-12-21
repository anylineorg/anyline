package org.anyline.office.docx.util;

import org.anyline.entity.DataRow;
import org.anyline.entity.DataSet;
import org.anyline.office.docx.entity.Wtable;
import org.anyline.office.docx.entity.Wtc;
import org.anyline.office.docx.entity.Wtr;
import org.anyline.office.docx.entity.WDocument;
import org.anyline.office.docx.entity.html.Table;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.*;
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
                    Element pr = t.getParent().getParent().element("pPr");
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

    /**
     * copy的样式复制给src
     * @param src src
     * @param copy 被复制
     */
    public static void copyStyle(Element src, Element copy){
        String name = src.getName();
        String prName = name+"Pr";
        Element srcPr = src.element(prName);
        if(null != srcPr){
            src.remove(srcPr);
        }
        Element pr = DomUtil.element(copy, prName);
        if(null != pr){
            Element newPr = pr.createCopy();
            src.elements().add(0,newPr);
        }
    }

    /**
     * 前一个节点
     * @param  element element
     * @return element
     */
    public static Element prevByName(Element element){
        Element prev = null;
        List<Element> elements = DomUtil.elements(top(element), element.getName());
        int index = elements.indexOf(element);
        if(index > 0){
            prev = elements.get(index -1);
        }
        return prev;
    }
    public static Element prevByName(Element parent, Element element){
        Element prev = null;
        List<Element> elements = DomUtil.elements(parent, element.getName());
        int index = elements.indexOf(element);
        if(index > 0){
            prev = elements.get(index -1);
        }
        return prev;
    }
    public static Element nextByName(Element element){
        Element prev = null;
        List<Element> elements = DomUtil.elements(top(element), element.getName());
        int index = elements.indexOf(element);
        if(index < elements.size()-1 && index > 0){
            prev = elements.get(index + 1);
        }
        return prev;
    }
    public static Element nextByName(Element parent, Element element){
        Element prev = null;
        List<Element> elements = DomUtil.elements(parent, element.getName());
        int index = elements.indexOf(element);
        if(index < elements.size()-1 && index > 0){
            prev = elements.get(index + 1);
        }
        return prev;
    }
    public static Element top(Element element){
        Element top = element.getParent();
        while (null != top.getParent()){
            top = top.getParent();
        }
        return top;
    }
    /**
     * 前一个节点
     * @param  element element
     * @return element
     */
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

    public static boolean isEmpty(Element element){
        List<Element> elements = DomUtil.elements(element, "drawing,tbl,t");
        for(Element item:elements){
            String name = item.getName();
            if(name.equalsIgnoreCase("drawing")){
                return false;
            }

            if(name.equalsIgnoreCase("tbl")){
                return false;
            }
            if(name.equalsIgnoreCase("t")){
                if(item.getTextTrim().length() > 0){
                    return false;
                }
            }
        }
        if(element.getTextTrim().length() > 0){
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

    public static Element getParent(Element element, String parent){
        Element p = element.getParent();
        while(true){
            if(null == p){
                break;
            }
            if(p.getName().equalsIgnoreCase(parent)) {
                return p;
            }
            p = p.getParent();
        }
        return null;
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
        int dxa = DocxUtil.dxa(width);
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
        int dxa = DocxUtil.dxa(width);
        if(BasicUtil.isNotEmpty(width)){
            Element item = addElement(margin, side);
            item.addAttribute("w:w", dxa+"");
            item.addAttribute("w:type",  "dxa");
        }
    }
    public static int fontSize(String size){
        int pt = 0;
        if(fontSizes.containsKey(size)){
            pt = fontSizes.get(size);
        }else{
            if(size.endsWith("px")){
                int px = BasicUtil.parseInt(size.replace("px",""),0);
                pt = (int)DocxUtil.px2pt(px);
            }else if(size.endsWith("pt")){
                pt = BasicUtil.parseInt(size.replace("pt",""),0);
            }
        }
        return pt;
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
        String strike = styles.get("strike");
        if(null != strike){
            if(strike.equalsIgnoreCase("true")){
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
        Attribute attribute = element.attribute(key);
        if(null != attribute){
            element.remove(attribute);
        }
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


    public static Element pr(Element element, String styles){
        return pr(element, StyleParser.parse(styles));
    }
    public static Element pr(Element element, Map<String,String> styles){
        if(null == styles){
            styles = new HashMap<String,String>();
        }
        String name = element.getName();
        String prName = name+"Pr";
        Element pr = DocxUtil.addElement(element, prName);
        if("p".equalsIgnoreCase(name)){
            for(String sk: styles.keySet()){
                String sv = styles.get(sk);
                if(BasicUtil.isEmpty(sv)){
                    continue;
                }
                if(sk.equalsIgnoreCase("list-style-type")){
                    DocxUtil.addElement(pr, "pStyle", "val",sv);
                }else if(sk.equalsIgnoreCase("list-lvl")){
                    Element numPr = DocxUtil.addElement(pr,"numPr");
                    DocxUtil.addElement(numPr, "ilvl", "val",sv+"");
                }else if(sk.equalsIgnoreCase("numFmt")){
                    Element numPr = DocxUtil.addElement(pr,"numPr");
                    DocxUtil.addElement(numPr, "numFmt", "val",sv+"");
                }else if ("text-align".equalsIgnoreCase(sk)) {
                    DocxUtil.addElement(pr, "jc","val", sv);
                }else if(sk.equalsIgnoreCase("margin-left")){
                    DocxUtil.addElement(pr, "ind", "left",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("margin-right")){
                    DocxUtil.addElement(pr, "ind", "right",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("margin-top")){
                    DocxUtil.addElement(pr, "spacing", "before",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("margin-bottom")){
                    DocxUtil.addElement(pr, "spacing", "after",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-left")){
                    DocxUtil.addElement(pr, "ind", "left",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-right")){
                    DocxUtil.addElement(pr, "ind", "right",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-top")){
                    DocxUtil.addElement(pr, "spacing", "before",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-bottom")){
                    DocxUtil.addElement(pr, "spacing", "after",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("text-indent")){
                    DocxUtil.addElement(pr, "ind", "firstLine",DocxUtil.dxa(sv)+"");
                }else if(sk.equalsIgnoreCase("line-height")){
                    DocxUtil.addElement(pr, "spacing", "line",DocxUtil.dxa(sv)+"");
                }
            }
            if(styles.containsKey("list-style-num")){
                //如果在样式里指定了样式
                Element numPr = DocxUtil.addElement(pr,"numPr");
                DocxUtil.addElement(numPr, "numId", "val",styles.get("list-style-num"));
            }else if(styles.containsKey("list-num")){
                //运行时自动生成
                Element numPr = DocxUtil.addElement(pr,"numPr");
                DocxUtil.addElement(numPr, "numId", "val",styles.get("list-num"));
            }

            //<div style="page-size-orient:landscape"/>
            if(styles.containsKey("page-size-orient")){
                String orient = styles.get("page-size-orient");
                if(!"landscape".equalsIgnoreCase(orient)){
                    orient = "portrait";
                }
                setOrient(pr, orient, styles);
            }

            Element border = DocxUtil.addElement(pr, "bdr");
            DocxUtil.border(border, styles);
            //DocxUtil.background(pr, styles);

        }else if("r".equalsIgnoreCase(name)){
            for (String sk : styles.keySet()) {
                String sv = styles.get(sk);
                if(BasicUtil.isEmpty(sv)){
                    continue;
                }
                if(sk.equalsIgnoreCase("color")){
                    Element color = pr.addElement("w:color");
                    color.addAttribute("w:val", sv.replace("#",""));
                }else if(sk.equalsIgnoreCase("background-color")){
                    //<w:highlight w:val="yellow"/>
                    DocxUtil.addElement(pr, "highlight", "val",sv.replace("#",""));
                }else if(sk.equalsIgnoreCase("vertical-align")){
                    DocxUtil.addElement(pr,"vertAlign", "val", sv );
                }
            }
            Element border = DocxUtil.addElement(pr, "bdr");
            DocxUtil.border(border, styles);
            DocxUtil.font(pr, styles);
        }else if("tbl".equalsIgnoreCase(name)){

            //DocxUtil.addElement(pr,"tblCellSpacing","w","0");
            //DocxUtil.addElement(pr,"tblCellSpacing","type","nil");

            Element mar = DocxUtil.addElement(pr,"tblCellMar");
            /*DocxUtil.addElement(mar,"top","w","0");
            DocxUtil.addElement(mar,"top","type","dxa");
            DocxUtil.addElement(mar,"bottom","w","0");
            DocxUtil.addElement(mar,"bottom","type","dxa");
            DocxUtil.addElement(mar,"right","w","0"); //新版本end
            DocxUtil.addElement(mar,"right","type","dxa");
            DocxUtil.addElement(mar,"end","w","0");
            DocxUtil.addElement(mar,"end","type","dxa");
            DocxUtil.addElement(mar,"left","w","0");//新版本用start,但07版本用start会报错
            DocxUtil.addElement(mar,"left","type","dxa");*/
            for (String sk : styles.keySet()) {
                String sv = styles.get(sk);
                if(BasicUtil.isEmpty(sv)){
                    continue;
                }
                if(sk.equalsIgnoreCase("width")){
                    DocxUtil.addElement(pr,"tblW","w", DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(pr,"tblW","type", DocxUtil.widthType(sv));
                }else if(sk.equalsIgnoreCase("color")){
                }else if(sk.equalsIgnoreCase("margin-left")){
                    DocxUtil.addElement(pr,"tblInd","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(pr,"tblInd","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-left")){
                    DocxUtil.addElement(mar,"left","w",DocxUtil.dxa(sv)+""); //新版本用start,但07版本用start会报错
                    DocxUtil.addElement(mar,"left","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-right")){
                    DocxUtil.addElement(mar,"right","w",DocxUtil.dxa(sv)+""); //新版本用end
                    DocxUtil.addElement(mar,"right","type","dxa");
                    DocxUtil.addElement(mar,"end","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(mar,"end","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-top")){
                    DocxUtil.addElement(mar,"top","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(mar,"top","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-bottom")){
                    DocxUtil.addElement(mar,"bottom","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(mar,"bottom","type","dxa");
                }
            }

            Element border = DocxUtil.addElement(pr,"tblBorders");
            DocxUtil.border(border, styles);
            DocxUtil.background(pr, styles);
        }else if("tr".equalsIgnoreCase(name)){
            for(String sk:styles.keySet()){
                String sv = styles.get(sk);
                if(BasicUtil.isEmpty(sv)){
                    continue;
                }
                if("repeat-header".equalsIgnoreCase(sk)){
                    DocxUtil.addElement(pr,"tblHeader","val","true");
                }else if("min-height".equalsIgnoreCase(sk)){
                    DocxUtil.addElement(pr,"trHeight","hRule","atLeast");
                    DocxUtil.addElement(pr,"trHeight","val",(int)DocxUtil.dxa2pt(DocxUtil.dxa(sv))*20+"");
                }else if("height".equalsIgnoreCase(sk)){
                    DocxUtil.addElement(pr,"trHeight","hRule","exact");
                    DocxUtil.addElement(pr,"trHeight","val",(int)DocxUtil.dxa2pt(DocxUtil.dxa(sv))*20+"");
                }
            }
        }else if("tc".equalsIgnoreCase(name)){
            for(String sk:styles.keySet()){
                String sv = styles.get(sk);
                if(BasicUtil.isEmpty(sv)){
                    continue;
                }

                Element mar = DocxUtil.addElement(pr,"tcMar");
                /*DocxUtil.addElement(mar,"top","w","0");
                DocxUtil.addElement(mar,"top","type","dxa");
                DocxUtil.addElement(mar,"bottom","w","0");
                DocxUtil.addElement(mar,"bottom","type","dxa");
                DocxUtil.addElement(mar,"right","w","0"); //新版本end
                DocxUtil.addElement(mar,"right","type","dxa");
                DocxUtil.addElement(mar,"end","w","0");
                DocxUtil.addElement(mar,"end","type","dxa");
                DocxUtil.addElement(mar,"left","w","0");//新版本用start,但07版本用start会报错
                DocxUtil.addElement(mar,"left","type","dxa");*/
                if("vertical-align".equalsIgnoreCase(sk)){
                    DocxUtil.addElement(pr,"vAlign", "val", sv );
                }else if("text-align".equalsIgnoreCase(sk)){
                    DocxUtil.addElement(pr, "jc","val", sv);
                }else if(sk.equalsIgnoreCase("white-space")){
                    DocxUtil.addElement(pr,"noWrap");
                }else if(sk.equalsIgnoreCase("width")){
                    DocxUtil.addElement(pr,"tcW","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(pr,"tcW","type",DocxUtil.widthType(sv));
                }else if(sk.equalsIgnoreCase("padding-left")){
                    DocxUtil.addElement(mar,"left","w",DocxUtil.dxa(sv)+""); //新版本用start,但07版本用start会报错
                    DocxUtil.addElement(mar,"left","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-right")){
                    DocxUtil.addElement(mar,"right","w",DocxUtil.dxa(sv)+""); //新版本用end
                    DocxUtil.addElement(mar,"right","type","dxa");
                    DocxUtil.addElement(mar,"end","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(mar,"end","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-top")){
                    DocxUtil.addElement(mar,"top","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(mar,"top","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-bottom")){
                    DocxUtil.addElement(mar,"bottom","w",DocxUtil.dxa(sv)+"");
                    DocxUtil.addElement(mar,"bottom","type","dxa");
                }
            }
            //
            Element padding = DocxUtil.addElement(pr,"tcMar");
            DocxUtil.padding(padding, styles);
            Element border = DocxUtil.addElement(pr,"tcBorders");
            DocxUtil.border(border, styles);
            DocxUtil.background(pr, styles);
        }
        if(pr.elements().size()==0){
            element.remove(pr);
        }
        return pr;
    }

    //插入排版方向
    public static void setOrient(Element pr, String orient, Map<String,String> styles){
        String w = styles.get("page-size-w");
        String h = styles.get("page-size-h");
        String top = styles.get("page-margin-top");
        String right = styles.get("page-margin-right");
        String bottom = styles.get("page-margin-bottom");
        String left = styles.get("page-margin-left");
        String header = styles.get("page-margin-left");
        String footer = styles.get("page-margin-left");

        header = BasicUtil.evl(header, "851").toString();
        footer = BasicUtil.evl(footer, "992").toString();
        if("portrait".equalsIgnoreCase(orient)){
            //竖板<w:pgMar w:top="1440" w:right="1134" w:bottom="1440" w:left="1531" w:header="851" w:footer="992" w:gutter="0"/>
            w = BasicUtil.evl(w, "11906").toString();
            h = BasicUtil.evl(h, "16838").toString();
            top = BasicUtil.evl(top, "1440").toString();
            right = BasicUtil.evl(right, "1134").toString();
            bottom = BasicUtil.evl(bottom, "1440").toString();
            left = BasicUtil.evl(left, "1531").toString();
        }else {
            //横板
            // <w:pgSz w:w="16838" w:h="11906" w:orient="landscape"/>
            // <w:pgMar w:top="1531" w:right="1440" w:bottom="1134" w:left="1440" w:header="851" w:footer="992" w:gutter="0"/>
            w = BasicUtil.evl(w, "16838").toString();
            h = BasicUtil.evl(h, "11906").toString();
            top = BasicUtil.evl(top, "1531").toString();
            right = BasicUtil.evl(right, "1134").toString();
            bottom = BasicUtil.evl(bottom, "1440").toString();
            left = BasicUtil.evl(left, "1531").toString();
        }
        Element sectPr = DocxUtil.addElement(pr,"sectPr");
        DocxUtil.addElement(sectPr,"pgSz","w", w);
        DocxUtil.addElement(sectPr,"pgSz","h", h);
        DocxUtil.addElement(sectPr,"pgSz","orient", orient);

        DocxUtil.addElement(sectPr,"pgMar","top", top);
        DocxUtil.addElement(sectPr,"pgMar","right", right);
        DocxUtil.addElement(sectPr,"pgMar","bottom", bottom);
        DocxUtil.addElement(sectPr,"pgMar","left", left);
        DocxUtil.addElement(sectPr,"pgMar","header", header);
        DocxUtil.addElement(sectPr,"pgMar","footer", footer);

    }
    public static void removeAttribute(Element element, String attribute){
        Attribute att = element.attribute("w:"+attribute);
        if(null != att){
            element.remove(att);
        }
    }

    public static void removeContent(Element parent){
        List<Element> ts = DomUtil.elements(parent,"t");
        for(Element t:ts){
            t.getParent().remove(t);
        }
        List<Element> imgs = DomUtil.elements(parent,"drawing");
        for(Element img:imgs){
            img.getParent().remove(img);
        }
        List<Element> brs = DomUtil.elements(parent,"br");
        for(Element br:brs){
            br.getParent().remove(br);
        }
    }
    public static void removeElement(Element parent, String element){
        List<Element> elements = DomUtil.elements(parent, element);
        for(Element item:elements){
            item.getParent().remove(item);
        }
    }
    /**
     * 宽度计算
     * @param src width
     * @return dxa
     */
    public static int dxa(String src){
        int dxa = 0;
        if(null != src){
            src = src.trim().toLowerCase();
            if(src.endsWith("px")){
                src = src.replace("px","");
                dxa = px2dxa(BasicUtil.parseInt(src,0));
            }else if(src.endsWith("cm")){
                src = src.replace("cm","");
                dxa = cm2dxa(BasicUtil.parseDouble(src,0d));
            }else if(src.endsWith("厘米")){
                src = src.replace("厘米","");
                dxa = cm2dxa(BasicUtil.parseDouble(src,0d));
            }else if(src.endsWith("pt")){
                src = src.replace("pt","");
                dxa = pt2dxa(BasicUtil.parseInt(src,0));
            }else if(src.endsWith("%")){
                dxa = (int)(BasicUtil.parseDouble(src.replace("%",""),0d)/100*5000);
            }else if(src.endsWith("dxa")){
                dxa = BasicUtil.parseInt(src.replace("dxa",""),0);
            }else{
                dxa = px2dxa(BasicUtil.parseInt(src,0));
            }
        }
        return dxa;
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
    public static final int px2dxa(double px){
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

    public static final int cm2dxa(double cm) {
        return px2dxa(cm2px(cm));
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
