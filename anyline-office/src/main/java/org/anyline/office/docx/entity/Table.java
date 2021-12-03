package org.anyline.office.docx.entity;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Table {

    private List<Tr> trs = new ArrayList<>();
    private Map<String,String> styles = new HashMap();
    private Element src;
    public List<Tr> getTrs() {
        return trs;
    }

    public void setTrs(List<Tr> trs) {
        this.trs = trs;
    }

    public Map<String, String> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }
    public Tr getTr(int index){
        return trs.get(index);
    }
    public Td getTd(int rows, int cols){
        return trs.get(rows).getTd(cols);
    }
    public Table setTd(int rows, int cols, Td td){
        trs.get(rows).setTd(cols, td);
        return this;
    }

    public Element getSrc() {
        return src;
    }

    public void setSrc(Element src) {
        this.src = src;
    }
    public Table addTr(Tr tr){
        trs.add(tr);
        tr.setTable(this);
        return this;
    }
}
