package org.anyline.entity.html;

import org.dom4j.Element;

import java.util.*;

public class Table {
    private String clazz;
    private List<Tr> trs = new ArrayList<>();
    private String header = null; //复杂的头表直接设置html
    private String footer = null;
    private boolean isOffset = false;//是否计算过偏移量(多次执行build, merge ,offset等只计算一次,)
    private Map<String,String> styles = new HashMap();
    private List<Integer> mergeRows = new ArrayList<>(); //根据内容合并行依据
    private Map<Integer, List<Integer>> refs = new HashMap<>(); //
    private List<Integer[]> mergeCols = new ArrayList<>();//根据内容合并列，开始列,合并数量
    private Element src;
    private String widthUnit = "px";     //默认长度单位 px pt cm/厘米
    public Table(){}

    /**
     * 构造表格
     * @param rows 行数量
     * @param cols 列数量
     */
    public Table(int rows, int cols){
        trs = new ArrayList<>();
        for(int r=0; r<rows; r++){
            Tr tr = new Tr();
            tr.setTable(this);
            for(int c=0; c<cols; c++){
                Td td = new Td();
                tr.addTd(td);
            }
            trs.add(tr);
        }
    }
    public Td getTd(int row, int col){
        return trs.get(row).getTd(col);
    }
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

    public Table setTd(int rows, int cols, Td td){
        trs.get(rows).setTd(cols, td);
        return this;
    }

    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
        for(Tr tr:trs){
            tr.setWidthUnit(widthUnit);
        }
    }

    public Element getSrc() {
        return src;
    }

    public void setSrc(Element src) {
        this.src = src;
    }

    /**
     * 添加行
     * @param tr tr
     * @return table
     */
    public Table addTr(Tr tr){
        trs.add(tr);
        tr.setTable(this);
        return this;
    }


    /**
     * 创建html
     * @param box 是否需要table标签
     * @return html
     */
    public String build(boolean box){
        merge();
        StringBuilder builder = new StringBuilder();
        int rows = trs.size();
        int cols = 0;
        if(rows>0){
            cols = trs.get(0).getTds().size();
        }
        if(box){
            builder.append("<table");
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
        }
        if(null != header){
            builder.append(header);
        }
        for(Tr tr:trs){
            tr.build(builder);
            builder.append("\r\n");
        }
        if(null != footer){
            builder.append(footer);
        }
        if(box){
            builder.append("</table>");
        }
        return builder.toString();
    }
    public String build(){
        return build(true);
    }
    /**
     * 根据内容是否相同，在右侧qty范围内检测需要合并的列数量
     * @param td 单元格
     * @param qty 检测范围
     * @return colspan 需要合并列的数量
     */
    private int checkColspan(Td td, int qty){
        int colspan= 1;
        int[] index = td.index();
        int end = index[1] + qty;
        String value = td.getText();
        if(null != value){
            for(int i=index[1]+1; i<=end; i++){
                Td cur = getTd(index[0], i);
                if(value.equals(cur.getText())){
                    colspan ++;
                }
            }
        }
        return colspan;
    }

    /**
     * 执行合并列
     */
    private void exeMergeCol(){
        int rows = trs.size();
        //根据内容合并
        for(int r=0; r<rows; r++) {
            Tr tr = getTr(r);
            for(Integer[] mergeCol:mergeCols){
                int mergeIndex = mergeCol[0];
                int mergeQty = mergeCol[1];
                //如果所有值相同则合并
                for(int i=mergeIndex; i<mergeIndex+mergeQty; i++){
                    Td td = getTd(r,mergeIndex);
                    int colspan =checkColspan(td, mergeQty);
                    if(colspan > 1){
                        td.setColspan(colspan);
                        td.merge();
                    }
                }
            }
        }
        //根据rowspan colspan
        for(Tr tr:trs){
            for(Td td:tr.getTds()){
                td.merge();
            }
        }
    }

    /**
     * 根据内容是否相同检测需要合并行的数量
     * @param td 单元格
     * @return rowspan
     */
    private int checkRowspan(Td td){
        int qty= 1;
        int r = td.getRowIndex();
        int c = td.getColIndex();
        String value = td.getText();
        if(null != value) {
            int size = trs.size();
            for (int i = r + 1; i < size; i++) {
                Td cur = getTd(i, c);
                String cvalue = cur.getText();
                if (value.equals(cvalue)) {
                    List<Integer> curRefs = refs.get(c);
                    boolean refMerge = true; //依赖列是否已合并
                    if (null != curRefs) {
                        for (Integer refIndex : curRefs) {
                            Td prev = getTd(i, refIndex); //同一行的 依赖列
                            if(!prev.isRemove()){
                                refMerge = false;
                                break;
                            }
                        }
                        if(refMerge) {
                            qty++;
                        }
                    }
                }else{
                    break;
                }
            }
        }
        return qty;
    }

    /**
     * 执行合并行
     */
    private void exeMergeRow(){
        int rows = trs.size();
        for(int r=0; r<rows; r++) {
            Tr tr = getTr(r);
            for(int col:mergeRows) {
                Td td = tr.getTd(col);
                if(!td.isRemove()){
                    int rowspan = checkRowspan(td);
                    if(rowspan > 1){
                        td.setRowspan(rowspan);
                        td.merge();
                    }
                }
            }
        }
    }
    //根据内容合并
    private void merge(){
        exeMergeRow();
        exeMergeCol();
        offset();
    }
    //根据 colspan rowspan 计算偏移量
    public Table offset(){
        if(isOffset){
            return this;
        }
        isOffset = true;
        int rows = trs.size();
        for(int r=0; r<rows; r++){
            Tr tr = trs.get(r);
            List<Td> tds = tr.getTds();
            int cols = tds.size();
            for(int c=0; c<cols; c++){
                Td td = tds.get(c);
                int colspan = td.getColspan();
                int rowspan = td.getRowspan();
                int offset = colspan -1;
                if( offset > 0){
                    //当前行 往后所有列 偏移增加colspan-1
                    for(int cc=c+1; r<cols; cc++){
                        Td after = tds.get(cc);
                        after.addOffset(offset);
                    }
                }
                if(rowspan > 1){
                    offset ++;
                    //下rowspan-1行
                    for(int rr=r+1; rr<r+rowspan; rr++){
                        Tr afterTr = trs.get(rr);
                        int begin = td.getColIndex()+td.getOffset();
                        List<Td> afterTds = afterTr.getTdsByOffset(begin);
                        for(Td afterTd:afterTds){
                            afterTd.addOffset(offset);
                        }
                    }
                }
            }
        }
        return this;
    }
    /**
     * 设置需要合并行的列下标
     * @param cols 依据列1,2,3(1,2) 第1,2,3列值相同时合并行,第3列合并的前提是第1,2列已合并
     * @return Table
     */
    public Table setMergeRow(String ... cols){
        for(String col:cols){
            List<Integer> refs = new ArrayList<>();
            int c = -1;
            if(col.contains("(")){
                col = col.trim();
                String[] ref = col.substring(col.indexOf("(")+1, col.length()-1).split(",");
                for(int i=0; i<ref.length; i++){
                    refs.add(Integer.parseInt(ref[i]));
                }
                col = col.substring(0,col.indexOf("("));
            }
            c = Integer.parseInt(col);
            mergeRows.add(c);
            this.refs.put(c, refs);
        }
        return this;
    }

    /**
     * 设置需要合并行的列
     * @param cols cols
     * @return Table
     */
    public Table setMergeRow(Integer ... cols){
        mergeRows = Arrays.asList(cols);
        return this;
    }

    /**
     * 设置需要合并的列(根据内容)
     * @param start 开始
     * @param qty 右侧合并范围
     * @return Table
     */
    public Table setMergeCol(int start, int qty){
        Integer[] merge = new Integer[2];
        merge[0] = start;
        merge[1] = qty;
        mergeCols.add(merge);
        return this;
    }

    /**
     * 单元格文本
     * @param row row
     * @param col col
     * @return String
     */
    public String getText(int row, int col){
        Td td = getTd(row, col);
        return td.getText();
    }
    public Table setText(int row, int col, String text){
        Td td = getTd(row, col);
        if(null != text) {
            td.setText(text);
        }
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getFooter() {
        return footer;
    }

    public void setFooter(String footer) {
        this.footer = footer;
    }


    public Td removeLeftBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeLeftBorder();
        return td;
    }
    public Td removeRightBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeRightBorder();
        return td;
    }
    public Td removeTopBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeTopBorder();
        return td;
    }
    public Td removeBottomBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeBottomBorder();
        return td;
    }
    public Td removeTl2brBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeTl2brBorder();
        return td;
    }

    public Td removeTr2blBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeTr2blBorder();
        return td;
    }
    public Td removeBorder(String side, int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeBorder(side);
        return td;
    }

    public Td removeBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.removeBorder();
        return td;
    }
    public Td setBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.setBorder();
        return td;
    }
    public Td setBorder(int rows, int cols, String weight, String color, String style){
        Td td = getTd(rows, cols);
        td.setBorder(weight, color, style);
        return td;
    }
    public Td setLeftBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.setLeftBorder();
        return td;
    }
    public Td setRightBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.setRightBorder();
        return td;
    }
    public Td setTopBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.setTopBorder();
        return td;
    }
    public Td setBottomBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.setBottomBorder();
        return td;
    }
    public Td setTl2brBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.setTl2brBorder();
        return td;
    }

    /**
     * 设置左上至右下边框
     * @param rows rows
     * @param cols cols
     * @param top 右上文本
     * @param bottom 左下文本
     * @return Td
     */
    public Td setTl2brBorder(int rows, int cols, String top, String bottom){
        Td td = getTd(rows, cols);
        td.setTl2brBorder(top, bottom);
        return td;
    }
    public Td setTr2blBorder(int rows, int cols){
        Td td = getTd(rows, cols);
        td.setTr2blBorder();
        return td;
    }
    /**
     * 设置左上至右下边框
     * @param rows rows
     * @param cols cols
     * @param top 左上文本
     * @param bottom 右下文本
     * @return Td
     */
    public Td setTr2blBorder(int rows, int cols, String top, String bottom){
        Td td = getTd(rows, cols);
        td.setTr2blBorder(top, bottom);
        return td;
    }

    public Td setLeftBorder(int rows, int cols, String width, String color, String style){
        return getTd(rows, cols).setLeftBorder(width, color, style);
    }
    public Td setLeftBorder(int rows, int cols, int width, String color, String style){
        return getTd(rows, cols).setLeftBorder(width, color, style);
    }
    public Td setLeftBorder(int rows, int cols, double width, String color, String style){
        return getTd(rows, cols).setLeftBorder(width+widthUnit, color, style);
    }
    public Td setRightBorder(int rows, int cols, String width, String color, String style){
        return getTd(rows, cols).setRightBorder(width, color, style);
    }
    public Td setRightBorder(int rows, int cols, int width, String color, String style){
        return getTd(rows, cols).setRightBorder(width, color, style);
    }
    public Td setRightBorder(int rows, int cols, double width, String color, String style){
        return getTd(rows, cols).setRightBorder(width, color, style);
    }
    public Td setTopBorder(int rows, int cols, String width, String color, String style){
        return getTd(rows, cols).setTopBorder(width, color, style);
    }
    public Td setTopBorder(int rows, int cols, int width, String color, String style){
        return getTd(rows, cols).setTopBorder(width, color, style);
    }
    public Td setTopBorder(int rows, int cols, double width, String color, String style){
        return getTd(rows, cols).setTopBorder(width, color, style);
    }
    public Td setBottomBorder(int rows, int cols, String width, String color, String style){
        return getTd(rows, cols).setBottomBorder(width, color, style);
    }
    public Td setBottomBorder(int rows, int cols, int width, String color, String style){
        return getTd(rows, cols).setBottomBorder(width, color, style);
    }
    public Td setBottomBorder(int rows, int cols, double width, String color, String style){
        return getTd(rows, cols).setBottomBorder(width, color, style);
    }
    public Td setTl2brBorder(int rows, int cols, String width, String color, String style){
        return getTd(rows, cols).setTl2brBorder(width, color, style);
    }
    public Td setTl2brBorder(int rows, int cols, int width, String color, String style){
        return getTd(rows, cols).setTl2brBorder(width, color, style);
    }
    public Td setTl2brBorder(int rows, int cols, double width, String color, String style){
        return getTd(rows, cols).setTl2brBorder(width, color, style);
    }
    public Td setTr2blBorder(int rows, int cols, String width, String color, String style){
        return getTd(rows, cols).setTr2blBorder(width, color, style);
    }
    public Td setTr2blBorder(int rows, int cols, int width, String color, String style){
        return getTd(rows, cols).setTr2blBorder(width, color, style);
    }
    public Td setTr2blBorder(int rows, int cols, double width, String color, String style){
        return getTd(rows, cols).setTr2blBorder(width, color, style);
    }
    public Td setBorder(int rows, int cols, String side, String width, String color, String style){
        return getTd(rows, cols).setBorder(side, width, color, style);
    }
    public Td setBorder(int rows, int cols, String side, int width, String color, String style){
        return getTd(rows, cols).setBorder(side, width, color, style);
    }
    public Td setBorder(int rows, int cols, String side, double width, String color, String style){
        return getTd(rows, cols).setBorder(side, width, color, style);
    }
    public Td setColor(int rows, int cols, String color){
        return getTd(rows, cols).setColor(color);
    }
    public Td setFont(int rows, int cols, String weight, String eastAsia, String ascii, String hint){
        return getTd(rows, cols).setFont(weight, eastAsia, ascii, hint);
    }
    public Td setFontSize(int rows, int cols, String weight){
        return getTd(rows, cols).setFontSize(weight);
    }
    public Td setFontWeight(int rows, int cols, String weight){
        return getTd(rows, cols).setFontSize(weight);
    }
    public Td setFontFamily(int rows, int cols, String font){
        return getTd(rows, cols).setFontFamily(font);
    }

    /**
     * 设置文本水平对齐方式
     * @param rows rows
     * @param cols cols
     * @param align left center right
     * @return Td
     */
    public Td setAlign(int rows, int cols, String align){
        return getTd(rows, cols).setAlign(align);
    }

    /**
     * 设置文本垂直对齐方式
     * @param rows rows
     * @param cols cols
     * @param align top middle|center bottom
     * @return Td
     */
    public Td setVerticalAlign(int rows, int cols, String align){
        return getTd(rows, cols).setVerticalAlign(align);
    }

    /**
     * 背景色
     * @param rows rows
     * @param cols cols
     * @param color 颜色
     * @return Td
     */
    public Td setBackgroundColor(int rows, int cols, String color){
        return getTd(rows, cols).setBackgroundColor(color);
    }

    /**
     * 清除样式
     * @param rows rows
     * @param cols cols
     * @return td
     */
    public Td removeStyle(int rows, int cols){
        return getTd(rows, cols).removeStyle();
    }
    /**
     * 清除背景色
     * @param rows rows
     * @param cols cols
     * @return td
     */
    public Td removeBackgroundColor(int rows, int cols){
        return getTd(rows, cols).removeBackgroundColor();
    }

    /**
     * 清除颜色
     * @param rows rows
     * @param cols cols
     * @return Td
     */
    public Td removeColor(int rows, int cols){
        return getTd(rows, cols).removeColor();
    }
    /**
     * 粗体
     * @param rows rows
     * @param cols cols
     * @param bold 是否
     * @return Td
     */
    public Td setBold(int rows, int cols, boolean bold){
        return getTd(rows, cols).setBold(bold);
    }
    public Td setBold(int rows, int cols){
        return getTd(rows, cols).setBold();
    }

    /**
     * 下划线
     * @param rows rows
     * @param cols cols
     * @param underline 是否
     * @return Td
     */
    public Td setUnderline(int rows, int cols, boolean underline){
        return getTd(rows, cols).setUnderline(underline);
    }
    public Td setUnderline(int rows, int cols){
        return getTd(rows, cols).setUnderline();
    }

    /**
     * 删除线
     * @param rows rows
     * @param cols cols
     * @param strike 是否
     * @return Td
     */
    public Td setStrike(int rows, int cols, boolean strike){
        return getTd(rows, cols).setStrike(strike);
    }
    public Td setStrike(int rows, int cols){
        return getTd(rows, cols).setStrike();
    }

    public Td setDelete(int rows, int cols){
        return getTd(rows, cols).setStrike();
    }
    public Td setDelete(int rows, int cols, boolean strike){
        return getTd(rows, cols).setDelete();
    }
    /**
     * 斜体
     * @param rows rows
     * @param cols cols
     * @param italic 是否
     * @return Td
     */
    public Td setItalic(int rows, int cols, boolean italic){
        return getTd(rows, cols).setItalic(italic);
    }

    public Td setItalic(int rows, int cols){
        return getTd(rows, cols).setItalic();
    }

    /**
     * 设置行高
     * @param rows rows
     * @param cols cols
     * @param height pt/px/cm
     * @return Td
     */
    public Td setLineHeight(int rows, int cols, String height){
        return getTd(rows, cols).setLineHeight(height);
    }
    public Td setLineHeight(int rows, int cols, int height){
        return getTd(rows, cols).setLineHeight(height);
    }
    public Td setLineHeight(int rows, int cols, double height){
        return getTd(rows, cols).setLineHeight(height);
    }
    public Td setColspan(int rows, int cols, int colspan){
        return getTd(rows, cols).setColspan(colspan);
    }
    public Td setRowspan(int rows, int cols, int rowspan){
        return getTd(rows, cols).setRowspan(rowspan);
    }

    public Td setPadding(int rows, int cols, String padding){
        return getTd(rows, cols).setPadding(padding);
    }
    public Td setPadding(int rows, int cols, int padding){
        return getTd(rows, cols).setPadding(padding);
    }
    public Td setPadding(int rows, int cols, double padding){
        return getTd(rows, cols).setPadding(padding);
    }

    public Td setBottomPadding(int rows, int cols, String padding){
        return getTd(rows, cols).setBottomPadding(padding);
    }
    public Td setBottomPadding(int rows, int cols, int padding){
        return getTd(rows, cols).setBottomPadding(padding);
    }
    public Td setBottomPadding(int rows, int cols, double padding){
        return getTd(rows, cols).setBottomPadding(padding);
    }

    public Td setTopPadding(int rows, int cols, String padding){
        return getTd(rows, cols).setTopPadding(padding);
    }
    public Td setTopPadding(int rows, int cols, int padding){
        return getTd(rows, cols).setTopPadding(padding);
    }
    public Td setTopPadding(int rows, int cols, double padding){
        return getTd(rows, cols).setTopPadding(padding);
    }

    public Td setRightPadding(int rows, int cols, String padding){
        return getTd(rows, cols).setRightPadding(padding);
    }
    public Td setRightPadding(int rows, int cols, int padding){
        return getTd(rows, cols).setRightPadding(padding);
    }
    public Td setRightPadding(int rows, int cols, double padding){
        return getTd(rows, cols).setRightPadding(padding);
    }

    public Td setLeftPadding(int rows, int cols, String padding){
        return getTd(rows, cols).setLeftPadding(padding);
    }
    public Td setLeftPadding(int rows, int cols, int padding){
        return getTd(rows, cols).setLeftPadding(padding);
    }
    public Td setLeftPadding(int rows, int cols, double padding){
        return getTd(rows, cols).setLeftPadding(padding);
    }
    public Td setPadding(int rows, int cols, String side, String padding){
        return getTd(rows, cols).setPadding(side,padding);
    }
    public Td setPadding(int rows, int cols, String side, int padding){
        return getTd(rows, cols).setPadding(side,padding);
    }
    public Td setPadding(int rows, int cols, String side, double padding){
        return getTd(rows, cols).setPadding(side,padding);
    }

}
