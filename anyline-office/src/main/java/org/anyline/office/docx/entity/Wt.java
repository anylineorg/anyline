package org.anyline.office.docx.entity;

import org.anyline.util.HtmlUtil;
import org.dom4j.Element;

public class Wt {
    private WDocument doc;
    private Element src;
    public Wt(WDocument doc, Element src){
        this.doc = doc;
        this.src = src;
    }
    public Wt setText(String text){
        if(doc.IS_HTML_ESCAPE) {
            text = HtmlUtil.decode(text);
        }
        src.setText(text);
        return this;
    }
    public String getText(){
        return src.getText();
    }
}
