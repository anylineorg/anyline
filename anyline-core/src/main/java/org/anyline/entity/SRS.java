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



package org.anyline.entity;

public enum SRS {
    WGS84LL("大地坐标系", "GPS/国外谷歌"),
    GCJ02LL("国家测绘局坐标系", "国家测绘局制定(国内谷歌/高德/腾讯)"),
    BD09LL("百度坐标系", "百度坐标系"),
    BD09MC( "百度米制坐标系", "百度米制坐标系");

    private final String title;
    private final String remark;
    SRS(String title, String remark){
        this.title = title;
        this.remark = remark;
    }
    public String title(){
        return title;
    }
    public String remark(){
        return remark;
    }
}
