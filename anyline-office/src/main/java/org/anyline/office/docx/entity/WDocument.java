package org.anyline.office.docx.entity;

import org.anyline.net.HttpUtil;
import org.anyline.office.docx.entity.html.Table;
import org.anyline.office.docx.entity.html.Td;
import org.anyline.office.docx.entity.html.Tr;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.office.docx.util.StyleParser;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.action.GetPropertyAction;

import java.io.File;
import java.security.AccessController;
import java.util.*;

public class WDocument {
    private static Logger log = LoggerFactory.getLogger(WDocument.class);
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

    public Element getBody() {
        return body;
    }

    public void setBody(Element body) {
        this.body = body;
    }

    public WDocument(File file){
        this.file = file;
    }
    public WDocument(String file){
        this.file = new File(file);
    }

    private void load(){
        if(null == xml){
            try {
                xml = ZipUtil.read(file, "word/document.xml");
                relsXml = ZipUtil.read(file, "word/_rels/document.xml.rels");
                doc = DocumentHelper.parseText(xml);
                relsDoc = DocumentHelper.parseText(relsXml);
                body = doc.getRootElement().element("body");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
    public void reload(){
        try {
            xml = ZipUtil.read(file, "word/document.xml");
            relsXml = ZipUtil.read(file, "word/_rels/document.xml.rels");
            doc = DocumentHelper.parseText(xml);
            relsDoc = DocumentHelper.parseText(relsXml);
            body = doc.getRootElement().element("body");
        }catch (Exception e){
            e.printStackTrace();
        }
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
    public void insert(Element parent, String html){
        parseHtml(parent, null, html);
    }
    public void insert(int index, Element parent, String html){
        List<Element> elements = parent.elements();
        if(index <=1 ){
            index = 1;
        }else if(index >= elements.size()){
            index = elements.size()-1;
        }
        Element prev = elements.get(index-1);
        parseHtml(parent, null, html);
    }
    public Element getParent(String bookmark, String tag){
        load();
        Element bk = DocxUtil.bookmark(doc.getRootElement(), bookmark);
        return DocxUtil.getParent(bk, tag);
    }
    public Wtable getTable(String bookmark){
        Element src = getParent(bookmark, "tbl");
        Wtable table = new Wtable(this, src);
        return table;
    }
    //插入排版方向
    public void setOrient(Element prev, String orient, Map<String,String> styles){
        int index = index(body, prev);
        Element p = body.addElement("w:p");
        Element pr = p.addElement("pPr");

        DocxUtil.setOrient(pr, orient, styles);

        List<Element> elements = body.elements();
        if(index > -1 && index <elements.size()-1){
            elements.remove(p);
            elements.add(index+1, p);
        }
    }
    public void setOrient(Element prev, String orient){
        setOrient(prev, orient, null);
    }
    //插入换页
    public void insertPageBreak(Element prev){
        int index = index(body, prev);
        Element p = body.addElement("w:p");

        p.addElement("w:r").addElement("w:br").addAttribute("w:type","page");
        p.addElement("w:r").addElement("w:lastRenderedPageBreak");

        List<Element> elements = body.elements();
        if(index > -1 && index <elements.size()-1){
            elements.remove(p);
            elements.add(index+1, p);
        }

    }
    public static int index(Element parent, Element element){
        int index = parent.indexOf(element);
        while(element.getParent() != parent){
            element = element.getParent();
            if(element.getParent() == parent) {
                index = parent.indexOf(element);
                break;
            }
        }
        return index;
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
        if(null == content){
            return;
        }
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
    public  Element pr(Element element, Map<String,String> styles){
        return DocxUtil.pr(element, styles);
    }

    private List<Element> parseHtml(Element box, Element prev, String html){
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
            html = "<body>" + html + "</body>";
            org.dom4j.Document doc = DocumentHelper.parseText(html);
            Element root = doc.getRootElement();
            parseHtml(box, prev, root, null);
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
                        for(int i=tcIndex+merge_qty+1; i<tcIndex+merge_qty+colspan; i++){
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
                    parseHtml(tc, null, td.getSrc(), StyleParser.inherit(null, styles));
                }
            }else{
                p(tc,"",null);
            }
        }
        return  tc;
    }
    private Element inline(Element parent, Element prev, String text, Map<String, String> styles){
        String pname = parent.getName();
        Element r;
        if(pname.equalsIgnoreCase("r")){
            r = parent;
            pr(parent, styles);
            DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("tc")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("p")){
            pr(parent, styles);
            r = parent.addElement("w:r");
            //DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("body")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(p, prev);
        }else{
            throw new RuntimeException("text.parent异常:"+parent.getName());
        }
        pr(r, styles);
        Element t = r.addElement("w:t");
        t.setText(text.trim());
        return r;
    }
    public Element block(Element parent, Element prev, Element element, Map<String,String> styles){
        Element box = null;
        String pname = parent.getName();
        Element newPrev = null;
        Element wp = null;
        pr(parent, styles);
        if(pname.equalsIgnoreCase("p")){
            box = parent.addElement("w:r");
            prev = box.addElement("w:br");
            DocxUtil.after(box, prev);
            newPrev = parent;
            wp = parent;
        }else if(pname.equalsIgnoreCase("r")){
            box = parent.getParent().addElement("w:r");
            prev = box.addElement("w:br");
            DocxUtil.after(box, prev);
            newPrev = parent.getParent();
            wp = newPrev;
        }else if(pname.equalsIgnoreCase("tc")){
            box = DocxUtil.addElement(parent,"p");
            DocxUtil.after(box, prev);
            newPrev = box;
            wp = box;
        }else if(pname.equalsIgnoreCase("body")){
            box = parent.addElement("w:p");
            newPrev = box;
            DocxUtil.after(box, prev);
            wp = box;
        }else{
            throw new RuntimeException("div.parent 异常:"+pname+":"+element.getName()+":"+element.getTextTrim());
            //新建一个段落
        }

        pr(box, styles);
        parseHtml(box, prev, element, styles);

        if(null != styles && null != styles.get("page-break-after")){
            //分页
            wp.addElement("w:r").addElement("w:br").addAttribute("w:type","page");
            wp.addElement("w:r").addElement("w:lastRenderedPageBreak");
        }
        return newPrev;
    }
    private Element ol(Element parent, Element prev, Element element, Map<String,String> styles){
        styles = StyleParser.parse(styles, element.attributeValue("style"), true);
        if(!DocxUtil.hasParent(element, "ol")){
            listNum ++;//新一组编号
        }
        List<Element> lis = element.elements();
        for(Element li:lis){
            String liName = li.getName();
            if(liName.equalsIgnoreCase("ol")) {
                prev = ol(body, prev, li, styles);
            }else{
                prev = li(body, prev, li, styles);
            }
        }
        return prev;
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
    private Element li(Element parent, Element prev, Element element, Map<String,String> styles){
        Element box = parent.addElement("w:p");
        int lvl = lvl(element);
        styles.put("list-lvl",lvl+"");
        styles.put("list-num", listNum+"");
        pr(box, styles);
        DocxUtil.after(box, prev);
        prev = parseHtml(box, prev, element, styles);
        return prev;
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
    private Element word(Element parent, Element prev, Element element, Map<String, String> styles){
        String path = element.getTextTrim();
        String bookmark = element.attributeValue("bookmark");
        return word(parent, prev, new File(path), bookmark, styles);
    }
    private Element word(Element parent, Element prev, File word, String bookmark, Map<String, String> styles){
        Element newPrev = null;
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
                        newPrev = element;
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return newPrev;
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

    private Element img(Element parent, Element prev, Element element, Map<String, String> styles){
        String pname = parent.getName();
        Element r;
        if(pname.equalsIgnoreCase("r")){
            r = parent;
            pr(parent, styles);
            DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("tc")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("p")){
            pr(parent, styles);
            r = parent.addElement("w:r");
            //DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("body")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(p, prev);
        }else{
            throw new RuntimeException("text.parent异常:"+parent.getName());
        }

        styles = StyleParser.inherit(style(element), styles);
        pr(r, styles);
        String widthType = DocxUtil.widthType(styles.get("width"));
        int width = 0;
        if("pct".equalsIgnoreCase(widthType)) {
        }else{
            width = DocxUtil.px2emu((int)DocxUtil.dxa2px(DocxUtil.dxa(styles.get("width"))));
        }
        String heightType = DocxUtil.widthType(styles.get("height"));
        int height = 0;
        if("pct".equalsIgnoreCase(heightType)) {
        }else{
            height = DocxUtil.px2emu((int)DocxUtil.dxa2px(DocxUtil.dxa(styles.get("height"))));
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

    /**
     * 解析html
     * @param parent 上一级
     * @param prev 放在prev之后
     * @param html html
     * @param styles 样式
     * @return prev
     */
    public Element parseHtml(Element parent, Element prev, Element html, Map<String,String> styles){
        String pname = parent.getName();
        String txt = html.getTextTrim();
        if(html.elements().size()==0){
            //txt = txt.replaceAll("\\s","");
            txt = txt.trim();
            html.setText(txt);
        }
        Iterator<Node> nodes = html.nodeIterator();
        boolean empty = true;
        while (nodes.hasNext()){
            Node node = nodes.next();
            String tag = node.getName();
            int type = node.getNodeType();
            //Element:1 Attribute:2 Text:3 CDATA:4 Entity:5 Comment:8 Document:9
            if(type == 3){//text
                String text = node.getText().trim();
                if(text.length()>0) {
                    empty = false;
                   Element r = inline(parent, prev, text, styles);
                    prev = r;
                }
            }else if(type == 1 ) {//element
                empty = false;
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
                    Element tbl = table(box, prev, element);
                    prev = tbl;
                }else if("div".equalsIgnoreCase(tag)){
                    if("inline".equalsIgnoreCase(display) || "inline-block".equalsIgnoreCase(display)){
                        prev = parseHtml(parent, prev, element, itemStyles);
                    }else {
                        prev = block(parent, prev, element, itemStyles);
                    }
                }else if("span".equalsIgnoreCase(tag)){
                    if("block".equalsIgnoreCase(display)){
                        prev = block(parent, prev, element, itemStyles);
                    }else {
                        prev =  parseHtml(parent, prev, element, itemStyles);
                    }
                }else if("img".equalsIgnoreCase(tag)){
                    Element img = img(parent, prev, element, styles);
                    prev = img;
                }else if("word".equalsIgnoreCase(tag)){
                    Element word = word(parent, prev, element, styles);
                    prev = word;
                }else if("ol".equalsIgnoreCase(tag)){
                    prev = ol(body, prev, element, itemStyles);
                }else if("li".equalsIgnoreCase(tag)){
                    prev = li(body, prev, element, itemStyles);
                }else if("br".equalsIgnoreCase(tag)){
                    parent.addElement("w:br");
                }else if("u".equalsIgnoreCase(tag)){
                    itemStyles.put("underline","true");
                    prev = parseHtml(parent, prev, element, itemStyles);
                }else if("b".equalsIgnoreCase(tag)){
                    itemStyles.put("font-weight","700");
                    prev = parseHtml(parent, prev, element, itemStyles);
                }else if("i".equalsIgnoreCase(tag)){
                    itemStyles.put("italics","true");
                    prev = parseHtml(parent, prev, element, itemStyles);
                }else if("del".equalsIgnoreCase(tag)){
                    itemStyles.put("dstrike","true");
                    prev = parseHtml(parent, prev, element, itemStyles);
                }else if("sup".equalsIgnoreCase(tag)){
                    itemStyles.put("vertical-align","superscript");
                    prev = parseHtml(parent, prev, element, itemStyles);
                }else if("sub".equalsIgnoreCase(tag)){
                    itemStyles.put("vertical-align","subscript");
                    prev = parseHtml(parent, prev, element, itemStyles);
                }else{
                    prev = parseHtml(parent, prev, element, itemStyles);
                }
            }
        }
        if(empty && "tc".equalsIgnoreCase(pname)){
            parent.addElement("w:p");
        }
        return prev;
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
    public void insert(Element prev, File file){
        WDocument idoc = new WDocument(file);
        idoc.load();
        Element body = idoc.getBody();
        List<Element> elements = DomUtil.elements(body, "p","tbl");
    }

    public List<String> listStyles(){
        return DocxUtil.listStyles(file);
    }
    public void save(){
        try {
            load();

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
                Element prev = null;
                if(index < elements.size()-1){
                    prev = elements.get(index+1);
                }
                for(int i=0; i<flags.size(); i++){
                    String flag = flags.get(i);
                    String content = flag;
                    String key = null;
                    if(flag.startsWith("${") && flag.endsWith("}")) {
                        key = flag.substring(2, flag.length() - 1);
                        content = replaces.get(key);
                        if(null == content){
                            content = replaces.get(flag);
                        }
                    }else if(flag.startsWith("{") && flag.endsWith("}")){
                        key = flag.substring(2, flag.length() - 1);
                        content = replaces.get(key);
                        if(null == content){
                            content = replaces.get(flag);
                        }
                    }else{
                        content = replaces.get(flag);
                    }
                    //boolean isblock = DocxUtil.isBlock(content);
                    //Element p = t.getParent();
                    /*if(null != key && DocxUtil.isEmpty(p, t) && !DocxUtil.hasParent(t,"tc")){
                        prev = DocxUtil.prev(body, p);
                        body.remove(p);
                        List<Element> list = parseHtml(body, prev ,content);
                    }else{
                        List<Element> list = parseHtml(r, prev ,content);
                    }*/
                    if(null != content) {
                        List<Element> list = parseHtml(r, prev, content);
                    }
                }
                elements.remove(t);
            }
            List<Element> bookmarks = DomUtil.elements(body, "bookmarkStart");
            for(Element bookmark:bookmarks){
                replaceBookmark(bookmark);
            }
            checkContentTypes();
            ZipUtil.replace(file,"word/document.xml", DomUtil.format(doc));
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
