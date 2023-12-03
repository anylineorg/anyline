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


package org.anyline.util.img;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import com.sun.imageio.plugins.gif.GIFImageWriter;
import com.sun.imageio.plugins.gif.GIFImageWriterSpi;
import org.anyline.util.ConfigTable;
import org.anyline.util.NumberUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GifUtil {
	private static Logger log = LoggerFactory.getLogger(GifUtil.class); 
    /** 
     * 拆分gif 
     * @param file  gif文件
     * @param dir 解压目录 
     * @return List
     */ 
    public static List<File> split(File file, File dir){
    	List<File> files = new ArrayList<>();
    	FileImageInputStream in = null; 
    	FileImageOutputStream out = null; 
    	try {
    		if(null != dir && !dir.exists()){
    			dir.mkdirs(); 
    		} 
    		in = new FileImageInputStream(file); 
			ImageReaderSpi readerSpi = new GIFImageReaderSpi(); 
    		GIFImageReader gifReader = (GIFImageReader) readerSpi.createReaderInstance(); 
    		gifReader.setInput(in); 
    		int size = gifReader.getNumImages(true); 
    		ImageWriterSpi writerSpi = new GIFImageWriterSpi(); 
    		GIFImageWriter writer = (GIFImageWriter) writerSpi.createWriterInstance(); 
    		for (int i = 0; i < size; i++) {
    			File target = new File(dir, file.getName().replace(".gif", "_"+i+".jpg")); 
    			out = new FileImageOutputStream(target); 
    			writer.setOutput(out); 
    			writer.write(gifReader.read(i)); 
    		} 
    	}catch(Exception e){
    		e.printStackTrace(); 
    	} 
    	return files; 
    } 
    /** 
     *  
     * @param delay 播放间隔时间
     * @param tar  生成文件
     * @param srcs  源图片列表
     */ 
    public synchronized static void create(int delay, String tar, String ... srcs) {
    	List<File> list = new ArrayList<>();
    	for(String src:srcs){
    		list.add(new File(src)); 
    	}
		create(delay, new File(tar), list);
    } 
    public synchronized static void create(int delay, String tar, List<String> srcs) {
    	List<File> list = new ArrayList<>();
    	for(String src:srcs){
    		list.add(new File(src)); 
    	}
		create(delay, new File(tar), list);
    } 
    public synchronized static void create(int delay, File tar, File ... srcs) {
    	List<File> list = new ArrayList<>();
		Collections.addAll(list, srcs);
		create(delay, tar, list);
    } 
    /**
	 * @param delay 播放间隔时间(milliseconds)
	 * @param tar  生成文件
	 * @param srcs  源图片列表
     */ 
    public synchronized static void create(int delay, File tar, List<File> srcs) {
        try {
        	long fr = 0; 
        	File dir = tar.getParentFile(); 
        	if(null != dir && !dir.exists()){
        		dir.mkdirs(); 
        	} 
            AnimatedGifEncoder e = new AnimatedGifEncoder();
            e.setRepeat(0);   
            e.start(tar.getAbsolutePath()); 
            int size = srcs.size(); 
            BufferedImage src[] = new BufferedImage[size];   
            for (int i = 0; i < size; i++) {
            	File item = srcs.get(i); 
            	fr = System.currentTimeMillis(); 
                e.setDelay(delay); // 设置播放的延迟时间   
                src[i] = ImageIO.read(item); // 读入需要播放的jpg文件   
                e.addFrame(src[i]);  // 添加到帧中   
                if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                	log.warn("[合成 gif][第{}/{}帧][gif:{}][源文件:{}][耗时:{}]",i+1,size,tar.getAbsolutePath(), item.getAbsoluteFile(),System.currentTimeMillis()-fr); 
                } 
            }   
            e.finish();   
        } catch (Exception e) {
            e.printStackTrace();   
        }   
    }

	/**
	 *
	 * @param min 最小间隔时间(milliseconds)
	 * @param max 最大间隔时间(milliseconds)
	 * @param tar 生成文件
	 * @param srcs 源图片集合
	 */
	public synchronized static void create(int min, int max, File tar, List<File> srcs) {
        try {
        	long fr = 0; 
        	File dir = tar.getParentFile(); 
        	if(null != dir && !dir.exists()){
        		dir.mkdirs(); 
        	} 
            AnimatedGifEncoder e = new AnimatedGifEncoder();  
            e.setRepeat(0);   
            e.start(tar.getAbsolutePath()); 
            int size = srcs.size(); 
            BufferedImage src[] = new BufferedImage[size];   
            for (int i = 0; i < size; i++) {
            	File item = srcs.get(i); 
            	fr = System.currentTimeMillis(); 
            	int delay = NumberUtil.random(min, max);
                e.setDelay(delay); // 设置播放的延迟时间   
                src[i] = ImageIO.read(item); // 读入需要播放的jpg文件   
                e.addFrame(src[i]);  // 添加到帧中   
                if(ConfigTable.IS_DEBUG && log.isWarnEnabled()){
                	log.warn("[合成 gif][第{}/{}帧][gif:{}][源文件:{}][耗时:{}]",i+1,size,tar.getAbsoluteFile(),item.getAbsoluteFile(),System.currentTimeMillis()-fr); 
                } 
            }   
            e.finish();   
        } catch (Exception e) {
            e.printStackTrace();   
        }   
    }
} 
