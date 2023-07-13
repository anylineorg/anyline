package org.anyline.wechat.mp.entity;

import org.anyline.util.BeanUtil;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private String menuid;
    private MenuMatchrule matchrule;
    private List<MenuItem> button = new ArrayList<MenuItem>();
    public Menu addButton(MenuItem item){

        button.add(item);
        return this;
    }

    public Menu addButton(String type, String name, String key, String url){
        MenuItem item = new MenuItem(type, name, key, url);
        button.add(item);
        return this;
    }

    public MenuMatchrule getMatchrule() {
        return matchrule;
    }

    public String getMenuid() {
        return menuid;
    }

    public void setMenuid(String menuid) {
        this.menuid = menuid;
    }

    public void setMatchrule(MenuMatchrule matchrule) {
        this.matchrule = matchrule;
    }

    public List<MenuItem> getButton() {
        return button;
    }

    public void setButton(List<MenuItem> button) {
        this.button = button;
    }
    public String toJson(){
        return BeanUtil.object2json(this);
    }
}