package org.anyline.office.docx.entity.html;

import org.dom4j.Element;

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
    private int offset = 0; //向右偏移(前一列被上一行合并后，当前td偏移 +1, 前一列被更前一列合并时当前td偏移+1)
    private boolean remove = false;
    private String width;
    private boolean isMerge = false;
    private Tr tr;
    public Map<String, String> getStyles() {
        return styles;
    }
    private String widthUnit = "px";     //默认长度单位 px pt cm/厘米

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }


    public Element getSrc() {
        return src;
    }

    public void setSrc(Element src) {
        this.src = src;
    }

    public int getOffset(){
        return offset;
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
    public Td setOffset(int offset){
        this.offset = offset;
        return this;
    }
    public Td addOffset(int offset){
        this.offset += offset;
        return this;
    }
    public Td setColspan(int colspan) {
        this.colspan = colspan;
        if(colspan > 1) {
            isMerge = true;
            //后面的所有列偏移+(colspan-1)
           /* Tr tr = this.getTr();
            int max = tr.getTds().size();
            for(int i=this.getColIndex()+1; i<max; i++){
                Td td = tr.getTd(i);
                td.addOffset(colspan-1);
            }*/
        }
        return this;
    }


    public int getRowspan() {
        return rowspan;
    }

    public Td setRowspan(int rowspan) {
        this.rowspan = rowspan;
        if(rowspan > 1) {
            isMerge = true;
        }
        return this;
    }
    //根据 rowspan colspan合并
    public void merge(){
        if(isMerge){
            return;
        }
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
        isMerge = true;
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

    public Td setWidth(String width) {
        this.width = width;
        return this;
    }
    public Td setWidth(int width) {
        this.width = width+widthUnit;
        return this;
    }
    public Td setWidth(double width) {
        this.width = width+widthUnit;
        return this;
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

    public String getTextNvl() {
        if(null == text){
            return "";
        }
        return text;
    }
    public String getTextTrim() {
        if(null == text){
            return "";
        }
        return text.trim();
    }

    public Td setText(String text) {
        this.text = text;
        return this;
    }

    public Td setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }

    public int getMerge() {
        return merge;
    }

    public Td setMerge(int merge) {
        this.merge = merge;
        return this;
    }

    public boolean isRemove() {
        return remove;
    }

    public Td setRemove(boolean remove) {
        this.remove = remove;
        return this;
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

            builder.append(" data-row-index='").append(getRowIndex()).append("'");
            builder.append(" data-col-index='").append(getColIndex()).append("'");
            builder.append(" data-offset='").append(offset).append("'");
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




    public Td left(){
        Td left = null;
        List<Td> tcs = tr.getTds();
        int index = tcs.indexOf(this);
        if(index > 0){
            left = tcs.get(index-1);
        }
        return left;
    }
    public Td right(){
        Td right = null;
        List<Td> tcs = tr.getTds();
        int index = tcs.indexOf(this);
        if(index < tcs.size()-1){
            right = tcs.get(index+1);
        }
        return right;
    }
    public Td bottom(){
        Td bottom = null;
        Table table = tr.getTable();
        List<Tr> trs = table.getTrs();
        int y = trs.indexOf(tr);
        if(y < trs.size()-1){
            Tr tr = trs.get(y+1);
            int x = tr.getTds().indexOf(this);
            bottom = tr.getTd(x);
        }
        return bottom;
    }
    public Td top(){
        Td top = null;
        Table table = tr.getTable();
        List<Tr> trs = table.getTrs();
        int y = trs.indexOf(tr);
        if(y < trs.size()-1 && y>0){
            Tr tr = trs.get(y-1);
            int x = tr.getTds().indexOf(this);
            top = tr.getTd(x);
        }
        return top;
    }
    public Td removeLeftBorder(){
        removeBorder( "left");
        Td left = left();
        if(null != left) {
            left.removeBorder("right");
        }
        return this;
    }
    public Td removeRightBorder(){
        removeBorder("right");
        Td right = right();
        if(null != right) {
            right.removeBorder("left");
        }
        return this;
    }
    public Td removeTopBorder(){
        removeBorder("top");
        Td top = top();
        if(null != top) {
            top.removeBorder("bottom");
        }
        return this;
    }
    public Td removeBottomBorder(){
        removeBorder( "bottom");
        Td bottom = bottom();
        if(null != bottom) {
            bottom.removeBorder("top");
        }
        return this;
    }
    public Td removeTl2brBorder(){
        removeBorder( "tl2br");
        return this;
    }

    public Td removeTr2blBorder(){
        removeBorder( "tr2bl");
        return this;
    }
    public Td removeBorder(String side){
        styles.remove( "border-"+side+"-size");
        styles.remove("border-"+side+"-color");
        styles.remove("border-"+side+"-style");
        styles.remove("border-"+side);
        return this;
    }

    public Td removeBorder(){
        removeLeftBorder();
        removeRightBorder();
        removeTopBorder();
        removeBottomBorder();
        removeTl2brBorder();
        removeTr2blBorder();
        return this;
    }
    public Td setBorder(){
        setLeftBorder();
        setRightBorder();
        setTopBorder();
        setBottomBorder();
        setTl2brBorder();
        setTr2blBorder();
        return this;
    }
    public Td setBorder(String weight, String color, String style){
        setLeftBorder(weight, color, style);
        setRightBorder(weight, color, style);
        setTopBorder(weight, color, style);
        setBottomBorder(weight, color, style);
        setTl2brBorder(weight, color, style);
        setTr2blBorder(weight, color, style);
        return this;
    }
    public Td setLeftBorder(){
        setBorder( "left", "1px", "auto", "single");
        return this;
    }
    public Td setRightBorder(){
        setBorder( "right", "1px", "auto", "single");
        return this;
    }
    public Td setTopBorder(){
        setBorder( "top", "1px", "auto", "single");
        return this;
    }
    public Td setBottomBorder(){
        setBorder( "bottom", "1px", "auto", "single");
        return this;
    }
    public Td setTl2brBorder(){
        setBorder( "tl2br", "1px", "auto", "single");
        return this;
    }

    /**
     * 设置左上至右下边框
     * @param top 右上文本
     * @param bottom 左下文本
     * @return td
     */
    public Td setTl2brBorder(String top, String bottom){
        String text = "<div style='text-align:right'>" + top + "</div><div style='text-align:left'>"+bottom+"</div>";
        setText(text);
        setBorder( "tl2br", "1px", "auto", "single");
        return this;
    }
    public Td setTr2blBorder(){
        setBorder( "tr2bl", "1px", "auto", "single");
        return this;
    }
    /**
     * 设置左上至右下边框
     * @param top 左上文本
     * @param bottom 右下文本
     * @return td
     */
    public Td setTr2blBorder(String top, String bottom){
        String text = "<div style='text-align:left'>" + top + "</div><div style='text-align:right'>"+bottom+"</div>";
        setText(text);
        setBorder( "tr2bl", "1px", "auto", "single");
        return this;
    }

    public Td setLeftBorder(String width, String color, String style){
        setBorder( "left", width, color, style);
        return this;
    }
    public Td setLeftBorder(int width, String color, String style){
        setBorder( "left", width+widthUnit, color, style);
        return this;
    }
    public Td setLeftBorder(double width, String color, String style){
        setBorder( "left", width+widthUnit, color, style);
        return this;
    }
    public Td setRightBorder(String width, String color, String style){
        setBorder( "right", width, color, style);
        return this;
    }
    public Td setRightBorder(int width, String color, String style){
        setBorder( "right", width+widthUnit, color, style);
        return this;
    }
    public Td setRightBorder(double width, String color, String style){
        setBorder( "right", width+widthUnit, color, style);
        return this;
    }
    public Td setTopBorder(String width, String color, String style){
        setBorder( "top", width, color, style);
        return this;
    }
    public Td setTopBorder(int width, String color, String style){
        setBorder( "top", width+widthUnit, color, style);
        return this;
    }
    public Td setTopBorder(double width, String color, String style){
        setBorder( "top", width+widthUnit, color, style);
        return this;
    }
    public Td setBottomBorder(String width, String color, String style){
        setBorder( "bottom", width, color, style);
        return this;
    }
    public Td setBottomBorder(int width, String color, String style){
        setBorder( "bottom", width+widthUnit, color, style);
        return this;
    }
    public Td setBottomBorder(double width, String color, String style){
        setBorder( "bottom", width+widthUnit, color, style);
        return this;
    }
    public Td setTl2brBorder(String width, String color, String style){
        setBorder( "tl2br", width, color, style);
        return this;
    }
    public Td setTl2brBorder(int width, String color, String style){
        setBorder( "tl2br", width+widthUnit, color, style);
        return this;
    }
    public Td setTl2brBorder(double width, String color, String style){
        setBorder( "tl2br", width+widthUnit, color, style);
        return this;
    }
    public Td setTr2blBorder(String width, String color, String style){
        setBorder( "tr2bl", width, color, style);
        return this;
    }
    public Td setTr2blBorder(int width, String color, String style){
        setBorder( "tr2bl", width+widthUnit, color, style);
        return this;
    }
    public Td setTr2blBorder(double width, String color, String style){
        setBorder( "tr2bl", width+widthUnit, color, style);
        return this;
    }
    public Td setBorder(String side, String width, String color, String style){
        styles.put("border-"+side+"-style",style);
        styles.put("border-"+side+"-color",color);
        styles.put("border-"+side+"-width",width);
        return this;
    }
    public Td setBorder(String side, int width, String color, String style){
        return setBorder(side, width+widthUnit, color, style);
    }
    public Td setBorder(String side, double width, String color, String style){
        return setBorder(side, width+widthUnit, color, style);
    }
    public Td setColor(String color){
        styles.put("color", color);
        return this;
    }
    public Td setFont(String weight, String eastAsia, String ascii, String hint){
        styles.put("font-weight", weight);
        styles.put("font-family-eastAsia", eastAsia);
        styles.put("font-family-ascii", ascii);
        styles.put("font-family-hint", hint);
        return this;
    }
    public Td setFontSize(String weight){
        styles.put("font-weight", weight);
        return this;
    }
    public Td setFontWeight(String weight){
        styles.put("font-weight", weight);
        return this;
    }
    public Td setFontFamily(String font){
        styles.put("font-family-eastAsia", font);
        styles.put("font-family-ascii", font);
        styles.put("font-family-hint", font);
        return this;
    }

    public Td setAlign(String align){
        styles.put("text-align", align);
        return this;
    }
    public Td setVerticalAlign(String align){
        styles.put("vertical-align", align);
        return this;
    }

    /**
     * 背景色
     * @param color 颜色
     * @return Td
     */
    public Td setBackgroundColor(String color){
        styles.put("background-color", color);
        return this;
    }

    /**
     * 清除样式
     * @return td
     */
    public Td removeStyle(){
        styles.clear();
        return this;
    }
    /**
     * 清除背景色
     * @return td
     */
    public Td removeBackgroundColor(){
        styles.remove("background-color");
        return this;
    }

    /**
     * 清除颜色
     * @return Td
     */
    public Td removeColor(){
        styles.remove("color");
        return this;
    }
    /**
     * 粗体
     * @param bold 是否
     * @return Td
     */
    public Td setBold(boolean bold){
        if(bold){
            styles.put("font-weight","700");
        }else{
            styles.remove("font-weight");
        }
        return this;
    }
    public Td setBold(){
        setBold(true);
        return this;
    }

    /**
     * 下划线
     * @param underline 是否
     * @return Td
     */
    public Td setUnderline(boolean underline){
        if(underline){
            styles.put("underline","true");
        }else{
            styles.remove("underline");
        }
        return this;
    }
    public Td setUnderline(){
        setUnderline(true);
        return this;
    }

    /**
     * 删除线
     * @param strike 是否
     * @return Td
     */
    public Td setStrike(boolean strike){
        if(strike){
            styles.put("strike","true");
        }else{
            styles.remove("");
        }
        return this;
    }
    public Td setStrike(){
        setStrike(true);
        return this;
    }

    public Td setDelete(){
        setStrike(true);
        return this;
    }
    public Td setDelete(boolean strike){
        if(strike){
            styles.put("strike","true");
        }else{
            styles.remove("");
        }
        return this;
    }
    /**
     * 斜体
     * @param italic 是否
     * @return Td
     */
    public Td setItalic(boolean italic){
        if(italic) {
            styles.put("italic", "true");
        }else{
            styles.remove("italic");
        }
        return this;
    }

    public Td setItalic(){
        return setItalic(true);
    }
    public Td setLineHeight(String height){
        styles.put("line-height", height);
        return this;
    }
    public Td setLineHeight(int height){
        return setLineHeight(height+widthUnit);
    }
    public Td setLineHeight(double height){
        return setLineHeight(height+widthUnit);
    }
    public Td setPadding(String padding){
        styles.put("padding-left", padding);
        styles.put("padding-right", padding);
        styles.put("padding-top", padding);
        styles.put("padding-bottom", padding);
        return this;
    }
    public Td setPadding(int padding){
        return setPadding(padding+widthUnit);
    }
    public Td setPadding(double padding){
        return setPadding(padding+widthUnit);
    }

    public Td setBottomPadding(String padding){
        return setPadding("bottom", padding);
    }
    public Td setBottomPadding(int padding){
        return setPadding("bottom", padding);
    }
    public Td setBottomPadding(double padding){
        return setPadding("bottom", padding);
    }

    public Td setTopPadding(String padding){
        return setPadding("top", padding);
    }
    public Td setTopPadding(int padding){
        return setPadding("top", padding);
    }
    public Td setTopPadding(double padding){
        return setPadding("top", padding);
    }

    public Td setRightPadding(String padding){
        return setPadding("right", padding);
    }
    public Td setRightPadding(int padding){
        return setPadding("right", padding);
    }
    public Td setRightPadding(double padding){
        return setPadding("right", padding);
    }

    public Td setLeftPadding(String padding){
        return setPadding("left", padding);
    }
    public Td setLeftPadding(int padding){
        return setPadding("left", padding);
    }
    public Td setLeftPadding(double padding){
        return setPadding("left", padding);
    }
    public Td setPadding(String side, String padding){
        styles.put("padding-"+side, padding);
        return this;
    }
    public Td setPadding(String side, int padding){
        return setPadding(side, padding+widthUnit);
    }
    public Td setPadding(String side, double padding){
        return setPadding(side, padding+widthUnit);
    }

    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
    }

}
