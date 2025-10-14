package org.anyline.data.coze.entity;

import org.anyline.entity.OriginRow;

public class Document extends OriginRow {
    private String id;
    private String name;
    private String url;
    private String biz; //业务代码(如表主键值)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
        super.put("id", id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        super.put("name", name);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
        super.put("url", url);
    }

    public String getBiz() {
        return biz;
    }

    public void setBiz(String biz) {
        this.biz = biz;
        super.put("biz", biz);
    }
}
