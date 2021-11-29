package org.anyline.office.docx.entity;

import org.anyline.entity.DataRow;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

public class Td extends DataRow {

    private Map<String,String> styles = new HashMap();
    private Element src;

    public Map<String, String> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }

    public Element getSrc() {
        return src;
    }

    public void setSrc(Element src) {
        this.src = src;
    }
}
