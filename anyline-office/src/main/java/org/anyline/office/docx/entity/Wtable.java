package org.anyline.office.docx.entity;

import org.anyline.entity.html.TableBuilder;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.StyleParser;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Wtable {
    private WDocument doc;
    private Element src;
    private String widthUnit = "px";     //默认长度单位 px pt cm/厘米
    private List<Wtr> wtrs = new ArrayList<>();
    private boolean isAutoLoad = true;
    public Wtable(WDocument doc){
        this.doc = doc;
        load();
    }
    public Wtable(WDocument doc, Element src){
        this.doc = doc;
        this.src = src;
        load();
    }
    public void reload(){
        load();
    }
    private void load(){
        wtrs.clear();
        List<Element> elements = src.elements("tr");
        for(Element element:elements){
            Wtr tr = new Wtr(doc, this, element);
            wtrs.add(tr);
        }
    }

    public Wtr getTr(String bookmark){
        Element src = getParent(bookmark, "tr");
        Wtr tr = new Wtr(doc,this, src);
        return tr;
    }


    public Element getParent(String bookmark, String tag){
        return doc.getParent(bookmark, tag);
    }


    private Wtr tr(Wtr template, Element src){
        Wtr tr = new Wtr(doc, this, template.getSrc().createCopy());
        tr.removeContent();
        List<Element> tds = src.elements("td");
        for(int i=0; i<tds.size(); i++){
            Wtc wtc = tr.getTc(i);
            Element td = tds.get(i);
            Map<String,String> styles = StyleParser.parse(td.attributeValue("style"));
            wtc.setHtml(td);
            //this.doc.block(tc, null, td, null);
            /*Element t = DomUtil.element(tc,"t");
            if(null == t){
                t = tc.element("p").addElement("w:r").addElement("w:t");
            }
            String text = td.getTextTrim();
            t.setText(td.getTextTrim());*/

        }
        return tr;
    }
    private Wtr tr(Element src){
        Element tr = this.src.addElement("w:tr");
        Wtr wtr = new Wtr(this.doc, this, tr);
        List<Element> tds = src.elements("td");
        for(int i=0; i<tds.size(); i++){
            Element tc = tr.addElement("w:tc");
            Wtc wtc = new Wtc(doc, wtr, tc);
            Element td = tds.get(i);
            wtc.setHtml(td);
        }
        return wtr;
    }

    public void insert(String html){
        insert(-1, html);
    }
    public void insert(Object data, String ... cols){
        insert(-1, null, data, cols);
    }
    public void insert(int index, Object data, String ... cols){
        Wtr template = null;
        insert(index, template, data, cols);
    }

    /**
     * 根据模版样式和数据 插入行
     * @param template 模版行
     * @param data 数据可以是一个实体也可以是一个集合
     * @param cols 指定从数据中提取的数据的属性或key
     */
    public void insert(Wtr template, Object data, String ... cols){
        insert(-1, template, data, cols);
    }
    /**
     * 根据模版样式和数据 插入行
     * @param index 插入位置
     * @param template 模版行
     * @param data 数据可以是一个实体也可以是一个集合
     * @param cols 指定从数据中提取的数据的属性或key
     */
    public void insert(int index, Wtr template, Object data, String ... cols){
        Collection datas = null;
        if(data instanceof Collection){
            datas = (Collection)data;
        }else{
            datas = new ArrayList();
            datas.add(data);
        }
        TableBuilder builder = TableBuilder.init().setFields(cols).setDatas(datas);
        String html = builder.build().build(false);
        insert(index, template, html);
    }

    public Wtable insert(int index, Wtr template, int qty){
        List<Element> trs = src.elements("tr");
        for(int i=0; i<qty; i++) {
            Element newTr = template.getSrc().createCopy();
            DocxUtil.removeContent(newTr);
            if(index != -1){
                trs.add(index++, newTr);
            }else {
                trs.add(newTr);
            }
        }
        reload();
        return this;
    }
    public Wtable insert(int index, int qty){
        if(index < wtrs.size()-1){
            Wtr template = wtrs.get(index-1);
            return insert(index, template, qty);
        }
        return this;
    }
    public void insert(int index, String html){
        List<Element> trs = src.elements("tr");
        Wtr template = null;//以最后一行作模板
        if(trs.size() > 1){
            template = new Wtr(doc, this, trs.get(trs.size()-1));
            if(index == -1){
                index = trs.size()-1;
            }
        }
        insert(-1, template, html);
    }
    public void insert(Wtr template, String html){
        int index = -1;
        if(null != template) {
            List<Element> trs = src.elements("tr");
            index = trs.indexOf(template.getSrc());
        }
        insert(index,template,  html);
    }

    /**
     * 根据模版样式 插入行
     * @param index 插入位置下标
     * @param template 模版行
     * @param html html片段 片段中应该有多个tr,不需要上级标签table
     */
    public void insert(int index, Wtr template, String html){
        List<Element> trs = src.elements("tr");
        if(index == -1 && null != template){
            index = trs.indexOf(template.getSrc());
        }
        try {
            org.dom4j.Document doc = DocumentHelper.parseText("<root>"+html+"</root>");
            Element root = doc.getRootElement();
            List<Element> rows = root.elements("tr");
            for(Element row:rows){
                Element newTr = null;
                if(null != template) {
                    newTr = tr(template, row).getSrc();
                }else{
                    newTr = tr(row).getSrc();
                    trs.remove(newTr);
                }
                if(index >= 0) {
                    trs.add(index++, newTr);
                }else{
                    trs.add(newTr);
                }
            }
            if(isAutoLoad) {
                reload();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void remove(int index){
        List<Element> trs = src.elements("tr");
        if(index < trs.size() && index >=0){
            trs.remove(index);
        }
        if(isAutoLoad) {
            reload();
        }
    }
    public void remove(Wtr tr){
        List<Element> trs = src.elements("tr");
        trs.remove(tr.getSrc());
        if(isAutoLoad) {
            reload();
        }
    }
    public String getText(int rows, int cols){
        String text = null;
        List<Element> trs = src.elements("tr");
        Element tr = trs.get(rows);
        List<Element> tcs = tr.elements("tc");
        Element tc = tcs.get(cols);
        text = DocxUtil.text(tc);
        return text;
    }
    public Wtable setText(int rows, int cols, String text){
        return setText(rows, cols, text, null);
    }
    public Wtable setText(int rows, int cols, String text, Map<String,String> styles){
        Wtc tc = getTc(rows, cols);
        if(null != tc){
            tc.setText(text, styles);
        }
        return this;
    }
    public Wtable setHtml(int rows, int cols, String html){
        Wtc tc = getTc(rows, cols);
        if(null != tc) {
            tc.setHtml(html);
        }
        return this;
    }
    public Wtable addColumns(int qty){
        List<Element> trs = src.elements("tr");
        for(Element tr:trs){
            List<Element> tcs = tr.elements();
            if(tcs.size()>0){
                Element tc = tcs.get(tcs.size()-1);
                for (int i = 0; i < qty; i++) {
                    Element newTc = tc.createCopy();
                    DocxUtil.removeContent(newTc);
                    tr.add(newTc);
                }
            }else {
                for (int i = 0; i < qty; i++) {
                    tr.addElement("w:tc").addElement("w:p");
                }
            }
        }
        if(isAutoLoad) {
            reload();
        }
        return this;
    }
    public Wtable addRows(int index, int qty){
        List<Element> trs = src.elements("tr");
        if(trs.size()>0){
            Element tr = null;
            if(index == -1) {
                tr = trs.get(trs.size() - 1);
            }else{
                tr = trs.get(index-1);
            }
            for(int i=0; i<qty; i++) {
                Element newTr = tr.createCopy();
                DocxUtil.removeContent(newTr);
                if(index != -1){
                    trs.add(index++, newTr);
                }else {
                    trs.add(newTr);
                }
            }
        }
        if(isAutoLoad) {
            reload();
        }
        return this;
    }
    public Wtable addRows(int qty){
        return addRows(-1, qty);
    }
    public int getTrSize(){
        return src.elements("tr").size();
    }
    public Wtable setWidth(String width){
        Element pr = DocxUtil.addElement(src, "tblPr");
        DocxUtil.addElement(pr, "tcW","w", DocxUtil.dxa(width)+"");
        DocxUtil.addElement(pr, "tcW","type", DocxUtil.widthType(width));
        return this;
    }

    public Wtable setWidth(int width){
        return setWidth(width+widthUnit);
    }
    public Wtable setWidth(double width){
        return setWidth(width+widthUnit);
    }

    public Wtable merge(int rows, int cols, int rowspan, int colspan){
        reload();
        for(int r=rows; r<rows+rowspan; r++){
            for(int c=cols; c<cols+colspan; c++){
                Wtc tc = getTc(r, c);
                Element pr = DocxUtil.addElement(tc.getSrc(), "tcPr");
                if(rowspan > 1){
                    if(r==rows){
                        DocxUtil.addElement(pr, "vMerge", "val",   "restart");
                    }else{
                        DocxUtil.addElement(pr, "vMerge");
                    }
                }
                if(colspan>1){
                    if(c==cols){
                        DocxUtil.addElement(pr, "gridSpan", "val",   colspan+"");
                    }else{
                        tc.remove();
                    }
                }
            }
        }
        reload();
        return this;
    }
    public List<Wtr> getTrs(){
        return wtrs;
    }
    public Wtr getTr(int index){
        return wtrs.get(index);
    }
    public Wtc getTc(int rows, int cols){
        Wtr wtr = getTr(rows);
        if(null == wtr){
            return null;
        }
        return wtr.getTc(cols);
    }

    public Wtable removeBorder(){
        removeTopBorder();
        removeBottomBorder();
        removeLeftBorder();
        removeRightBorder();
        removeInsideHBorder();
        removeInsideVBorder();
        removeTl2brBorder();
        removeTr2blBorder();
        return this;
    }


    /**
     * 清除表格上边框
     * @return wtable
     */
    public Wtable removeTopBorder(){
        removeBorder(src, "top");
        return this;
    }
    /**
     * 清除表格左边框
     * @return wtable
     */
    public Wtable removeLeftBorder(){
        removeBorder(src, "left");
        return this;
    }
    /**
     * 清除表格右边框
     * @return wtable
     */
    public Wtable removeRightBorder(){
        removeBorder(src, "right");
        return this;
    }
    /**
     * 清除表格下边框
     * @return wtable
     */
    public Wtable removeBottomBorder(){
        removeBorder(src, "bottom");
        return this;
    }
    /**
     * 清除表格垂直边框
     * @return wtable
     */
    public Wtable removeInsideVBorder(){
        removeBorder(src, "insideV");
        return this;
    }
    public Wtable removeTl2brBorder(){
        removeBorder(src, "tl2br");
        return this;
    }
    public Wtable removeTr2blBorder(){
        removeBorder(src, "tr2bl");
        return this;
    }

    /**
     * 清除表格水平边框
     * @return wtable
     */
    public Wtable removeInsideHBorder(){
        removeBorder(src, "insideH");
        return this;
    }
    /**
     * 清除所有单元格边框
     * @return wtable
     */
    public Wtable removeTcBorder(){
        for(Wtr tr:wtrs){
            List<Wtc> tcs = tr.getTcs();
            for(Wtc tc:tcs){
                tc.removeBorder();
            }
        }
        return this;
    }

    /**
     * 清除所有单元格颜色
     * @return wtable
     */
    public Wtable removeTcColor(){
        for(Wtr tr:wtrs){
            List<Wtc> tcs = tr.getTcs();
            for(Wtc tc:tcs){
                tc.removeColor();
            }
        }
        return this;
    }

    /**
     * 清除所有单元格背景色
     * @return wtable
     */
    public Wtable removeTcBackgroundColor(){
        for(Wtr tr:wtrs){
            List<Wtc> tcs = tr.getTcs();
            for(Wtc tc:tcs){
                tc.removeBackgroundColor();
            }
        }
        return this;
    }


    private void removeBorder(Element tbl, String side){
        Element tcPr = DocxUtil.addElement(tbl, "tblPr");
        Element borders = DocxUtil.addElement(tcPr, "tblBorders");
        Element border = DocxUtil.addElement(borders, side);
        border.addAttribute("w:val","nil");
        DocxUtil.removeAttribute(border, "sz");
        DocxUtil.removeAttribute(border, "space");
        DocxUtil.removeAttribute(border, "color");
    }

    /**
     * 删除整行的上边框
     * @param rows rows
     * @return Wtr
     */
    public Wtr removeTopBorder(int rows){
        Wtr tr = getTr(rows);
        List<Wtc> tcs = tr.getTcs();
        for(Wtc tc:tcs){
            tc.removeTopBorder();
        }
        return tr;
    }

    /**
     * 删除整行的下边框
     * @param rows rows
     * @return wtr
     */
    public Wtr removeBottomBorder(int rows){
        Wtr tr = getTr(rows);
        List<Wtc> tcs = tr.getTcs();
        for(Wtc tc:tcs){
            tc.removeBottomBorder();
        }
        return tr;
    }

    /**
     * 删除整列的左边框
     * @param cols cols
     * @return Wtable
     */
    public Wtable removeLeftBorder(int cols){
        for(Wtr tr: wtrs){
            Wtc tc = tr.getTcWithColspan(cols, true);
            if(null != tc){
                tc.removeLeftBorder();
            }
        }
        return this;
    }

    /**
     * 删除整列的右边框
     * @param cols cols
     * @return Wtable
     */
    public Wtable removeRightBorder(int cols){
        for(Wtr tr: wtrs){
            Wtc tc = tr.getTcWithColspan(cols, false);
            if(null != tc){
                tc.removeRightBorder();
            }
        }
        return this;
    }


    /**
     * 清除单元格左边框
     * @param rows rows
     * @param cols cols
     * @return Wtc
     */
    public Wtc removeLeftBorder(int rows, int cols){
        return getTc(rows, cols).removeLeftBorder();
    }
    /**
     * 清除单元格右边框
     * @param rows rows
     * @param cols cols
     * @return Wtc
     */
    public Wtc removeRightBorder(int rows, int cols){
        return getTc(rows, cols).removeRightBorder();
    }
    /**
     * 清除单元格上边框
     * @param rows rows
     * @param cols cols
     * @return Wtc
     */
    public Wtc removeTopBorder(int rows, int cols){
        return getTc(rows, cols).removeTopBorder();
    }
    /**
     * 清除单元格下边框
     * @param rows rows
     * @param cols cols
     * @return Wtc
     */
    public Wtc removeBottomBorder(int rows, int cols){
        return getTc(rows, cols).removeBottomBorder();
    }
    /**
     * 清除单元格左上到右下边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtc removeTl2brBorder(int rows, int cols){
        return getTc(rows, cols).removeTl2brBorder();
    }
    /**
     * 清除单元格右上到左下边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtc removeTr2blBorder(int rows, int cols){
        return getTc(rows, cols).removeBorder();
    }

    /**
     * 清除单元格所有边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtc removeBorder(int rows, int cols){
        return getTc(rows, cols)
                .removeLeftBorder()
                .removeRightBorder()
                .removeTopBorder()
                .removeBottomBorder()
                .removeTl2brBorder()
                .removeTr2blBorder();
    }

    /**
     * 设置单元格默认边框
     * @param rows rows
     * @param cols cols
     * @return  Wtc
     */
    public Wtc setBorder(int rows, int cols){
        return getTc(rows, cols)
        .setLeftBorder()
        .setRightBorder()
        .setTopBorder()
        .setBottomBorder()
        .setTl2brBorder()
        .setTr2blBorder();
    }
    public Wtc setBorder(int rows, int cols, int size, String color, String style){
        return getTc(rows, cols).setBorder(size, color, style);
    }
    public Wtc setLeftBorder(int rows, int cols){
        return getTc(rows, cols).setLeftBorder();
    }
    public Wtc setRightBorder(int rows, int cols){
        return getTc(rows, cols).setRightBorder();
    }
    public Wtc setTopBorder(int rows, int cols){
        return getTc(rows, cols).setTopBorder();
    }
    public Wtc setBottomBorder(int rows, int cols){
        return getTc(rows, cols).setBottomBorder();
    }
    public Wtc setTl2brBorder(int rows, int cols){
        return getTc(rows, cols).setTl2brBorder();
    }
    public Wtc setTl2brBorder(int rows, int cols, String top, String bottom){
        return getTc(rows, cols).setTl2brBorder(top, bottom);
    }
    public Wtc setTr2blBorder(int rows, int cols){
        return getTc(rows, cols).setTr2blBorder();
    }

    public Wtc setTr2blBorder(int rows, int cols, String top, String bottom){
        return getTc(rows, cols).setTr2blBorder(top, bottom);
    }

    public Wtc setLeftBorder(int rows, int cols, int size, String color, String style){
        return getTc(rows, cols).setLeftBorder(size, color, style);
    }
    public Wtc setRightBorder(int rows, int cols, int size, String color, String style){
        return getTc(rows, cols).setRightBorder(size, color, style);
    }
    public Wtc setTopBorder(int rows, int cols, int size, String color, String style){
        return getTc(rows, cols).setTopBorder(size, color, style);
    }
    public Wtc setBottomBorder(int rows, int cols, int size, String color, String style){
        return getTc(rows, cols).setBottomBorder(size, color, style);
    }
    public Wtc setTl2brBorder(int rows, int cols, int size, String color, String style){
        return getTc(rows, cols).setTl2brBorder(size, color, style);
    }
    public Wtc setTr2blBorder(int rows, int cols, int size, String color, String style){
        return getTc(rows, cols).setTr2blBorder(size, color, style);
    }
    public Wtc setColor(int rows, int cols, String color){
        return getTc(rows, cols).setColor(color);
    }

    /**
     * 设置整行颜色
     * @param rows 行
     * @param color 颜色
     * @return wtr
     */
    public Wtr setColor(int rows, String color){
        Wtr tr = getTr(rows);
        tr.setColor(color);
        return tr;
    }
    /**
     * 设置单元格 字体
     * @param rows 行
     * @param cols 列
     * @param size 字号
     * @param eastAsia 中文字体
     * @param ascii 西文字体
     * @param hint 默认字体
     * @return wtc
     */
    public Wtc setFont(int rows, int cols, String size, String eastAsia, String ascii, String hint){
        return getTc(rows, cols).setFont(size, eastAsia, ascii, hint);
    }

    /**
     * 设置整行 字体
     * @param rows 行
     * @param size 字号
     * @param eastAsia 中文字体
     * @param ascii 西文字体
     * @param hint 默认字体
     * @return wtr
     */
    public Wtr setFont(int rows, String size, String eastAsia, String ascii, String hint){
        Wtr tr = getTr(rows);
        tr.setFont(size, eastAsia, ascii, hint);
        return tr;
    }

    /**
     * 设置单元格字号
     * @param rows 行
     * @param cols 列
     * @param size 字号
     * @return wtc
     */
    public Wtc setFontSize(int rows, int cols, String size){
        return getTc(rows, cols).setFontSize(size);
    }
    /**
     * 设置整行字号
     * @param rows 行
     * @param size 字号
     * @return wtr
     */
    public Wtr setFontSize(int rows, String size){
        Wtr tr = getTr(rows);
        tr.setFontSize(size);
        return tr;
    }

    /**
     * 设置单元格字体
     * @param rows 行
     * @param cols 列
     * @param font 字体
     * @return wtc
     */
    public Wtc setFontFamily(int rows, int cols, String font){
        return getTc(rows, cols).setFontFamily(font);
    }

    /**
     * 设置整行字体
     * @param rows 行
     * @param font 字体
     * @return wtr
     */
    public Wtr setFontFamily(int rows, String font){
        Wtr tr = getTr(rows);
        tr.setFontFamily(font);
        return tr;
    }
    public Wtc setWidth(int rows, int cols, String width){
        return getTc(rows, cols).setWidth(width);
    }
    public Wtc setWidth(int rows, int cols, int width){
        return getTc(rows, cols).setWidth(width);
    }

    public Wtc setWidth(int rows, int cols, double width){
        return getTc(rows, cols).setWidth(width);
    }

    public Wtable setWidth(int cols, String width){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setWidth(width);
        }
        return this;
    }
    public Wtable setWidth(int cols, int width){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setWidth(width);
        }
        return this;
    }
    public Wtable setWidth(int cols, double width){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setWidth(width);
        }
        return this;
    }
    public Wtr setHeight(int rows, String height){
        Wtr tr = getTr(rows);
        tr.setHeight(height);
        return tr;
    }

    public Wtr setHeight(int rows, int height){
        return setHeight(rows, height+widthUnit);
    }
    public Wtr setHeight(int rows, double height){
        return setHeight(rows, height+widthUnit);
    }

    /**
     * 设置单元格内容水平对齐方式
     * @param rows 行
     * @param cols 列
     * @param align 对齐方式
     * @return wtc
     */
    public Wtc setAlign(int rows, int cols, String align){
        return getTc(rows, cols).setAlign(align);
    }
    /**
     * 设置整行单元格内容水平对齐方式
     * @param rows 行
     * @param align 对齐方式
     * @return wtcr
     */
    public Wtr setAlign(int rows, String align){
        Wtr tr = getTr(rows);
        tr.setAlign(align);
        return tr;
    }

    /**
     * 设置整个表格单元格内容水平对齐方式
     * @param align 对齐方式
     * @return wtable
     */
    public Wtable setAlign(String align){
        for(Wtr tr:wtrs) {
            tr.setAlign(align);
        }
        return this;
    }
    /**
     * 设置单元格内容垂直对齐方式
     * @param rows 行
     * @param cols 列
     * @param align 对齐方式
     * @return wtc
     */
    public Wtc setVerticalAlign(int rows, int cols, String align){
        return getTc(rows, cols).setVerticalAlign(align);
    }

    /**
     * 设置整行单元格内容垂直对齐方式
     * @param rows 行
     * @param align 对齐方式
     * @return wtr
     */
    public Wtr setVerticalAlign(int rows, String align){
        Wtr tr = getTr(rows);
        tr.setVerticalAlign(align);
        return tr;
    }

    /**
     * 设置整个表格单元格内容垂直对齐方式
     * @param align 对齐方式
     * @return wtable
     */
    public Wtable setVerticalAlign(String align){
        for(Wtr tr:wtrs) {
            tr.setVerticalAlign(align);
        }
        return this;
    }
    /**
     * 设置单元格下边距
     * @param rows 行
     * @param cols 列
     * @param padding 边距 可以指定单位,如:10px
     * @return wtc
     */
    public Wtc setBottomPadding(int rows, int cols, String padding){
        return getTc(rows, cols).setBottomPadding(padding);
    }
    /**
     * 设置单元格下边距
     * @param rows 行
     * @param cols 列
     * @param padding 边距 默认单位dxa
     * @return wtc
     */
    public Wtc setBottomPadding(int rows, int cols, int padding){
        return getTc(rows, cols).setBottomPadding(padding);
    }
    public Wtc setBottomPadding(int rows, int cols, double padding){
        return getTc(rows, cols).setBottomPadding(padding);
    }


    /**
     * 设置整行单元格下边距
     * @param rows 行
     * @param padding 边距 可以指定单位,如:10px
     * @return wtr
     */
    public Wtr setBottomPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setBottomPadding(padding);
        return tr;
    }
    public Wtr setBottomPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setBottomPadding(padding);
        return tr;
    }
    public Wtr setBottomPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setBottomPadding(padding);
        return tr;
    }
    /**
     * 设置整个表格中所有单元格下边距
     * @param padding 边距 可以指定单位,如:10px
     * @return wtable
     */
    public Wtable setBottomPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setBottomPadding(padding);
        }
        return this;
    }
    public Wtable setBottomPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setBottomPadding(padding);
        }
        return this;
    }
    public Wtable setBottomPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setBottomPadding(padding);
        }
        return this;
    }

    public Wtc setTopPadding(int rows, int cols, String padding){
        return getTc(rows, cols).setTopPadding(padding);
    }
    public Wtc setTopPadding(int rows, int cols, int padding){
        return getTc(rows, cols).setTopPadding(padding);
    }
    public Wtc setTopPadding(int rows, int cols, double padding){
        return getTc(rows, cols).setTopPadding(padding);
    }

    public Wtr setTopPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setTopPadding(padding);
        return tr;
    }
    public Wtr setTopPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setTopPadding(padding);
        return tr;
    }
    public Wtr setTopPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setTopPadding(padding);
        return tr;
    }
    public Wtable setTopPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setTopPadding(padding);
        }
        return this;
    }
    public Wtable setTopPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setTopPadding(padding);
        }
        return this;
    }
    public Wtable setTopPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setTopPadding(padding);
        }
        return this;
    }
    public Wtc setRightPadding(int rows, int cols, String padding){
        return getTc(rows, cols).setRightPadding(padding);
    }
    public Wtc setRightPadding(int rows, int cols, int padding){
        return getTc(rows, cols).setRightPadding(padding);
    }
    public Wtc setRightPadding(int rows, int cols, double padding){
        return getTc(rows, cols).setRightPadding(padding);
    }

    public Wtr setRightPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setRightPadding(padding);
        return tr;
    }
    public Wtr setRightPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setRightPadding(padding);
        return tr;
    }
    public Wtr setRightPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setRightPadding(padding);
        return tr;
    }
    public Wtable setRightPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setRightPadding(padding);
        }
        return this;
    }
    public Wtable setRightPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setRightPadding(padding);
        }
        return this;
    }
    public Wtable setRightPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setRightPadding(padding);
        }
        return this;
    }


    public Wtc setLeftPadding(int rows, int cols, String padding){
        return getTc(rows, cols).setLeftPadding(padding);
    }
    public Wtc setLeftPadding(int rows, int cols, int padding){
        return getTc(rows, cols).setLeftPadding(padding);
    }
    public Wtc setLeftPadding(int rows, int cols, double padding){
        return getTc(rows, cols).setLeftPadding(padding);
    }

    public Wtr setLeftPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setLeftPadding(padding);
        return tr;
    }
    public Wtr setLeftPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setLeftPadding(padding);
        return tr;
    }
    public Wtr setLeftPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setLeftPadding(padding);
        return tr;
    }

    public Wtable setLeftPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setLeftPadding(padding);
        }
        return this;
    }
    public Wtable setLeftPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setLeftPadding(padding);
        }
        return this;
    }
    public Wtable setLeftPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setLeftPadding(padding);
        }
        return this;
    }



    public Wtc setPadding(int rows, int cols, String side, String padding){
        return getTc(rows, cols).setPadding(side, padding);
    }
    public Wtc setPadding(int rows, int cols, String side, int padding){
        return getTc(rows, cols).setPadding(side, padding);
    }
    public Wtc setPadding(int rows, int cols, String side, double padding){
        return getTc(rows, cols).setPadding(side, padding);
    }
    public Wtr setPadding(int rows, String side, String padding){
        Wtr tr = getTr(rows);
        tr.setPadding(side, padding);
        return tr;
    }
    public Wtr setPadding(int rows, String side, int padding){
        Wtr tr = getTr(rows);
        tr.setPadding(side, padding);
        return tr;
    }
    public Wtr setPadding(int rows, String side, double padding){
        Wtr tr = getTr(rows);
        tr.setPadding(side, padding);
        return tr;
    }
    public Wtable setPadding(String side, String padding){
        for(Wtr tr:wtrs){
            tr.setPadding(side, padding);
        }
        return this;
    }

    public Wtable setPadding(String side, int padding){
        for(Wtr tr:wtrs){
            tr.setPadding(side, padding);
        }
        return this;
    }

    public Wtable setPadding(String side, double padding){
        for(Wtr tr:wtrs){
            tr.setPadding(side, padding);
        }
        return this;
    }


    /**
     * 设置单元格背景色
     * @param rows rows
     * @param cols cols
     * @param color 颜色
     * @return Wtc
     */
    public Wtc setBackgroundColor(int rows, int cols, String color){
        return getTc(rows, cols).setBackgroundColor(color);
    }

    /**
     * 设置整行单元格背景色
     * @param rows rows
     * @param color 颜色
     * @return Wtr
     */
    public Wtr setBackgroundColor(int rows, String color){
        Wtr tr = getTr(rows);
        tr.setBackgroundColor(color);
        return tr;
    }

    public Wtable setBackgroundColor(String color){
        for(Wtr tr:wtrs){
            tr.setBackgroundColor(color);
        }
        return this;
    }

    /**
     * 清除单元格样式
     * @param rows rows
     * @param cols cols
     * @return Wtc
     */
    public Wtc removeStyle(int rows, int cols){
        return getTc(rows, cols).removeStyle();
    }
    /**
     * 清除整行单元格样式
     * @param rows rows
     * @return Wtr
     */
    public Wtr removeStyle(int rows){
        Wtr tr = getTr(rows);
        tr.removeContent();
        return tr;
    }
    public Wtable removeStyle(){
        for(Wtr tr:wtrs){
            tr.removeStyle();
        }
        return this;
    }
    /**
     * 清除单元格背景色
     * @param rows rows
     * @param cols cols
     * @return Wtc
     */
    public Wtc removeBackgroundColor(int rows, int cols){
        return getTc(rows, cols).removeBackgroundColor();
    }

    /**
     * 清除整行单元格背景色
     * @param rows rows
     * @return Wtr
     */
    public Wtr removeBackgroundColor(int rows){
        Wtr tr = getTr(rows);
        tr.removeBackgroundColor();
        return tr;
    }
    public Wtable removeBackgroundColor(){
        for(Wtr tr:wtrs){
            tr.removeBackgroundColor();
        }
        return this;
    }

    /**
     * 清除单元格颜色
     * @param rows rows
     * @param cols cols
     * @return Wtc
     */
    public Wtc removeColor(int rows, int cols){
        return getTc(rows, cols).removeColor();
    }
    /**
     * 清除整行单元格颜色
     * @param rows rows
     * @return Wtr
     */
    public Wtr removeColor(int rows){
        Wtr tr = getTr(rows);
        tr.removeColor();
        return tr;
    }
    public Wtable removeColor(){
        for(Wtr tr:wtrs){
            tr.removeColor();
        }
        return this;
    }
    /**
     * 粗体
     * @param rows rows
     * @param cols cols
     * @param bold 是否
     * @return Wtc
     */
    public Wtc setBold(int rows, int cols, boolean bold){
        return getTc(rows, cols).setBold(bold);
    }
    public Wtc setBold(int rows, int cols){
        return setBold(rows, cols, true);
    }
    public Wtr setBold(int rows){
        return setBold(rows, true);
    }
    public Wtr setBold(int rows, boolean bold){
        Wtr tr = getTr(rows);
        tr.setBold(bold);
        return tr;
    }
    public Wtable setBold(boolean bold){
        for(Wtr tr:wtrs){
            tr.setBold(bold);
        }
        return this;
    }
    public Wtable setBold(){
        return setBold(true);
    }

    /**
     * 下划线
     * @param rows rows
     * @param cols cols
     * @param underline 是否
     * @return Wtc
     */
    public Wtc setUnderline(int rows, int cols, boolean underline){
        return getTc(rows, cols).setUnderline(underline);
    }
    public Wtc setUnderline(int rows, int cols){
        return setUnderline(rows, cols, true);
    }

    /**
     * 删除线
     * @param rows rows
     * @param cols cols
     * @param strike 是否
     * @return Wtc
     */
    public Wtc setStrike(int rows, int cols, boolean strike){
        return getTc(rows, cols).setStrike(strike);
    }
    public Wtc setStrike(int rows, int cols){
        return setStrike(rows, cols, true);
    }
    public Wtr setStrike(int rows, boolean strike){
        Wtr tr = getTr(rows);
        tr.setStrike(strike);
        return tr;
    }
    public Wtable setStrike(boolean strike){
        for(Wtr tr:wtrs){
            tr.setStrike(strike);
        }
        return this;
    }
    public Wtable setStrike(){
        return setStrike(true);
    }

    /**
     * 斜体
     * @param rows rows
     * @param cols cols
     * @param italic 是否
     * @return Wtc
     */
    public Wtc setItalic(int rows, int cols, boolean italic){
        return getTc(rows, cols).setItalic(italic);
    }

    public Wtc setItalic(int rows, int cols){
        return setItalic(rows, cols, true);
    }

    /**
     * 设置整行斜体
     * @param rows 行
     * @param italic 是否斜体
     * @return wtr
     */
    public Wtr setItalic(int rows,  boolean italic){
        Wtr tr = getTr(rows);
        tr.setItalic(italic);
        return tr;
    }
    public Wtable setItalic(boolean italic){
        for(Wtr tr:wtrs){
            tr.setItalic(italic);
        }
        return this;
    }
    public Wtable setItalic(){
        return setItalic(true);
    }

    /**
     * 替换单元格内容
     * @param rows 行
     * @param cols 行
     * @param src src
     * @param tar tar
     * @return wtc
     */
    public Wtc replace(int rows, int cols, String src, String tar){
        return getTc(rows, cols).replace(src, tar);
    }

    /**
     * 替换整行单元格内容
     * @param rows 行
     * @param src src
     * @param tar tar
     * @return wtr
     */
    public Wtr replace(int rows, String src, String tar){
        Wtr tr = getTr(rows);
        tr.replace(src, tar);
        return tr;
    }
    public Wtable replace(String src, String tar){
        for(Wtr tr:wtrs){
            tr.replace(src, tar);
        }
        return this;
    }

    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
        for(Wtr tr:wtrs){
            tr.setWidthUnit(widthUnit);
        }
    }

    public boolean isAutoLoad() {
        return isAutoLoad;
    }

    public void setAutoLoad(boolean autoLoad) {
        isAutoLoad = autoLoad;
    }
}
