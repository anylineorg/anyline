package org.anyline.office.docx.entity;

import org.anyline.entity.html.TableBuilder;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.BasicUtil;
import org.anyline.util.BeanUtil;
import org.anyline.util.NumberUtil;
import org.anyline.util.StyleParser;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Wtable {
    private WDocument doc;
    private Element src;
    private String widthUnit = "px";     // 默认长度单位 px pt cm/厘米
    private List<Wtr> wtrs = new ArrayList<>();
    //是否自动同步(根据word源码重新构造 wtable wtr wtc)
    //在大批量操作时需要关掉自动同步,在操作完成后调用一次 reload()
    private boolean isAutoLoad = true;
    public Wtable(WDocument doc){
        this.doc = doc;
        load();
    }
    public Wtable(WDocument doc, Element src){
        this.doc = doc;
        this.src = src;
        load();
    }
    public void reload(){
        load();
    }
    private void load(){
        wtrs.clear();
        List<Element> elements = src.elements("tr");
        for(Element element:elements){
            Wtr tr = new Wtr(doc, this, element);
            wtrs.add(tr);
        }
    }

    /**
     * 根据书签获取行
     * @param bookmark 书签
     * @return wtr
     */
    public Wtr getTr(String bookmark){
        Element src = getParent(bookmark, "tr");
        Wtr tr = new Wtr(doc,this, src);
        return tr;
    }


    public Element getParent(String bookmark, String tag){
        return doc.getParent(bookmark, tag);
    }


    private Wtr tr(Wtr template, Element src){
        Wtr tr = new Wtr(doc, this, template.getSrc().createCopy());
        tr.removeContent();
        List<Element> tds = src.elements("td");
        for(int i=0; i<tds.size(); i++){
            Wtc wtc = tr.getTc(i);
            Element td = tds.get(i);
            Map<String,String> styles = StyleParser.parse(td.attributeValue("style"));
            wtc.setHtml(td);
            // this.doc.block(tc, null, td, null);
            /*Element t = DomUtil.element(tc,"t");
            if(null == t){
                t = tc.element("p").addElement("w:r").addElement("w:t");
            }
            String text = td.getTextTrim();
            t.setText(td.getTextTrim());*/

        }
        return tr;
    }
    private Wtr tr(Element src){
        Element tr = this.src.addElement("w:tr");
        Wtr wtr = new Wtr(this.doc, this, tr);
        List<Element> tds = src.elements("td");
        for(int i=0; i<tds.size(); i++){
            Element tc = tr.addElement("w:tc");
            Wtc wtc = new Wtc(doc, wtr, tc);
            Element td = tds.get(i);
            wtc.setHtml(td);
        }
        return wtr;
    }

    /**
     * 获取模板行
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @return Wtr
     */
    public Wtr getTemplate(Integer index){
        Wtr template = null;
        int size = wtrs.size();
        if(size>0){
            if(null == index){
                template = wtrs.get(size-1);
            }else {
                index = index(index, size);
                template = wtrs.get(index);
            }

        }
        return template;
    }

    /**
     * 在最后位置插入一行
     * @param html html.tr源码
     */
    public void insert(String html){
        Integer index = null;
        insert(index, html);
    }

    /**
     * 在最后位置插入一行,半填充内容
     * 内容从data中获取
     * @param data DataRow/Map/Entity
     * @param cols data的属性
     */
    public void insert(Object data, String ... cols){
        Integer index = null;
        Wtr template = getTemplate(index);
        insert(index, template, data, cols);
    }
    public void append(Object data, String ... cols){
        Integer index = null;
        Wtr template = getTemplate(index);
        insert(index, template, data, cols);
    }

    /**
     * 在index位置插入一行,原来index位置的行被挤到下一行,并填充内容
     * 内容从data中获取
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param data DataRow/Map/Entity
     * @param cols data的属性
     */
    public void insert(Integer index, Object data, String ... cols){
        Wtr template = getTemplate(index);
        insert(index, template, data, cols);
    }

    /**
     * 根据模版样式和数据 插入行
     * @param template 模版行
     * @param data 数据可以是一个实体也可以是一个集合
     * @param fields 指定从数据中提取的数据的属性或key
     */
    public void insert(Wtr template, Object data, String ... fields){
        insert(null, template, data, fields);
    }
    public void append(Wtr template, Object data, String ... fields){
        insert(null, template, data, fields);
    }
    /**
     * 根据模版样式和数据 插入行,原来index位置的行被挤到下一行
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param template 模版行
     * @param data 数据可以是一个实体也可以是一个集合
     * @param fields 指定从数据中提取的数据的属性或key
     */
    public void insert(Integer index, Wtr template, Object data, String ... fields){
        Collection datas = null;
        if(data instanceof Collection){
            datas = (Collection)data;
        }else{
            datas = new ArrayList();
            datas.add(data);
        }
        TableBuilder builder = TableBuilder.init().setFields(fields).setDatas(datas);
        String html = builder.build().build(false);
        insert(index, template, html);
    }

    /**
     * 插入行,原来index位置的行被挤到下一行,并填充内容
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param tds 每列的文本 数量多于表格列的 条目无效
     */
    public void insert(Integer index, List<String> tds){
        int size = NumberUtil.min(tds.size(), wtrs.get(0).getTcs().size());
        StringBuilder builder = new StringBuilder();
        builder.append("<tr>");
        for(int i=0; i<size; i++){
            builder.append("<td>");
            builder.append(tds.get(i));
            builder.append("</td>");
        }
        builder.append("</tr>");
        insert(index, builder.toString());
    }

    public void append(List<String> tds){
        insert(null, tds);
    }
    /**
     * 追加行
     * @param tds 每列的文本 数量多于表格列的 条目无效
     */
    public void insert(List<String> tds){
        Integer index = null;
        insert(index, tds);
    }
    /**
     * 在index位置插入行,原来index位置的行被挤到下一行,并填充内容
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param tds 每列的文本 数量多于表格列的 条目无效
     */
    public void insert(Integer index, String ... tds){
        insert(index, BeanUtil.array2list(tds));
    }
    /**
     * 追加行，并填充内容
     * @param tds 每列的文本 数量多于表格列的 条目无效
     */
    public void insert(String ... tds){
        Integer index = null;
        insert(index, tds);
    }
    public void append(String ... tds){
        Integer index = null;
        insert(index, tds);
    }
    /**
     * 在index位置插入行,原来index位置的行被挤到下一行，以template为模板
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param template 模板
     * @param qty 插入数量
     * @return Wtable
     */
    public Wtable insert(Integer index, Wtr template, int qty){
        List<Element> trs = src.elements("tr");
        int idx = index(index, trs.size());
        for(int i=0; i<qty; i++) {
            Element newTr = template.getSrc().createCopy();
            DocxUtil.removeContent(newTr);
            if(null == index) {
                trs.add(newTr);
            }else{
                trs.add(idx++, newTr);
            }
        }
        reload();
        return this;
    }
    public Wtable append(Wtr template, int qty){
        return insert(null, template, qty);
    }
    /**
     * 在index位置插入qty行，以原来index位置行为模板,原来index位置以下行的挤到下一行
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param qty 插入数量
     * @return Wtable
     */
    public Wtable insert(Integer index, int qty){
        int size = wtrs.size();
        if(size > 0){
            Wtr template = getTemplate(index);
            return insert(index, template, qty);
        }
        return this;
    }
    /**
     * 在最后位置插入qty行，以最后一行为模板
     * @param qty 插入数量
     * @return Wtable
     */
    public Wtable insert(int qty){
        return insert(null, qty);
    }
    public Wtable append(int qty){
        return insert(null, qty);
    }

    /**
     * 在index位置插入1行，以原来index位置行为模板,原来index位置以下行的挤到下一行
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param html html内容
     */
    public void insert(Integer index, String html){
        List<Element> trs = src.elements("tr");
        Wtr template = getTemplate(index); //取原来在当前位置的一行作模板
        insert(index, template, html);
    }
    public void append(String html){
        Integer index = null;
        insert(index, html);
    }

    /**
     * 插入行 如果模板位于当前表中则从当前模板位置往后插入，否则插入到最后一行
     * @param template 模板
     * @param html html.tr源码
     */
    public void insert(Wtr template, String html){
        Integer index = null;
        if(null != template) {
            List<Element> trs = src.elements("tr");
            index = trs.indexOf(template.getSrc());
        }
        insert(index,template,  html);
    }
    public void append(Wtr template, String html){
        insert(template, html);
    }

    /**
     * 根据模版样式 插入行
     * @param index 插入位置下标 负数表示倒数 插入 null表示从最后追加与append效果一致
     * @param template 模版行
     * @param html html片段 片段中应该有多个tr,不需要上级标签table
     */
    public void insert(Integer index, Wtr template, String html){
        List<Element> trs = src.elements("tr");
        int idx = index(index, trs.size());

        /*
        if(index == -1 && null != template){
            index = trs.indexOf(template.getSrc());
        }
        */
        try {
            org.dom4j.Document doc = DocumentHelper.parseText("<root>"+html+"</root>");
            Element root = doc.getRootElement();
            List<Element> rows = root.elements("tr");
            for(Element row:rows){
                Element newTr = null;
                if(null != template) {
                    newTr = tr(template, row).getSrc();
                }else{
                    newTr = tr(row).getSrc();
                    trs.remove(newTr);
                }
                if(null == index){
                    trs.add(newTr);
                }else {
                    trs.add(idx++, newTr);
                }
            }
            if(isAutoLoad) {
                reload();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    /**
     * 删除行
     * @param index  下标从0开始  负数表示倒数第index行
     */
    public void remove(int index){
        List<Element> trs = src.elements("tr");
        if(trs.size() == 0){
            return;
        }
        index = index(index, trs.size());
        trs.remove(index);
        if(isAutoLoad) {
            reload();
        }
    }
    public void remove(Wtr tr){
        List<Element> trs = src.elements("tr");
        trs.remove(tr.getSrc());
        if(isAutoLoad) {
            reload();
        }
    }

    /**
     * 获取row行col列的文本
     * @param row 行
     * @param col 列
     * @return String
     */
    public String getText(int row, int col){
        String text = null;
        List<Element> trs = src.elements("tr");
        Element tr = trs.get(row);
        List<Element> tcs = tr.elements("tc");
        Element tc = tcs.get(col);
        text = DocxUtil.text(tc);
        return text;
    }

    /**
     * 设置row行col列的文本
     * @param row 行
     * @param col 列
     * @param text 内容 不支持html标签 如果需要html标签 调用setHtml()
     * @return wtable
     */
    public Wtable setText(int row, int col, String text){
        return setText(row, col, text, null);
    }
    /**
     * 在row行col列原有基础上追加文本
     * @param row 行
     * @param col 列
     * @param text 内容 不支持html标签 如果需要html标签 调用setHtml()
     * @return wtable
     */
    public Wtc addText(int row, int col, String text){
        return getTc(row, col).addText(text);
    }

    /**
     *
     * 设置row行col列的文本 并设置样式
     * @param row 行
     * @param col 列
     * @param text 内容 不支持html标签 如果需要html标签 调用setHtml()
     * @param styles css样式
     * @return wtable
     */
    public Wtable setText(int row, int col, String text, Map<String,String> styles){
        Wtc tc = getTc(row, col);
        if(null != tc){
            tc.setText(text, styles);
        }
        return this;
    }
    /**
     * 设置row行col列的文本 支持html标签
     * @param row 行
     * @param col 列
     * @param html 内容
     * @return wtable
     */
    public Wtable setHtml(int row, int col, String html){
        Wtc tc = getTc(row, col);
        if(null != tc) {
            tc.setHtml(html);
        }
        return this;
    }

    /**
     * 追加列, 每一行追加,追加的列将复制前一列的样式(背景色、字体等)
     * @param qty 追加数量
     * @return table table
     */
    public Wtable addColumns(int qty){
        insertColumns(-1, qty);
        return this;
    }

    /**
     * 插入列
     * 追加的列将复制前一列的样式(背景色、字体等)
     * 如果col=0则将制后一列的样式(背景色、字体等)
     * @param col 插入位置 -1:表示追加以最后一行
     * @param qty 数量
     * @return table table
     */
    public Wtable insertColumns(int col, int qty){
        List<Element> trs = src.elements("tr");
        for(Element tr:trs){
            List<Element> tcs = tr.elements("tc");
            int cols = tcs.size();
            if(cols > 0 && col < cols){
                Element template = null;
                if(col == 0){
                    template = tcs.get(0);
                }else if(col == -1){
                    template = tcs.get(cols-1);
                }else{
                    template = tcs.get(col-1);
                }
                int index = col;
                for (int i = 0; i < qty; i++) {
                    Element newTc = template.createCopy();
                    DocxUtil.removeContent(newTc);
                    if(col == -1){//追加到最后
                        tcs.add(newTc);
                    }else {
                        tcs.add(index++, newTc);
                    }
                }
            }else {
                for (int i = 0; i < qty; i++) {
                    tr.addElement("w:tc").addElement("w:p");
                }
            }
        }
        if(isAutoLoad) {
            reload();
        }
        return this;
    }
    /**
     * 追加行,追加的行将复制上一行的样式(背景色、字体等)
     * @param index 插入位置下标 负数表示倒数第index行 插入 null表示从最后追加与append效果一致
     * @param qty 追加数量
     * @return table table
     */
    public Wtable insertRows(Integer index, int qty){
        if(wtrs.size()>0){
            insertRows(getTemplate(index), index, qty);
        }
        return this;
    }

    /**
     * 以template为模板 在index位置插入qty行，以原来index位置行为模板,原来index位置以下行的挤到下一行
     * @param template 模板
     * @param index 插入位置
     * @param qty 插入数量
     * @return wtable
     */
    public Wtable insertRows(Wtr template, Integer index, int qty){
        List<Element> trs = src.elements("tr");
        int idx = index(index, trs.size());
        if(trs.size()>0){
            for(int i=0; i<qty; i++) {
                Element newTr = template.getSrc().createCopy();
                DocxUtil.removeContent(newTr);
                if(null == index){
                    trs.add(newTr);
                }else {
                    trs.add(index++, newTr);
                }
            }
        }
        if(isAutoLoad) {
            reload();
        }
        return this;
    }

    /**
     * 追加qty行
     * @param qty 行数
     * @return tables
     */
    public Wtable addRows(int qty){
        return insertRows(null, qty);
    }


    /**
     * 追加行,追加的行将复制上一行的样式(背景色、字体等)
     * @param index 位置  下标从0开始  负数表示倒数第index行
     * @param qty 追加数量
     * @return table table
     */
    public Wtable addRows(int index, int qty){
        return insertRows(index, qty);
    }

    /**
     * 获取行数
     * @return int
     */
    public int getTrSize(){
        return src.elements("tr").size();
    }
    public Wtable setWidth(String width){
        Element pr = DocxUtil.addElement(src, "tblPr");
        DocxUtil.addElement(pr, "tcW","w", DocxUtil.dxa(width)+"");
        DocxUtil.addElement(pr, "tcW","type", DocxUtil.widthType(width));
        return this;
    }

    /**
     * 设置表格宽度 默认px
     * @param width 宽度
     * @return wtable
     */
    public Wtable setWidth(int width){
        return setWidth(width+widthUnit);
    }
    /**
     * 设置表格宽度 默认px
     * @param width 宽度
     * @return wtable
     */
    public Wtable setWidth(double width){
        return setWidth(width+widthUnit);
    }

    /**
     * 合并行列
     * @param row 开始行
     * @param col 开始列
     * @param rowspan 合并行数量
     * @param colspan 合并列数量
     * @return wtable
     */
    public Wtable merge(int row, int col, int rowspan, int colspan){
        reload();
        for(int r=row; r<row+rowspan; r++){
            for(int c=col; c<col+colspan; c++){
                Wtc tc = getTc(r, c);
                Element pr = DocxUtil.addElement(tc.getSrc(), "tcPr");
                if(rowspan > 1){
                    if(r==row){
                        DocxUtil.addElement(pr, "vMerge", "val",   "restart");
                    }else{
                        DocxUtil.addElement(pr, "vMerge");
                    }
                }
                if(colspan>1){
                    if(c==col){
                        DocxUtil.addElement(pr, "gridSpan", "val",   colspan+"");
                    }else{
                        tc.remove();
                    }
                }
            }
        }
        reload();
        return this;
    }
    public List<Wtr> getTrs(){
        return wtrs;
    }
    public Wtr getTr(int index){
        index = index(index, wtrs.size());
        return wtrs.get(index);
    }

    /**
     * 获取row行col列位置的单元格
     * @param row 行
     * @param col 列
     * @return wtc
     */
    public Wtc getTc(int row, int col){
        Wtr wtr = getTr(row);
        if(null == wtr){
            return null;
        }
        return wtr.getTc(col);
    }

    public Wtable removeBorder(){
        removeTopBorder();
        removeBottomBorder();
        removeLeftBorder();
        removeRightBorder();
        removeInsideHBorder();
        removeInsideVBorder();
        removeTl2brBorder();
        removeTr2blBorder();
        return this;
    }


    /**
     * 清除表格上边框
     * @return wtable
     */
    public Wtable removeTopBorder(){
        removeBorder(src, "top");
        return this;
    }
    /**
     * 清除表格左边框
     * @return wtable
     */
    public Wtable removeLeftBorder(){
        removeBorder(src, "left");
        return this;
    }
    /**
     * 清除表格右边框
     * @return wtable
     */
    public Wtable removeRightBorder(){
        removeBorder(src, "right");
        return this;
    }
    /**
     * 清除表格下边框
     * @return wtable
     */
    public Wtable removeBottomBorder(){
        removeBorder(src, "bottom");
        return this;
    }
    /**
     * 清除表格垂直边框
     * @return wtable
     */
    public Wtable removeInsideVBorder(){
        removeBorder(src, "insideV");
        return this;
    }
    public Wtable removeTl2brBorder(){
        removeBorder(src, "tl2br");
        return this;
    }
    public Wtable removeTr2blBorder(){
        removeBorder(src, "tr2bl");
        return this;
    }

    /**
     * 清除表格水平边框
     * @return wtable
     */
    public Wtable removeInsideHBorder(){
        removeBorder(src, "insideH");
        return this;
    }
    /**
     * 清除所有单元格边框
     * @return wtable
     */
    public Wtable removeTcBorder(){
        for(Wtr tr:wtrs){
            List<Wtc> tcs = tr.getTcs();
            for(Wtc tc:tcs){
                tc.removeBorder();
            }
        }
        return this;
    }

    /**
     * 清除所有单元格颜色
     * @return wtable
     */
    public Wtable removeTcColor(){
        for(Wtr tr:wtrs){
            List<Wtc> tcs = tr.getTcs();
            for(Wtc tc:tcs){
                tc.removeColor();
            }
        }
        return this;
    }

    /**
     * 清除所有单元格背景色
     * @return wtable
     */
    public Wtable removeTcBackgroundColor(){
        for(Wtr tr:wtrs){
            List<Wtc> tcs = tr.getTcs();
            for(Wtc tc:tcs){
                tc.removeBackgroundColor();
            }
        }
        return this;
    }


    private void removeBorder(Element tbl, String side){
        Element tcPr = DocxUtil.addElement(tbl, "tblPr");
        Element borders = DocxUtil.addElement(tcPr, "tblBorders");
        Element border = DocxUtil.addElement(borders, side);
        border.addAttribute("w:val","nil");
        DocxUtil.removeAttribute(border, "sz");
        DocxUtil.removeAttribute(border, "space");
        DocxUtil.removeAttribute(border, "color");
    }

    /**
     * 删除整行的上边框
     * @param row 行
     * @return Wtr
     */
    public Wtr removeTopBorder(int row){
        Wtr tr = getTr(row);
        List<Wtc> tcs = tr.getTcs();
        for(Wtc tc:tcs){
            tc.removeTopBorder();
        }
        return tr;
    }

    /**
     * 删除整行的下边框
     * @param row 行
     * @return wtr
     */
    public Wtr removeBottomBorder(int row){
        Wtr tr = getTr(row);
        tr.removeBottomBorder();
        return tr;
    }

    /**
     * 删除整列的左边框
     * @param col 列
     * @return Wtable
     */
    public Wtable removeLeftBorder(int col){
        for(Wtr tr: wtrs){
            Wtc tc = tr.getTcWithColspan(col, true);
            if(null != tc){
                tc.removeLeftBorder();
            }
        }
        return this;
    }

    /**
     * 删除整列的右边框
     * @param col 列
     * @return Wtable
     */
    public Wtable removeRightBorder(int col){
        for(Wtr tr: wtrs){
            Wtc tc = tr.getTcWithColspan(col, false);
            if(null != tc){
                tc.removeRightBorder();
            }
        }
        return this;
    }


    /**
     * 清除单元格左边框
     * @param row 行
     * @param col 列
     * @return Wtc
     */
    public Wtc removeLeftBorder(int row, int col){
        return getTc(row, col).removeLeftBorder();
    }
    /**
     * 清除单元格右边框
     * @param row 行
     * @param col 列
     * @return Wtc
     */
    public Wtc removeRightBorder(int row, int col){
        return getTc(row, col).removeRightBorder();
    }
    /**
     * 清除单元格上边框
     * @param row 行
     * @param col 列
     * @return Wtc
     */
    public Wtc removeTopBorder(int row, int col){
        return getTc(row, col).removeTopBorder();
    }
    /**
     * 清除单元格下边框
     * @param row 行
     * @param col 列
     * @return Wtc
     */
    public Wtc removeBottomBorder(int row, int col){
        return getTc(row, col).removeBottomBorder();
    }
    /**
     * 清除单元格左上到右下边框
     * @param row 行
     * @param col 列
     * @return wtable
     */
    public Wtc removeTl2brBorder(int row, int col){
        return getTc(row, col).removeTl2brBorder();
    }
    /**
     * 清除单元格右上到左下边框
     * @param row 行
     * @param col 列
     * @return wtable
     */
    public Wtc removeTr2blBorder(int row, int col){
        return getTc(row, col).removeBorder();
    }

    /**
     * 清除单元格所有边框
     * @param row 行
     * @param col 列
     * @return wtable
     */
    public Wtc removeBorder(int row, int col){
        return getTc(row, col)
                .removeLeftBorder()
                .removeRightBorder()
                .removeTopBorder()
                .removeBottomBorder()
                .removeTl2brBorder()
                .removeTr2blBorder();
    }

    public Wtr setBorder(int row){
        Wtr tr = getTr(row);
        tr.setBorder();
        return tr;
    }

    /**
     * 设置所有单元格默认边框
     * @return table table
     */
    public Wtable setCellBorder(){
        for(Wtr tr:wtrs){
            tr.setBorder();
        }
        return this;
    }
    /**
     * 设置单元格默认边框
     * @param row 行
     * @param col 列
     * @return  Wtc
     */
    public Wtc setBorder(int row, int col){
        return getTc(row, col)
        .setLeftBorder()
        .setRightBorder()
        .setTopBorder()
        .setBottomBorder()
        .setTl2brBorder()
        .setTr2blBorder();
    }
    public Wtc setBorder(int row, int col, int size, String color, String style){
        return getTc(row, col).setBorder(size, color, style);
    }
    public Wtc setLeftBorder(int row, int col){
        return getTc(row, col).setLeftBorder();
    }
    public Wtc setRightBorder(int row, int col){
        return getTc(row, col).setRightBorder();
    }
    public Wtc setTopBorder(int row, int col){
        return getTc(row, col).setTopBorder();
    }
    public Wtc setBottomBorder(int row, int col){
        return getTc(row, col).setBottomBorder();
    }
    public Wtc setTl2brBorder(int row, int col){
        return getTc(row, col).setTl2brBorder();
    }
    public Wtc setTl2brBorder(int row, int col, String top, String bottom){
        return getTc(row, col).setTl2brBorder(top, bottom);
    }
    public Wtc setTr2blBorder(int row, int col){
        return getTc(row, col).setTr2blBorder();
    }

    public Wtc setTr2blBorder(int row, int col, String top, String bottom){
        return getTc(row, col).setTr2blBorder(top, bottom);
    }

    public Wtc setLeftBorder(int row, int col, int size, String color, String style){
        return getTc(row, col).setLeftBorder(size, color, style);
    }
    public Wtc setRightBorder(int row, int col, int size, String color, String style){
        return getTc(row, col).setRightBorder(size, color, style);
    }
    public Wtc setTopBorder(int row, int col, int size, String color, String style){
        return getTc(row, col).setTopBorder(size, color, style);
    }
    public Wtc setBottomBorder(int row, int col, int size, String color, String style){
        return getTc(row, col).setBottomBorder(size, color, style);
    }
    public Wtc setTl2brBorder(int row, int col, int size, String color, String style){
        return getTc(row, col).setTl2brBorder(size, color, style);
    }
    public Wtc setTr2blBorder(int row, int col, int size, String color, String style){
        return getTc(row, col).setTr2blBorder(size, color, style);
    }


    /**
     * 设置所有行指定列的左边框
     * @param cols 列
     * @param size 边框宽度
     * @param color 颜色
     * @param style 样式
     * @return table table
     */
    public Wtable setLeftBorder(int cols, int size, String color, String style){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setLeftBorder(size, color, style);
        }
        return this;
    }
    /**
     * 设置所有行指定列的右边框
     * @param cols 列
     * @param size 边框宽度
     * @param color 颜色
     * @param style 样式
     * @return table table
     */
    public Wtable setRightBorder(int cols, int size, String color, String style){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setRightBorder(size, color, style);
        }
        return this;
    }

    /**
     * 设置整行所有单元格上边框
     * @param rows 行
     * @param size 边框宽度
     * @param color 颜色
     * @param style 样式
     * @return tr
     */
    public Wtr setTopBorder(int rows, int size, String color, String style){
        return getTr(rows).setTopBorder(size, color, style);
    }
    /**
     * 设置整行所有单元格下边框
     * @param rows 行
     * @param size 边框宽度
     * @param color 颜色
     * @param style 样式
     * @return tr
     */
    public Wtr setBottomBorder(int rows,int size, String color, String style){
        return getTr(rows).setBottomBorder(size, color, style);
    }





    public Wtc setColor(int row, int col, String color){
        return getTc(row, col).setColor(color);
    }

    /**
     * 设置整行颜色
     * @param rows 行
     * @param color 颜色
     * @return wtr
     */
    public Wtr setColor(int rows, String color){
        Wtr tr = getTr(rows);
        tr.setColor(color);
        return tr;
    }
    /**
     * 设置单元格 字体
     * @param row 行
     * @param col 列
     * @param size 字号
     * @param eastAsia 中文字体
     * @param ascii 西文字体
     * @param hint 默认字体
     * @return wtc
     */
    public Wtc setFont(int row, int col, String size, String eastAsia, String ascii, String hint){
        return getTc(row, col).setFont(size, eastAsia, ascii, hint);
    }

    /**
     * 设置整行 字体
     * @param row 行
     * @param size 字号
     * @param eastAsia 中文字体
     * @param ascii 西文字体
     * @param hint 默认字体
     * @return wtr
     */
    public Wtr setFont(int row, String size, String eastAsia, String ascii, String hint){
        Wtr tr = getTr(row);
        tr.setFont(size, eastAsia, ascii, hint);
        return tr;
    }

    /**
     * 设置单元格字号
     * @param row 行
     * @param col 列
     * @param size 字号
     * @return wtc
     */
    public Wtc setFontSize(int row, int col, String size){
        return getTc(row, col).setFontSize(size);
    }
    /**
     * 设置整行字号
     * @param rows 行
     * @param size 字号
     * @return wtr
     */
    public Wtr setFontSize(int rows, String size){
        Wtr tr = getTr(rows);
        tr.setFontSize(size);
        return tr;
    }

    /**
     * 设置单元格字体
     * @param row 行
     * @param col 列
     * @param font 字体
     * @return wtc
     */
    public Wtc setFontFamily(int row, int col, String font){
        return getTc(row, col).setFontFamily(font);
    }

    /**
     * 设置整行字体
     * @param rows 行
     * @param font 字体
     * @return wtr
     */
    public Wtr setFontFamily(int rows, String font){
        Wtr tr = getTr(rows);
        tr.setFontFamily(font);
        return tr;
    }
    public Wtc setWidth(int row, int col, String width){
        return getTc(row, col).setWidth(width);
    }
    public Wtc setWidth(int row, int col, int width){
        return getTc(row, col).setWidth(width);
    }

    public Wtc setWidth(int row, int col, double width){
        return getTc(row, col).setWidth(width);
    }

    /**
     * 设置整列宽度
     * @param cols 列
     * @param width 宽度
     * @return table table
     */
    public Wtable setWidth(int cols, String width){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setWidth(width);
        }
        return this;
    }
    public Wtable setWidth(int cols, int width){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setWidth(width);
        }
        return this;
    }
    public Wtable setWidth(int cols, double width){
        for(Wtr tr:wtrs){
            tr.getTc(cols).setWidth(width);
        }
        return this;
    }
    public Wtr setHeight(int rows, String height){
        Wtr tr = getTr(rows);
        tr.setHeight(height);
        return tr;
    }

    public Wtr setHeight(int rows, int height){
        return setHeight(rows, height+widthUnit);
    }
    public Wtr setHeight(int rows, double height){
        return setHeight(rows, height+widthUnit);
    }

    /**
     * 设置单元格内容水平对齐方式
     * @param row 行
     * @param col 列
     * @param align 对齐方式
     * @return wtc
     */
    public Wtc setAlign(int row, int col, String align){
        return getTc(row, col).setAlign(align);
    }
    /**
     * 设置整行单元格内容水平对齐方式
     * @param rows 行
     * @param align 对齐方式
     * @return wtcr
     */
    public Wtr setAlign(int rows, String align){
        Wtr tr = getTr(rows);
        tr.setAlign(align);
        return tr;
    }

    /**
     * 设置整个表格单元格内容水平对齐方式
     * @param align 对齐方式
     * @return wtable
     */
    public Wtable setAlign(String align){
        for(Wtr tr:wtrs) {
            tr.setAlign(align);
        }
        return this;
    }
    /**
     * 设置单元格内容垂直对齐方式
     * @param row 行
     * @param col 列
     * @param align 对齐方式
     * @return wtc
     */
    public Wtc setVerticalAlign(int row, int col, String align){
        return getTc(row, col).setVerticalAlign(align);
    }

    /**
     * 设置整行单元格内容垂直对齐方式
     * @param rows 行
     * @param align 对齐方式
     * @return wtr
     */
    public Wtr setVerticalAlign(int rows, String align){
        Wtr tr = getTr(rows);
        tr.setVerticalAlign(align);
        return tr;
    }

    /**
     * 设置整个表格单元格内容垂直对齐方式
     * @param align 对齐方式
     * @return wtable
     */
    public Wtable setVerticalAlign(String align){
        for(Wtr tr:wtrs) {
            tr.setVerticalAlign(align);
        }
        return this;
    }
    /**
     * 设置单元格下边距
     * @param row 行
     * @param col 列
     * @param padding 边距 可以指定单位,如:10px
     * @return wtc
     */
    public Wtc setBottomPadding(int row, int col, String padding){
        return getTc(row, col).setBottomPadding(padding);
    }
    /**
     * 设置单元格下边距
     * @param row 行
     * @param col 列
     * @param padding 边距 默认单位dxa
     * @return wtc
     */
    public Wtc setBottomPadding(int row, int col, int padding){
        return getTc(row, col).setBottomPadding(padding);
    }
    public Wtc setBottomPadding(int row, int col, double padding){
        return getTc(row, col).setBottomPadding(padding);
    }


    /**
     * 设置整行单元格下边距
     * @param rows 行
     * @param padding 边距 可以指定单位,如:10px
     * @return wtr
     */
    public Wtr setBottomPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setBottomPadding(padding);
        return tr;
    }
    public Wtr setBottomPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setBottomPadding(padding);
        return tr;
    }
    public Wtr setBottomPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setBottomPadding(padding);
        return tr;
    }
    /**
     * 设置整个表格中所有单元格下边距
     * @param padding 边距 可以指定单位,如:10px
     * @return wtable
     */
    public Wtable setBottomPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setBottomPadding(padding);
        }
        return this;
    }
    public Wtable setBottomPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setBottomPadding(padding);
        }
        return this;
    }
    public Wtable setBottomPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setBottomPadding(padding);
        }
        return this;
    }

    public Wtc setTopPadding(int row, int col, String padding){
        return getTc(row, col).setTopPadding(padding);
    }
    public Wtc setTopPadding(int row, int col, int padding){
        return getTc(row, col).setTopPadding(padding);
    }
    public Wtc setTopPadding(int row, int col, double padding){
        return getTc(row, col).setTopPadding(padding);
    }

    public Wtr setTopPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setTopPadding(padding);
        return tr;
    }
    public Wtr setTopPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setTopPadding(padding);
        return tr;
    }
    public Wtr setTopPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setTopPadding(padding);
        return tr;
    }
    public Wtable setTopPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setTopPadding(padding);
        }
        return this;
    }
    public Wtable setTopPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setTopPadding(padding);
        }
        return this;
    }
    public Wtable setTopPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setTopPadding(padding);
        }
        return this;
    }
    public Wtc setRightPadding(int row, int col, String padding){
        return getTc(row, col).setRightPadding(padding);
    }
    public Wtc setRightPadding(int row, int col, int padding){
        return getTc(row, col).setRightPadding(padding);
    }
    public Wtc setRightPadding(int row, int col, double padding){
        return getTc(row, col).setRightPadding(padding);
    }

    public Wtr setRightPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setRightPadding(padding);
        return tr;
    }
    public Wtr setRightPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setRightPadding(padding);
        return tr;
    }
    public Wtr setRightPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setRightPadding(padding);
        return tr;
    }
    public Wtable setRightPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setRightPadding(padding);
        }
        return this;
    }
    public Wtable setRightPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setRightPadding(padding);
        }
        return this;
    }
    public Wtable setRightPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setRightPadding(padding);
        }
        return this;
    }


    public Wtc setLeftPadding(int row, int col, String padding){
        return getTc(row, col).setLeftPadding(padding);
    }
    public Wtc setLeftPadding(int row, int col, int padding){
        return getTc(row, col).setLeftPadding(padding);
    }
    public Wtc setLeftPadding(int row, int col, double padding){
        return getTc(row, col).setLeftPadding(padding);
    }

    public Wtr setLeftPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setLeftPadding(padding);
        return tr;
    }
    public Wtr setLeftPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setLeftPadding(padding);
        return tr;
    }
    public Wtr setLeftPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setLeftPadding(padding);
        return tr;
    }

    public Wtable setLeftPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setLeftPadding(padding);
        }
        return this;
    }
    public Wtable setLeftPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setLeftPadding(padding);
        }
        return this;
    }
    public Wtable setLeftPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setLeftPadding(padding);
        }
        return this;
    }



    public Wtc setPadding(int row, int col, String side, String padding){
        return getTc(row, col).setPadding(side, padding);
    }
    public Wtc setPadding(int row, int col, String side, int padding){
        return getTc(row, col).setPadding(side, padding);
    }
    public Wtc setPadding(int row, int col, String side, double padding){
        return getTc(row, col).setPadding(side, padding);
    }
    public Wtr setPadding(int rows, String side, String padding){
        Wtr tr = getTr(rows);
        tr.setPadding(side, padding);
        return tr;
    }
    public Wtr setPadding(int rows, String side, int padding){
        Wtr tr = getTr(rows);
        tr.setPadding(side, padding);
        return tr;
    }
    public Wtr setPadding(int rows, String side, double padding){
        Wtr tr = getTr(rows);
        tr.setPadding(side, padding);
        return tr;
    }
    public Wtable setPadding(String side, String padding){
        for(Wtr tr:wtrs){
            tr.setPadding(side, padding);
        }
        return this;
    }

    public Wtable setPadding(String side, int padding){
        for(Wtr tr:wtrs){
            tr.setPadding(side, padding);
        }
        return this;
    }

    public Wtable setPadding(String side, double padding){
        for(Wtr tr:wtrs){
            tr.setPadding(side, padding);
        }
        return this;
    }



    public Wtc setPadding(int row, int col, String padding){
        return getTc(row, col).setPadding(padding);
    }
    public Wtc setPadding(int row, int col, int padding){
        return getTc(row, col).setPadding(padding);
    }
    public Wtc setPadding(int row, int col, double padding){
        return getTc(row, col).setPadding(padding);
    }
    public Wtr setPadding(int rows, String padding){
        Wtr tr = getTr(rows);
        tr.setPadding(padding);
        return tr;
    }
    public Wtr setPadding(int rows, int padding){
        Wtr tr = getTr(rows);
        tr.setPadding(padding);
        return tr;
    }
    public Wtr setPadding(int rows, double padding){
        Wtr tr = getTr(rows);
        tr.setPadding(padding);
        return tr;
    }
    public Wtable setPadding(String padding){
        for(Wtr tr:wtrs){
            tr.setPadding(padding);
        }
        return this;
    }

    public Wtable setPadding(int padding){
        for(Wtr tr:wtrs){
            tr.setPadding(padding);
        }
        return this;
    }

    public Wtable setPadding(double padding){
        for(Wtr tr:wtrs){
            tr.setPadding(padding);
        }
        return this;
    }


    /**
     * 设置单元格背景色
     * @param row 行
     * @param col 列
     * @param color 颜色
     * @return Wtc
     */
    public Wtc setBackgroundColor(int row, int col, String color){
        return getTc(row, col).setBackgroundColor(color);
    }

    /**
     * 设置整行单元格背景色
     * @param row 行
     * @param color 颜色
     * @return Wtr
     */
    public Wtr setBackgroundColor(int row, String color){
        Wtr tr = getTr(row);
        tr.setBackgroundColor(color);
        return tr;
    }

    public Wtable setBackgroundColor(String color){
        for(Wtr tr:wtrs){
            tr.setBackgroundColor(color);
        }
        return this;
    }

    /**
     * 清除单元格样式
     * @param row 行
     * @param col 列
     * @return Wtc
     */
    public Wtc removeStyle(int row, int col){
        return getTc(row, col).removeStyle();
    }
    /**
     * 清除整行单元格样式
     * @param row 行
     * @return Wtr
     */
    public Wtr removeStyle(int row){
        Wtr tr = getTr(row);
        tr.removeContent();
        return tr;
    }
    public Wtable removeStyle(){
        for(Wtr tr:wtrs){
            tr.removeStyle();
        }
        return this;
    }
    /**
     * 清除单元格背景色
     * @param row 行
     * @param col 列
     * @return Wtc
     */
    public Wtc removeBackgroundColor(int row, int col){
        return getTc(row, col).removeBackgroundColor();
    }

    /**
     * 清除整行单元格背景色
     * @param row 行
     * @return Wtr
     */
    public Wtr removeBackgroundColor(int row){
        Wtr tr = getTr(row);
        tr.removeBackgroundColor();
        return tr;
    }
    public Wtable removeBackgroundColor(){
        for(Wtr tr:wtrs){
            tr.removeBackgroundColor();
        }
        return this;
    }

    /**
     * 清除单元格颜色
     * @param row 行
     * @param col 列
     * @return Wtc
     */
    public Wtc removeColor(int row, int col){
        return getTc(row, col).removeColor();
    }
    /**
     * 清除整行单元格颜色
     * @param row 行
     * @return Wtr
     */
    public Wtr removeColor(int row){
        Wtr tr = getTr(row);
        tr.removeColor();
        return tr;
    }
    public Wtable removeColor(){
        for(Wtr tr:wtrs){
            tr.removeColor();
        }
        return this;
    }
    /**
     * 粗体
     * @param row 行
     * @param col 列
     * @param bold 是否
     * @return Wtc
     */
    public Wtc setBold(int row, int col, boolean bold){
        return getTc(row, col).setBold(bold);
    }
    public Wtc setBold(int row, int col){
        return setBold(row, col, true);
    }
    public Wtr setBold(int rows){
        return setBold(rows, true);
    }
    public Wtr setBold(int rows, boolean bold){
        Wtr tr = getTr(rows);
        tr.setBold(bold);
        return tr;
    }
    public Wtable setBold(boolean bold){
        for(Wtr tr:wtrs){
            tr.setBold(bold);
        }
        return this;
    }
    public Wtable setBold(){
        return setBold(true);
    }

    /**
     * 下划线
     * @param row 行
     * @param col 列
     * @param underline 是否
     * @return Wtc
     */
    public Wtc setUnderline(int row, int col, boolean underline){
        return getTc(row, col).setUnderline(underline);
    }
    public Wtc setUnderline(int row, int col){
        return setUnderline(row, col, true);
    }

    /**
     * 删除线
     * @param row 行
     * @param col 列
     * @param strike 是否
     * @return Wtc
     */
    public Wtc setStrike(int row, int col, boolean strike){
        return getTc(row, col).setStrike(strike);
    }
    public Wtc setStrike(int row, int col){
        return setStrike(row, col, true);
    }
    public Wtr setStrike(int rows, boolean strike){
        Wtr tr = getTr(rows);
        tr.setStrike(strike);
        return tr;
    }
    public Wtable setStrike(boolean strike){
        for(Wtr tr:wtrs){
            tr.setStrike(strike);
        }
        return this;
    }
    public Wtable setStrike(){
        return setStrike(true);
    }

    /**
     * 斜体
     * @param row 行
     * @param col 列
     * @param italic 是否
     * @return Wtc
     */
    public Wtc setItalic(int row, int col, boolean italic){
        return getTc(row, col).setItalic(italic);
    }

    public Wtc setItalic(int row, int col){
        return setItalic(row, col, true);
    }

    /**
     * 设置整行斜体
     * @param rows 行
     * @param italic 是否斜体
     * @return wtr
     */
    public Wtr setItalic(int rows,  boolean italic){
        Wtr tr = getTr(rows);
        tr.setItalic(italic);
        return tr;
    }
    public Wtable setItalic(boolean italic){
        for(Wtr tr:wtrs){
            tr.setItalic(italic);
        }
        return this;
    }
    public Wtable setItalic(){
        return setItalic(true);
    }

    /**
     * 替换单元格内容
     * @param row 行
     * @param col 行
     * @param src src
     * @param tar tar
     * @return wtc
     */
    public Wtc replace(int row, int col, String src, String tar){
        return getTc(row, col).replace(src, tar);
    }

    /**
     * 替换整行单元格内容
     * @param rows 行
     * @param src src
     * @param tar tar
     * @return wtr
     */
    public Wtr replace(int rows, String src, String tar){
        Wtr tr = getTr(rows);
        tr.replace(src, tar);
        return tr;
    }
    public Wtable replace(String src, String tar){
        for(Wtr tr:wtrs){
            tr.replace(src, tar);
        }
        return this;
    }


    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
        for(Wtr tr:wtrs){
            tr.setWidthUnit(widthUnit);
        }
    }

    public boolean isAutoLoad() {
        return isAutoLoad;
    }

    public void setAutoLoad(boolean autoLoad) {
        isAutoLoad = autoLoad;
    }

    /**
     * 计算下标
     * @param index 下标 从0开始 -1表示最后一行 -2表示倒数第2行
     * @param size 总行数
     * @return 最终下标
     */
    private int index(Integer index, int size){
        if(null == index){
            return 0;
        }
        return BasicUtil.index(index, size);
    }
    public Wtable copy(){
        Element src = this.src.createCopy();
        Wtable wtable = new Wtable(doc, src);
        return wtable;
    }
}
