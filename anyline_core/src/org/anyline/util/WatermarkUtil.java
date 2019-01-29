/* 
 * Copyright 2006-2015 www.anyline.org
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
 *          AnyLine以及一切衍生库 不得用于任何与网游相关的系统
 */


package org.anyline.util;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

public class WatermarkUtil {
	protected static final Logger log = Logger.getLogger(WatermarkUtil.class);
	private Integer degree;								//旋转角度
	private Color color = Color.LIGHT_GRAY;				//水印颜色
	private String fontName = "宋体";						//字体名称
	private int fontStyle = Font.BOLD + Font.ITALIC;	//字体样式
	private int fontSize = 30;							//字体大小
	private float alpha = 0.3f;							//透明度
	private Integer x;				//坐标X
	private Integer y;				//坐标Y
	
	public WatermarkUtil(){
		this.x = 0;
		this.y = 0;
	}
	public WatermarkUtil(int x, int y){
		this.x = x;
		this.y = y;
	}
	 /**
     * 给图片添加水印、可设置水印的旋转角度
     * @param text
     * @param src
     * @param target
     * @param degree
     */
    public void markText(String text, File src, File target) {
        // 主图片的路径
        InputStream is = null;
        OutputStream os = null;
        try {
            Image srcImg = ImageIO.read(src);
            BufferedImage buffImg = new BufferedImage(srcImg.getWidth(null),srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);

            // 得到画笔对象
            Graphics2D g = buffImg.createGraphics();

            // 设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0, 0, null);

            if (null != degree) {
                // 设置水印旋转
                g.rotate(Math.toRadians(degree),(double) buffImg.getWidth() / 2, (double) buffImg.getHeight() / 2);
            }

            // 设置颜色
            g.setColor(color);

            // 设置 Font
            g.setFont(new Font(fontName, fontStyle, fontSize));

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,alpha));

            // 第一参数->设置的内容，后面两个参数->文字在图片上的坐标位置(x,y) .
            g.drawString(text, x, y);

            g.dispose();

            os = new FileOutputStream(target);

            // 生成图片
            ImageIO.write(buffImg, "JPG", os);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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
     * @param src 源图片路径
     * @param target 目标图片路径
     * @param degree 水印图片旋转角度
     */
    public void markIcon(File icon, File src, File target) {
        OutputStream os = null;
        try {
            Image srcImg = ImageIO.read(src);

            BufferedImage buffImg = new BufferedImage(srcImg.getWidth(null),
                    srcImg.getHeight(null), BufferedImage.TYPE_INT_RGB);

            // 得到画笔对象
            Graphics2D g = buffImg.createGraphics();

            // 设置对线段的锯齿状边缘处理
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            g.drawImage(srcImg.getScaledInstance(srcImg.getWidth(null), srcImg.getHeight(null), Image.SCALE_SMOOTH), 0, 0, null);

            if (null != degree) {
                // 设置水印旋转
                g.rotate(Math.toRadians(degree),(double) buffImg.getWidth() / 2, (double) buffImg.getHeight() / 2);
            }

            // 水印图象的路径 水印一般为gif或者png的，这样可设置透明度
            ImageIcon imgIcon = new ImageIcon(icon.getAbsolutePath());

            // 得到Image对象。
            Image img = imgIcon.getImage();

            float alpha = 0.5f; // 透明度
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP,alpha));

            // 表示水印图片的位置
            g.drawImage(img, 150, 300, null);

            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));

            g.dispose();

            os = new FileOutputStream(target);

            // 生成图片
            ImageIO.write(buffImg, "JPG", os);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != os)
                    os.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
	public Integer getX() {
		return x;
	}
	public void setX(Integer x) {
		this.x = x;
	}
	public Integer getY() {
		return y;
	}
	public void setY(Integer y) {
		this.y = y;
	}

}
