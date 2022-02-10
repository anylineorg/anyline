/*
 * Copyright 2006-2022 www.anyline.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package org.anyline.entity.html;

import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.StyleParser;
import org.anyline.util.regular.RegularUtil;

import java.util.*;

public class TableBuilder {
    private Collection datas = null;
    private List<String> headers = new ArrayList<>();
    private String header = null; //复杂的头表直接设置html
    private String footer = null;
    private String clazz = null;
    private List<String> fields = new ArrayList<>();
    private List<String> unions = new ArrayList<>();//需要合并字段，值相同的几行合并(如里相关列合并的情况下才会合并,如前一列学校合并时，后一列班级才有可能合并，班级列名(学校列名,其他列名))
    private Map<String,Map<String,String>> styles = new HashMap<>();
    private Map<String,String[]> unionRefs = new HashMap<>();
    private Map<String,Map<String,String>> options = new HashMap<>();   //外键对应关系
    private List<String> ignoreUnionValues = new ArrayList<>();         //不参与合并的值
    private String width = "100%";                                      //整个表格宽度
    private String widthUnit = "px";                                    //默认长度单位 px pt cm/厘米
    private String replaceEmpty = "";                                   //遇到空值替换成
    private String cellBorder = "";                                     //单元格边框
    private String lineHeight = "";                                     //行高
    private String mergeCellVerticalAlign = "";                         //合并单元格垂直对齐方式
    private String mergeCellHorizontalAlign = "";                       //合并单元格水平对齐方式
    private String emptyCellVerticalAlign = "";                         //空单元格垂直对齐方式
    private String emptyCellHorizontalAlign = "";                       //空单元格水平对齐方式

    public static TableBuilder init(){
        TableBuilder builder = new TableBuilder();
        return builder;
    }


    public String getWidthUnit() {
        return widthUnit;
    }

    /**
     * 设置宽度 单位
     * @param widthUnit px pt cm/厘米
     */
    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
    }

    /**
     * 检测参考列是否合并
     * @param field 当前列
     * @param r 当前行
     * @param prevRow 上一行数据
     * @return boolean
     */
    private boolean checkRefMerge(String field, int r, Object prevRow ){
        boolean merge = true;
        String[] refs = unionRefs.get(field);
        Object data = list[r];
        if(null != refs){
            for (String ref:refs) {
                if(field.equals(ref)){ //如果误加了 参考自己 忽略
                    continue;
                }
                int refIndex = fields.indexOf(ref);
                Map<String, String> refCell = cells[r][refIndex];
                if(null == refCell.get("checked")){
                    checkMerge(r, refIndex, ref);
                }
                String curRefValue = BeanUtil.parseRuntimeValue(data, ref);//当前行 参与列的值
                if(null == curRefValue){
                    curRefValue =replaceEmpty;
                }
                if(ignoreUnionValues.indexOf(curRefValue) != -1){
                    merge = false;
                    break;
                }
                String prevRefValue = BeanUtil.parseRuntimeValue(prevRow, ref);
                if(null == prevRefValue){
                    prevRefValue = replaceEmpty;
                }
                if(!curRefValue.equals(prevRefValue)){
                    //当前行参考列值  与上一行参考列值比较
                    merge = false;
                    break;
                }

                if (null != refCell &&  !"1".equals(refCell.get("merge")) &&  !"1".equals(refCell.get("merged"))) {
                    //依赖列未合并
                    merge = false;
                    break;
                }
            }
        }
        return merge;
    }
    private void checkMerge(int r, int c, String field){

        Map<String,String> map = cells[r][c];
        String value = map.get("value");
        map.put("checked","1");
        int rowspan = 1;
        if(null != unions && unions.contains(field)) {
            //向上查看相同值
            int rr = r+1;
            Object data = list[r];
            int rsize = list.length;
            while (true) {
                if (rr >= rsize) {
                    break;
                }
                //下一行
                Map<String, String> next = cells[rr][c];
                Object prevRow = list[rr];
                String pvalue = next.get("value");
                if (pvalue.equals(value) && ignoreUnionValues.indexOf(value) == -1) {
                    boolean refMerge = checkRefMerge(field, r, prevRow);   //参考列是否已被合并
                    if (refMerge) {//参考列已合并 或没有参考列
                        map.put("merge", "1");  //合并
                        next.put("merged", "1"); //被合并
                        rowspan ++;
                    } else {
                        break;
                    }
                } else {
                    break;
                }
                rr++;
            }
        }

        map.put("rowspan",  rowspan+ "");
    }
    private void checkNum(int c, String field){
        int rows = cells.length;
        int num = 1;
        for(int r=0; r<rows; r++){
            Map<String, String> cell = cells[r][c];
            cell.put("value", num+"");
            String ref = field.substring(field.indexOf("(")+1, field.length()-1);
            int refCol = fields.indexOf(ref);
            if(refCol != -1) {
                Map<String, String> refCell = cells[r][refCol];
                if(null != refCell){
                    int rowspan = BasicUtil.parseInt(refCell.get("rowspan"),1);
                    if(rowspan > 1) {
                        cell.put("merge", "1");  //合并
                        cell.put("rowspan", rowspan+"");
                        //当前行以下rowspan-1行全部被合并
                        for(int rr=r+1; rr<r+rowspan&&rr<rows; rr++){
                            Map<String, String> mergedCell = cells[rr][c];
                            mergedCell.put("merged", "1"); //被合并
                        }
                        r += rowspan-1;
                    }
                    //num -= rowspan;
                }
            }
            num ++;
        }

    }
    //向下求相同值
    Map<String,String>[][] cells = null;
    Object[] list = null;
    public Table build(){
        Table table = null;
        table = new Table();
        if(BasicUtil.isNotEmpty(width)){
            table.setWidth(width);
        }
        parseUnion();
        table.setClazz(clazz);
        table.setHeader(header);
        if(null != header){
            List<String> strs = RegularUtil.cuts(header,"<tr",">","</tr>");
            for(String str:strs){
                List<String> stds = RegularUtil.cuts(str,"<td","</td>");
                Tr tr = new Tr();
                for(String std:stds){
                    Td td = new Td();
                    String text = RegularUtil.cut(std,">",RegularUtil.TAG_END);
                    String style = RegularUtil.fetchAttributeValue(std,"style");
                    int colspan = BasicUtil.parseInt(RegularUtil.fetchAttributeValue(std,"colspan"),1);
                    int rowspan = BasicUtil.parseInt(RegularUtil.fetchAttributeValue(std,"rowspan"),1);
                    td.setColspan(colspan);
                    td.setRowspan(rowspan);
                    td.setText(text);
                    tr.addTd(td);
                }
                table.addTr(tr);
            }
        }
        if(null != headers && headers.size() >0){
            Tr tr = new Tr();
            int size = headers.size();
            for(int i=0; i<size; i++){
                String header = headers.get(i);
                Td td = new Td();
                td.setText(header);
                if(BasicUtil.isNotEmpty(cellBorder)) {
                    td.setBorder();
                }
                tr.addTd(td);
            }
            table.addTr(tr);
        }
        if(null != datas && null != fields){
            list = datas.toArray();
            int rsize = list.length;    //行数
            int csize = fields.size();  //列数
            cells = new HashMap[rsize][csize];
            for(int r=0; r<rsize; r++){
                Object data = list[r];
                for(int c=0; c<csize; c++){
                    Map<String,String> map = new HashMap<>();
                    String field = fields.get(c);
                    String value = null;
                    if(field.equals("{num}")){
                        value = (r+1)+"";
                    }else if(field.contains("{num}")){
                        value = field;
                    }else{
                        value = BeanUtil.parseRuntimeValue(data, field);
                    }
                    //外键对应关系
                    Map<String,String> option = options.get(field);
                    if(null != option && null != value){
                        value = option.get(value);
                    }
                    if(null == value){
                        value = replaceEmpty;
                    }
                    map.put("value", value);
                    cells[r][c] = map;
                }

            }
            //检测合并单元格
            for(int r=0; r<rsize; r++){
                for(int c=0; c<csize; c++){
                    String field = fields.get(c);
                    checkMerge(r, c, field);
                }
            }
            //检测序号 {num}(DEPT_CODE)
            for(int c=0; c<csize; c++){
                String field = fields.get(c);
                if(field.contains("{num}")){
                    checkNum(c, field);
                }
            }

            for(int r=0; r<rsize; r++){
                Object data = list[r];
                Tr tr = new Tr();
                for(int c=0; c<csize; c++){
                    Map<String,String> map = cells[r][c];
                    String value = map.get("value");
                    String merge = map.get("merge");    //合并其他行
                    String merged = map.get("merged");  //被其他行合并
                    int rowspan = BasicUtil.parseInt(map.get("rowspan"),1);
                    if(!"1".equals(merged)){
                        Td td = new Td();
                        td.setText(value);
                        tr.addTd(td);
                        Map<String,String> tdStyle = styles.get(fields.get(c));
                        if(null != tdStyle) {
                            td.setStyles(tdStyle);
                        }
                        if (rowspan > 1) {
                            td.setRowspan(rowspan);
                            if(BasicUtil.isNotEmpty(mergeCellHorizontalAlign)){
                                td.setAlign(mergeCellHorizontalAlign);
                            }
                            if(BasicUtil.isNotEmpty(mergeCellVerticalAlign)){
                                td.setVerticalAlign(mergeCellVerticalAlign);
                            }
                        }
                        if(BasicUtil.isEmpty(value) || replaceEmpty.equals(value)){
                            if(BasicUtil.isNotEmpty(emptyCellHorizontalAlign)){
                                td.setAlign(emptyCellHorizontalAlign);
                            }
                            if(BasicUtil.isNotEmpty(emptyCellVerticalAlign)){
                                td.setVerticalAlign(emptyCellVerticalAlign);
                            }
                        }
                        if(BasicUtil.isNotEmpty(cellBorder)) {
                            td.setBorder();
                        }
                    }
                }
                table.addTr(tr);
            }
        }
        //需要检测变量如合计
        table.setFooter(footer);
        if(null != footer){
            List<String> strs = RegularUtil.cuts(footer,"<tr",">","</tr>");
            for(String str:strs){
                List<String> stds = RegularUtil.cuts(str,"<td","</td>");
                Tr tr = new Tr();
                for(String std:stds){
                    Td td = new Td();
                    String text = RegularUtil.cut(std,">",RegularUtil.TAG_END);
                    String style = RegularUtil.fetchAttributeValue(std,"style");
                    td.setStyles(StyleParser.parse(style));
                    int colspan = BasicUtil.parseInt(RegularUtil.fetchAttributeValue(std,"colspan"),1);
                    int rowspan = BasicUtil.parseInt(RegularUtil.fetchAttributeValue(std,"rowspan"),1);
                    td.setColspan(colspan);
                    td.setRowspan(rowspan);
                    td.setText(text);
                    if(BasicUtil.isNotEmpty(cellBorder)) {
                        td.setBorder();
                    }
                    tr.addTd(td);
                }
                table.addTr(tr);
            }
        }
        table.offset();
        return table;
    }
    private void parseUnion(){
        List<String> list = new ArrayList<>();
        for(String union:unions){
            if(union.contains("(")){
                union = union.trim();
                String[] refs = union.substring(union.indexOf("(")+1, union.length()-1).split(",");
                union = union.substring(0,union.indexOf("("));
                unionRefs.put(union, refs);

            }
            list.add(union);
        }
        setUnions(list);
    }

    public Collection getDatas() {
        return datas;
    }

    public TableBuilder setDatas(Collection datas) {
        this.datas = datas;
        return this;
    }

    public List<String> getHeaders() {
        return headers;
    }

    public TableBuilder setHeaders(List<String> headers) {
        this.headers = headers;
        return this;
    }

    public List<String> getFields() {
        return fields;
    }

    public TableBuilder setFields(List<String> fields) {
        this.fields = fields;
        return this;
    }
    public TableBuilder setFields(String ... fields) {
        this.fields = Arrays.asList(fields);
        return this;
    }

    public List<String> getUnions() {
        return unions;
    }


    public TableBuilder setUnions(List<String> unions) {
        this.unions = unions;
        return this;
    }


    public TableBuilder addUnion(String ... fields) {
        if(null == unions){
            unions = new ArrayList<>();
        }
        for(String field:fields) {
            if(!unions.contains(field)) {
                unions.add(field);
            }
        }
        return this;
    }

    public String getClazz() {
        return clazz;
    }

    public TableBuilder setClazz(String clazz) {
        this.clazz = clazz;
        return this;
    }

    public String getHeader() {
        return header;
    }

    public TableBuilder setHeader(String header) {
        this.header = header;
        return this;
    }

    public String getFooter() {
        return footer;
    }

    public TableBuilder setFooter(String footer) {
        this.footer = footer;
        return this;
    }

    public Map<String,Map<String, String>> getStyles() {
        return styles;
    }

    public Map<String, String> getStyle(String field) {
        return styles.get(field);
    }

    public TableBuilder setStyle(String field, Map<String, String> style) {
        styles.put(field, style);
        return this;
    }
    /**
     * 设置水平对齐方式
     * @param field field
     * @param align left center right
     * @return TableBuilder
     */
    public TableBuilder setHorizontalAlign(String field, String align) {
        Map<String,String> style = styles.get(field);
        if(null == style){
            style = new HashMap<>();
        }
        style.put("text-align", align);
        return this;
    }

    /**
     * 设置所有列的水平对齐方式
     * @param align  left center right
     * @return TableBuilder
     */
    public TableBuilder setHorizontalAlign(String align) {
        if(null != fields) {
            for (String field : fields) {
                setHorizontalAlign(field, align);
            }
        }
        return this;
    }
    public TableBuilder setTextAlign(String field, String align) {
        return setHorizontalAlign(field, align);
    }
    public TableBuilder setTextAlign(String align) {
        return setHorizontalAlign(align);
    }

    /**
     * 设置垂直对齐方式
     * @param field field
     * @param align top middle/center bottom
     * @return TableBuilder
     */
    public TableBuilder setVerticalAlign(String field, String align) {
        Map<String,String> style = styles.get(field);
        if(null == style){
            style = new HashMap<>();
        }
        style.put("vertical-align", align);
        return this;
    }

    /**
     * 设置所有列垂直对齐方式
     * @param align top middle/center bottom
     * @return TableBuilder
     */
    public TableBuilder setVerticalAlign(String align) {
        if(null != fields) {
            for (String field : fields) {
                setVerticalAlign(field, align);
            }
        }
        return this;
    }
    public TableBuilder addConfig(String header, String field, String width){
        headers.add(header);
        fields.add(field);
        Map<String,String> style = styles.get(field);
        if(null == style){
            style = new HashMap<>();
        }
        style.put("width", width);
        return this;
    }
    public TableBuilder addConfig(String header, String field, int width){
        return addConfig(header, field, width+widthUnit);
    }
    public TableBuilder addConfig(String header, String field){
        headers.add(header);
        fields.add(field);
        return this;
    }
    public TableBuilder setWidth(String field, String width){

        return this;
    }

    public TableBuilder setWidth(String width){
        this.width = width;
        return this;
    }


    public List<String> getIgnoreUnionValues() {
        return ignoreUnionValues;
    }

    public TableBuilder setIgnoreUnionValues(List<String> ignoreUnionValue) {
        this.ignoreUnionValues = ignoreUnionValue;
        return this;
    }
    public TableBuilder addIgnoreUnionValue(String ... vals){
        if(null != vals){
            for(String val:vals){
                ignoreUnionValues.add(val);
            }
        }
        return this;
    }

    public String getCellBorder() {
        return cellBorder;
    }

    public TableBuilder setCellBorder(String cellBorder) {
        this.cellBorder = cellBorder;
        return this;
    }
    public TableBuilder setCellBorder(boolean border) {
        if(border){
            cellBorder = "1px solid auto";
        }else{
            cellBorder = "";
        }
        return this;
    }

    public String getLineHeight() {
        return lineHeight;
    }

    public TableBuilder setLineHeight(String lineHeight) {
        this.lineHeight = lineHeight;
        return this;
    }

    public TableBuilder setReplaceEmpty(String replaceEmpty) {
        this.replaceEmpty = replaceEmpty;
        return this;
    }

    public TableBuilder setMergeCellVerticalAlign(String mergeCellVerticalAlign) {
        this.mergeCellVerticalAlign = mergeCellVerticalAlign;
        return this;
    }

    public TableBuilder setMergeCellHorizontalAlign(String mergeCellHorizontalAlign) {
        this.mergeCellHorizontalAlign = mergeCellHorizontalAlign;
        return this;
    }

    public TableBuilder setEmptyCellVerticalAlign(String emptyCellVerticalAlign) {
        this.emptyCellVerticalAlign = emptyCellVerticalAlign;
        return this;
    }

    public TableBuilder setEmptyCellHorizontalAlign(String emptyCellHorizontalAlign) {
        this.emptyCellHorizontalAlign = emptyCellHorizontalAlign;
        return this;
    }
    public TableBuilder setOptions(String field,Map<String,String> option){
        options.put(field, option);
        return this;
    }

    public TableBuilder addOption(String field, String ... kvs){
        Map<String,String> map = options.get(field);
        if(null != kvs){
            Map<String,String> tmps = BeanUtil.array2map(kvs);
            if(map == null){
                map = tmps;
            }else{
                map.putAll(tmps);
            }
        }
        options.put(field, map);
        return this;
    }

    public TableBuilder setOptions(String field, Collection datas, String value, String text){
        Map<String,String> map = new HashMap<>();
        for(Object obj:datas){
            String v = null;
            String t = null;
            Object ov = BeanUtil.getFieldValue(obj, value);
            Object ot = BeanUtil.getFieldValue(obj, text);
            if(null != ov){
                v = ov.toString();
            }
            if(null != ot){
                t = ot.toString();
            }
            map.put(v, t);
        }
        options.put(field, map);
        return this;
    }
}
