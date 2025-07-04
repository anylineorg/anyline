/*
 * Copyright 2006-2025 www.anyline.org
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

package org.anyline.util;

import org.anyline.log.Log;
import org.anyline.log.LogProxy;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;

/** 
 * 图片处理工具类:<br> 
 * 功能:缩放图像、切割图像、图像类型转换、彩色转黑白、文字水印、图片水印等 
 */
public class ImgUtil {
	private static final Log log = LogProxy.get(ImgUtil.class); 
 
	public static enum IMAGE_TYPE{
		GIF{
			public String getName() {return "图形交换格式";}
			public String getCode() {return "gif";}
		} 
		, JPG{
			public String getName() {return "联合照片专家组";}
			public String getCode() {return "jpg";}
		} 
		, JPEG{
			public String getName() {return "联合照片专家组";}
			public String getCode() {return "jpeg";}
		} 
		, BMP{
			public String getName() {return "位图, Windows操作系统中的标准图像文件格式";}
			public String getCode() {return "bmp";}
		} 
		, PNG{
			public String getName() {return "可移植网络图形";}
			public String getCode() {return "png";}
		} 
		, PSD{
			public String getName() {return "Photoshop的专用格式Photoshop";}
			public String getCode() {return "psd";}
		} 
		; 
		public abstract String getName(); 
		public abstract String getCode(); 
	};    
 
 
 
    /** 
     * 缩放图像（按比例缩放） 
     * @param src 源图像文件地址 
     * @param tar 缩放后的图像地址 
     * @param scale 缩放比例 
     */ 
    public static void scale(File src, File tar, float scale) {
        try {
            BufferedImage img = ImageIO.read(src); // 读入文件 
            int width = img.getWidth(); // 得到源图宽 
            int height = img.getHeight(); // 得到源图长 
            width = (int)(width * scale); 
            height = (int)(height * scale); 
            Image image = img.getScaledInstance(width, height, Image.SCALE_DEFAULT);
            BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = tag.getGraphics(); 
            g.drawImage(image, 0, 0, null); // 绘制缩小后的图
            g.dispose(); 
            File dir = tar.getParentFile(); 
            if(null != dir && !dir.exists()) {
            	dir.mkdirs(); 
            } 
            ImageIO.write(tag, "JPEG", tar);// 输出到文件流
        } catch (IOException e) {
            log.error("scale image exception:", e);
        } 
    } 
    /** 
     * 缩放图像（按高度和宽度缩放） 
     * @param src 源图像文件地址 
     * @param tar 缩放后的图像地址 
     * @param format 格式
     * @param height 缩放后的高度 
     * @param width 缩放后的宽度 
     * @param fill 比例不对时是否需要补白:true为补白; false为不补白; 
     */ 
    public static void scale(File src, File tar, String format, int width, int height, boolean fill) {
    	long fr = System.currentTimeMillis(); 
        try {
            double ratio = 0.0; // 缩放比例 
            BufferedImage bi = ImageIO.read(src); 
            Image itemp = bi.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
            // 计算比例 
            if (bi.getHeight() > bi.getWidth()) {
                ratio = (Integer.valueOf(height)).doubleValue() / bi.getHeight(); 
            } else {
                ratio = (Integer.valueOf(width)).doubleValue() / bi.getWidth();
            } 
            AffineTransformOp op = new AffineTransformOp(AffineTransform .getScaleInstance(ratio, ratio), null);
            itemp = op.filter(bi, null);
            if (fill) {//补白 
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics(); 
                g.setColor(Color.white); 
                g.fillRect(0, 0, width, height);
                if (width == itemp.getWidth(null)) {
                    g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
                }else{
                    g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
                } 
                g.dispose(); 
                itemp = image; 
            } 
            File dir = tar.getParentFile(); 
            if(null != dir && !dir.exists()) {
            	dir.mkdirs(); 
            } 
           // ImageIO.write((BufferedImage) itemp, "JPEG", tar);
            ImageIO.write((BufferedImage)itemp, format, tar);
        } catch (IOException e) {
            log.error("scale image exception:", e);
        } 
        log.info("[压缩图片][耗时:{}][source:{}][target:{}]", DateUtil.format(System.currentTimeMillis()-fr), src, tar);
    } 
 
    public static void scale(File src, File tar, String format, int width, int height) {
    	scale(src, tar, format, width, height, false);
    } 
	public static void scale(File src, File tar, int width, int height, boolean fill) {
        String format = "JPEG";
        if(src.getName().toLowerCase().endsWith("png")) {
            format = "PNG";
        }
        scale(src, tar, format, width, height, fill);
	} 
    /** 
     * 图像切割(按指定起点坐标和宽高切割) 
     * @param src 源图像地址 
     * @param tar 切片后的图像地址 
     * @param x 目标切片起点坐标X 
     * @param y 目标切片起点坐标Y 
     * @param w 目标切片宽度 
     * @param h 目标切片高度 
     */ 
 
	public static void cut(File src, File tar, int x, int y, int w, int h) {
		ImageReader reader = null; 
		ImageInputStream iis = null; 
	    try {
	    	String format = "JPEG"; 
	    	if(src.getName().toLowerCase().endsWith("png")) {
	    		format = "PNG"; 
	    	} 
	        Iterator<ImageReader> iterator = ImageIO.getImageReadersByFormatName(format);/*JPEG, PNG, BMP*/
	        reader = iterator.next();/*获取图片尺寸*/
	        iis = ImageIO.createImageInputStream(src);   
	        reader.setInput(iis, true);
	        ImageReadParam param = reader.getDefaultReadParam();   
	        Rectangle rectangle = new Rectangle(x, y, w, h);/*指定截取范围*/
	        param.setSourceRegion(rectangle);   
	        BufferedImage bi = reader.read(0, param);
	        ImageIO.write(bi, format, tar);
	    } catch (Exception e) {
            log.error("cut file exception:", e);
	    }finally{
	    	try {
				iis.close(); 
				reader.dispose(); 
			} catch (IOException e) {
                log.error("close file exception:", e);
			} 
	    } 
	} 
     
    /** 
     * 图像切割（指定切片的行数和列数） 
     * @param src 源图像地址 
     * @param dir 切片目标文件夹 
     * @param rows 目标切片行数.默认2, 必须是范围 [1, 20] 之内
     * @param cols 目标切片列数.默认2, 必须是范围 [1, 20] 之内
     */ 
    public static void split(File src, File dir, int rows, int cols) {
        try {
            String format = "JPEG";
            if(src.getName().toLowerCase().endsWith("png")) {
                format = "PNG";
            }
            if(rows<=0||rows>20) rows = 2; // 切片行数 
            if(cols<=0||cols>20) cols = 2; // 切片列数 
            // 读取源图像 
            BufferedImage bi = ImageIO.read(src); 
            int srcWidth = bi.getHeight(); // 源图宽度 
            int srcHeight = bi.getWidth(); // 源图高度 
            if (srcWidth > 0 && srcHeight > 0) {
                Image img; 
                ImageFilter cropFilter; 
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                int destWidth = srcWidth; // 每张切片的宽度 
                int destHeight = srcHeight; // 每张切片的高度 
                // 计算切片的宽度和高度 
                if (srcWidth % cols == 0) {
                    destWidth = srcWidth / cols; 
                } else {
                    destWidth = (int) Math.floor(srcWidth / cols) + 1; 
                } 
                if (srcHeight % rows == 0) {
                    destHeight = srcHeight / rows; 
                } else {
                    destHeight = (int) Math.floor(srcWidth / rows) + 1; 
                } 
                // 循环建立切片 
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高 
                        // 即: CropImageFilter(int x, int y, int width, int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight, destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth, destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics(); 
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                        g.dispose(); 
                        // 输出为文件 
 
                        if(null != dir && !dir.exists()) {
                        	dir.mkdirs(); 
                        } 
                        ImageIO.write(tag, format, new File(dir, "_r" + i + "_c" + j + "."+format));
                    } 
                } 
            } 
        } catch (Exception e) {
            log.error("cut file exception:", e);
        } 
    } 
    public static float getSizeScale(File file) {
    	float scale = 0; 
		try {
			 BufferedImage bi = ImageIO.read(file); 
			 float w = bi.getHeight(); // 源图宽度 
			 float h = bi.getWidth(); // 源图高度 
			 scale = w/h; 
		} catch (IOException e) {
            log.error("read file exception:", e);
		} 
         return scale; 
    } 
    /** 
     * 图像切割（指定切片的宽度和高度） 
     * @param src 源图像地址 
     * @param dir 切片目标文件夹 
     * @param width 目标切片宽度.默认200 
     * @param height 目标切片高度.默认150 
     */ 
    public final static void split(File src, int width, int height, File dir) {
        try {
            if(width<=0) width = 200; // 切片宽度 
            if(height<=0) height = 150; // 切片高度 
            // 读取源图像 
            BufferedImage bi = ImageIO.read(src); 
            int srcWidth = bi.getHeight(); // 源图宽度 
            int srcHeight = bi.getWidth(); // 源图高度 
            if (srcWidth > width && srcHeight > height) {
                int cols = 0; // 切片横向数量 
                int rows = 0; // 切片纵向数量 
                // 计算切片的横向和纵向数量 
                if (srcWidth % width == 0) {
                    cols = srcWidth / width; 
                } else {
                    cols = (int) Math.floor(srcWidth / width) + 1; 
                } 
                if (srcHeight % height == 0) {
                    rows = srcHeight / height; 
                } else {
                    rows = (int) Math.floor(srcHeight / height) + 1; 
                }
                split(src, dir, rows, cols);
            } 
        } catch (Exception e) {
            log.error("cut img exception:", e);
        } 
    }

    /**
     * 图片合并 竖向
     * @param target 结果文件
     * @param format 结果图片文件格式 PNG
     * @param files 图片文件
     */
    public static void merge(File target, String format, File ... files) throws Exception{
        java.util.List<File> list = new ArrayList<>();
        for(File file:files){
            list.add(file);
        }
        merge(target, format, list);
    }
    public static void merge(File target, String format, java.util.List<File> files) throws Exception{
        java.util.List<BufferedImage> imgs = new ArrayList<>();
        int width = 0;
        int height = 0;
        for(File file:files){
            BufferedImage img = ImageIO.read(file);
            imgs.add(img);
            width = NumberUtil.max(width, img.getWidth());
            height += img.getHeight();
        }
        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // 获取Graphics对象进行绘制
        Graphics g = combined.getGraphics();
        int x = 0;
        int y = 0;
        for(BufferedImage img:imgs){
            g.drawImage(img, x, y, null);
            y += img.getHeight();
        }
        g.dispose(); // 释放资源
        // 保存合并后的图片
        ImageIO.write(combined, format, target);
    }
    /** 
     * 图像类型转换:GIF&gt;JPG、GIF&gt;PNG、PNG&gt;JPG、PNG&gt;GIF(X)、BMP&gt;PNG 
     * @param src 源图像地址 
     * @param format 包含格式非正式名称的 String:如JPG、JPEG、GIF等 
     * @param dest 目标图像地址 
     */ 
    public final static void convert(File src, String format, String dest) {
        try {
            src.canRead(); 
            src.canWrite(); 
            BufferedImage img = ImageIO.read(src); 
            ImageIO.write(img, format, new File(dest));
        } catch (Exception e) {
            log.error("convert exception:", e);
        } 
    } 
 
    /** 
     * 彩色转为黑白  
     * @param src 源图像地址 
     * @param tar 目标图像地址 
     */ 
    public final static void gray(File src, File tar) {
        try {
            String format = "JPEG";
            if(src.getName().toLowerCase().endsWith("png")) {
                format = "PNG";
            }
            BufferedImage img = ImageIO.read(src); 
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY); 
            ColorConvertOp op = new ColorConvertOp(cs, null);
            img = op.filter(img, null);
            ImageIO.write(img, format, tar);
        } catch (IOException e) {
            log.error("gray exception:", e);
        } 
    } 
    /** 
     * 根据图片转换为base64编码字符串 
     * @param img  img
     * @return String
     */ 
    public static String base64Img(File img) {
    	InputStream in = null; 
    	byte[] data = null; 
        try {
            in = new FileInputStream(img); 
            data = new byte[in.available()]; 
            in.read(data); 
        } catch (IOException e) {
            log.error("base642file exception:", e);
        }finally{
        	try{
        		in.close(); 
        	}catch(Exception ignored) {
        	} 
        }

        return new String(Base64.getEncoder().encode(data));
    } 
    public static String base64(File img) {
    	return base64Img(img);
    } 
    public static String base64Img(URL url) {
    	byte[] data = null; 
 
		HttpURLConnection conn = null; 
        InputStream is = null; 
        BufferedInputStream bis = null; 
        ByteArrayOutputStream baos = null; 
    	try{
    		conn = (HttpURLConnection) url.openConnection(); 
	        is = conn.getInputStream(); 
	        bis = new BufferedInputStream(is); 
	        baos = new ByteArrayOutputStream(); 
	        final int BUFFER_SIZE = 1024 * 80;
	        final int EOF = -1; 
	        int c; 
	        byte[] buf = new byte[BUFFER_SIZE]; 
	        while (true) {
	            c = bis.read(buf); 
	            if (c == EOF) {
	                break; 
	            } 
	            baos.write(buf, 0, c);
	        } 
	        data = baos.toByteArray(); 
	        baos.flush(); 
    	}catch(Exception e) {
            log.error("base642file exception:", e);
    	}finally{
    		try{
                assert conn != null;
                conn.disconnect();
                assert is != null;
                is.close();
                assert bis != null;
                bis.close();
                assert baos != null;
                baos.close();
    		}catch(Exception ignored) {
    		} 
    	}

        return Base64.getEncoder().encodeToString(data);
    } 
    public static String base64(URL url) {
    	return base64Img(url); 
    } 
    /** 
     * base64 转 图片文件
     * @param file  file
     * @param str  str
     * @return boolean
     */ 
    public static boolean base64Img(File file, String str) {
    	if (null == str || null == file) return false; 
    	File dir = file.getParentFile(); 
    	if(null != dir && !dir.exists()) {
    		dir.mkdirs(); 
    	} 
    	str = str.replace("data:image/jpeg;base64, ","").replace("data:image/png;base64, ","");
    	OutputStream  out = null; 
    	try {
            Base64.Decoder decoder = Base64.getDecoder();
    		// 解密 
    		byte[] b = decoder.decode(str);
    		// 处理数据 
    		for (int i = 0; i < b.length; ++i) {
    			if (b[i] < 0) {
    				b[i] += 256; 
    			} 
    		} 
    		out = new FileOutputStream(file); 
    		out.write(b); 
    		out.flush(); 
    		out.close(); 
    		return true; 
    	} catch (Exception e) {
            log.error("base642img exception:", e);
    		return false; 
    	} finally{
    		if(null != out) {
    			try {
					out.close(); 
				} catch (IOException e) {
                    log.error("stream close exception:", e);
				} 
    		} 
    	} 
    } 
    public static boolean base64(File file, String str) {
    	return base64Img(file, str);
    }  
    /** 
     * 获取文件宽高 
     * @param img  img
     * @return int
     */ 
	public static int[] size(File img) {
    	int width = -1; 
    	int height = -1; 
    	BufferedImage bi = null; 
		try {
			bi = ImageIO.read(img); 
	        width = bi.getWidth(); // 得到源图宽 
	        height = bi.getHeight(); // 得到源图长 
		} catch (Exception e) {
			e.printStackTrace(); 
		}finally{
			try{
				bi.flush(); 
			}catch(Exception e) {
                log.error("file size exception:", e);
			} 
		} 
        int[] result = {width, height};
    	return result; 
    } 
	public static int width(File img) {
		return size(img)[0]; 
	} 
	public static int height(File img) {
		return size(img)[1]; 
	} 
}
