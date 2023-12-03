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

import org.anyline.util.HtmlUtil;
import org.dom4j.Element;

public class Wt extends Welement {
    public Wt(WDocument doc, Element src){
        this.root = doc;
        this.src = src;
    }
    public Wt setText(String text){
        if(root.IS_HTML_ESCAPE) {
            text = HtmlUtil.display(text);
        }
        src.setText(text);
        return this;
    }
    public String getText(){
        return src.getText();
    }
}
