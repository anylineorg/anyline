package org.anyline.office.docx.entity;

import org.anyline.entity.DataRow;
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
     * 当前节点下的文本
     * @param element element
     * @return String
     */
    public String text(Element element){
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
    private boolean isBlock(String text){
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

    /**
     * 当前节点后的所有节点
     * @param element element
     * @param tag 过滤标签
     * @return List
     */
    private List<Element> afters(Element element, String tag){
        List<Element> list = new ArrayList<>();
        List<Element> elements = element.getParent().elements();
        int index = elements.indexOf(element);
        for(int i=index+1; i<elements.size(); i++){
            Element item = elements.get(i);
            if(item.getName().equalsIgnoreCase(tag)) {
                list.add(item);
            }
        }
        return list;
    }
    /**
     * 当前节点前的所有节点
     * @param element element
     * @param tag 过滤标签
     * @return List
     */
    private List<Element> befores(Element element, String tag){
        List<Element> list = new ArrayList<>();
        List<Element> elements = element.getParent().elements();
        int index = elements.indexOf(element);
        for(int i=elements.size()-1; i>index; i--){
            Element item = elements.get(i);
            if(item.getName().equalsIgnoreCase(tag)) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * start与end之间的所有节点
     * @param start 开始
     * @param end 结束
     * @param tag 过滤
     * @return List
     */
    private List<Element> betweens(Element start,Element end, String tag){
        List<Element> list = new ArrayList<>();
        List<Element> elements = start.getParent().elements();
        int fr = elements.indexOf(start);
        int to = elements.indexOf(end);
        int index = elements.indexOf(start);
        for(int i=fr+1; i>to; i++){
            Element item = elements.get(i);
            if(item.getName().equalsIgnoreCase(tag)) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * 删除parent下的removes节点
     * @param parent parent
     * @param removes removes
     */
    private void remove(Element parent, List<Element> removes){
        List<Element> elements = parent.elements();
        for(Element remove:removes){
            elements.remove(remove);
        }
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
        boolean isblock = isBlock(content);
        Element startP = start.getParent();
        Element endP = end.getParent();
        if(isblock){
            if(startP == endP){
                //结束标签拆分到下一段落
                //<start.p><content.p><end.p>
                Element nEndP = startP.getParent().addElement("w:p");
                endP.elements().remove(end);
                nEndP.elements().add(end);
                after(nEndP, startP);
            }
            remove(startP, afters(start,"r"));
            remove(endP, befores(end,"r"));
            parseHtml(startP.getParent(),startP,content);
        }else{
            if(startP == endP){
                remove(startP,betweens(start, end,"r"));
                parseHtml(startP,startP,content);
            }else{
                remove(startP, afters(start,"r"));
                remove(endP, befores(end,"r"));
                parseHtml(startP,startP,content);
            }
        }
    }

    /**
     * 拆分关键字
     * 拆分123${key}abc成多个w:t
     * @param txt txt
     * @return List
     */
    private List<String> splitKey(String txt){
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
            }else{
                list.add(txt);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 宽度计算
     * @param src width
     * @return dxa
     */
    public int width(String src){
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
    public String widthType(String width){
        if(null != width && width.trim().endsWith("%")){
            return "pct";
        }
        if(null != width && width.trim().endsWith("dxa")){
            return "dxa";
        }
        return "dxa";
    }
    public void border(Element border, Map<String,String> styles){
        border(border,"top", styles);
        border(border,"right", styles);
        border(border,"bottom", styles);
        border(border,"left", styles);

    }
    public void border(Element border, String side, Map<String,String> styles){
        Element item = null;
        String width = styles.get("border-"+side+"-width");
        String style = styles.get("border-"+side+"-style");
        String color = styles.get("border-"+side+"-color");
        int dxa = width(width);
        int line = (int)(dxa2pt(dxa)*8);
        if(BasicUtil.isNotEmpty(width)){
            item = element(border, side);
            item.addAttribute("w:sz", line+"");
            item.addAttribute("w:val", style);
            item.addAttribute("w:color", color);
        }
    }
    public void padding(Element margin, Map<String,String> styles){
        padding(margin,"top", styles);
        padding(margin,"start", styles);
        padding(margin,"bottom", styles);
        padding(margin,"end", styles);

    }
    public void padding(Element margin, String side, Map<String,String> styles){
        String width = styles.get("padding-"+side);
        int dxa = width(width);
        if(BasicUtil.isNotEmpty(width)){
            Element item = element(margin, side);
            item.addAttribute("w:w", dxa+"");
            item.addAttribute("w:type",  "dxa");
        }
    }
    public void font(Element pr, Map<String,String> styles){
        String fontSize = styles.get("font-size");
        if(null != fontSize){
            int pt = 0;
            if(fontSize.endsWith("px")){
                int px = BasicUtil.parseInt(fontSize.replace("px",""),0);
                pt = (int)px2pt(px);
            }else if(fontSize.endsWith("pt")){
                pt = BasicUtil.parseInt(fontSize.replace("pt",""),0);
            }
            if(pt>0){
                // <w:sz w:val="28"/>
                element(pr, "sz","val", pt+"");
            }
        }
        //加粗
        String fontWeight = styles.get("font-weight");
        if(null != fontWeight && fontWeight.length()>0){
            int weight = BasicUtil.parseInt(fontWeight,0);
            if(weight >=700){
                //<w:b w:val="true"/>
                element(pr, "b","val","true");
            }
        }
        //下划线
        String underline = styles.get("underline");
        if(null != underline){
            if(underline.equalsIgnoreCase("true") || underline.equalsIgnoreCase("single")){
                //<w:u w:val="single"/>
                element(pr, "u","val","single");
            }else{
                element(pr, "u","val",underline);
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
                element(pr, "dstrike","val","true");
            }
        }
        //斜体
        String italics = styles.get("italic");
        if(null != italics){
            if(italics.equalsIgnoreCase("true")){
                //<w:dstrike w:val="true"/>
                element(pr, "i","val","true");
            }
        }
    }
    public Element element(Element parent, String tag){
        Element element = parent.element(tag);
        if(null == element){
            element = parent.addElement("w:"+tag);
        }
        return element;
    }
    public Element pr(Element element, Map<String,String> styles){
        if(null == styles){
            styles = new HashMap<String,String>();
        }
        String name = element.getName();
        String prName = name+"Pr";
        Element pr = element(element, prName);
        if("p".equals(name)){
            for(String sk: styles.keySet()){
                String sv = styles.get(sk);
                if(sk.equalsIgnoreCase("list-style-type")){
                    element(pr, "pStyle", "val",sv);
                }else if(sk.equals("list-lvl")){
                    Element numPr = element(pr,"numPr");
                    element(numPr, "ilvl", "val",sv+"");
                }else if(sk.equalsIgnoreCase("numFmt")){
                    Element numPr = element(pr,"numPr");
                    element(numPr, "numFmt", "val",sv+"");
                }else if ("text-align".equals(sk)) {
                    element(pr, "jc","val", sv);
                }else if(sk.equals("background-color")){
                    //<w:shd w:val="clear" w:color="auto" w:fill="FFFF00"/>
                    element(pr, "shd", "fill",sv.replace("#",""));
                }else if(sk.equals("margin-left")){
                    element(pr, "ind", "left",width(sv)+"");
                }else if(sk.equals("margin-right")){
                    element(pr, "ind", "right",width(sv)+"");
                }else if(sk.equals("margin-top")){
                    element(pr, "spacing", "before",width(sv)+"");
                }else if(sk.equals("margin-bottom")){
                    element(pr, "spacing", "after",width(sv)+"");
                }else if(sk.equals("padding-left")){
                    element(pr, "ind", "left",width(sv)+"");
                }else if(sk.equals("padding-right")){
                    element(pr, "ind", "right",width(sv)+"");
                }else if(sk.equals("padding-top")){
                    element(pr, "spacing", "before",width(sv)+"");
                }else if(sk.equals("padding-bottom")){
                    element(pr, "spacing", "after",width(sv)+"");
                }else if(sk.equalsIgnoreCase("text-indent")){
                    element(pr, "ind", "firstLine",width(sv)+"");
                }else if(sk.equals("line-height")){
                    element(pr, "spacing", "line",width(sv)+"");
                }
            }
            if(styles.containsKey("list-style-num")){
                //如果在样式里指定了样式
                Element numPr = element(pr,"numPr");
                element(numPr, "numId", "val",styles.get("list-style-num"));
            }else if(styles.containsKey("list-num")){
                //运行时自动生成
                Element numPr = element(pr,"numPr");
                element(numPr, "numId", "val",styles.get("list-num"));
            }
            Element border = element(pr, "bdr");
            border(border, styles);
        }else if("r".equals(name)){
            for (String sk : styles.keySet()) {
                String sv = styles.get(sk);
                if(sk.equals("color")){
                    Element color = pr.addElement("w:color");
                    color.addAttribute("w:val", sv.replace("#",""));
                }else if(sk.equals("background-color")){
                    //<w:highlight w:val="yellow"/>
                    element(pr, "highlight", "val",sv.replace("#",""));
                }
            }
            Element border = element(pr, "bdr");
            border(border, styles);
            font(pr, styles);
        }else if("tbl".equals(name)){
            for (String sk : styles.keySet()) {
                String sv = styles.get(sk);
                if(sk.equals("width")){
                    element(pr,"tblW","w", width(sv)+"");
                    element(pr,"tblW","type", widthType(sv));
                }else if(sk.equals("color")){

                }else if(sk.equalsIgnoreCase("margin-left")){
                    element(pr,"tblInd","w",width(sv)+"");
                    element(pr,"tblInd","type","dxa");
                }else if(sk.equalsIgnoreCase("padding-left")){
                    element(pr,"tblInd","w",width(sv)+"");
                    element(pr,"tblInd","type","dxa");
                }
            }
            Element border = element(pr,"tblBorders");
            border(border, styles);
        }else if("tr".equals(name)){
            for(String sk:styles.keySet()){
                String sv = styles.get(sk);
            }
        }else if("tc".equals(name)){
            for(String sk:styles.keySet()){
                String sv = styles.get(sk);
                if("vertical-align".equals(sk)){
                    element(pr,"vAlign", "val", sv );
                }else if("text-align".equals(sk)){
                    element(pr, "jc","val", sv);
                }else if(sk.equals("background-color")){
                    //<w:shd w:val="clear" w:color="auto" w:fill="FFFF00"/>
                    element(pr, "shd", "fill",sv.replace("#",""));
                }
            }
            //
            Element padding = element(pr,"tcMar");
            padding(padding, styles);
            Element border = element(pr,"tcBorders");
            border(border, styles);
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

    /**
     * src插入到ref之后
     * @param src src
     * @param ref ref
     */
    private void after(Element src, Element ref){
        if(null == ref || null == src){
            return;
        }
        //同级
        if(ref.getParent() == src.getParent()){
            List<Element> elements = ref.getParent().elements();
            int index = elements.indexOf(ref)+1;
            elements.remove(src);
            if(index > elements.size()-1){
                index = elements.size()-1;
            }
            elements.add(index, src);
        }else{
            //ref更下级
            after(src, ref.getParent());
        }

    }
    private void after(List<Element> srcs, Element ref){
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
    private void before1(Element src, Element ref){
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
    private boolean contains(String[] list, String item){
        for(String i:list){
            if(i.equalsIgnoreCase(item)){
                return true;
            }
        }
        return false;
    }
    /**
     * 找到到当前p的上一级(用来创建与当前所在p平级的新p,遇到tc停止)
     * @param element 当前节点
     * @return Element
     */
    public Element pp1(Element element){
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
    //找到当前节点所在的p
    public Element p(Element element){
        return parent(element, "p");

    }
    public Element prev(Element element){
        Element prev = null;
        List<Element> elements = element.getParent().elements();
        int index = elements.indexOf(element);
        if(index>0){
            prev = elements.get(index-1);
        }
        return prev;
    }
    public String prevName(Element element){
        Element prev = prev(element);
        if(null != prev){
            return prev.getName();
        }else{
            return "";
        }
    }
    public Element last(Element element){
        Element last = null;
        List<Element> elements = element.getParent().elements();
        if(elements.size()>0){
            last = elements.get(elements.size()-1);
        }
        return last;
    }
    public String lastName(Element element){
        Element last = last(element);
        if(null != last){
            return last.getName();
        }else{
            return "";
        }
    }

    private boolean isEmpty(Element element){
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
     * @param element
     * @param exclude
     * @return
     */
    private boolean isEmpty(Element element, Element exclude){
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
    private boolean isEmpty(List<Element> elements){
        for(Element item:elements){
            String name = item.getName();
            if(name.equalsIgnoreCase("r") || name.equalsIgnoreCase("t") || name.equalsIgnoreCase("tbl")){
                return false;
            }
        }
        return true;
    }

    private boolean hasParent(Element element, String parent){
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
    //当element位置开始把parent拆分(element不一定是parent直接子级)
    private void split(Element element){
        Element parent = parent(element,"p");
        int pindex = index(parent);
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

    /**
     * 当前节点在上级节点的下标
     * @param element element
     * @return index
     */
    private int index(Element element){
        int index = -1;
        List<Element> elements = element.getParent().elements();
        index = elements.indexOf(element);
        return index;
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
        after(tbl, after);

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
            Element tcPr = element(tc, "tcPr");
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
                styles = style(styles,td.getString("style"));
                pr(tc, styles);
                Element src = (Element)td.get("src");
                if(null != src) {
                    //Element p = tc.addElement("w:p");
                    //pr(p, styles);
                    parseHtml(tc, null, src, style(styles,""));
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
            after(r, next);
        }else if(pname.equals("tc")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            after(r, next);
        }else if(pname.equals("p")){
            r = parent.addElement("w:r");
            //after(r, next);
        }else if(pname.equals("body")){
            Element p = parent.addElement("w:p");
            pr(p, styles);
            r = p.addElement("w:r");
            after(p, next);
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
            after(box, next);
            newNext = parent;
        }else if(pname.equals("r")){
            box = parent.getParent().addElement("w:r");
            next = box.addElement("w:br");
            after(box, next);
            newNext = parent.getParent();
        }else if(pname.equals("tc")){
            box = parent.addElement("w:p");
            after(box, next);
            newNext = box;
        }else if(pname.equals("body")){
            box = parent.addElement("w:p");
            newNext = box;
            after(box, next);

        }else{
            throw new RuntimeException("div.parent 异常:"+pname+":"+element.getName()+":"+element.getTextTrim());
            //新建一个段落
        }
        pr(box, styles);
        parseHtml(box, next, element, styles);
        return newNext;
    }

    private Element ol(Element parent, Element next, Element element, Map<String,String> styles){
        styles = style(styles, element.attributeValue("style"));
        if(!hasParent(element, "ol")){
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
        after(box, next);
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
    /**
     * 添加element及属性
     * @param parent parent
     * @param tag element tag
     * @param key attribute key
     * @param value attribute value
     */
    public void element(Element parent, String tag, String key, String value){
        Element element = element(parent,tag);
        element.addAttribute("w:"+key, value);
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
        result = style(result, element.attributeValue("style"));

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
    public Map<String,String> style(Map<String,String> src, String txt){
        if(null == src){
            src = new HashMap<String,String>();
        }
        BeanUtil.merge(src,StyleParser.parse(txt));
        return src;
    }

    public void save(){
        try {
            load();
            doc = DocumentHelper.parseText(xml);
            body = doc.getRootElement().element("body");

            List<Element> ts = DomUtil.elements(body, "t");
            for(Element t:ts){
                String txt = t.getTextTrim();
                List<String> flags = splitKey(txt);

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
                    boolean isblock = isBlock(content);
                    Element p = t.getParent().getParent();
                    if(null != key && isEmpty(p, t) && !hasParent(t,"tc")){
                        next = prev(body, p);
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
    private Element next(Element parent, Element child){
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
    private Element prev(Element parent, Element child){
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

    public final double PT_PER_PX = 0.75;
    public final int IN_PER_PT = 72;
    public final double CM_PER_PT = 28.3;
    public final double MM_PER_PT = 2.83;
    public final int EMU_PER_PX = 9525;
    public final int px2dxa(int px){
        return pt2dxa(px2pt(px));
    }
    public final int pt2dxa(double pt){
        return (int)(pt*20);
    }
    public final double dxa2pt(double dxa){
        return  dxa/20;
    }
    public final double dxa2px(double dxa){
        return  pt2px(dxa2pt(dxa));
    }
    public final double px2emu(double px) {
        return  (px* EMU_PER_PX);
    }

    public final double emu2px(double emu) {
        return (emu*EMU_PER_PX);
    }

    public final double pt2px(double pt) {
        return (pt/PT_PER_PX);
    }

    public final double in2px(double in) {
        return (in2pt(in)*PT_PER_PX);
    }

    public final double px2in(double px) {
        return pt2in(px2pt(px));
    }

    public final double cm2px(double cm) {
        return (cm2pt(cm)*PT_PER_PX);
    }

    public final double px2cm(double px) {
        return pt2cm(px2pt(px));
    }

    public final double mm2px(double mm) {
        return (mm2pt(mm)*PT_PER_PX);
    }

    public final double px2mm(double px) {
        return pt2mm(px2pt(px));
    }

    public final double pt2in(double pt) {
        return (pt/IN_PER_PT);
    }

    public final double pt2mm(double mm) {
        return (mm/MM_PER_PT);
    }

    public final double pt2cm(double in) {
        return (in/CM_PER_PT);
    }

    public final double px2pt(double px) {
        return (px*PT_PER_PX);
    }

    public final double in2pt(double in) {
        return (in*IN_PER_PT);
    }

    public final double mm2pt(double mm) {
        return (mm*MM_PER_PT);
    }

    public final double cm2pt(double cm) {
        return (cm*CM_PER_PT);
    }
}
