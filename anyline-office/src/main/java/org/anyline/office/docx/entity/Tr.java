package org.anyline.office.docx.entity;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tr {
    private List<Td> tds = new ArrayList<>();
    private Map<String,String> styles = new HashMap();
    private Element src;

    public Element getSrc() {
        return src;
    }

    public void setSrc(Element src) {
        this.src = src;
    }

    public List<Td> getTds() {
        return tds;
    }

    public void setTds(List<Td> tds) {
        this.tds = tds;
    }

    public Map<String, String> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }
    public Td getTd(int index){
        return tds.get(index);
    }

    public Tr setTd(int index, Td td){
        tds.add(index, td);
        return this;
    }
    public Tr addTd(Td td){
        tds.add(td);
        return this;
    }
}
