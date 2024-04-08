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
