package org.anyline.office.docx.entity;

import org.anyline.entity.DataRow;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.office.docx.util.StyleParser;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.util.*;

public class Document {
    private File file;
    private String xml = null;      //document.xml文本
    private org.dom4j.Document doc = null;
    private Element body = null;
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
        xml = ZipUtil.read(file,"word/document.xml","UTF-8");
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
            DocxUtil.remove(startP, DocxUtil.afters(start,"r"));
            DocxUtil.remove(endP, DocxUtil.befores(end,"r"));
            parseHtml(startP.getParent(),startP,content);
        }else{
            if(startP == endP){
                DocxUtil.remove(startP,DocxUtil.betweens(start, end,"r"));
                parseHtml(startP,startP,content);
            }else{
                DocxUtil.remove(startP, DocxUtil.afters(start,"r"));
                DocxUtil.remove(endP, DocxUtil.befores(end,"r"));
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
        Element pr = DocxUtil.element(element, prName);
        if("p".equals(name)){
            for(String sk: styles.keySet()){
                String sv = styles.get(sk);
                if(sk.equalsIgnoreCase("list-style-type")){
                    DocxUtil.element(pr, "pStyle", "val",sv);
                }else if(sk.equals("list-lvl")){
                    Element numPr = DocxUtil.element(pr,"numPr");
                    DocxUtil.element(numPr, "ilvl", "val",sv+"");
                }else if(sk.equalsIgnoreCase("numFmt")){
                    Element numPr = DocxUtil.element(pr,"numPr");
                    DocxUtil.element(numPr, "numFmt", "val",sv+"");
                }else if ("text-align".equals(sk)) {
                    DocxUtil.element(pr, "jc","val", sv);
                }else if(sk.equals("background-color")){
                    //<w:shd w:val="clear" w:color="auto" w:fill="FFFF00"/>
                    DocxUtil.element(pr, "shd", "fill",sv.replace("#",""));
                }else if(sk.equals("margin-left")){
                    DocxUtil.element(pr, "ind", "left",DocxUtil.width(sv)+"");
                }else if(sk.equals("margin-right")){
                    DocxUtil.element(pr, "ind", "right",DocxUtil.width(sv)+"");
                }else if(sk.equals("margin-top")){
                    DocxUtil.element(pr, "spacing", "before",DocxUtil.width(sv)+"");
                }else if(sk.equals("margin-bottom")){
                    DocxUtil.element(pr, "spacing", "after",DocxUtil.width(sv)+"");
                }else if(sk.equals("padding-left")){
                    DocxUtil.element(pr, "ind", "left",DocxUtil.width(sv)+"");
                }else if(sk.equals("padding-right")){
                    DocxUtil.element(pr, "ind", "right",DocxUtil.width(sv)+"");
                }else if(sk.equals("padding-top")){
                    DocxUtil.element(pr, "spacing", "before",DocxUtil.width(sv)+"");
                }else if(sk.equals("padding-bottom")){
                    DocxUtil.element(pr, "spacing", "after",DocxUtil.width(sv)+"");
                }else if(sk.equalsIgnoreCase("text-indent")){
                    DocxUtil.element(pr, "ind", "firstLine",DocxUtil.width(sv)+"");
                }else if(sk.equals("line-height")){
                    DocxUtil.element(pr, "spacing", "line",DocxUtil.width(sv)+"");
                }
            }
            if(styles.containsKey("list-style-num")){
                //如果在样式里指定了样式
                Element numPr = DocxUtil.element(pr,"numPr");
                DocxUtil.element(numPr, "numId", "val",styles.get("list-style-num"));
            }else if(styles.containsKey("list-num")){
                //运行时自动生成
                Element numPr = DocxUtil.element(pr,"numPr");
                DocxUtil.element(numPr, "numId", "val",styles.get("list-num"));
            }
            Element border = DocxUtil.element(pr, "bdr");
            DocxUtil.border(border, styles);
        }else if("r".equals(name)){
            for (String sk : styles.keySet()) {
                String sv = styles.get(sk);
                if(sk.equals("color")){
                    Element color = pr.addElement("w:color");
                    color.addAttribute("w:val", sv.replace("#",""));
                }else if(sk.equals("background-color")){
                    //<w:highlight w:val="yellow"/>
                    DocxUtil.element(pr, "highlight", "val",sv.replace("#",""));
                }
            }
            Element border = DocxUtil.element(pr, "bdr");
            DocxUtil.border(border, styles);
            DocxUtil.font(pr, styles);
        }else if("tbl".equals(name)){
            for (String sk : styles.keySet()) {
                String sv = styles.get(sk);
                if(sk.equals("width")){
                    DocxUtil.element(pr,"tblW","w", DocxUtil.width(sv)+"");
                    DocxUtil.element(pr,"tblW","type", DocxUtil.widthType(sv));
                }else if(sk.equals("color")){

                }else if(sk.equalsIgnoreCase("margin-left")){
                    DocxUtil.element(pr,"tblInd","w",DocxUtil.width(sv)+"");
                    DocxUtil.element(pr,"tblInd","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-left")){
                    DocxUtil.element(pr,"tblInd","w",DocxUtil.width(sv)+"");
                    DocxUtil.element(pr,"tblInd","type","dxa");
                }
            }
            Element border = DocxUtil.element(pr,"tblBorders");
            DocxUtil.border(border, styles);
        }else if("tr".equals(name)){
            for(String sk:styles.keySet()){
                String sv = styles.get(sk);
            }
        }else if("tc".equals(name)){
            for(String sk:styles.keySet()){
                String sv = styles.get(sk);
                if("vertical-align".equals(sk)){
                    DocxUtil.element(pr,"vAlign", "val", sv );
                }else if("text-align".equals(sk)){
                    DocxUtil.element(pr, "jc","val", sv);
                }else if(sk.equals("background-color")){
                    //<w:shd w:val="clear" w:color="auto" w:fill="FFFF00"/>
                    DocxUtil.element(pr, "shd", "fill",sv.replace("#",""));
                }
            }
            //
            Element padding = DocxUtil.element(pr,"tcMar");
            DocxUtil.padding(padding, styles);
            Element border = DocxUtil.element(pr,"tcBorders");
            DocxUtil.border(border, styles);
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
            html = html.replace("&nbsp;","&amp;nbsp;");
            html = "<body>" + html + "</body>";
            org.dom4j.Document doc = DocumentHelper.parseText(html);
            Element root = doc.getRootElement();
            parseHtml(box, next, root, null);
        }catch (Exception e){
            e.printStackTrace();
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



    public Element table(Element box, Element after, Element table){

        Element tbl = body.addElement("w:tbl");
        Element tblPr = tbl.addElement("w:tblPr");
        Element tblStyle = tblPr.addElement("w:tblStyle");
        tblStyle.addAttribute("w:val","a5");

        Map<String,String> styles = style(null, table);
        pr(tbl, styles);
        List<Element> html_rows = table.elements("tr");
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
        DataRow[][] cells = new DataRow[rows_size][cols_size];
        for(int r=0; r<rows_size; r++) {
            for (int c = 0; c < cols_size; c++) {
                DataRow tc = new DataRow();
                cells[r][c] = tc;
            }
        }
        for(int r=0; r<rows_size; r++) {
            Element html_row = html_rows.get(r);
            String row_style = html_row.attributeValue("style");
            List<Element> cols = html_row.elements("td");
            for(int c = 0; c<cols.size(); c++){
                Element html_col = cols.get(c);
                String value = html_col.getTextTrim();
                DataRow tc = cells[r][c];
                int merge_qty = 0;
                while(tc.size() > 0){
                    merge_qty ++;
                    tc = cells[r][c+merge_qty];
                }
                tc.put("src", html_col);
                tc.put("value", value);
                tc.put("style", row_style+";"+html_col.attributeValue("style"));
                tc.put("class", html_col.attributeValue("class"));
                int rowspan = BasicUtil.parseInt(html_col.attributeValue("rowspan"), 1);
                int colspan = BasicUtil.parseInt(html_col.attributeValue("colspan"),1);

                if(rowspan > 1){
                    tc.put("merge",1);
                    for(int i=r+1; i<=r+rowspan-1; i++){
                        for(int j=c+1; j<c+colspan; j++){
                            DataRow merge = cells[i][j];
                            merge.put("remove",1);//被上一列合燕
                        }
                        DataRow merge = cells[i][c];
                        merge.put("merge",2);//被上一行合并
                    }
                }
                if(colspan > 1){
                    tc.put("colspan", colspan);
                    for(int i=c+1; i<c+colspan; i++){
                        DataRow merge = cells[r][i];
                        merge.put("remove",1);//被上一列合并
                    }
                }
            }
        }
        for(int r=0; r<rows_size; r++) {
            Element tr = tr(tbl, cells[r], styles);
        }
        DocxUtil.after(tbl, after);

        return tbl;
    }

    public Element tr(Element parent, DataRow[] tds, Map<String, String> styles){
        Element tr = parent.addElement("w:tr");
        for (DataRow td:tds) {
            Element tc = tc(tr, td, styles);
        }
        return tr;
    }
    public Element tc(Element parent, DataRow td, Map<String, String> styles){
        Element tc = null;
        int merge = td.getInt("merge",0); //0:不合并 1:向下合并 2:被合并
        int colspan = td.getInt("colspan",1); //向右合并
        int remove = td.getInt("remove",0); //被左侧合并
        if(remove == 0){
            tc = parent.addElement("w:tc");
            Element tcPr = DocxUtil.element(tc, "tcPr");
            if(merge > 0){
                Element vMerge = tcPr.addElement("w:vMerge");
                if(merge == 1) {
                    vMerge.addAttribute("w:val", "restart");
                }
            }
            if(colspan >1){
                Element span = tcPr.addElement("w:gridSpan");
                span.addAttribute("w:val", colspan+"");
            }
            if(tcPr.elements().size()==0){
                tc.remove(tcPr);
            }
            if(merge !=2){
                styles = StyleParser.parse(styles,td.getString("style"));
                pr(tc, styles);
                Element src = (Element)td.get("src");
                if(null != src) {
                    //Element p = tc.addElement("w:p");
                    //pr(p, styles);
                    parseHtml(tc, null, src, StyleParser.parse(styles,""));
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
        if(pname.equals("r")){
            r = parent;
            DocxUtil.after(r, next);
        }else if(pname.equals("tc")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(r, next);
        }else if(pname.equals("p")){
            r = parent.addElement("w:r");
            //DocxUtil.after(r, next);
        }else if(pname.equals("body")){
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
        if(pname.equals("p")){
            box = parent.addElement("w:r");
            next = box.addElement("w:br");
            DocxUtil.after(box, next);
            newNext = parent;
        }else if(pname.equals("r")){
            box = parent.getParent().addElement("w:r");
            next = box.addElement("w:br");
            DocxUtil.after(box, next);
            newNext = parent.getParent();
        }else if(pname.equals("tc")){
            box = parent.addElement("w:p");
            DocxUtil.after(box, next);
            newNext = box;
        }else if(pname.equals("body")){
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
        styles = StyleParser.parse(styles, element.attributeValue("style"));
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
    public Element parseHtml(Element parent, Element next, Element html, Map<String,String> styles){
        String pname = parent.getName();

        styles = style(styles, html);

        String txt = html.getTextTrim().replace("&nbsp;"," ");
        if(html.elements().size()==0){
            txt = txt.replaceAll("\\s","");
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
                Map<String,String> itemStyles = style(styles, element);
                String display = itemStyles.get("display");
                if("none".equalsIgnoreCase(display)){
                    continue;
                }
                if("table".equalsIgnoreCase(tag)){
                    Element box = null;
                    if(pname.equals("tc")){
                        box = parent;
                        pr(box, styles);
                    }else if(pname.equals("p")){
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
        while(parent.getName().equals("p")){
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
    public Map<String,String> style(Map<String,String> src, Element element){
        Map<String,String> result = new HashMap<String,String>();

        if(null != src){
            for(String k: src.keySet()){
                if(!k.contains("border") && !k.contains("margin") && !k.contains("padding") && !k.contains("width") && !k.contains("height")) {
                    result.put(k, src.get(k));
                }
            }
        }
        result = StyleParser.parse(result, element.attributeValue("style"));

        String name = element.getName();
        BeanUtil.merge(result, this.styles.get(name));
        String id = element.attributeValue("id");
        if(null != id){
            BeanUtil.merge(result, this.styles.get("#"+id));
        }
        String clazz = element.attributeValue("class");
        if(null != clazz){
            String[] cs = clazz.split(" ");
            for(String c:cs){
                BeanUtil.merge(result, this.styles.get("."+c));
            }
        }

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
            body = doc.getRootElement().element("body");

            List<Element> ts = DomUtil.elements(body, "t");
            for(Element t:ts){
                String txt = t.getTextTrim();
                List<String> flags = DocxUtil.splitKey(txt);

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
                    boolean isblock = DocxUtil.isBlock(content);
                    Element p = t.getParent().getParent();
                    if(null != key && DocxUtil.isEmpty(p, t) && !DocxUtil.hasParent(t,"tc")){
                        next = DocxUtil.prev(body, p);
                        body.remove(p);
                        List<Element> list = parseHtml(body, next ,content);
                    }else{
                        List<Element> list = parseHtml(r, next ,content);
                    }
                }
                elements.remove(t);
            }
            List<Element> bookmarks = DomUtil.elements(body, "bookmarkStart");
            for(Element bookmark:bookmarks){
                replaceBookmark(bookmark);
            }
            xml = DomUtil.format(doc);
            ZipUtil.replace(file,"word/document.xml", xml);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
