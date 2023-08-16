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

import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.DomUtil;
import org.anyline.util.HtmlUtil;
import org.anyline.util.StyleParser;
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
    private String widthUnit = "px";     // 默认长度单位 px pt cm/厘米
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

    /**
     * 当前单元格内所有书签名称
     * @return list
     */
    public List<String> getBookmarks(){
        List<String> list = new ArrayList<>();
        List<Element> marks = DomUtil.elements(src, "bookmarkStart");
        for(Element mark:marks){
            list.add(mark.attributeValue("name"));
        }
        return list;
    }

    /**
     * 当前单元格内第一个书签名称
     * @return String
     */
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

    /**
     * 宽度尺寸单位
     * @return String
     */
    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
    }

    /**
     * 当前单元格合并列数量
     * @return colspan
     */
    public int getColspan(){
        Element tcPr = src.element("tcPr");
        if(null != tcPr){
            Element gridSpan = tcPr.element("gridSpan");
            if(null != gridSpan){
                return BasicUtil.parseInt(gridSpan.attributeValue("val"), 1);
            }
        }
        return 1;
    }

    /**
     * 当前单元格合并行数量,被合并返回-1
     * @return rowspan
     */
    public int getRowspan(){
        Element tcPr = src.element("tcPr");
        if(null != tcPr){
            Element vMerge = tcPr.element("vMerge");
            if(null != vMerge){
                String val = vMerge.attributeValue("val");
                if(!"restart".equalsIgnoreCase(val)){
                    return -1;
                }
            }
        }
        return 1;
    }

    /**
     * 当前单元格 左侧单元格
     * @return wtc
     */
    public Wtc left(){
        Wtc left = null;
        List<Wtc> tcs = parent.getTcs();
        int index = tcs.indexOf(this);
        if(index > 0){
            left = tcs.get(index-1);
        }
        return left;
    }
    /**
     * 当前单元格 右侧单元格
     * @return wtc
     */
    public Wtc right(){
        Wtc right = null;
        List<Wtc> tcs = parent.getTcs();
        int index = tcs.indexOf(this);
        if(index < tcs.size()-1){
            right = tcs.get(index+1);
        }
        return right;
    }

    /**
     * 当前单元格 下方单元格
     * @return wtc
     */
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
    /**
     * 当前单元格 上方单元格
     * @return wtc
     */
    public Wtc top(){
        Wtc top = null;
        Wtable table = parent.getParent();
        List<Wtr> trs = table.getTrs();
        int y = trs.indexOf(parent);
        if(y < trs.size()-1 && y>0){
            Wtr tr = trs.get(y-1);
            int x = parent.getTcs().indexOf(this);
            top = tr.getTc(x);
        }
        return top;
    }
    /**
     * 删除左边框
     * @return wtc
     */
    public Wtc removeLeftBorder(){
        removeBorder(src, "left");
        Wtc left = left();
        if(null != left) {
            removeBorder(left.getSrc(), "right");
        }
        return this;
    }
    /**
     * 删除右边框
     * @return wtc
     */
    public Wtc removeRightBorder(){
        removeBorder(src, "right");
        Wtc right = right();
        if(null != right) {
            removeBorder(right.getSrc(), "left");
        }
        return this;
    }
    /**
     * 删除上边框
     * @return wtc
     */
    public Wtc removeTopBorder(){
        removeBorder(src, "top");
        Wtc top = top();
        if(null != top) {
            removeBorder(top.getSrc(), "bottom");
        }
        return this;
    }
    /**
     * 删除下边框
     * @return wtc
     */
    public Wtc removeBottomBorder(){
        removeBorder(src, "bottom");
        Wtc bottom = bottom();
        if(null != bottom) {
            removeBorder(bottom.getSrc(), "top");
        }
        return this;
    }

    /**
     * 删除左上至右下分隔线
     * @return wtc
     */
    public Wtc removeTl2brBorder(){
        removeBorder(src, "tl2br");
        return this;
    }
    /**
     * 删除右上至左下分隔线
     * @return wtc
     */
    public Wtc removeTr2blBorder(){
        removeBorder(src, "tr2bl");
        return this;
    }
    /**
     * 删除边框
     * @return wtc
     */
    private void removeBorder(Element tc, String side){
        Element tcPr = DocxUtil.addElement(tc, "tcPr");
        Element borders = DocxUtil.addElement(tcPr, "tcBorders");
        Element border = DocxUtil.addElement(borders, side);
        border.addAttribute("w:val","nil");
        DocxUtil.removeAttribute(border, "sz");
        DocxUtil.removeAttribute(border, "space");
        DocxUtil.removeAttribute(border, "color");
    }


    /**
     * 删除所有
     * @return wtc
     */
    public Wtc removeBorder(){
        removeLeftBorder();
        removeRightBorder();
        removeTopBorder();
        removeBottomBorder();
        removeTl2brBorder();
        removeTr2blBorder();
        return this;
    }
    /**
     * 设置上下左右默认边框
     * @return wtc
     */
    public Wtc setBorder(){
        setLeftBorder();
        setRightBorder();
        setTopBorder();
        setBottomBorder();
        return this;
    }

    /**
     * 设置上下左右边框
     * @param size 边框宽度(1px)
     * @param color 颜色
     * @param style 样式(single)
     * @return wtc
     */
    public Wtc setBorder(int size, String color, String style){
        setLeftBorder(size, color, style);
        setRightBorder(size, color, style);
        setTopBorder(size, color, style);
        setBottomBorder(size, color, style);
        setTl2brBorder(size, color, style);
        setTr2blBorder(size, color, style);
        return this;
    }
    /**
     * 设置左默认边框
     * @return wtc
     */
    public Wtc setLeftBorder(){
        setBorder(src, "left", 4, "auto", "single");
        return this;
    }
    /**
     * 设置右默认边框
     * @return wtc
     */
    public Wtc setRightBorder(){
        setBorder(src, "right", 4, "auto", "single");
        return this;
    }
    /**
     * 设置上默认边框
     * @return wtc
     */
    public Wtc setTopBorder(){
        setBorder(src, "top", 4, "auto", "single");
        return this;
    }
    /**
     * 设置下默认边框
     * @return wtc
     */
    public Wtc setBottomBorder(){
        setBorder(src, "bottom", 4, "auto", "single");
        return this;
    }
    /**
     * 设置左上至右下默认边框
     * @return wtc
     */
    public Wtc setTl2brBorder(){
        setBorder(src, "tl2br", 4, "auto", "single");
        return this;
    }

    /**
     * 设置 左上 至 右下分隔线
     * @param top 右上内容
     * @param bottom 左下内容
     * @return wtc
     */
    public Wtc setTl2brBorder(String top, String bottom){
        setBorder(src, "tl2br", 4, "auto", "single");
        String html = "<div style='text-align:right;'>"+top+"</div><div style='text-align:left;'>"+bottom+"</div>";
        setHtml(html);
        return this;
    }
    /**
     * 设置 左上 至 右下默认样式分隔线
     * @return wtc
     */
    public Wtc setTr2blBorder(){
        setBorder(src, "tr2bl", 4, "auto", "single");
        return this;
    }
    /**
     * 设置 右上 至 左下分隔线
     * @param top 左上内容
     * @param bottom 右下内容
     * @return wtc
     */
    public Wtc setTr2blBorder(String top, String bottom){
        setBorder(src, "tr2bl", 4, "auto", "single");
        String html = "<div style='text-align:left;'>"+top+"</div><div style='text-align:right;'>"+bottom+"</div>";
        setHtml(html);
        return this;
    }

    /**
     * 设置左边框
     * @param size 边框宽度(1px)
     * @param color 颜色
     * @param style 样式(single)
     * @return wtc
     */
    public Wtc setLeftBorder(int size, String color, String style){
        setBorder(src, "left", size, color, style);
        return this;
    }
    /**
     * 设置右边框
     * @param size 边框宽度(1px)
     * @param color 颜色
     * @param style 样式(single)
     * @return wtc
     */
    public Wtc setRightBorder(int size, String color, String style){
        setBorder(src, "right", size, color, style);
        return this;
    }
    /**
     * 设置上边框
     * @param size 边框宽度(1px)
     * @param color 颜色
     * @param style 样式(single)
     * @return wtc
     */
    public Wtc setTopBorder(int size, String color, String style){
        setBorder(src, "top", size, color, style);
        return this;
    }
    /**
     * 设置下边框
     * @param size 边框宽度(1px)
     * @param color 颜色
     * @param style 样式(single)
     * @return wtc
     */
    public Wtc setBottomBorder(int size, String color, String style){
        setBorder(src, "bottom", size, color, style);
        return this;
    }
    /**
     * 设置左上至右下边框
     * @param size 边框宽度(1px)
     * @param color 颜色
     * @param style 样式(single)
     * @return wtc
     */
    public Wtc setTl2brBorder(int size, String color, String style){
        setBorder(src, "tl2br", size, color, style);
        return this;
    }
    /**
     * 设置右上至左下边框
     * @param size 边框宽度(1px)
     * @param color 颜色
     * @param style 样式(single)
     * @return wtc
     */
    public Wtc setTr2blBorder(int size, String color, String style){
        setBorder(src, "tr2bl", size, color, style);
        return this;
    }
    private void setBorder(Element tc, String side, int size, String color, String style){
        Element tcPr = DocxUtil.addElement(tc, "tcPr");
        Element borders = DocxUtil.addElement(tcPr, "tcBorders");
        Element border = DocxUtil.addElement(borders, side);
        border.addAttribute("w:val",style);
        border.addAttribute("w:sz",size+"");
        border.addAttribute("w:color",color.replace("#",""));
        border.addAttribute("w:space","0");
    }

    /**
     * 设置下边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setBottomPadding(String padding){
        return setPadding(src, "bottom", padding);
    }
    /**
     * 设置下边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setBottomPadding(int padding){
        return setPadding(src, "bottom", padding);
    }

    /**
     * 设置下边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setBottomPadding(double padding){
        return setPadding(src, "bottom", padding);
    }

    /**
     * 设置上边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setTopPadding(String padding){
        return setPadding(src, "top", padding);
    }
    /**
     * 设置上边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setTopPadding(int padding){
        return setPadding(src, "top", padding);
    }
    /**
     * 设置上边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setTopPadding(double padding){
        return setPadding(src, "top", padding);
    }

    /**
     * 设置右边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setRightPadding(String padding){
        return setPadding(src, "right", padding);
    }
    /**
     * 设置右边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setRightPadding(int padding){
        return setPadding(src, "right", padding);
    }
    /**
     * 设置右边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setRightPadding(double padding){
        return setPadding(src, "right", padding);
    }

    /**
     * 设置左边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setLeftPadding(String padding){
        return setPadding(src, "left", padding);
    }
    /**
     * 设置左边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setLeftPadding(int padding){
        return setPadding(src, "left", padding);
    }
    /**
     * 设置左边距
     * @param padding 边距
     * @return wtc
     */
    public Wtc setLeftPadding(double padding){
        return setPadding(src, "left", padding);
    }


    public Wtc setPadding(String side, String padding){
        return setPadding(src, side, padding);
    }
    public Wtc setPadding(String side, int padding){
        return setPadding(src, side, padding);
    }
    public Wtc setPadding(String side, double padding){
        return setPadding(src, side, padding);
    }


    public Wtc setPadding(String padding){
        setPadding(src, "top", padding);
        setPadding(src, "bottom", padding);
        setPadding(src, "right", padding);
        setPadding(src, "left", padding);
        return this;
    }
    public Wtc setPadding(int padding){
        return setPadding(padding+widthUnit);
    }
    public Wtc setPadding(double padding){
        return setPadding(padding+widthUnit);
    }


    private Wtc setPadding(Element tc, String side, int padding){
        return setPadding(tc, side, padding+widthUnit);
    }
    private Wtc setPadding(Element tc, String side, double padding){
        return setPadding(tc, side, padding+widthUnit);
    }
    private Wtc setPadding(Element tc, String side, String padding){
        Element pr = DocxUtil.addElement(tc, "tcPr");
        Element mar = DocxUtil.addElement(pr,"tcMar");
        DocxUtil.addElement(mar,side,"w",DocxUtil.dxa(padding)+"");
        DocxUtil.addElement(mar,side,"type","dxa");
        return this;
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
    public Wtc setWidth(int width){
        return setWidth(widthUnit+widthUnit);
    }
    public Wtc setWidth(double width){
        return setWidth(widthUnit+widthUnit);
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
        if(align.equals("middle")){
            align = "center";
        }
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
            // wp.setBackgroundColor(color);
        }
        return this;
    }

    /**
     * 清除样式
     * @return Wtc
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
     * @return Wtc
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
            if(doc.IS_HTML_ESCAPE){
                html = HtmlUtil.name2code(html);
            }
            Document doc = DocumentHelper.parseText("<root>"+html+"</root>");
            Element root = doc.getRootElement();
            this.doc.parseHtml(src, null, root, null, false);
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
    public Wtc setText(String text, Map<String, String> styles){
        DocxUtil.removeContent(src);
        Element p = DocxUtil.addElement(src, "p");
        Element r = DocxUtil.addElement(p, "r");
        Element t = DocxUtil.addElement(r, "t");
        if(doc.IS_HTML_ESCAPE) {
            text = HtmlUtil.display(text);
        }
        t.setText(text);
        DocxUtil.pr(r, styles);
        return this;
    }
    public Wtc addText(String text){
        Element p = DocxUtil.addElement(src, "p");
        Element r = DocxUtil.addElement(p, "r");
        Element t = r.addElement("w:t");
        if(doc.IS_HTML_ESCAPE) {
            text = HtmlUtil.display(text);
        }
        t.setText(text);
        return this;
    }
    public List<String> getTextList(){
        List<String> texts = new ArrayList<>();
        List<Element> ts = DomUtil.elements(src, "t");
        for(Element t:ts){
            texts.add(t.getTextTrim());
        }
        return texts;
    }
    public String getTexts(){
        List<String> list = getTextList();
        String texts = "";
        for(String item:list){
            if(null != item){
                texts += item;
            }
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
    public Wtc replace(String src, String tar){
        for(Wp wp:wps){
            wp.replace(src, tar);
        }
        return this;
    }

}