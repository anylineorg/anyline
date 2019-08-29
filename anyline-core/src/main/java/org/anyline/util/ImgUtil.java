package org.anyline.util;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

/**
 * 图片处理工具类：<br>
 * 功能：缩放图像、切割图像、图像类型转换、彩色转黑白、文字水印、图片水印等
 */
@SuppressWarnings("restriction")
public class ImgUtil {
	private static final Logger log = Logger.getLogger(ImgUtil.class);

	public static enum IMAGE_TYPE{
		GIF{
			public String getName(){return "图形交换格式";}
			public String getCode(){return "gif";}
		}
		,JPG{
			public String getName(){return "联合照片专家组";}
			public String getCode(){return "jpg";}
		}
		,JPEG{
			public String getName(){return "联合照片专家组";}
			public String getCode(){return "jpeg";}
		}
		,BMP{
			public String getName(){return "位图,Windows操作系统中的标准图像文件格式";}
			public String getCode(){return "bmp";}
		}
		,PNG{
			public String getName(){return "可移植网络图形";}
			public String getCode(){return "png";}
		}
		,PSD{
			public String getName(){return "Photoshop的专用格式Photoshop";}
			public String getCode(){return "psd";}
		}
		;
		public abstract String getName();
		public abstract String getCode();
	};   



    /**
     * 缩放图像（按比例缩放）
     * @param srcImageFile 源图像文件地址
     * @param result 缩放后的图像地址
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
            if(!dir.exists()){
            	dir.mkdirs();
            }
            ImageIO.write(tag, "JPEG", tar);// 输出到文件流
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 缩放图像（按高度和宽度缩放）
     * @param srcImageFile 源图像文件地址
     * @param result 缩放后的图像地址
     * @param height 缩放后的高度
     * @param width 缩放后的宽度
     * @param fill 比例不对时是否需要补白：true为补白; false为不补白;
     */
    public static void scale(File src, File tar, int width, int height, boolean fill) {
    	long fr = System.currentTimeMillis();
        try {
            double ratio = 0.0; // 缩放比例
            BufferedImage bi = ImageIO.read(src);
            Image itemp = bi.getScaledInstance(width, height, BufferedImage.SCALE_SMOOTH);
            // 计算比例
            if ((bi.getHeight() > height) || (bi.getWidth() > width)) {
                if (bi.getHeight() > bi.getWidth()) {
                    ratio = (new Integer(height)).doubleValue() / bi.getHeight();
                } else {
                    ratio = (new Integer(width)).doubleValue() / bi.getWidth();
                }
                AffineTransformOp op = new AffineTransformOp(AffineTransform .getScaleInstance(ratio, ratio), null);
                itemp = op.filter(bi, null);
            }
            if (fill) {//补白
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics2D g = image.createGraphics();
                g.setColor(Color.white);
                g.fillRect(0, 0, width, height);
                if (width == itemp.getWidth(null)){
                    g.drawImage(itemp, 0, (height - itemp.getHeight(null)) / 2, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
                }else{
                    g.drawImage(itemp, (width - itemp.getWidth(null)) / 2, 0, itemp.getWidth(null), itemp.getHeight(null), Color.white, null);
                }
                g.dispose();
                itemp = image;
            }
            File dir = tar.getParentFile();
            if(!dir.exists()){
            	dir.mkdirs();
            }
            ImageIO.write((BufferedImage) itemp, "JPEG", tar);
        } catch (IOException e) {
            e.printStackTrace();
        }
        log.warn("[压缩图片][耗时:"+(System.currentTimeMillis()-fr)+"][source:"+src+"][target:"+tar+"]");
    }
    
    /**
     * 图像切割(按指定起点坐标和宽高切割)
     * @param srcImageFile 源图像地址
     * @param result 切片后的图像地址
     * @param x 目标切片起点坐标X
     * @param y 目标切片起点坐标Y
     * @param width 目标切片宽度
     * @param height 目标切片高度
     */
    public static void cut(File src, File tar, int x, int y, int width, int height) {
        try {
            // 读取源图像
            BufferedImage bi = ImageIO.read(src);
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > 0 && srcHeight > 0) {
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
                // 四个参数分别为图像起点坐标和宽高
                // 即: CropImageFilter(int x,int y,int width,int height)
                ImageFilter cropFilter = new CropImageFilter(x, y, width, height);
                Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
                BufferedImage tag = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                Graphics g = tag.getGraphics();
                g.drawImage(img, 0, 0, width, height, null); // 绘制切割后的图
                g.dispose();
                // 输出为文件

                File dir = tar.getParentFile();
                if(!dir.exists()){
                	dir.mkdirs();
                }
                ImageIO.write(tag, "JPEG", tar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * 图像切割（指定切片的行数和列数）
     * @param srcImageFile 源图像地址
     * @param descDir 切片目标文件夹
     * @param rows 目标切片行数。默认2，必须是范围 [1, 20] 之内
     * @param cols 目标切片列数。默认2，必须是范围 [1, 20] 之内
     */
    public static void cut(File src, File dir, int rows, int cols) {
        try {
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
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * destWidth, i * destHeight, destWidth, destHeight);
                        img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), cropFilter));
                        BufferedImage tag = new BufferedImage(destWidth,  destHeight, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                        g.dispose();
                        // 输出为文件

                        if(!dir.exists()){
                        	dir.mkdirs();
                        }
                        ImageIO.write(tag, "JPEG", new File(dir,  "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static float getSizeScale(File file){
    	float scale = 0;
		try {
			 BufferedImage bi = ImageIO.read(file);
			 float w = bi.getHeight(); // 源图宽度
			 float h = bi.getWidth(); // 源图高度
			 scale = w/h;
		} catch (IOException e) {
			e.printStackTrace();
		}
         return scale;
    }
    /**
     * 图像切割（指定切片的宽度和高度）
     * @param srcImageFile 源图像地址
     * @param descDir 切片目标文件夹
     * @param destWidth 目标切片宽度。默认200
     * @param destHeight 目标切片高度。默认150
     */
    public final static void cut3(File src, File dir, int width, int height) {
        try {
            if(width<=0) width = 200; // 切片宽度
            if(height<=0) height = 150; // 切片高度
            // 读取源图像
            BufferedImage bi = ImageIO.read(src);
            int srcWidth = bi.getHeight(); // 源图宽度
            int srcHeight = bi.getWidth(); // 源图高度
            if (srcWidth > width && srcHeight > height) {
                Image img;
                ImageFilter cropFilter;
                Image image = bi.getScaledInstance(srcWidth, srcHeight, Image.SCALE_DEFAULT);
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
                // 循环建立切片
                // 改进的想法:是否可用多线程加快切割速度
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        // 四个参数分别为图像起点坐标和宽高
                        // 即: CropImageFilter(int x,int y,int width,int height)
                        cropFilter = new CropImageFilter(j * width, i * height,
                                width, height);
                        img = Toolkit.getDefaultToolkit().createImage(
                                new FilteredImageSource(image.getSource(),
                                        cropFilter));
                        BufferedImage tag = new BufferedImage(width,
                                height, BufferedImage.TYPE_INT_RGB);
                        Graphics g = tag.getGraphics();
                        g.drawImage(img, 0, 0, null); // 绘制缩小后的图
                        g.dispose();

                        if(!dir.exists()){
                        	dir.mkdirs();
                        }
                        // 输出为文件
                        ImageIO.write(tag, "JPEG", new File(dir, "_r" + i + "_c" + j + ".jpg"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 图像类型转换：GIF->JPG、GIF->PNG、PNG->JPG、PNG->GIF(X)、BMP->PNG
     * @param srcImageFile 源图像地址
     * @param formatName 包含格式非正式名称的 String：如JPG、JPEG、GIF等
     * @param dest 目标图像地址
     */
    public final static void convert(File src, String format, String dest) {
        try {
            src.canRead();
            src.canWrite();
            BufferedImage img = ImageIO.read(src);
            ImageIO.write(img, format, new File(dest));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 彩色转为黑白 
     * @param srcImageFile 源图像地址
     * @param destImageFile 目标图像地址
     */
    public final static void gray(File src, File tar) {
        try {
            BufferedImage img = ImageIO.read(src);
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
            ColorConvertOp op = new ColorConvertOp(cs, null);
            img = op.filter(img, null);
            ImageIO.write(img, "JPEG", tar);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * 根据图片转换为base64编码字符串
     * @param imgFile
     * @return
     */
    public static String base64Img(File img) {
    	InputStream in = null;
    	byte[] data = null;
        try {
            in = new FileInputStream(img);
            data = new byte[in.available()];
            in.read(data);
        } catch (IOException e) {
            e.printStackTrace();
        }finally{
        	try{
        		in.close();
        	}catch(Exception e){
        		e.printStackTrace();
        	}
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
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
	        final int BUFFER_SIZE = 2*1024;
	        final int EOF = -1;
	        int c;
	        byte[] buf = new byte[BUFFER_SIZE];
	        while (true) {
	            c = bis.read(buf);
	            if (c == EOF){
	                break;
	            }
	            baos.write(buf, 0, c);
	        }
	        data = baos.toByteArray();
	        baos.flush();
    	}catch(Exception e){
    		e.printStackTrace();
    	}finally{
    		try{
		        conn.disconnect();
		        is.close();
		        bis.close();
		        baos.close();
    		}catch(Exception e){
    			e.printStackTrace();
    		}
    	}
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }
    public static String base64(URL url) {
    	return base64Img(url);
    }
    /**
     * base64 图片转文件
     * @param imgStr
     * @param path
     * @return
     */
    public static boolean base64Img(File file, String str) {
    	if (null == str || null == file) return false;
    	File dir = file.getParentFile();
    	if(!dir.exists()){
    		dir.mkdirs();
    	}
    	str = str.replace("data:image/jpeg;base64,", "").replace("data:image/png;base64,", "");
    	OutputStream  out = null;
    	try {
    		BASE64Decoder decoder = new BASE64Decoder();
    		// 解密
    		byte[] b = decoder.decodeBuffer(str);
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
    		e.printStackTrace();
    		return false;
    	} finally{
    		if(null != out){
    			try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	}
    }
    public static boolean base64(File file, String str) {
    	return base64Img(file, str);
    } 
    /**
     * 获取文件宽高
     * @param img
     * @return
     */
	public static int[] size(File img){
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
			}catch(Exception e){
				
			}
		}
        int[] result = {width,height};
    	return result;
    }
	public static int width(File img){
		return size(img)[0];
	}
	public static int height(File img){
		return size(img)[1];
	}
}