package org.anyline.handler;

import java.io.File;
import java.io.InputStream;

public interface Uploader {
    /**
     * 文件上传
     * @param name 文件名
     * @param file 文件
     * @return 返回文件上传后地址
     */
    default String upload(String name, File file){
        return null;
    }
    default String upload(String name, InputStream is){
        return null;
    }
}
