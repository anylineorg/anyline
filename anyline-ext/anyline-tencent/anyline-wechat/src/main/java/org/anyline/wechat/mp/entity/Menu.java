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