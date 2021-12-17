package org.anyline.office.docx.entity;

import org.anyline.office.docx.entity.data.TableBuilder;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.office.docx.util.StyleParser;
import org.anyline.util.DomUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Wtable {
    private WDocument doc;
    private Element src;
    private List<Wtr> trs = new ArrayList<>();
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
        trs.clear();
        List<Element> elements = src.elements("tr");
        for(Element element:elements){
            Wtr tr = new Wtr(doc, this, element);
            trs.add(tr);
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
            org.dom4j.Document doc = DocumentHelper.parseText("<body>"+html+"</body>");
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
            if(null != template){
                //trs.remove(template.getSrc());
            }
            reload();
        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void remove(int index){
        List<Element> trs = src.elements("tr");
        if(index < trs.size() && index >=0){
            trs.remove(index);
        }
        reload();
    }
    public void remove(Wtr tr){
        List<Element> trs = src.elements("tr");
        trs.remove(tr.getSrc());
        reload();
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
        reload();
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
        reload();
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
        return trs;
    }
    public Wtr getTr(int index){
        return trs.get(index);
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
        for(Wtr tr:trs){
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
        for(Wtr tr:trs){
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
        for(Wtr tr:trs){
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
        border.addAttribute("val","nil");
        DocxUtil.removeAttribute(border, "sz");
        DocxUtil.removeAttribute(border, "space");
        DocxUtil.removeAttribute(border, "color");
    }

    /**
     * 清除单元格左边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtable removeLeftBorder(int rows, int cols){
        getTc(rows, cols).removeLeftBorder();
        return this;
    }
    /**
     * 清除单元格右边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtable removeRightBorder(int rows, int cols){
        getTc(rows, cols).removeRightBorder();
        return this;
    }
    /**
     * 清除单元格上边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtable removeTopBorder(int rows, int cols){
        getTc(rows, cols).removeTopBorder();
        return this;
    }
    /**
     * 清除单元格下边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtable removeBottomBorder(int rows, int cols){
        getTc(rows, cols).removeBottomBorder();
        return this;
    }
    /**
     * 清除单元格左上到右下边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtable removeTl2brBorder(int rows, int cols){
        getTc(rows, cols).removeTl2brBorder();
        return this;
    }
    /**
     * 清除单元格右上到左下边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtable removeTr2blBorder(int rows, int cols){
        getTc(rows, cols).removeBorder();
        return this;
    }

    /**
     * 清除单元格所有边框
     * @param rows rows
     * @param cols cols
     * @return wtable
     */
    public Wtable removeBorder(int rows, int cols){
        removeLeftBorder(rows, cols);
        removeRightBorder(rows, cols);
        removeTopBorder(rows, cols);
        removeBottomBorder(rows, cols);
        removeTl2brBorder(rows, cols);
        removeTr2blBorder(rows, cols);
        return this;
    }

    /**
     * 设置单元格默认边框
     * @param rows rows
     * @param cols cols
     * @return  Wtable
     */
    public Wtable setBorder(int rows, int cols){
        setLeftBorder(rows, cols);
        setRightBorder(rows, cols);
        setTopBorder(rows, cols);
        setBottomBorder(rows, cols);
        setTl2brBorder(rows, cols);
        setTr2blBorder(rows, cols);
        return this;
    }
    public Wtable setBorder(int rows, int cols, int size, String color, String style){
        getTc(rows, cols).setBorder(size, color, style);
        return this;
    }
    public Wtable setLeftBorder(int rows, int cols){
        getTc(rows, cols).setLeftBorder();
        return this;
    }
    public Wtable setRightBorder(int rows, int cols){
        getTc(rows, cols).setRightBorder();
        return this;
    }
    public Wtable setTopBorder(int rows, int cols){
        getTc(rows, cols).setTopBorder();
        return this;
    }
    public Wtable setBottomBorder(int rows, int cols){
        getTc(rows, cols).setBottomBorder();
        return this;
    }
    public Wtable setTl2brBorder(int rows, int cols){
        getTc(rows, cols).setTl2brBorder();
        return this;
    }
    public Wtable setTr2blBorder(int rows, int cols){
        getTc(rows, cols).setTr2blBorder();
        return this;
    }

    public Wtable setLeftBorder(int rows, int cols, int size, String color, String style){
        getTc(rows, cols).setLeftBorder(size, color, style);
        return this;
    }
    public Wtable setRightBorder(int rows, int cols, int size, String color, String style){
        getTc(rows, cols).setRightBorder(size, color, style);
        return this;
    }
    public Wtable setTopBorder(int rows, int cols, int size, String color, String style){
        getTc(rows, cols).setTopBorder(size, color, style);
        return this;
    }
    public Wtable setBottomBorder(int rows, int cols, int size, String color, String style){
        getTc(rows, cols).setBottomBorder(size, color, style);
        return this;
    }
    public Wtable setTl2brBorder(int rows, int cols, int size, String color, String style){
        getTc(rows, cols).setTl2brBorder(size, color, style);
        return this;
    }
    public Wtable setTr2blBorder(int rows, int cols, int size, String color, String style){
        getTc(rows, cols).setTr2blBorder(size, color, style);
        return this;
    }
    public Wtable setColor(int rows, int cols, String color){
        getTc(rows, cols).setColor(color);
        return this;
    }
    public Wtable setFont(int rows, int cols, String size, String eastAsia, String ascii, String hint){
        getTc(rows, cols).setFont(size, eastAsia, ascii, hint);
        return this;
    }
    public Wtable setFontSize(int rows, int cols, String size){
        getTc(rows, cols).setFontSize(size);
        return this;
    }
    public Wtable setFontFamily(int rows, int cols, String font){
        getTc(rows, cols).setFontFamily(font);
        return this;
    }
    public Wtable setWidth(int rows, int cols, String width){
        getTc(rows, cols).setWidth(width);
        return this;
    }

    public Wtable setAlign(int rows, int cols, String align){
        getTc(rows, cols).setAlign(align);
        return this;
    }
    public Wtable setVerticalAlign(int rows, int cols, String align){
        getTc(rows, cols).setVerticalAlign(align);
        return this;
    }

    /**
     * 背景色
     * @param color 颜色
     * @return Wtable
     */
    public Wtable setBackgroundColor(int rows, int cols,String color){
        getTc(rows, cols).setBackgroundColor(color);
        return this;
    }

    /**
     * 清除样式
     * @return Wtable
     */
    public Wtable removeStyle(int rows, int cols){
        getTc(rows, cols).removeStyle();
        return this;
    }
    /**
     * 清除背景色
     * @return Wtable
     */
    public Wtable removeBackgroundColor(int rows, int cols){
        getTc(rows, cols).removeBackgroundColor();
        return this;
    }

    /**
     * 清除颜色
     * @return Wtable
     */
    public Wtable removeColor(int rows, int cols){
        getTc(rows, cols).removeColor();
        return this;
    }
    /**
     * 粗体
     * @param bold 是否
     * @return Wtable
     */
    public Wtable setBold(int rows, int cols, boolean bold){
        getTc(rows, cols).setBold(bold);
        return this;
    }
    public Wtable setBold(int rows, int cols){
        setBold(rows, cols, true);
        return this;
    }

    /**
     * 下划线
     * @param underline 是否
     * @return Wtable
     */
    public Wtable setUnderline(int rows, int cols, boolean underline){
        getTc(rows, cols).setUnderline(underline);
        return this;
    }
    public Wtable setUnderline(int rows, int cols){
        setUnderline(rows, cols, true);
        return this;
    }

    /**
     * 删除线
     * @param strike 是否
     * @return Wtable
     */
    public Wtable setStrike(int rows, int cols, boolean strike){
        getTc(rows, cols).setStrike(strike);
        return this;
    }
    public Wtable setStrike(int rows, int cols){
        setStrike(rows, cols, true);
        return this;
    }

    /**
     * 斜体
     * @param italic 是否
     * @return Wtable
     */
    public Wtable setItalic(int rows, int cols, boolean italic){
        getTc(rows, cols).setItalic(italic);
        return this;
    }

    public Wtable setItalic(int rows, int cols){
        return setItalic(rows, cols,true);
    }

}
