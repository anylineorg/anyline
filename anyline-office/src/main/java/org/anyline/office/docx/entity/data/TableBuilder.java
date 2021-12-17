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

    private static String TAG_TR_BEGIN = "\t<tr";
    private static String TAG_TR_END = "\t</tr>\n";
    private static String TAG_TD_BEGIN = "\t\t<td";
    private static String TAG_TD_END = "</td>\n";
    private static String TAG_CLOSE = ">";
    private static String TAB = "\t";
    private static String BR = "\n";
    public static TableBuilder init(){
        TableBuilder builder = new TableBuilder();
        return builder;
    }

    public String buildHtml(boolean box){
        StringBuilder build = new StringBuilder();
        parseUnion();
        if(box){
        build.append("<table");
            if(null != clazz){
                build.append(" class='").append(clazz).append("'");
            }
            build.append(TAG_CLOSE).append(BR);
        }
        if(null != header){
            build.append(header);
        }else if(null != headers && headers.size() >0){
            build.append(TAG_TR_BEGIN).append(TAG_CLOSE).append(BR);
            int size = headers.size();
            for(int i=0; i<size; i++){
                String header = headers.get(i);
                build.append(TAG_TD_BEGIN);
                if(null != widths && i<widths.size()){
                    build.append(" style='width:").append(widths.get(i)).append("'");
                }
                build.append(TAG_CLOSE).append(header).append(TAG_TD_END);
            }
            build.append(TAG_TR_END);
        }
        if(null != datas && null != fields){
            int dsize = datas.size();
            int ksize = fields.size();
            Object[] objs = datas.toArray();
            Map<String,String>[][] cells = new HashMap[dsize][ksize];
            for(int i=0; i<dsize; i++){
                Object data = objs[i];
                for(int j=0; j<ksize; j++){
                    String field = fields.get(j);
                    String value = null;
                    if(field.equals("{num}")){
                        value = (i+1)+"";
                    }else{
                        value = BeanUtil.parseRuntimeValue(data, field);
                    }
                    Map<String,String> map = new HashMap<>();
                    map.put("value", value);
                    cells[i][j] = map;
                    if(null != unions && unions.contains(field)) {
                        //向上查看相同值
                        int ii = 1;
                        while (true) {
                            if (i - ii < 0) {
                                break;
                            }
                            Map<String, String> prev = cells[i - ii][j];
                            Object prevRow = objs[i-ii];
                            String pvalue = prev.get("value");
                            if (null != pvalue && pvalue.equals(value)) {
                                boolean leftMerge = true;
                                String[] refs = unionRefs.get(field);
                                if(null != refs){
                                    for (String ref:refs) {
                                        int refIndex = fields.indexOf(ref);
                                        Map<String, String> left = cells[i][refIndex];
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
                                    prev.put("rowspan", BasicUtil.parseInt(prev.get("rowspan"), 1) + 1 + "");
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                            ii++;
                        }
                    }
                }
            }

            for(int i=0; i<dsize; i++){
                Object data = objs[i];
                build.append(TAG_TR_BEGIN).append(TAG_CLOSE).append(BR);
                for(int j=0; j<ksize; j++){
                    Map<String,String> map = cells[i][j];
                    String value = map.get("value");
                    String remove = map.get("remove");
                    String rowspan = map.get("rowspan");
                    String width = "";
                    String style = "";
                    if(j<widths.size()){
                        width = widths.get(j);
                    }
                    if(j<styles.size()){
                        style = styles.get(j);
                    }
                    if(!"1".equals(remove)){
                        build.append(TAG_TD_BEGIN);
                        if (null != rowspan) {
                            build.append(" rowspan='").append(rowspan).append("'");
                        }
                        build.append(" style='");
                        if(null != width){
                            build.append("width:").append(width).append(";");
                        }
                        if(null != style){
                            build.append(style);
                        }
                        build.append("'");
                        build.append(TAG_CLOSE);
                        if(null != value){
                            build.append(value.trim());
                        }
                        build.append(TAG_TD_END);
                    }
                }
                build.append(TAG_TR_END);
            }
        }
        if(null != footer){
            build.append(footer);
        }
        if(box) {
            build.append("</table>");
        }

        return build.toString();
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
            for(int i=0; i<dsize; i++){
                Object data = objs[i];
                for(int j=0; j<ksize; j++){
                    String field = fields.get(j);
                    String value = null;
                    if(field.equals("{num}")){
                        value = (i+1)+"";
                    }else{
                        value = BeanUtil.parseRuntimeValue(data, field);
                    }
                    Map<String,String> map = new HashMap<>();
                    map.put("value", value);
                    cells[i][j] = map;
                    if(null != unions && unions.contains(field)) {
                        //向上查看相同值
                        int ii = 1;
                        while (true) {
                            if (i - ii < 0) {
                                break;
                            }
                            Map<String, String> prev = cells[i - ii][j];
                            Object prevRow = objs[i-ii];
                            String pvalue = prev.get("value");
                            if (null != pvalue && pvalue.equals(value)) {
                                boolean leftMerge = true;
                                String[] refs = unionRefs.get(field);
                                if(null != refs){
                                    for (String ref:refs) {
                                        int refIndex = fields.indexOf(ref);
                                        Map<String, String> left = cells[i][refIndex];
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
                                    prev.put("rowspan", BasicUtil.parseInt(prev.get("rowspan"), 1) + 1 + "");
                                } else {
                                    break;
                                }
                            } else {
                                break;
                            }
                            ii++;
                        }
                    }
                }
            }

            for(int i=0; i<dsize; i++){
                Object data = objs[i];
                Tr tr = new Tr();
                for(int j=0; j<ksize; j++){
                    Map<String,String> map = cells[i][j];
                    String value = map.get("value");
                    String remove = map.get("remove");
                    String rowspan = map.get("rowspan");
                    String width = "";
                    String style = "";
                    if(j<widths.size()){
                        width = widths.get(j);
                    }
                    if(j<styles.size()){
                        style = styles.get(j);
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


    public TableBuilder addUnion(String union) {
        if(null == unions){
            unions = new ArrayList<>();
        }
        unions.add(union);
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
    public TableBuilder addConfig(String header, String field){
        headers.add(header);
        fields.add(field);
        widths.add(null);
        styles.add(null);
        return this;
    }
}
