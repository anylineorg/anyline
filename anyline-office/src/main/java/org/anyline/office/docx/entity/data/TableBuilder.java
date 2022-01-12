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
package org.anyline.office.docx.entity.data;
 
import org.anyline.office.docx.entity.html.Table;
import org.anyline.office.docx.entity.html.Td;
import org.anyline.office.docx.entity.html.Tr;
import org.anyline.office.docx.util.StyleParser;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;

import java.util.*;

public class TableBuilder {
    private Collection datas = null;
    private List<String> headers = new ArrayList<>();
    private String header = null; //复杂的头表直接设置html
    private String footer = null;
    private String clazz = null;
    private List<String> fields = new ArrayList<>();
    private List<String> unions = new ArrayList<>();//需要合并字段，值相同的几行合并(如里相关列合并的情况下才会合并,如前一列学校合并时，后一列班级才有可能合并，班级列名(学校列名,其他列名))
    private List<String> widths = new ArrayList<>();
    private List<String> styles = new ArrayList<>();
    private Map<String,String[]> unionRefs = new HashMap<>();
    private String widthUnit = "px";     //默认长度单位 px pt cm/厘米

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

    public Table build(){
        parseUnion();
        Table table = new Table();
        table.setClazz(clazz);
        table.setHeader(header);
        table.setFooter(footer);

         if(null != headers && headers.size() >0){
             Tr tr = new Tr();
            int size = headers.size();
            for(int i=0; i<size; i++){
                String header = headers.get(i);
                Td td = new Td();
                td.setText(header);
                if(null != widths && i<widths.size()){
                    td.setWidth(widths.get(i));
                }
                tr.addTd(td);
            }
            table.addTr(tr);
        }
        if(null != datas && null != fields){
            int dsize = datas.size();
            int ksize = fields.size();
            Object[] objs = datas.toArray();
            Map<String,String>[][] cells = new HashMap[dsize][ksize];
            for(int r=0; r<dsize; r++){
                Object data = objs[r];
                for(int c=0; c<ksize; c++){
                    String field = fields.get(c);
                    String value = null;
                    if(field.equals("{num}")){
                        value = (r+1)+"";
                    }else{
                        value = BeanUtil.parseRuntimeValue(data, field);
                    }
                    Map<String,String> map = new HashMap<>();
                    map.put("value", value);
                    cells[r][c] = map;
                    if(null != unions && unions.contains(field)) {
                        //向上查看相同值
                        int rr = 1;
                        while (true) {
                            if (r - rr < 0) {
                                break;
                            }
                            //上一行
                            Map<String, String> prev = cells[r - rr][c];
                            Object prevRow = objs[r-rr];
                            String pvalue = prev.get("value");
                            if (null != pvalue && pvalue.equals(value)) {
                                boolean leftMerge = true;   //左侧是否已被合并
                                String[] refs = unionRefs.get(field);
                                if(null != refs){
                                    for (String ref:refs) {
                                        int refIndex = fields.indexOf(ref);
                                        Map<String, String> left = cells[r][refIndex];
                                        String curRefValue = BeanUtil.parseRuntimeValue(data, ref);
                                        String prevRefValue = BeanUtil.parseRuntimeValue(prevRow, ref);
                                        if(null ==curRefValue  || !curRefValue.equals(prevRefValue)){
                                            //当前行左侧值  与上一行左侧值比较
                                            leftMerge = false;
                                            break;
                                        }

                                        if (!"1".equals(left.get("remove"))) {
                                            //依赖列未合并
                                            leftMerge = false;
                                            break;
                                        }
                                    }
                                }
                                if (leftMerge) {//左列是否已合并
                                    map.put("remove", "1");
                                    map.put("merge", "1");
                                    prev.put("merge", "1");
                                    int rowspan = BasicUtil.parseInt(prev.get("rowspan"), 1) + 1;
                                    prev.put("rowspan",  rowspan+ "");
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                            rr++;
                        }
                    }
                }
            }

            for(int r=0; r<dsize; r++){
                Object data = objs[r];
                Tr tr = new Tr();
                for(int c=0; c<ksize; c++){
                    Map<String,String> map = cells[r][c];
                    String value = map.get("value");
                    String remove = map.get("remove");
                    String rowspan = map.get("rowspan");
                    String width = "";
                    String style = "";
                    if(c<widths.size()){
                        width = widths.get(c);
                    }
                    if(c<styles.size()){
                        style = styles.get(c);
                    }
                    if(!"1".equals(remove)){
                        Td td = new Td();
                        if (null != rowspan) {
                            td.setRowspan(BasicUtil.parseInt(rowspan,1));
                        }
                        if(BasicUtil.isNotEmpty(width)){
                            td.setWidth(width);
                        }
                        if(BasicUtil.isNotEmpty(style)){
                            td.setStyles(StyleParser.parse(style));
                        }
                        td.setText(value);
                        tr.addTd(td);
                    }
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
            unions.add(field);
        }
        return this;
    }

    public List<String> getWidths() {
        return widths;
    }

    public TableBuilder setWidths(List<String> widths) {
        this.widths = widths;
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

    public List<String> getStyles() {
        return styles;
    }

    public TableBuilder setStyles(List<String> styles) {
        this.styles = styles;
        return this;
    }
    public TableBuilder addConfig(String header, String field, String width, String style){
        headers.add(header);
        fields.add(field);
        widths.add(width);
        styles.add(style);
        return this;
    }
    public TableBuilder addConfig(String header, String field, int width, String style){
        return addConfig(header, field, width+widthUnit, style);
    }
    public TableBuilder addConfig(String header, String field){
        headers.add(header);
        fields.add(field);
        widths.add(null);
        styles.add(null);
        return this;
    }
}
