package org.anyline.wechat.mp.entity;

import java.util.ArrayList;
import java.util.List;

public class MenuItem {
    private String type;
    private String name;
    private String key;
    private String url;
    private String appid;
    private String pagepath;
    private List<MenuItem> sub_button = new ArrayList<MenuItem>();

    public MenuItem(String type, String name, String key, String url){
        this.type = type;
        this.name = name;
        this.key = key;
        this.url = url;
    }
    public MenuItem addSubButton(MenuItem item){
        sub_button.add(item);
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getPagepath() {
        return pagepath;
    }

    public void setPagepath(String pagepath) {
        this.pagepath = pagepath;
    }

    public List<MenuItem> getSub_button() {
        return sub_button;
    }

    public void setSub_button(List<MenuItem> sub_button) {
        this.sub_button = sub_button;
    }
}