/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.entity.html;

import org.anyline.util.BeanUtil;
import org.dom4j.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tr {
    private Table table;
    private String clazz;
    private List<Td> tds = new ArrayList<>();
    private Map<String, String> styles = new HashMap();
    private Element src;
    private String widthUnit = "px";     // 默认长度单位 px pt cm/厘米

    public Element getSrc() {
        return src;
    }

    public void setSrc(Element src) {
        this.src = src;
    }

    public List<Td> getTds() {
        return tds;
    }

    public void setTds(List<Td> tds) {
        this.tds = tds;
    }

    public Map<String, String> getStyles() {
        return styles;
    }

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }
    public void addStyle(String key, String value) {
        styles.put(key, value);
    }
    public Td getTd(int index) {
        return tds.get(index);
    }

    /**
     * 根据偏移量 查询右侧tds
     * @param begin index+offset
     * @return List
     */
    public List<Td> getTdsByOffset(int begin) {
        List<Td> list = new ArrayList<>();
        for(Td td:tds) {
            if(td.getOffset()+td.getColIndex() >= begin) {
                list.add(td);
            }
        }
        return list;
    }
    public Tr setTd(int index, Td td) {
        String bg = styles.get("background-color");
        if(null != bg) {
            td.getStyles().put("background-color", bg);
        }
        tds.add(index, td);
        return this;
    }
    public Tr setHeight(int index, String height) {
        styles.put("height", height);
        return this;
    }
    public Tr setHeight(int index, int height) {
        styles.put("height", height+widthUnit);
        return this;
    }
    public Tr setHeight(int index, double height) {
        styles.put("height", height+widthUnit);
        return this;
    }
    public Tr addTd(String ... tds) {
        if(null != tds) {
            for(String td:tds) {
                this.addTd(new Td(td));
            }
        }
        return this;
    }
    public Tr addTd(Td td) {
        tds.add(td);

        String bg = styles.get("background-color");
        if(null != bg) {
            td.getStyles().put("background-color", bg);
        }
        td.setTr(this);
        return this;
    }
    public int index() {
        List<Tr> trs = table.getTrs();
        return trs.indexOf(this);
    }
    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public Table getTable() {
        return table;
    }

    public String getWidthUnit() {
        return widthUnit;
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
        for(Td td:tds) {
            td.setWidthUnit(widthUnit);
        }
    }

    public void setTable(Table table) {
        this.table = table;
    }
    public void build(StringBuilder builder) {
        if(null == builder) {
            builder = new StringBuilder();
        }
        builder.append("<tr");
        if(null != clazz) {
            builder.append(" class='").append(clazz).append("'");
        }
        if (null != styles && !styles.isEmpty()) {
            builder.append(" style='");
            for(String key:styles.keySet()) {
                builder.append(key).append(":").append(styles.get(key)).append(";");
            }
            builder.append("'");
        }
        builder.append(">\n");
        for(Td td:tds) {
            td.build(builder);
            builder.append("\n");
        }
        builder.append("</tr>");
    }
    public String build() {
        StringBuilder builder = new StringBuilder();
        build(builder);
        return builder.toString();
    }
    public Tr createCopy(boolean style, boolean content) {
        Tr copy = new Tr();
        copy.setWidthUnit(widthUnit);
        int offset = 0; // 已追加的偏移
        for(Td td:tds) {
            for(int i= offset; i<td.getOffset(); i++) {
                copy.addTd(new Td());
                offset++;
            }
            copy.addTd(td.createCopy(style, content));
        }
        if(style) {
            copy.setClazz(this.clazz);
            List<String> keys = BeanUtil.getMapKeys(styles);
            for (String key : keys) {
                copy.addStyle(key, styles.get(key));
            }
        }

        return copy;
    }

    public Tr createCopy() {
        return createCopy(true, false);
    }
}
