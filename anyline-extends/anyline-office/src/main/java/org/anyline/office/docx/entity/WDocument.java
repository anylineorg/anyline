/*
 * Copyright 2006-2023 www.anyline.org
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


package org.anyline.office.docx.entity;

import org.anyline.entity.html.Table;
import org.anyline.entity.html.Td;
import org.anyline.entity.html.Tr;
import org.anyline.net.HttpUtil;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.*;
import org.anyline.util.regular.RegularUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.*;

public class WDocument extends Welement{
    private static Logger log = LoggerFactory.getLogger(WDocument.class);
    private File file;
    private String xml = null;      // document.xml文本
    // word/document.xml
    private org.dom4j.Document doc = null; 
    public boolean IS_HTML_ESCAPE = false;  //设置文本时是否解析转义符

    // word/_rels/document.xml.rels
    private String relsXml = null;
    private org.dom4j.Document relsDoc;

    private Map<String, Map<String, String>> styles = new HashMap<String, Map<String, String>>();
    private Map<String, String> replaces = new HashMap<String, String>();
    private int listNum = 0;


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
                src = doc.getRootElement().element("body");
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
            src = doc.getRootElement().element("body");
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
        Map<String,Map<String, String>> map = StyleParser.load(html);
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

    public void save(){
        save(Charset.forName("UTF-8"));
    }
    public void save(Charset charset){
        try {
            //加载文件
            load();
            //执行替换
            replace(src, replaces);
            //检测内容类型
            checkContentTypes();
            //合并列的表格,如果没有设置宽度,在wps中只占一列,需要在表格中根据总列数添加
            checkMergeCol();
            ZipUtil.replace(file,"word/document.xml", DomUtil.format(doc), charset);
            ZipUtil.replace(file,"word/_rels/document.xml.rels", DomUtil.format(relsDoc), charset);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 执行替换
     * @param box 最外层元素
     * @param replaces k:v
     */
    public void replace(Element box, Map<String, String> replaces){
        List<Element> ts = DomUtil.elements(box, "t");
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
                    key = flag.substring(1, flag.length() - 1);
                    content = replaces.get(key);
                    if(null == content){
                        content = replaces.get(flag);
                    }
                }else{
                    content = replaces.get(flag);
                }
                // boolean isblock = DocxUtil.isBlock(content);
                // Element p = t.getParent();
                    /*if(null != key && DocxUtil.isEmpty(p, t) && !DocxUtil.hasParent(t,"tc")){
                        prev = DocxUtil.prev(src, p);
                        src.remove(p);
                        List<Element> list = parseHtml(src, prev ,content);
                    }else{
                        List<Element> list = parseHtml(r, prev ,content);
                    }*/
                if(null != content) {
                    List<Element> list = parseHtml(r, prev, content);
                }
            }
            elements.remove(t);
        }
        List<Element> bookmarks = DomUtil.elements(box, "bookmarkStart");
        for(Element bookmark:bookmarks){
            replaceBookmark(bookmark, replaces);
        }
    }
    /**
     * 合并列的表格,如果没有设置宽度,在wps中只占一列,需要在表格中根据总列数添加
     * w:tblGrid
     *      w:gridCol w:w="1000"
     */
    private void checkMergeCol(){
        List<Element> tables = DomUtil.elements(doc.getRootElement(), "tbl");
        for(Element table:tables){
            int max = 0;
            boolean isMerge = false;
            List<Element> trs = DomUtil.elements(table, "tr");
            for(Element tr:trs){
                int size = 0;
                List<Element> tcs = DomUtil.elements(tr,"tc");
                for(Element tc:tcs){
                    int colspan = 1;
                    Element pr = DomUtil.element(tc,"tcPr");
                    if(null != pr){
                        Element grid = DomUtil.element(pr,"gridSpan");
                        if(null != grid){
                            colspan = BasicUtil.parseInt(grid.attributeValue("w:val"),1);
                        }
                    }
                    if(colspan > 1){
                        isMerge = true;
                    }
                    size += colspan;
                }
                if(size > max){
                    max = size;
                }
            }
            if(isMerge){

                int tableWidth = 5000;
                Element  tblW = DomUtil.element(table, "tblW");
                if(null != tblW){
                    tableWidth = BasicUtil.parseInt(tblW.attributeValue("w:w"),5000);
                }
                Element tblGrid = DomUtil.element(table,"tblGrid");
                if(null == tblGrid){
                    tblGrid = DocxUtil.addElement(table, "tblGrid");
                }
                List<Element> gridCols = DomUtil.elements(tblGrid, "gridCol");
                int width = tableWidth / max;
                for(int i=gridCols.size(); i<max; i++){
                    Element gridCol = tblGrid.addElement("w:gridCol");
                    gridCol.addAttribute("w:w", width+"");
                }
            }
        }
    }

    /**
     * 检测内容类型
     */
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
    /**
     * 在element之前插入节点
     * @param element element
     * @param html html
     */
    public Element before(Element element, String html){
        Element parent = element.getParent();
        List<Element> elements = parent.elements();
        int index = elements.indexOf(element)-1;
        Element prev = null;
        if(index >= 0){
            prev = elements.get(index);
        }
        List<Element> list = parseHtml(parent, prev, html);
        if(list.isEmpty()){
            return null;
        }
        return list.get(list.size()-1);
    }

    public Element before(Element point, Element element){
        Element parent = point.getParent();
        List<Element> elements = parent.elements();
        int index = elements.indexOf(point);
        elements.add(index ,element);
        return element;
    }

    public Element before(Element point, Wtable table){
        Element src = table.getSrc();
        before(point, src);
        return src;
    }
    public Element after(Element point, Element element){
        Element parent = point.getParent();
        List<Element> elements = parent.elements();
        int index = elements.indexOf(point)+1;
        if(index >= elements.size()-1){
            elements.add(element);
        }else {
            elements.add(index, element);
        }
        return element;
    }

    public Element after(Element point, Wtable table){
        Element src = table.getSrc();
        after(point, src);
        return src;
    }
    /**
     * 在element之后 插入节点
     * 解析html有可能解析出多个element这里会返回最外层的最后一个
     * @param element element
     * @param html html
     * @return Element
     */
    public Element after(Element element, String html){
        Element parent = element.getParent();
        List<Element> list = parseHtml(parent, element, html);
        if(list.isEmpty()){
            return null;
        }
        return list.get(list.size()-1);
    }
    public Element insert(Element parent, String html){
        List<Element> list = parseHtml(parent, null, html);
        if(list.isEmpty()){
            return null;
        }
        return list.get(list.size()-1);
    }
    public Element insert(int index, Element parent, String html){
        List<Element> elements = parent.elements();
        if(index <= 1){
            index = 1;
        }else if(index >= elements.size()){
            index = elements.size()-1;
        }
        Element prev = elements.get(index-1);
        List<Element> list = parseHtml(parent, prev, html);
        if(list.isEmpty()){
            return null;
        }
        return list.get(list.size()-1);
    }

    public Element insert(Element parent, Element element){
        parent.elements().add(element);
        return element;
    }
    public Element insert(Element parent, Wtable table){
        Element src = table.getSrc();
        insert(parent, src);
        return src;
    }
    public Element insert(int index, Element parent, Element element){
        List<Element> elements = parent.elements();
        elements.add(index, element);
        return element;
    }
    public Element insert(int index, Element parent,  Wtable table){
        Element src = table.getSrc();
        insert(index, parent, src);
        return src;
    }

    /**
     * 获取书签所在的标签 通常用来定位
     * @param bookmark 书签
     * @param tag 上一级标签名 如tbl
     * @return Element
     */
    public Element parent(String bookmark, String tag){
        load();
        Element bk = DocxUtil.bookmark(doc.getRootElement(), bookmark);
        return DocxUtil.getParent(bk, tag);
    }
    public Element parent(String bookmark){
        return parent(bookmark, null);
    }
    public Wtable table(String bookmark){
        Element src = parent(bookmark, "tbl");
        Wtable table = new Wtable(this, src);
        return table;
    }

    /**
     * 获取doby下的table
     * @param recursion 是否递归获取所有级别的table,正常情况下不需要,word中的tbl一般在src下的最顶级,除非有表格嵌套
     * @return tables
     */
    public List<Wtable> tables(boolean recursion){
        if(!recursion){
            return tables();
        }
        load();
        List<Wtable> tables = new ArrayList<>();
        List<Element> elements = children(src);
        for(Element element:elements){
            if(element.getName().equals("tbl")){
                Wtable table = new Wtable(this, element);
                tables.add(table);
            }
        }
        return tables;
    }

    private List<Element> children(Element parent){
        List<Element> result = new ArrayList<>();
        List<Element> items = parent.elements();
        for(Element item:items){
            result.add(item);
            result.addAll(children(item));
        }
        return result;
    }
    public List<Wtable> tables(){
        load();
        List<Wtable> tables = new ArrayList<>();
        List<Element> elements = src.elements("tbl");
        for(Element element:elements){
            Wtable table = new Wtable(this, element);
            tables.add(table);
        }
        return tables;
    }
    // 插入排版方向
    public void setOrient(Element prev, String orient, Map<String, String> styles){
        int index = index(src, prev);
        Element p = src.addElement("w:p");
        Element pr = p.addElement("pPr");

        DocxUtil.setOrient(pr, orient, styles);

        List<Element> elements = src.elements();
        if(index > -1 && index <elements.size()-1){
            elements.remove(p);
            elements.add(index+1, p);
        }
    }
    public void setOrient(Element prev, String orient){
        setOrient(prev, orient, null);
    }
    // 插入换页
    public void insertPageBreak(Element prev){
        int index = index(src, prev);
        Element p = src.addElement("w:p");

        p.addElement("w:r").addElement("w:br").addAttribute("w:type","page");
        p.addElement("w:r").addElement("w:lastRenderedPageBreak");

        List<Element> elements = src.elements();
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
     * @replaces K:v
     */
    private void replaceBookmark(Element start, Map<String,String> replaces){
        String id = start.attributeValue("id");
        Element end =  DomUtil.element(src, "bookmarkEnd","id",id);
        String name = start.attributeValue("name");
        String content = replaces.get(name);
        if(null == content){
            return;
        }
        boolean isblock = DocxUtil.isBlock(content);
        Element startParent = start.getParent();
        Element endParent = end.getParent();
        if(isblock){
            if(startParent == endParent){
                // 结束标签拆分到下一段落
                // <start.p><content.p><end.p>
                Element nEndP = startParent.getParent().addElement("w:p");
                endParent.elements().remove(end);
                nEndP.elements().add(end);
                DocxUtil.after(nEndP, startParent);
            }
            DomUtil.remove(startParent, DomUtil.afters(start,"t"));
            DomUtil.remove(endParent, DomUtil.befores(end,"t"));
            parseHtml(startParent.getParent(),startParent,content);
        }else{
            if(startParent == endParent){
                DomUtil.remove(startParent,DomUtil.betweens(start, end,"t"));
                parseHtml(startParent,start,content);
            }else{
                DomUtil.remove(startParent, DomUtil.afters(start,"t"));
                DomUtil.remove(endParent, DomUtil.befores(end,"t"));
                parseHtml(startParent,start,content);
            }
        }
    }

    public  Element pr(Element element, Map<String, String> styles){
        return DocxUtil.pr(element, styles);
    }

    /**
     * 在prev之后插入节点
     * @param box box
     * @param prev 放在prev之后
     * @param html html
     * @return list
     */
    private List<Element> parseHtml(Element box, Element prev, String html){
        List<Element> list = new ArrayList<Element>();
        if(null == html || html.trim().length()==0){
            return list;
        }
        // 抽取style
        this.styles.clear();
        List<String> styles = RegularUtil.cuts(html,true,"<style",">","</style>");
        for(String style:styles){
            loadStyle(style);
            html = html.replace(style,"");
        }
        try {
            if(IS_HTML_ESCAPE){
                html = HtmlUtil.name2code(html);
            }
            html = "<root>" + html + "</root>";
            org.dom4j.Document doc = DocumentHelper.parseText(html);
            Element root = doc.getRootElement();
            parseHtml(box, prev, root, null, true);
            //提取出新添加的elements
            int size = root.elements().size();
            List<Element> elements = box.elements();
            int index = elements.indexOf(prev);
            for(int i=0; i<size; i++){
                list.add(elements.get(index+i+1));
            }
        }catch (Exception e){
            e.printStackTrace();
            // log.error(html);
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
    // 当element位置开始把parent拆分(element不一定是parent直接子级)
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

        Element tbl = src.addElement("w:tbl");
        Element tblPr = tbl.addElement("w:tblPr");

        Table table = new Table();
        Map<String, String> styles = style(src);
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
                if(IS_HTML_ESCAPE) {
                    text = HtmlUtil.display(text);
                }
                tc.setText(text);
                Map<String, String> tdStyles = StyleParser.join(tc.getStyles(),style(html_col));
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
        Map<String, String> styles = StyleParser.inherit(tr.getStyles(), tr.getTable().getStyles());
        tr.setStyles(styles);
        pr(etr, tr.getStyles());
        for (Td td:tr.getTds()) {
            Element tc = tc(etr, td);
        }
        return etr;
    }
    public Element tc(Element parent, Td td){
        Element tc = null;
        int merge = td.getMerge(); // 0:不合并 1:向下合并(restart) 2:被合并(continue)
        int colspan = td.getColspan(); // 向右合并
        boolean remove = td.isRemove(); // 被左侧合并
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
                // tc.remove(tcPr);
            }

            Map<String, String> styles = StyleParser.inherit(td.getStyles(), td.getTr().getStyles());
            pr(tc, styles);
            if(merge !=2){
                if(null != td.getSrc()) {
                    parseHtml(tc, null, td.getSrc(), StyleParser.inherit(null, styles), false);
                }
            }else{
                p(tc,"",null);
            }
        }
        return  tc;
    }
    private Element inline(Element parent, Element prev, String text, Map<String, String> styles, boolean copyPrevStyle){
        String pname = parent.getName();
        Element r;
        if(pname.equalsIgnoreCase("r")){
            r = parent;
            pr(parent, styles);
            DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("tc")){
            Element p = DocxUtil.addElement(parent, "p");
            r = DocxUtil.addElement(p, "r");
            pr(r, styles);
            DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("p")){
            pr(parent, styles);
            r = parent.addElement("w:r");
            // 复制前一个w 的样式
            if(copyPrevStyle && null != prev){
                Element prevR = prevStyle(prev);
                DocxUtil.copyStyle(r, prevR, true);
            }

            DocxUtil.after(r, prev);
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
        if(IS_HTML_ESCAPE) {
            text = HtmlUtil.display(text);
        }
        t.setText(text.trim());
        return r;
    }
    // 前一个样式
    public Element prevStyle(Element prev){
        Element prevStyle = null;
        if(prev.getName().equals("r")){
            prevStyle = prev;
        }else{
            Element tmp = DocxUtil.prev(prev);
            if(null != tmp) {
                String tmpName = tmp.getName();
                if (tmpName.equals("r")) {
                    prevStyle = tmp;
                } else if (tmpName.equals("pPr")) {
                    prevStyle = tmp;
                } else if (tmpName.equals("p")) {
                    prevStyle = tmp;
                }
            }
        }
        return prevStyle;
    }
    public Element block(Element parent, Element prev, Element element, Map<String, String> styles){
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
            // box = DocxUtil.addElement(parent,"p");
            Element p = parent.element("p");
            if(null != p && DocxUtil.isEmpty(p)){
                box = p;
            }else{
                box = parent.addElement("w:p");
            }
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
            // 新建一个段落
        }

        pr(box, styles);
        parseHtml(box, prev, element, styles, false);

        if(null != styles && null != styles.get("page-break-after")){
            // 分页
            wp.addElement("w:r").addElement("w:br").addAttribute("w:type","page");
            wp.addElement("w:r").addElement("w:lastRenderedPageBreak");
        }
        return newPrev;
    }
    private Element ol(Element parent, Element prev, Element element, Map<String, String> styles){
        styles = StyleParser.parse(styles, element.attributeValue("style"), true);
        if(!DocxUtil.hasParent(element, "ol")){
            listNum ++;//新一组编号
        }
        List<Element> lis = element.elements();
        for(Element li:lis){
            String liName = li.getName();
            if(liName.equalsIgnoreCase("ol")) {
                prev = ol(src, prev, li, styles);
            }else{
                prev = li(src, prev, li, styles);
            }
        }
        return prev;
    }
    private List<Map<String, String>> lis(Element parent){
        List<Map<String, String>> lis = new ArrayList<Map<String, String>>();
        Iterator<Node> nodes = parent.nodeIterator();
        while(nodes.hasNext()){
            Node node = nodes.next();
            int type = node.getNodeType();
            if(type ==3){

            }else if(type ==1){
                Element element = (Element)node;
                String tag = element.getName();
                if(tag.equalsIgnoreCase("li")){
                    Map<String, String> li = new HashMap<String, String>();
                    li.put("tag",tag);
                    lis.add(li);
                }
            }
        }
        return lis;
    }
    private Element li(Element parent, Element prev, Element element, Map<String, String> styles){
        Element box = parent.addElement("w:p");
        int lvl = lvl(element);
        styles.put("list-lvl",lvl+"");
        styles.put("list-num", listNum+"");
        pr(box, styles);
        DocxUtil.after(box, prev);
        prev = parseHtml(box, prev, element, styles, false);
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
            Element wsrc =  DocumentHelper.parseText(wxml).getRootElement().element("body");
            List<Element> elements = null;
            if(null != bookmark){
                Element start = DocxUtil.bookmark(wsrc, bookmark);
                elements = DocxUtil.betweens(start,"tr","tblGrid","p");
            }else{
                elements =  wsrc.elements();
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
            Element p = parent.element("p");
            if(null == p || !DocxUtil.isEmpty(p)){
                p = parent.addElement("w:p");
            }
            pr(p, styles);
            r = p.addElement("w:r");
            DocxUtil.after(r, prev);
        }else if(pname.equalsIgnoreCase("p")){
            pr(parent, styles);
            r = parent.addElement("w:r");
            // DocxUtil.after(r, prev);
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
        String src = element.attributeValue("body");
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
        File tmpdir = new File(System.getProperty("java.io.tmpdir"));
        File img = null;
        try {
            // 下载文件
            if(HttpUtil.isUrl(src)) {
                img = new File(tmpdir,"image" + rdm + "." + subfix);
                HttpUtil.download(src, img);
            }else{
                // 本地图片
                if(src.startsWith("file:")){
                    src = src.substring("file:".length());
                }
                img = new File(src);
            }
            Map<String,File> map = new HashMap<>();
            map.put("word/media/"+img.getName(),img);
            ZipUtil.append( map,file);
            if(HttpUtil.isUrl(src)) {
                // 删除临时文件
                img.delete();
            }
            // 创建文件资源引用
            Element relRoot = relsDoc.getRootElement();
            Element imgRel = relRoot.addElement("Relationship");
            imgRel.addAttribute("Id",rId);
            imgRel.addAttribute("Type","http://schemas.openxmlformats.org/officeDocument/2006/relationships/image");
            imgRel.addAttribute("Target","media/"+img.getName());
        }catch (Exception e){
            e.printStackTrace();
        }

        Element draw = r.addElement("w:drawing");
        Element box = null;
        String positionType = styles.get("position");
        /*
            fixed            相对于页面的左上角定位对象
            relative         相对其他元素定位
        */
        boolean isFloat = "relative".equalsIgnoreCase(positionType) || "fixed".equalsIgnoreCase(positionType);
        if(isFloat){
            // 浮动
            box = draw.addElement("wp:anchor");
            box.addAttribute("distT","0");
            box.addAttribute("distB","0");
            box.addAttribute("distL","114300");
            box.addAttribute("distR","114300");

            int zIndex = BasicUtil.parseInt(styles.get("z-index"),100);
            box.addAttribute("relativeHeight",zIndex+"");
            box.addAttribute("behindDoc","0");
            box.addAttribute("locked","0");
            box.addAttribute("layoutInCell","0");
            box.addAttribute("allowOverlap","1");


            // 水平偏移
            int offsetX = (int)DocxUtil.dxa2emu(DocxUtil.dxa(styles.get("offset-x")));
            // 垂直偏移
            int offsetY = (int)DocxUtil.dxa2emu(DocxUtil.dxa(styles.get("offset-y")));

            Element simplePos= box.addElement("wp:simplePos");
            if("fixed".equals(positionType)){
                // 如果使用simplePos定位 这里设置成1
                // 相对于页面的左上角定位对象
                box.addAttribute("simplePos","1");
                simplePos.addAttribute("x",offsetX+"");
                simplePos.addAttribute("y",offsetY+"");
            }else{
                // relative相对位
                box.addAttribute("simplePos","0");
                simplePos.addAttribute("x","0");
                simplePos.addAttribute("y","0");
                /*对于水平定位:
                character - 相对于锚点在运行内容中的位置
                column  - 相对于包含锚的列的范围
                insideMargin  - 相对于奇数页的左边距,偶数页的右边距
                leftMargin  - 相对于左边距
                margin  - 相对于页边距
                outsideMargin  - 相对于奇数页的右边距,偶数页的左边距
                page - 相对于页面边缘
                rightMargin - 相对于右边距
                */

                // 水平参照
                String relativeX = styles.get("relative-x");

                Element positionH = box.addElement("wp:positionH");
                positionH.addAttribute("relativeFrom",relativeX);
                Element posOffsetH = positionH.addElement("wp:posOffset");
                posOffsetH.setText(offsetX+"");
                /*对于垂直定位:
                bottomMargin - 相对于底部边距
                insideMargin - 相对于当前页面的内边距
                line - 相对于包含锚字符的行
                margin - 相对于页边距
                outsideMargin - 相对于当前页面的外边距
                page - 相对于页面边缘
                paragraph - 相对于包含锚的段落
                topMargin - 相对于上边距
                */
                // 垂直参照
                String relativeY = styles.get("relative-y");

                Element positionV = box.addElement("wp:positionV");
                positionV.addAttribute("relativeFrom",relativeY);
                Element posOffsetV = positionV.addElement("wp:posOffset");
                posOffsetV.setText(offsetY+"");
            }
        }else{
            // 不浮动
            box = draw.addElement("wp:inline");
            box.addAttribute("distT","0");
            box.addAttribute("distB","0");
            box.addAttribute("distL","0");
            box.addAttribute("distR","0");
        }

        Element extent = box.addElement("wp:extent");
        extent.addAttribute("cx", width+"");
        extent.addAttribute("cy", height+"");

        Element effectExtent = box.addElement("wp:effectExtent"); // 边距
        effectExtent.addAttribute("l","0");
        effectExtent.addAttribute("r","0");
        effectExtent.addAttribute("t","0");
        effectExtent.addAttribute("b","0");
        if(isFloat){
            // 浮动时才需要
            // 这个wrapNone位置不能变
            box.addElement("wp:wrapNone");
        }
        Element docPr = box.addElement("wp:docPr");
        int docPrId = NumberUtil.random(0,100);
        docPr.addAttribute("id", docPrId+"");
        docPr.addAttribute("name", "图片"+rdm);
        docPr.addAttribute("descr", img.getName());
        Element cNvGraphicFramePr = box.addElement("wp:cNvGraphicFramePr");
        Element graphicFrameLocks = cNvGraphicFramePr.addElement("a:graphicFrameLocks","http://schemas.openxmlformats.org/drawingml/2006/main");
        graphicFrameLocks.addAttribute("xmlns:a","http://schemas.openxmlformats.org/drawingml/2006/main");
        graphicFrameLocks.addAttribute("noChangeAspect","1");
        Element graphic = box.addElement("a:graphic","http://schemas.openxmlformats.org/drawingml/2006/main");
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
        blip.addAttribute("r:embed", rId);     // 图片资源编号
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

        DocxUtil.after(r, prev);
        return r;

    }

    /**
     * 解析html
     * @param parent 上一级
     * @param prev 放在prev之后
     * @param html html
     * @param styles 样式
     * @param copyPrevStyle 是否复制前一个标签的样式,在替换书签时需要用到,但在div中嵌套的span需要避免复制闰一个标签的样式
     * @return prev
     */
    public Element parseHtml(Element parent, Element prev, Element html, Map<String, String> styles, boolean copyPrevStyle){
        String pname = parent.getName();
        String txt = html.getTextTrim();
        if(html.elements().size()==0){
            txt = txt.trim();
            html.setText(txt);
        }
        Iterator<Node> nodes = html.nodeIterator();
        boolean empty = true;
        while (nodes.hasNext()){
            Node node = nodes.next();
            String tag = node.getName();
            int type = node.getNodeType();
            // Element:1 Attribute:2 Text:3 CDATA:4 Entity:5 Comment:8 Document:9
            if(type == 3){//text
                String text = node.getText().trim();
                if(text.length()>0) {
                    empty = false;
                   Element r = inline(parent, prev, text, styles, copyPrevStyle);
                    prev = r;
                }
            }else if(type == 1 ) {//element
                empty = false;
                Element element = (Element) node;
                Map<String, String> itemStyles = StyleParser.inherit(style(element),styles);
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
                        // 新建一个段落
                    }
                    Element tbl = table(box, prev, element);
                    prev = tbl;
                }else if("div".equalsIgnoreCase(tag)){
                    if("inline".equalsIgnoreCase(display) || "inline-block".equalsIgnoreCase(display)){
                        prev = parseHtml(parent, prev, element, itemStyles, false);
                    }else {
                        prev = block(parent, prev, element, itemStyles);
                    }
                }else if("span".equalsIgnoreCase(tag)){
                    if("block".equalsIgnoreCase(display)){
                        prev = block(parent, prev, element, itemStyles);
                    }else {
                        prev =  parseHtml(parent, prev, element, itemStyles, false);
                    }
                }else if("img".equalsIgnoreCase(tag)){
                    Element img = img(parent, prev, element, styles);
                    prev = img;
                }else if("word".equalsIgnoreCase(tag)){
                    Element word = word(parent, prev, element, styles);
                    prev = word;
                }else if("ol".equalsIgnoreCase(tag)){
                    prev = ol(src, prev, element, itemStyles);
                }else if("li".equalsIgnoreCase(tag)){
                    prev = li(src, prev, element, itemStyles);
                }else if("br".equalsIgnoreCase(tag)){
                    parent.addElement("w:br");
                }else if("u".equalsIgnoreCase(tag)){
                    itemStyles.put("underline","true");
                    prev = parseHtml(parent, prev, element, itemStyles, false);
                }else if("b".equalsIgnoreCase(tag)){
                    itemStyles.put("font-weight","700");
                    prev = parseHtml(parent, prev, element, itemStyles, false);
                }else if("i".equalsIgnoreCase(tag)){
                    itemStyles.put("italics","true");
                    prev = parseHtml(parent, prev, element, itemStyles, false);
                }else if("del".equalsIgnoreCase(tag)){
                    itemStyles.put("strike","true");
                    prev = parseHtml(parent, prev, element, itemStyles, false);
                }else if("sup".equalsIgnoreCase(tag)){
                    itemStyles.put("vertical-align","superscript");
                    prev = parseHtml(parent, prev, element, itemStyles, false);
                }else if("sub".equalsIgnoreCase(tag)){
                    itemStyles.put("vertical-align","subscript");
                    prev = parseHtml(parent, prev, element, itemStyles, false);
                }else{
                    prev = parseHtml(parent, prev, element, itemStyles, false);
                }
            }
        }
        if(empty && "tc".equalsIgnoreCase(pname)){
            parent.addElement("w:p");
        }
        return prev;
    }
    public WDocument remove(Element element){
        element.getParent().remove(element);
        reload();
        return this;
    }
    public WDocument remove(Wtable table){
        Element element = table.getSrc();
        element.getParent().remove(element);
        reload();
        return this;
    }
    public Element parseHtml(Element parent, Element prev, Element html, Map<String, String> styles){
        return parseHtml(parent, prev, html, styles, false);
    }
    public Element p(Element parent, String text, Map<String, String> styles){
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
    public Element r(Element parent, String text, Map<String, String> styles){
        Element r= null;
        if(null != text && text.trim().length()>0) {
            r = parent.addElement("w:r");
            pr(r, styles);
            Element t = r.addElement("w:t");
            if(IS_HTML_ESCAPE) {
                text = HtmlUtil.display(text);
            }
            t.setText(text);
        }
        return r;
    }
    public Map<String, String> style(Element element){
        int index = element.getParent().elements(element.getName()).indexOf(element);
        String nth = ":nth-child(even)";
        if((index+1)%2 ==0){
            nth = ":nth-child(odd)";
        }
        Map<String, String> result = new HashMap<String, String>();
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
            StyleParser.join(result, this.styles.get("."+pc + " "+name), true);
            StyleParser.join(result, this.styles.get("."+pc + " "+name+nth), true);
        }

        if(null != parentName){
            StyleParser.join(result, this.styles.get(parentName + " "+name), true);
            StyleParser.join(result, this.styles.get(parentName + " "+name+nth), true);
        }

        StyleParser.join(result, this.styles.get(name), true);
        StyleParser.join(result, this.styles.get(name+nth), true);

        String clazz = element.attributeValue("class");
        if(null != clazz){
            String[] cs = clazz.split(" ");
            for(String c:cs){
                if(null != parentName){
                    StyleParser.join(result, this.styles.get(parentName + " ."+c), true);
                    StyleParser.join(result, this.styles.get(parentName + " ."+c+nth), true);
                }
                for(String pc:parentClassList){
                    StyleParser.join(result, this.styles.get("."+pc + " ."+c), true);
                    StyleParser.join(result, this.styles.get("."+pc + " ."+nth), true);
                }
                StyleParser.join(result, this.styles.get("."+c), true);
                StyleParser.join(result, this.styles.get("."+c+nth), true);
            }
        }


        String id = element.attributeValue("id");
        if(null != id){
            StyleParser.join(result, this.styles.get("#"+id),true);
        }

        result = StyleParser.parse(result, element.attributeValue("style"),true);
        return result;
    }
    /**
     * 根据关键字查找样式列表ID
     * @return List
     */
    public String listStyle(String key){
        return DocxUtil.listStyle(file, key);
    }

    /**
     * 插入模板文件
     * @param prev 在prev元素之前插入
     * @param file 模板文件
     * @return element
     */
    public Element insert(Element prev, File file){
        Element last = null;
        WDocument idoc = new WDocument(file);
        idoc.load();
        Element src = idoc.getSrc();
        List<Element> inserts = DomUtil.elements(src, "p,tbl");
        int index = index(src, prev);
        List<Element> elements = src.elements();
        for(Element insert:inserts){
            insert.getParent().remove(insert);
            elements.add(index++, insert);
            last = insert;
        }
        return last;
    }

    /**
     * 根据关键字查找样式列表ID
     * @return List
     */
    public List<String> listStyles(){
        return DocxUtil.listStyles(file);
    }


}
