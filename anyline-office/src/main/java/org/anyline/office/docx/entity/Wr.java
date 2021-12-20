package org.anyline.office.docx.entity;

import org.anyline.office.docx.util.DocxUtil;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class Wr {
    private WDocument doc;
    private Element src;
    public Wr(WDocument doc, Element src){
        this.doc = doc;
        this.src = src;
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

    public List<Wt> getWts(){
        List<Wt> wts = new ArrayList<>();
        List<Element> ts = src.elements("t");
        for(Element t:ts){
            Wt wt = new Wt(doc, t);
            wts.add(wt);
        }
        return wts;
    }
    public List<String> getTexts(){
        List<String> texts = new ArrayList<>();
        List<Element> ts = src.elements("t");
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
    /**
     * 字体颜色
     * @param  color color
     * @return Wr
     */
    public Wr setColor(String color){
        Element pr = DocxUtil.addElement(src, "rPr");
        DocxUtil.addElement(pr, "color","val", color.replace("#",""));
        return this;
    }

    /**
     * 字体字号
     * @param size 字号
     * @param eastAsia 中文字体
     * @param ascii 英文字体
     * @param hint 默认字体
     * @return Wr
     */
    public Wr setFont(String size, String eastAsia, String ascii, String hint){
        int pt = DocxUtil.fontSize(size);
        Element pr = DocxUtil.addElement(src, "rPr");
        DocxUtil.addElement(pr, "sz","val", pt+"");
        DocxUtil.addElement(pr, "rFonts","eastAsia", eastAsia);
        DocxUtil.addElement(pr, "rFonts","ascii", ascii);
        DocxUtil.addElement(pr, "rFonts","hint", hint);

        return this;
    }

    /**
     * 字号
     * @param size size
     * @return Wr
     */
    public Wr setFontSize(String size){
        int pt = DocxUtil.fontSize(size);
        Element pr = DocxUtil.addElement(src, "rPr");
        DocxUtil.addElement(pr, "sz","val", pt+"");
        return this;
    }

    /**
     * 字体
     * @param font font
     * @return Wr
     */
    public Wr setFontFamily(String font){
        Element pr = DocxUtil.addElement(src, "rPr");
        DocxUtil.addElement(pr, "rFonts","eastAsia", font);
        DocxUtil.addElement(pr, "rFonts","ascii", font);
        DocxUtil.addElement(pr, "rFonts","hAnsi", font);
        DocxUtil.addElement(pr, "rFonts","cs", font);
        DocxUtil.addElement(pr, "rFonts","hint", font);
        return this;
    }

    /**
     * 背景色
     * @param color color
     * @return Wr
     */
    public Wr setBackgroundColor(String color){
        Element pr = DocxUtil.addElement(src, "rPr");
        DocxUtil.addElement(pr, "highlight", "val", color.replace("#",""));
        return this;
    }

    /**
     * 粗体
     * @param bold 是否
     * @return Wr
     */
    public Wr setBold(boolean bold){
        Element pr = DocxUtil.addElement(src, "rPr");
        Element b = pr.element("b");
        if(bold){
            if(null == b){
                pr.addElement("w:b");
            }
        }else{
            if(null != b){
                pr.remove(b);
            }
        }
        return this;
    }

    /**
     * 下划线
     * @param underline 是否
     * @return Wr
     */
    public Wr setUnderline(boolean underline){
        Element pr = DocxUtil.addElement(src, "rPr");
        Element u = pr.element("u");
        if(underline){
            if(null == u){
                DocxUtil.addElement(pr, "u", "val", "single");
            }
        }else{
            if(null != u){
                pr.remove(u);
            }
        }
        return this;
    }

    /**
     * 删除线
     * @param strike 是否
     * @return Wr
     */
    public Wr setStrike(boolean strike){
        Element pr = DocxUtil.addElement(src, "rPr");
        Element s = pr.element("strike");
        if(strike){
            if(null == s){
                pr.addElement("w:strike");
            }
        }else{
            if(null != s){
                pr.remove(s);
            }
        }
        return this;
    }

    /**
     * 垂直对齐方式
     * @param align 上标:superscript 下标:subscript
     * @return Wr
     */
    public Wr setVerticalAlign(String align){
        Element pr = DocxUtil.addElement(src, "rPr");
        DocxUtil.addElement(pr, "vertAlign", "val", align);
        return this;
    }
    public Wr setItalic(boolean italic){
        Element pr = DocxUtil.addElement(src, "rPr");
        Element i = pr.element("i");
        if(italic){
            if(null == i){
                pr.addElement("w:i");
            }
        }else{
            if(null != i){
                pr.remove(i);
            }
        }
        return this;
    }
    /**
     * 清除样式
     * @return wr
     */
    public Wr removeStyle(){
        Element pr = src.element("rPr");
        if(null != pr){
            src.remove(pr);
        }
        return this;
    }
    /**
     * 清除背景色
     * @return wr
     */
    public Wr removeBackgroundColor(){
        DocxUtil.removeElement(src,"highlight");
        return this;
    }

    /**
     * 清除颜色
     * @return wr
     */
    public Wr removeColor(){
        DocxUtil.removeElement(src,"color");
        return this;
    }
    public Wr replace(String src, String tar){
        List<Wt> wts = getWts();
        for(Wt wt:wts){
            String text = wt.getText();
            text = text.replace(src, tar);
            wt.setText(text);
        }
        return this;
    }
}
