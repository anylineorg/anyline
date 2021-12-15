package org.anyline.office.docx.entity;

import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.DomUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.List;
import java.util.Map;

public class WTr {
    private WDocument doc;
    private Element src;
    public WTr(WDocument doc){
        this.doc = doc;
    }
    public WTr(WDocument doc, Element src){
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
}
