package org.anyline.office.docx.entity;

import org.dom4j.Element;

public class Wt {
    private WDocument doc;
    private Element src;
    public Wt(WDocument doc, Element src){
        this.doc = doc;
        this.src = src;
    }
    public Wt setText(String text){
        src.setText(text);
        return this;
    }
    public String getText(){
        return src.getText();
    }
}
