package org.anyline.office.docx.entity;

import org.anyline.office.docx.util.DocxUtil;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class Wtr {
    private WDocument doc;
    private Element src;
    private Wtable parent;
    private List<Wtc> wtcs = new ArrayList<>();
    private String widthUnit = "px";     // 默认长度单位 px pt cm/厘米
    public Wtr(WDocument doc, Wtable parent, Element src){
        this.doc = doc;
        this.src = src;
        this.parent = parent;
        load();
    }

    public Wtr reload(){
        load();
        return this;
    }
    private Wtr load(){
        wtcs.clear();
        List<Element> items = src.elements("tc");
        for(Element tc:items){
            Wtc wtc = new Wtc(doc, this, tc);
            wtcs.add(wtc);
        }
        return this;
    }

    public WDocument getDoc() {
        return doc;
    }
    public Wtable getParent(){
        return parent;
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
    public Wtr setHeight(String height){
        int dxa = DocxUtil.dxa(height);
        Element pr = DocxUtil.addElement(src, "trPr");
        DocxUtil.addElement(pr,"trHeight", "val", dxa+"" );
        return this;
    }
    public Wtr setHeight(int height){
        return setHeight(height+widthUnit);
    }
    public Wtr setHeight(double height){
        return setHeight(height+widthUnit);
    }
    public List<Wtc> getWtcs(){
        if(wtcs.size() ==0){
            List<Element> elements = src.elements("tc");
            for(Element element:elements){
                Wtc tc = new Wtc(doc,this, element);
                wtcs.add(tc);
            }
        }
        return wtcs;
    }
    public Wtc getTc(int index){
        return wtcs.get(index);
    }

    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
        for(Wtc tc:wtcs){
            tc.setWidthUnit(widthUnit);
        }
    }

    /**
     * 获取单元格,计算合并列
     * @param index 索引
     * @param prev 如果index位置被合并了,是否返 当前合并组中的第一个单元格
     * @return tc
     */
    public Wtc getTcWithColspan(int index, boolean prev){
        int qty = -1;
        for(Wtc tc:wtcs){
            qty += tc.getColspan();
            if(qty == index){
                return tc;
            }

            if(qty > index){
                if(prev){
                    return tc;
                }else {
                    break;
                }
            }
        }
        return null;
    }
    public List<Wtc> getTcs(){
        return wtcs;
    }


    private Wtr removeBorder(){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.removeBorder();
        }
        return this;
    }
    public Wtr setBorder(){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setBorder();
        }
        return this;
    }

    /**
     * 设置边框
     * @param size 宽度根据width unit单位
     * @param color 颜色
     * @param style 样式
     * @return tr
     */
    public Wtr setBorder(int size, String color, String style){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setBorder(size, color, style);
        }
        return this;
    }
    public Wtr setTopBorder(int size, String color, String style){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setTopBorder(size, color, style);
        }
        return this;
    }
    public Wtr removeTopBorder(){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.removeTopBorder();
        }
        return this;
    }
    public Wtr setBottomBorder(int size, String color, String style){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setBottomBorder(size, color, style);
        }
        return this;
    }
    public Wtr removeBottomBorder(){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.removeBottomBorder();
        }
        return this;
    }
    /**
     * 设置颜色
     * @param color color
     * @return tr
     */
    public Wtr setColor(String color){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setColor(color);
        }
        return this;
    }

    /**
     * 设置字体
     * @param size 字号
     * @param eastAsia 中文字体
     * @param ascii 英文字体
     * @param hint 默认字体
     * @return tr
     */
    public Wtr setFont(String size, String eastAsia, String ascii, String hint){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setFont(size, eastAsia, ascii, hint);
        }
        return this;
    }

    /**
     * 设置字号
     * @param size px|pt|cm
     * @return tr
     */
    public Wtr setFontSize(String size){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setFontSize(size);
        }
        return this;
    }

    /**
     * 设置字体
     * @param font 字体
     * @return tr
     */
    public Wtr setFontFamily(String font){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setFontFamily(font);
        }
        return this;
    }

    /**
     * 设置水平对齐方式
     * @param align start/left center end/right
     * @return tr
     */
    public Wtr setAlign(String align){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setAlign(align);
        }
        return this;
    }

    /**
     * 设置垂直对齐方式
     * @param align top/center/bottom
     * @return Wtr
     */
    public Wtr setVerticalAlign(String align){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setVerticalAlign(align);
        }
        return this;
    }

    /**
     * 设置整行背景色
     * @param color color
     * @return  Wtr
     */
    public Wtr setBackgroundColor(String color){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setBackgroundColor(color);
        }
        return this;
    }

    /**
     * 删除整行背景色
     * @return tr
     */
    public Wtr removeContent(){
        DocxUtil.removeContent(src);
        return this;
    }
    public Wtr removeStyle(){
        for(Wtc tc:wtcs){
            tc.removeStyle();
        }
        return this;
    }
    public Wtr removeBackgroundColor(){
        for(Wtc tc:wtcs){
            tc.removeBackgroundColor();
        }
        return this;
    }
    public Wtr removeColor(){
        for(Wtc tc:wtcs){
            tc.removeColor();
        }
        return this;
    }
    public Wtr replace(String src, String tar){
        for(Wtc tc:wtcs){
            tc.replace(src, tar);
        }
        return this;
    }
    public Wtr setBold(){
        for(Wtc tc:wtcs){
            tc.setBold();
        }
        return this;
    }
    public Wtr setBold(boolean bold){
        for(Wtc tc:wtcs){
            tc.setBold(bold);
        }
        return this;
    }
    /**
     * 下划线
     * @param underline 是否
     * @return Wtc
     */
    public Wtr setUnderline(boolean underline){
        for(Wtc tc:wtcs){
            tc.setUnderline(underline);
        }
        return this;
    }
    public Wtr setUnderline(){
        setUnderline(true);
        return this;
    }

    /**
     * 删除线
     * @param strike 是否
     * @return Wtc
     */
    public Wtr setStrike(boolean strike){
        for(Wtc tc:wtcs){
            tc.setStrike(strike);
        }
        return this;
    }
    public Wtr setStrike(){
        setStrike(true);
        return this;
    }

    /**
     * 斜体
     * @param italic 是否
     * @return Wtc
     */
    public Wtr setItalic(boolean italic){
        for(Wtc tc:wtcs){
            tc.setItalic(italic);
        }
        return this;
    }

    public Wtr setItalic(){
        return setItalic(true);
    }
    public Wtr setPadding(String side, double padding){
        for(Wtc tc:wtcs){
            tc.setPadding(side, padding);
        }
        return this;
    }
    public Wtr setPadding(String side, String padding){
        for(Wtc tc:wtcs){
            tc.setPadding(side, padding);
        }
        return this;
    }
    public Wtr setPadding(String side, int padding){
        for(Wtc tc:wtcs){
            tc.setPadding(side, padding);
        }
        return this;
    }

    public Wtr setLeftPadding(double padding){
        for(Wtc tc:wtcs){
            tc.setLeftPadding(padding);
        }
        return this;
    }
    public Wtr setLeftPadding(String padding){
        for(Wtc tc:wtcs){
            tc.setLeftPadding(padding);
        }
        return this;
    }
    public Wtr setLeftPadding(int padding){
        for(Wtc tc:wtcs){
            tc.setLeftPadding(padding);
        }
        return this;
    }

    public Wtr setRightPadding(double padding){
        for(Wtc tc:wtcs){
            tc.setRightPadding(padding);
        }
        return this;
    }
    public Wtr setRightPadding(String padding){
        for(Wtc tc:wtcs){
            tc.setRightPadding(padding);
        }
        return this;
    }
    public Wtr setRightPadding(int padding){
        for(Wtc tc:wtcs){
            tc.setRightPadding(padding);
        }
        return this;
    }

    public Wtr setTopPadding(double padding){
        for(Wtc tc:wtcs){
            tc.setTopPadding(padding);
        }
        return this;
    }
    public Wtr setTopPadding(String padding){
        for(Wtc tc:wtcs){
            tc.setTopPadding(padding);
        }
        return this;
    }
    public Wtr setTopPadding(int padding){
        for(Wtc tc:wtcs){
            tc.setTopPadding(padding);
        }
        return this;
    }


    public Wtr setBottomPadding(double padding){
        for(Wtc tc:wtcs){
            tc.setBottomPadding(padding);
        }
        return this;
    }
    public Wtr setBottomPadding(String padding){
        for(Wtc tc:wtcs){
            tc.setBottomPadding(padding);
        }
        return this;
    }
    public Wtr setBottomPadding(int padding){
        for(Wtc tc:wtcs){
            tc.setBottomPadding(padding);
        }
        return this;
    }


    public Wtr setPadding(double padding){
        for(Wtc tc:wtcs){
            tc.setPadding(padding);
        }
        return this;
    }
    public Wtr setPadding(String padding){
        for(Wtc tc:wtcs){
            tc.setPadding(padding);
        }
        return this;
    }
    public Wtr setPadding(int padding){
        for(Wtc tc:wtcs){
            tc.setPadding(padding);
        }
        return this;
    }
}
