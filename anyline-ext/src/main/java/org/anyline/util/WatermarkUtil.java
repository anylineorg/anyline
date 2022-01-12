/* 
 * Copyright 2006-2022 www.anyline.org
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
 *
 *          
 */


package org.anyline.util; 
 
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 
public class WatermarkUtil {
	protected static final Logger log = LoggerFactory.getLogger(WatermarkUtil.class); 
	private int degree = 0;								//旋转角度 
	private Color color = Color.LIGHT_GRAY;				//水印颜色 
	private String fontName = "宋体";						//字体名称 
	private int fontStyle = Font.BOLD + Font.ITALIC;	//字体样式 
	private int fontSize = 30;							//字体大小 
	private float alpha = 0.3f;							//透明度 
	private float x = 0;								//坐标X如果x<0,x=width+x 
	private float y = 0;								//坐标Y
	//xy按百分比
	 
	public WatermarkUtil(){ 
		this.x = 0; 
		this.y = 0; 
	}
	public WatermarkUtil(int x, int y){
		this.x = x;
		this.y = y;
	}
	public WatermarkUtil(float x, float y){
		this.x = x;
		this.y = y;
	}
	 
	 /** 
     * 给图片添加水印、可设置水印的旋转角度 
     * @param text  text
     * @param src  src
     * @param target  target
     * @return return
     */ 
    public boolean markText(String text, File src, File target) {
    	if(null == text || null == src || null == target || !src.exists()){
    		return false;
    	}
    	long fr = System.currentTimeMillis();
    	File dir = target.getParentFile();
    	if(!dir.exists()){
    		dir.mkdirs();
    	}
        // 主图片的路径 
        InputStream is = null; 
        OutputStream os = null;
        BufferedImage buffImg = null;
        try { 
            Image srcImg = ImageIO.read(src); 
            buffImg = new BufferedImage(srcImg.getWidth(null),srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
            int width = buffImg.getWidth();
            int height = buffImg.getHeight();
            
            // 得到画笔对象 
            Graphics2D g = buffImg.createGraphics(); 
            // 设置对线段的锯齿状边缘处理 
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR); 
            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0, 0, null); 
            if (degree != 0) { 
                // 设置水印旋转 
                g.rotate(Math.toRadians(degree),(double) width / 2, (double) height / 2); 
            } 
            // 设置颜色 
            g.setColor(color); 
            // 设置 Font 
            g.setFont(new Font(fontName, fontStyle, fontSize)); 
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,alpha));
            int[] offset = finalOffset(width,height); 
            g.drawString(text, offset[0], offset[1]); 
            g.dispose(); 
            os = new FileOutputStream(target); 
            // 生成图片// 生成图片
            String format = "jpg";
            if(target.getName().toLowerCase().endsWith("png")){
            	format = "png";
            } 
            ImageIO.write(buffImg, format, os); 
        } catch (Exception e) { 
            e.printStackTrace(); 
        } finally {
            try {
                if (null != buffImg)
                	buffImg.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (null != is)
                    is.close();
            } catch (Exception e) {
                e.printStackTrace();
            } 
            try { 
                if (null != os) 
                    os.close(); 
            } catch (Exception e) { 
                e.printStackTrace(); 
            } 
        }
        log.warn("[添加水印][耗时:{}][text:{}][src:{}][target:{}]",DateUtil.conversion(System.currentTimeMillis()-fr),text, src.getAbsoluteFile(), target.getAbsoluteFile());
        return true; 
    } 
    public void markText(String text, String src, String target){ 
    	markText(text, new File(src), new File(target)); 
    } 
    public void markText(String text, File src){ 
    	markText(text, src, src); 
    } 
    public void markText(String text, String src){ 
    	markText(text, src, src); 
    }
    /** 
     * 给图片添加水印、可设置水印图片旋转角度 
     * @param icon 水印图片路径 
     * @param iconWidth icon宽度
     * @param iconHeight icon高度 
     * @param src 源图片路径 
     * @param target 目标图片路径 
     */ 
    public void markIcon(File icon, int iconWidth, int iconHeight, File src, File target) { 
        OutputStream os = null;
        BufferedImage buffImg = null;
        long fr = System.currentTimeMillis();
        int _x = 0;
        int _y = 0;
        try {
            Image srcImg = ImageIO.read(src); 
            buffImg = new BufferedImage(srcImg.getWidth(null), srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);
            int width = buffImg.getWidth();
            int height = buffImg.getHeight();

            int icon_width = iconWidth;
            int icon_height = iconHeight;
            // 得到画笔对象 
            Graphics2D g = buffImg.createGraphics();
            buffImg = g.getDeviceConfiguration()
                    .createCompatibleImage(srcImg.getWidth(null), srcImg.getHeight(null), Transparency.TRANSLUCENT);
            //g.dispose();
            g = buffImg.createGraphics();
            // 设置对线段的锯齿状边缘处理 
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR); 
            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0, 0, null); 
            if (degree !=0) { 
                // 设置水印旋转 
                g.rotate(Math.toRadians(degree),(double) width / 2, (double) height / 2); 
            } 
            // 水印图象的路径 水印一般为gif或者png的，这样可设置透明度 
            ImageIcon imgIcon = new ImageIcon(icon.getAbsolutePath()); 
            // 得到Image对象。 
            Image img = imgIcon.getImage(); 
            float alpha = 0.5f; // 透明度 
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,alpha)); 
            int[] offset = finalOffset(width,height);
            _x = offset[0]-icon_width;
            _y = offset[1]-icon_height;
            if(_x<0){
            	_x = 0;
            }
            if(_y<0){
            	_y = 0;
            }
            g.drawImage(img, _x, _y, icon_width, icon_height, null);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER)); 
            g.dispose(); 
            os = new FileOutputStream(target); 
            // 生成图片
            String format = "jpg";
            if(target.getName().toLowerCase().endsWith("png")){
            	format = "png";
            }
            if ("jpg".equals(format)) { //重画一下，要么会变色
                BufferedImage tag;
                tag = new BufferedImage(buffImg.getWidth(), buffImg.getHeight(), BufferedImage.TYPE_INT_BGR);
                Graphics gg = tag.getGraphics();
                gg.drawImage(buffImg, 0, 0, null); // 绘制缩小后的图
                gg.dispose();
                buffImg = tag;
            }
            ImageIO.write(buffImg, format, os);
        } catch (Exception e) { 
            e.printStackTrace(); 
        } finally {
        	try{
        		buffImg.flush();
        	}catch(Exception e){
        	} 
            try { 
                if (null != os) 
                    os.close(); 
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
        }
        log.warn("[添加水印][耗时:{}][x:{}][y:{}][icon:{}][src:{}][target:{}]",DateUtil.conversion(System.currentTimeMillis()-fr), _x,_y,icon.getAbsoluteFile(), src.getAbsoluteFile(), target.getAbsoluteFile());
    }

    public void markIcon(File icon, File src, File target) {
        int[] iconOffset = ImgUtil.size(icon);
        int icon_width = iconOffset[0];
        int icon_height = iconOffset[1];
        markIcon(icon, icon_width, icon_height, src, target);
    }
    private int[] finalOffset(int width, int height){
    	 float x = this.x;
         float y = this.y;
         if(!NumberUtil.isInt(x)){
        	 x = width * x;
         }
         if(!NumberUtil.isInt(y)){
        	 y = height * y;
         }
         if(x<0){
        	 x = width + x;
         }
         if(y<0){
        	 y = height + y;
         }
     	int result[] = {(int)x,(int)y};
    	return result;
    }
    /**
     * 水印偏移
     * @param x x
     * @param y y
     */
    public void offset(int x, int y){
    	this.x = x;
    	this.y = y;
    }
    /**
     * 水印偏移
     * @param x  x%
     * @param y  y%
     */
    public void offset(float x, float y){
    	this.x = x;
    	this.y = y;
    }
    public void markIcon(String icon, String src, String target){ 
    	markIcon(new File(icon), new File(src), new File(target)); 
    } 
    public void markIcon(File icon, File src){ 
    	markIcon(icon, src, src); 
    } 
    public void markIcon(String icon, String src){ 
    	markIcon(icon, src, src); 
    } 
	public Integer getDegree() { 
		return degree; 
	} 
	public void setDegree(Integer degree) { 
		this.degree = degree; 
	} 
	public Color getColor() { 
		return color; 
	} 
	public void setColor(Color color) { 
		this.color = color; 
	} 
	public String getFontName() { 
		return fontName; 
	} 
	public void setFontName(String fontName) { 
		this.fontName = fontName; 
	} 
	public int getFontStyle() { 
		return fontStyle; 
	} 
	public void setFontStyle(int fontStyle) { 
		this.fontStyle = fontStyle; 
	} 
	public int getFontSize() { 
		return fontSize; 
	} 
	public void setFontSize(int fontSize) { 
		this.fontSize = fontSize; 
	} 
	public float getAlpha() { 
		return alpha; 
	} 
	public void setAlpha(float alpha) { 
		this.alpha = alpha; 
	}
} 
