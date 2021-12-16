package org.anyline.office.docx.entity;

import org.anyline.office.docx.util.DocxUtil;
import org.anyline.office.docx.util.StyleParser;
import org.anyline.util.DomUtil;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Wtc {
    private WDocument doc;
    private Element src;
    private Wtr parent;
    private List<Wp> wps = new ArrayList<>();
    public Wtc(WDocument doc, Wtr parent, Element src){
        this.doc = doc;
        this.src = src;
        this.parent = parent;
        load();
    }
    public Wtc reload(){
        load();
        return this;
    }
    private Wtc load(){
        wps.clear();
        List<Element> ps = src.elements("p");
        for(Element p:ps){
            Wp wp = new Wp(doc, p);
            wps.add(wp);
        }
        return this;
    }
    public WDocument getDoc() {
        return doc;
    }

    public void setDoc(WDocument doc) {
        this.doc = doc;
    }

    public Element getSrc() {
        return src;
    }

    public void setSrc(Element src) {
        this.src = src;
    }
    public List<String> getBookmarks(){
        List<String> list = new ArrayList<>();
        List<Element> marks = DomUtil.elements(src, "bookmarkStart");
        for(Element mark:marks){
            list.add(mark.attributeValue("name"));
        }
        return list;
    }
    public String getBookmark(){
        Element mark = DomUtil.element(src, "bookmarkStart");
        if(null != mark){
            return mark.attributeValue("name");
        }
        return null;
    }
    public Wtc setBorder(String side, String style){
        return this;
    }
    public Wtc left(){
        Wtc left = null;
        List<Wtc> tcs = parent.getTcs();
        int index = tcs.indexOf(this);
        if(index > 0){
            left = tcs.get(index-1);
        }
        return left;
    }
    public Wtc right(){
        Wtc right = null;
        List<Wtc> tcs = parent.getTcs();
        int index = tcs.indexOf(this);
        if(index < tcs.size()-1){
            right = tcs.get(index+1);
        }
        return right;
    }
    public Wtc bottom(){
        Wtc bottom = null;
        Wtable table = parent.getParent();
        List<Wtr> trs = table.getTrs();
        int y = trs.indexOf(parent);
        if(y < trs.size()-1){
            Wtr tr = trs.get(y+1);
            int x = parent.getTcs().indexOf(this);
            bottom = tr.getTc(x);
        }
        return bottom;
    }
    public Wtc top(){
        Wtc top = null;
        Wtable table = parent.getParent();
        List<Wtr> trs = table.getTrs();
        int y = trs.indexOf(parent);
        if(y < trs.size()-1){
            Wtr tr = trs.get(y-1);
            int x = parent.getTcs().indexOf(this);
            top = tr.getTc(x);
        }
        return top;
    }
    public Wtc removeLeftBorder(){
        removeBorder(src, "left");
        Wtc left = left();
        if(null != left) {
            removeBorder(left.getSrc(), "right");
        }
        return this;
    }
    public Wtc removeRightBorder(){
        removeBorder(src, "right");
        Wtc right = right();
        if(null != right) {
            removeBorder(right.getSrc(), "left");
        }
        return this;
    }
    public Wtc removeTopBorder(){
        removeBorder(src, "top");
        Wtc top = top();
        if(null != top) {
            removeBorder(top.getSrc(), "bottom");
        }
        return this;
    }
    public Wtc removeBottomBorder(){
        removeBorder(src, "bottom");
        Wtc bottom = bottom();
        if(null != bottom) {
            removeBorder(bottom.getSrc(), "top");
        }
        return this;
    }
    public Wtc removeTl2brBorder(){
        removeBorder(src, "tl2br");
        return this;
    }
    public Wtc removeTr2blBorder(){
        removeBorder(src, "tr2bl");
        return this;
    }
    private void removeBorder(Element tc, String side){
        Element tcPr = DocxUtil.addElement(tc, "tcPr");
        Element borders = DocxUtil.addElement(tcPr, "tcBorders");
        Element border = DocxUtil.addElement(borders, side);
        border.addAttribute("val","nil");
        DocxUtil.removeAttribute(border, "sz");
        DocxUtil.removeAttribute(border, "space");
        DocxUtil.removeAttribute(border, "color");
    }

    public Wtc removeBorder(){
        removeLeftBorder();
        removeRightBorder();
        removeTopBorder();
        removeBottomBorder();
        removeTl2brBorder();
        removeTr2blBorder();
        return this;
    }
    public Wtc setBorder(){
        setLeftBorder();
        setRightBorder();
        setTopBorder();
        setBottomBorder();
        setTl2brBorder();
        setTr2blBorder();
        return this;
    }
    public Wtc setBorder(int size, String color, String style){
        setLeftBorder(size, color, style);
        setRightBorder(size, color, style);
        setTopBorder(size, color, style);
        setBottomBorder(size, color, style);
        setTl2brBorder(size, color, style);
        setTr2blBorder(size, color, style);
        return this;
    }
    public Wtc setLeftBorder(){
        setBorder(src, "left", 4, "auto", "single");
        return this;
    }
    public Wtc setRightBorder(){
        setBorder(src, "right", 4, "auto", "single");
        return this;
    }
    public Wtc setTopBorder(){
        setBorder(src, "top", 4, "auto", "single");
        return this;
    }
    public Wtc setBottomBorder(){
        setBorder(src, "bottom", 4, "auto", "single");
        return this;
    }
    public Wtc setTl2brBorder(){
        setBorder(src, "tl2br", 4, "auto", "single");
        return this;
    }
    public Wtc setTr2blBorder(){
        setBorder(src, "tr2bl", 4, "auto", "single");
        return this;
    }

    public Wtc setLeftBorder(int size, String color, String style){
        setBorder(src, "left", size, color, style);
        return this;
    }
    public Wtc setRightBorder(int size, String color, String style){
        setBorder(src, "right", size, color, style);
        return this;
    }
    public Wtc setTopBorder(int size, String color, String style){
        setBorder(src, "top", size, color, style);
        return this;
    }
    public Wtc setBottomBorder(int size, String color, String style){
        setBorder(src, "bottom", size, color, style);
        return this;
    }
    public Wtc setTl2brBorder(int size, String color, String style){
        setBorder(src, "tl2br", size, color, style);
        return this;
    }
    public Wtc setTr2blBorder(int size, String color, String style){
        setBorder(src, "tr2bl", size, color, style);
        return this;
    }
    private void setBorder(Element tc, String side, int size, String color, String style){
        Element tcPr = DocxUtil.addElement(tc, "tcPr");
        Element borders = DocxUtil.addElement(tcPr, "tcBorders");
        Element border = DocxUtil.addElement(borders, side);
        border.addAttribute("val",style);
        border.addAttribute("sz",size+"");
        border.addAttribute("color",color.replace("#",""));
        border.addAttribute("space","0");
    }
    public Wtc setColor(String color){
        for(Wp wp:wps){
            wp.setColor(color);
        }
        return this;
    }
    public Wtc setFont(String size, String eastAsia, String ascii, String hint){
        for(Wp wp:wps){
            wp.setFont(size, eastAsia, ascii, hint);
        }
        return this;
    }
    public Wtc setFontSize(String size){
        for(Wp wp:wps){
            wp.setFontSize(size);
        }
        return this;
    }
    public Wtc setFontFamily(String font){
        for(Wp wp:wps){
            wp.setFontFamily(font);
        }
        return this;
    }
    public Wtc setWidth(String width){
        Element pr = DocxUtil.addElement(src, "tcPr");
        DocxUtil.addElement(pr, "tcW","w", DocxUtil.dxa(width)+"");
        DocxUtil.addElement(pr, "tcW","type", DocxUtil.widthType(width));
        return this;
    }

    public Wtc setAlign(String align){
        Element pr = DocxUtil.addElement(src, "tcPr");
        DocxUtil.addElement(pr, "jc","val", align);
        for(Wp wp:wps){
            wp.setAlign(align);
        }
        return this;
    }
    public Wtc setVerticalAlign(String align){
        Element pr = DocxUtil.addElement(src, "tcPr");
        DocxUtil.addElement(pr,"vAlign", "val", align );
        return this;
    }

    /**
     * 背景色
     * @param color 颜色
     * @return Wtc
     */
    public Wtc setBackgroundColor(String color){
        Element pr = DocxUtil.addElement(src, "tcPr");
        DocxUtil.addElement(pr, "shd", "color","auto");
        DocxUtil.addElement(pr, "shd", "val","clear");
        DocxUtil.addElement(pr, "shd", "fill",color.replace("#",""));
        for(Wp wp:wps){
            wp.setBackgroundColor(color);
        }
        return this;
    }

    /**
     * 清除样式
     * @return
     */
    public Wtc removeStyle(){
        Element pr = src.element("tcPr");
        if(null != pr){
            src.remove(pr);
        }
        for(Wp wp:wps){
            wp.removeStyle();
        }
        return this;
    }
    /**
     * 清除背景色
     * @return
     */
    public Wtc removeBackgroundColor(){
        DocxUtil.removeElement(src,"shd");
        return this;
    }

    /**
     * 清除颜色
     * @return wtc
     */
    public Wtc removeColor(){
        DocxUtil.removeElement(src,"color");
        return this;
    }
    /**
     * 粗体
     * @param bold 是否
     * @return Wtc
     */
    public Wtc setBold(boolean bold){
        for(Wp wp:wps){
            wp.setBold(bold);
        }
        return this;
    }
    public Wtc setBold(){
        setBold(true);
        return this;
    }

    /**
     * 下划线
     * @param underline 是否
     * @return Wtc
     */
    public Wtc setUnderline(boolean underline){
        for(Wp wp:wps){
            wp.setUnderline(underline);
        }
        return this;
    }
    public Wtc setUnderline(){
        setUnderline(true);
        return this;
    }

    /**
     * 删除线
     * @param strike 是否
     * @return Wtc
     */
    public Wtc setStrike(boolean strike){
        for(Wp wp:wps){
            wp.setStrike(strike);
        }
        return this;
    }
    public Wtc setStrike(){
        setStrike(true);
        return this;
    }

    /**
     * 斜体
     * @param italic 是否
     * @return Wtc
     */
    public Wtc setItalic(boolean italic){
        for(Wp wp:wps){
            wp.setItalic(italic);
        }
        return this;
    }

    public Wtc setItalic(){
        return setItalic(true);
    }
    public List<Wp> getWps(){
        return wps;
    }
    public Wtc setHtml(String html){

        DocxUtil.removeContent(src);
        try {
            Document doc = DocumentHelper.parseText("<body>"+html+"</body>");
            Element root = doc.getRootElement();
            this.doc.block(src, null, root, null);
        }catch (Exception e){
            e.printStackTrace();
        }
        return this;
    }
    public Wtc setHtml(Element html){
        String tag = html.getName();
        DocxUtil.removeContent(src);
        List<Element> elements = html.elements();
        if(html.elements().size()>0){
            doc.block(src, null, html, null);
        }else{
            setText(html.getText(), StyleParser.parse(html.attributeValue("style")));
        }
        return this;
    }
    public void remove(){
        src.getParent().remove(src);
        parent.getTcs().remove(this);
    }
    public Wtc setText(String text){
        setText(text, null);
        return this;
    }
    public Wtc setText(String text, Map<String,String> styles){
        DocxUtil.removeContent(src);
        Element p = DocxUtil.addElement(src, "p");
        Element r = DocxUtil.addElement(p, "r");
        Element t = DocxUtil.addElement(r, "t");
        t.setText(text);
        DocxUtil.pr(r, styles);
        return this;
    }
    public Wtc addText(String text){
        DocxUtil.removeContent(src);
        Element p = DocxUtil.addElement(src, "p");
        Element r = DocxUtil.addElement(p, "r");
        Element t = r.addElement("w:t");
        t.setText(text);
        return this;
    }
    public List<String> getTexts(){
        List<String> texts = new ArrayList<>();
        List<Element> ts = DomUtil.elements(src, "t");
        for(Element t:ts){
            texts.add(t.getTextTrim());
        }
        return texts;
    }
    public String getText(){
        Element t = src.element("t");
        if(null != t){
            return t.getText();
        }
        return null;
    }

}