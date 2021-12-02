package org.anyline.office.docx.entity;

import org.anyline.entity.DataRow;
import org.dom4j.Element;

import java.util.HashMap;
import java.util.Map;

public class Td {

    private Map<String,String> styles = new HashMap();
    private Element src;
    private String text = null;
    private int colspan = 1;
    private int rowspan = 1;
    private String clazz = null;
    private int merge = -1;
    private boolean remove = false;
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

    public int getColspan() {
        return colspan;
    }
    public boolean isEmpty(){
        if(null != src){
            return false;
        }
        if(null != clazz){
            return false;
        }
        if(null != text){
            return false;
        }
        if(merge >= 0){
            return false;
        }
        if(rowspan >1){
            return false;
        }
        if(colspan >1){
            return false;
        }
        if(isRemove()){
            return false;
        }
        return true;
    }
    public void setColspan(int colspan) {
        this.colspan = colspan;
    }

    public int getRowspan() {
        return rowspan;
    }

    public void setRowspan(int rowspan) {
        this.rowspan = rowspan;
    }

    public String getClazz() {
        return clazz;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public int getMerge() {
        return merge;
    }

    public void setMerge(int merge) {
        this.merge = merge;
    }

    public boolean isRemove() {
        return remove;
    }

    public void setRemove(boolean remove) {
        this.remove = remove;
    }
}
