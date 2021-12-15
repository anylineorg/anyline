package org.anyline.office.docx.entity;

import org.anyline.entity.DataSet;
import org.anyline.office.docx.entity.data.TableBuilder;
import org.anyline.office.docx.util.DocxUtil;
import org.anyline.util.DomUtil;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class WTable {
    private WDocument doc;
    private Element src;
    public WTable(WDocument doc){
        this.doc = doc;
    }
    public WTable(WDocument doc, Element src){
        this.doc = doc;
        this.src = src;
    }
    private Element tr(WTr template, Element src){
        Element tr = null;

        tr = template.getSrc().createCopy();
        List<Element> tds = src.elements("td");
        List<Element> tcs = tr.elements("tc");
        for(int i=0; i<tcs.size(); i++){
            Element tc = tcs.get(i);
            if(i<tds.size()) {
                Element td = tds.get(i);
                //this.doc.block(tc, null, td, null);
                Element t = DomUtil.element(tc,"t");
                if(null == t){
                    t = tc.element("p").addElement("w:r").addElement("w:t");
                }
                String text = td.getTextTrim();
                t.setText(td.getTextTrim());
            }
        }
        return tr;
    }
    private Element tr(Element src){
        Element tr = this.src.addElement("w:tr");
        List<Element> tds = src.elements("td");
        for(int i=0; i<tds.size(); i++){
            Element tc = tr.addElement("w:tc");
            Element td = tds.get(i);
            this.doc.block(tc, null, td, null);
        }
        return tr;
    }

    public void insert(String html){
        insert(-1, html);
    }
    public void insert(Object data, String ... cols){
        insert(-1, null, data, cols);
    }
    public void insert(int index, Object data, String ... cols){
        WTr template = null;
        insert(index, template, data, cols);
    }
    public void insert(WTr template, Object data, String ... cols){
        insert(-1, template, data, cols);
    }
    public void insert(int index, WTr template, Object data, String ... cols){
        Collection datas = null;
        if(data instanceof Collection){
            datas = (Collection)data;
        }else{
            datas = new ArrayList();
            datas.add(data);
        }
        TableBuilder builder = TableBuilder.init().setFields(cols).setDatas(datas);
        String html = builder.build(false);
        insert(index, template, html);
    }
    public void insert(int index, String html){
        List<Element> trs = src.elements("tr");
        WTr template = null;//以最后一行作模板
        if(trs.size() > 1){
            template = new WTr(doc, trs.get(trs.size()-1));
            if(index == -1){
                index = trs.size()-1;
            }
        }
        insert(-1, template, html);
    }
    public void insert(WTr template, String html){
        int index = -1;
        if(null != template) {
            List<Element> trs = src.elements("tr");
            index = trs.indexOf(template.getSrc());
        }
        insert(index,template,  html);
    }
    public void insert(int index,WTr template,  String html){
        List<Element> trs = src.elements("tr");
        if(index == -1 && null != template){
            index = trs.indexOf(template.getSrc());
        }
        try {
            org.dom4j.Document doc = DocumentHelper.parseText("<body>"+html+"</body>");
            Element root = doc.getRootElement();
            List<Element> rows = root.elements("tr");
            for(Element row:rows){
                Element newTr = null;
                if(null != template) {
                    newTr = tr(template, row);
                }else{
                    newTr = tr(row);
                    trs.remove(newTr);
                }
                if(index >= 0) {
                    trs.add(index++, newTr);
                }else{
                    trs.add(newTr);
                }
            }
            if(null != template){
                //trs.remove(template.getSrc());
            }

        }catch (Exception e){
            e.printStackTrace();
        }

    }
    public void remove(int index){
        List<Element> trs = src.elements("tr");
        if(index < trs.size() && index >=0){
            trs.remove(index);
        }
    }
    public void remove(WTr tr){
        List<Element> trs = src.elements("tr");
        trs.remove(tr.getSrc());
    }
    public String getText(int rows, int cols){
        String text = null;
        List<Element> trs = src.elements("tr");
        Element tr = trs.get(rows);
        List<Element> tcs = tr.elements("tc");
        Element tc = tcs.get(cols);
        text = DocxUtil.text(tc);
        return text;
    }
    public WTable setText(int rows, int cols, String text){
        return setText(rows, cols, text, null);
    }
    public WTable setText(int rows, int cols, String text, Map<String,String> styles){
        List<Element> trs = src.elements("tr");
        Element tr = trs.get(rows);
        List<Element> tcs = tr.elements("tc");
        Element tc = tcs.get(cols);
        List<Element> ts = DomUtil.elements(tc,"t");
        for(Element t:ts){
            t.setText("");
        }
        if(ts.size()>0){
            ts.get(0).setText(text);
        }else{
            Element r = DomUtil.element(tc,"r");
            if(null != r){
                r.addElement("w:t").setText(text);
                DocxUtil.pr(r, styles);
            }else{
                Element p = tc.element("p");
                r = p.addElement("w:r");
                Element t = r.addElement("w:t");
                t.setText(text);
                DocxUtil.pr(r, styles);
            }
        }
        return this;
    }
    public WTable addColumns(int qty){
        List<Element> trs = src.elements("tr");
        for(Element tr:trs){
            List<Element> tcs = tr.elements();
            if(tcs.size()>0){
                Element tc = tcs.get(tcs.size()-1);
                for (int i = 0; i < qty; i++) {
                    Element newTc = tc.createCopy();
                    DocxUtil.cleanText(newTc);
                    tr.add(newTc);
                }
            }else {
                for (int i = 0; i < qty; i++) {
                    tr.addElement("w:tc").addElement("w:p");
                }
            }
        }
        return this;
    }
    public WTable addRows(int qty){
        List<Element> trs = src.elements("tr");
        if(trs.size()>0){
            Element tr = trs.get(trs.size()-1);
            for(int i=0; i<qty; i++) {
                trs.add(tr.createCopy());
            }
        }
        return this;
    }
    public int getTrSize(){
        return src.elements("tr").size();
    }
    public List<WTr> getTrs(){
        List<Element> trs = src.elements("tr");
        List<WTr> list = new ArrayList<>();
        for(Element tr:trs){
            WTr wtr = new WTr(doc, tr);
            list.add(wtr);
        }
        return list;
    }

}
