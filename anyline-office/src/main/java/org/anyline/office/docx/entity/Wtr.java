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
    public Wtr setBorder(int size, String color, String style){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setBorder(size, color, style);
        }
        return this;
    }

    public Wtr setColor(String color){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setColor(color);
        }
        return this;
    }
    public Wtr setFont(String size, String eastAsia, String ascii, String hint){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setFont(size, eastAsia, ascii, hint);
        }
        return this;
    }
    public Wtr setFontSize(String size){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setFontSize(size);
        }
        return this;
    }
    public Wtr setFontFamily(String font){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setFontFamily(font);
        }
        return this;
    }

    public Wtr setAlign(String align){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setAlign(align);
        }
        return this;
    }
    public Wtr setVerticalAlign(String align){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setVerticalAlign(align);
        }
        return this;
    }
    public Wtr setBackgroundColor(String color){
        List<Wtc> tcs = getWtcs();
        for(Wtc tc:tcs){
            tc.setBackgroundColor(color);
        }
        return this;
    }
    public Wtr removeContent(){
        DocxUtil.removeContent(src);
        return this;
    }
}
