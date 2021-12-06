package org.anyline.office.docx.entity;

import org.anyline.net.HttpUtil;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.office.docx.util.StyleParser;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.ElementPath;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.action.GetPropertyAction;

import java.io.File;
import java.security.AccessController;
import java.util.*;

public class Document {
    private static Logger log = LoggerFactory.getLogger(Document.class);
    private File file;
    private String xml = null;      //document.xml文本
    //word/document.xml
    private org.dom4j.Document doc = null;
    private Element body = null;

    //word/_rels/document.xml.rels
    private String relsXml = null;
    private org.dom4j.Document relsDoc;

    private Map<String, Map<String,String>> styles = new HashMap<String, Map<String,String>>();
    private Map<String,String> replaces = new HashMap<String,String>();
    private int listNum = 0;



    public Document(File file){
        this.file = file;
    }
    public Document(String file){
        this.file = new File(file);
    }

    private void load(){
        xml = ZipUtil.read(file,"word/document.xml");
        relsXml = ZipUtil.read(file,"word/_rels/document.xml.rels");
    }
    public void reload(){
        load();
    }
    public void flush(){
        try {
            xml = DomUtil.format(doc);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public void loadStyle(String html){
        Map<String,Map<String,String>> map = StyleParser.load(html);
        for(String key:map.keySet()){
            this.styles.put(key, map.get(key));
        }

    }
    public void replace(String key, String content){
        if(null == key && key.trim().length()==0){
            return;
        }
        replaces.put(key, content);
    }


    /**
     * 替换书签
     * @param start 开始书签
     */
    private void replaceBookmark(Element start){
        String id = start.attributeValue("id");
        Element end =  DomUtil.element(body, "bookmarkEnd","id",id);
        String name = start.attributeValue("name");
        String content = replaces.get(name);
        boolean isblock = DocxUtil.isBlock(content);
        Element startP = start.getParent();
        Element endP = end.getParent();
        if(isblock){
            if(startP == endP){
                //结束标签拆分到下一段落
                //<start.p><content.p><end.p>
                Element nEndP = startP.getParent().addElement("w:p");
                endP.elements().remove(end);
                nEndP.elements().add(end);
                DocxUtil.after(nEndP, startP);
            }
            DomUtil.remove(startP, DomUtil.afters(start,"r"));
            DomUtil.remove(endP, DomUtil.befores(end,"r"));
            parseHtml(startP.getParent(),startP,content);
        }else{
            if(startP == endP){
                DomUtil.remove(startP,DomUtil.betweens(start, end,"r"));
                parseHtml(startP,startP,content);
            }else{
                DomUtil.remove(startP, DomUtil.afters(start,"r"));
                DomUtil.remove(endP, DomUtil.befores(end,"r"));
                parseHtml(startP,startP,content);
            }
        }
    }
    public Element pr(Element element, Map<String,String> styles){
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
                    DocxUtil.addElement(pr, "ind", "left",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("margin-right")){
                    DocxUtil.addElement(pr, "ind", "right",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("margin-top")){
                    DocxUtil.addElement(pr, "spacing", "before",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("margin-bottom")){
                    DocxUtil.addElement(pr, "spacing", "after",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-left")){
                    DocxUtil.addElement(pr, "ind", "left",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-right")){
                    DocxUtil.addElement(pr, "ind", "right",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-top")){
                    DocxUtil.addElement(pr, "spacing", "before",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("padding-bottom")){
                    DocxUtil.addElement(pr, "spacing", "after",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("text-indent")){
                    DocxUtil.addElement(pr, "ind", "firstLine",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("line-height")){
                    DocxUtil.addElement(pr, "spacing", "line",DocxUtil.width(sv)+"");
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
                }
            }
            Element border = DocxUtil.addElement(pr, "bdr");
            DocxUtil.border(border, styles);
            DocxUtil.font(pr, styles);
        }else if("tbl".equalsIgnoreCase(name)){

            DocxUtil.addElement(pr,"tblCellSpacing","w","0");
            DocxUtil.addElement(pr,"tblCellSpacing","type","nil");

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
                    DocxUtil.addElement(pr,"tblW","w", DocxUtil.width(sv)+"");
                    DocxUtil.addElement(pr,"tblW","type", DocxUtil.widthType(sv));
                }else if(sk.equalsIgnoreCase("color")){
                }else if(sk.equalsIgnoreCase("margin-left")){
                    DocxUtil.addElement(pr,"tblInd","w",DocxUtil.width(sv)+"");
                    DocxUtil.addElement(pr,"tblInd","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-left")){
                    DocxUtil.addElement(mar,"left","w",DocxUtil.width(sv)+""); //新版本用start,但07版本用start会报错
                    DocxUtil.addElement(mar,"left","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-right")){
                    DocxUtil.addElement(mar,"right","w",DocxUtil.width(sv)+""); //新版本用end
                    DocxUtil.addElement(mar,"right","type","dxa");
                    DocxUtil.addElement(mar,"end","w",DocxUtil.width(sv)+"");
                    DocxUtil.addElement(mar,"end","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-top")){
                    DocxUtil.addElement(mar,"top","w",DocxUtil.width(sv)+"");
                    DocxUtil.addElement(mar,"top","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-bottom")){
                    DocxUtil.addElement(mar,"bottom","w",DocxUtil.width(sv)+"");
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
                    DocxUtil.addElement(pr,"trHeight","val",(int)DocxUtil.dxa2pt(DocxUtil.width(sv))*20+"");
                }else if("height".equalsIgnoreCase(sk)){
                    DocxUtil.addElement(pr,"trHeight","hRule","exact");
                    DocxUtil.addElement(pr,"trHeight","val",(int)DocxUtil.dxa2pt(DocxUtil.width(sv))*20+"");
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
                    DocxUtil.addElement(pr,"tcW","w",DocxUtil.width(sv)+"");
                    DocxUtil.addElement(pr,"tcW","type",DocxUtil.widthType(sv));
                }else if(sk.equalsIgnoreCase("padding-left")){
                    DocxUtil.addElement(mar,"left","w",DocxUtil.width(sv)+""); //新版本用start,但07版本用start会报错
                    DocxUtil.addElement(mar,"left","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-right")){
                    DocxUtil.addElement(mar,"right","w",DocxUtil.width(sv)+""); //新版本用end
                    DocxUtil.addElement(mar,"right","type","dxa");
                    DocxUtil.addElement(mar,"end","w",DocxUtil.width(sv)+"");
                    DocxUtil.addElement(mar,"end","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-top")){
                    DocxUtil.addElement(mar,"top","w",DocxUtil.width(sv)+"");
                    DocxUtil.addElement(mar,"top","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-bottom")){
                    DocxUtil.addElement(mar,"bottom","w",DocxUtil.width(sv)+"");
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

    private List<Element> parseHtml(Element box, Element next, String html){
        List<Element> list = new ArrayList<Element>();
        if(null == html || html.trim().length()==0){
            return list;
        }
        //抽取style
        this.styles.clear();
        List<String> styles = RegularUtil.cuts(html,true,"<style",">","</style>");
        for(String style:styles){
            loadStyle(style);
            html = html.replace(style,"");
        }
        try {
            html = html.replace("&","&amp;");
            html = "<body>" + html + "</body>";
            org.dom4j.Document doc = DocumentHelper.parseText(html);
            Element root = doc.getRootElement();
            parseHtml(box, next, root, null);
        }catch (Exception e){
            e.printStackTrace();
            //log.error(html);
        }
        return list;
    }

    public Element parent(Element element, String ... tags){
        Element parent = null;
        if(contains(tags, element.getName())){
            return element;
        }
        parent = element.getParent();
        if(null == parent){
            return null;
        }
        while(!contains(tags, parent.getName())){
            parent = parent.getParent();
            if(null == parent){
                break;
            }
        }
        return parent;
    }

    /**
     * 找到到当前p的上一级(用来创建与当前所在p平级的新p,遇到tc停止)
     * @param element 当前节点
     * @return Element
     */
    public Element pp(Element element){
        String tag = "p";
        Element parent = null;
        if(element.getName().equalsIgnoreCase(tag)){
            parent = element.getParent();
        }else if(element.getName().equalsIgnoreCase("tc")){
            parent = element;
        }else {
            parent = element.getParent();
            while(true){
                if(null == parent){
                    return doc.getRootElement().element("body");
                }else if(parent.getName().equalsIgnoreCase(tag)){
                    parent = parent.getParent();
                    break;
                }else if(parent.getName().equalsIgnoreCase("tc")){
                    break;
                }
                parent = parent.getParent();
            }
        }
        return parent;
    }
    public Element p(Element element){
        return parent(element, "p");

    }
    private boolean contains(String[] list, String item){
        for(String i:list){
            if(i.equalsIgnoreCase(item)){
                return true;
            }
        }
        return false;
    }
    //当element位置开始把parent拆分(element不一定是parent直接子级)
    private void split(Element element){
        Element parent = parent(element,"p");
        int pindex = DocxUtil.index(parent);
        split(parent.getParent(), parent, element, pindex);
    }
    private Element split(Element box, Element parent, Element stop, int index){
        Element element = null;
        if(null != parent){
            List<Element> elements = parent.elements();
            for(Element item:elements){

            }
        }
        return element;
    }



    public Element table(Element box, Element after, Element src){

        Element tbl = body.addElement("w:tbl");
        Element tblPr = tbl.addElement("w:tblPr");

        Table table = new Table();
        Map<String,String> styles = style(src);
        pr(tbl, styles);
        table.setStyles(styles);
        List<Element> html_rows = src.elements("tr");
        for(Element row:html_rows){
            Tr tr = new Tr();
            tr.setStyles(style(row));
            table.addTr(tr);
        }
        int rows_size = html_rows.size();
        int cols_size = 0;
        if(rows_size>0){
            Element html_row = html_rows.get(0);
            List<Element> cols = html_row.elements("td");
            for(Element col:cols){
                int colspan = BasicUtil.parseInt(col.attributeValue("colspan"), 1);
                cols_size += colspan;
            }
        }
        Td[][] cells = new Td[rows_size][cols_size];
        for(int r=0; r<rows_size; r++) {
            Tr tr = table.getTr(r);
            for (int c = 0; c < cols_size; c++) {
                Td td = new Td();
                cells[r][c] = td;
                tr.addTd(td);
            }
        }

        for(int r=0; r<rows_size; r++) {
            Element html_row = html_rows.get(r);
            String row_style = html_row.attributeValue("style");
            List<Element> cols = html_row.elements("td");
            int tcIndex = 0;
            for(int tdIndex = 0; tdIndex<cols.size(); tdIndex++,tcIndex++){
                Element html_col = cols.get(tdIndex);
                String text = html_col.getTextTrim();
                Td tc = cells[r][tcIndex];
                int merge_qty = 0;
                while(!tc.isEmpty()){
                    merge_qty ++;
                    tc = cells[r][tcIndex+merge_qty];
                }
                tc.setSrc(html_col);
                tc.setText(text);
                Map<String,String> tdStyles = StyleParser.merge(tc.getStyles(),style(html_col));
                tdStyles = StyleParser.parse(tdStyles, html_col.attributeValue("style"), true);
                tc.setStyles(tdStyles);
                tc.setClazz(html_col.attributeValue("class"));
                int rowspan = BasicUtil.parseInt(html_col.attributeValue("rowspan"), 1);
                int colspan = BasicUtil.parseInt(html_col.attributeValue("colspan"),1);

                if(rowspan > 1){
                    tc.setMerge(1);
                    for(int i=r+1; i<=r+rowspan-1; i++){
                        for(int j=tcIndex+1; j<tcIndex+colspan; j++){
                            Td merge = cells[i][j];
                            merge.setRemove(true);//被上一列合并
                        }
                        Td merge = cells[i][tcIndex+merge_qty];
                        merge.setMerge(2);//被上一行合并
                    }
                }
                if(colspan > 1){
                    tc.setColspan(colspan);
                    for(int j=r; j<r+rowspan; j++){
                        if(j>r) {
                            Td merge = cells[j][tcIndex];
                            merge.setMerge(2);//被上一行合并
                            merge.setColspan(colspan);
                        }
                        for(int i=tcIndex+1; i<tcIndex+colspan; i++){
                            Td cur = cells[j][i];
                            cur.setRemove(true);//被上一列合并
                        }

                    }
                }
                tcIndex += colspan-1;
            }
        }
        Element word = src.element("word");
        if(null != word){
            word(tbl, null, word, styles);
        }
        for(int r=0; r<rows_size; r++) {
            Tr tr = table.getTr(r);
            tr(tbl, tr);
        }
        DocxUtil.after(tbl, after);

        return tbl;
    }

    public Element tr(Element parent, Tr tr){
        Element etr = parent.addElement("w:tr");
        Map<String,String> styles = StyleParser.inherit(tr.getStyles(), tr.getTable().getStyles());
        tr.setStyles(styles);
        pr(etr, tr.getStyles());
        for (Td td:tr.getTds()) {
            Element tc = tc(etr, td);
        }
        return etr;
    }
    public Element tc(Element parent, Td td){
        Element tc = null;
        int merge = td.getMerge(); //0:不合并 1:向下合并(restart) 2:被合并(continue)
        int colspan = td.getColspan(); //向右合并
        boolean remove = td.isRemove(); //被左侧合并
        if(!remove){
            tc = parent.addElement("w:tc");
            Element tcPr = DocxUtil.addElement(tc, "tcPr");
            if(merge > 0){
                Element vMerge = tcPr.addElement("w:vMerge");//被上一行合并
                if(merge == 1) {//向下合并
                    vMerge.addAttribute("w:val", "restart");
                }
            }
            if(colspan >1){
                Element span = tcPr.addElement("w:gridSpan");
                span.addAttribute("w:val", colspan+"");
            }
            if(tcPr.elements().size()==0){
                //tc.remove(tcPr);
            }

            Map<String, String> styles = StyleParser.inherit(td.getStyles(), td.getTr().getStyles());
            pr(tc, styles);
            if(merge !=2){
                if(null != td.getSrc()) {
                    Element p = tc.addElement("w:p");
                    parseHtml(p, null, td.getSrc(), StyleParser.inherit(null, styles));
                }
            }else{
                p(tc,"",null);
            }
        }
        return  tc;
    }
    private Element inline(Element parent, Element next, String text, Map<String, String> styles){
        String pname = parent.getName();
        Element r;
        if(pname.equalsIgnoreCase("r")){
            r = parent;
            pr(parent, styles);
            DocxUtil.after(r, next);
        }else if(pname.equalsIgnoreCase("tc")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(r, next);
        }else if(pname.equalsIgnoreCase("p")){
            pr(parent, styles);
            r = parent.addElement("w:r");
            //DocxUtil.after(r, next);
        }else if(pname.equalsIgnoreCase("body")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(p, next);
        }else{
            throw new RuntimeException("text.parent异常:"+parent.getName());
        }
        pr(r, styles);
        Element t = r.addElement("w:t");
        t.setText(text.trim());
        return r;
    }
    private Element block(Element parent, Element next, Element element, Map<String,String> styles){
        Element box = null;
        String pname = parent.getName();
        Element newNext = null;
        pr(parent, styles);
        if(pname.equalsIgnoreCase("p")){
            box = parent.addElement("w:r");
            next = box.addElement("w:br");
            DocxUtil.after(box, next);
            newNext = parent;
        }else if(pname.equalsIgnoreCase("r")){
            box = parent.getParent().addElement("w:r");
            next = box.addElement("w:br");
            DocxUtil.after(box, next);
            newNext = parent.getParent();
        }else if(pname.equalsIgnoreCase("tc")){
            box = parent.addElement("w:p");
            DocxUtil.after(box, next);
            newNext = box;
        }else if(pname.equalsIgnoreCase("body")){
            box = parent.addElement("w:p");
            newNext = box;
            DocxUtil.after(box, next);

        }else{
            throw new RuntimeException("div.parent 异常:"+pname+":"+element.getName()+":"+element.getTextTrim());
            //新建一个段落
        }
        pr(box, styles);
        parseHtml(box, next, element, styles);
        return newNext;
    }
    private Element ol(Element parent, Element next, Element element, Map<String,String> styles){
        styles = StyleParser.parse(styles, element.attributeValue("style"), true);
        if(!DocxUtil.hasParent(element, "ol")){
            listNum ++;//新一组编号
        }
        List<Element> lis = element.elements();
        for(Element li:lis){
            String liName = li.getName();
            if(liName.equalsIgnoreCase("ol")) {
                next = ol(body, next, li, styles);
            }else{
                next = li(body, next, li, styles);
            }
        }
        return next;
    }
    private List<Map<String,String>> lis(Element parent){
        List<Map<String,String>> lis = new ArrayList<Map<String,String>>();
        Iterator<Node> nodes = parent.nodeIterator();
        while(nodes.hasNext()){
            Node node = nodes.next();
            int type = node.getNodeType();
            if(type ==3){

            }else if(type ==1){
                Element element = (Element)node;
                String tag = element.getName();
                if(tag.equalsIgnoreCase("li")){
                    Map<String,String> li = new HashMap<String,String>();
                    li.put("tag",tag);
                    lis.add(li);
                }
            }
        }
        return lis;
    }
    private Element li(Element parent, Element next, Element element, Map<String,String> styles){
        Element box = parent.addElement("w:p");
        int lvl = lvl(element);
        styles.put("list-lvl",lvl+"");
        styles.put("list-num", listNum+"");
        pr(box, styles);
        DocxUtil.after(box, next);
        next = parseHtml(box, next, element, styles);
        return next;
    }
    private int lvl(Element li){
        int lvl = -1;
        while(true){
            li = li.getParent();
            if(li == null){
                break;
            }
            if(li.getName().equalsIgnoreCase("ol")){
                lvl ++;
            }
        }
        return lvl;
    }
    private Element word(Element parent, Element next, Element element, Map<String, String> styles){
        String path = element.getTextTrim();
        String bookmark = element.attributeValue("bookmark");
        return word(parent, next, new File(path), bookmark, styles);
    }
    private Element word(Element parent, Element next, File word, String bookmark, Map<String, String> styles){
        Element newNext = null;
        String wxml = ZipUtil.read(word,"word/document.xml");
        try {
            Element wbody =  DocumentHelper.parseText(wxml).getRootElement().element("body");
            List<Element> elements = null;
            if(null != bookmark){
                Element start = DocxUtil.bookmark(wbody, bookmark);
                elements = DocxUtil.betweens(start,"tr","tblGrid","p");
            }else{
                elements =  wbody.elements();
            }
            if(null != elements) {
                for (Element element : elements) {
                    String name = element.getName();
                    if (!"sectPr".equalsIgnoreCase(name)) {
                        element.getParent().remove(element);
                        if(parent.getName().equalsIgnoreCase(element.getName())){
                            addElements(parent, element.elements(), true);
                        }else {
                            parent.elements().add(element);
                        }
                        newNext = element;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return newNext;
    }
    private void addElements(Element parent, List<Element> elements, boolean over){
        for(Element element:elements){
            String name = element.getName();
            element.getParent().remove(element);
            List<Element> exists = DomUtil.elements(parent, name, false);
            if(null != exists && exists.size()>0){
                if(over){
                    DomUtil.remove(parent, exists);
                    parent.elements().add(element);
                }
            }else{
                parent.elements().add(element);
            }
        }
    }

    private Element img(Element parent, Element next, Element element, Map<String, String> styles){
        String pname = parent.getName();
        Element r;
        if(pname.equalsIgnoreCase("r")){
            r = parent;
            pr(parent, styles);
            DocxUtil.after(r, next);
        }else if(pname.equalsIgnoreCase("tc")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(r, next);
        }else if(pname.equalsIgnoreCase("p")){
            pr(parent, styles);
            r = parent.addElement("w:r");
            //DocxUtil.after(r, next);
        }else if(pname.equalsIgnoreCase("body")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(p, next);
        }else{
            throw new RuntimeException("text.parent异常:"+parent.getName());
        }

        styles = StyleParser.inherit(style(element), styles);
        pr(r, styles);
        String widthType = DocxUtil.widthType(styles.get("width"));
        int width = 0;
        if("pct".equalsIgnoreCase(widthType)) {
        }else{
            width = DocxUtil.px2emu((int)DocxUtil.dxa2px(DocxUtil.width(styles.get("width"))));
        }
        String heightType = DocxUtil.widthType(styles.get("height"));
        int height = 0;
        if("pct".equalsIgnoreCase(heightType)) {
        }else{
            height = DocxUtil.px2emu((int)DocxUtil.dxa2px(DocxUtil.width(styles.get("height"))));
        }

        String rdm = System.currentTimeMillis()+"";
        String rId = "rId"+rdm;
        String src = element.attributeValue("src");
        String subfix = element.attributeValue("type");

        if(null == subfix) {
            int idx = src.lastIndexOf(".");
            if (idx != -1) {
                subfix = src.substring(idx + 1);
            } else {
                subfix = "jpg";
            }
            if (subfix.length() > 20) {
                subfix = "jpeg";
            }
        }
        File tmpdir = new File(AccessController.doPrivileged(new GetPropertyAction("java.io.tmpdir")));
        File img = new File(tmpdir,"image" + rdm + "." + subfix);
        try {
            //下载文件
            HttpUtil.download(src, img);
            Map<String,File> map = new HashMap<>();
            map.put("word/media/"+img.getName(),img);
            ZipUtil.append( map,file);
            img.delete();
            //创建文件资源引用
            Element relRoot = relsDoc.getRootElement();
            Element imgRel = relRoot.addElement("Relationship");
            imgRel.addAttribute("Id",rId);
            imgRel.addAttribute("Type","http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
            imgRel.addAttribute("Target","media/"+img.getName());
        }catch (Exception e){
            e.printStackTrace();
        }

        Element draw = r.addElement("w:drawing");
        Element inline = draw.addElement("wp:inline");
        inline.addAttribute("distT","0");
        inline.addAttribute("distB","0");
        inline.addAttribute("distL","0");
        inline.addAttribute("distR","0");
        Element extent = inline.addElement("wp:extent");
        extent.addAttribute("cx", width+"");
        extent.addAttribute("cy", height+"");
        Element effectExtent = inline.addElement("wp:effectExtent"); //边距
        effectExtent.addAttribute("l","0");
        effectExtent.addAttribute("t","0");
        effectExtent.addAttribute("r","0");
        effectExtent.addAttribute("b","0");
        Element docPr = inline.addElement("wp:docPr");
        int docPrId = NumberUtil.random(0,100);
        docPr.addAttribute("id", docPrId+"");
        docPr.addAttribute("name", "图片"+rdm);
        docPr.addAttribute("descr", img.getName());
        Element cNvGraphicFramePr = inline.addElement("wp:cNvGraphicFramePr");
        Element graphicFrameLocks = cNvGraphicFramePr.addElement("a:graphicFrameLocks","http://schemas.openxmlformats.org/drawingml/2006/main");
        graphicFrameLocks.addAttribute("xmlns:a","http://schemas.openxmlformats.org/drawingml/2006/main");
        graphicFrameLocks.addAttribute("noChangeAspect","1");
        Element graphic = inline.addElement("a:graphic","http://schemas.openxmlformats.org/drawingml/2006/main");
        graphic.addAttribute("xmlns:a","http://schemas.openxmlformats.org/drawingml/2006/main");
        Element graphicData = graphic.addElement("a:graphicData");
        graphicData.addAttribute("uri","http://schemas.openxmlformats.org/drawingml/2006/picture");
        Element pic = graphicData.addElement("pic:pic","http://schemas.openxmlformats.org/drawingml/2006/picture");
        pic.addAttribute("xmlns:pic","http://schemas.openxmlformats.org/drawingml/2006/picture");
        Element nvPicPr = pic.addElement("pic:nvPicPr");
        Element cNvPr = nvPicPr.addElement("pic:cNvPr");
        cNvPr.addAttribute("id",docPrId+"");
        cNvPr.addAttribute("name",img.getName());
        Element cNvPicPr = nvPicPr.addElement("pic:cNvPicPr");
        Element blipFill = pic.addElement("pic:blipFill");
        Element blip = blipFill.addElement("a:blip");
        blip.addAttribute("r:embed", rId);     //图片资源编号
        Element stretch = blipFill.addElement("a:stretch");
        Element fillRect = stretch.addElement("a:fillRect");
        Element spPr = pic.addElement("pic:spPr");

        Element xfrm = spPr.addElement("a:xfrm");
        Element off = xfrm.addElement("a:off");
        off.addAttribute("x","0");
        off.addAttribute("y","0");
        Element ext = xfrm.addElement("a:ext");
        ext.addAttribute("cx", width+"");
        ext.addAttribute("cy", height+"");
        Element prstGeom = spPr.addElement("a:prstGeom");
        prstGeom.addAttribute("prst","rect");
        prstGeom.addElement("a:avLst");

        return r;

    }
    public Element parseHtml(Element parent, Element next, Element html, Map<String,String> styles){
        String pname = parent.getName();
        String txt = html.getTextTrim().replace("&nbsp;"," ");
        if(html.elements().size()==0){
            //txt = txt.replaceAll("\\s","");
            txt = txt.trim();
            html.setText(txt);
        }
        Iterator<Node> nodes = html.nodeIterator();
        while (nodes.hasNext()){
            Node node = nodes.next();
            String tag = node.getName();
            int type = node.getNodeType();
            //Element:1 Attribute:2 Text:3 CDATA:4 Entity:5 Comment:8 Document:9
            if(type == 3){//text
                String text = node.getText().trim();
                if(text.length()>0) {
                   Element r = inline(parent, next, text, styles);
                   next = r;
                }
            }else if(type == 1 ) {//element
                Element element = (Element) node;
                Map<String,String> itemStyles = StyleParser.inherit(style(element),styles);
                String display = itemStyles.get("display");
                if("none".equalsIgnoreCase(display)){
                    continue;
                }
                if("table".equalsIgnoreCase(tag)){
                    Element box = null;
                    if(pname.equalsIgnoreCase("tc")){
                        box = parent;
                        pr(box, styles);
                    }else if(pname.equalsIgnoreCase("p")){
                        box = parent.addElement("w:r");
                    }else{
                        box = doc.getRootElement().element("body");
                        //新建一个段落
                    }
                    Element tbl = table(box, next, element);
                    next = tbl;
                }else if("div".equalsIgnoreCase(tag)){
                    if("inline".equalsIgnoreCase(display) || "inline-block".equalsIgnoreCase(display)){
                        next = parseHtml(parent, next, element, itemStyles);
                    }else {
                        next = block(parent, next, element, itemStyles);
                    }
                }else if("span".equalsIgnoreCase(tag)){
                    if("block".equalsIgnoreCase(display)){
                        next = block(parent, next, element, itemStyles);
                    }else {
                        next =  parseHtml(parent, next, element, itemStyles);
                    }
                }else if("img".equalsIgnoreCase(tag)){
                    Element img = img(parent, next, element, styles);
                    next = img;
                }else if("word".equalsIgnoreCase(tag)){
                    Element word = word(parent, next, element, styles);
                    next = word;
                }else if("ol".equalsIgnoreCase(tag)){
                    next = ol(body, next, element, itemStyles);
                }else if("li".equalsIgnoreCase(tag)){
                    next = li(body, next, element, itemStyles);
                }else if("br".equalsIgnoreCase(tag)){
                    parent.addElement("w:br");
                }else if("u".equalsIgnoreCase(tag)){
                    itemStyles.put("underline","true");
                    next = parseHtml(parent, next, element, itemStyles);
                }else if("b".equalsIgnoreCase(tag)){
                    itemStyles.put("font-weight","700");
                    next = parseHtml(parent, next, element, itemStyles);
                }else if("i".equalsIgnoreCase(tag)){
                    itemStyles.put("italics","true");
                    next = parseHtml(parent, next, element, itemStyles);
                }else if("del".equalsIgnoreCase(tag)){
                    itemStyles.put("dstrike","true");
                    next = parseHtml(parent, next, element, itemStyles);
                }else{
                    next = parseHtml(parent, next, element, itemStyles);
                }
            }
        }
        return next;
    }
    public Element p(Element parent, String text, Map<String,String> styles){
        while(parent.getName().equalsIgnoreCase("p")){
            parent = parent.getParent();
        }
        Element p = parent.addElement("w:p");
        pr(p, styles);
        if(null != text && text.trim().length()>0) {
            Element r = r(p, text, styles);
        }
        return p;
    }
    public Element r(Element parent, String text, Map<String,String> styles){
        Element r= null;
        if(null != text && text.trim().length()>0) {
            r = parent.addElement("w:r");
            pr(r, styles);
            Element t = r.addElement("w:t");
            t.setText(text);
        }
        return r;
    }
    public Map<String,String> style(Element element){
        int index = element.getParent().elements(element.getName()).indexOf(element);
        String nth = ":nth-child(even)";
        if((index+1)%2 ==0){
            nth = ":nth-child(odd)";
        }
        Map<String,String> result = new HashMap<String,String>();
        if(null == element){
            return result;
        }
        String parentName = null;
        List<String> parentClassList = new ArrayList<>();
        Element parent = element.getParent();
        if(null != parent){
            parentName = parent.getName();
            String parentClass = parent.attributeValue("class");
            if(null != parentClass){
                String[] tmps = parentClass.trim().split(" ");
                for(String tmp:tmps){
                    tmp = tmp.trim();
                    if(tmp.length()>0){
                        parentClassList.add(tmp);
                    }
                }
            }
        }
        String tag = element.getName();
        if(tag.equalsIgnoreCase("table")){
            result = StyleParser.parse(result, "border:1px solid auto;");
        }else if(tag.equalsIgnoreCase("td")){
            result = StyleParser.parse(result, "vertical-align:center;");
        }

        String name = element.getName();



        for(String pc:parentClassList){
            StyleParser.merge(result, this.styles.get("."+pc + " "+name), true);
            StyleParser.merge(result, this.styles.get("."+pc + " "+name+nth), true);
        }

        if(null != parentName){
            StyleParser.merge(result, this.styles.get(parentName + " "+name), true);
            StyleParser.merge(result, this.styles.get(parentName + " "+name+nth), true);
        }

        StyleParser.merge(result, this.styles.get(name), true);
        StyleParser.merge(result, this.styles.get(name+nth), true);

        String clazz = element.attributeValue("class");
        if(null != clazz){
            String[] cs = clazz.split(" ");
            for(String c:cs){
                if(null != parentName){
                    StyleParser.merge(result, this.styles.get(parentName + " ."+c), true);
                    StyleParser.merge(result, this.styles.get(parentName + " ."+c+nth), true);
                }
                for(String pc:parentClassList){
                    StyleParser.merge(result, this.styles.get("."+pc + " ."+c), true);
                    StyleParser.merge(result, this.styles.get("."+pc + " ."+nth), true);
                }
                StyleParser.merge(result, this.styles.get("."+c), true);
                StyleParser.merge(result, this.styles.get("."+c+nth), true);
            }
        }


        String id = element.attributeValue("id");
        if(null != id){
            StyleParser.merge(result, this.styles.get("#"+id),true);
        }

        result = StyleParser.parse(result, element.attributeValue("style"),true);
        return result;
    }
    public String listStyle(String key){
        return DocxUtil.listStyle(file, key);
    }


    public List<String> listStyles(){
        return DocxUtil.listStyles(file);
    }
    public void save(){
        try {
            load();
            doc = DocumentHelper.parseText(xml);
            relsDoc = DocumentHelper.parseText(relsXml);
            body = doc.getRootElement().element("body");

            List<Element> ts = DomUtil.elements(body, "t");
            for(Element t:ts){
                String txt = t.getTextTrim();
                List<String> flags = DocxUtil.splitKey(txt);
                if(flags.size() == 0){
                    continue;
                }
                Collections.reverse(flags);
                Element r = t.getParent();
                List<Element> elements = r.elements();
                int index = elements.indexOf(t);
                Element next = null;
                if(index < elements.size()-1){
                    next = elements.get(index+1);
                }
                for(int i=0; i<flags.size(); i++){
                    String flag = flags.get(i);
                    String content = flag;
                    String key = null;
                    if(flag.startsWith("${") && flag.endsWith("}")) {
                        key = flag.substring(2, flag.length() - 1);
                        content = replaces.get(key);
                    }
                    //boolean isblock = DocxUtil.isBlock(content);
                    //Element p = t.getParent();
                    /*if(null != key && DocxUtil.isEmpty(p, t) && !DocxUtil.hasParent(t,"tc")){
                        next = DocxUtil.prev(body, p);
                        body.remove(p);
                        List<Element> list = parseHtml(body, next ,content);
                    }else{
                        List<Element> list = parseHtml(r, next ,content);
                    }*/
                    List<Element> list = parseHtml(r, next ,content);
                }
                elements.remove(t);
            }
            List<Element> bookmarks = DomUtil.elements(body, "bookmarkStart");
            for(Element bookmark:bookmarks){
                replaceBookmark(bookmark);
            }
            checkContentTypes();
            ZipUtil.replace(file,"word/document.xml", DomUtil.format(doc));
            ZipUtil.replace(file,"word/_rels/document.xml.rels", DomUtil.format(relsDoc));

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void checkContentTypes(){
        try {
            String xml = ZipUtil.read(file,"[Content_Types].xml");
            org.dom4j.Document doc = DocumentHelper.parseText(xml);
            Element root = doc.getRootElement();
            checkContentTypes(root, "png","image/png");
            checkContentTypes(root, "jpg","image/jpeg");
            checkContentTypes(root, "jpeg","image/jpeg");
            checkContentTypes(root, "gif","image/gif");
            checkContentTypes(root, "tiff","image/tiff");
            checkContentTypes(root, "pict","image/pict");
            ZipUtil.replace(file,"[Content_Types].xml", DomUtil.format(doc));
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void checkContentTypes(Element root, String extension, String type){
        List<Element> elements = root.elements("Default");
        for(Element element:elements){
            if(extension.equals(element.attributeValue("Extension"))){
                return;
            }
        }
        Element element = root.addElement("Default");
        element.addAttribute("Extension", extension);
        element.addAttribute("ContentType", type);
    }

}
