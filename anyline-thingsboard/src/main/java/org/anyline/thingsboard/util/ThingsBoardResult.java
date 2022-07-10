package org.anyline.thingsboard.util;

import org.anyline.net.HttpResult;

public class ThingsBoardResult extends HttpResult {
    private String timestamp    ;
    private  int pages          ; //总页数
    private int rows            ; //总行数
    private boolean last        ; //是否是最后一页

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public boolean isLast() {
        return last;
    }

    public void setLast(boolean last) {
        this.last = last;
    }
}
