package org.anyline.handler;

import java.io.File;

public interface Downloader {
    /**
     * 下载文件
     * @param url url
     * @param file 文件
     * @return 返回文件上传后地址
     */
    default boolean download(String url, File file){
        return false;
    }
}
