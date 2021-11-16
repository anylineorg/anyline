package org.anyline.util;

import org.dom4j.Element;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Dom4jUtil {
    /**
     * 根据标签name搜索element
     * @param root 根节点
     * @param tag 标签名(不含namespace)
     * @return Element
     */
    public static Element element(Element root,String tag){
        Element result = null;
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext() && null == result){
            Element e= it.next();
            if (e.getName().equals(tag)){
                result = e;
                break;
            }else{
                result = element(e, tag);
            }
        }
        return result;
    }
    /**
     * 根据标签name搜索element
     * @param root 根节点
     * @param tag 标签名(不含namespace)
     * @return List
     */
    public static List<Element> elements(Element root, String tag){
        List<Element> list = new ArrayList<Element>();
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext()){
            Element e= it.next();
            if (e.getName().equals(tag)){
                list.add(e);
            }
            List<Element> items = elements(e, tag);
            list.addAll(items);
        }
        return list;
    }

    /**
     * 根据标签name以及属性值搜索element
     * @param root 根节点
     * @param tag 标签名(不含namespace)
     * @param attribute 属性名
     * @param value 属性值
     * @return Element
     */
    public static Element element(Element root,String tag, String attribute, String value){
        Element result = null;
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext() && null == result){
            Element e= it.next();
            if (e.getName().equals(tag) && value.equals(e.attributeValue(attribute))){
                result = e;
                break;
            }else{
                result = element(e, tag, attribute, value);
            }
        }
        return result;
    }
    /**
     * 根据标签name以及属性值搜索element
     * @param root 根节点
     * @param tag 标签名(不含namespace)
     * @param attribute 属性名
     * @param value 属性值
     * @return List
     */
    public static List<Element> elements(Element root, String tag, String attribute, String value){
        List<Element> list = new ArrayList<Element>();
        Iterator<Element> it = root.elementIterator();
        while(it.hasNext()){
            Element e= it.next();
            if (e.getName().equals(tag) && value.equals(e.attributeValue(attribute))){
                list.add(e);
            }
            List<Element> items = elements(e, tag, attribute, value);
            list.addAll(items);
        }
        return list;
    }

    /**
     * 根据属性值搜索element
     * @param root 根节点
     * @param attribute 属性名
     * @param value 属性值
     * @return Element
     */
    public static Element element(Element root,String attribute, String value){
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
     * @param attribute 属性名
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
}
