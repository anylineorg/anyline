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


package org.anyline.util;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DomUtil {

    /**
     * 根据标签name搜索element
     * @param root 根节点
     * @param tags 标签名(不含namespace)
     * @return Element
     */
    public static Element element(Element root,List<String> tags){
        Element result = null;
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext() && null == result){
            Element e= it.next();
            if (tags.contains(e.getName())){
                result = e;
                break;
            }else{
                result = element(e, tags);
            }
        }
        return result;
    }
    /**
     * 根据标签name搜索element
     * @param root 根节点
     * @param tags 标签名(不含namespace)
     * @return Element
     */
    public static Element element(Element root, String tags){
        return element(root, BeanUtil.array2list(tags.split(",")));
    }
    /**
     * 根据标签name搜索element
     * @param root 根节点
     * @param tags 标签名(不含namespace)
     * @param recursion 递归查询子类
     * @return List
     */
    public static List<Element> elements(Element root, List<String> tags, boolean recursion){
        List<Element> list = new ArrayList<Element>();
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext()){
            Element e= it.next();
            if (tags.contains(e.getName())){
                list.add(e);
            }
            if(recursion) {
                List<Element> items = elements(e, tags, recursion);
                list.addAll(items);
            }
        }
        return list;
    }

    public static List<Element> elements(Element root, String tags, boolean recursion){
        return elements(root, BeanUtil.array2list(tags.split(",")), recursion);
    }
    public static List<Element> elements(Element root, List<String> tags){
        return elements(root, tags,true);
    }
    public static List<Element> elements(Element root, String tags){
        return elements(root, tags,true);
    }
    /**
     * 根据标签name以及属性值搜索element
     * @param root 根节点
     * @param tags 标签名(不含namespace)
     * @param attribute 属性名(不含namespace)
     * @param value 属性值
     * @return Element
     */
    public static Element element(Element root,List<String> tags, String attribute, String value){
        Element result = null;
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext() && null == result){
            Element e= it.next();
            if (tags.contains(e.getName()) && value.equals(e.attributeValue(attribute))){
                result = e;
                break;
            }else{
                result = element(e, tags, attribute, value);
            }
        }
        return result;
    }

    public static Element element(Element root, String tags, String attribute, String value){
        return element(root, BeanUtil.array2list(tags.split(",")), attribute, value);
    }
    /**
     * 根据标签name以及属性值搜索element
     * @param root 根节点
     * @param tags 标签名(不含namespace)
     * @param attribute 属性名(不含namespace)
     * @param value 属性值
     * @return List
     */
    public static List<Element> elements(Element root, List<String> tags, String attribute, String value){
        List<Element> list = new ArrayList<Element>();
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext()){
            Element e= it.next();
            if (tags.contains(e.getName()) && value.equals(e.attributeValue(attribute))){
                list.add(e);
            }
            List<Element> items = elements(e, tags, attribute, value);
            list.addAll(items);
        }
        return list;
    }

    public static List<Element> elements(Element root, String tags, String attribute, String value){
        return elements(root, BeanUtil.array2list(tags.split(",")), attribute, value);
    }
    /**
     * 根据属性值搜索element
     * @param root 根节点
     * @param attribute 属性名(不含namespace)
     * @param value 属性值
     * @return Element
     */
    public static Element element(Element root, String attribute, String value){
        Element result = null;
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext() && null == result){
            Element e= it.next();
            if (value.equals(e.attributeValue(attribute))){
                result = e;
                break;
            }else{
                result = element(e, attribute, value);
            }
        }
        return result;
    }
    /**
     * 根据属性值搜索element
     * @param root 根节点
     * @param attribute 属性名(不含namespace)
     * @param value 属性值
     * @return List
     */
    public static List<Element> elements(Element root, String attribute, String value){
        List<Element> list = new ArrayList<Element>();
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext()){
            Element e= it.next();
            if (value.equals(e.attributeValue(attribute))){
                list.add(e);
            }
            List<Element> items = elements(e,  attribute, value);
            list.addAll(items);
        }
        return list;
    }

    public static String format(String xml){
        String result = null;
        XMLWriter writer = null;
        try {
            Document document = DocumentHelper.parseText(xml);
            result = format(document);
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }
    public static String format(Document document){
        String result = null;
        XMLWriter writer = null;
        try {
            StringWriter stringWriter = new StringWriter();
            OutputFormat format = new OutputFormat("\t", true);
            writer = new XMLWriter(stringWriter, format);
            writer.write(document);
            writer.flush();
            result = stringWriter.getBuffer().toString();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                }
            }
        }
        return result;
    }

    /**
     * 当前节点后的所有节点
     * @param element element
     * @param tag 过滤标签
     * @return List
     */
    public static List<Element> afters(Element element, String tag){
        List<Element> list = new ArrayList<>();
        List<Element> elements = element.getParent().elements();
        int index = elements.indexOf(element);
        for(int i=index+1; i<elements.size(); i++){
            Element item = elements.get(i);
            if(item.getName().equalsIgnoreCase(tag)) {
                list.add(item);
            }
        }
        return list;
    }
    /**
     * 当前节点前的所有节点
     * @param element element
     * @param tag 过滤标签
     * @return List
     */
    public static List<Element> befores(Element element, String tag){
        List<Element> list = new ArrayList<>();
        List<Element> elements = element.getParent().elements();
        int index = elements.indexOf(element);
        for(int i=elements.size()-1; i>index; i--){
            Element item = elements.get(i);
            if(item.getName().equalsIgnoreCase(tag)) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * start与end之间的所有节点
     * @param start 开始
     * @param end 结束
     * @param tags 过滤
     * @return List
     */
    public static List<Element> betweens(Element start,Element end, String ... tags){
        List<Element> list = new ArrayList<>();
        List<Element> elements = start.getParent().elements();
        int fr = elements.indexOf(start);
        int to = elements.indexOf(end);
        int index = elements.indexOf(start);
        for(int i=fr+1; i<to; i++){
            Element item = elements.get(i);
            if(null == tags || tags.length ==0 || BasicUtil.contains(tags, item.getName())) {
                list.add(item);
            }
        }
        return list;
    }

    /**
     * 删除parent下的removes节点
     * @param parent parent
     * @param removes removes
     */
    public static void remove(Element parent, List<Element> removes){
        List<Element> elements = parent.elements();
        for(Element remove:removes){
            elements.remove(remove);
        }
    }


}
