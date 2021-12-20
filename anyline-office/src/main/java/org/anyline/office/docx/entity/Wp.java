package org.anyline.office.docx.entity;

import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.DomUtil;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class Wp {
    private WDocument doc;
    private Element src;
    private List<Wr> wrs = new ArrayList<>();
    public Wp(WDocument doc, Element src){
        this.doc = doc;
        this.src = src;
        load();
    }
    public void reload(){
        load();
    }
    private void load(){
        wrs.clear();
        List<Element> elements = src.elements("r");
        for(Element element:elements){
            Wr wr = new Wr(doc, element);
            wrs.add(wr);
        }
    }
    public Wp setColor(String color){
        for(Wr wr:wrs){
            wr.setColor(color);
        }
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "color","val", color.replace("#",""));
        return this;
    }
    public Wp setFont(String size, String eastAsia, String ascii, String hint){

        for(Wr wr:wrs){
            wr.setFont(size, eastAsia, ascii, hint);
        }
        int pt = DocxUtil.fontSize(size);
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "sz","val", pt+"");
        DocxUtil.addElement(pr, "rFonts","eastAsia", eastAsia);
        DocxUtil.addElement(pr, "rFonts","ascii", ascii);
        DocxUtil.addElement(pr, "rFonts","hint", hint);

        return this;
    }
    public Wp setFontSize(String size){
        for(Wr wr:wrs){
            wr.setFontSize(size);
        }
        int pt = DocxUtil.fontSize(size);
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "sz","val", pt+"");
        return this;
    }
    public Wp setFontFamily(String font){
        for(Wr wr:wrs){
            wr.setFontFamily(font);
        }
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "rFonts","eastAsia", font);
        DocxUtil.addElement(pr, "rFonts","ascii", font);
        DocxUtil.addElement(pr, "rFonts","hAnsi", font);
        DocxUtil.addElement(pr, "rFonts","cs", font);
        DocxUtil.addElement(pr, "rFonts","hint", font);
        return this;
    }

    public Wp setAlign(String align){
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "jc","val", align);
        return this;
    }

    public Wp setBackgroundColor(String color){
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "highlight", "val", color.replace("#",""));
        for(Wr wr:wrs){
            wr.setBackgroundColor(color);
        }
        return this;
    }

    public Wp setBold(boolean bold){
        Element pr = DocxUtil.addElement(src, "pPr");
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
        for(Wr wr:wrs){
            wr.setBold(bold);
        }
        return this;
    }
    public Wp setUnderline(boolean underline){
        Element pr = DocxUtil.addElement(src, "pPr");
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
        for(Wr wr:wrs){
            wr.setUnderline(underline);
        }
        return this;
    }
    public Wp setStrike(boolean strike){
        Element pr = DocxUtil.addElement(src, "pPr");
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
        for(Wr wr:wrs){
            wr.setStrike(strike);
        }
        return this;
    }
    public Wp setItalic(boolean italic){
        Element pr = DocxUtil.addElement(src, "pPr");
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
        for(Wr wr:wrs){
            wr.setItalic(italic);
        }
        return this;
    }

    /**
     * 清除样式
     * @return wp
     */
    public Wp removeStyle(){
        Element pr = src.element("pPr");
        if(null != pr){
            src.remove(pr);
        }
        for(Wr wr:wrs){
            wr.removeStyle();
        }
        return this;
    }
    /**
     * 清除背景色
     * @return wp
     */
    public Wp removeBackgroundColor(){
        DocxUtil.removeElement(src,"shd");
        return this;
    }
    /**
     * 清除颜色
     * @return wp
     */
    public Wp removeColor(){
        DocxUtil.removeElement(src,"color");
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
    public Wp addWr(Wr wr){
        wrs.add(wr);
        return this;
    }
    public Wp replace(String src, String tar){
        for(Wr wr:wrs){
            wr.replace(src, tar);
        }
        return this;
    }
}
