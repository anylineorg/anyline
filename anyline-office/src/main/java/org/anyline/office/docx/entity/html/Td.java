package org.anyline.office.docx.entity.html;

import org.dom4j.Element;

import java.security.CodeSigner;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Td {

    private Map<String,String> styles = new HashMap();
    private Element src;
    private String text = null;
    private int colspan = 1;
    private int rowspan = 1;
    private String clazz = null;
    private int merge = -1; // -1 不合并 1:合并 2:被合并
    private boolean remove = false;
    private String width;
    private Tr tr;
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
    //根据 rowspan colspan合并
    public void merge(){
        Tr tr = getTr();
        List<Td> tds = tr.getTds();
        List<Tr> trs = tr.getTable().getTrs();
        int cols = tds.indexOf(this);
        int rows = trs.indexOf(tr);

        for(int c=cols+1; c<cols+colspan; c++){
            tr.getTd(c).setRemove(true);
        }
        for(int r=rows+1; r<rows+rowspan; r++){
            tr = trs.get(r);
            for(int c=cols; c<cols+colspan; c++){
                tr.getTd(c).setRemove(true);
            }
        }
    }
    public int[] index(){
        int result[] = new int[2];
        Tr tr = getTr();
        List<Td> tds = tr.getTds();
        result[0] = tr.index();
        result[1] = tds.indexOf(this);
        return result;
    }
    public int getColIndex(){
        Tr tr = getTr();
        List<Td> tds = tr.getTds();
        return tds.indexOf(this);
    }
    public int getRowIndex(){
        Tr tr = getTr();
        return tr.index();
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getClazz() {
        return clazz;
    }

    public Tr getTr() {
        return tr;
    }

    public void setTr(Tr tr) {
        this.tr = tr;
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

    public void build(StringBuilder builder){
        if(null == builder) {
            builder = new StringBuilder();
        }
        if(!remove) {
            builder.append("<td");
            if (null != styles && !styles.isEmpty()) {
                builder.append(" style='");
                for (String key : styles.keySet()) {
                    builder.append(key).append(":").append(styles.get(key)).append(";");
                }
                builder.append("'");
            }
            if (colspan > 1) {
                builder.append(" colspan='").append(colspan).append("'");
            }
            if (rowspan > 1) {
                builder.append(" rowspan='").append(rowspan).append("'");
            }
            builder.append(">");
            if(null != text) {
                builder.append(text);
            }
            builder.append("</td>");
        }
    }
    public String build(){
        StringBuilder builder = new StringBuilder();
        build(builder);
        return builder.toString();
    }
}
