package org.anyline.office.docx.entity.html;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tr {
    private Table table;
    private String clazz;
    private List<Td> tds = new ArrayList<>();
    private Map<String,String> styles = new HashMap();
    private Element src;
    private String widthUnit = "px";     //默认长度单位 px pt cm/厘米

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
        String bg = styles.get("background-color");
        if(null != bg){
            td.getStyles().put("background-color", bg);
        }
        tds.add(index, td);
        return this;
    }
    public Tr setHeight(int index, String height){
        styles.put("height", height);
        return this;
    }
    public Tr setHeight(int index, int height){
        styles.put("height", height+widthUnit);
        return this;
    }
    public Tr setHeight(int index, double height){
        styles.put("height", height+widthUnit);
        return this;
    }
    public Tr addTd(Td td){
        tds.add(td);

        String bg = styles.get("background-color");
        if(null != bg){
            td.getStyles().put("background-color", bg);
        }
        td.setTr(this);
        return this;
    }
    public int index(){
        List<Tr> trs = table.getTrs();
        return trs.indexOf(this);
    }
    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Table getTable() {
        return table;
    }

    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
        for(Td td:tds){
            td.setWidthUnit(widthUnit);
        }
    }

    public void setTable(Table table) {
        this.table = table;
    }
    public void build(StringBuilder builder){
        if(null == builder) {
            builder = new StringBuilder();
        }
        builder.append("<tr");
        if(null != clazz){
            builder.append(" class='").append(clazz).append("'");
        }
        if (null != styles && !styles.isEmpty()) {
            builder.append(" style='");
            for(String key:styles.keySet()){
                builder.append(key).append(":").append(styles.get(key)).append(";");
            }
            builder.append("'");
        }
        builder.append(">");
        for(Td td:tds){
            td.build(builder);
        }
        builder.append("</tr>");
    }
    public String build(){
        StringBuilder builder = new StringBuilder();
        build(builder);
        return builder.toString();
    }
}
