package org.anyline.util; 
 
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.FileImageOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.imageio.plugins.gif.GIFImageReader;
import com.sun.imageio.plugins.gif.GIFImageReaderSpi;
import com.sun.imageio.plugins.gif.GIFImageWriter;
import com.sun.imageio.plugins.gif.GIFImageWriterSpi;
 
public class GifUtil { 
	private static Logger log = LoggerFactory.getLogger(GifUtil.class); 
    /** 
     * 拆分gif 
     * @param file  file
     * @param dir 解压目录 
     * @return return
     */ 
    public static List<File> splitGif(File file, File dir){ 
    	List<File> files = new ArrayList<File>(); 
    	FileImageInputStream in = null; 
    	FileImageOutputStream out = null; 
    	try { 
    		if(!dir.exists()){ 
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
     * @param delay 播放延迟时间   
     * @param tar  tar
     * @param srcs  srcs
     */ 
    public synchronized static void createGif(int delay, String tar, String ... srcs) { 
    	List<File> list = new ArrayList<File>(); 
    	for(String src:srcs){ 
    		list.add(new File(src)); 
    	} 
    	createGif(delay, new File(tar), list); 
    } 
    public synchronized static void createGif(int delay, String tar, List<String> srcs) { 
    	List<File> list = new ArrayList<File>(); 
    	for(String src:srcs){ 
    		list.add(new File(src)); 
    	} 
    	createGif(delay, new File(tar), list); 
    } 
    public synchronized static void createGif(int delay, File tar, File ... srcs) { 
    	List<File> list = new ArrayList<File>(); 
    	for(File src:srcs){ 
    		list.add(src); 
    	} 
    	createGif(delay, tar, list); 
    } 
    /** 
     *  
     * @param delay 播放延迟时间   
     * @param tar  tar
     * @param srcs  srcs
     */ 
    public synchronized static void createGif(int delay, File tar, List<File> srcs) {   
        try { 
        	long fr = 0; 
        	File dir = tar.getParentFile(); 
        	if(!dir.exists()){ 
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
                e.setDelay(delay); //设置播放的延迟时间   
                src[i] = ImageIO.read(item); // 读入需要播放的jpg文件   
                e.addFrame(src[i]);  //添加到帧中   
                if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
                	log.warn("[合成 gif][第{}/{}帧][gif:{}][源文件:{}][耗时:{}]",i+1,size,tar.getAbsolutePath(), item.getAbsoluteFile(),System.currentTimeMillis()-fr); 
                } 
            }   
            e.finish();   
        } catch (Exception e) { 
            e.printStackTrace();   
        }   
    }   
 
    public synchronized static void createGif(int delay0, int delay1, File tar, List<File> srcs) {   
        try { 
        	long fr = 0; 
        	File dir = tar.getParentFile(); 
        	if(!dir.exists()){ 
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
            	int delay = NumberUtil.random(delay0, delay1); 
                e.setDelay(delay); //设置播放的延迟时间   
                src[i] = ImageIO.read(item); // 读入需要播放的jpg文件   
                e.addFrame(src[i]);  //添加到帧中   
                if(ConfigTable.isDebug() && log.isWarnEnabled()){ 
                	log.warn("[合成 gif][第{}/{}帧][gif:{}][源文件:{}][耗时:{}]",i+1,size,tar.getAbsoluteFile(),item.getAbsoluteFile(),System.currentTimeMillis()-fr); 
                } 
            }   
            e.finish();   
        } catch (Exception e) { 
            e.printStackTrace();   
        }   
    }   
} 
