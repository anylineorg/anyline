/*
 * Copyright 2006-2023 www.anyline.org
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
 */


package org.anyline.office.docx.entity;

import org.anyline.office.docx.util.DocxUtil;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.List;

public class Wp extends Welement{
    private List<Wr> wrs = new ArrayList<>();
    public Wp(WDocument doc, Element src){
        this.root = doc;
        this.src = src;
        load();
    }
    public void reload(){
        load();
    }
    private void load(){
        wrs.clear();
        List<Element> elements = src.elements("r");
        for(Element element:elements){
            Wr wr = new Wr(root, element);
            wrs.add(wr);
        }
    }
    public Wp setColor(String color){
        for(Wr wr:wrs){
            wr.setColor(color);
        }
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "color","val", color.replace("#",""));
        return this;
    }
    public Wp setFont(String size, String eastAsia, String ascii, String hint){

        for(Wr wr:wrs){
            wr.setFont(size, eastAsia, ascii, hint);
        }
        int pt = DocxUtil.fontSize(size);
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "sz","val", pt+"");
        DocxUtil.addElement(pr, "rFonts","eastAsia", eastAsia);
        DocxUtil.addElement(pr, "rFonts","ascii", ascii);
        DocxUtil.addElement(pr, "rFonts","hint", hint);

        return this;
    }
    public Wp setFontSize(String size){
        for(Wr wr:wrs){
            wr.setFontSize(size);
        }
        int pt = DocxUtil.fontSize(size);
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "sz","val", pt+"");
        return this;
    }
    public Wp setFontFamily(String font){
        for(Wr wr:wrs){
            wr.setFontFamily(font);
        }
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "rFonts","eastAsia", font);
        DocxUtil.addElement(pr, "rFonts","ascii", font);
        DocxUtil.addElement(pr, "rFonts","hAnsi", font);
        DocxUtil.addElement(pr, "rFonts","cs", font);
        DocxUtil.addElement(pr, "rFonts","hint", font);
        return this;
    }

    public Wp setAlign(String align){
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "jc","val", align);
        return this;
    }

    public Wp setBackgroundColor(String color){
        Element pr = DocxUtil.addElement(src, "pPr");
        color = color.replace("#","");
        DocxUtil.addElement(pr, "highlight", "val", color);
        for(Wr wr:wrs){
            wr.setBackgroundColor(color);
        }
        return this;
    }

    public Wp setBold(boolean bold){
        Element pr = DocxUtil.addElement(src, "pPr");
        Element b = pr.element("b");
        if(bold){
            if(null == b){
                pr.addElement("w:b");
            }
        }else{
            if(null != b){
                pr.remove(b);
            }
        }
        for(Wr wr:wrs){
            wr.setBold(bold);
        }
        return this;
    }
    public Wp setUnderline(boolean underline){
        Element pr = DocxUtil.addElement(src, "pPr");
        Element u = pr.element("u");
        if(underline){
            if(null == u){
                DocxUtil.addElement(pr, "u", "val", "single");
            }
        }else{
            if(null != u){
                pr.remove(u);
            }
        }
        for(Wr wr:wrs){
            wr.setUnderline(underline);
        }
        return this;
    }
    public Wp setStrike(boolean strike){
        Element pr = DocxUtil.addElement(src, "pPr");
        Element s = pr.element("strike");
        if(strike){
            if(null == s){
                pr.addElement("w:strike");
            }
        }else{
            if(null != s){
                pr.remove(s);
            }
        }
        for(Wr wr:wrs){
            wr.setStrike(strike);
        }
        return this;
    }
    public Wp setItalic(boolean italic){
        Element pr = DocxUtil.addElement(src, "pPr");
        DocxUtil.addElement(pr, "i","val",italic+"");
        for(Wr wr:wrs){
            wr.setItalic(italic);
        }
        return this;
    }

    /**
     * 清除样式
     * @return wp
     */
    public Wp removeStyle(){
        Element pr = src.element("pPr");
        if(null != pr){
            src.remove(pr);
        }
        for(Wr wr:wrs){
            wr.removeStyle();
        }
        return this;
    }
    /**
     * 清除背景色
     * @return wp
     */
    public Wp removeBackgroundColor(){
        DocxUtil.removeElement(src,"shd");
        return this;
    }
    /**
     * 清除颜色
     * @return wp
     */
    public Wp removeColor(){
        DocxUtil.removeElement(src,"color");
        return this;
    }
    public Wp addWr(Wr wr){
        wrs.add(wr);
        return this;
    }
    public Wp replace(String target, String replacement){
        for(Wr wr:wrs){
            wr.replace(target, replacement);
        }
        return this;
    }
}
